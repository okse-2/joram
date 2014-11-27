/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2013 ScalAgent Distributed Technologies
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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.objectweb.joram.mom.dest.Queue;
import org.objectweb.joram.mom.messages.Message;
import org.objectweb.joram.mom.messages.MessageJMXWrapper;
import org.objectweb.joram.mom.util.DMQManager;
import org.objectweb.joram.mom.util.JoramHelper;
import org.objectweb.joram.mom.util.MessageIdList;
import org.objectweb.joram.mom.util.MessageIdListFactory;
import org.objectweb.joram.mom.util.MessageTable;
import org.objectweb.joram.shared.MessageErrorConstants;
import org.objectweb.joram.shared.client.ConsumerMessages;
import org.objectweb.joram.shared.excepts.RequestException;
import org.objectweb.joram.shared.selectors.Selector;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.encoding.Decoder;
import fr.dyade.aaa.common.encoding.Encodable;
import fr.dyade.aaa.common.encoding.EncodableFactory;
import fr.dyade.aaa.common.encoding.EncodableHelper;
import fr.dyade.aaa.common.encoding.Encoder;

/**
 * The <code>ClientSubscription</code> class holds the data of a client
 * subscription, and the methods managing the delivery and acknowledgement
 * of the messages.
 */
class ClientSubscription implements ClientSubscriptionMBean, Serializable, Encodable {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  public static Logger logger = Debug.getLogger(ClientSubscription.class.getName());
  
  public static final String MESSAGE_ID_LIST_PREFIX = "MIL_";
  
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
   * Threshold above which messages are considered as undeliverable because
   * constantly denied.
   * 0 stands for no threshold, -1 for value not set (use servers' default value).
   */
  private int threshold = -1;

  /**
   * Returns the threshold above which messages are considered undeliverable
   * because constantly denied.
   * 
   * @return  the threshold if set; -1 otherwise.
   */
  public int getThreshold() {
    return threshold;
  }
  
  /** Sets the subscription's threshold value. */
  public void setThreshold(int threshold) {
    this.threshold = threshold;
    setModified();
  }

  /** Max number of Message stored in the queue (-1 no limit). */
  protected int nbMaxMsg = -1;

  /**
   * Returns the maximum number of message for the subscription.
   * If the limit is unset the method returns -1.
   *
   * @return the maximum number of message for subscription if set;
   *       -1 otherwise.
   */
  public int getNbMaxMsg() {
    return nbMaxMsg;
  }

  /**
   * Sets the maximum number of message for the subscription.
   *
   * @param nbMaxMsg the maximum number of message for subscription (-1 set
   *         no limit).
   */
  public void setNbMaxMsg(int nbMaxMsg) {
    this.nbMaxMsg = nbMaxMsg;
  }

  /** Vector of identifiers of the messages to deliver. */
  private transient MessageIdList messageIds;
  /** Table of delivered messages identifiers. */
  private Map<String, String> deliveredIds;
  /** Table keeping the denied messages identifiers. */
  private Map<String, Integer> deniedMsgs;

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

  /** Give the maximum number of messages per request if the subscription is active,
   * 0 if the subscription is passive. */
  private transient int active;
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
   * Proxy messages table. Be careful: currently this table is shared between
   * all subscription.
   */
  private transient MessageTable messagesTable;

  private transient ProxyAgentItf proxy;
  
  /** the number of erroneous messages forwarded to the DMQ */
  protected long nbMsgsSentToDMQSinceCreation = 0;
  
  /** the number of delivered messages */
  protected long nbMsgsDeliveredSinceCreation = 0;

  private int DEFAULT_MAX_NUMBER_OF_MSG_PER_REQUEST = 100;
  
  public transient String txName;
  
  public transient boolean modified;
  
  private String clientID;
  
  ClientSubscription() {}
  
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
   * @param clientID the clientID
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
                     int threshold,
                     int nbMaxMsg,
                     MessageTable messagesTable,
                     String clientID) throws RequestException {
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
    this.nbMaxMsg = nbMaxMsg;
    this.messagesTable = messagesTable;
    this.clientID = clientID;

    // initialized in a separate method
    messageIds = null;
    
    deliveredIds = new Hashtable<String, String>();
    deniedMsgs = new Hashtable();

    noFiltering = (! noLocal) && (selector == null || selector.equals(""));

    active = DEFAULT_MAX_NUMBER_OF_MSG_PER_REQUEST;
    requestId = -1;
    toListener = false;
    
    modified = true;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + ": created.");
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

