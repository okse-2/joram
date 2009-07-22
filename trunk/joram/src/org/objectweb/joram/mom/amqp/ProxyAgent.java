/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 - 2009 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.amqp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NameNotFoundException;

import org.objectweb.joram.mom.amqp.marshalling.AMQP;
import org.objectweb.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;
import org.objectweb.joram.mom.amqp.marshalling.AMQP.Basic.CancelOk;
import org.objectweb.joram.mom.amqp.marshalling.AMQP.Queue.PurgeOk;
import org.objectweb.joram.mom.amqp.marshalling.AMQP.Queue.UnbindOk;
import org.objectweb.joram.mom.amqp.proxy.request.BasicAckNot;
import org.objectweb.joram.mom.amqp.proxy.request.BasicCancelNot;
import org.objectweb.joram.mom.amqp.proxy.request.BasicConsumeNot;
import org.objectweb.joram.mom.amqp.proxy.request.BasicGetNot;
import org.objectweb.joram.mom.amqp.proxy.request.BasicPublishNot;
import org.objectweb.joram.mom.amqp.proxy.request.BasicRecoverNot;
import org.objectweb.joram.mom.amqp.proxy.request.ChannelCloseNot;
import org.objectweb.joram.mom.amqp.proxy.request.ChannelOpenNot;
import org.objectweb.joram.mom.amqp.proxy.request.ConnectionCloseNot;
import org.objectweb.joram.mom.amqp.proxy.request.ConnectionStartOkNot;
import org.objectweb.joram.mom.amqp.proxy.request.ExchangeDeclareNot;
import org.objectweb.joram.mom.amqp.proxy.request.ExchangeDeleteNot;
import org.objectweb.joram.mom.amqp.proxy.request.QueueBindNot;
import org.objectweb.joram.mom.amqp.proxy.request.QueueDeclareNot;
import org.objectweb.joram.mom.amqp.proxy.request.QueueDeleteNot;
import org.objectweb.joram.mom.amqp.proxy.request.QueuePurgeNot;
import org.objectweb.joram.mom.amqp.proxy.request.QueueUnbindNot;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.DeleteAck;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.SyncNotification;

public class ProxyAgent extends Agent {
  
  class ChannelContext {
  
    // Links between consumer tags and queue ids
    private Map consumers = new HashMap();
    
    private int consumerTag = 0;
    
    private String lastQueueCreated;
    
  }

  static class DeliverContext implements Serializable {

    private AgentId queueId;
    private int channelId;
    private long queueMsgId;
    private long deliveryTag;

    public DeliverContext(AgentId queueId, int channelId, long queueMsgId, long deliveryTag) {
      super();
      this.queueId = queueId;
      this.channelId = channelId;
      this.queueMsgId = queueMsgId;
      this.deliveryTag = deliveryTag;
    }
  }

  public final static Logger logger = 
    fr.dyade.aaa.common.Debug.getLogger(ProxyAgent.class.getName());
  
  public static final String USER_NAME = "userName";
  
  public static final String PASSWORD = "password";
  
  private String userName;
  
  private String password;
  
  private Map pendingRequests = new HashMap();
  
  // Used to find queue from deliveryTag
  private LinkedList deliveriesToAck = new LinkedList();
  
  private long tagCounter;
  
  // Maps channel id to its context
  private Map channelContexts = new HashMap();
  
  private List exclusiveQueues = new ArrayList();

  public void react(AgentId from, Notification not) throws Exception {
    // This agent is not persistent.
    setNoSave();
    if (not instanceof ConnectionStartOkNot) {
      doReact((ConnectionStartOkNot) not);
    } else if (not instanceof ChannelOpenNot) {
      doReact((ChannelOpenNot) not);
    } else if (not instanceof ExchangeDeclareNot) {
      doReact((ExchangeDeclareNot) not);
    } else if (not instanceof ExchangeDeleteNot) {
      doReact((ExchangeDeleteNot) not);
    } else if (not instanceof QueueDeclareNot) {
      doReact((QueueDeclareNot) not);
    } else if (not instanceof QueueDeleteNot) {
      doReact((QueueDeleteNot) not);
    } else if (not instanceof QueuePurgeNot) {
      doReact((QueuePurgeNot) not);
    } else if (not instanceof BasicConsumeNot) {
      doReact((BasicConsumeNot) not);
    } else if (not instanceof BasicGetNot) {
      doReact((BasicGetNot) not);
    } else if (not instanceof BasicPublishNot) {
      doReact((BasicPublishNot) not);
    } else if (not instanceof BasicCancelNot) {
      doReact((BasicCancelNot) not);
    } else if (not instanceof BasicAckNot) {
      doReact((BasicAckNot) not);
    } else if (not instanceof BasicRecoverNot) {
      doReact((BasicRecoverNot) not);
    } else if (not instanceof QueueBindNot) {
      doReact((QueueBindNot) not);
    } else if (not instanceof QueueUnbindNot) {
      doReact((QueueUnbindNot) not);
    } else if (not instanceof DeleteAck) {
      doReact((DeleteAck) not);
    } else if (not instanceof ChannelCloseNot) {
      doReact((ChannelCloseNot) not);
    } else if (not instanceof ConnectionCloseNot) {
      doReact((ConnectionCloseNot) not);
    } else {
      super.react(from, not);
    }
  }

