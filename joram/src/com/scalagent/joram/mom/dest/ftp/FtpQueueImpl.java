/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2007 ScalAgent Distributed Technologies
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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import org.objectweb.joram.mom.dest.DeadMQueueImpl;
import org.objectweb.joram.mom.dest.QueueImpl;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Debug;

/**
 * The <code>FtpQueueImpl</code> class implements the MOM queue behaviour,
 * basically storing messages and delivering them upon clients requests.
 */
public class FtpQueueImpl extends QueueImpl {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public static Logger logger = Debug.getLogger(FtpQueueImpl.class.getName());
  
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
      logger.log(BasicLevel.ERROR, 
                 "FtpQueueImpl : transfer = null" ,exc);
    }
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this +
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

  public void ftpNot(FtpNot not) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this +
                 " ftpNot(" + not + ")\n" +
                 "transferTable = " + transferTable);
    Message msg = (Message) ((Vector) not.getMessages()).get(0);
    storeMessage(new org.objectweb.joram.mom.messages.Message(msg));
    deliverMessages(0);
    transferTable.remove(new FtpMessage(msg).getIdentifier());

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "--- " + this + " doProcess : transferTable = " + transferTable);
  }

  public ClientMessages preProcess(AgentId from, ClientMessages not) {
    for (Enumeration msgs = not.getMessages().elements();
         msgs.hasMoreElements();) {
      Message msg = (Message) msgs.nextElement();
      if (isFtpMsg(msg)) {
        doProcessFtp(not,msg);
        not.getMessages().remove(msg);
      }
    }
    if (not.getMessages().size() > 0) {
      return not;
    }
    return null;
  }

  protected boolean isFtpMsg(Message message) {
    FtpMessage msg = new FtpMessage(message);
    return (msg.propertyExists(SharedObj.url) &&
            msg.propertyExists(SharedObj.crc) &&
            msg.propertyExists(SharedObj.ack));
  }

  protected void doProcessFtp(ClientMessages not,
                              Message msg) {

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "--- " + this + " doProcessFtp(" + not + "," + msg + ")");

    if (transfer != null) {
      dmq = not.getDMQId();
      if (dmq == null && super.dmqId != null)
        dmq = super.dmqId;
      else if ( dmq == null)
        dmq = DeadMQueueImpl.getId();

      clientContext = not.getClientContext();
      requestId = not.getRequestId();

      FtpMessage ftpMsg = new FtpMessage(msg);
      transferTable.put(ftpMsg.getIdentifier(),ftpMsg);
      FtpThread t = new FtpThread(transfer,
                                  (FtpMessage) ftpMsg.clone(),
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
      logger.log(BasicLevel.ERROR, 
                 "readObject : transfer = null" ,exc);
    }
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this +
                 " readObject transfer = "+ transfer);

    if (transfer != null) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "--- " + this +
                   " readObject : transferTable = " + transferTable);

      for (Enumeration e = transferTable.elements(); e.hasMoreElements(); ) {
        Message msg = (Message) e.nextElement();
        FtpMessage ftpMsg = new FtpMessage(msg);
        FtpThread t = new FtpThread(transfer,
                                    (FtpMessage) ftpMsg.clone(),
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
