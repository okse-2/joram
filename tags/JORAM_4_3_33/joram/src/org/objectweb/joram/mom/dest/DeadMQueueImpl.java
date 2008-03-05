/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2006 ScalAgent Distributed Technologies
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
import java.util.Vector;
import java.util.Properties;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.UnknownAgent;
import org.objectweb.joram.mom.notifications.*;
import org.objectweb.joram.shared.excepts.*;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.joram.shared.selectors.Selector;

import org.objectweb.joram.mom.MomTracing;
import org.objectweb.util.monolog.api.BasicLevel;

/**
 * The <code>DeadMQueueImpl</code> class implements the MOM dead message queue
 * behaviour, basically storing dead messages and delivering them upon clients
 * requests.
 */
public class DeadMQueueImpl extends QueueImpl {
  /** Static value holding the default DMQ identifier for a server. */
  static AgentId id = null;
  /** Static value holding the default threshold for a server. */
  static Integer threshold = null;

  /**
   * Constructs a <code>DeadMQueueImpl</code> instance.
   *
   * @param destId  Identifier of the agent hosting the queue.
   * @param adminId  Identifier of the administrator of the queue.
   * @param prop     The initial set of properties.
   */
  public DeadMQueueImpl(AgentId destId, AgentId adminId, Properties prop) {
    super(destId, adminId, prop);
  }


  public String toString() {
    return "DeadMQueueImpl:" + destId.toString();
  }


  /** Static method returning the default DMQ identifier. */
  public static AgentId getId() {
    return id;
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
  protected void doReact(AgentId from, SetDMQRequest req)
                 throws AccessException {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
      MomTracing.dbgDestination.log(BasicLevel.WARN,
                                    "Unexpected request: " + req);
  }
  
  /**
   * Overrides this <code>DestinationImpl</code> method; the messages carried 
   * by the <code>ClientMessages</code> instance are stored in their arrival
   * order, WRITE right is not checked.
   *
   * @exception AccessException  Never thrown.
   */
  protected void doReact(AgentId from, ClientMessages not)
                 throws AccessException {
    // Getting and persisting the messages:
    Message msg;
    for (Enumeration msgs = not.getMessages().elements();
         msgs.hasMoreElements();) {
      msg = (Message) msgs.nextElement();
      // Be careful, the message has been saved by its initial destination,
      // resets the saved flag.
      msg.getMessageBody().saved = false;
      msg.setExpiration(0L);
      messages.add(msg);
      msg.save(getDestinationId());
    }
    // Lauching a delivery sequence:
    deliverMessages(0);
  }

  /**
   * Overrides this <code>QueueImpl</code> method; this request is
   * not expected by a dead message queue.
   *
   * @exception AccessException  Not thrown.
   */
  protected void doReact(AgentId from, SetThreshRequest req)
                 throws AccessException {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
      MomTracing.dbgDestination.log(BasicLevel.WARN,
                                    "Unexpected request: " + req);
  }

  /**
   * Overrides this <code>QueueImpl</code> method; messages matching the
   * request's selector are actually sent as a reply; no cleaning nor DMQ
   * sending is done.
   *
   * @exception AccessException  If the requester is not a reader.
   */
  protected void doReact(AgentId from, BrowseRequest not)
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
      if (Selector.matches(message, not.getSelector()))
        rep.addMessage(message);
    }
    // Delivering the reply:
    Channel.sendTo(from, rep);

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Request answered.");
  }

  /**
   * Overrides this <code>QueueImpl</code> method;
   * <code>AcknowledgeRequest</code> requests are actually not processed
   * in dead message queues.
   */
  protected void doReact(AgentId from, AcknowledgeRequest not) {}
 
  /**
   * Overrides this <code>QueueImpl</code> method;
   * <code>DenyRequest</code> requests are actually not processed
   * in dead message queues.
   */
  protected void doReact(AgentId from, DenyRequest not) {}

  /**
   * Overrides this <code>QueueImpl</code> method; if the sent notification
   * was a <code>QueueMsgReply</code> instance, putting the sent message back
   * in queue.
   */
  protected void doProcess(UnknownAgent uA) {
    AgentId client = uA.agent;
    Notification not = uA.not;

    // If the notification is not a delivery, doing nothing. 
    if (! (not instanceof QueueMsgReply))
      return;

    // Putting the message back in queue:
    Vector msgList = ((QueueMsgReply) not).getMessages();
    for (int i = 0; i < msgList.size(); i++) {
      Message msg = (Message)msgList.elementAt(i);
      messages.add(msg);
      msg.save(getDestinationId());
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
    Message msg;
    QueueMsgReply notMsg;

    // Processing each request as long as there are deliverable messages:
    while (! messages.isEmpty() && index < requests.size()) { 
      notRec = (ReceiveRequest) requests.get(index);
      replied = false;
      notMsg = new QueueMsgReply(notRec);

      // Checking the deliverable messages:
      while (j < messages.size()) {
        msg = (Message) messages.get(j);
        
        // If the selector matches, sending it:
        if (Selector.matches(msg, notRec.getSelector())) {
          notMsg.addMessage(msg);
          
          if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
            MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Message "
                                          + msg.getIdentifier()
                                          + " sent to "
                                          + notRec.requester
                                          + " as a reply to "
                                          + notRec.getRequestId());

          // Removing the message:
          messages.remove(j);
          msg.delete();
          // Removing the request.
          replied = true;
          requests.remove(index);
          break;
        }
        // If selector does not match: going on
        else
          j++;
      }

      if (notMsg.getSize() > 0) {
        Channel.sendTo(notRec.requester, notMsg);
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
}