  private void doReact(ConnectionStartOkNot not) {
    connectionStartOk(not.getClientProperties());
  }
  
  private void connectionStartOk(Map clientProperties) {
    userName = (String) clientProperties.get(USER_NAME);
    password = (String) clientProperties.get(PASSWORD);
  }
  
  private void doReact(ChannelOpenNot not) {
    AMQP.Channel.OpenOk res = channelOpen(not.getChannelId());
    not.Return(res);
  }
  
  public AMQP.Channel.OpenOk channelOpen(int channelId) {
    channelContexts.put(new Integer(channelId), new ChannelContext());
    return new AMQP.Channel.OpenOk();
  }
  
  private void doReact(ExchangeDeclareNot not) throws Exception {
    try {
      AMQP.Exchange.DeclareOk res = exchangeDeclare(
          not.getChannelId(),
          not.getExchange(),
          not.getType(),
          not.isPassive(),
          not.isDurable(),
          not.getArguments());
      not.Return(res);
    } catch (ClassNotFoundException cnfe) {
      not.Throw(cnfe);
    } catch (NameNotFoundException nnfe) {
      not.Throw(nnfe);
    } catch (IllegalArgumentException iae) {
      not.Throw(iae);
    }
  }

  public AMQP.Exchange.DeclareOk exchangeDeclare(int channelId, String exchange, String type,
      boolean passive, boolean durable, Map arguments) throws Exception {
    // Check if the exchange already exists
    Object ref = NamingAgent.getSingleton().lookup(exchange);
    if (ref == null) {
      if (passive) {
        throw new NameNotFoundException("Passive declaration of an unknown exchange.");
      }
      ExchangeAgent exchangeAgent;
      if (type.equalsIgnoreCase("direct")) {
        exchangeAgent = new DirectExchange(exchange, durable);
      } else if (type.equalsIgnoreCase("topic")) {
        exchangeAgent = new TopicExchange(exchange, durable);
      } else if (type.equalsIgnoreCase("fanout")) {
        exchangeAgent = new FanoutExchange(exchange, durable);
      } else if (type.equalsIgnoreCase("headers")) {
        exchangeAgent = new HeadersExchange(exchange, durable);
      } else {
        Class exchangeClass = Class.forName(type);
        exchangeAgent = (ExchangeAgent) exchangeClass.newInstance();
      }
      exchangeAgent.setArguments(arguments);
      NamingAgent.getSingleton().bind(exchange, exchangeAgent.getId());
      NamingAgent.getSingleton().bind(exchange + "$_type", type);
      exchangeAgent.deploy();
    } else {
      // Check if exchange type corresponds with existing exchange
      String previousType = (String) NamingAgent.getSingleton().lookup(exchange + "$_type");
      if (!previousType.equals(type)) {
        throw new IllegalArgumentException("Exchange type do not match existing exchange.");
      }
    }
    return new AMQP.Exchange.DeclareOk();
  }
  
