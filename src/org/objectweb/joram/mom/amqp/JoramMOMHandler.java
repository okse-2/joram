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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.joram.mom.amqp.marshalling.AMQP;
import org.objectweb.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;
import org.objectweb.joram.mom.amqp.proxy.request.AccessRequestNot;
import org.objectweb.joram.mom.amqp.proxy.request.BasicAckNot;
import org.objectweb.joram.mom.amqp.proxy.request.BasicCancelNot;
import org.objectweb.joram.mom.amqp.proxy.request.BasicConsumeNot;
import org.objectweb.joram.mom.amqp.proxy.request.BasicGetNot;
import org.objectweb.joram.mom.amqp.proxy.request.BasicPublishNot;
import org.objectweb.joram.mom.amqp.proxy.request.ChannelCloseNot;
import org.objectweb.joram.mom.amqp.proxy.request.ConnectionCloseNot;
import org.objectweb.joram.mom.amqp.proxy.request.ExchangeDeclareNot;
import org.objectweb.joram.mom.amqp.proxy.request.ExchangeDeleteNot;
import org.objectweb.joram.mom.amqp.proxy.request.QueueBindNot;
import org.objectweb.joram.mom.amqp.proxy.request.QueueDeclareNot;
import org.objectweb.joram.mom.amqp.proxy.request.QueueDeleteNot;
import org.objectweb.joram.mom.amqp.proxy.request.QueuePurgeNot;
import org.objectweb.joram.mom.amqp.proxy.request.QueueUnbindNot;

import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.util.Queue;

public class JoramMOMHandler implements MOMHandler {
  
  private NamingAgent naming;
  private ProxyAgent proxy;
  private Consumer consumer;

  public JoramMOMHandler() {
    try {
      naming = new NamingAgent();
      naming.deploy();
      proxy = new ProxyAgent();
      proxy.deploy();
    } catch (IOException exc) {
      // TODO Auto-generated catch block
      exc.printStackTrace();
    }
  }
  
  public void setConsumer(Consumer consumer) {
    this.consumer = consumer;
  }

  public AMQP.Access.RequestOk accessRequest(String realm, boolean exclusive, boolean passive,
      boolean active, boolean write, boolean read, int channelNumber) throws Exception {
    AccessRequestNot accessRequest = new AccessRequestNot(channelNumber, realm, exclusive, passive, active,
        write, read);
    AMQP.Access.RequestOk accessRes = accessRequest.accessRequest(proxy.getId());
    return accessRes;
  }
  
  public void basicAck(long deliveryTag, boolean multiple, int channelNumber) throws Exception {
    BasicAckNot basicAck = new BasicAckNot(channelNumber, deliveryTag, multiple);
    basicAck.basicAck(proxy.getId());
  }

  public void basicCancel(String consumerTag, int channelNumber) throws Exception {
    BasicCancelNot basicCancel = new BasicCancelNot(channelNumber, consumerTag);
    basicCancel.basicCancel(proxy.getId());
  }

  public void basicConsume(String queue, boolean noAck, String consumerTag, boolean noLocal,
      boolean exclusive, int ticket, boolean noWait, int channelNumber, Queue queueOut) throws Exception {
    BasicConsumeNot basicConsume = new BasicConsumeNot(channelNumber, ticket, queue, noAck, consumerTag,
        noWait, new DeliverMessageConsumer(channelNumber, consumerTag), queueOut);
    basicConsume.basicConsume(proxy.getId());
  }

  public void basicGet(String queue, boolean noAck, int ticket, int channelNumber) throws Exception {
    BasicGetNot basicGetNot = new BasicGetNot(channelNumber, ticket, queue, noAck,
        new GetMessageConsumer(channelNumber));
    basicGetNot.basicGet(proxy.getId());
  }

  public void basicPublish(PublishRequest publishRequest, int channelNumber) throws Exception {
    BasicPublishNot basicPublish = new BasicPublishNot(channelNumber,
        publishRequest.getPublish().reserved1,
        publishRequest.getPublish().exchange,
        publishRequest.getPublish().routingKey,
        publishRequest.getPublish().mandatory,
        publishRequest.getPublish().immediate,
        publishRequest.getHeader(),
        publishRequest.body);
    Channel.sendTo(proxy.getId(), basicPublish);
    
    // Let some time for the message to go to the exchange then the queue.
    try {
      Thread.sleep(50);
    } catch (Exception exc) {
    }
  }

  public void close() {
    // TODO Auto-generated method stub

  }

