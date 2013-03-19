/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2012 ScalAgent Distributed Technologies
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.objectweb.joram.mom.dest.Queue;
import org.objectweb.joram.mom.messages.Message;
import org.objectweb.joram.mom.messages.MessageJMXWrapper;
import org.objectweb.joram.mom.util.DMQManager;
import org.objectweb.joram.mom.util.JoramHelper;
import org.objectweb.joram.shared.MessageErrorConstants;
import org.objectweb.joram.shared.client.ConsumerMessages;
import org.objectweb.joram.shared.selectors.Selector;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.serialize.EncodedString;
import fr.dyade.aaa.util.TransactionObject;
import fr.dyade.aaa.util.TransactionObjectFactory;

/**
 * The <code>ClientSubscription</code> class holds the data of a client
 * subscription, and the methods managing the delivery and acknowledgement
 * of the messages.
 */
class ClientSubscription implements ClientSubscriptionMBean, Serializable, TransactionObject {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  public static Logger logger = Debug.getLogger(ClientSubscription.class.getName());
  
  /** The proxy's agent identifier. */
  private AgentId proxyId;
  /** <code>true</code> if the subscription is durable. */
  private boolean durable;
  /** The topic identifier. */
  private AgentId topicId;
  /** The subscription name. */
  // JORAM_PERF_BRANCH
  private EncodedString name;
  /** The selector for filtering messages. */
  // JORAM_PERF_BRANCH
  private EncodedString selector;
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
    //save();
    // JORAM_PERF_BRANCH
    isModified = true;
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
  private List<String> messageIds;
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
  private transient Map messagesTable;

  private transient ProxyAgentItf proxy;
  
  /** the number of erroneous messages forwarded to the DMQ */
  protected long nbMsgsSentToDMQSinceCreation = 0;
  
  /** the number of delivered messages */
  protected long nbMsgsDeliveredSinceCreation = 0;

  private int DEFAULT_MAX_NUMBER_OF_MSG_PER_REQUEST = 100;
  
  public transient boolean isModified;
  
