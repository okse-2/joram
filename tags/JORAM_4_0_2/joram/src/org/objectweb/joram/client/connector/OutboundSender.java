/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
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
 * Initial developer(s): Frederic Maistre (Bull SA)
 */
package org.objectweb.joram.client.connector;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;


/**
 * An <code>OutboundSender</code> instance wraps a JMS producer
 * for a component involved in PTP outbound messaging. 
 */
public class OutboundSender extends OutboundProducer
                            implements javax.jms.QueueSender
{
  /**
   * Constructs an <code>OutboundSender</code> instance.
   *
   * @param producer  The JMS producer to wrap.
   */
  OutboundSender(MessageProducer producer)
  {
    super(producer);
  }

 
  /** Delegates the call to the wrapped producer. */
  public Queue getQueue() throws JMSException
  {
    return (Queue) producer.getDestination();
  }

  /** Delegates the call to the wrapped producer. */
  public void send(Queue queue,
                   Message message,
                   int deliveryMode, 
                   int priority,
                   long timeToLive)
         throws JMSException
  {
    producer.send(queue, message, deliveryMode, priority, timeToLive);
  }

  /** Delegates the call to the wrapped producer. */
  public void send(Queue queue, Message message) throws JMSException
  {
    producer.send(queue, message);
  }
}
