/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - ScalAgent Distributed Technologies
 * Copyright (C) 2004 - France Telecom R&D
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
package org.objectweb.joram.mom.proxies;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import org.objectweb.joram.mom.MomTracing;
import org.objectweb.joram.mom.dest.DeadMQueueImpl;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.util.MessagePersistenceModule;
import org.objectweb.joram.shared.client.ConsumerMessages;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.joram.shared.selectors.Selector;

import org.objectweb.util.monolog.api.BasicLevel;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


/**
 * The <code>ClientSubscription</code> class holds the data of a client
 * subscription, and the methods managing the delivery and acknowledgement
 * of the messages.
 */
class ClientSubscription implements java.io.Serializable {
  /** The proxy's agent identifier. */
  private AgentId proxyId;
  /** <code>true</code> if the subscription is durable. */
  private boolean durable;
  /** The topic identifier. */
  private AgentId topicId;
  /** The subscription name. */
  private String name;
  /** The selector for filtering messages. */
  private String selector;
  /**
   * Identifier of the subscriber's dead message queue, <code>null</code> for
   * DMQ not set.
   */
  private AgentId dmqId;
  /**
   * Threshold value, 0 or negative for no threshold, <code>null</code> for
   * value not set.
   */
  private Integer threshold;

  /** Vector of identifiers of the messages to deliver. */
  private Vector messageIds;
  /** Table of delivered messages identifiers. */
  private Hashtable deliveredIds;
  /** Table keeping the denied messages identifiers. */
  private Hashtable deniedMsgs;

  /** Identifier of the subscription context. */
  private transient int contextId;
  /** Identifier of the subscription request. */
  private transient int subRequestId;
  /**
   * <code>true</code> if the subscriber does not wish to consume 
   * messages published in the same context.
   */
  private transient boolean noLocal;
  /**
   * <code>true</code> if the subscription does not filter messages
   * in any way.
   */
  private transient boolean noFiltering;

  /** <code>true</code> if the subscription is active. */
  private transient boolean active;
  /**
   * Identifier of the request requesting messages, either the listener's
   * request, or a "receive" request.
   */
  private transient int requestId;
  /** <code>true</code> if the messages are destinated to a listener. */
  private transient boolean toListener;
  /** Expiration time of the "receive" request, if any. */
  private transient long requestExpTime;

  /** Messages persistence module. */
  private transient MessagePersistenceModule persistenceModule;
  /** Proxy messages table. */
  private transient Hashtable messagesTable;

