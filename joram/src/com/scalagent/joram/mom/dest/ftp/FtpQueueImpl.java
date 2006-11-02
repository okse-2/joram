/*
 * Copyright (C) 2003 - ScalAgent Distributed Technologies
 *
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package com.scalagent.joram.mom.dest.ftp;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;
import org.objectweb.joram.mom.MomTracing;
import org.objectweb.joram.mom.notifications.*;
import org.objectweb.joram.shared.excepts.*;
import org.objectweb.joram.shared.messages.*;
import org.objectweb.joram.shared.selectors.*;
import org.objectweb.joram.mom.dest.*;

import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;
import java.io.IOException;

import org.objectweb.util.monolog.api.BasicLevel;


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
   */
  public FtpQueueImpl(AgentId destId, 
                      AgentId adminId,
                      String user,
                      String pass,
                      String path) {
    super(destId, adminId);
    ftpImplName = System.getProperty("ftpImpl","com.scalagent.joram.mom.dest.ftp.TransferImplRef");
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
