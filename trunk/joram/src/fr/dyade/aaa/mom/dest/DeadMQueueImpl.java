/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package fr.dyade.aaa.mom.dest;

import fr.dyade.aaa.agent.*;
import fr.dyade.aaa.mom.MomTracing;
import fr.dyade.aaa.mom.comm.*;
import fr.dyade.aaa.mom.excepts.*;
import fr.dyade.aaa.mom.messages.Message;
import fr.dyade.aaa.mom.selectors.Selector;

import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * The <code>DeadMQueueImpl</code> class implements the MOM dead message queue
 * behaviour, basically storing dead messages and delivering them upon clients
 * requests.
 */
public class DeadMQueueImpl extends QueueImpl
{
  /** Static value holding the default DMQ identifier for a server. */
  static AgentId id = null;
  /** Static value holding the default threshold for a server. */
  static Integer threshold = null;


  /**
   * Constructs a <code>DeadMQueueImpl</code> instance.
   *
   * @param queueId  See superclass.
   * @param adminId  See superclass.
   */
  public DeadMQueueImpl(AgentId queueId, AgentId adminId)
  {
    super(queueId, adminId);
  }

  /** Returns a string view of this DeadMQueueImpl instance. */
  public String toString()
  {
    return "DeadMQueueImpl:" + destId.toString();
  }

  /** Static method returning the default DMQ identifier. */
  public static AgentId getId()
  {
    return id;
  }
  
  /** Static method returning the default threshold. */
  public static Integer getThreshold()
  {
    return threshold;
  }

  /**
   * Overrides this <code>DestinationImpl</code> method; the
   * <code>SetDMQRequest</code> request actually sets the default DMQ for 
   * the local server.
   *
   * @exception AccessException  If the requester is not the administrator.
   */
  protected void doReact(AgentId from, SetDMQRequest req)
                 throws AccessException
  {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    id = req.getDmqId();

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Default DMQ id set to "
                                    + dmqId);
  }
  
  /**
   * Overrides this <code>DestinationImpl</code> method; the messages carried 
   * by the <code>ClientMessages</code> instance are stored in their arrival
   * order, WRITE right is not checked.
   *
   * @exception AccessException  Never thrown.
   */
  protected void doReact(AgentId from, ClientMessages not)
                 throws AccessException
  {
    messages.addAll(not.getMessages());
    // Lauching a delivery sequence:
    deliverMessages(0);
  }

  /**
   * Overrides this <code>QueueImpl</code> method; the
   * <code>SetThreshRequest</code> request actually sets the default
   * threshold value for the local server.
   *
   * @exception AccessException  If the requester is not the administrator.
   */
  protected void doReact(AgentId from, SetThreshRequest req)
                 throws AccessException
  {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    threshold = req.getThreshold();

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Default threshold set"
                                                      + " to " + threshold);
  }

  /**
   * Overrides this <code>QueueImpl</code> method; messages matching the
   * request's selector are actually sent as a reply; no cleaning nor DMQ
   * sending is done.
   *
   * @exception AccessException  If the requester is not a reader.
   */
  protected void doReact(AgentId from, BrowseRequest not)
                 throws AccessException
  {
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
   *
   * @exception RequestException  Never thrown.
   */
  protected void doReact(AgentId from, AcknowledgeRequest not)
                 throws RequestException
  {}
 
  /**
   * Overrides this <code>QueueImpl</code> method;
   * <code>DenyRequest</code> requests are actually not processed
   * in dead message queues.
   *
   * @exception RequestException  Never thrown.
   */
  protected void doReact(AgentId from, DenyRequest not)
                 throws RequestException
  {}

  /**
   * Overrides this <code>QueueImpl</code> method; if the sent notification
   * was a <code>QueueMsgReply</code> instance, putting the sent message back
   * in queue.
   */
  protected void doProcess(UnknownAgent uA)
  {
    AgentId client = uA.agent;
    Notification not = uA.not;

    // If the notification is not a delivery, doing nothing. 
    if (! (not instanceof QueueMsgReply))
      return;

    // Putting the message back in queue:
    messages.add(((QueueMsgReply) not).getMessage());
    // Launching a delivery sequence:
    deliverMessages(messages.size() - 1); 
  }

  /**
   * Overrides this <code>QueueImpl</code> method; delivered messages are not
   * kept for acknowledgement or denying; validity of messages is
   * not checked and message fields are not updated; also, no sending to
   * any DMQ.
   */ 
  protected void deliverMessages(int index)
  {
    ReceiveRequest notRec = null;
    boolean replied;
    int j = 0;
    Message msg;
    QueueMsgReply notMsg;

    // Processing each request as long as there are deliverable messages:
    while (! messages.isEmpty() && index < requests.size()) { 
      notRec = (ReceiveRequest) requests.get(index);
      replied = false;

      // Checking the deliverable messages:
      while (j < messages.size()) {
        msg = (Message) messages.get(j);

        // If the selector matches, sending it:
        if (Selector.matches(msg, notRec.getSelector())) {
          notMsg = new QueueMsgReply(notRec, msg);
          Channel.sendTo(notRec.requester, notMsg);

          if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
            MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Message "
                                          + msg.getIdentifier()
                                          + " sent to "
                                          + notRec.requester
                                          + " as a reply to "
                                          + notRec.getRequestId());

          // Removing the message:
          messages.remove(j);
          // Removing the request.
          replied = true;
          requests.remove(index);
          break;
        }
        // If selector does not match: going on
        else
          j++;
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
  protected void sendToDMQ(Vector deadMessages, AgentId dmqId)
  {}
}
