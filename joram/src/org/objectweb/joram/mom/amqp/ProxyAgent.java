/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
 * Copyright (C) 2008 CNES
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.objectweb.joram.mom.amqp.marshalling.AMQP;
import org.objectweb.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;
import org.objectweb.joram.mom.amqp.marshalling.AMQP.Basic.CancelOk;
import org.objectweb.joram.mom.amqp.marshalling.AMQP.Queue.PurgeOk;
import org.objectweb.joram.mom.amqp.marshalling.AMQP.Queue.UnbindOk;
import org.objectweb.joram.mom.amqp.proxy.request.AccessRequestNot;
import org.objectweb.joram.mom.amqp.proxy.request.BasicAckNot;
import org.objectweb.joram.mom.amqp.proxy.request.BasicCancelNot;
import org.objectweb.joram.mom.amqp.proxy.request.BasicConsumeNot;
import org.objectweb.joram.mom.amqp.proxy.request.BasicGetNot;
import org.objectweb.joram.mom.amqp.proxy.request.BasicPublishNot;
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
import fr.dyade.aaa.util.Queue;

public class ProxyAgent extends Agent {
  
  public final static Logger logger = 
    fr.dyade.aaa.util.Debug.getLogger(ProxyAgent.class.getName());
  
  public static final String USER_NAME = "userName";
  
  public static final String PASSWORD = "password";
  
  private String userName;
  
  private String password;
  
  private int ticketCounter;
  
  // Links between consumer tags and queue ids
  private Map consumers = new HashMap();
  
  private Map pendingRequests = new HashMap();
  
  // Used to find queue from deliveryTag
  private LinkedList deliveriesToAck = new LinkedList();
  