  public AgentId getProxyId() {
    return proxyId;
  }

  public void setProxyId(AgentId proxyId) {
    this.proxyId = proxyId;
  }

  public String toString() {
    return "ClientSubscription" + proxyId + name;
  }

  /** Returns the subscription's context identifier. */
  public int getContextId() {
    return contextId;
  }

  /** Returns the identifier of the subscribing request. */
  public int getSubRequestId() {
    return subRequestId;
  }

  /** Returns the name of the subscription. */
  public String getName() {
    return name;
  }

  /** Returns the identifier of the subscription topic. */
  public AgentId getTopicId() {
    return topicId;
  }
  
  /** Returns the identifier of the subscription topic. */
  public String getTopicIdAsString() {
    return topicId.toString();
  }

  /** Returns the selector. */
  public String getSelector() {
    return selector;
  }

  /** Returns <code>true</code> if the subscription is durable. */
  public boolean getDurable() {
    return durable;
  }

  /** Returns the maximum number of messages per request if the subscription is active. */
  public int getActive() {
    return active;
  }

  /**
   * Returns the number of pending messages for the subscription.
   *
   * @return The number of pending message for the subscription.
   */
  public int getPendingMessageCount() {
    return messageIds.size();
  }
  
  /**
   * Returns the number of messages delivered and waiting for acknowledge.
   *
   * @return The number of messages delivered and waiting for acknowledge.
   */
  public int getDeliveredMessageCount() {
    return deliveredIds.size();
  }

  /**
   * Returns the list of message's identifiers for the subscription.
   *
   * @return the list of message's identifiers for the subscription.
   */
  public String[] getMessageIds() {
    String[] res = new String[messageIds.size()];
    messageIds.toArray(res);
    return res;
  }
  
  void setProxyAgent(ProxyAgentItf px) {
    proxy = px;
  }
  
  /**
   * Re-initializes the client subscription.
   * 
   * @param messagesTable  Proxy's table where storing the messages.
   * @param persistedMessages  Proxy's persisted messages.
   * @param denyDeliveredMessages Denies already delivered messages.
   */
  void reinitialize(MessageTable messagesTable, List persistedMessages, boolean denyDeliveredMessages) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "ClientSubscription[" + this + "].reinitialize()");
    
    this.messagesTable = messagesTable;

    // Browsing the persisted messages.
    Message message;
    String msgId;
    for (Iterator e = persistedMessages.iterator(); e.hasNext();) {
      message = (Message) e.next();
      msgId = message.getId();

      if (messageIds.contains(msgId) || deliveredIds.containsKey(msgId)) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, " -> contains message " + msgId);
        message.acksCounter++;
        message.durableAcksCounter++;
        
        if (message.acksCounter == 1) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, " -> messagesTable.put(" + msgId + ')');
          messagesTable.put(message);
        }
//          if (message.durableAcksCounter == 1) {
        // if (logger.isLoggable(BasicLevel.DEBUG))
        // logger.log(
        //                BasicLevel.DEBUG,