  private void doReact(ExchangeDeleteNot not) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "ProxyAgent.exchangeDelete(" + not + ')');
    try {
      exchangeDelete(
          not.getChannelId(),
          not.getExchange(),
          not.isIfUnused(),
          not.isNowait(),
          not);
    } catch (NameNotFoundException nnfe) {
      not.Throw(nnfe);
    }
  }

  public void exchangeDelete(int channelId, String exchange, boolean ifUnused, boolean nowait,
      ExchangeDeleteNot not) throws Exception {
    
    AgentId exchangeId = (AgentId) NamingAgent.getSingleton().lookup(exchange);
    if (exchangeId == null) {
      throw new NameNotFoundException("Exchange not found for deletion.");
    }
    
    pendingRequests.put(exchangeId, not);
    sendTo(exchangeId, new DeleteNot(ifUnused));
    
  }

  private void doReact(QueueDeclareNot not) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "ProxyAgent.queueDeclare(" + not + ')');
    try {
      AMQP.Queue.DeclareOk res = queueDeclare(
          not.getChannelId(),
          not.getQueue(),
          not.isPassive(),
          not.isDurable(),
          not.isExclusive(),
          not.isAutoDelete(),
          not.getArguments());
      not.Return(res);
    } catch (NameNotFoundException nnfe) {
      not.Throw(nnfe);
    }
  }

  public AMQP.Queue.DeclareOk queueDeclare(int channelId, String queue, boolean passive,
      boolean durable, boolean exclusive, boolean autoDelete, Map arguments) throws Exception {
    // Check if the queue already exists
    Object ref = null;
    if (queue != null && !queue.equals("")) {
      ref = NamingAgent.getSingleton().lookup(queue);
    }
    String queueName = queue;
    if (ref == null) {
      if (passive) {
        throw new NameNotFoundException("Passive declaration of an unknown queue.");
      }
      if (queue != null && queue.equals("")) {
        queueName = null;
      }
      QueueAgent queueAgent = new QueueAgent(queueName, durable, autoDelete);
      queueAgent.deploy();
      queueName = queueAgent.getName();
      NamingAgent.getSingleton().bind(queueName, queueAgent.getId());
      
      // All message queues MUST BE automatically bound to the nameless exchange using the
      // message queue's name as routing key.
      queueBind(channelId, queueName, ExchangeAgent.DEFAULT_EXCHANGE_NAME, queueName, null);

      if (exclusive) {
        exclusiveQueues.add(queueAgent.getId());
      }
    }

    ChannelContext context = (ChannelContext) channelContexts.get(new Integer(channelId));
    context.lastQueueCreated = queueName;

    // TODO msgCount / consumerCount
    return new AMQP.Queue.DeclareOk(queueName, 0, 0);
  }
  
  private void doReact(QueueDeleteNot not) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "ProxyAgent.queueDelete(" + not + ')');
    try {
      queueDelete(
          not.getChannelId(),
          not.getQueue(),
          not.isIfUnused(),
          not.isIfEmpty(),
          not.isNowait(),
          not);
    } catch (NameNotFoundException nnfe) {
      not.Throw(nnfe);
    }
  }
  
  public void queueDelete(int channelId, String queue, boolean ifUnused, boolean ifEmpty,
      boolean nowait, QueueDeleteNot not) throws Exception {

    AgentId queueId = (AgentId) NamingAgent.getSingleton().lookup(queue);
    if (queueId == null) {
      throw new NameNotFoundException("Unknown queue for deletion.");
    }

    // Clean non-acked deliveries.
    Iterator iter = deliveriesToAck.iterator();
    while (iter.hasNext()) {
      DeliverContext delivery = (DeliverContext) iter.next();
      if (delivery.queueId.equals(queueId)) {
        iter.remove();
      }
    }

    // Remove queue from exclusive list
    exclusiveQueues.remove(queueId);

    // Remove consumers using this queue
    ChannelContext channelContext = (ChannelContext) channelContexts.get(new Integer(channelId));
    Iterator consumerQueueIterator = channelContext.consumers.values().iterator();
    while (consumerQueueIterator.hasNext()) {
      AgentId consumerQueueId = (AgentId) consumerQueueIterator.next();
      if (consumerQueueId.equals(queueId)) {
        consumerQueueIterator.remove();
      }
    }

    pendingRequests.put(queueId, not);
    sendTo(queueId, new DeleteNot(ifUnused, ifEmpty));
  }

  private void doReact(QueuePurgeNot not) {
    AMQP.Queue.PurgeOk res = queuePurge(
        not.getChannelId(),
        not.getQueue(),
        not.isNowait());
    not.Return(res);
  }

  public PurgeOk queuePurge(int channelId, String queue, boolean nowait) {
    AgentId queueId = (AgentId) NamingAgent.getSingleton().lookup(queue);
    ClearQueueNot purgeNot = new ClearQueueNot();
    sendTo(queueId, purgeNot);
    return new AMQP.Queue.PurgeOk(0);
  }

  private void doReact(BasicConsumeNot not) throws Exception {
    try {
      AMQP.Basic.ConsumeOk consumeOk = basicConsume(
          not.getChannelId(),
          not.getQueue(),
          not.isNoAck(),
          not.getConsumerTag(),
          not.isNoWait(),
          not.getCallback());
      not.Return(consumeOk);
    } catch (NameNotFoundException nnfe) {
      not.Throw(nnfe);
    } catch (IllegalArgumentException iae) {
      not.Throw(iae);
    }
  }
  
  public AMQP.Basic.ConsumeOk basicConsume(int channelId, String queue, boolean noAck, String consumerTag,
      boolean noWait, DeliveryListener callback) throws Exception {
    if (queue == null || queue.equals("")) {
      throw new IllegalArgumentException("Consuming from unspecified queue.");
    }

    ChannelContext channelContext = (ChannelContext) channelContexts.get(new Integer(channelId));
    // The consumer tag is local to a channel, so two clients can use the
    // same consumer tags. If this field is empty the server will generate a unique tag.
    String tag = consumerTag;
    if (consumerTag.equals("")) {
      channelContext.consumerTag++;
      tag = "genTag-" + channelId + "-" + channelContext.consumerTag;
    }

    if (channelContext.consumers.get(tag) != null) {
      throw new IllegalArgumentException("Consume request failed due to non-unique tag.");
    }

    AgentId queueId = (AgentId) NamingAgent.getSingleton().lookup(queue);
    if (queueId == null) {
      throw new NameNotFoundException("Consuming from non-existent queue.");
    }

    ConsumeNot consumeNot = new ConsumeNot(channelId, callback, this, tag, noAck);
    sendTo(queueId, consumeNot);
    channelContext.consumers.put(tag, queueId);
    return new AMQP.Basic.ConsumeOk(tag);
  }
  
  private void doReact(BasicCancelNot not) {
    AMQP.Basic.CancelOk res = basicCancel(not.getChannelId(), not.getConsumerTag());
    not.Return(res);
  }

  public CancelOk basicCancel(int channelId, String consumerTag) {
    ChannelContext channelContext = (ChannelContext) channelContexts.get(new Integer(channelId));
    AgentId queueId = (AgentId) channelContext.consumers.remove(consumerTag);
    if (queueId != null) {
      CancelNot cancelNot = new CancelNot(consumerTag);
      sendTo(queueId, cancelNot);
    }
    return new AMQP.Basic.CancelOk(consumerTag);
  }

  private void doReact(BasicGetNot not) {
    basicGet(not.getChannelId(), not.getQueueName(), not.isNoAck(), not.getCallback());
    not.Return();
  }
  
  public void basicGet(int channelId, String queue, boolean noAck, GetListener callback) {
    String queueName = queue;
    if (queueName.equals("")) {
      queueName = ((ChannelContext) channelContexts.get(new Integer(channelId))).lastQueueCreated;
    }
    AgentId queueId = (AgentId) NamingAgent.getSingleton().lookup(queueName);
    ReceiveNot receiveNot = new ReceiveNot(channelId, callback, this, noAck);
    sendTo(queueId, receiveNot);
  }

  private void doReact(BasicPublishNot not) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "ProxyAgent.basicPublish(" + not + ")");
    basicPublish(
        not.getChannelId(),
        not.getExchange(),
        not.getRoutingKey(),
        not.isMandatory(),
        not.isImmediate(),
        not.getProps(),
        not.getBody());
  }

  public void basicPublish(int channelId, String exchange, String routingKey, boolean mandatory,
      boolean immediate, BasicProperties props, byte[] body) throws Exception {
    AgentId exchangeId = (AgentId) NamingAgent.getSingleton().lookup(exchange);
    //    if (exchangeId == null) {
    //      throw new NameNotFoundException("Exchange " + exchange + " not found");
    //    }
    PublishNot publishNot = new PublishNot(exchange, routingKey, props, body);
    sendTo(exchangeId, publishNot);
  }
  
  private void doReact(BasicAckNot not) {
    basicAck(not.getChannelId(), not.getDeliveryTag(), not.isMultiple());
    not.Return();
  }
  
  public void basicAck(int channelId, long deliveryTag, boolean multiple) {
    Iterator iter = deliveriesToAck.iterator();
    if (!multiple) {
      while (iter.hasNext()) {
        DeliverContext delivery = (DeliverContext) iter.next();
        if (delivery.deliveryTag == deliveryTag && delivery.channelId == channelId) {
          List ackList = new ArrayList(1);
          ackList.add(new Long(delivery.queueMsgId));
          sendTo(delivery.queueId, new AckNot(ackList));
          return;
        }
      }
    } else {
      Map deliveryMap = new HashMap();
      while (iter.hasNext()) {
        DeliverContext delivery = (DeliverContext) iter.next();
        if (delivery.deliveryTag <= deliveryTag && delivery.channelId == channelId) {
          List ackList = (List) deliveryMap.get(delivery.queueId);
          if (ackList == null) {
            ackList = new ArrayList();
            deliveryMap.put(delivery.queueId, ackList);
          }
          ackList.add(new Long(delivery.queueMsgId));
        } else if (delivery.deliveryTag > deliveryTag) {
          break;
        }
      }
      Iterator iterQueues = deliveryMap.keySet().iterator();
      while (iterQueues.hasNext()) {
        AgentId queueId = (AgentId) iterQueues.next();
        sendTo(queueId, new AckNot((List) deliveryMap.get(queueId)));
      }
    }
  }

  private void doReact(BasicRecoverNot not) {
    basicRecover(not.getChannelId(), not.isRequeue());
    not.Return();
  }

  public void basicRecover(int channelId, boolean requeue) {
    // Recover non-acked messages on the channel
    Iterator iter = deliveriesToAck.iterator();
    Map recoverMap = new HashMap();
    while (iter.hasNext()) {
      DeliverContext delivery = (DeliverContext) iter.next();
      if (delivery.channelId == channelId) {
        List ackList = (List) recoverMap.get(delivery.queueId);
        if (ackList == null) {
          ackList = new ArrayList();
          recoverMap.put(delivery.queueId, ackList);
        }
        ackList.add(new Long(delivery.queueMsgId));
        iter.remove();
      }
    }
    Iterator iterQueues = recoverMap.keySet().iterator();
    while (iterQueues.hasNext()) {
      AgentId queueId = (AgentId) iterQueues.next();
      sendTo(queueId, new RecoverNot((List) recoverMap.get(queueId)));
    }
  }

  private void doReact(QueueBindNot not) throws Exception {
    try {
      AMQP.Queue.BindOk res = queueBind(
          not.getChannelId(),
          not.getQueue(),
          not.getExchange(),
          not.getRoutingKey(),
          not.getArguments());
      not.Return(res);
    } catch (SyntaxErrorException exc) {
      not.Throw(exc);
    } catch (NameNotFoundException nnfe) {
      not.Throw(nnfe);
    }
  }
  
  public AMQP.Queue.BindOk queueBind(int channelId, String queue, String exchange,
      String routingKey, Map arguments) throws Exception {
    /*
     * If the queue name is empty, the server uses the last queue declared on
     * the channel. If the routing key is also empty, the server uses this queue
     * name for the routing key as well. If the queue name is provided but the
     * routing key is empty, the server does the binding with that empty routing
     * key.
     */
    String queueName = queue;
    String rkey = routingKey;
    if (queueName.equals("")) {
      queueName = ((ChannelContext) channelContexts.get(new Integer(channelId))).lastQueueCreated;
      /*
       * If the client did not declare a queue, and the method needs a queue
       * name, this will result in a 502 (syntax error) channel exception.
       */
      if (queueName == null) {
        throw new SyntaxErrorException("No queue declared.");
      }
      if (routingKey.equals("")) {
        rkey = queueName;
      }
    }
    AgentId exchangeId = (AgentId) NamingAgent.getSingleton().lookup(exchange);
    if (exchangeId == null) {
      throw new NameNotFoundException("Binding to a non-existent exchange.");
    }
    AgentId queueId = (AgentId) NamingAgent.getSingleton().lookup(queueName);
    if (queueId == null) {
      throw new NameNotFoundException("Binding to a non-existent queue.");
    }

    BindNot bindNot = new BindNot(queueId, rkey, arguments);
    sendTo(exchangeId, bindNot);
    return new AMQP.Queue.BindOk();
  }

  private void doReact(QueueUnbindNot not) {
    try {
      AMQP.Queue.UnbindOk res = queueUnbind(
          not.getChannelId(),
          not.getQueue(),
          not.getExchange(),
          not.getRoutingKey(),
          not.getArguments());
      not.Return(res);
    } catch (NameNotFoundException exc) {
      not.Throw(exc);
    }
  }

  public UnbindOk queueUnbind(int channelId, String queue, String exchange, String routingKey,
      Map arguments) throws NameNotFoundException {
    AgentId exchangeId = (AgentId) NamingAgent.getSingleton().lookup(exchange);
    if (exchangeId != null) {
      AgentId queueId = (AgentId) NamingAgent.getSingleton().lookup(queue);
      if (queueId != null) {
        UnbindNot unbindNot = new UnbindNot(queueId, routingKey, arguments);
        sendTo(exchangeId, unbindNot);
        return new AMQP.Queue.UnbindOk();
      } else {
        throw new NameNotFoundException("Queue not found.");
      }
    } else {
      throw new NameNotFoundException("Exchange not found.");
    }
  }

  private void doReact(DeleteAck not) {
    SyncNotification syncNot = (SyncNotification) pendingRequests.get(not.agent);
    if (syncNot instanceof QueueDeleteNot) {
      // TODO msgCount
      int msgCount = 0;
      QueueDeleteNot deleteNot = (QueueDeleteNot) syncNot;
      if (not.exc == null) {
        deleteNot.Return(new AMQP.Queue.DeleteOk(msgCount));
      } else {
        deleteNot.Throw((Exception) not.exc);
      }
    } else if (syncNot instanceof ExchangeDeleteNot) {
      ExchangeDeleteNot deleteNot = (ExchangeDeleteNot) syncNot;
      if (not.exc == null) {
        deleteNot.Return(new AMQP.Exchange.DeleteOk());
      } else {
        deleteNot.Throw((Exception) not.exc);
      }
    }
  }
  
  private void doReact(ChannelCloseNot not) {
    channelClose(not.getChannelId());
    not.Return();
  }
  
  public void channelClose(int channelId) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "ProxyAgent.channelClose(" + channelId + ")");

    // Close consumers on the channel
    ChannelContext channelContext = (ChannelContext) channelContexts.remove(new Integer(channelId));
    if (channelContext != null) {
      Set entrySet = channelContext.consumers.entrySet();
      for (Iterator iterator = entrySet.iterator(); iterator.hasNext();) {
        Map.Entry consumer = (Map.Entry) iterator.next();
        CancelNot cancelNot = new CancelNot((String) consumer.getKey());
        sendTo((AgentId) consumer.getValue(), cancelNot);
      }
    }
    basicRecover(channelId, true);
  }

  private void doReact(ConnectionCloseNot not) {
    connectionClose();
    not.Return();
  }

  private void connectionClose() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "ProxyAgent.connectionClose()");

    // Close consumers on remaining channels
    Collection contexts = channelContexts.values();
    for (Iterator iterator = contexts.iterator(); iterator.hasNext();) {
      ChannelContext channelContext = (ChannelContext) iterator.next();
      Set entrySet = channelContext.consumers.entrySet();
      for (Iterator subIterator = entrySet.iterator(); subIterator.hasNext();) {
        Map.Entry consumer = (Map.Entry) subIterator.next();
        CancelNot cancelNot = new CancelNot((String) consumer.getKey());
        sendTo((AgentId) consumer.getValue(), cancelNot);
      }
    }
    channelContexts.clear();

    // Recover all non-acked messages
    Iterator iter = deliveriesToAck.iterator();
    Map recoverMap = new HashMap();
    while (iter.hasNext()) {
      DeliverContext delivery = (DeliverContext) iter.next();
      List ackList = (List) recoverMap.get(delivery.queueId);
      if (ackList == null) {
        ackList = new ArrayList();
        recoverMap.put(delivery.queueId, ackList);
      }
      ackList.add(new Long(delivery.queueMsgId));
      iter.remove();
    }
    Iterator iterQueues = recoverMap.keySet().iterator();
    while (iterQueues.hasNext()) {
      AgentId queueId = (AgentId) iterQueues.next();
      sendTo(queueId, new RecoverNot((List) recoverMap.get(queueId)));
    }

    // Delete exclusive queues
    iterQueues = exclusiveQueues.iterator();
    while (iterQueues.hasNext()) {
      AgentId queueId = (AgentId) iterQueues.next();
      sendTo(queueId, new DeleteNot(false, false));
    }
    exclusiveQueues.clear();
  }

  public synchronized long getDeliveryTag(AgentId queueId, int channelId, long queueMsgId, boolean noAck) {
    long deliveryTag = tagCounter++;
    if (!noAck) {
      deliveriesToAck.add(new DeliverContext(queueId, channelId, queueMsgId, deliveryTag));
    }
    return deliveryTag;
  }

}