  public void exchangeDeclare(String exchangeName, String type, boolean passive, boolean durable,
      boolean autoDelete, Map arguments, int ticket, int channelNumber) throws Exception {
    ExchangeDeclareNot exchangeDeclare = new ExchangeDeclareNot(
        channelNumber, ticket, exchangeName, type, passive, durable, autoDelete, arguments);
    exchangeDeclare.exchangeDeclare(proxy.getId());
  }
  
  public void exchangeDelete(String exchangeName, boolean ifUnused, boolean nowait, int ticket,
      int channelNumber) throws Exception {
    ExchangeDeleteNot exchangeDelete = new ExchangeDeleteNot(channelNumber, ticket, exchangeName, ifUnused,
        nowait);
    exchangeDelete.exchangeDelete(proxy.getId());
  }

  public Map getMOMProperties() {
    Map tab = new HashMap();
    tab.put("product", "JORAM_AMQP");
    tab.put("platform", "java");
    tab.put("copyright", "ScalAgent");
    tab.put("version", "0.1");
    return tab;
  }

  public void queueBind(String queue, String exchange, boolean nowait, String routingKey, Map arguments,
      int ticket, int channelNumber) throws Exception {
    QueueBindNot queueBind = new QueueBindNot(channelNumber, ticket, queue, 
        exchange, routingKey, arguments);
    queueBind.queueBind(proxy.getId());
  }

  public void queueUnbind(String queue, String exchange, String routingKey, Map arguments, int ticket,
      int channelNumber) throws Exception {
    QueueUnbindNot queueUnbind = new QueueUnbindNot(channelNumber, ticket, queue, exchange, routingKey,
        arguments);
    queueUnbind.queueUnbind(proxy.getId());
  }

  public AMQP.Queue.DeclareOk queueDeclare(String queueName, boolean passive, boolean durable,
      boolean exclusive, boolean autoDelete, Map arguments, int ticket, int channelNumber) throws Exception {
    QueueDeclareNot queueDeclare = new QueueDeclareNot(
        channelNumber, ticket, queueName, passive, durable,
        exclusive, autoDelete, arguments);
    AMQP.Queue.DeclareOk queueDeclareOk = queueDeclare.queueDeclare(proxy.getId());
    return queueDeclareOk;
  }

  public AMQP.Queue.DeleteOk queueDelete(String queue, boolean ifUnused, boolean ifEmpty, boolean nowait,
      int ticket, int channelNumber) throws Exception {
    QueueDeleteNot queueDelete = new QueueDeleteNot(channelNumber, ticket, queue, ifUnused, ifEmpty, nowait);
    AMQP.Queue.DeleteOk queueDeleteOk = queueDelete.queueDelete(proxy.getId());
    return queueDeleteOk;
  }

  public void queuePurge(String queue, boolean nowait, int ticket, int channelNumber) throws Exception {
    QueuePurgeNot queuePurge = new QueuePurgeNot(channelNumber, ticket, queue, nowait);
    queuePurge.queuePurge(proxy.getId());
  }

  public void channelClose(int channelNumber) throws Exception {
    ChannelCloseNot channelClose = new ChannelCloseNot(channelNumber);
    channelClose.closeChannel(proxy.getId());
  }

  public void connectionClose() throws Exception {
    ConnectionCloseNot channelClose = new ConnectionCloseNot();
    channelClose.closeConnection(proxy.getId());
  }
  
  
  class DeliverMessageConsumer implements DeliveryListener {
    
    private int channelNumber;
    private String consumerTag;

    public DeliverMessageConsumer(int channelNumber, String consumerTag) {
      this.channelNumber = channelNumber;
      this.consumerTag = consumerTag;
    }

    public void handleDelivery(long deliveryTag, boolean redelivered, String exchange,
        String routingKey, BasicProperties properties, byte[] body) {
      AMQP.Basic.Deliver deliver = new AMQP.Basic.Deliver(consumerTag, deliveryTag, redelivered, exchange,
          routingKey);
      consumer.handleDelivery(channelNumber, deliver, properties, body);
    }
  }
  
  class GetMessageConsumer implements GetListener {

    private int channelNumber;

    public GetMessageConsumer(int channelNumber) {
      this.channelNumber = channelNumber;
    }
    
    public void handleGet(long deliveryTag, boolean redelivered, String exchange, String routingKey,
        int msgCount, BasicProperties properties, byte[] body) {
      AMQP.Basic.GetOk getOk = new AMQP.Basic.GetOk(deliveryTag, redelivered, exchange, routingKey,
          msgCount);
      consumer.handleGet(channelNumber, getOk, properties, body);
    }
  }
}