//                " -> save message " + message);
// it's alredy save.
//          message.save(proxyStringId);          
//        }
      }
    }

    if (denyDeliveredMessages) {
      // Denying all previously delivered messages:
      HashSet h = new HashSet(deliveredIds.keySet());
      deny(h.iterator(), false);
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
                  boolean noLocal) {
    this.contextId = contextId;
    this.subRequestId = reqId;
    this.topicId = topicId;
    this.selector = selector;
    this.noLocal = noLocal;

    noFiltering = (! noLocal) && (selector == null || selector.equals(""));

    active = DEFAULT_MAX_NUMBER_OF_MSG_PER_REQUEST;
    requestId = -1;
    toListener = false;
    
    // Some updated attributes are persistent
    setModified();

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + ": reactivated.");
  }

  /** 
   * De-activates the subscription.
   * @param denies denies the non acknowledged messages. 
   */  
  void deactivate(boolean denies) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "ClientSubscription.deactivate(" + denies+ ')');

    unsetListener();
    unsetReceiver();
    active = 0;
   
    if (denies) {
      // Denying all delivered messages:
      HashSet h = new HashSet(deliveredIds.keySet());
      deny(h.iterator(), false);
      deliveredIds.clear();
      // deliveredIds is persistent
      setModified();
    }

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + ": deactivated.");
  }

  void setActive(int active) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "setActive(" + active + ')');
    this.active = active;
  }

  /**
   * Sets a listener.
   *
   * @param requestId  Identifier of the listener request.
   */   
  void setListener(int requestId) {
    this.requestId = requestId;
    toListener = true;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + ": listener set.");
  }

  /** Unsets the listener. */
  void unsetListener() {
    requestId = -1;
    toListener = false;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + ": listener unset.");
  }

  /**
   * Sets a receiver request.
   * 
   * @param requestId
   *            Identifier of the "receive" request.
   * @param timeToLive
   *            Request's time to live value.
   */
  void setReceiver(int requestId, long timeToLive) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + ".setReceiver(" + requestId + "," + timeToLive + ")");

    this.requestId = requestId;
    toListener = false;

    if (timeToLive > 0)
      requestExpTime = System.currentTimeMillis() + timeToLive;
    else
      requestExpTime = 0;
  }

  /** Unsets a receiver request. */
  void unsetReceiver() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + ".unsetReceiver()");
    requestId = -1;
    requestExpTime = 0;
  }

  /** Sets the subscription's dead message queue identifier. */
  void setDMQId(AgentId dmqId) {
    this.dmqId = dmqId;
    setModified();
  }
  
  /**
   * Browses messages and keeps those which will have to be delivered
   * to the subscriber.
   */
  // AF: TODO we should parse each message for each subscription
  // see UserAgent.doFwd
  void browseNewMessages(List newMessages) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + ".browseNewMessages(" + newMessages + ')');
    // Browsing the messages one by one.
    Message message;
    String msgId;
    DMQManager dmqManager = null;
    for (Iterator e = newMessages.iterator(); e.hasNext();) {
      message = (Message) e.next();
      msgId = message.getId();

      // test nbMaxMsg
      if (nbMaxMsg > 0 && nbMaxMsg <= messageIds.size()) {
        if (dmqManager == null)
          dmqManager = new DMQManager(dmqId, null);
        nbMsgsSentToDMQSinceCreation++;
        dmqManager.addDeadMessage(message.getFullMessage(), MessageErrorConstants.QUEUE_FULL);
        continue;
      }

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, this + ".browseNewMessages message.getClientID() = " + message.getClientID() + ", clientID = " + clientID + ", noLocal = " + noLocal);
      
      // Keeping the message if filtering is successful.
      if (noFiltering ||
          (Selector.matches(message.getHeaderMessage(), selector) &&
           ((message.getClientID() == null && clientID != null) 
               || !noLocal 
               || (message.getClientID() != null && ! message.getClientID().equals(clientID))) )) {

        // It's the first delivery, adds the message to the proxy's table
        if (message.acksCounter == 0)
          messagesTable.put(message);
        
        message.acksCounter++;
        if (durable)
          message.durableAcksCounter++;

        messageIds.add(msgId, message.isPersistent());
        if (message.isPersistent()) {
          setModified();
        }

        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, this + ": added msg " + msgId + " for delivery.");
      }
    }
    if (dmqManager != null) {
      dmqManager.sendToDMQ();
    }
  }

  /**
   * Launches a delivery sequence, either for a listener, or for a receiver.
   */
  ConsumerMessages deliver() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ClientSubscription[" + proxyId + ',' + topicId + ',' + name + "].deliver()");

    // Returning null if no request exists:
    if (requestId == -1)
      return null;

     // Returning null if a "receive" request has expired:
    if (!toListener && requestExpTime > 0 && System.currentTimeMillis() >= requestExpTime) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, this + ": receive request " + requestId + " expired.");
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
    DMQManager dmqManager = null;
    boolean isActive = true;
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "deliver -> messageIds = " + messageIds.size() + ", active = " + active + ", deliveredIds = " + deliveredIds.size());
    
    // Delivering to a listener.
    if (toListener) {
      // Browsing the identifiers of the messages to deliver.
      while (! messageIds.isEmpty()) {
        
        if (active < deliverables.size()) {
          // passivate 
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "passivate, active = " + active + " < deliverables = " + deliverables.size());
          isActive = false;
          break;
        }
        
        id = (String) messageIds.remove(0);
        setModified();
        message = (Message) messagesTable.get(id);

        if (message != null) {
          // Message still exists.
          if (message.isValid(System.currentTimeMillis())) {
            // Delivering it if valid.
            deliveredIds.put(id, id);

            // Setting the message's deliveryCount and denied fields.
            deliveryAttempts = (Integer) deniedMsgs.get(id);
            if (deliveryAttempts == null)
              message.setDeliveryCount(1);
            else {
              message.setDeliveryCount(deliveryAttempts.intValue() +1);
              message.setRedelivered();
            }

            // Inserting it according to its priority.
            if (lastPrior == -1 || message.getPriority() == lastPrior)
              insertionIndex++;
            else {
              insertionIndex = 0;
              while (insertionIndex < deliverables.size()) {
                prior = ((Message) deliverables.get(insertionIndex)).getPriority();
                if (prior >= message.getPriority())
                  insertionIndex++;
                else
                  break;
              }
            }
            lastPrior = message.getPriority();
            deliverables.add(insertionIndex, message.getFullMessage().clone());

            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, this + ": message " + id + " added for delivery.");
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
              message.setDeliveryCount(deliveryAttempts.intValue() +1);
              message.setRedelivered();
            }
            if (dmqManager == null) {
              dmqManager = new DMQManager(dmqId, null);
            }
            nbMsgsSentToDMQSinceCreation++;
            dmqManager.addDeadMessage(message.getFullMessage(), MessageErrorConstants.EXPIRED);
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
        id = (String) messageIds.get(i);
        message = (Message) messagesTable.get(id);
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, " -> message = " + message);

        // Message still exists.
        if (message != null) {
          // Checking valid message.
          if (message.isValid(System.currentTimeMillis())) {
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, " -> valid message");
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
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, " -> invalid message");
            messageIds.remove(id);
            setModified();
            messagesTable.remove(id);
            // Deleting the message, if needed.
            if (durable)
              message.delete();

            // Setting the message's deliveryCount, denied and expired fields.
            deliveryAttempts = (Integer) deniedMsgs.remove(id);
            if (deliveryAttempts != null) {
              message.setDeliveryCount(deliveryAttempts.intValue());
              message.setRedelivered();
            }
            
            if (dmqManager == null) {
              dmqManager = new DMQManager(dmqId, null);
            }
            nbMsgsSentToDMQSinceCreation++;
            dmqManager.addDeadMessage(message.getFullMessage(), MessageErrorConstants.EXPIRED);
          }
        } else {
          // Message has already been deleted.
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, " -> deleted message");

          messageIds.remove(id);
          deniedMsgs.remove(id);
          setModified();
        }
      }

      // Putting the kept message in the vector.
      if (keptMsg != null) {
        messageIds.remove(keptMsg.getId());
        deliveredIds.put(keptMsg.getId(), keptMsg.getId());
        setModified();

        // Setting the message's deliveryCount and denied fields.
        deliveryAttempts = (Integer) deniedMsgs.get(keptMsg.getId());
        if (deliveryAttempts == null)
          keptMsg.setDeliveryCount(1);
        else {
          keptMsg.setDeliveryCount(deliveryAttempts.intValue() +1);
          keptMsg.setRedelivered();
        }
        deliverables.add(keptMsg.getFullMessage().clone());

        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, this + ": message " + keptMsg.getId() + " added for delivery.");
      } else {
        i++;
      }
    }
   
    // Sending the dead messages to the DMQ, if any:
    if (dmqManager != null)
      dmqManager.sendToDMQ();

    // Finally, returning the reply or null:
    if (! deliverables.isEmpty()) {
      nbMsgsDeliveredSinceCreation += deliverables.size();
      ConsumerMessages consM = new ConsumerMessages(requestId,
                                                    deliverables,
                                                    name,
                                                    false);
      // set The activity: false if the subscription is 
      // passivate by the clientSubscription.
      if (!isActive)
        consM.setActive(false);
      
      if (! toListener) requestId = -1;

      return consM;
    }
    return null;
  }

  /**
   * Acknowledges messages.
   */
  void acknowledge(Iterator acks) {
    while (acks.hasNext()) {
      String id = (String) acks.next();
      acknowledge(id);
    }
  }

  void acknowledge(String id) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + ": acknowledges message: " + id);
    
    deliveredIds.remove(id);
    deniedMsgs.remove(id);
    setModified();
    Message msg = (Message) messagesTable.get(id);
    
    // Message may be null if it is not valid anymore
    if (msg != null) {
      decrAckCounters(id, msg);
    }
  }

  /**
   * Denies messages.
   * 
   * @param denies all ids of the messages to deny
   * @param redelivered true if redelivered.
   */
  void deny(Iterator<String> denies, boolean redelivered) {
    deny(denies, true, redelivered);
  }

  /**
   * Denies the messages.
   * 
   * @param denies all ids of the messages to deny
   * @param remove true to remove messages from deliveredIds map. Must be false
   *          when denies iterates over deliveredIds map keys, to avoid a
   *          ConcurrentModificationException.
   * @param redelivered true if redelivered.
   */
  private void deny(Iterator<String> denies, boolean remove, boolean redelivered) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + ".deny(" + denies + ')');
    String id;
    Message message;
    int deliveryAttempts = 1;
    int i;
    String currentId;
    long currentO;
    DMQManager dmqManager = null;

    denyLoop: while (denies.hasNext()) {
      id = (String) denies.next();

      if (remove) {
        String deliveredMsgId = (String) deliveredIds.remove(id);
        if (deliveredMsgId == null) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, this + ": cannot deny message: " + id);
          continue denyLoop;
        }
      }
      setModified();
      
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, this + ": deny message: " + id);
      
      message = (Message) messagesTable.get(id);
      
      // Message may be null if it is not valid anymore
      if (message == null) continue denyLoop;
      
      Integer value = (Integer) deniedMsgs.get(id);
      if (value != null)
        deliveryAttempts = value.intValue() + 1;
      
      // If maximum delivery attempts is reached, the message is no more
      // deliverable to this subscriber.
      if (isUndeliverable(deliveryAttempts)) {
        deniedMsgs.remove(id);
        message.setDeliveryCount(deliveryAttempts);
        if (dmqManager == null)
          dmqManager = new DMQManager(dmqId, null);
        nbMsgsSentToDMQSinceCreation++;
        dmqManager.addDeadMessage(message.getFullMessage(), MessageErrorConstants.UNDELIVERABLE);
        decrAckCounters(id, message);
      } else {
        // Else, putting it back to the deliverables vector according to its
        // original delivery order, and adding a new entry for it in the
        // denied messages table.
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, " -> put back to the messages to deliver");
        
        i = 0;
        insertLoop:
        while (i < messageIds.size()) {
          currentId = (String) messageIds.get(i);
          Message currentMessage = (Message) messagesTable.get(currentId);
            
          // Message may be null if it is not valid anymore
          if (currentMessage != null) {
            currentO = currentMessage.order;
            if (currentO > message.order) break insertLoop;
            i++;
          } else {
            // Remove the invalid message
            messageIds.remove(i);
          }
        }
        
        // TODO: should be able to know whether the message is persistent or not
        messageIds.add(i, id, true);
        if (redelivered)
          deniedMsgs.put(id, new Integer(deliveryAttempts));
      }
    }

    // Sending dead messages to the DMQ, if needed:
    if (dmqManager != null)
      dmqManager.sendToDMQ();

  }

  /**
   * Decreases the subscription's messages acknowledgement expectations,
   * deletes those not to be consumed anymore.
   */
  void deleteMessages() {
    Iterator<String> it = deliveredIds.keySet().iterator();
    while (it.hasNext()) {
      // TODO: should be able to know whether the message is persistent or not
      messageIds.add(it.next(), true);
    }

    for (Iterator allMessageIds = messageIds.iterator(); allMessageIds.hasNext();) {
      removeMessage((String) allMessageIds.next());
    }
  }
  
  /**
   * Returns <code>true</code> if a given message is considered as  undeliverable,
   * because its delivery count matches the subscription's threshold, if any, or the
   * server's default threshold value (if any).
   */
  private boolean isUndeliverable(int deliveryAttempts) {
    if (threshold == 0) return false;
    
    if (threshold > 0)
      return (deliveryAttempts >= threshold);
    else if (Queue.getDefaultThreshold() > 0)
      return (deliveryAttempts >= Queue.getDefaultThreshold());
    return false;
  }
  
  public long getNbMsgsSentToDMQSinceCreation() {
    return nbMsgsSentToDMQSinceCreation;
  }

  public long getNbMsgsDeliveredSinceCreation() {
    return nbMsgsDeliveredSinceCreation;
  }

  Message getSubscriptionMessage(String msgId) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "ClientSubscription.getSubscriptionMessage(" + msgId + ')');
    
    if (! messageIds.contains(msgId)) {
      // The message has been delivered
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> message not found");
      
      return null;
    }
    return (Message) messagesTable.get(msgId);
  }
  
  /**
   * Returns the description of a particular pending message. The message is
   * pointed out through its unique identifier.
   * 
   * @param msgId The unique message's identifier.
   * @return the description of the message.
   * 
   * @see org.objectweb.joram.mom.messages.MessageJMXWrapper
   */
  public CompositeData getMessage(String msgId) throws Exception {
    Message msg = getSubscriptionMessage(msgId);
    if (msg == null) return null;
    
    return MessageJMXWrapper.createCompositeDataSupport(msg);
  }

  /**
   * Returns the description of all pending messages.
   * 
   * @return the description of the message.
   * 
   * @see org.objectweb.joram.mom.messages.MessageJMXWrapper
   */
  public TabularData getMessages() throws Exception {
    return MessageJMXWrapper.createTabularDataSupport(messagesTable.getMap(), messageIds);
  }

  public List getMessagesView() {
    List messages = new ArrayList();
    
    // DF: should be avoided with swap... (check the table type?)
    for (int i = 0; i < messageIds.size(); i++) {
      messages.add(messagesTable.get(messageIds.get(i)));
    }
    return messages;
  }

  public void deleteMessage(String msgId) {
    messageIds.remove(msgId);
    Message message = removeMessage(msgId);
    setModified();
    if (message != null) {
      DMQManager dmqManager = new DMQManager(dmqId, null);
      nbMsgsSentToDMQSinceCreation++;
      dmqManager.addDeadMessage(message.getFullMessage(), MessageErrorConstants.ADMIN_DELETED);
      dmqManager.sendToDMQ();
    }
  }

  public void clear() {
    DMQManager dmqManager = null;
    for (int i = 0; i < messageIds.size(); i++) {
      String msgId = (String) messageIds.get(i);
      Message message = removeMessage(msgId);
      if (message != null) {
        if (dmqManager == null)
          dmqManager = new DMQManager(dmqId, null);
        nbMsgsSentToDMQSinceCreation++;
        dmqManager.addDeadMessage(message.getFullMessage(), MessageErrorConstants.ADMIN_DELETED);
      }
    }
    if (dmqManager != null)
      dmqManager.sendToDMQ();
    messageIds.clear();
    setModified();
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
      decrAckCounters(msgId, message);
    }
    return message;
  }

  private void decrAckCounters(String msgId, Message message) {
    message.acksCounter--;
    if (message.acksCounter == 0)
      messagesTable.remove(msgId);
    if (durable) {
      message.durableAcksCounter--;
      if (message.durableAcksCounter == 0)
        message.delete();
    }
  }

  public int getEncodableClassId() {
    return JoramHelper.CLIENT_SUBSCRIPTION_CLASS_ID;
  }

  public int getEncodedSize() throws Exception {
    int encodedSize = INT_ENCODED_SIZE;
    Iterator<Entry<String, String>> deliveredIdIterator = deliveredIds.entrySet().iterator();
    while (deliveredIdIterator.hasNext()) {
      Entry<String, String> deliveredId = deliveredIdIterator.next();
      encodedSize += EncodableHelper.getStringEncodedSize(deliveredId.getKey());
    }
    encodedSize += INT_ENCODED_SIZE;
    Iterator<Entry<String, Integer>> deniedMsgIterator = deniedMsgs.entrySet().iterator();
    while (deniedMsgIterator.hasNext()) {
      Entry<String, Integer> deniedMsg = deniedMsgIterator.next();
      encodedSize += EncodableHelper.getStringEncodedSize(deniedMsg.getKey());
      encodedSize += INT_ENCODED_SIZE;
    }
    encodedSize += BOOLEAN_ENCODED_SIZE;
    if (dmqId != null) {
      encodedSize += dmqId.getEncodedSize();
    }
    encodedSize += BOOLEAN_ENCODED_SIZE;
    
    encodedSize += EncodableHelper.getStringEncodedSize(name);
    encodedSize += INT_ENCODED_SIZE + LONG_ENCODED_SIZE * 2;
    
    encodedSize += BOOLEAN_ENCODED_SIZE;
    if (selector != null) {
      encodedSize += EncodableHelper.getStringEncodedSize(selector);
    }
    encodedSize += INT_ENCODED_SIZE;
    encodedSize += topicId.getEncodedSize();
    encodedSize += EncodableHelper.getNullableStringEncodedSize(clientID);
    return encodedSize;
  }

  public void encode(Encoder encoder) throws Exception {
    encoder.encodeUnsignedInt(deliveredIds.size());
    Iterator<Entry<String, String>> deliveredIdIterator = deliveredIds.entrySet().iterator();
    while (deliveredIdIterator.hasNext()) {
      Entry<String, String> deliveredId = deliveredIdIterator.next();
      encoder.encodeString(deliveredId.getKey());
      // no need to encode the value
    }
    encoder.encodeUnsignedInt(deniedMsgs.size());
    Iterator<Entry<String, Integer>> deniedMsgIterator = deniedMsgs.entrySet().iterator();
    while (deniedMsgIterator.hasNext()) {
      Entry<String, Integer> deniedMsg = deniedMsgIterator.next();
      encoder.encodeString(deniedMsg.getKey());
      encoder.encodeUnsignedInt(deniedMsg.getValue());
    }
    if (dmqId == null) {
      encoder.encodeBoolean(true);
    } else {
      encoder.encodeBoolean(false);
      dmqId.encode(encoder);
    }
    encoder.encodeBoolean(durable);
    
    encoder.encodeString(name);

    encoder.encodeUnsignedInt(nbMaxMsg);
    encoder.encodeUnsignedLong(nbMsgsDeliveredSinceCreation);
    encoder.encodeUnsignedLong(nbMsgsSentToDMQSinceCreation);
    
    if (selector == null) {
      encoder.encodeBoolean(true);
    } else {
      encoder.encodeBoolean(false);
      encoder.encodeString(selector);
    }
    encoder.encodeUnsignedInt(threshold);
    topicId.encode(encoder);
    encoder.encodeNullableString(clientID);
  }
  
  public void decode(Decoder decoder) throws Exception {
    int deliveredIdsSize = decoder.decodeUnsignedInt();
    deliveredIds = new Hashtable<String, String>(deliveredIdsSize);
    for (int i = 0; i < deliveredIdsSize; i++) {
      String key = decoder.decodeString();
      deliveredIds.put(key, key);
    }
    int deniedMsgSize = decoder.decodeUnsignedInt();
    deniedMsgs = new Hashtable<String, Integer>(deniedMsgSize);
    for (int i = 0; i < deniedMsgSize; i++) {
      String key = decoder.decodeString();
      Integer value = decoder.decodeUnsignedInt();
      deniedMsgs.put(key, value);
    }
    boolean isNull = decoder.decodeBoolean();
    if (isNull) {
      dmqId = null;
    } else {
      dmqId = new AgentId((short) 0, (short) 0, 0);
      dmqId.decode(decoder);
    }
    durable = decoder.decodeBoolean();
    
    name = decoder.decodeString();
    
    nbMaxMsg = decoder.decodeUnsignedInt();
    nbMsgsDeliveredSinceCreation = decoder.decodeUnsignedLong();
    nbMsgsSentToDMQSinceCreation = decoder.decodeUnsignedLong();
    
    isNull = decoder.decodeBoolean();
    if (isNull) {
      selector = null;
    } else {
      selector = decoder.decodeString();
    }
    threshold = decoder.decodeUnsignedInt();
    topicId = new AgentId((short) 0, (short) 0, 0);
    topicId.decode(decoder);
    clientID = decoder.decodeNullableString();
  }
  
  public static String getTransactionPrefix(AgentId proxyId) {
    StringBuffer subscriptionContextPrefix = new StringBuffer(19).append("CS").append(proxyId.toString()).append('_');
    return subscriptionContextPrefix.toString();
  }
  
  private String getTxName() {
    if (txName == null) {
      txName = getTransactionPrefix(proxyId) + name;
    }
    return txName;
  }
  
  public void save() {
    if (! durable) return;
    
    try {
      AgentServer.getTransaction().save(this, getTxName());
      
      // The method 'save' is called only once per agent reaction.
      // Calling 'save' several times would not be efficient as
      // it would encode the message id list several times.
      
      messageIds.save();
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, "ClientSubscription named [" + txName
          + "] could not be saved", exc);
    }
  }
  
  public void delete() {
    if (! durable) return;
    
    AgentServer.getTransaction().delete(getTxName());
    messageIds.delete();
  }
  
  public void initMessageIds() throws Exception {
    MessageIdListFactory messageIdListFactory = MessageIdListFactory.newFactory();
    messageIds = messageIdListFactory.createMessageIdList(MESSAGE_ID_LIST_PREFIX + getTxName());
  }
  
  public void loadMessageIds() throws Exception {
    MessageIdListFactory messageIdListFactory = MessageIdListFactory.newFactory();
    messageIds = messageIdListFactory.loadMessageIdList(MESSAGE_ID_LIST_PREFIX + getTxName());
  }
  
  private void setModified() {
    if (! modified) {
      modified = true;
      proxy.modifiedSubscription(this);
    }
  }
  
  Map<String, String> getDeliveredIds() {
    return deliveredIds;
  }

  Map<String, Integer> getDeniedMsgs() {
    return deniedMsgs;
  }

  String getClientID() {
    return clientID;
  }

  public static class ClientSubscriptionFactory implements EncodableFactory {

    public Encodable createEncodable() {
      return new ClientSubscription();
    }

  }

}
