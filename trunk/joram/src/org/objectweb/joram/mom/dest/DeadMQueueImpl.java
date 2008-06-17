/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.mom.dest;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import org.objectweb.joram.mom.messages.Message;
import org.objectweb.joram.mom.notifications.AcknowledgeRequest;
import org.objectweb.joram.mom.notifications.BrowseReply;
import org.objectweb.joram.mom.notifications.BrowseRequest;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.notifications.DenyRequest;
import org.objectweb.joram.mom.notifications.QueueMsgReply;
import org.objectweb.joram.mom.notifications.ReceiveRequest;
import org.objectweb.joram.mom.notifications.SetDMQRequest;
import org.objectweb.joram.mom.notifications.SetThreshRequest;
import org.objectweb.joram.mom.notifications.TopicMsgsReply;
import org.objectweb.joram.shared.excepts.AccessException;
import org.objectweb.joram.shared.selectors.Selector;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.ExpiredNot;
import fr.dyade.aaa.agent.UnknownAgent;
import fr.dyade.aaa.util.Debug;

/**
 * The <code>DeadMQueueImpl</code> class implements the MOM dead message queue
 * behavior, basically storing dead messages and delivering them upon clients
 * requests.
 */
public class DeadMQueueImpl extends QueueImpl {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  /** Static value holding the default DMQ identifier for a server. */
  static AgentId defaultDMQId = null;
  /** Static value holding the default threshold for a server. */
  static Integer threshold = null;
  
  public static Logger logger = Debug.getLogger(DeadMQueueImpl.class.getName());

  /**
   * Constructs a <code>DeadMQueueImpl</code> instance.
   *
   * @param adminId  Identifier of the administrator of the queue.
   * @param prop     The initial set of properties.
   */
  public DeadMQueueImpl(AgentId adminId, Properties prop) {
    super(adminId, prop);
    setFreeWriting(true);
  }

  public String toString() {
    return "DeadMQueueImpl:" + getId().toString();
  }

  /** Static method returning the default DMQ identifier. */
  public static AgentId getDefaultDMQId() {
    return defaultDMQId;
  }
  
  /** Static method returning the default threshold. */
  public static Integer getDefaultThreshold() {
    return threshold;
  }

  /**
   * Overrides this <code>DestinationImpl</code> method; this request is
   * not expected by a dead message queue.
   *
   * @exception AccessException  Not thrown.
   */
  public void setDMQRequest(AgentId from, SetDMQRequest req) throws AccessException {
    if (logger.isLoggable(BasicLevel.WARN))
      logger.log(BasicLevel.WARN, "Unexpected request: " + req);
  }
  
