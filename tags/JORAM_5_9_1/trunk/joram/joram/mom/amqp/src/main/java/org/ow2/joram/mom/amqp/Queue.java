/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 - 2011 ScalAgent Distributed Technologies
 * Copyright (C) 2008 - 2009 CNES
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
package org.ow2.joram.mom.amqp;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.AlreadyBoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.ow2.joram.mom.amqp.exceptions.AccessRefusedException;
import org.ow2.joram.mom.amqp.exceptions.NoConsumersException;
import org.ow2.joram.mom.amqp.exceptions.ResourceLockedException;
import org.ow2.joram.mom.amqp.exceptions.TransactionException;
import org.ow2.joram.mom.amqp.marshalling.AMQP;
import org.ow2.joram.mom.amqp.structures.Deliver;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.util.Transaction;

/**
 * An AMQP queue.
 */
public class Queue implements QueueMBean, Externalizable {

  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  public final static Logger logger = Debug.getLogger(Queue.class.getName());
  
  public static final long FIRST_DELIVERY = -1;

  private String name;

  private boolean durable;
  
  private boolean autodelete;
  
  private boolean exclusive;

  // Exchanges bound to this queue. May contain duplicates.
  private List<String> boundExchanges = new ArrayList<String>();

  // These 2 fields are used to identify the consumer for exclusive queues
  private short serverId;
  private long proxyId;

  private long msgCounter;

  private SortedSet<Message> toDeliver = new TreeSet<Message>();
  
  private SortedSet<Message> toAck = new TreeSet<Message>();
  
  // Use LinkedHashMap because ordering is necessary for round robin consumers
  private Map<SubscriptionKey, Subscription> consumers = new LinkedHashMap<SubscriptionKey, Subscription>(16,
      0.75f, true);
  
  public static final String PREFIX_QUEUE = "Queue_";
  private static final String PREFIX_MSG = "M.";
  private static final String PREFIX_BOUND_EXCHANGE = "BE_";
  
  private String prefixMsg = null;
  private String prefixBE = null;
  
  public Queue() { }
  
  public Queue(String name, boolean durable, boolean autodelete, boolean exclusive, short serverId, long proxyId) throws TransactionException {
    this.name = name;
    this.durable = durable;
    this.autodelete = autodelete;
    this.exclusive = exclusive;
    this.serverId = serverId;
    this.proxyId = proxyId;
    String localName = Naming.getLocalName(name);
    prefixMsg = PREFIX_MSG + localName;
    prefixBE = PREFIX_BOUND_EXCHANGE + localName;
    if (durable) {
      saveQueue(this);
    }
  }
  
  public synchronized Message receive(boolean noAck, short serverId, long proxyId) throws ResourceLockedException, TransactionException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Queue.receive()");

