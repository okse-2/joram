/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Contributor(s):
 */
package fr.dyade.aaa.mom.dest;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.DeleteNot;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.UnknownAgent;
import fr.dyade.aaa.agent.UnknownNotificationException;
import fr.dyade.aaa.mom.MomTracing;
import fr.dyade.aaa.mom.admin.*;
import fr.dyade.aaa.mom.comm.*;
import fr.dyade.aaa.mom.comm.AdminReply;
import fr.dyade.aaa.mom.excepts.*;
import fr.dyade.aaa.mom.messages.*;
import fr.dyade.aaa.mom.selectors.*;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;


/**
 * The <code>QueueImpl</code> class implements the MOM queue behaviour,
 * basically storing messages and delivering them upon clients requests.
 */
public class QueueImpl extends DestinationImpl implements QueueImplMBean
{
  /** Counter of messages arrivals. */
  private long arrivalsCounter = 0;

  /** Table keeping the messages' consumers identifiers. */
  private Hashtable consumers;
  /** Table keeping the messages' consumers contexts. */
  private Hashtable contexts;
  /**
   * Threshold above which messages are considered as undeliverable because
   * constantly denied; 0 stands for no threshold, <code>null</code> for value
   * not set.
   */
  private Integer threshold = null;

  /** <code>true</code> if all the stored messages have the same priority. */
  private boolean samePriorities;
  /** Common priority value. */
  private int priority; 

  /** Vector holding the requests before reply or expiry. */
  protected Vector requests;
  /** The persistence module used for managing the messages' persistence. */
  protected PersistenceModule persistenceModule;

  /** <code>true</code> if the queue is currently receiving messages. */
  private transient boolean receiving = false;
  /** Vector holding the messages before delivery. */
  protected transient Vector messages;
  /** Table holding the delivered messages before acknowledgement. */
  protected transient Hashtable deliveredMsgs;


  /**
   * Constructs a <code>QueueImpl</code> instance.
   *
   * @param destId  Identifier of the agent hosting the queue.
   * @param adminId  Identifier of the administrator of the queue.
   */
  public QueueImpl(AgentId destId, AgentId adminId)
  {
    super(destId, adminId);
    consumers = new Hashtable();
    contexts = new Hashtable();
    requests = new Vector();
    persistenceModule = new PersistenceModule(destId); 
  }


  public String toString()
  {
    return "QueueImpl:" + destId.toString();
  }