  /**
   * Overrides this <code>DestinationImpl</code> method; the messages carried
   * by the <code>ClientMessages</code> instance are stored in their arrival
   * order, WRITE right is not checked.
   */
  public ClientMessages preProcess(AgentId from, ClientMessages not) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Preprocess on the dead message queue: " + not);
    doMessages(not.getMessages());
    return null;
  }

  private void doMessages(Vector msgs) {
    // Getting and persisting the messages:
    Message msg;
    Enumeration enu = msgs.elements();

    // Storing each received message:
    while (enu.hasMoreElements()) {
      msg = new Message((org.objectweb.joram.shared.messages.Message) enu.nextElement());
      msg.setExpiration(0L);
      msg.order = arrivalsCounter++;
      messages.add(msg);
      nbMsgsReceiveSinceCreation++;
      // Persisting the message.
      setMsgTxName(msg);
      msg.save();
    }
  }
  
  /**
   * Overrides this <code>QueueImpl</code> method; this request is
   * not expected by a dead message queue.
   *
   * @exception AccessException  Not thrown.
   */
  public void setThreshRequest(AgentId from, SetThreshRequest req)
                 throws AccessException {
    if (logger.isLoggable(BasicLevel.WARN))
      logger.log(BasicLevel.WARN, "Unexpected request: " + req);
  }

  /**
   * Overrides this <code>QueueImpl</code> method; messages matching the
   * request's selector are actually sent as a reply; no cleaning nor DMQ
   * sending is done.
   * 
   * @exception AccessException
   *                If the requester is not a reader.
   */
  public void browseRequest(AgentId from, BrowseRequest not)
                 throws AccessException {
    // If client is not a reader, throwing an exception.
    if (! isReader(from))
      throw new AccessException("READ right not granted");

    // Building the reply:
    BrowseReply rep = new BrowseReply(not);

    // Adding the messages to it:
    Message message;
    for (int i = 0; i < messages.size(); i++) {
      message = (Message) messages.get(i);
      // Message matching the selector: adding it.
      if (Selector.matches(message.getHeaderMessage(), not.getSelector()))
        rep.addMessage(message.getFullMessage());
    }
    // Delivering the reply:
    forward(from, rep);

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Request answered.");
  }

  /**
   * Overrides this <code>QueueImpl</code> method;
   * <code>AcknowledgeRequest</code> requests are actually not processed
   * in dead message queues.
   */
  public void acknowledgeRequest(AgentId from, AcknowledgeRequest not) {}
 
  /**
   * Overrides this <code>QueueImpl</code> method;
   * <code>DenyRequest</code> requests are actually not processed
   * in dead message queues.
   */
  public void denyRequest(AgentId from, DenyRequest not) {}

  /**
   * Overrides this <code>QueueImpl</code> method; if the sent notification
   * was a <code>QueueMsgReply</code> instance, putting the sent message back
   * in queue.
   */
  protected void doUnknownAgent(UnknownAgent uA) {
    Notification not = uA.not;

    // If the notification is not a delivery, doing nothing. 
    if (! (not instanceof QueueMsgReply))
      return;

    // Putting the message back in queue:
    Vector msgList = ((QueueMsgReply) not).getMessages();
    for (int i = 0; i < msgList.size(); i++) {
      Message msg = (Message)msgList.elementAt(i);
      msg.order = arrivalsCounter++;
      messages.add(msg);
      // Persisting the message.
      setMsgTxName(msg);
      msg.save();
    }
    // Launching a delivery sequence:
    deliverMessages(0); 
  }

  /**
   * Overrides this <code>QueueImpl</code> method; delivered messages are not
   * kept for acknowledgement or denying; validity of messages is
   * not checked and message fields are not updated; also, no sending to
   * any DMQ.
   */ 
  protected void deliverMessages(int index) {
    ReceiveRequest notRec = null;
    boolean replied;
    int j = 0;
    Message message;
    QueueMsgReply notMsg;

    long current = System.currentTimeMillis();
    cleanWaitingRequest(current);

    // Processing each request as long as there are deliverable messages:
    while (! messages.isEmpty() && index < requests.size()) { 
      notRec = (ReceiveRequest) requests.get(index);
      replied = false;
      notMsg = new QueueMsgReply(notRec);

      // Checking the deliverable messages:
      while (j < messages.size()) {
        message = (Message) messages.get(j);
        
        // If the selector matches, sending it:
        if (Selector.matches(message.getHeaderMessage(), notRec.getSelector())) {
          notMsg.addMessage(message.getFullMessage());
          
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "Message " + message.getIdentifier() + " sent to "
                + notRec.requester + " as a reply to " + notRec.getRequestId());

          // Removing the message:
          messages.remove(j);
          message.delete();
          // Removing the request.
          replied = true;
          nbMsgsDeliverSinceCreation++;
          requests.remove(index);
          break;
        }
        // If selector does not match: going on
        else
          j++;
      }

      if (notMsg.getSize() > 0) {
        forward(notRec.requester, notMsg);
      }

      // Next request:
      j = 0;
      if (! replied)
        index++;
    }
  }

  /** 
   * Overwrites this <code>DestinationImpl</code> method so that no messages
   * may be sent by the DMQ to itself.
   */
  protected void sendToDMQ(Vector deadMessages, AgentId dmqId) {}

  protected void handleExpiredNot(AgentId from, ExpiredNot not) {
    Notification expiredNot = not.getExpiredNot();
    if (expiredNot instanceof ClientMessages) {
      doMessages(((ClientMessages) expiredNot).getMessages());
    } else if (expiredNot instanceof TopicMsgsReply) {
      doMessages(((TopicMsgsReply) expiredNot).getMessages());
    } else {
      doMessages(((QueueMsgReply) expiredNot).getMessages());
    }
  }
}
