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

import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * The <code>QueueImpl</code> class implements the MOM queue behaviour,
 * basically storing messages and delivering them upon clients requests.
 */
public class QueueImpl extends DestinationImpl
{
  /**
   * Threshold above which messages are considered as undeliverable because
   * constantly denied; 0 stands for no threshold, <code>null</code> for value
   * not set.
   */
  private Integer threshold = null;
  
  /** Vector holding messages before delivery and acknowledgement. */
  protected Vector messages;
  /** Vector holding requests before reply or expiry. */
  protected Vector requests;
  /** Number of non delivered messages. */
  protected int deliverables = 0;


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
  }

  /** Returns a string view of this QueueImpl instance. */
  public String toString()
  {
    return "QueueImpl:" + destId.toString();
  }

  /**
   * Distributes the received notifications to the appropriate reactions.
   *
   * @exception UnknownNotificationException  Thrown at superclass level.
   */
  public void react(AgentId from, Notification not)
              throws UnknownNotificationException
  {
    String reqId = null;
    if (not instanceof AbstractRequest)
      reqId = ((AbstractRequest) not).getRequestId();

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "--- " + this
                                    + ": got " + not.getClass().getName()
                                    + " with id: " + reqId
                                    + " from: " + from.toString());
    try {
      if (not instanceof SetThreshRequest)
        doReact(from, (SetThreshRequest) not);
      else if (not instanceof ReceiveRequest)
        doReact(from, (ReceiveRequest) not);
      else if (not instanceof BrowseRequest)
        doReact(from, (BrowseRequest) not);
      else if (not instanceof AcknowledgeRequest)
        doReact(from, (AcknowledgeRequest) not);
      else if (not instanceof DenyRequest)
        doReact(from, (DenyRequest) not);
      else
        super.react(from, not);
    }
    // MOM Exceptions are sent to the requester.
    catch (MomException exc) {
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
        MomTracing.dbgDestination.log(BasicLevel.WARN, exc);

      AbstractRequest req = (AbstractRequest) not;
      Channel.sendTo(from, new ExceptionReply(req, exc));
    }
  }

  
  /**
   * Method implementing the reaction to a <code>SetThreshRequest</code>
   * instance setting the threshold value for this queue.
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
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Threshold set to "
                                    + threshold);
  }

  /**
   * Method implementing the reaction to a <code>ReceiveRequest</code>
   * instance, requesting a message.
   * <p>
   * This method stores the request and launches a delivery sequence.
   *
   * @exception AccessException  If the sender is not a reader.
   */
  protected void doReact(AgentId from, ReceiveRequest not)
                 throws AccessException
  {
    // If client is not a reader, sending an exception.
    if (! isReader(from))
      throw new AccessException("READ right not granted");

    // Storing the request:
    not.requester = from;
    not.setExpiration(System.currentTimeMillis());
    requests.add(not);

    // Launching a delivery sequence for this request:
    deliverMessages(requests.size() - 1);

    // If the request has not been answered and if it is an immediate
    // delivery request, sending a null:
    if (requests.contains(not) && not.getTimeOut() == -1) {
      requests.remove(not);
      QueueMsgReply reply = new QueueMsgReply(not, null);
      Channel.sendTo(from, reply);

      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Receive answered"
                                        + " by a null.");
    }
  }

  /**
   * Method implementing the queue reaction to a <code>BrowseRequest</code>
   * instance, requesting an enumeration of the messages on the queue.
   * <p>
   * The method sends a <code>BrowseReply</code> back to the client. Expired
   * messages are sent to the DMQ.
   *
   * @exception AccessException  If the requester is not a reader.
   */
  protected void doReact(AgentId from, BrowseRequest not)
                 throws AccessException
  {
    // If client is not a reader, sending an exception.
    if (! isReader(from))
      throw new AccessException("READ right not granted");

    // Building the reply:
    BrowseReply rep = new BrowseReply(not);

    // Adding the deliverable messages to it:
    int i = 0;
    Message message;
    Vector deadM = null;
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
      // Invalid message: removing it and adding it to the vector of dead
      // messages:
      else {
        messages.remove(i);

        // If message was not consumed, decreasing the deliverables counter:
        if (message.consId == null)
          deliverables--;

        if (deadM == null)
          deadM = new Vector();

        message.expired = true;
        deadM.add(message);

        if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Expired message"
                                        + message.getIdentifier()
                                        + " removed.");
      }
    }
    // Sending the dead messages to the DMQ, if needed:
    if (deadM != null)
      sendToDMQ(deadM, null);

    // Delivering the reply:
    Channel.sendTo(from, rep);

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Request answered.");
  }

  /**
   * Method implementing the reaction to an <code>AcknowledgeRequest</code>
   * instance, requesting messages to be acknowledged.
   *
   * @exception RequestException  If the request does not come from the
   *              messages consumer.
   */
  protected void doReact(AgentId from, AcknowledgeRequest not)
                 throws RequestException
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
   * Method implementing the reaction to a <code>DenyRequest</code>
   * instance, requesting messages to be denied.
   * <p>
   * This method denies the messages and launches a delivery sequence.
   * Messages considered as undeliverable are sent to the DMQ.
   *
   * @exception RequestException  If the request does not come from the
   *              messages consumer.
   */
  protected void doReact(AgentId from, DenyRequest not)
                 throws RequestException
  {
    // Checking the parameters and getting the acknowledged msg indexes:
    Vector indexes = getIndexes(from.toString(), not.getMsgIds());
    int index;
    int shift = 0;
    Message msg;
    Vector deadM = null;

    while (! indexes.isEmpty()) {
      index = ((Integer) indexes.remove(0)).intValue() - shift;
      // Denying message:
      msg = (Message) messages.get(index);
      msg.denied = true;

      // If message considered as undeliverable, removing it and adding it
      // to the vector of dead messages:
      if (isUndeliverable(msg)) {
        messages.remove(index);
        shift++;

        if (deadM == null)
          deadM = new Vector();

        msg.undeliverable = true;
        deadM.add(msg);
      }
      else {
        msg.consId = null;
        deliverables++;
      }

      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Message "
                                      + msg.getIdentifier()
                                      + " denied.");
    }
    // Sending the dead messages to the DMQ, if needed:
    if (deadM != null)
      sendToDMQ(deadM, null);

    // Lauching a delivery sequence:
    deliverMessages(0);
  }


  /**
   * The <code>DestinationImpl</code> class calls this method for passing
   * notifications which have been partly processed, so that they are
   * specifically processed by the <code>QueueImpl</code> class.
   */
  protected void specialProcess(Notification not)
  {
    if (not instanceof SetRightRequest)
      doProcess((SetRightRequest) not);
    else if (not instanceof ClientMessages)
      doProcess((ClientMessages) not);
    else if (not instanceof UnknownAgent)
      doProcess((UnknownAgent) not);
    else if (not instanceof DeleteNot)
      doProcess((DeleteNot) not);
  }


  /**
   * Method specifically processing a <code>SetRightRequest</code> instance.
   * <p>
   * When a reader is removed, and receive requests of this reader are still
   * on the queue, they are replied to by an <code>ExceptionReply</code>.
   */
  protected void doProcess(SetRightRequest not)
  {
    // If the request does not unset a reader, doing nothing.
    if (not.getRight() != -READ)
      return;

    AgentId user = not.getClient();

    ReceiveRequest request;
    AccessException exc;
    ExceptionReply reply;

    // Free reading right has been removed; replying to the non readers
    // requests.
    if (user == null) {
      for (int i = 0; i < requests.size(); i++) {
        request = (ReceiveRequest) requests.get(i);
        if (! isReader(request.requester)) {
          exc = new AccessException("Free READ access removed");
          reply = new ExceptionReply(request, exc);
          Channel.sendTo(request.requester, reply);
          requests.remove(i);
          i--;
        }
      }
    }
    // Reading right of a given user has been removed; replying to its
    // requests.
    else {
      for (int i = 0; i < requests.size(); i++) {
        request = (ReceiveRequest) requests.get(i);
        if (user.equals(request.requester)) {
          exc = new AccessException("READ right removed");
          reply = new ExceptionReply(request, exc);
          Channel.sendTo(request.requester, reply);
          requests.remove(i);
          i--;
        }
      }
    }
  }

  /**
   * Method specifically processing a <code>ClientMessages</code> instance.
   * <p>
   * This method stores the messages and launches a delivery sequence.
   */
  protected void doProcess(ClientMessages not)
  {
    Vector recM = not.getMessages();

    Message msg;
    int priority;
    int k;
    Message storedMsg;

    // Storing each message according to its priority:
    for (int i = 0; i < recM.size(); i++) {
      try {
        msg = (Message) recM.get(i);
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
      // Invalid message class: going on.
      catch (ClassCastException cE) {
        if (MomTracing.dbgDestination.isLoggable(BasicLevel.ERROR))
          MomTracing.dbgDestination.log(BasicLevel.ERROR, "Invalid message"
                                        + " class: " + cE);
      }
    }
    // Lauching a delivery sequence:
    deliverMessages(0);
  }

  /**
   * Method specifically processing an <code>UnknownAgent</code> instance.
   * <p>
   * The specific processing is done when a <code>QueueMsgReply</code> was 
   * sent to a requester which does not exist anymore. In that case, the
   * messages sent to this requester and not yet acknowledged are marked as
   * "denied" for delivery to an other requester, and a new delivery sequence
   * is launched. Messages considered as undeliverable are removed and sent to
   * the DMQ.
   */ 
  protected void doProcess(UnknownAgent uA)
  {
    AgentId client = uA.agent;
    Notification not = uA.not;

    // If the notification is not a delivery, doing nothing. 
    if (! (not instanceof QueueMsgReply))
      return;

    Vector deadM = new Vector();
    Message msg;
    for (int i = 0; i < messages.size(); i++) {
      msg = (Message) messages.get(i);
      if (msg.consId.equals(client.toString())) {
        msg.denied = true;

        // If message considered as undeliverable, removing it and adding it
        // to the vector of dead messages:
        if (isUndeliverable(msg)) {
          messages.remove(i);
          i--;

          msg.undeliverable = true; 
          deadM.add(msg);
        }
        else {
          msg.consId = null;
          deliverables++;
        }

        if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
          MomTracing.dbgDestination.log(BasicLevel.WARN, "Message "
                                        + msg.getIdentifier() + " denied.");
      }
    }
    // Sending dead messages to the DMQ, if needed:
    if (! deadM.isEmpty())
      sendToDMQ(deadM, null);

    // Launching a delivery sequence:
    deliverMessages(0);
  }

  /**
   * Method specifically processing a
   * <code>fr.dyade.aaa.agent.DeleteNot</code> instance.
   * <p>
   * <code>ExceptionReply</code> replies are sent to the pending receivers,
   * and the remaining messages are sent to the DMQ. 
   */
  protected void doProcess(DeleteNot not)
  {
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
    // Sending the remaining messages to the DMQ, if needed:
    if (! messages.isEmpty()) {
      for (int i = 0; i < messages.size(); i++)
        ((Message) messages.get(i)).deletedDest = true;
      sendToDMQ(messages, null);
    }
  }

  /**
   * Actually tries to answer the pending "receive" requests.
   * <p>
   * The method may send <code>QueueMsgReply</code> replies to clients.
   *
   * @param index  Index where starting to "browse" the requests.
   */
  protected void deliverMessages(int index)
  {
    ReceiveRequest notRec = null;
    boolean replied;
    int j = 0;
    Message msg;
    QueueMsgReply notMsg;
    Vector deadM = null;

    // Processing each request as long as there are deliverable messages:
    while (deliverables > 0 && index < requests.size()) { 
      notRec = (ReceiveRequest) requests.get(index);
      replied = false;

      // Checking the request validity:
      if (notRec.isValid()) {
        // Checking the deliverable messages:
        while (deliverables > 0 && j < messages.size()) {
          msg = (Message) messages.get(j);

          // If the message is still valid:
          if (msg.isValid()) {
            // If the message has not yet been delivered and if selector
            // matches, sending it:
            if (msg.consId == null
                && Selector.matches(msg, notRec.getSelector())) {
              msg.deliveryCount++;
              notMsg = new QueueMsgReply(notRec, msg);
              Channel.sendTo(notRec.requester, notMsg);

              if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
                MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Message "
                                              + msg.getIdentifier()
                                              + " sent to "
                                              + notRec.requester
                                              + " as a reply to "
                                              + notRec.getRequestId());

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
          // If message is invalid, removing it and adding it to the vector of
          // dead messages:
          else {
            messages.remove(j);
             
            // If message was not consumed, decreasing the
            // deliverables counter:
            if (msg.consId == null)
              deliverables--;
  
            if (deadM == null)
              deadM = new Vector();
  
            msg.expired = true;
            deadM.add(msg);
  
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
      // The request expired: removing it.
      else {
        requests.remove(index);

        if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Request "
                                        + notRec.getRequestId()
                                        + " expired");
      }
    }
    // If needed, sending the dead messages to the DMQ:
    if (deadM != null)
      sendToDMQ(deadM, null);
  }
  
  /**
   * Internal method for getting the indexes of the messages matching the
   * given parameters.
   *
   * @param consumer  AgentId of client acknowledging or denying messages.
   * @param msgIds  Vector of message identifiers.
   *
   * @exception RequestException  If the requester is not the consumer of
   *              one of the messages.
   */
  protected Vector getIndexes(String requester, Vector msgIds)
                 throws RequestException
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
            throw new RequestException("Forbidden acknowledgement or denying"
                                       + " of message " + msgId);
          // Breaking the loop as the message was found:
          break;
        }
        index++;
      }
    }
    return indexes;
  }

  /**
   * Returns <code>true</code> if a given message is considered as 
   * undeliverable, because its delivery count matches the queue's 
   * threshold, if any, or the server's default threshold value (if any).
   */
  protected boolean isUndeliverable(Message message)
  {
    if (threshold != null)
      return message.deliveryCount == threshold.intValue();
    else if (DeadMQueueImpl.threshold != null)
      return message.deliveryCount == DeadMQueueImpl.threshold.intValue();
    return false;
  }
}
