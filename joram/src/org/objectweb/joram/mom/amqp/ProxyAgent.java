/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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

import java.util.HashMap;
import java.util.Map;

import org.objectweb.joram.mom.amqp.proxy.request.AccessRequestNot;
import org.objectweb.joram.mom.amqp.proxy.request.BasicConsumeNot;
import org.objectweb.joram.mom.amqp.proxy.request.BasicGetNot;
import org.objectweb.joram.mom.amqp.proxy.request.BasicPublishNot;
import org.objectweb.joram.mom.amqp.proxy.request.ChannelOpenNot;
import org.objectweb.joram.mom.amqp.proxy.request.ConnectionStartOkNot;
import org.objectweb.joram.mom.amqp.proxy.request.ExchangeDeclareNot;
import org.objectweb.joram.mom.amqp.proxy.request.ExchangeDeleteNot;
import org.objectweb.joram.mom.amqp.proxy.request.QueueBindNot;
import org.objectweb.joram.mom.amqp.proxy.request.QueueDeclareNot;
import org.objectweb.joram.mom.amqp.proxy.request.QueueDeleteNot;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.Exchange.DeleteOk;
import com.rabbitmq.client.impl.AMQImpl;

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;

public class ProxyAgent extends Agent {
  
  public final static Logger logger = 
    fr.dyade.aaa.util.Debug.getLogger(ProxyAgent.class.getName());
  
  public static final String USER_NAME = "userName";
  
  public static final String PASSWORD = "password";
  
  private String userName;
  
  private String password;
  
  private int channelCounter;
  
  private int ticketCounter;
  
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
    } else if (not instanceof BasicConsumeNot) {
      doReact((BasicConsumeNot) not);
    } else if (not instanceof BasicGetNot) {
      doReact((BasicGetNot) not);
    } else if (not instanceof BasicPublishNot) {
      doReact((BasicPublishNot) not);
    } else if (not instanceof QueueBindNot) {
      doReact((QueueBindNot) not);
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
    return new AMQImpl.Channel.OpenOk();
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
    return new AMQImpl.Access.RequestOk(ticketCounter++);
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
    if (ref == null) {
      String className = (String) arguments.get("joram.exchange.class.name");
      Class exchangeClass = Class.forName(className);
      ExchangeAgent exchangeAgent = (ExchangeAgent) exchangeClass.newInstance();
      exchangeAgent.setArguments(arguments);
      NamingAgent.getSingleton().bind(exchange, exchangeAgent.getId());
      exchangeAgent.deploy();
    }
    return new AMQImpl.Exchange.DeclareOk();
  }
  
  private void doReact(ExchangeDeleteNot not) {
    AMQP.Exchange.DeleteOk res = exchangeDelete(
        not.getChannelId(),
        not.getTicket(),
        not.getExchange(),
        not.isIfUnused(),
        not.isNowait());
    not.Return(res);
  }

  public DeleteOk exchangeDelete(int channelId, int ticket, String exchange, boolean ifUnused, boolean nowait) {
    // Check if the exchange exists
    Object ref = NamingAgent.getSingleton().lookup(exchange);
    if (ref != null) {
      // TODO
      // NamingAgent.getSingleton().unbind(exchange);
      // exchangeAgent.delete();
    }
    return new AMQImpl.Exchange.DeleteOk();
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
    if (ref == null) {
      QueueAgent queueAgent = new QueueAgent();
      NamingAgent.getSingleton().bind(queue, queueAgent.getId());
      queueAgent.deploy();
    }
    return new AMQImpl.Queue.DeclareOk(queue, 0, 0);
  }
  
  private void doReact(QueueDeleteNot not) {
    AMQP.Queue.DeleteOk res = queueDelete(
        not.getChannelId(),
        not.getTicket(),
        not.getQueue(),
        not.isIfUnused(),
        not.isIfEmpty(),
        not.isNowait());
    not.Return(res);
  }
  
  public AMQP.Queue.DeleteOk queueDelete(int channelId, int ticket, String queue,
      boolean ifUnused, boolean ifEmpty, boolean nowait) {
    // TODO Auto-generated method stub
    int msgCount = 0;
    return new AMQImpl.Queue.DeleteOk(msgCount);
  }

  private void doReact(BasicConsumeNot not) throws Exception {
    AMQP.Basic.ConsumeOk res = basicConsume(
        not.getChannelId(),
        not.getTicket(),
        not.getQueue(),
        not.isNoAck(),
        not.getConsumerTag(),
        not.getCallback());
    not.Return(res);
  }
  
  public AMQP.Basic.ConsumeOk basicConsume(int channelId, int ticket, String queue,
      boolean noAck, String consumerTag, DeliveryListener callback)
      throws Exception {
    AgentId queueId = (AgentId) NamingAgent.getSingleton().lookup(queue);
    ConsumeNot consumeNot = new ConsumeNot(callback);
    sendTo(queueId, consumeNot);
    return new AMQImpl.Basic.ConsumeOk(consumerTag);
  }

  private void doReact(BasicGetNot not) {
    basicGet(not.getChannelId(), not.getTicket(), not.getQueueName(), not.isNoAck(), not.getCallback());
    not.Return();
  }
  
  public AMQP.Basic.GetOk basicGet(int channelId, int ticket, String queueName, boolean noAck,
      GetListener callback) {
    AgentId queueId = (AgentId) NamingAgent.getSingleton().lookup(queueName);
    ReceiveNot receiveNot = new ReceiveNot(callback);
    sendTo(queueId, receiveNot);
    return new AMQImpl.Basic.GetOk(0, false, "?", "?", 0);
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
    AgentId exhangeId = (AgentId) NamingAgent.getSingleton().lookup(exchange);
    PublishNot publishNot = new PublishNot(routingKey, props, body);
    sendTo(exhangeId, publishNot);
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
      String routingKey, HashMap arguments) throws Exception {
    AgentId exhangeId = (AgentId) NamingAgent.getSingleton().lookup(exchange);
    BindNot bindNot = new BindNot(queue, routingKey, arguments);
    sendTo(exhangeId, bindNot);
    return new AMQImpl.Queue.BindOk();
  }

}
