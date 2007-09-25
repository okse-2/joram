/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2006 ScalAgent Distributed Technologies
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
 * Contributor(s): 
 */
package com.scalagent.joram.mom.dest.ftp;

import java.io.IOException;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;

import org.objectweb.joram.shared.excepts.*;
import org.objectweb.joram.shared.messages.*;
import org.objectweb.joram.shared.selectors.*;
import org.objectweb.joram.mom.dest.*;
import org.objectweb.joram.mom.notifications.*;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.joram.mom.MomTracing;

/**
 * The <code>FtpQueueImpl</code> class implements the MOM queue behaviour,
 * basically storing messages and delivering them upon clients requests.
 */
public class FtpQueueImpl extends QueueImpl {

  private String user = "anonymous";
  private String pass = "no@no.no";
  private String path = null;
  private transient TransferItf transfer = null;
  private ClientMessages currentNot = null;
  private AgentId dmq = null;
  private int clientContext;
  private int requestId;

  public String ftpImplName = "com.scalagent.joram.mom.dest.ftp.TransferImplRef";

  private Hashtable transferTable;
  /**
   * Constructs a <code>FtpQueueImpl</code> instance.
   *
   * @param destId  Identifier of the agent hosting the queue.
   * @param adminId  Identifier of the administrator of the queue.
   * @param prop     The initial set of properties.
   */
  public FtpQueueImpl(AgentId destId, 
                      AgentId adminId,
                      Properties prop) {
    super(destId, adminId, prop);
    setProperties(prop);

    transferTable = new Hashtable();
    this.path = path;
    if (user != null)
      this.user = user;
    if (pass != null)
      this.pass = pass;
    try {
      if ((ftpImplName != null) && (ftpImplName.length() > 0))
        transfer = (TransferItf) Class.forName(ftpImplName).newInstance();
    } catch (Exception exc) {
      transfer = null;
      MomTracing.dbgDestination.log(BasicLevel.ERROR, 
                                    "FtpQueueImpl : transfer = null" ,exc);
    }
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "--- " + this +
                                    " transfer = "+ transfer);
  }

  protected void setProperties(Properties prop) {
    user = prop.getProperty("user", user);
    pass = prop.getProperty("pass", pass);
    path = prop.getProperty("path", path);
    ftpImplName = prop.getProperty("ftpImpl", ftpImplName);
  }

  public String toString() {
    return "FtpQueueImpl:" + destId.toString();
  }

  /**
   * The <code>DestinationImpl</code> class calls this method for passing
   * notifications which have been partly processed, so that they are
   * specifically processed by the <code>FtpQueueImpl</code> class.
   */
  protected void specialProcess(Notification not) {
    if (not instanceof FtpNot) {
      doProcess((FtpNot) not);
    } else if (not instanceof ClientMessages)
      doProcess((ClientMessages) not);
  }

  protected void doProcess(FtpNot not) {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "--- " + this +
                                    " doProcess(" + not + ")\n" +
                                    "transferTable = " + transferTable);
    Message msg = (Message) ((Vector) not.getMessages()).get(0);
    storeMessage(msg);
    deliverMessages(0);
    transferTable.remove(msg.getIdentifier());

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "--- " + this +
                                    " doProcess : transferTable = " + transferTable);
  }

  protected void doProcess(ClientMessages not) {
    for (Enumeration msgs = not.getMessages().elements();
         msgs.hasMoreElements();) {
      Message msg = (Message) msgs.nextElement();
      if (isFtpMsg(msg))
        doProcessFtp(not,msg);
      else {
        storeMessage(msg);
        deliverMessages(0);
      }
    }
  }

  protected boolean isFtpMsg(Message msg) {
    return (msg.propertyExists(SharedObj.url) &&
            msg.propertyExists(SharedObj.crc) &&
            msg.propertyExists(SharedObj.ack));
  }

  protected void doProcessFtp(ClientMessages not,
                              Message msg) {

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "--- " + this +
                                    " doProcessFtp(" + not + "," + msg + ")");

    if (transfer != null) {
      dmq = not.getDMQId();
      if (dmq == null && super.dmqId != null)
        dmq = super.dmqId;
      else if ( dmq == null)
        dmq = DeadMQueueImpl.getId();

      clientContext = not.getClientContext();
      requestId = not.getRequestId();

      transferTable.put(msg.getIdentifier(),msg);

      FtpThread t = new FtpThread(transfer,
                                  (Message) msg.clone(),
                                  destId,
                                  dmq,
                                  clientContext,
                                  requestId,
                                  user,
                                  pass,
                                  path);
      t.start();
    } else {
      ClientMessages deadM = 
        new ClientMessages(not.getClientContext(), 
                           not.getRequestId());
      
      msg.notWriteable = true;
      deadM.addMessage(msg);
      sendToDMQ(deadM,not.getDMQId());
    }
  }

  private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException {
    in.defaultReadObject();

    try {
      if ((ftpImplName != null) && (ftpImplName.length() > 0))
        transfer = (TransferItf) Class.forName(ftpImplName).newInstance();
    } catch (Exception exc) {
      transfer = null;
      MomTracing.dbgDestination.log(BasicLevel.ERROR, 
                                    "readObject : transfer = null" ,exc);
    }
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "--- " + this +
                                    " readObject transfer = "+ transfer);

    if (transfer != null) {
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, "--- " + this +
                                      " readObject : transferTable = " + transferTable);

      for (Enumeration e = transferTable.elements(); e.hasMoreElements(); ) {
        Message msg = (Message) e.nextElement();
        FtpThread t = new FtpThread(transfer,
                                    (Message) msg.clone(),
                                    destId,
                                    dmq,
                                    clientContext,
                                    requestId,
                                    user,
                                    pass,
                                    path);
        t.start();
      }
    }
  }
}
