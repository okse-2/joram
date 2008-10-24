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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.ListIterator;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import com.rabbitmq.client.AMQP.BasicProperties;

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;

public class QueueAgent extends Agent {
  
  public final static Logger logger = 
    fr.dyade.aaa.util.Debug.getLogger(QueueAgent.class.getName());
  
  private LinkedList toDeliver;
  
  private LinkedList consumers;
  
  public QueueAgent() {
    this.toDeliver = new LinkedList();
    this.consumers = new LinkedList();
  }
  
  public void react(AgentId from, Notification not) throws Exception {
    if (not instanceof ConsumeNot) {
      doReact((ConsumeNot) not);
    } else if (not instanceof PublishNot) {
      doReact((PublishNot) not);
    } else if (not instanceof ReceiveNot) {
      doReact((ReceiveNot) not);
    } else if (not instanceof CancelNot) {
      doReact((CancelNot) not);
    } else if (not instanceof DeleteNot) {
      doReact((DeleteNot) not, from);
    } else if (not instanceof ClearQueueNot) {
      doReact((ClearQueueNot) not);
    } else {
      super.react(from, not);
    }
  }

  private void doReact(ReceiveNot not) {
    receive(not.getCallback());
  }
  
  public void receive(GetListener consumer) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "QueueAgent.receive()");
    if (toDeliver.size() > 0) {
      Message msg = (Message) toDeliver.removeFirst();
      consumer.handleGet(0, false, msg.exchange, msg.routingKey, toDeliver.size(), msg.properties, msg.body);
    } else {
      // Blocking get
      //      consumers.addLast(new DeliverContext(consumer));

      // Get empty
      consumer.handleGet(-1, false, null, null, 0, null, null);
    }
    
  }

  private void doReact(ConsumeNot not) {
    consume(not.getCallback(), not.getConsumerTag());
  }

  public void consume(DeliveryListener consumer, String consumerTag) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "QueueAgent.consume()");
    while (toDeliver.size() > 0) {
      Message msg = (Message) toDeliver.removeFirst();
      consumer.handleDelivery(0, false, msg.exchange, msg.routingKey, msg.properties, msg.body);
    }
    consumers.addLast(new DeliverContext(consumer, consumerTag));
  }
  
  private void doReact(PublishNot not) {
    publish(not.getExchange(), not.getRoutingKey(), not.getProperties(), not.getBody());
  }
  
  public void publish(String exchange, String routingKey, BasicProperties properties, byte[] body) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "QueueAgent.publish(" + properties + ')');
    
    if (consumers.size() > 0) {
      DeliverContext deliverContext = (DeliverContext) consumers.removeFirst();
      Listener callback = deliverContext.getConsumer();
      if (callback instanceof DeliveryListener) {
        DeliveryListener consumer = (DeliveryListener) callback;
        consumer.handleDelivery(0, false, exchange, routingKey, properties, body);
      } else if (callback instanceof GetListener) {
        GetListener consumer = (GetListener) callback;
        consumer.handleGet(0, false, exchange, routingKey, toDeliver.size(), properties, body);
      }
      if (deliverContext.isSubscription) {
        consumers.addLast(deliverContext);
      }
    } else {
      toDeliver.addLast(new Message(exchange, routingKey, properties, body));
    }
  }

  private void doReact(CancelNot not) {
    cancel(not.getConsumerTag());
  }

  public void cancel(String consumerTag) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "QueueAgent.cancel()");
    ListIterator iterator = consumers.listIterator();
    while (iterator.hasNext()) {
      DeliverContext deliverContext = (DeliverContext) iterator.next();
      if (deliverContext.isSubscription && deliverContext.getConsumerTag().equals(consumerTag)) {
        iterator.remove();
      }
    }
  }

  private void doReact(DeleteNot not, AgentId from) {
    delete(from);
  }
  
  private void doReact(ClearQueueNot not) {
    toDeliver.clear();
  }

  static class Message implements Serializable {

    private String exchange;
    private String routingKey;
    private BasicProperties properties;
    private byte[] body;

    /**
     * @param properties
     * @param body
     */
    public Message(String exchange, String routingKey, BasicProperties properties, byte[] body) {
      this.exchange = exchange;
      this.routingKey = routingKey;
      this.properties = properties;
      this.body = body;
    }

    public String getExchange() {
      return exchange;
    }

    public String getRoutingKey() {
      return routingKey;
    }

    public byte[] getBody() {
      return body;
    }
    
    public BasicProperties getProperties() {
      return properties;
    }
  }
  
  
  static class DeliverContext implements Serializable {

    private Listener consumer;
    private boolean isSubscription;
    private String consumerTag;
    
    public DeliverContext(GetListener consumer) {
      super();
      this.consumer = consumer;
      this.isSubscription = false;
    }

    public DeliverContext(DeliveryListener consumer, String consumerTag) {
      super();
      this.consumer = consumer;
      this.consumerTag = consumerTag;
      this.isSubscription = true;
    }

    public Listener getConsumer() {
      return consumer;
    }
    
    public String getConsumerTag() {
      return consumerTag;
    }

    public boolean isSubscription() {
      return isSubscription;
    }
    
  }

}
