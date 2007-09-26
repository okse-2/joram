/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2006 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.proxies;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import org.objectweb.joram.mom.dest.DeadMQueueImpl;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.messages.Message;
import org.objectweb.joram.shared.client.ConsumerMessages;
import org.objectweb.joram.shared.selectors.Selector;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;

import org.objectweb.joram.shared.JoramTracing;
import org.objectweb.util.monolog.api.BasicLevel;

/**
 * The <code>ClientSubscription</code> class holds the data of a client
 * subscription, and the methods managing the delivery and acknowledgement
 * of the messages.
 */
class ClientSubscription implements java.io.Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
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

  /** nb Max of Message store in queue (-1 no limit). */
  protected int nbMaxMsg = -1;

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

  /**
   * Proxy messages table.
   * AF: Currently this table is shared between all subscription.
   */
  private transient Hashtable messagesTable;

  /** string proxy agent id */
  private transient String proxyStringId;
  
  private transient ProxyAgentItf proxy;

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
    this.messagesTable = messagesTable;

    messageIds = new Vector();
    deliveredIds = new Hashtable();
    deniedMsgs = new Hashtable();

    noFiltering = (! noLocal) && (selector == null || selector.equals(""));

    active = true;
    requestId = -1;
    toListener = false;

    proxyStringId = proxyId.toString();

    if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgProxy.log(BasicLevel.DEBUG,
                              this + ": created.");
  }

