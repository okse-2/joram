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
import fr.dyade.aaa.mom.selectors.*;
import fr.dyade.aaa.task.*;

import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * The <code>DeadMQueueImpl</code> class provides the MOM dead message queue
 * behaviour, basically storing dead messages and delivering them upon clients
 * requests.
 * <p>
 * A dead message queue is very similar to a queue except that the messages
 * consumed on it are not supposed to be acknowledged or denied, because
 * they are remove right after consumption.
 */
public class DeadMQueueImpl extends QueueImpl
{
  /** Static value holding the default DMQ identifier for a server. */
  public static AgentId id = null;
  /** Static value holding the default threshold for a server. */
  public static Integer threshold = null;


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

  /** Returns a string view of this queue implementation. */
  public String toString()
  {
    return "DeadMQueueImpl:" + destId.toString();
  }

  /**
   * Distributes the requests to the appropriate dead message queue methods.
   * <p>
   * Accepted requests are:
   * <ul>
   * <li><code>ClientMessages</code> notifications,</li>
   * <li><code>BrowseRequest</code> notifications,</li>
   * <li><code>AcknowledgeRequest</code> notifications,</li>
   * <li><code>DenyRequest</code> notifications.</li>
   * </ul>
   * <p>
   * An <code>ExceptionReply</code> notification is sent back in the case
   * of an error while processing a request.
   */
  public void doReact(AgentId from, AbstractRequest req)
  {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "--- " + this
                                    + ": got " + req.getClass().getName()
                                    + " with id: " + req.getRequestId()
                                    + " from: " + from.toString());
    try {
      if (req instanceof ClientMessages)
        doReact(from, (ClientMessages) req);
      else if (req instanceof BrowseRequest)
        doReact(from, (BrowseRequest) req);
      else if (req instanceof AcknowledgeRequest)
        doReact(from, (AcknowledgeRequest) req);
      else if (req instanceof DenyRequest)
        doReact(from, (DenyRequest) req);
      else
        super.doReact(from, req);
    }
    catch (MomException mE) {
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
        MomTracing.dbgDestination.log(BasicLevel.WARN, mE);

      ExceptionReply eR = new ExceptionReply((AbstractRequest) req, mE);
      Channel.sendTo(from, eR);
    }
  }


  /**
   * Method implementing the queue reaction to a <code>ClientMessages</code>
   * instance holding dead messages.
   */
  private void doReact(AgentId from, ClientMessages not)
  {
    Vector deadMessages = not.getMessages();
    Message msg;

    while (! deadMessages.isEmpty()) {
      msg = (Message) deadMessages.remove(0);
      messages.add(msg);
      deliverables++;
    }

    // Lauching a delivery sequence:
    deliverMessages(0); 
  }

  /**
   * Method implementing the queue reaction to a <code>BrowseRequest</code>
   * instance, requesting an enumeration of the messages on the queue.
   * <p>
   * The method sends a <code>BrowseReply</code> back to the client.
   *
   * @exception AccessException  If the sender is not a reader.
   */
  private void doReact(AgentId from, BrowseRequest not) throws AccessException
  {
    // If the client is not a reader, throwing an AccessException:
    if (! super.isReader(from))
      throw new AccessException("The needed READ right is not granted"
                                + " on queue " + destId);

    // Building the reply:
    BrowseReply rep = new BrowseReply(not);

    // Adding the matching messages to it:
    int i = 0;
    Message message;
    while (deliverables > 0 && i < messages.size()) {
      message = (Message) messages.get(i);
   
      if (Selector.matches(message, not.getSelector()))
        rep.addMessage(message);

      i++;
    }

    // Delivering the reply:
    Channel.sendTo(from, rep);

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Request answered.");
  }


  /**
   * Method implementing the queue reaction to an
   * <code>AcknowledgeRequest</code> instance, requesting messages to be
   * acknowledged.
   * <p>
   * A dead message queue does not react to acknowledgements.
   *
   * @exception RequestException  Never thrown.
   */
  private void doReact(AgentId from,
                       AcknowledgeRequest not) throws RequestException
  {}

  /**
   * Method implementing the queue reaction to a <code>DenyRequest</code>
   * instance, requesting messages to be denied.
   * <p>
   * A dead message queue does not react to denyings.
   *
   * @exception RequestException  Never thrown.
   */
  private void doReact(AgentId from, DenyRequest not) throws RequestException
  {}

  /** 
   * Method implementing the queue reaction to a
   * <code>fr.dyade.aaa.agent.UnknownAgent</code> notification sent by the
   * engine if the queue tried to send a reply to a non existing agent.
   * <p>
   * This case might happen when sending a <code>QueueMsgReply</code> to a
   * requester which does not exist anymore. In that case, the messages sent
   * to this requester are put back in the queue.
   */ 
  public void removeDeadClient(UnknownAgent uA)
  {
    AgentId client = uA.agent;

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
      MomTracing.dbgDestination.log(BasicLevel.WARN, "--- " + this
                                    + " notified of a dead client: "
                                    + client.toString());

    if (uA.not instanceof QueueMsgReply) {
      messages.add(((QueueMsgReply) uA.not).getMessage());
  
      // Launching a delivery sequence:
      deliverMessages(0);
    }
  }

  /** 
   * Method implementing the queue reaction to a
   * <code>fr.dyade.aaa.agent.DeleteNot</code> notification requesting it
   * to be deleted.
   * <p>
   * The notification is ignored if the sender is not an admin of the queue.
   * Otherwise, <code>ExceptionReply</code> replies are sent to the pending
   * receivers, and the remaining messages are sent to an other DMQ.
   */
  public void delete(AgentId from)
  {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
      MomTracing.dbgDestination.log(BasicLevel.WARN, "--- " + this
                                    + " notified to be deleted by "
                                    + from.toString());

    // If the sender is not an admin, ignoring the notification:
    if (! super.isAdministrator(from)) {
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
        MomTracing.dbgDestination.log(BasicLevel.WARN, "Deletion request"
                                      + " sent by invalid admin.");
      return;
    }

    // Building the exception to send to the pending receivers:
    DestinationException exc = new DestinationException("Dead Message Queue "
                                                        + destId
                                                        + " is deleted.");
    ReceiveRequest rec;
    ExceptionReply excRep;
    // Sending it to the pending receivers:
    for (int i = 0; i < requests.size(); i++) {
      rec = (ReceiveRequest) requests.elementAt(i);
      excRep = new ExceptionReply(rec, exc);

      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Requester "
                                      + rec.requester
                                      + " notified of the queue deletion.");
      
      Channel.sendTo(rec.requester, excRep);
    }

    if (id != null && this.id.equals(id))
      id = null;

    // Sending the remaining messages to an other DMQ:
    sendToDMQ(messages, null);

    deleted = true;
  }

  /**
   * Actually tries to answer the pending "receive" requests.
   * <p>
   * The method may send <code>QueueMsgReply</code> replies to clients and
   * <code>fr.dyade.aaa.task.RemoveConditionListener</code> notifications
   * to the scheduler.
   *
   * @param index  Index where starting to "browse" the requests.
   */
  private void deliverMessages(int index)
  {
    ReceiveRequest notRec = null;
    boolean replied;
    int j = 0;
    Message msg;
    QueueMsgReply notMsg;

    // Processing each request as long as there are deliverable messages:
    while (deliverables > 0 && index < requests.size()) { 
      notRec = (ReceiveRequest) requests.get(index);
      replied = false;

      // Checking the deliverable messages:
      while (deliverables > 0 && j < messages.size()) {
        msg = (Message) messages.get(j);

        // If selector matches, sending it:
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

          // If request timeOut is positive, removing the condition
          // listener:
          if (notRec.getTimeOut() > 0) {
            RemoveConditionListener remL =
              new RemoveConditionListener(notRec.requester.toString()
                                          + notRec.getRequestId());
            Channel.sendTo(scheduler, remL);
  
            if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
              MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Condition"
                                              + " listener removed from"
                                              + " Scheduler."); 
          }
          // Removing the message:
          messages.remove(j);
          // Removing the request.
          replied = true;
          requests.remove(index);
          deliverables--;
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
}
