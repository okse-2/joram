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
 * Contributor(s): Nicolas Tachker (Bull SA)
 */
package org.objectweb.joram.client.connector;

import javax.jms.Destination;
import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * An <code>OutboundProducer</code> instance wraps a JMS producer
 * for a component involved in outbound messaging. 
 */
public class OutboundProducer implements javax.jms.MessageProducer
{
  /** The <code>OutboundSession</code> this producer belongs to. */
  protected OutboundSession session;
  /** The wrapped JMS producer. */
  protected MessageProducer producer;

  /** <code>false</code> if producer is no more valid. */
  boolean valid = true;
  

  /**
   * Constructs an <code>OutboundProducer</code> instance.
   *
   * @param producer  The JMS producer to wrap.
   * @param session   The OutboundSession this producer belongs to.
   */
  OutboundProducer(MessageProducer producer, OutboundSession session) {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                    "OutboundProducer(" + producer + 
                                    ", " + session + ")");

    this.producer = producer;
    this.session = session;
  }


  /** Delegates the call to the wrapped producer. */
  public void setDisableMessageID(boolean value) throws JMSException
  {
    checkValidity();
    producer.setDisableMessageID(value);
  }

  /** Delegates the call to the wrapped producer. */
  public void setDeliveryMode(int deliveryMode) throws JMSException
  {
    checkValidity();
    producer.setDeliveryMode(deliveryMode);
  }

  /** Delegates the call to the wrapped producer. */
  public void setPriority(int priority) throws JMSException
  {
    checkValidity();
    producer.setPriority(priority);
  }

  /** Delegates the call to the wrapped producer. */
  public void setTimeToLive(long timeToLive) throws JMSException
  {
    checkValidity();
    producer.setTimeToLive(timeToLive);
  }

  /** Delegates the call to the wrapped producer. */
  public void setDisableMessageTimestamp(boolean value) throws JMSException
  {
    checkValidity();
    producer.setDisableMessageTimestamp(value);
  }

  /** Delegates the call to the wrapped producer. */
  public Destination getDestination() throws JMSException
  {
    checkValidity();
    return producer.getDestination();
  }

  /** Delegates the call to the wrapped producer. */
  public boolean getDisableMessageID() throws JMSException
  {
    checkValidity();
    return producer.getDisableMessageID();
  }

  /** Delegates the call to the wrapped producer. */
  public int getDeliveryMode() throws JMSException
  {
    checkValidity();
    return producer.getDeliveryMode();
  }

  /** Delegates the call to the wrapped producer. */
  public int getPriority() throws JMSException
  {
    checkValidity();
    return producer.getPriority();
  }

  /** Delegates the call to the wrapped producer. */
  public long getTimeToLive() throws JMSException
  {
    checkValidity();
    return producer.getTimeToLive();
  }

  /** Delegates the call to the wrapped producer. */
  public boolean getDisableMessageTimestamp() throws JMSException
  {
    checkValidity();
    return producer.getDisableMessageTimestamp();
  }


  /** Delegates the call to the wrapped producer. */
  public void send(Message message) throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, this + " send(" + message + ")");

    checkValidity();
    producer.send(message);
  }

  /** Delegates the call to the wrapped producer. */
  public void send(Message message,
                   int deliveryMode,
                   int priority,
                   long timeToLive)
    throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, this + " send(" + message + 
                                    ", " + deliveryMode + 
                                    ", " + priority + 
                                    ", " + timeToLive + ")");

    checkValidity();
    producer.send(message, deliveryMode, priority, timeToLive);
  }

  /** Delegates the call to the wrapped producer. */
  public void send(Destination dest, Message message) 
    throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG,
                                    this + " send(" + dest + ", " + message + ")");

    checkValidity();
    producer.send(dest, message);
  }

  /** Delegates the call to the wrapped producer. */
  public void send(Destination dest,
                   Message message,
                   int deliveryMode,
                   int priority,
                   long timeToLive)
    throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, this + " send(" + dest + 
                                    ", " + message + 
                                    ", " + deliveryMode + 
                                    ", " + priority + 
                                    ", " + timeToLive + ")");

    checkValidity();
    producer.send(dest, message, deliveryMode, priority, timeToLive);
  }

  /** Delegates the call to the wrapped producer. */
  public void close() throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, this + " close()");

    valid = false;
    producer.close();
  }

  /** Checks the validity of the subscriber instance. */
  protected void checkValidity() throws IllegalStateException
  {
    session.checkValidity();

    if (! valid)
      throw new IllegalStateException("Invalid call on a closed producer.");
  }
}