    if (exclusive && (this.serverId != serverId || this.proxyId != proxyId)) {
      throw new ResourceLockedException("Can't get message on the non-owned exclusive queue '" + name + "'.");
    }
    Message msg = null;
    if (toDeliver.size() > 0) {
      msg = toDeliver.first();
      toDeliver.remove(msg);
      if (noAck) {
        if (durable) {
          deleteMessage(msg.queueMsgId);
        }
      } else {
        toAck.add(msg);
      }
      msg.queueSize = toDeliver.size();
    }
    return msg;
  }
  
  public synchronized void consume(DeliveryListener proxy, int channelId, String consumerTag,
      boolean exclusiveConsumer, boolean noAck, boolean noLocal, short serverId, long proxyId)
      throws AccessRefusedException, ResourceLockedException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Queue.consume()");

    if (exclusive && (this.serverId != serverId || this.proxyId != proxyId)) {
      throw new ResourceLockedException("Can't consume on the non-owned exclusive queue '" + name + "'.");
    }
    if (exclusiveConsumer && consumers.size() != 0) {
      throw new AccessRefusedException("Exclusive consume request failed due to previous consumer on queue '"
          + name + "'.");
    }
    if (consumers.size() == 1 && consumers.values().iterator().next().exclusive) {
      throw new AccessRefusedException("Consume request failed due to previous exclusive consumer on queue '"
          + name + "'.");
    }

    consumers.put(new SubscriptionKey(serverId, proxyId, channelId, consumerTag), new Subscription(proxy,
        channelId, consumerTag, exclusiveConsumer, noAck, noLocal, serverId, proxyId));

  }

  public synchronized List<Deliver> getDeliveries(String consumerTag, int channelId, int maxMessage,
      short serverId, long proxyId) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Queue.getDeliveries(" + consumerTag + ',' + maxMessage + ')');

    SubscriptionKey subKey = new SubscriptionKey(serverId, proxyId, channelId, consumerTag);
    Subscription sub = consumers.get(subKey);

    if (sub == null || maxMessage == 0) {
      return null;
    }
    List<Deliver> deliveries = new ArrayList<Deliver>();
    if (toDeliver.size() > 0) {
      Iterator<Message> messagesIter = toDeliver.iterator();
      while (messagesIter.hasNext() && (maxMessage < 0 || deliveries.size() < maxMessage)) {
        Message msg = messagesIter.next();
        if (!sub.noAck) {
          toAck.add(msg);
        } else {
          if (durable) {
            deleteMessage(msg.queueMsgId);
          }
        }
        messagesIter.remove();

        AMQP.Basic.Deliver deliver = new AMQP.Basic.Deliver(consumerTag, msg.queueMsgId, msg.redelivered,
            msg.exchange, msg.routingKey);
        deliver.channelNumber = channelId;
        Deliver msgDelivery = new Deliver(deliver, msg.properties, msg.body, msg.queueMsgId, serverId,
            proxyId, name, sub.noAck);

        deliveries.add(msgDelivery);
      }
    }
    return deliveries;
  }

  public synchronized void publish(Message msg, boolean immediate, short serverId, long proxyId)
      throws NoConsumersException, TransactionException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Queue.publish(" + msg.properties + ')');

    if (exclusive && serverId != -1 && this.serverId != serverId && this.proxyId != proxyId) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, "Publishing to a non-owned exclusive queue '" + name + "'.");
      // TODO ?
      return;
    }
    boolean recover = false;
    if (msg.queueMsgId == FIRST_DELIVERY) {
      msg.queueMsgId = msgCounter++;
    } else {
      recover = true;
    }

    if (consumers.size() == 0 && immediate) {
      throw new NoConsumersException("No consumer available for immediate publication on queue '" + name + "'.");
    }
    
    if (durable) {
      if (recover) {
        toAck.remove(msg);
      } else {
        saveMessage(msg);
      }
    }

    toDeliver.add(msg);

    // If a consumer is present, try to deliver right now
    if (consumers.size() > 0) {
      Iterator<Entry<SubscriptionKey, Subscription>> iterEntries = consumers.entrySet().iterator();

      // Deliver to the first available (ie QoS buffer not full) consumer
      // this will achieve round-robin if no QoS is set.
      while (iterEntries.hasNext()) {
        Entry<SubscriptionKey, Subscription> entry = iterEntries.next();
        Subscription subscription = entry.getValue();
        if (subscription.deliveryListener.deliver(subscription.consumerTag, subscription.channelId, this,
            subscription.serverId, subscription.proxyId)) {
          consumers.get(entry.getKey());
          return;
        }
      }
    }
  }

  public synchronized void cancel(String consumerTag, int channelNumber, short serverId, long proxyId)
      throws ResourceLockedException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Queue.cancel()");

    if (exclusive && (this.serverId != serverId || this.proxyId != proxyId)) {
      throw new ResourceLockedException("Can't cancel a consumer on the non-owned exclusive queue '" + name + "'.");
    }
    
    SubscriptionKey subKey = new SubscriptionKey(serverId, proxyId, channelNumber, consumerTag);
    consumers.remove(subKey);
  }
  
  public synchronized void cleanConsumers(short sid) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Queue.cleanConsumers(" + sid +')');
    Set<SubscriptionKey> subscriptionKeys = consumers.keySet();
    Iterator<SubscriptionKey> it = subscriptionKeys.iterator();
    while (it.hasNext()) {
      SubscriptionKey subKey = it.next();
      if (subKey.serverId == sid) {
        consumers.remove(subKey);
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "Queue.cleanConsumers remove subKey = " + subKey);
      }
    }
  }
  
  public synchronized int clear(short serverId, long proxyId) throws ResourceLockedException, TransactionException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Queue.purge() " + toDeliver.size());

    if (exclusive && (this.serverId != serverId || this.proxyId != proxyId)) {
      throw new ResourceLockedException("Can't clear the non-owned exclusive queue '" + name + "'.");
    }
    int msgCount = toDeliver.size();
    if (durable && msgCount > 0)
      deleteAllMessage(toDeliver);
    toDeliver.clear();
    return msgCount;
  }

  public synchronized void ackMessages(List<Long> idsToAck) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Queue.ackMessages(" + idsToAck + ')');

    // Both lists must be sorted
    Iterator<Long> iterIds = idsToAck.iterator();
    Iterator<Message> iterMsgs = toAck.iterator();
    while (iterIds.hasNext()) {
      long id = iterIds.next().longValue();
      while (iterMsgs.hasNext()) {
        Message msg = iterMsgs.next();
        if (msg.queueMsgId == id) {
          iterMsgs.remove();
          if (durable)
            deleteMessage(msg.queueMsgId);
          break;
        }
      }
    }
  }

  public synchronized void recoverMessages(List<Long> idsToRecover) throws TransactionException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Queue.recoverMessages(" + idsToRecover + ')');

    // Both lists must be sorted
    Iterator<Long> iterIds = idsToRecover.iterator();
    Iterator<Message> iterMsgs = toAck.iterator();
    List<Message> recoveredMsgs = new ArrayList<Message>();
    while (iterIds.hasNext()) {
      long id = iterIds.next().longValue();
      while (iterMsgs.hasNext()) {
        Message msg = iterMsgs.next();
        if (msg.queueMsgId == id) {
          iterMsgs.remove();
          recoveredMsgs.add(msg);
          break;
        }
      }
    }

    iterMsgs = recoveredMsgs.iterator();
    while (iterMsgs.hasNext()) {
      Message msg = iterMsgs.next();
      try {
        msg.redelivered = true;
        publish(msg, false, (short) -1, -1);
      } catch (NoConsumersException exc) {
        // Can't happen with immediate=false
      }
    }
  }

  public synchronized AMQP.Queue.DeclareOk getInfo(short serverId, long proxyId) throws ResourceLockedException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Queue.getInfo()");

    if (exclusive && (this.serverId != serverId || this.proxyId != proxyId)) {
      throw new ResourceLockedException("Can't redeclare the non-owned exclusive queue '" + name + "'.");
    }
    AMQP.Queue.DeclareOk queueInfo = new AMQP.Queue.DeclareOk(name, toDeliver.size(), consumers.size());
    return queueInfo;
  }

  private static class Subscription {

    long proxyId;
    short serverId;
    String consumerTag;
    boolean exclusive;
    boolean noAck;
    boolean noLocal;
    int channelId;
    DeliveryListener deliveryListener;

    public Subscription(DeliveryListener deliveryListener, int channelId, String consumerTag, boolean exclusive,
        boolean noAck, boolean noLocal, short serverId, long proxyId) {
      this.deliveryListener = deliveryListener;
      this.consumerTag = consumerTag;
      this.exclusive = exclusive;
      this.noAck = noAck;
      this.noLocal = noLocal;
      this.channelId = channelId;
      this.serverId = serverId;
      this.proxyId = proxyId;
    }
  }

  public String getName() {
    return name;
  }

  public int getConsumerCount() {
    return consumers.size();
  }

  public boolean isAutodelete() {
    return autodelete;
  }

  public int getToDeliverMessageCount() {
    return toDeliver.size();
  }

  public int getToAckMessageCount() {
    return toAck.size();
  }

  public long getHandledMessageCount() {
    return msgCounter;
  }

  public List<String> getBoundExchanges() {
    return boundExchanges;
  }

  public boolean isDurable() {
    return durable;
  }

  public boolean isExclusive() {
    return exclusive;
  }

  public synchronized void addBoundExchange(String exchange, short serverId, long proxyId)
      throws TransactionException, ResourceLockedException {
    if (exclusive && (this.serverId != serverId || this.proxyId != proxyId)) {
      throw new ResourceLockedException("Can't bind the non-owned exclusive queue '" + name + "'.");
    }
    boundExchanges.add(exchange);
    if (durable) {
      saveBoundExchange(exchange);
    }
  }

  public synchronized void removeBoundExchange(String exchangeName) {
    try {
      removeBoundExchange(exchangeName, serverId, proxyId);
    } catch (ResourceLockedException exc) {
      // Can't happen
    }
  }

  public synchronized void removeBoundExchange(String exchangeName, short serverId, long proxyId)
      throws ResourceLockedException {
    if (exclusive && (this.serverId != serverId || this.proxyId != proxyId)) {
      throw new ResourceLockedException("Can't unbind the non-owned exclusive queue '" + name + "'.");
    }
    boundExchanges.remove(exchangeName);
    if (durable) {
      deleteBoundExchange(exchangeName);
    }
  }

  public synchronized void deleteQueue(String queueName, short serverId, long proxyId)
      throws ResourceLockedException,
      TransactionException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Queue.deleteQueue(" + queueName + ')');

    if (exclusive && (this.serverId != serverId || this.proxyId != proxyId)) {
      throw new ResourceLockedException("Can't delete the non-owned exclusive queue '" + name + "'.");
    }

    if (durable) {
      AgentServer.getTransaction().delete(PREFIX_QUEUE + Naming.getLocalName(queueName));
      deleteAllMessage(toDeliver);
      deleteAllMessage(toAck);
      Iterator<String> iterBoundExchanges = boundExchanges.iterator();
      while (iterBoundExchanges.hasNext()) {
        String exchangeName = iterBoundExchanges.next();
        deleteBoundExchange(exchangeName);
      }
    }
  }

  private static class SubscriptionKey {

    short serverId;

    long proxyId;

    int channelNumber;

    String consumerTag;

    public SubscriptionKey(short serverId, long proxyId, int channelNumber, String consumerTag) {
      super();
      this.serverId = serverId;
      this.proxyId = proxyId;
      this.channelNumber = channelNumber;
      this.consumerTag = consumerTag;
    }

    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + channelNumber;
      result = prime * result + ((consumerTag == null) ? 0 : consumerTag.hashCode());
      result = prime * result + (int) (proxyId ^ (proxyId >>> 32));
      result = prime * result + serverId;
      return result;
    }

    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (!(obj instanceof SubscriptionKey))
        return false;
      SubscriptionKey other = (SubscriptionKey) obj;
      if (channelNumber != other.channelNumber)
        return false;
      if (consumerTag == null) {
        if (other.consumerTag != null)
          return false;
      } else if (!consumerTag.equals(other.consumerTag))
        return false;
      if (proxyId != other.proxyId)
        return false;
      if (serverId != other.serverId)
        return false;
      return true;
    }

    public String toString() {
      return "SubscriptionKey [channelNumber=" + channelNumber + ", consumerTag=" + consumerTag
          + ", proxyId=" + proxyId + ", serverId=" + serverId + "]";
    }

  }

  //**********************************************************
  //* Persistence
  //**********************************************************
  public static Queue loadQueue(String name) throws IOException, ClassNotFoundException, TransactionException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Queue.loadQueue(" + name + ')');

    Transaction transaction = AgentServer.getTransaction();
    // load Queue
    Queue queue = (Queue) transaction.load(name);
    try {
      Naming.bindQueue(queue.name, queue);
    } catch (AlreadyBoundException exc) {
      // TODO
      exc.printStackTrace();
    }
    
    final String localName = Naming.getLocalName(queue.name);
    // load Messages
    String[] list = transaction.getList(PREFIX_MSG + localName);
    if (list != null) {
      for (int i = 0; i < list.length; i++) {
        Message msg = (Message) transaction.load(list[i]);
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "Queue.loadQueue: msg.queueMsgId = " + msg.queueMsgId + ", name = " + list[i]);
        queue.toDeliver.add(msg);
        queue.msgCounter = msg.queueMsgId + 1;
      }
    }
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Queue.loadQueue: msgCounter = " + queue.msgCounter);

    // load BoundExchanges
    list = transaction.getList(PREFIX_BOUND_EXCHANGE + localName);
    if (list != null) {
      for (int i = 0; i < list.length; i++) {
        String exchangeName = (String) transaction.load(list[i]);
        queue.boundExchanges.add(exchangeName);
      }
    }
    return queue;
  }
  
  private void saveQueue(Queue queue) throws TransactionException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Queue.saveQueue(" + queue + ')');
    try {
      AgentServer.getTransaction().create(queue, PREFIX_QUEUE + Naming.getLocalName(queue.name));
    } catch (IOException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "Queue.saveQueue ERROR::", e);
      throw new TransactionException(e.getMessage());  
    }
  }
  
  private void saveBoundExchange(String exchange) throws TransactionException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Queue.saveBoundExchange(" + exchange + ')');
    try {
      // replace / to . for transaction need.
      AgentServer.getTransaction().create(exchange, prefixBE + exchange.replace('/', '.'));
    } catch (IOException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "Queue.saveBoundExchange ERROR::", e);
      throw new TransactionException(e.getMessage());  
    }
  }

  private void deleteBoundExchange(String exchangeName) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Queue.deleteBoundExchange(" + exchangeName + ')');
    // replace / to . for transaction need.
    AgentServer.getTransaction().delete(prefixBE + exchangeName.replace('/', '.'));
  }

  private void deleteAllMessage(Set<Message> messages) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Queue.deleteAllMessage(" + messages + ')');
    if (!durable)
      return;
    Iterator<Message> msgs = messages.iterator();
    while (msgs.hasNext()) {
      Message message = msgs.next();
      deleteMessage(message.queueMsgId);
    }
  }

  private void saveMessage(Message msg) throws TransactionException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Queue.saveMessage(" + msg.queueMsgId + ')');
    try {
      AgentServer.getTransaction().create(msg, prefixMsg + msg.queueMsgId);
    } catch (IOException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "Queue.saveMessage ERROR::", e);
      throw new TransactionException(e.getMessage());  
    }
  }

  private void deleteMessage(long msgId) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Queue.deleteMessage(" + msgId + ')');
    AgentServer.getTransaction().delete(prefixMsg + msgId);
  }
  
  
  /**
   * @param out
   * @throws IOException
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    // Writes queue name
    out.writeObject(name);
    // Writes durable
    out.writeBoolean(durable);
    // Writes autodelete
    out.writeBoolean(autodelete);
    // Writes exclusive
    out.writeBoolean(exclusive);
    // Writes serverId
//    out.writeShort(serverId);
//    // Writes proxyId
//    out.writeLong(proxyId);
  }
  
  /**
   * @param in
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    toDeliver = new TreeSet<Message>();
    toAck = new TreeSet<Message>();
    consumers = new LinkedHashMap<SubscriptionKey, Subscription>();
    boundExchanges = new ArrayList<String>();
    // Reads queue name
    name = (String) in.readObject();
    // Reads durable
    durable = in.readBoolean();
    // Reads autodelete
    autodelete = in.readBoolean();
    // Reads exclusive
    exclusive = in.readBoolean();
//    // Reads serverId
//    serverId = in.readShort();
//    // Reads proxyId
//    proxyId = in.readLong();
    msgCounter = 0;
    String localName = Naming.getLocalName(name);
    prefixMsg = PREFIX_MSG + localName;
    prefixBE = PREFIX_BOUND_EXCHANGE + localName;
  }

}
