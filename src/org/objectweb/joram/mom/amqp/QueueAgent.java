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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.objectweb.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.DeleteAck;
import fr.dyade.aaa.agent.Notification;

public class QueueAgent extends Agent {
  
  public final static Logger logger = 
    fr.dyade.aaa.util.Debug.getLogger(QueueAgent.class.getName());
  
  private String name;
  
  private boolean durable;
  
  private boolean autodelete;
  
  private long msgCounter;
  
  private LinkedList toDeliver;
  
  private LinkedList toAck;
  
  private LinkedList consumers;
  
  public QueueAgent(String name, boolean durable, boolean autodelete) {
    this.name = name;
    this.durable = durable;
    this.autodelete = autodelete;
    this.toDeliver = new LinkedList();
    this.consumers = new LinkedList();
    this.toAck = new LinkedList();
  }
  
  protected void agentInitialize(boolean firstTime) throws Exception {
    super.agentInitialize(firstTime);
    if (!firstTime && !durable) {
      delete();
    }
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
    } else if (not instanceof AckNot) {
      doReact((AckNot) not);
    } else if (not instanceof RecoverNot) {
      doReact((RecoverNot) not);
    } else {
      super.react(from, not);
    }
    if (!durable) {
      setNoSave();
    }
  }

  private void doReact(ReceiveNot not) {
    receive(not.getChannelId(), not.getCallback(), not.getProxy(), not.isNoAck());
  }
  
  public void receive(int channelId, GetListener consumer, ProxyAgent proxy, boolean noAck) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "QueueAgent.receive()");
    if (toDeliver.size() > 0) {
      Message msg = (Message) toDeliver.removeFirst();
      if (!noAck) {
        toAck.addLast(msg);
      }
      // TODO should be a notification sent to proxy
      long deliveryTag = proxy.getDeliveryTag(getId(), channelId, msgCounter, noAck);
      consumer.handleGet(deliveryTag, msg.redelivered, msg.exchange, msg.routingKey, toDeliver.size(),
          msg.properties, msg.body);
    } else {
      // Blocking get
      // consumers.addLast(new DeliverContext(consumer, noAck));

      // Get empty
      consumer.handleGet(-1, false, null, null, 0, null, null);
    }
    
  }

  private void doReact(ConsumeNot not) {
    consume(not.getChannelId(), not.getCallback(), not.getProxy(), not.getConsumerTag(), not.isNoAck());
  }

  public void consume(int channelId, DeliveryListener consumer, ProxyAgent proxy, String consumerTag,
      boolean noAck) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "QueueAgent.consume()");
    while (toDeliver.size() > 0) {
      Message msg = (Message) toDeliver.removeFirst();
      if (!noAck) {
        toAck.addLast(msg);
      }
      // TODO should be a notification sent to proxy
      long deliveryTag = proxy.getDeliveryTag(getId(), channelId, msgCounter, noAck);
      consumer.handleDelivery(deliveryTag, msg.redelivered, msg.exchange, msg.routingKey, msg.properties,
          msg.body);
    }
    consumers.addLast(new DeliverContext(channelId, proxy, consumer, consumerTag, noAck));
  }
  
  private void doReact(PublishNot not) {
    publish(not.getExchange(), not.getRoutingKey(), not.getProperties(), not.getBody(), false);
  }
  
  public void publish(String exchange, String routingKey, BasicProperties properties, byte[] body,
      boolean redelivered) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "QueueAgent.publish(" + properties + ')');

    msgCounter++;
    if (consumers.size() > 0) {
      DeliverContext deliverContext = (DeliverContext) consumers.removeFirst();
      if (!deliverContext.isNoAck()) {
        toAck.addLast(new Message(exchange, routingKey, properties, body, msgCounter, redelivered));
      }
      Listener callback = deliverContext.getConsumer();
      long deliveryTag = deliverContext.proxy.getDeliveryTag(getId(), deliverContext.channelId, msgCounter,
          deliverContext.noAck);
      if (callback instanceof DeliveryListener) {
        DeliveryListener consumer = (DeliveryListener) callback;
        consumer.handleDelivery(deliveryTag, redelivered, exchange, routingKey, properties, body);
      } else if (callback instanceof GetListener) {
        GetListener consumer = (GetListener) callback;
        consumer.handleGet(deliveryTag, redelivered, exchange, routingKey, toDeliver.size(), properties, body);
      }
      if (deliverContext.isSubscription) {
        consumers.addLast(deliverContext);
      }
    } else {
      toDeliver.addLast(new Message(exchange, routingKey, properties, body, msgCounter, redelivered));
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
    if (consumers.size() == 0 && autodelete) {
      delete();
    }
  }

  private void doReact(DeleteNot not, AgentId from) throws Exception {
    boolean hasToBeDeleted = true;
    if (not.isIfEmpty() && toDeliver.size() > 0) {
      hasToBeDeleted = false;
    }
    if (not.isIfUnused() && consumers.size() > 0) {
      hasToBeDeleted = false;
    }
    if (hasToBeDeleted) {
      NamingAgent.getSingleton().unbind(name);
      delete(from);
    } else {
      sendTo(from, new DeleteAck(getId()));
    }
  }
  
  private void doReact(ClearQueueNot not) {
    toDeliver.clear();
  }

  private void doReact(AckNot not) {
    ackMessages(not.getIdsToAck());
  }

  public void ackMessages(List idsToAck) {
    // Both lists must be sorted
    Iterator iterIds = idsToAck.iterator();
    Iterator iterMsgs = toAck.iterator();
    while (iterIds.hasNext()) {
      long id = ((Long) iterIds.next()).longValue();
      while (iterMsgs.hasNext()) {
        Message msg = (Message) iterMsgs.next();
        if (msg.deliveryTag == id) {
          iterMsgs.remove();
          break;
        }
      }
    }
  }
  
  private void doReact(RecoverNot not) {
    recoverMessages(not.getIdsToRecover());
  }

  public void recoverMessages(List idsToRecover) {
    // Both lists must be sorted
    Iterator iterIds = idsToRecover.iterator();
    Iterator iterMsgs = toAck.iterator();
    while (iterIds.hasNext()) {     
      long id = ((Long) iterIds.next()).longValue();
      while (iterMsgs.hasNext()) {
        Message msg = (Message) iterMsgs.next();
        if (msg.deliveryTag == id) {
          publish(msg.exchange, msg.routingKey, msg.properties, msg.body, true);
          break;
        }
      }
    }
  }

  static class Message implements Serializable {

    private String exchange;
    private String routingKey;
    private BasicProperties properties;
    private byte[] body;
    private long deliveryTag;
    private boolean redelivered;

    /**
     * @param properties
     * @param body
     */
    public Message(String exchange, String routingKey, BasicProperties properties, byte[] body,
        long deliveryTag, boolean redelivered) {
      this.exchange = exchange;
      this.routingKey = routingKey;
      this.properties = properties;
      this.body = body;
      this.deliveryTag = deliveryTag;
      this.redelivered = redelivered;
    }
  }
  
  
  static class DeliverContext implements Serializable {

    private Listener consumer;
    private boolean isSubscription;
    private String consumerTag;
    private boolean noAck;
    private ProxyAgent proxy;
    private int channelId;
    
    public DeliverContext(int channelId, ProxyAgent proxy, GetListener consumer, boolean noAck) {
      super();
      this.consumer = consumer;
      this.isSubscription = false;
      this.noAck = noAck;
      this.channelId = channelId;
      this.proxy = proxy;
    }

    public DeliverContext(int channelId, ProxyAgent proxy, DeliveryListener consumer, String consumerTag,
        boolean noAck) {
      super();
      this.consumer = consumer;
      this.consumerTag = consumerTag;
      this.isSubscription = true;
      this.noAck = noAck;
      this.channelId = channelId;
      this.proxy = proxy;
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

    public boolean isNoAck() {
      return noAck;
    }
    
  }

}
