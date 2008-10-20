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

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import com.rabbitmq.client.AMQP.BasicProperties;

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.util.Daemon;
import fr.dyade.aaa.util.Queue;

/**
 * WARNING: the current implementation is limited
 * to:
 * - local and exclusive usage
 * - implicit delivery
 */
public class QueueAgent extends Agent {
  
  public final static Logger logger = 
    fr.dyade.aaa.util.Debug.getLogger(QueueAgent.class.getName());
  
  private Queue toDeliver;
  
  private LinkedList consumers;
  
  private MessageDispatcher messageDispatcher;
  
  public QueueAgent() {
    this.toDeliver = new Queue();
    this.consumers = new LinkedList();
    this.messageDispatcher = new MessageDispatcher(this.getClass().getName() + ".MessageDispatcher");
    this.messageDispatcher.start();
  }
  
  public void react(AgentId from, Notification not) throws Exception {
    if (not instanceof ConsumeNot) {
      doReact((ConsumeNot) not);
    } else if (not instanceof PublishNot) {
      doReact((PublishNot) not);
    } else if (not instanceof ReceiveNot) {
      doReact((ReceiveNot) not);
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
    consumers.addLast(new DeliverContext(consumer, false));
  }

  private void doReact(ConsumeNot not) {
    consume(not.getCallback());
  }

  public void consume(DeliveryListener consumer) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "QueueAgent.consume()");
    consumers.addLast(new DeliverContext(consumer, true));
  }
  
  private void doReact(PublishNot not) {
    publish(not.getProperties(), not.getBody());
  }
  
  public void publish(BasicProperties properties, byte[] body) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "QueueAgent.publish(" + properties + ')');
    toDeliver.push(new Message(properties, body));
  }
  
  static class Message implements Serializable {
    
    private BasicProperties properties;
    private byte[] body;
    
    /**
     * @param properties
     * @param body
     */
    public Message(BasicProperties properties, byte[] body) {
      super();
      this.properties = properties;
      this.body = body;
    }
    
    public byte[] getBody() {
      return body;
    }
    
    public BasicProperties getProperties() {
      return properties;
    }
  }
  
  
  static class DeliverContext {

    private Listener consumer;
    private boolean isSubscription;

    public DeliverContext(Listener consumer, boolean isSubscription) {
      super();
      this.consumer = consumer;
      this.isSubscription = isSubscription;
    }

    public Listener getConsumer() {
      return consumer;
    }

    public boolean isSubscription() {
      return isSubscription;
    }
    
  }
  
  final class MessageDispatcher extends Daemon {

    MessageDispatcher(String name) {
      super(name + ".MessageDispatcher");
    }

    protected void close() {
    }

    protected void shutdown() {
    }

    public void run() {

      try {
        while (running) {
          canStop = true;
          Message msg = null;
          try {
            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG, this.getName() + ", waiting message");
            msg = (Message) toDeliver.getAndPop();
          } catch (InterruptedException exc) {
            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG, this.getName() + ", interrupted");
            continue;
          }
          canStop = false;
          if (!running)
            break;

          DeliverContext deliverContext = (DeliverContext) consumers.removeFirst();
          Listener callback = deliverContext.getConsumer();
          if (callback instanceof DeliveryListener) {
            DeliveryListener consumer = (DeliveryListener) callback;
            consumer.handleDelivery("?", 0, false, "?", "?", msg.properties, msg.body);
          } else if (callback instanceof GetListener) {
            GetListener consumer = (GetListener) callback;
            consumer.handleGet(0, false, "?", "?", toDeliver.size(), msg.properties, msg.body);
          }
          if (deliverContext.isSubscription) {
            consumers.addLast(deliverContext);
          }
        }
        
      } catch (Exception exc) {
        this.logmon.log(BasicLevel.FATAL, this.getName() + ", unrecoverable exception", exc);
      } finally {
        finish();
      }
    }
  }
  
  

}
