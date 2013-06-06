/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import org.objectweb.joram.mom.dest.Queue;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.util.DMQManager;
import org.objectweb.joram.shared.MessageErrorConstants;
import org.objectweb.joram.shared.excepts.AccessException;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Debug;
import fr.dyade.aaa.agent.Notification;

/**
 * The <code>FtpQueue</code> class implements the MOM queue behaviour,
 * basically storing messages and delivering them upon clients requests.
 */
public class FtpQueue extends Queue {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  public static Logger logger = Debug.getLogger(FtpQueue.class.getName());
  
  private String user;
  private String pass;
  private String path;
  private transient TransferItf transfer;
  private AgentId dmq;
  private int clientContext;
  private int requestId;

  public String ftpImplName;

  private Hashtable transferTable;

  public FtpQueue() {
    fixed = true;
  }

  /**
   * Configures a <code>FtpQueue</code> instance.
   * 
   * @param prop The initial set of properties.
   */
  public void setProperties(Properties prop, boolean firstTime) throws Exception {
    super.setProperties(prop, firstTime);
    user = "anonymous";
    pass = "no@no.no";
    path = null;
    ftpImplName = "com.scalagent.joram.mom.dest.ftp.TransferImplRef";
    if (prop != null) {
      user = prop.getProperty("user", user);
      pass = prop.getProperty("pass", pass);
      path = prop.getProperty("path", path);
      ftpImplName = prop.getProperty("ftpImpl", ftpImplName);
    }
  }

  /**
   * Initializes the destination.
   * 
   * @param firstTime   true when first called by the factory
   */
  public void initialize(boolean firstTime) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "initialize(" + firstTime + ')');
    
    super.initialize(firstTime);

    try {
      if ((ftpImplName != null) && (ftpImplName.length() > 0))
        transfer = (TransferItf) Class.forName(ftpImplName).newInstance();
    } catch (Exception exc) {
      transfer = null;
      logger.log(BasicLevel.ERROR, 
                  "--- " + this + " initialize : transfer = null" ,exc);
    }

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                  "--- " + this + " initialize transfer = "+ transfer);

    if (transfer != null) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
                   "--- " + this + " initialize : transferTable = " + transferTable);

      if (transferTable != null) {
        for (Enumeration e = transferTable.elements(); e.hasMoreElements(); ) {
          Message msg = (Message) e.nextElement();
          FtpMessage ftpMsg = new FtpMessage(msg);
          FtpThread t = new FtpThread(transfer,
              (FtpMessage) ftpMsg.clone(),
              getId(),
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

  public void react(AgentId from, Notification not) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "FtpQueue.react(" + from + ',' + not + ')');
    if (not instanceof FtpNot) {
      ftpNot((FtpNot) not);
    } else
      super.react(from, not);
  }

  public String toString() {
    return "FtpQueue:" + getId().toString();
  }

  private void ftpNot(FtpNot not) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this +
                 " ftpNot(" + not + ")\n" +
                 "transferTable = " + transferTable);
    Message msg = (Message) not.getMessages().get(0);
    try {
      storeMessage(new org.objectweb.joram.mom.messages.Message(msg), false);
    } catch (AccessException e) {/* never happens */}
    deliverMessages(0);
    transferTable.remove(new FtpMessage(msg).getIdentifier());

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "--- " + this + " doProcess : transferTable = " + transferTable);
  }

  public ClientMessages preProcess(AgentId from, ClientMessages not) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "--- " + this + " preProcess : not.getMessages = " + not.getMessages().size());

    for (Iterator msgs = not.getMessages().iterator(); msgs.hasNext();) {
      Message msg = (Message) msgs.next();
      if (isFtpMsg(msg)) {
        doProcessFtp(not, msg);
        msgs.remove();
      }
    }

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "--- " + this + " preProcess : not.getMessages = " + not.getMessages().size());

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

  private void doProcessFtp(ClientMessages not,
                              Message msg) {

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "--- " + this + " doProcessFtp(" + not + "," + msg + ")");

    if (transfer != null) {
      dmq = not.getDMQId();
      if (dmq == null && super.dmqId != null)
        dmq = super.dmqId;
      else if ( dmq == null)
        dmq = Queue.getDefaultDMQId();

      clientContext = not.getClientContext();
      requestId = not.getRequestId();

      FtpMessage ftpMsg = new FtpMessage(msg);
      transferTable.put(ftpMsg.getIdentifier(),ftpMsg);
      
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this + " doProcessFtp: launch FtpThread (" + transferTable.size() + ')');
    
      FtpThread t = new FtpThread(transfer,
                                  (FtpMessage) ftpMsg.clone(),
                                  getId(),
                                  dmq,
                                  clientContext,
                                  requestId,
                                  user,
                                  pass,
                                  path);
      t.start();
    } else {
      DMQManager dmqManager = new DMQManager(not.getDMQId(), dmqId, getId());
      nbMsgsSentToDMQSinceCreation++;
      dmqManager.addDeadMessage(msg, MessageErrorConstants.UNEXPECTED_ERROR);
      dmqManager.sendToDMQ();
    }
  }
  
  public int getEncodableClassId() {
    // Not defined: still not encodable
    return -1;
  }
  
}
