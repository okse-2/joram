/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.client.jms.ha.local;

import org.objectweb.joram.client.jms.*;
import org.objectweb.joram.client.jms.Connection;
import org.objectweb.joram.client.jms.local.*;
import org.objectweb.joram.shared.client.*;
import org.objectweb.joram.mom.MomTracing;
import org.objectweb.joram.mom.proxies.*;
import org.objectweb.joram.mom.notifications.*;
import org.objectweb.joram.client.jms.connection.RequestChannel;

import fr.dyade.aaa.agent.*;

import javax.jms.*;
import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;

public class HALocalConnection 
    implements RequestChannel {
  
  public final static int NONE = 0;
  public final static int INIT = 1;
  public final static int RUN = 2;

  private static Object lock = new Object();

  private static int status;
  
  public static void init(String args, boolean firstTime) 
    throws Exception {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG,
        "HALocalConnection.init(" + 
        args + ',' + firstTime + ')');

    synchronized (lock) {
      status = INIT;
      lock.notifyAll();
    }
  }

  public static void waitForStart() {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG,
        "HALocalConnection.waitForStart()");
    synchronized (lock) {
      while (status == NONE) {      
	try {
	  lock.wait();
	} catch (InterruptedException exc) {}
      }
     
      if (status == INIT) {
        // Clean the proxies
        GetProxyIdListNot gpin = new GetProxyIdListNot();
        AgentId[] proxyIds;
        try {
          gpin.invoke(new AgentId(AgentServer.getServerId(),
                                  AgentServer.getServerId(),
                                  AgentId.JoramAdminStamp));
          proxyIds = gpin.getIds();
          ResetCollocatedConnectionsNot rccn = 
          new ResetCollocatedConnectionsNot();
          for (int i = 0; i < proxyIds.length; i++) {
            Channel.sendTo(proxyIds[i], rccn);
          }
          status = RUN;
        } catch (Exception exc) {
          JoramTracing.dbgClient.log(
            BasicLevel.ERROR, "", exc);
          throw new Error(exc.toString());
        }
      }
    }
  }

  private LocalConnection localConnection;

  public HALocalConnection(
    String userName, String password) throws JMSException {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG,
        "HALocalConnection.<init>(" + 
        userName + ',' + password + ')');
    waitForStart();
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG,
        " -> create the local connection");
    localConnection = new LocalConnection(userName, password);
  }
  
  public void send(AbstractJmsRequest request) 
    throws Exception {
    localConnection.send(request);
  }

  public AbstractJmsReply receive() 
    throws Exception {
    return localConnection.receive();
  }

  public void close() {
    localConnection.close();
  }
}
