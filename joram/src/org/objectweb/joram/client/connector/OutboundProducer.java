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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;


/**
 * An <code>OutboundProducer</code> instance wraps a JMS producer
 * for a component involved in outbound messaging. 
 */
public class OutboundProducer implements javax.jms.MessageProducer
{
  /** The wrapped JMS producer. */
  protected MessageProducer producer;
  

  /**
   * Constructs an <code>OutboundProducer</code> instance.
   *
   * @param producer  The JMS producer to wrap.
   */
  OutboundProducer(MessageProducer producer)
  {
    this.producer = producer;
  }


  /** Delegates the call to the wrapped producer. */
  public void setDisableMessageID(boolean value) throws JMSException
  {
    producer.setDisableMessageID(value);
  }

  /** Delegates the call to the wrapped producer. */
  public void setDeliveryMode(int deliveryMode) throws JMSException
  {
    producer.setDeliveryMode(deliveryMode);
  }

  /** Delegates the call to the wrapped producer. */
  public void setPriority(int priority) throws JMSException
  {
    producer.setPriority(priority);
  }

  /** Delegates the call to the wrapped producer. */
  public void setTimeToLive(long timeToLive) throws JMSException
  {
    producer.setTimeToLive(timeToLive);
  }

  /** Delegates the call to the wrapped producer. */
  public void setDisableMessageTimestamp(boolean value) throws JMSException
  {
    producer.setDisableMessageTimestamp(value);
  }

  /** Delegates the call to the wrapped producer. */
  public Destination getDestination() throws JMSException
  {
    return producer.getDestination();
  }

  /** Delegates the call to the wrapped producer. */
  public boolean getDisableMessageID() throws JMSException
  {
    return producer.getDisableMessageID();
  }

  /** Delegates the call to the wrapped producer. */
  public int getDeliveryMode() throws JMSException
  {
    return producer.getDeliveryMode();
  }

  /** Delegates the call to the wrapped producer. */
  public int getPriority() throws JMSException
  {
    return producer.getPriority();
  }

  /** Delegates the call to the wrapped producer. */
  public long getTimeToLive() throws JMSException
  {
    return producer.getTimeToLive();
  }

  /** Delegates the call to the wrapped producer. */
  public boolean getDisableMessageTimestamp() throws JMSException
  {
    return producer.getDisableMessageTimestamp();
  }


  /** Delegates the call to the wrapped producer. */
  public void send(Message message) throws JMSException
  {
    producer.send(message);
  }

  /** Delegates the call to the wrapped producer. */
  public void send(Message message,
                   int deliveryMode,
                   int priority,
                   long timeToLive)
              throws JMSException
  {
    producer.send(message, deliveryMode, priority, timeToLive);
  }

  /** Delegates the call to the wrapped producer. */
  public void send(Destination dest, Message message) throws JMSException
  {
    producer.send(dest, message);
  }

  /** Delegates the call to the wrapped producer. */
  public void send(Destination dest,
                   Message message,
                   int deliveryMode,
                   int priority,
                   long timeToLive)
              throws JMSException
  {
    producer.send(dest, message, deliveryMode, priority, timeToLive);
  }

  /** Delegates the call to the wrapped producer. */
  public void close() throws JMSException
  {
    producer.close();
  }
}