  /**
   * Constructs a <code>ClientSubscription</code> instance.
   *
   * @param proxyId  Proxy's identifier.
   * @param contextId  Context identifier.
   * @param reqId  Request identifier.
   * @param durable  <code>true</code> for a durable subscription.
   * @param topicId  Topic identifier.
   * @param name  Subscription's name.
   * @param selector  Selector for filtering messages.
   * @param noLocal  <code>true</code> for not consuming messages published
   *          within the same proxy's context.
   * @param dmqId  Identifier of the proxy's dead message queue, if any.
   * @param threshold  Proxy's threshold value, if any.
   * @param persistenceModule  Messages' persistence module.
   * @param messagesTable  Proxy's messages table.
   */
  ClientSubscription(AgentId proxyId,
                     int contextId,
                     int reqId,
                     boolean durable,
                     AgentId topicId,
                     String name,
                     String selector,
                     boolean noLocal,
                     AgentId dmqId,
                     Integer threshold,
                     MessagePersistenceModule persistenceModule,
                     Hashtable messagesTable)
  {
    this.proxyId = proxyId;
    this.contextId = contextId;
    this.subRequestId = reqId;
    this.durable = durable;
    this.topicId = topicId;
    this.name = name;
    this.selector = selector;
    this.noLocal = noLocal;
    this.dmqId = dmqId;
    this.threshold = threshold;
    this.persistenceModule = persistenceModule;
    this.messagesTable = messagesTable;

    messageIds = new Vector();
    deliveredIds = new Hashtable();
    deniedMsgs = new Hashtable();

    noFiltering = (! noLocal) && (selector == null || selector.equals(""));

    active = true;
    requestId = -1;
    toListener = false;

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG,
                              this + ": created.");
  }


  public String toString()
  {
    return "ClientSubscription" + proxyId + name;
  }


  /** Returns the subscription's context identifier. */
  int getContextId()
  {
    return contextId;
  }

  /** Returns the identifier of the subscribing request. */
  int getSubRequestId()
  {
    return subRequestId;
  }

  /** Returns the name of the subscription. */
  String getName()
  {
    return name;
  }

  /** Returns the identifier of the subscription topic. */
  AgentId getTopicId()
  {
    return topicId;
  }

  /** Returns the selector. */
  String getSelector()
  {
    return selector;
  }

  /** Returns <code>true</code> if the subscription is durable. */
  boolean getDurable()
  {
    return durable;
  }

  /** Returns <code>true</code> if the subscription is active. */
  boolean getActive()
  {
    return active;
  }

  
  /**
   * Re-initializes the client subscription.
   *
   * @param persistenceModule  Messages' persistence module.
   * @param messagesTable  Proxy's table where storing the messages.
   * @param persistedMessages  Proxy's persisted messages.
   * @param denyDeliveredMessages Denies already delivered messages.
   */
  void reinitialize(MessagePersistenceModule persistenceModule,
                    Hashtable messagesTable,
                    Vector persistedMessages,
                    boolean denyDeliveredMessages)
  {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG,
                              "ClientSubscription[" + this + 
                              "].reinitialize()");
    
    this.persistenceModule = persistenceModule;
    this.messagesTable = messagesTable;

    // Browsing the persisted messages.
    Message message;
    String msgId;
    for (Enumeration enum = persistedMessages.elements();
         enum.hasMoreElements();) {
      message = (Message) enum.nextElement();
      msgId = message.getIdentifier();

      if (messageIds.contains(msgId) || deliveredIds.contains(msgId)) {
        if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgProxy.log(
            BasicLevel.DEBUG,
            " -> contains message " + msgId);
        message.acksCounter++;
        message.durableAcksCounter++;
        
        if (message.acksCounter == 1) {
          if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
            MomTracing.dbgProxy.log(
              BasicLevel.DEBUG,
              " -> messagesTable.put(" + msgId + ')');
          messagesTable.put(msgId, message);
        }
        if (message.durableAcksCounter == 1) {
          if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
            MomTracing.dbgProxy.log(
              BasicLevel.DEBUG,
              " -> persistenceModule.save(" + message + ')');
          persistenceModule.save(message);          
        }
      }
    }

    if (denyDeliveredMessages) {
      // Denying all previously delivered messages:
      deny(deliveredIds.keys());
      deliveredIds.clear();
    }
  }

  /** 
   * Reactivates the subscription.
   *
   * @param context  Re-activation context.
   * @param reqId  Re-activation request identifier.
   * @param topicId  Topic identifier.
   * @param selector  Selector for filtering messages.
   * @param noLocal  <code>true</code> for not consuming messages published
   *          within the same proxy's context.
   */
  void reactivate(int contextId,
                  int reqId,
                  AgentId topicId,
                  String selector,
                  boolean noLocal)
  {
    this.contextId = contextId;
    this.subRequestId = reqId;
    this.topicId = topicId;
    this.selector = selector;
    this.noLocal = noLocal;

    noFiltering = (! noLocal) && (selector == null || selector.equals(""));

    active = true;
    requestId = -1;
    toListener = false;

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG,
                              this + ": reactivated.");
  }

  /** De-activates the subscription, denies the non acknowledgded messages. */  
  void deactivate()
  {
    unsetListener();
    unsetReceiver();
    active = false;
   
    // Denying all delivered messages:
    deny(deliveredIds.keys());
    deliveredIds.clear();

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG,
                              this + ": deactivated.");
  }

  /**
   * Sets a listener.
   *
   * @param requestId  Identifier of the listener request.
   */   
  void setListener(int requestId)
  {
    this.requestId = requestId;
    toListener = true;

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG,
                              this + ": listener set.");
  }

  /** Unsets the listener. */
  void unsetListener()
  {
    requestId = -1;
    toListener = false;

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG,
                              this + ": listener unset.");
  }

  /**
   * Sets a receiver request.
   *
   * @param requestId  Identifier of the "receive" request.
   * @param timeToLive  Request's time to live value.
   */
  void setReceiver(int requestId, long timeToLive)
  {
    this.requestId = requestId;
    toListener = false;

    if (timeToLive > 0)
      requestExpTime = System.currentTimeMillis() + timeToLive;
    else
      requestExpTime = 0;
  }

  /** Unsets a receiver request. */
  void unsetReceiver()
  {
    requestId = -1;
    requestExpTime = 0;
  }

  /** Sets the subscription's dead message queue identifier. */
  void setDMQId(AgentId dmqId)
  {
    this.dmqId = dmqId;
  }

  /** Sets the subscription's threshold value. */
  void setThreshold(Integer threshold)
  {
    this.threshold = threshold;
  }

  
  /**
   * Browses messages and keeps those which will have to be delivered
   * to the subscriber.
   */
  void browseNewMessages(Vector newMessages)
  {
    // Browsing the messages one by one.
    Message message;
    String msgId;
    for (Enumeration enum = newMessages.elements(); enum.hasMoreElements();) {
      message = (Message) enum.nextElement();
      msgId = message.getIdentifier();

      // Keeping the message if filtering is successful.
      if (noFiltering
          || (Selector.matches(message, selector)
              && (! noLocal
                  || (msgId.indexOf(proxyId.toString()) == -1
                      || msgId.indexOf("c" + contextId + "m") == -1)))) {

        if (messagesTable.containsKey(msgId))
          message = (Message) messagesTable.get(msgId);
        else
          messagesTable.put(msgId, message);

        message.acksCounter++;

        if (durable) {
          message.durableAcksCounter++;
          if (message.durableAcksCounter == 1)
            persistenceModule.save(message);
        }

        messageIds.add(msgId);

        if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgProxy.log(BasicLevel.DEBUG,
                                  this + ": added msg " + msgId
                                  + " for delivery.");
      }
    }
  }

  /**
   * Launches a delivery sequence, either for a listener, or for a receiver.
   */
  ConsumerMessages deliver()
  {
    // Returning null if no request exists:
    if (requestId == -1)
      return null;

     // Returning null if a "receive" request has expired:
    if (! toListener
        && requestExpTime > 0
        && System.currentTimeMillis() >= requestExpTime) {
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                      this + ": receive request " + requestId
                                      + " expired.");
      requestId = -1;
      requestExpTime = 0;
      return null;
    }

    String id;
    Message message;
    Integer deliveryAttempts = null;
    int lastPrior = -1;
    int insertionIndex = -1;
    int prior;
    Vector deliverables = new Vector();
    ClientMessages deadMessages = null;

    // Delivering to a listener.
    if (toListener) {
      // Browsing the identifiers of the messages to deliver.
      while (! messageIds.isEmpty()) {
        id = (String) messageIds.remove(0);
        message = (Message) messagesTable.get(id);
        // Message still exists.
        if (message != null) {
          // Delivering it if valid.
          if (message.isValid()) {
            deliveredIds.put(id, id);

            // Setting the message's deliveryCount and denied fields.
            deliveryAttempts = (Integer) deniedMsgs.get(id);
            if (deliveryAttempts == null)
              message.deliveryCount = 1;
            else {
              message.deliveryCount = deliveryAttempts.intValue() + 1;
              message.denied = true;
            }

            // Inserting it according to its priority.
            if (lastPrior == -1 || message.getPriority() == lastPrior)
              insertionIndex++;
            else {
              insertionIndex = 0;
              while (insertionIndex < deliverables.size()) {
                prior =
                  ((Message) deliverables.get(insertionIndex)).getPriority();
                if (prior >= message.getPriority())
                  insertionIndex++;
                else
                  break;
              }
            }
            lastPrior = message.getPriority();
            deliverables.insertElementAt(message.clone(), insertionIndex);

            if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
              MomTracing.dbgProxy.log(BasicLevel.DEBUG, 
                                      this + ": message " + id
                                      + " added for delivery.");
          }
          // Invalid message: removing and adding it to the vector of dead
          // messages.
          else {
            messagesTable.remove(id);
            // Deleting the message, if needed.
            if (durable)
              persistenceModule.delete(message);

            // Setting the message's deliveryCount, denied and expired fields.
            deliveryAttempts = (Integer) deniedMsgs.remove(id);
            if (deliveryAttempts != null) {
              message.deliveryCount = deliveryAttempts.intValue();
              message.denied = true;
            }
            message.expired = true;
            if (deadMessages == null)
              deadMessages = new ClientMessages();
            deadMessages.addMessage(message);
          }
        }
        // Message has already been deleted.
        else
          deniedMsgs.remove(id);
      }
    }
    // Delivering to a receiver: getting the highest priority message.
    else {
      int highestP = -1;
      Message keptMsg = null;
      // Browsing the non delivered messages.
      for (Enumeration ids = messageIds.elements(); ids.hasMoreElements();) {
        id = (String) ids.nextElement();
        message = (Message) messagesTable.get(id);
        // Message still exists.
        if (message != null) {
          // Checking valid message.
          if (message.isValid()) {
            // Higher priority: keeping the message.
            if (message.getPriority() > highestP) {
              highestP = message.getPriority();
              keptMsg = message;
            }
          }
          // Invalid message: removing and adding it to the vector of dead
          // messages.
          else {
            messageIds.remove(id);
            messagesTable.remove(id);
            // Deleting the message, if needed.
            if (durable)
              persistenceModule.delete(message);

            // Setting the message's deliveryCount, denied and expired fields.
            deliveryAttempts = (Integer) deniedMsgs.remove(id);
            if (deliveryAttempts != null) {
              message.deliveryCount = deliveryAttempts.intValue();
              message.denied = true;
            }
            message.expired = true;
            deadMessages = new ClientMessages();
            deadMessages.addMessage(message);
          }
        }
        // Message has already been deleted.
        else {
          messageIds.remove(id);
          deniedMsgs.remove(id);
        }
      }
      // Putting the kept message in the vector.
      if (keptMsg != null) {
        messageIds.remove(keptMsg.getIdentifier());
        deliveredIds.put(keptMsg.getIdentifier(), keptMsg.getIdentifier());

        // Setting the message's deliveryCount and denied fields.
        deliveryAttempts = (Integer) deniedMsgs.get(keptMsg.getIdentifier());
        if (deliveryAttempts == null)
          keptMsg.deliveryCount = 1;
        else {
          keptMsg.deliveryCount = deliveryAttempts.intValue() + 1;
          keptMsg.denied = true;
        }
        deliverables.add(keptMsg.clone());

        if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgProxy.log(BasicLevel.DEBUG, 
                                  this + ": message " + keptMsg.getIdentifier()
                                  + " added for delivery.");
      }
    }
   
    // Sending the dead messages to the DMQ, if any:
    if (deadMessages != null)
      sendToDMQ(deadMessages);

    // Finally, returning the reply or null:
    if (! deliverables.isEmpty()) {
      ConsumerMessages consM = new ConsumerMessages(requestId,
                                                    deliverables,
                                                    name,
                                                    false);
      if (! toListener)
        requestId = -1;

      return consM;
    }
    return null;
  }

  /**
   * Acknowledges messages.
   */
  void acknowledge(Enumeration acks)
  {
    String id;
    Message msg;
    while (acks.hasMoreElements()) {
      id = (String) acks.nextElement();

      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(BasicLevel.DEBUG, 
                                this + ": acknowledges message: " + id);

      deliveredIds.remove(id);
      deniedMsgs.remove(id);
      msg = (Message) messagesTable.get(id);

      if (msg != null) {
        msg.acksCounter--;
        if (msg.acksCounter == 0)
          messagesTable.remove(id);
        if (durable) {
          msg.durableAcksCounter--;

          if (msg.durableAcksCounter == 0)
            persistenceModule.delete(msg);
        }
      }
    }
  }

  /**
   * Denies messages.
   */
  void deny(Enumeration denies)
  {
    String id;
    Message msg;
    ClientMessages deadMessages = null;
    int deliveryAttempts = 1;
    int i;
    String currentId;
    long currentO;

    while (denies.hasMoreElements()) {
      id = (String) denies.nextElement();

      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(BasicLevel.DEBUG, 
                                this + ": denies message: " + id);

      deliveredIds.remove(id);
      msg = (Message) messagesTable.get(id);
      
      Integer value = (Integer) deniedMsgs.get(id);
      if (value != null)
        deliveryAttempts = value.intValue() + 1;

      // If maximum delivery attempts reached, the message is no more
      // deliverable to this sbscriber.
      if (isUndeliverable(deliveryAttempts)) {
        deniedMsgs.remove(id);
        msg.deliveryCount = deliveryAttempts;
        msg.undeliverable = true;
        if (deadMessages == null)
          deadMessages = new ClientMessages();
        deadMessages.addMessage(msg);

        msg.acksCounter--;
        if (msg.acksCounter == 0)
          messagesTable.remove(id);

        if (durable) {
          msg.durableAcksCounter--;
          if (msg.durableAcksCounter == 0)
            persistenceModule.delete(msg);
        }
      }
      // Else, putting it back to the deliverables vector according to its
      // original delivery order, and adding a new entry for it in the
      // denied messages table.
      else {
        i = 0;
        for (Enumeration enum = messageIds.elements();
             enum.hasMoreElements();) {
          currentId = (String) enum.nextElement();
          currentO = ((Message) messagesTable.get(currentId)).order;

          if (currentO > msg.order)
            break;
       
          i++;
        }
        messageIds.insertElementAt(id, i);
        deniedMsgs.put(id, new Integer(deliveryAttempts));
      }
    }

    // Sending dead messages to the DMQ, if needed:
    if (deadMessages != null)
      sendToDMQ(deadMessages);

  }

  /**
   * Decreases the subscription's messages acknowledgement expectations,
   * deletes those not to be consumed anymore.
   */
  void delete()
  {
    for (Enumeration enum = deliveredIds.keys(); enum.hasMoreElements();)
      messageIds.add(enum.nextElement());

    String id;
    Message msg;
    for (Enumeration allMessageIds = messageIds.elements();
         allMessageIds.hasMoreElements();) {

      id = (String) allMessageIds.nextElement();
      msg = (Message) messagesTable.get(id);

      if (msg != null) {
        msg.acksCounter--;
        if (msg.acksCounter == 0)
          messagesTable.remove(id);
        if (durable) {
          msg.durableAcksCounter--;
          if (msg.durableAcksCounter == 0)
            persistenceModule.delete(msg);
        }
      }
    }
  }

  
  /**
   * Returns <code>true</code> if a given value matches the threshold value
   * for this user.
   */
  private boolean isUndeliverable(int deliveryAttempts)
  {
    if (threshold != null)
      return deliveryAttempts == threshold.intValue();
    else if (DeadMQueueImpl.getThreshold() != null)
      return deliveryAttempts == DeadMQueueImpl.getThreshold().intValue();
    return false;
  }

  /**
   * Method used for sending messages to the appropriate dead message queue.
   */
  private void sendToDMQ(ClientMessages messages)
  {
    if (dmqId != null)
      Channel.sendTo(dmqId, messages);
    else if (DeadMQueueImpl.getId() != null)
      Channel.sendTo(DeadMQueueImpl.getId(), messages);
  }
}