  /**
   * Distributes the received notifications to the appropriate reactions.
   *
   * @exception UnknownNotificationException  When receiving an unexpected
   *              notification.
   */
  public void react(AgentId from, Notification not)
              throws UnknownNotificationException
  {
    int reqId = -1;
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
      else if (not instanceof Monit_GetPendingMessages)
        doReact(from, (Monit_GetPendingMessages) not);
      else if (not instanceof Monit_GetPendingRequests)
        doReact(from, (Monit_GetPendingRequests) not);
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

      // Commiting the message persistence orders.
      persistenceModule.commit();
    }
    // MOM Exceptions are sent to the requester.
    catch (MomException exc) {
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
        MomTracing.dbgDestination.log(BasicLevel.WARN, exc);

      AbstractRequest req = (AbstractRequest) not;
      Channel.sendTo(from, new ExceptionReply(req, exc));

      // Rolling back the message persistence orders.
      persistenceModule.rollback();
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
    
    String info = "Request ["
                  + req.getClass().getName()
                  + "], sent to Queue ["
                  + destId
                  + "], successful [true]: threshold ["
                  + threshold
                  + "] set";
    Channel.sendTo(from, new AdminReply(req, true, info));

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, info);
  }

  /**
   * Overrides this <code>DestinationImpl</code> method for sending back
   * the threshold along with the DMQ id.
   *
   * @exception AccessException  If the requester is not the administrator.
   */
  protected void doReact(AgentId from, Monit_GetDMQSettings not)
                 throws AccessException
  {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    String id = null;
    if (dmqId != null)
      id = dmqId.toString();
    Channel.sendTo(from, new Monit_GetDMQSettingsRep(not, id, threshold));
  }

  /**
   * Method implementing the reaction to a
   * <code>Monit_GetPendingMessages</code> notification requesting the
   * number of pending messages.
   *
   * @exception AccessException  If the requester is not the administrator.
   */
  protected void doReact(AgentId from, Monit_GetPendingMessages not)
                 throws AccessException
  {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    Channel.sendTo(from, new Monit_GetNumberRep(not, messages.size()));
  }

  /**
   * Method implementing the reaction to a
   * <code>Monit_GetPendingRequests</code> notification requesting the
   * number of pending requests.
   *
   * @exception AccessException  If the requester is not the administrator.
   */
  protected void doReact(AgentId from, Monit_GetPendingRequests not)
                 throws AccessException
  {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    Channel.sendTo(from, new Monit_GetNumberRep(not, requests.size()));
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
    int reqIndex = requests.size() - 1;
    deliverMessages(reqIndex);

    // If the request has not been answered and if it is an immediate
    // delivery request, sending a null:
    if ((requests.size() - 1) == reqIndex && not.getTimeOut() == -1) {
      requests.remove(reqIndex);
      QueueMsgReply reply = new QueueMsgReply(not, null);
      Channel.sendTo(from, reply);

      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                      "Receive answered by a null.");
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
    ClientMessages deadMessages = null;
    while (i < messages.size()) {
      message = (Message) messages.get(i);
      // Testing message validity:
      if (message.isValid()) {
        // Matching selector: adding the message:
        if (Selector.matches(message, not.getSelector()))
          rep.addMessage(message);

        i++;
      }
      // Invalid message: removing it and adding it to the vector of dead
      // messages:
      else {
        messages.remove(i);
        persistenceModule.delete(message);
        
        message.expired = true;

        if (deadMessages == null)
          deadMessages = new ClientMessages();
        deadMessages.addMessage(message);

        if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Expired message"
                                        + message.getIdentifier()
                                        + " removed.");
      }
    }
    // Sending the dead messages to the DMQ, if needed:
    if (deadMessages != null)
      sendToDMQ(deadMessages, null);

    // Delivering the reply:
    Channel.sendTo(from, rep);

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Request answered.");
  }

  /**
   * Method implementing the reaction to an <code>AcknowledgeRequest</code>
   * instance, requesting messages to be acknowledged.
   */
  protected void doReact(AgentId from, AcknowledgeRequest not)
  {
    String msgId;
    Message msg;
    for (Enumeration ids = not.getIds(); ids.hasMoreElements();) {
      msgId = (String) ids.nextElement();
      msg = (Message) deliveredMsgs.remove(msgId);
      consumers.remove(msgId);
      contexts.remove(msgId);

      if (msg != null) {
        persistenceModule.delete(msg);
      
        if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Message "
                                        + msgId + " acknowledged.");
      }
      else if (MomTracing.dbgDestination.isLoggable(BasicLevel.ERROR))
        MomTracing.dbgDestination.log(BasicLevel.ERROR,
                                      "Message " + msgId
                                      + " not found for acknowledgement.");
    }
  }

  /**
   * Method implementing the reaction to a <code>DenyRequest</code>
   * instance, requesting messages to be denied.
   * <p>
   * This method denies the messages and launches a delivery sequence.
   * Messages considered as undeliverable are sent to the DMQ.
   */
  protected void doReact(AgentId from, DenyRequest not)
  {
    Enumeration ids = not.getIds();

    String msgId;
    Message msg;
    AgentId consId;
    int consCtx;
    ClientMessages deadMessages = null;

    // If the deny request is empty, the denying is a contextual one: it
    // requests the denying of all the messages consumed by the denier in
    // the denying context:
    if (! ids.hasMoreElements()) {
      // Browsing the delivered messages:
      for (Enumeration delIds = deliveredMsgs.keys();
           delIds.hasMoreElements();) {
        msgId = (String) delIds.nextElement();
        msg = (Message) deliveredMsgs.get(msgId);
        consId = (AgentId) consumers.get(msgId);
        consCtx = ((Integer) contexts.get(msgId)).intValue();

        // If the current message has been consumed by the denier in the same
        // context: denying it.
        if (consId.equals(from) && consCtx == not.getClientContext()) {
          consumers.remove(msgId);
          contexts.remove(msgId);
          deliveredMsgs.remove(msgId);
          msg.denied = true;

          // If message considered as undeliverable, adding
          // it to the vector of dead messages:
          if (isUndeliverable(msg)) {
            persistenceModule.delete(msg);

            msg.undeliverable = true;

            if (deadMessages == null)
              deadMessages = new ClientMessages();
            deadMessages.addMessage(msg);
          }
          // Else, putting the message back into the deliverables vector:
          else
            storeMessage(msg);

          if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
            MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Message "
                                          + msgId + " denied.");
        }
      }
    }

    // For a non empty request, browsing the denied messages:
    for (ids = not.getIds(); ids.hasMoreElements();) {
      msgId = (String) ids.nextElement();
      msg = (Message) deliveredMsgs.remove(msgId);

      // Message may have already been denied. For example, a proxy may deny
      // a message twice, first when detecting a connection failure - and
      // in that case it sends a contextual denying -, then when receiving 
      // the message from the queue - and in that case it also sends an
      // individual denying.
      if (msg == null)
        break;

      msg.denied = true;

      consumers.remove(msgId);
      contexts.remove(msgId);

      // If message considered as undeliverable, adding it
      // to the vector of dead messages:
      if (isUndeliverable(msg)) {
        persistenceModule.delete(msg);

        msg.undeliverable = true;

        if (deadMessages == null)
          deadMessages = new ClientMessages();
        deadMessages.addMessage(msg);
      }
      // Else, putting the message back into the deliverables vector:
      else
        storeMessage(msg);

      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Message "
                                      + msgId + " denied.");
    }

    // Sending the dead messages to the DMQ, if needed:
    if (deadMessages != null)
      sendToDMQ(deadMessages, null);
    
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
    receiving = true;

    Message msg;
    // Storing each received message:
    for (Enumeration msgs = not.getMessages().elements();
         msgs.hasMoreElements();) {

      if (arrivalsCounter == Long.MAX_VALUE)
        arrivalsCounter = 0;

      msg = (Message) msgs.nextElement();
      msg.order = arrivalsCounter++;
      storeMessage(msg);
    }

    // Lauching a delivery sequence:
    deliverMessages(0);

    receiving = false;
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

    String msgId;
    Message msg;
    AgentId consId;
    ClientMessages deadMessages = null;
    for (Enumeration e = deliveredMsgs.keys(); e.hasMoreElements();) {
      msgId = (String) e.nextElement();
      msg = (Message) deliveredMsgs.get(msgId);
      consId = (AgentId) consumers.get(msgId);
      // Delivered message has been delivered to the deleted client:
      // denying it.
      if (consId.equals(client)) {
        deliveredMsgs.remove(msgId);
        msg.denied = true;

        consumers.remove(msgId);
        contexts.remove(msgId);

        // If message considered as undeliverable, adding it to the
        // vector of dead messages:
        if (isUndeliverable(msg)) {
          persistenceModule.delete(msg);
          msg.undeliverable = true;
          if (deadMessages == null)
            deadMessages = new ClientMessages();
          deadMessages.addMessage(msg);
        }
        // Else, putting it back into the deliverables vector:
        else
          storeMessage(msg);

        if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
          MomTracing.dbgDestination.log(BasicLevel.WARN, "Message "
                                        + msg.getIdentifier() + " denied.");
      }
    }
    // Sending dead messages to the DMQ, if needed:
    if (deadMessages != null)
      sendToDMQ(deadMessages, null);

    // Launching a delivery sequence:
    deliverMessages(0);

    // Commiting the message persistence orders.
    persistenceModule.commit();
  }

  /**
   * Method specifically processing a
   * <code>fr.dyade.aaa.agent.DeleteNot</code> instance.
   * <p>
   * <code>ExceptionReply</code> replies are sent to the pending receivers,
   * and the remaining messages are sent to the DMQ and deleted.
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
      Message msg;
      ClientMessages deadMessages = new ClientMessages();
      while (! messages.isEmpty()) {
        msg = (Message) messages.remove(0);
        msg.deletedDest = true;
        deadMessages.addMessage(msg);
      }
      sendToDMQ(deadMessages, null);
    }

    // Deleting the messages:
    persistenceModule.deleteAll();
  }

  /**
   * Actually stores a message in the deliverables vector.
   *
   * @param message  The message to store.
   */
  protected void storeMessage(Message message)
  {
    if (messages.isEmpty()) {
      samePriorities = true;
      priority = message.getPriority();
    }
    else if (samePriorities && priority != message.getPriority())
      samePriorities = false;

    // Constant priorities: no need to insert the message according to
    // its priority.
    if (samePriorities) {
      // Message being received: adding it at the end of the queue.
      if (receiving)
        messages.add(message);
      // Denying or recovery: adding the message according to its original
      // arrival order.
      else {
        long currentO;
        int i = 0;
        for (Enumeration enum = messages.elements();
             enum.hasMoreElements();) {
          currentO = ((Message) enum.nextElement()).order;

          if (currentO > message.order)
            break;

          i++;
        }
        messages.insertElementAt(message, i);
      }
    }
    // Non constant priorities: inserting the message according to its 
    // priority.
    else {
      Message currentMsg;
      int currentP;
      long currentO;
      int i = 0;
      for (Enumeration enum = messages.elements(); enum.hasMoreElements();) {
        currentMsg = (Message) enum.nextElement();
        currentP = currentMsg.getPriority();
        currentO = currentMsg.order;

        // Message denied or recovered, priorities are equal: inserting the
        // message according to its original arrival order.
        if (! receiving && currentP == message.getPriority()) {
          if (currentO > message.order)
            break;
        }
        // Current priority lower than the message to store: inserting it.
        else if (currentP < message.getPriority())
          break;

        i++;
      }
      messages.insertElementAt(message, i);
    }

    // Persisting the message.
    persistenceModule.save(message);

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Message "
                                    + message.getIdentifier() + " stored.");
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
    ClientMessages deadMessages = null;

    // Processing each request as long as there are deliverable messages:
    while (! messages.isEmpty() && index < requests.size()) { 
      notRec = (ReceiveRequest) requests.get(index);
      replied = false;

      // Checking the request validity:
      if (notRec.isValid()) {
        // Checking the deliverable messages:
        while (j < messages.size()) {
          msg = (Message) messages.get(j);

          // If the message is still valid:
          if (msg.isValid()) {
            // If selector matches, sending the message:
            if (Selector.matches(msg, notRec.getSelector())) {
              messages.remove(j);
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
                persistenceModule.delete(msg);
              // Else, putting the message in the delivered messages table:
              else {
                consumers.put(msg.getIdentifier(), notRec.requester);
                contexts.put(msg.getIdentifier(),
                             new Integer(notRec.getClientContext()));
                deliveredMsgs.put(msg.getIdentifier(), msg);
              }

              // Removing the request.
              replied = true;
              requests.remove(index);
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
            persistenceModule.delete(msg);
            
            msg.expired = true;

            if (deadMessages == null)
              deadMessages = new ClientMessages();
            deadMessages.addMessage(msg);
  
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
    if (deadMessages != null)
      sendToDMQ(deadMessages, null);
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


  /**
   * MBean method: returns <code>true</code> if the destination is freely
   * readable.
   */
  public boolean isFreelyReadable()
  {
    return freeReading;
  }

  /**
   * MBean method: returns <code>true</code> if the destination is freely
   * writeable.
   */
  public boolean isFreelyWriteable()
  {
    return freeWriting;
  }

  /**
   * MBean method: returns the identifiers of the readers on the destination.
   */
  public String getReaders()
  {
    Object key;
    int right;
    Vector readers = new Vector();
    for (Enumeration enum = clients.keys(); enum.hasMoreElements();) {
      key = enum.nextElement();
      right = ((Integer) clients.get(key)).intValue();
      if (right == READ || right == READWRITE)
        readers.add(key.toString());
    }
    return readers.toString();
  }

  /**
   * MBean method: returns the identifiers of the writers on the destination.
   */
  public String getWriters()
  {
    Object key;
    int right;
    Vector writers = new Vector();
    for (Enumeration enum = clients.keys(); enum.hasMoreElements();) {
      key = enum.nextElement();
      right = ((Integer) clients.get(key)).intValue();
      if (right == WRITE || right == READWRITE)
        writers.add(key.toString());
    }
    return writers.toString();
  }

  /** MBean interface implementation: deletes the destination. */
  public void delete()
  {
    DeleteDestination request = new DeleteDestination(destId.toString());
    Channel.sendTo(adminId, new MBeanNotification(request));
  }

  /**
   * MBean interface implementation: removes a given writer.
   *
   * @param writerId  Identifier of the writer.
   *
   * @exception Exception  If the identifier is invalid or if the specified
   *              writer is not a registered writer.
   */
  public void removeWriter(String writerId) throws Exception
  {
    try {
      if (writerId != null && ! isWriter(AgentId.fromString(writerId)))
        throw new Exception("Unknown writer identifier.");
    }
    catch (IllegalArgumentException exc) {
      throw new Exception("Invalid identifier.");
    }

    UnsetWriter request = new UnsetWriter(writerId, destId.toString());
    Channel.sendTo(adminId, new MBeanNotification(request));
  }
        

  /**
   * MBean interface implementation: removes a given reader.
   *
   * @param readerId  Identifier of the reader.
   *
   * @exception Exception  If the identifier is invalid or if the specified
   *              reader is not a registered reader.
   */
  public void removeReader(String readerId) throws Exception
  {
    try {
      if (readerId != null && ! isReader(AgentId.fromString(readerId)))
        throw new Exception("Unknown reader identifier.");
    }
    catch (IllegalArgumentException exc) {
      throw new Exception("Invalid identifier.");
    }

    UnsetReader request = new UnsetReader(readerId, destId.toString());
    Channel.sendTo(adminId, new MBeanNotification(request));
  }

  /**
   * MBean interface implementation: adds a given writer on the destination.
   *
   * @param writerId  Identifier of the writer.
   *
   * @exception Exception  If the identifier is invalid.
   */
  public void addWriter(String writerId) throws Exception
  {
    try {
      AgentId.fromString(writerId);
    }
    catch (IllegalArgumentException exc) {
      throw new Exception("Invalid identifier.");
    }
    SetWriter request = new SetWriter(writerId, destId.toString());
    Channel.sendTo(adminId, new MBeanNotification(request));
  }

  /**
   * MBean interface implementation: adds a given reader on the destination.
   *
   * @param readerId  Identifier of the reader.
   *
   * @exception Exception  If the identifier is invalid.
   */
  public void addReader(String readerId) throws Exception
  {
    try {
      AgentId.fromString(readerId);
    }
    catch (IllegalArgumentException exc) {
      throw new Exception("Invalid identifier.");
    }
    SetReader request = new SetReader(readerId, destId.toString());
    Channel.sendTo(adminId, new MBeanNotification(request));
  }

  /**
   * MBean interface implementation: removes free writing access on
   * the destination.
   */
  public void removeFreeWriting()
  {
    UnsetWriter request = new UnsetWriter(null, destId.toString());
    Channel.sendTo(adminId, new MBeanNotification(request));
  }  

  /**
   * MBean interface implementation: removes free reading access on
   * the destination.
   */
  public void removeFreeReading()
  {
    UnsetReader request = new UnsetReader(null, destId.toString());
    Channel.sendTo(adminId, new MBeanNotification(request));
  }  

  /**
   * MBean interface implementation: provides free writing access on
   * the destination.
   */
  public void provideFreeWriting()
  {
    try {
      addWriter(null);
    }
    catch (Exception exc) {}
  }

  /**
   * MBean interface implementation: provides free reading access on
   * the destination.
   */
  public void provideFreeReading()
  {
    try {
      addReader(null);
    }
    catch (Exception exc) {}
  }

  /**
   * MBean interface implementation: returns the number of pending messages.
   */
  public int getNumberOfPendingMessages()
  {
    return messages.size();
  }

  /**
   * MBean interface implementation: returns the number of pending requests.
   */
  public int getNumberOfPendingRequests()
  {
    return requests.size();
  }


  /** Deserializes a <code>QueueImpl</code> instance. */
  private void readObject(java.io.ObjectInputStream in)
               throws IOException, ClassNotFoundException
  {
    in.defaultReadObject();

    receiving = false;
    messages = new Vector();
    deliveredMsgs = new Hashtable();

    // Retrieving the persisted messages, if any.
    Vector persistedMsgs = persistenceModule.loadAll();

    if (persistedMsgs != null) {
      persistenceModule.deleteAll();
      Message persistedMsg;
      AgentId consId;
      while (! persistedMsgs.isEmpty()) {
        persistedMsg = (Message) persistedMsgs.remove(0);
        consId = (AgentId) consumers.get(persistedMsg.getIdentifier());
        if (consId == null)
          storeMessage(persistedMsg);
        else {
          deliveredMsgs.put(persistedMsg.getIdentifier(), persistedMsg);
          persistenceModule.save(persistedMsg);
        }
      }
    }
    // Commiting the messages persistence orders.
    persistenceModule.commit();
  }
}