//    public String dump() {
//      StringBuffer buff = new StringBuffer();
//      buff.append("ClientSubscription (proxyId=");
//      buff.append(proxyId);
//      buff.append(",topicId=");
//      buff.append(topicId);
//      buff.append(",messageIds=");
//      buff.append(messageIds);
//      buff.append(",contextId=");
//      buff.append(contextId);
//      buff.append(",subRequestId=");
//      buff.append(subRequestId);
//      buff.append(",noLocal=");
//      buff.append(noLocal);
//      buff.append(",active=");
//      buff.append(active);
//      buff.append(",requestId=");
//      buff.append(requestId);
//      buff.append(",toListener=");
//      buff.append(toListener);
//      buff.append(",messagesTable=");
//      buff.append(messagesTable);
//      buff.append(")");
//      return buff.toString();
//    }

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
   * Returns the maximum number of message for the subscription.
   * If the limit is unset the method returns -1.
   *
   * @return the maximum number of message for subscription if set;
   *	     -1 otherwise.
   */
  public int getNbMaxMsg() {
    return nbMaxMsg;
  }

  /**
   * Sets the maximum number of message for the subscription.
   *
   * @param nbMaxMsg the maximum number of message for subscription (-1 set
   *		     no limit).
   */
  public void setNbMaxMsg(int nbMaxMsg) {
    this.nbMaxMsg = nbMaxMsg;
  }

  /**
   * Returns the number of pending messages for the subscription.
   *
   * @return The number of pending message for the subscription.
   */
  int getMessageCount() {
    return messageIds.size();
  }

  /**
   * Returns the list of message's identifiers for the subscription.
   *
   * @return the list of message's identifiers for the subscription.
   */
  String[] getMessageIds() {
    String[] res = new String[messageIds.size()];
    messageIds.copyInto(res);
    return res;
  }
  
  void setProxyAgent(ProxyAgentItf px) {
    proxy = px;
  }
  
  /**
   * Re-initializes the client subscription.
   * 
   * @param proxyStringId  string proxy id.
   * @param messagesTable  Proxy's table where storing the messages.
   * @param persistedMessages  Proxy's persisted messages.
   * @param denyDeliveredMessages Denies already delivered messages.
   */
  void reinitialize(String proxyStringId,
                    Hashtable messagesTable,
                    Vector persistedMessages,
                    boolean denyDeliveredMessages)
  {
    if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgProxy.log(BasicLevel.DEBUG,
                              "ClientSubscription[" + this + 
                              "].reinitialize()");
    
    this.proxyStringId = proxyStringId;
    this.messagesTable = messagesTable;

    // Browsing the persisted messages.
    Message message;
    String msgId;
    for (Enumeration e = persistedMessages.elements(); e.hasMoreElements();) {
      message = (Message) e.nextElement();
      msgId = message.getIdentifier();

      if (messageIds.contains(msgId) || deliveredIds.contains(msgId)) {
        if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgProxy.log(
            BasicLevel.DEBUG,
            " -> contains message " + msgId);
        message.acksCounter++;
        message.durableAcksCounter++;
        
        if (message.acksCounter == 1) {
          if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
            JoramTracing.dbgProxy.log(
              BasicLevel.DEBUG,
              " -> messagesTable.put(" + msgId + ')');
          messagesTable.put(msgId, message);
        }
//          if (message.durableAcksCounter == 1) {
//            if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
//              JoramTracing.dbgProxy.log(
//                BasicLevel.DEBUG,
//                " -> save message " + message);
// it's alredy save.
//          message.save(proxyStringId);          
//        }
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
    
    // Some updated attributes are persistent
    save();

    if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgProxy.log(BasicLevel.DEBUG,
                              this + ": reactivated.");
  }

  /** De-activates the subscription, denies the non acknowledgded messages. */  
  void deactivate() {
    if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgProxy.log(BasicLevel.DEBUG,
                              "ClientSubscription.deactivate()");

    unsetListener();
    unsetReceiver();
    active = false;
   
    // Denying all delivered messages:
    deny(deliveredIds.keys());
    deliveredIds.clear();
    
    // deliveredIds is persistent
    save();

    if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgProxy.log(BasicLevel.DEBUG,
                              this + ": deactivated.");
  }

  void setActive(boolean active) {
    this.active = active;
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

    if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgProxy.log(BasicLevel.DEBUG,
                              this + ": listener set.");
  }

  /** Unsets the listener. */
  void unsetListener()
  {
    requestId = -1;
    toListener = false;

    if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgProxy.log(BasicLevel.DEBUG,
                              this + ": listener unset.");
  }

  /**
   * Sets a receiver request.
   *
   * @param requestId  Identifier of the "receive" request.
   * @param timeToLive  Request's time to live value.
   */
  void setReceiver(int requestId, long timeToLive) {
    if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgProxy.log(BasicLevel.DEBUG,
                              this + ".setReceiver(" + requestId + 
                              "," + timeToLive + ")");

    this.requestId = requestId;
    toListener = false;

    if (timeToLive > 0)
      requestExpTime = System.currentTimeMillis() + timeToLive;
    else
      requestExpTime = 0;
  }

  /** Unsets a receiver request. */
  void unsetReceiver() {
    if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgProxy.log(BasicLevel.DEBUG,
                              this + ".unsetReceiver()");
    requestId = -1;
    requestExpTime = 0;
  }

  /** Sets the subscription's dead message queue identifier. */
  void setDMQId(AgentId dmqId)
  {
    this.dmqId = dmqId;
    save();
  }

  /** Sets the subscription's threshold value. */
  void setThreshold(Integer threshold)
  {
    this.threshold = threshold;
    save();
  }

  
  /**
   * Browses messages and keeps those which will have to be delivered
   * to the subscriber.
   */
  // AF: TODO we should parse each message for each subscription
  // see ProxyImpl.doFwd
  void browseNewMessages(Vector newMessages) {
    if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgProxy.log(BasicLevel.DEBUG,
                              this + ".browseNewMessages(" + 
                              newMessages + ')');
    // Browsing the messages one by one.
    Message message;
    String msgId;
    for (Enumeration e = newMessages.elements(); e.hasMoreElements();) {
      message = (Message) e.nextElement();
      msgId = message.getIdentifier();

      // test nbMaxMsg
      if (nbMaxMsg > -1 && nbMaxMsg <= messageIds.size()) {
        ClientMessages deadMessages = new ClientMessages();
        deadMessages.addMessage(message.msg);
        sendToDMQ(deadMessages);
        continue;
      }

      // Keeping the message if filtering is successful.
      if (noFiltering ||
          (Selector.matches(message.msg, selector) &&
           (! noLocal || ! msgId.startsWith(proxyId.toString().substring(1) + "c" + contextId + "m", 3)))) {

        // It's the first delivery, adds the message to the proxy's table
        if (message.acksCounter == 0)

          messagesTable.put(msgId, message);
        
        message.acksCounter++;
        if (durable)
          message.durableAcksCounter++;

        messageIds.add(msgId);
        save();

        if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgProxy.log(BasicLevel.DEBUG,
                                  this + ": added msg " + msgId
                                  + " for delivery.");
      }
    }
  }

  /**
   * Launches a delivery sequence, either for a listener, or for a receiver.
   */
  ConsumerMessages deliver() {
    if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgProxy.log(
        BasicLevel.DEBUG,
        "ClientSubscription[" + proxyId + ',' + 
        topicId + ',' + name + "].deliver()");

    // Returning null if no request exists:
    if (requestId == -1)
      return null;

     // Returning null if a "receive" request has expired:
    if (! toListener
        && requestExpTime > 0
        && System.currentTimeMillis() >= requestExpTime) {
      if (JoramTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgDestination.log(BasicLevel.DEBUG,
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

    if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgProxy.log(BasicLevel.DEBUG, 
                                " -> messageIds.size() = " + messageIds.size());
    
    // Delivering to a listener.
    if (toListener) {
      // Browsing the identifiers of the messages to deliver.
      while (! messageIds.isEmpty()) {
        id = (String) messageIds.remove(0);
        save();
        message = (Message) messagesTable.get(id);

        // Message still exists.
        if (message != null) {
          // Delivering it if valid.
          if (message.isValid(System.currentTimeMillis())) {
            deliveredIds.put(id, id);

            // Setting the message's deliveryCount and denied fields.
            deliveryAttempts = (Integer) deniedMsgs.get(id);
            if (deliveryAttempts == null)
              message.msg.deliveryCount = 1;
            else {
              message.msg.deliveryCount = deliveryAttempts.intValue() + 1;
              message.msg.redelivered = true;
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
            deliverables.insertElementAt(message.msg.clone(), insertionIndex);

            if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
              JoramTracing.dbgProxy.log(BasicLevel.DEBUG, 
                                      this + ": message " + id
                                      + " added for delivery.");
          } else {
            // Invalid message: removing and adding it to the vector of dead
            // messages.
            messagesTable.remove(id);
            // Deleting the message, if needed.
            if (durable)
              message.delete();

            // Setting the message's deliveryCount, denied and expired fields.
            deliveryAttempts = (Integer) deniedMsgs.remove(id);
            if (deliveryAttempts != null) {
              message.msg.deliveryCount = deliveryAttempts.intValue();
              message.msg.redelivered = true;
            }
            message.msg.expired = true;
            if (deadMessages == null)
              deadMessages = new ClientMessages();
            deadMessages.addMessage(message.msg);
          }
        } else {
          // Message has already been deleted.
          deniedMsgs.remove(id);
        }
      }
    } else {
      // Delivering to a receiver: getting the highest priority message.
      int highestP = -1;
      Message keptMsg = null;
      // Browsing the non delivered messages.
      int i = 0;
      while (i < messageIds.size()) {
        id = (String) messageIds.elementAt(i);
        message = (Message) messagesTable.get(id);
        if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgProxy.log(BasicLevel.DEBUG, " -> message = " + message);
        
        // Message still exists.
        if (message != null) {
          // Checking valid message.
          if (message.isValid(System.currentTimeMillis())) {
            if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
              JoramTracing.dbgProxy.log(BasicLevel.DEBUG, " -> valid message");
            // Higher priority: keeping the message.
            if (message.getPriority() > highestP) {
              highestP = message.getPriority();
              keptMsg = message;
            }

            // get next message
            i++;
          } else {
            // Invalid message: removing and adding it to the vector of dead
            // messages.
            if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
              JoramTracing.dbgProxy.log(BasicLevel.DEBUG, " -> invalid message");
            messageIds.remove(id);
            save();
            messagesTable.remove(id);
            // Deleting the message, if needed.
            if (durable)
              message.delete();

            // Setting the message's deliveryCount, denied and expired fields.
            deliveryAttempts = (Integer) deniedMsgs.remove(id);
            if (deliveryAttempts != null) {
              message.msg.deliveryCount = deliveryAttempts.intValue();
              message.msg.redelivered = true;
            }
            message.msg.expired = true;
            deadMessages = new ClientMessages();
            deadMessages.addMessage(message.msg);
          }
        } else {
          // Message has already been deleted.
          if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
            JoramTracing.dbgProxy.log(BasicLevel.DEBUG, " -> deleted message");

          messageIds.remove(id);
          deniedMsgs.remove(id);
          save();
        }
      }

      // Putting the kept message in the vector.
      if (keptMsg != null) {
        messageIds.remove(keptMsg.getIdentifier());
        deliveredIds.put(keptMsg.getIdentifier(), keptMsg.getIdentifier());
        save();

        // Setting the message's deliveryCount and denied fields.
        deliveryAttempts = (Integer) deniedMsgs.get(keptMsg.getIdentifier());
        if (deliveryAttempts == null)
          keptMsg.msg.deliveryCount = 1;
        else {
          keptMsg.msg.deliveryCount = deliveryAttempts.intValue() + 1;
          keptMsg.msg.redelivered = true;
        }
        deliverables.add(keptMsg.msg.clone());

        if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgProxy.log(BasicLevel.DEBUG, 
                                  this + ": message " + keptMsg.getIdentifier()
                                  + " added for delivery.");
      } else {
        i++;
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
      if (! toListener) requestId = -1;

      return consM;
    }
    return null;
  }

  /**
   * Acknowledges messages.
   */
  void acknowledge(Enumeration acks) {
    while (acks.hasMoreElements()) {
      String id = (String) acks.nextElement();
      acknowledge(id);
    }
  }

  void acknowledge(String id) {
    if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgProxy.log(BasicLevel.DEBUG, 
                              this + ": acknowledges message: " + id);
    
    deliveredIds.remove(id);
    deniedMsgs.remove(id);
    save();
    Message msg = (Message) messagesTable.get(id);
    
    // Message may be null if it is not valid anymore
    if (msg != null) {
      msg.acksCounter--;
      if (msg.acksCounter == 0)
        messagesTable.remove(id);
      if (durable) {
        msg.durableAcksCounter--;
        
        if (msg.durableAcksCounter == 0)
          msg.delete();
      }
    }
  }

  /**
   * Denies messages.
   */
  void deny(Enumeration denies) {
    if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgProxy.log(BasicLevel.DEBUG, 
                              this + ".deny(" + denies + ')');
    String id;
    Message message;
    ClientMessages deadMessages = null;
    int deliveryAttempts = 1;
    int i;
    String currentId;
    long currentO;

    denyLoop:
    while (denies.hasMoreElements()) {
      id = (String) denies.nextElement();

      String deliveredMsgId = (String)deliveredIds.remove(id);
      if (deliveredMsgId == null) {
        if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgProxy.log(BasicLevel.DEBUG, 
                                  this + ": cannot denies message: " + id);

        continue denyLoop;
      }
      save();
      
      if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgProxy.log(BasicLevel.DEBUG, 
                                this + ": denies message: " + id);
      
      message = (Message) messagesTable.get(id);
      
      // Message may be null if it is not valid anymore
      if (message == null) continue denyLoop;
      
      Integer value = (Integer) deniedMsgs.get(id);
      if (value != null)
        deliveryAttempts = value.intValue() + 1;
      
      // If maximum delivery attempts reached, the message is no more
      // deliverable to this sbscriber.
      if (isUndeliverable(deliveryAttempts)) {
        deniedMsgs.remove(id);
        message.msg.deliveryCount = deliveryAttempts;
        message.msg.undeliverable = true;
        if (deadMessages == null)
          deadMessages = new ClientMessages();
        deadMessages.addMessage(message.msg);
        
        message.acksCounter--;
        if (message.acksCounter == 0)
          messagesTable.remove(id);
        
        if (durable) {
          message.durableAcksCounter--;
          if (message.durableAcksCounter == 0)
            message.delete();
        }
      } else {
        // Else, putting it back to the deliverables vector according to its
        // original delivery order, and adding a new entry for it in the
        // denied messages table.
        if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgProxy.log(BasicLevel.DEBUG, 
                                    " -> put back to the messages to deliver");
        
        i = 0;
        insertLoop:
        while (i < messageIds.size()) {
          currentId = (String) messageIds.elementAt(i);
          Message currentMessage = (Message) messagesTable.get(currentId);
            
          // Message may be null if it is not valid anymore
          if (currentMessage != null) {
            currentO = currentMessage.order;
            if (currentO > message.order) {
              break insertLoop;
            } else {
              i++;
            }
          } else {
            // Remove the invalid message
            messageIds.removeElementAt(i);
          }
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
  void delete() {
    for (Enumeration e = deliveredIds.keys(); e.hasMoreElements();)
      messageIds.add(e.nextElement());

    for (Enumeration allMessageIds = messageIds.elements();
         allMessageIds.hasMoreElements();) {
      removeMessage((String) allMessageIds.nextElement());
    }
  }

  
  /**
   * Returns <code>true</code> if a given value matches the threshold value
   * for this user.
   */
  private boolean isUndeliverable(int deliveryAttempts) {
    if (threshold != null)
      return deliveryAttempts == threshold.intValue();
    else if (DeadMQueueImpl.getDefaultThreshold() != null)
      return deliveryAttempts == DeadMQueueImpl.getDefaultThreshold().intValue();
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

  Message getMessage(String msgId) {
    if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgProxy.log(
        BasicLevel.DEBUG, 
        "ClientSubscription.getMessage(" + msgId + ')');
    int index = messageIds.indexOf(msgId);
    if (index < 0) {
      // The message has been delivered
      if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgProxy.log(
          BasicLevel.DEBUG, " -> message not found");
      return null;
    } else {
      return (Message) messagesTable.get(msgId);
    }
  }

  void deleteMessage(String msgId) {
    messageIds.remove(msgId);
    Message message = removeMessage(msgId);
    save();
    if (message != null) {
      ClientMessages deadMessages = new ClientMessages();
      deadMessages.addMessage(message.msg);
      sendToDMQ(deadMessages);
    }
  }

  void clear() {
    ClientMessages deadMessages = null;
    for (int i = 0; i < messageIds.size(); i++) {
      String msgId = (String)messageIds.elementAt(i);
      Message message = removeMessage(msgId);
      if (message != null) {
        if (deadMessages == null)
          deadMessages = new ClientMessages();
        deadMessages.addMessage(message.msg);
      }
    }
    if (deadMessages != null)
      sendToDMQ(deadMessages);
    messageIds.clear();
    save();
  }

  /**
   * Removes a particular pending message in the subscription.
   * The message is pointed out through its unique identifier.
   *
   * @param msgId    The unique message's identifier.
   */
  Message removeMessage(String msgId) {
    Message message = (Message) messagesTable.get(msgId);
    if (message != null) {
      message.acksCounter--;
      if (message.acksCounter == 0)
        messagesTable.remove(msgId);
      if (durable) {
        message.durableAcksCounter--;
        if (message.durableAcksCounter == 0)
          message.delete();
      }
    }
    return message;
  }
  
  private void save() {
    if (durable) proxy.setSave();
  }

  public void readBag(ObjectInputStream in) 
    throws IOException, ClassNotFoundException {
    if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgProxy.log(
        BasicLevel.DEBUG,
        "ClientSubscription[" + 
        proxyId + 
        "].readbag()");

    contextId = in.readInt();
    subRequestId = in.readInt();
    noLocal = in.readBoolean();
    noFiltering = in.readBoolean();
    active = in.readBoolean();
    requestId = in.readInt();
    toListener = in.readBoolean();
    requestExpTime = in.readLong();
  }

  public void writeBag(ObjectOutputStream out)
    throws IOException {
    if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgProxy.log(
        BasicLevel.DEBUG,
        "ClientSubscription[" + 
        proxyId + 
        "].writeBag()");

    out.writeInt(contextId);
    out.writeInt(subRequestId);
    out.writeBoolean(noLocal);
    out.writeBoolean(noFiltering);
    out.writeBoolean(active);
    out.writeInt(requestId);
    out.writeBoolean(toListener);
    out.writeLong(requestExpTime);
  }
}