  private long tagCounter;
  
  
  public void react(AgentId from, Notification not) throws Exception {
    // This agent is not persistent.
    setNoSave();
    if (not instanceof ConnectionStartOkNot) {
      doReact((ConnectionStartOkNot) not);
    } else if (not instanceof ChannelOpenNot) {
      doReact((ChannelOpenNot) not);
    } else if (not instanceof AccessRequestNot) {
      doReact((AccessRequestNot) not);
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
    AMQP.Channel.OpenOk res = channelOpen();
    not.Return(res);
  }
  
  public AMQP.Channel.OpenOk channelOpen() {
    return new AMQP.Channel.OpenOk();
  }
  
  private void doReact(AccessRequestNot not) throws Exception {
    AMQP.Access.RequestOk res = accessRequest(
        not.getChannelId(),
        not.getRealm(),
        not.isExclusive(),
        not.isPassive(),
        not.isActive(),
        not.isWrite(),
        not.isRead());
    not.Return(res);
  }
  
  public AMQP.Access.RequestOk accessRequest(int channelId, String realm,
      boolean exclusive, boolean passive, boolean active, boolean write,
      boolean read) throws Exception {
    return new AMQP.Access.RequestOk(ticketCounter++);
  }
  
  private void doReact(ExchangeDeclareNot not) throws Exception {
    AMQP.Exchange.DeclareOk res = exchangeDeclare(
        not.getChannelId(),
        not.getTicket(),
        not.getExchange(),
        not.getType(),
        not.isPassive(),
        not.isDurable(),
        not.isAutoDelete(),
        not.getArguments());
    not.Return(res);
  }

  public AMQP.Exchange.DeclareOk exchangeDeclare(int channelId, int ticket,
      String exchange, String type, boolean passive, boolean durable, boolean autoDelete, 
      Map arguments) throws Exception {
    // Check if the exchange already exists
    Object ref = NamingAgent.getSingleton().lookup(exchange);
    if (ref == null && !passive) {
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
      exchangeAgent.deploy();
    }
    return new AMQP.Exchange.DeclareOk();
  }
  
  private void doReact(ExchangeDeleteNot not) throws Exception {
    exchangeDelete(
        not.getChannelId(),
        not.getTicket(),
        not.getExchange(),
        not.isIfUnused(),
        not.isNowait(),
        not);
  }

  public void exchangeDelete(int channelId, int ticket, String exchange, boolean ifUnused, boolean nowait,
      ExchangeDeleteNot not) throws Exception {
    AgentId exchangeId = (AgentId) NamingAgent.getSingleton().lookup(exchange);
    if (exchangeId != null) {
      pendingRequests.put(exchangeId, not);
      sendTo(exchangeId, new DeleteNot(ifUnused));
    }
  }

  private void doReact(QueueDeclareNot not) throws Exception {
    AMQP.Queue.DeclareOk res = queueDeclare(
        not.getChannelId(),
        not.getTicket(),
        not.getQueue(),
        not.isPassive(),
        not.isDurable(),
        not.isExclusive(),
        not.isAutoDelete(),
        not.getArguments());
    not.Return(res);
  }

  public AMQP.Queue.DeclareOk queueDeclare(int channelId, int ticket, String queue,
      boolean passive, boolean durable, boolean exclusive, boolean autoDelete,
      Map arguments) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "ProxyAgent.queueDeclare(" + 
          channelId + ',' + ticket + ',' + queue + ')');
    // Check if the queue already exists
    Object ref = NamingAgent.getSingleton().lookup(queue);
    String queueName = queue;
    if (ref == null && !passive) {
      QueueAgent queueAgent = new QueueAgent(queue, durable, autoDelete);
      queueAgent.deploy();
      if (queueName == null || queueName.equals("")) {
        queueName = queueAgent.getAgentId();
      }
      NamingAgent.getSingleton().bind(queueName, queueAgent.getId());
    }
    // TODO msgCount / consumerCount
    return new AMQP.Queue.DeclareOk(queueName, 0, 0);
  }
  
  private void doReact(QueueDeleteNot not) throws Exception {
    queueDelete(
        not.getChannelId(),
        not.getTicket(),
        not.getQueue(),
        not.isIfUnused(),
        not.isIfEmpty(),
        not.isNowait(),
        not);
  }
  
  public void queueDelete(int channelId, int ticket, String queue, boolean ifUnused, boolean ifEmpty,
      boolean nowait, QueueDeleteNot not) throws Exception {
    AgentId queueId = (AgentId) NamingAgent.getSingleton().lookup(queue);
    if (queueId != null) {
      pendingRequests.put(queueId, not);
      sendTo(queueId, new DeleteNot(ifUnused, ifEmpty));
      //      NamingAgent.getSingleton().unbind(name);
    }
  }

  private void doReact(QueuePurgeNot not) {
    AMQP.Queue.PurgeOk res = queuePurge(
        not.getChannelId(),
        not.getTicket(),
        not.getQueue(),
        not.isNowait());
    not.Return(res);
  }

  public PurgeOk queuePurge(int channelId, int ticket, String queue, boolean nowait) {
    AgentId queueId = (AgentId) NamingAgent.getSingleton().lookup(queue);
    ClearQueueNot purgeNot = new ClearQueueNot();
    sendTo(queueId, purgeNot);
    return new AMQP.Queue.PurgeOk(0);
  }

  private void doReact(BasicConsumeNot not) throws Exception {
    basicConsume(
        not.getChannelId(),
        not.getTicket(),
        not.getQueue(),
        not.isNoAck(),
        not.getConsumerTag(),
        not.isNoWait(),
        not.getCallback(),
        not.getQueueOut());
  }
  
  public void basicConsume(int channelId, int ticket, String queue,
      boolean noAck, String consumerTag, boolean noWait, DeliveryListener callback, Queue queueOut)
      throws Exception {
    AgentId queueId = (AgentId) NamingAgent.getSingleton().lookup(queue);
    ConsumeNot consumeNot = new ConsumeNot(channelId, callback, this, consumerTag, noAck);
    sendTo(queueId, consumeNot);
    consumers.put(consumerTag, queueId);
    if (!noWait) {
      // TODO The marshalling code should not be there
      queueOut.push(new AMQP.Basic.ConsumeOk(consumerTag).toFrame(channelId));
    }
  }
  
  private void doReact(BasicCancelNot not) {
    AMQP.Basic.CancelOk res = basicCancel(not.getConsumerTag());
    not.Return(res);
  }

  public CancelOk basicCancel(String consumerTag) {
    AgentId queueId = (AgentId) consumers.remove(consumerTag);
    if (queueId != null) {
      CancelNot cancelNot = new CancelNot(consumerTag);
      sendTo(queueId, cancelNot);
    }
    return new AMQP.Basic.CancelOk(consumerTag);
  }

  private void doReact(BasicGetNot not) {
    basicGet(not.getChannelId(), not.getTicket(), not.getQueueName(), not.isNoAck(), not.getCallback());
    not.Return();
  }
  
  public void basicGet(int channelId, int ticket, String queueName, boolean noAck, GetListener callback) {
    AgentId queueId = (AgentId) NamingAgent.getSingleton().lookup(queueName);
    ReceiveNot receiveNot = new ReceiveNot(channelId, callback, this, noAck);
    sendTo(queueId, receiveNot);
  }

  private void doReact(BasicPublishNot not) throws Exception {
    basicPublish(
        not.getChannelId(),
        not.getTicket(),
        not.getExchange(),
        not.getRoutingKey(),
        not.isMandatory(),
        not.isImmediate(),
        not.getProps(),
        not.getBody());
  }

  public void basicPublish(int channelId, int ticket, String exchange,
      String routingKey, boolean mandatory, boolean immediate,
      BasicProperties props, byte[] body) throws Exception {
    AgentId exchangeId = (AgentId) NamingAgent.getSingleton().lookup(exchange);
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

  private void doReact(QueueBindNot not) throws Exception {
    AMQP.Queue.BindOk res = queueBind(
        not.getChannelId(),
        not.getTicket(),
        not.getQueue(),
        not.getExchange(),
        not.getRoutingKey(),
        not.getArguments());
    not.Return(res);
  }
  
  public AMQP.Queue.BindOk queueBind(int channelId, int ticket, String queue, String exchange,
      String routingKey, Map arguments) throws Exception {
    AgentId exchangeId = (AgentId) NamingAgent.getSingleton().lookup(exchange);
    BindNot bindNot = new BindNot(queue, routingKey, arguments);
    sendTo(exchangeId, bindNot);
    return new AMQP.Queue.BindOk();
  }

  private void doReact(QueueUnbindNot not) {
    AMQP.Queue.UnbindOk res = queueUnbind(
        not.getChannelId(),
        not.getTicket(),
        not.getQueue(),
        not.getExchange(),
        not.getRoutingKey(),
        not.getArguments());
    not.Return(res);
  }

  public UnbindOk queueUnbind(int channelId, int ticket, String queue, String exchange, String routingKey,
      Map arguments) {
    AgentId exchangeId = (AgentId) NamingAgent.getSingleton().lookup(exchange);
    UnbindNot unbindNot = new UnbindNot(queue, routingKey, arguments);
    sendTo(exchangeId, unbindNot);
    return new AMQP.Queue.UnbindOk();
  }

  private void doReact(DeleteAck not) {
    SyncNotification syncNot = (SyncNotification) pendingRequests.get(not.agent);
    if (syncNot instanceof QueueDeleteNot) {
      // TODO msgCount
      int msgCount = 0;
      QueueDeleteNot deleteNot = (QueueDeleteNot) syncNot;
      deleteNot.Return(new AMQP.Queue.DeleteOk(msgCount));
    } else if (syncNot instanceof ExchangeDeleteNot) {
      ExchangeDeleteNot deleteNot = (ExchangeDeleteNot) syncNot;
      deleteNot.Return(new AMQP.Exchange.DeleteOk());
    }
  }
  
  private void doReact(ChannelCloseNot not) {
    channelClose(not.getChannelId());
    not.Return();
  }
  
  public void channelClose(int channelId) {
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

  private void doReact(ConnectionCloseNot not) {
    connectionClose();
    not.Return();
  }

  private void connectionClose() {
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
  }

  public synchronized long getDeliveryTag(AgentId queueId, int channelId, long queueMsgId, boolean noAck) {
    long deliveryTag = tagCounter++;
    if (!noAck) {
      deliveriesToAck.add(new DeliverContext(queueId, channelId, queueMsgId, deliveryTag));
    }
    return deliveryTag;
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

}
