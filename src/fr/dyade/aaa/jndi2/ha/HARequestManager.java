/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 *
 * Initial developer(s): David Feliot
 */
package fr.dyade.aaa.jndi2.ha;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.BagSerializer;
import fr.dyade.aaa.jndi2.msg.JndiReply;
import fr.dyade.aaa.jndi2.server.LifeCycleListener;
import fr.dyade.aaa.jndi2.server.RequestManager;
import fr.dyade.aaa.jndi2.server.TcpRequestNot;
import fr.dyade.aaa.jndi2.server.Trace;

public class HARequestManager 
    implements LifeCycleListener, BagSerializer, java.io.Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public static final int IDEMPOTENT = -2;
  public static final int NOT_IDEMPOTENT = -1;

  public static final String HA_REQUEST_COUNTER = "haRequestCounter";

  private transient int requestCounter;

  private transient Hashtable requests;

  private RequestManager manager;

  public void setRequestManager(RequestManager manager) {
    this.manager = manager;
  }

  public void agentInitialize(boolean firstTime) throws Exception {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "HARequestManager.agentInitialize(" + firstTime + ')');
    Integer counter = (Integer)AgentServer.getTransaction().load(
      HA_REQUEST_COUNTER);
    if (counter == null) {
      requestCounter = 0;
    } else {
      requestCounter = counter.intValue();
    }
    requests = new Hashtable();
    manager.agentInitialize(firstTime);
  }

  public void agentFinalize(boolean lastTime) {
    manager.agentFinalize(lastTime);
  }

  void doReact(GetRequestIdNot not) {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "HARequestManager.doReact((GetRequestIdNot)" + 
                       not + ')');
    int id = requestCounter;
    requestCounter++;
    saveRequestCounter();

    // Notice that the counter is actually saved
    // at the end of the reaction. If a failure occurs
    // before the end of the reaction, the id may have been
    // delivered whereas the counter is not saved. So after
    // a recovery, the same identifier may be given again.
    // But a HA server cannot be recovered (it is transient).
    // If it is recoverable then the counter storage must
    // be synchronous.
    not.Return(id);
  }

  void doReact(TcpRequestNot not) throws Exception {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "HARequestManager.doReact((TcpRequestNot)" + 
                       not + ')');
    HARequestContext reqCtx = 
      (HARequestContext)not.getRequestContext();
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       " -> request id = " + reqCtx.getId());
    if (reqCtx.getId() == IDEMPOTENT) {
      JndiReply reply = manager.invoke(reqCtx);
      reqCtx.reply(reply);
    } else {
      Integer reqId = new Integer(reqCtx.getId());
      HARequestContext recoveredReqCtx = 
        (HARequestContext)requests.get(reqId);
      if (recoveredReqCtx == null) {        
        requests.put(new Integer(requestCounter), reqCtx);
        JndiReply reply = manager.invoke(reqCtx);
        reqCtx.reply(reply);
      } else {
        JndiReply reply = recoveredReqCtx.getReply();
        if (reply == null) {
          recoveredReqCtx.recover(
            reqCtx);
        } else {
          reqCtx.reply(reply);
        }
      }
    }
  }

  void removeContext(int id) {
    requests.remove(new Integer(id));
  }

  private void saveRequestCounter() {
    try {
      AgentServer.getTransaction().save(
        new Integer(requestCounter), HA_REQUEST_COUNTER);
    } catch (IOException exc) {
      throw new Error(exc.toString());
    }
  }

  public void writeBag(ObjectOutputStream out)
    throws IOException {
    out.writeInt(requestCounter);
    out.writeObject(requests);
    manager.writeBag(out);
  }

  public void readBag(ObjectInputStream in) 
    throws IOException, ClassNotFoundException {
    requestCounter = in.readInt();
    requests = (Hashtable)in.readObject();
    manager.readBag(in);
  }
}
