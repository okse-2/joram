/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
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
 * The present code contributor is ScalAgent Distributed Technologies.
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
 * The <code>QueueImpl</code> class provides the MOM queue behaviour,
 * basically storing messages and delivering them upon clients requests.
 */
public class QueueImpl extends DestinationImpl
{
  /** Vector holding messages before delivery and acknowledgement. */
  private Vector messages;
  /** Vector holding requests before reply or expiry. */
  private Vector requests;
  /** Id of Scheduler for checking the stored requests validity. */
  private AgentId scheduler;
  /** Number of non delivered messages. */
  private int deliverables = 0;

  /**
   * Constructs a <code>QueueImpl</code> instance.
   *
   * @param queueId  See superclass.
   * @param adminId  See superclass.
   */
  public QueueImpl(AgentId queueId, AgentId adminId)
  {
    super(queueId, adminId);
    messages = new Vector();
    requests = new Vector();
    scheduler = Scheduler.getDefault();

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, this + ": created.");
  }

  /** Returns a string view of this queue implementation. */
  public String toString()
  {
    return "QueueImpl:" + destId.toString();
  }

  /**
   * Distributes the requests to the appropriate queue methods.
   * <p>
   * Accepted requests are:
   * <ul>
   * <li><code>ClientMessages</code> notifications,</li>
   * <li><code>ReceiveRequest</code> notifications,</li>
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
      else if (req instanceof ReceiveRequest)
        doReact(from, (ReceiveRequest) req);
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
   * instance holding messages sent by a client agent.
   * <p>
   * This method stores the valid messages and launches a delivery sequence.
   *
   * @exception AccessException  If the sender is not a WRITER.
   */
  private void doReact(AgentId from, ClientMessages not) throws AccessException
  {
    // If sender is not a writer, throwing an exception:
    if (! super.isWriter(from))
      throw new AccessException("The needed WRITE right is not granted"
                                + " on queue " + destId);

    Vector recM = not.getMessages();

    Message msg;
    int priority;
    int k;
    Message storedMsg;

    // For each sent message:
    for (int i = 0; i < recM.size(); i++) {
      try {
        msg = (Message) recM.get(i);

        // Checking if the message is valid:
        if (msg.isValid()) {

          // Storing the message according to its priority:
          priority = msg.getPriority();
          k = 0;
          while (k < messages.size()) {
            storedMsg = (Message) messages.get(k);
            if (priority > storedMsg.getPriority())
              break;
            else
              k++;
          }
          messages.insertElementAt(msg, k);
          deliverables++;

          if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
            MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Message "
                                          + msg.getIdentifier() + " stored"
                                          + " at pos " + k);
        } 
      }
      catch (ClassCastException cE) {
        if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
          MomTracing.dbgDestination.log(BasicLevel.WARN, "Invalid message: "
                                        + cE);
      }
    }
    // Lauching a delivery sequence:
    deliverMessages(0);
  }

  /**
   * Method implementing the queue reaction to a <code>ReceiveRequest</code>
   * instance, requesting a message.
   * <p>
   * This method stores the request and launches a delivery sequence. If the
   * request has finally not been answered, the method may send
   * <code>AddConditionListener</code> and <code>ScheduleEvent</code>
   * notifications to the scheduler if the request has a positive timer, or
   * a <code>QueueMsgReply</code> back to the requester if it is an immediate
   * delivery request.
   *
   * @exception AccessException  If the sender is not a reader.
   */
  private void doReact(AgentId from, ReceiveRequest not) throws AccessException
  {
    // If client is not a reader, throwing a MomException:
    if (! super.isReader(from))
      throw new AccessException("The needed READ right is not granted"
                                + " on queue " + destId);

    // Storing the request:
    not.requester = from;
    requests.add(not);

    // Launching a delivery sequence for this request:
    deliverMessages(requests.size() - 1);

    // If the request has not been answered:
    if (requests.contains(not)) {

      // If it has a positive timer, registering it to the scheduler:
      if (not.getTimeOut() > 0) {
        AddConditionListener addL =
          new AddConditionListener(from.toString() + not.getRequestId());
        Channel.sendTo(scheduler, addL);
        ScheduleEvent sched =
          new ScheduleEvent(from.toString() + not.getRequestId(),
                            new java.util.Date(System.currentTimeMillis()
                                               + not.getTimeOut()));
        Channel.sendTo(scheduler, sched);

        if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Request"
                                        + " registered to the Scheduler.");
      } 

      // If it is an immediate delivery request, sending a null:
      else if (not.getTimeOut() == 0) {
        requests.remove(not);
        QueueMsgReply reply = new QueueMsgReply(not, null);
        Channel.sendTo(from, reply);

        if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Receive answered"
                                        + " by a null.");
      }
    }
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

    // Adding the deliverable messages to it:
    int i = 0;
    Message message;
    while (deliverables > 0 && i < messages.size()) {
      message = (Message) messages.get(i);
   
      // Testing message validity:
      if (message.isValid()) {
        // Non delivered message, matching the selector: adding it:
        if (message.consId == null
            && Selector.matches(message, not.getSelector()))
          rep.addMessage(message);

        i++;
      }
      // Invalid message: removing it:
      else {
        messages.remove(i);
        deliverables--;

        if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Expired message"
                                        + message.getIdentifier()
                                        + " removed.");
      }
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
   *
   * @exception RequestException  If the request is invalid.
   */
  private void doReact(AgentId from,
                       AcknowledgeRequest not) throws RequestException
  {
    // Checking the parameters and getting the acknowledged msg indexes:
    Vector indexes = getIndexes(from.toString(), not.getMsgIds());
    int index;
    int shift = 0;
    Message msg;

    while (! indexes.isEmpty()) {
      index = ((Integer) indexes.remove(0)).intValue() - shift;
      // Acknowledging the message:
      msg = (Message) messages.remove(index);
      shift++;

      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Message "
                                      + msg.getIdentifier()
                                      + " acknowledged.");
    }
  }

  /**
   * Method implementing the queue reaction to a <code>DenyRequest</code>
   * instance, requesting messages to be denied.
   * <p>
   * This method denies the messages and launches a delivery sequence.
   *
   * @exception RequestException  If the request is invalid.
   */
  private void doReact(AgentId from, DenyRequest not) throws RequestException
  {
    // Checking the parameters and getting the acknowledged msg indexes:
    Vector indexes = getIndexes(from.toString(), not.getMsgIds());
    int index;
    Message msg;

    while (! indexes.isEmpty()) {
      index = ((Integer) indexes.remove(0)).intValue();
      // Denying message:
      msg = (Message) messages.get(index);
      msg.denied = true;
      msg.consId = null;
      deliverables++;

      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Message "
                                      + msg.getIdentifier()
                                      + " denied.");
    }
    // Lauching a delivery sequence:
    deliverMessages(0);
  }

  /**
   * Method implementing the queue reaction to a
   * <code>fr.dyade.aaa.task.Condition</code> instance sent by the Scheduler
   * service, notifying the expiry of a request.
   * <p>
   * The method usually sends a <code>QueueMsgReply</code> back to the
   * expired request's requester, and a
   * <code>fr.dyade.aaa.task.RemoveConditionListener</code> to the
   * scheduler.
   */
  public void answerExpiredRequest(Condition not)
  {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "--- " + this
                                    + " notified of an expired request.");
    int i = 0;
    ReceiveRequest notRec;

    // Searching the expired request, answering it and removing it.
    while (i < requests.size()) {
      notRec = (ReceiveRequest) requests.elementAt(i);

      if ((not.name).equals(notRec.requester.toString()
                            + notRec.getRequestId())) { 
        requests.removeElementAt(i);
        QueueMsgReply notMsg = new QueueMsgReply(notRec, null);
        Channel.sendTo(notRec.requester, notMsg);

        if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Request: "
                                        + notRec.getRequestId()
                                        + " answered by null");
        break;
      }
      else
        i++;
    }
    // Removing the condition listener corresponding to this request.
    RemoveConditionListener remL = new RemoveConditionListener(not.name);
    Channel.sendTo(scheduler, remL);
  }


  /** 
   * Method implementing the queue reaction to a
   * <code>fr.dyade.aaa.agent.UnknownAgent</code> notification sent by the
   * engine if the queue tried to send a reply to a non existing agent.
   * <p>
   * This case might happen when sending a <code>QueueMsgReply</code> to a
   * requester which does not exist anymore. In that case, the messages sent
   * to this requester and not yet acknowledged are marked as "denied"
   * for delivery to an other requester, and a new delivery sequence is
   * launched.
   */ 
  public void removeDeadClient(UnknownAgent uA)
  {
    AgentId client = uA.agent;

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
      MomTracing.dbgDestination.log(BasicLevel.WARN, "--- " + this
                                    + " notified of a dead client: "
                                    + client.toString());
  
    Message msg;
    for (int i = 0; i < messages.size(); i++) {
      msg = (Message) messages.get(i);
      if (msg.consId.equals(client.toString())) {
        msg.denied = true;
        msg.consId = null;
        deliverables++;

        if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
          MomTracing.dbgDestination.log(BasicLevel.WARN, "Message "
                                        + msg.getIdentifier() + " denied.");
      }
    }
    // Launching a delivery sequence:
    deliverMessages(0);
  }

  /** 
   * Method implementing the queue reaction to a
   * <code>fr.dyade.aaa.agent.DeleteNot</code> notification requesting it
   * to be deleted.
   * <p>
   * The notification is ignored if the sender is not an admin of the queue.
   * Otherwise, <code>ExceptionReply</code> replies are sent to the pending
   * receivers.
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
    DestinationException exc = new DestinationException("Queue " + destId
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

        // If the message is still valid:
        if (msg.isValid()) {
          // If the message has not yet been delivered and if selector matches:
          // sending it:
          if (msg.consId == null
              && Selector.matches(msg, notRec.getSelector())) {
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
            // Removing the message if request in auto ack mode:
            if (notRec.getAutoAck())
              messages.remove(j);
            // Else, updating the consumer field for later acknowledgement:
            else
              msg.consId = notRec.requester.toString();
            // Removing the request.
            replied = true;
            requests.remove(index);
            deliverables--;
            break;
          }
          // If message delivered or selector does not match: going on
          else
            j++;
        }
        // If message is invalid, removing it:
        else {
          messages.remove(j);
          deliverables--;

          if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
            MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Expired"
                                          + " message " + msg.getIdentifier()
                                          + " removed.");
        } 
      }
      // Next request:
      j = 0;
      if (! replied)
        index++;
    }
  }
  
  /**
   * Internal method for getting the indexes of the messages matching the
   * given parameters.
   *
   * @param requester  AgentId of client acknowledging or denying messages.
   * @param msgIds  Vector of message identifiers.
   *
   * @exception RequestException  If the requester is not the consumer of
   *              one of the messages.
   */
  private Vector getIndexes(String requester,
                            Vector msgIds) throws RequestException
  {
    String msgId;
    int index = 0;
    Message msg;
    Vector indexes = new Vector();

    while (! msgIds.isEmpty()) {
      msgId = (String) msgIds.remove(0);

      // Checking the stored messages one by one:
      while (index < messages.size()) {
        msg = (Message) messages.get(index);
     
        // If the current message matches the sent identifier:
        if (msgId.equals(msg.getIdentifier())) {
          // If the requester matches the consumer, adding it to the list:
          if (msg.consId != null && msg.consId.equals(requester))
            indexes.add(new Integer(index));
          // Else, throwing an exception:
          else
            throw new RequestException("Message " + msgId + " not consumed"
                                       + " by requester: " + requester);
          // Breaking the loop as the message was found:
          break;
        }
        index++;
      }
    }
    return indexes;
  }
}