  public ClientSubscription() {}
  
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
                     EncodedString name,
                     EncodedString selector,
                     boolean noLocal,
                     AgentId dmqId,
                     int threshold,
                     int nbMaxMsg,
                     Map messagesTable) {
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

    messageIds = new Vector();
    deliveredIds = new Hashtable();
    deniedMsgs = new Hashtable();

    noFiltering = (! noLocal) && (selector == null || selector.equals(""));

    active = DEFAULT_MAX_NUMBER_OF_MSG_PER_REQUEST;
    requestId = -1;
    toListener = false;
    
    // JORAM_PERF_BRANCH
    isModified = true;

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
    return name.getString();
  }
  
  //JORAM_PERF_BRANCH
  public EncodedString getEncodedName() {
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
    if (selector == null) return null;
    else return selector.getString();
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
    
    // JORAM_PERF_BRANCH
    proxyId = px.getId();
  }
  
  /**
   * Re-initializes the client subscription.
   * 
   * @param messagesTable  Proxy's table where storing the messages.
   * @param persistedMessages  Proxy's persisted messages.
   * @param denyDeliveredMessages Denies already delivered messages.
   */
  void reinitialize(Map messagesTable, List persistedMessages, boolean denyDeliveredMessages) {
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
          messagesTable.put(msgId, message);
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
                  EncodedString selector,
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
    //save();
    // JORAM_PERF_BRANCH
    isModified = true;

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
      //save();
      // JORAM_PERF_BRANCH
      isModified = true;
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
    //save();
    // JORAM_PERF_BRANCH
    isModified = true;
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

      // Keeping the message if filtering is successful.
      if (noFiltering ||
          (Selector.matches(message.getHeaderMessage(), selector.getString()) &&
           (! noLocal || ! msgId.startsWith(proxyId.toString().substring(1) + "c" + contextId + "m", 3)))) {

        // It's the first delivery, adds the message to the proxy's table
        if (message.acksCounter == 0)
          messagesTable.put(msgId, message);
        
        message.acksCounter++;
        if (durable)
          message.durableAcksCounter++;

        messageIds.add(msgId);
        
        // JORAM_PERF_BRANCH
        if (message.isPersistent()) {
          //save();
          // JORAM_PERF_BRANCH
          isModified = true;
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

        message = (Message) messagesTable.get(id);
        
        // JORAM_PERF_BRANCH
        if (message.isPersistent()) {
          //save();
          // JORAM_PERF_BRANCH
          isModified = true;
        }

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
            
            // JORAM_PERF_BRANCH
            if (message.isPersistent()) {
              //save();
              // JORAM_PERF_BRANCH
              isModified = true;
            }
            
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
          
          // JORAM_PERF_BRANCH
          if (message.isPersistent()) {
            //save();
            // JORAM_PERF_BRANCH
            isModified = true;
          }
        }
      }

      // Putting the kept message in the vector.
      if (keptMsg != null) {
        messageIds.remove(keptMsg.getId());
        deliveredIds.put(keptMsg.getId(), keptMsg.getId());
        
        // JORAM_PERF_BRANCH
        if (keptMsg.isPersistent()) {
          //save();
          // JORAM_PERF_BRANCH
          isModified = true;
        }

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
                                                    name.getString(),
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
    
    Message msg = (Message) messagesTable.get(id);
    
    // JORAM_PERF_BRANCH
    if (msg.isPersistent()) {
      //save();
      // JORAM_PERF_BRANCH
      isModified = true;
    }

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
      
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, this + ": deny message: " + id);
      
      message = (Message) messagesTable.get(id);
      
      // Message may be null if it is not valid anymore
      if (message == null) continue denyLoop;
      
      // JORAM_PERF_BRANCH
      if (message.isPersistent()) {
        //save();
        // JORAM_PERF_BRANCH
        isModified = true;
      }
      
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
        
        messageIds.add(i, id);
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
  //JORAM_PERF_BRANCH
  void deleteMessages() {
    messageIds.addAll(deliveredIds.keySet());

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
    
    int index = messageIds.indexOf(msgId);
    if (index < 0) {
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
    return MessageJMXWrapper.createTabularDataSupport(messagesTable, messageIds);
  }

  public List getMessagesView() {
    List messages = new ArrayList();
    for (int i = 0; i < messageIds.size(); i++) {
      messages.add(messagesTable.get(messageIds.get(i)));
    }
    return messages;
  }

  public void deleteMessage(String msgId) {
    messageIds.remove(msgId);
    Message message = removeMessage(msgId);
    
    // JORAM_PERF_BRANCH
    if (message.isPersistent()) {
      //save();
      // JORAM_PERF_BRANCH
      isModified = true;
    }
    
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
    //save();
    // JORAM_PERF_BRANCH
    isModified = true;
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
  
  /* JORAM_PERF_BRANCH
  private void save() {
    if (durable) proxy.setSave();
  }
  */

  void cleanMessageIds() {
    messageIds.retainAll(messagesTable.keySet());
  }

  // JORAM_PERF_BRANCH
  public int getClassId() {
    return JoramHelper.CLIENTSUBSCRIPTION_CLASS_ID;
  }

  //JORAM_PERF_BRANCH
  public void encodeTransactionObject(DataOutputStream os) throws IOException {
    os.writeInt(deliveredIds.size());
    Iterator<Entry<String, String>> deliveredIdIterator = deliveredIds.entrySet().iterator();
    while (deliveredIdIterator.hasNext()) {
      Entry<String, String> deliveredId = deliveredIdIterator.next();
      os.writeUTF(deliveredId.getKey());
      // no need to encode the value
    }
    os.writeInt(deniedMsgs.size());
    Iterator<Entry<String, Integer>> deniedMsgIterator = deniedMsgs.entrySet().iterator();
    while (deniedMsgIterator.hasNext()) {
      Entry<String, Integer> deniedMsg = deniedMsgIterator.next();
      os.writeUTF(deniedMsg.getKey());
      os.writeInt(deniedMsg.getValue());
    }
    if (dmqId == null) {
      os.writeBoolean(true);
    } else {
      os.writeBoolean(false);
      dmqId.encodeTransactionObject(os);
    }
    os.writeBoolean(durable);
    os.writeInt(messageIds.size());
    for (String messageId : messageIds) {
      os.writeUTF(messageId);
    }
    //os.writeUTF(name);
    name.writeTo(os);
    os.writeInt(nbMaxMsg);
    os.writeLong(nbMsgsDeliveredSinceCreation);
    os.writeLong(nbMsgsSentToDMQSinceCreation);
    
    //proxyId.encodeTransactionObject(os);
    
    if (selector == null) {
      os.writeBoolean(true);
    } else {
      os.writeBoolean(false);
      //os.writeUTF(selector);
      selector.writeTo(os);
    }
    os.writeInt(threshold);
    topicId.encodeTransactionObject(os);
  }

  //JORAM_PERF_BRANCH
  public void decodeTransactionObject(DataInputStream is) throws IOException {
    int deliveredIdsSize = is.readInt();
    deliveredIds = new Hashtable<String, String>(deliveredIdsSize);
    for (int i = 0; i < deliveredIdsSize; i++) {
      String key = is.readUTF();
      deliveredIds.put(key, key);
    }
    int deniedMsgSize = is.readInt();
    deniedMsgs = new Hashtable<String, Integer>(deniedMsgSize);
    for (int i = 0; i < deniedMsgSize; i++) {
      String key = is.readUTF();
      Integer value = is.readInt();
      deniedMsgs.put(key, value);
    }
    boolean isNull = is.readBoolean();
    if (isNull) {
      dmqId = null;
    } else {
      dmqId = new AgentId((short) 0, (short) 0, 0);
      dmqId.decodeTransactionObject(is);
    }
    durable = is.readBoolean();
    int messageIdsSize = is.readInt();
    messageIds = new Vector<String>(messageIdsSize);
    for (int i = 0; i < messageIdsSize; i++) {
      String messageId = is.readUTF();
      messageIds.add(messageId);
    }
    //name = is.readUTF();
    name = new EncodedString();
    name.readFrom(is);
    
    nbMaxMsg = is.readInt();
    nbMsgsDeliveredSinceCreation = is.readLong();
    nbMsgsSentToDMQSinceCreation = is.readLong();
    
    //proxyId = new AgentId((short) 0, (short) 0, 0);
    //proxyId.decodeTransactionObject(is);
    
    isNull = is.readBoolean();
    if (isNull) {
      selector = null;
    } else {
      //selector = is.readUTF();
      selector = new EncodedString();
      selector.readFrom(is);
    }
    threshold = is.readInt();
    topicId = new AgentId((short) 0, (short) 0, 0);
    topicId.decodeTransactionObject(is);
  }
  
  //JORAM_PERF_BRANCH
  public static class ClientSubscriptionFactory implements TransactionObjectFactory {

    public TransactionObject newInstance() {
      return new ClientSubscription();
    }
    
  }
  
  //JORAM_PERF_BRANCH
  public transient String txname;
  
  public void save() {
    if (! durable) return;
    
    try {
      AgentServer.getTransaction().create(this, getTxName());
    } catch (IOException exc) {
      logger.log(BasicLevel.ERROR, "ClientSubscription named [" + txname
          + "] could not be saved", exc);
    }
  }
  
  //JORAM_PERF_BRANCH
  public void delete() {
    if (! durable) return;
    
    AgentServer.getTransaction().delete(getTxName());
  }
  
  //JORAM_PERF_BRANCH
  private String getTxName() {
    if (txname == null) {
      txname = getTransactionPrefix(proxyId) + name.getString();
    }
    return txname;
  }
  
  //JORAM_PERF_BRANCH
  public static String getTransactionPrefix(AgentId proxyId) {
    StringBuffer subscriptionContextPrefix = new StringBuffer(19).append("CS").append(proxyId.toString()).append('_');
    return subscriptionContextPrefix.toString();
  }

}
