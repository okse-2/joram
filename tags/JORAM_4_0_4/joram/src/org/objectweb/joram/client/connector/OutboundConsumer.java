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

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;


/**
 * An <code>OutboundConsumer</code> instance wraps a JMS consumer
 * for a component involved in outbound messaging. 
 */
public class OutboundConsumer implements javax.jms.MessageConsumer
{
  /** The <code>OutboundSession</code> this consumer belongs to. */
  protected OutboundSession session;
  /** Wrapped JMS consumer. */
  protected MessageConsumer consumer;

  /** <code>false</code> if consumer is no more valid. */
  boolean valid = true;
  

  /**
   * Constructs an <code>OutboundConsumer</code> instance.
   *
   * @param consumer  JMS consumer to wrap.
   * @param session   The OutboundSession this consumer belongs to.
   */
  OutboundConsumer(MessageConsumer consumer, OutboundSession session)
  {
    this.consumer = consumer;
    this.session = session;
  }


  /**
   * Forbidden call on a component's outbound consumer, throws a 
   * <code>IllegalStateException</code> instance.
   */
  public void setMessageListener(javax.jms.MessageListener messageListener)
              throws JMSException
  {
    checkValidity();
    throw new IllegalStateException("Invalid call on a component's producer.");
  }

  /**
   * Forbidden call on a component's outbound consumer, throws a 
   * <code>IllegalStateException</code> instance.
   */
  public javax.jms.MessageListener getMessageListener() throws JMSException
  {
    checkValidity();
    throw new IllegalStateException("Invalid call on a component's producer.");
  }

  /**
   * Delegates the call to the wrapped JMS consumer.
   */
  public String getMessageSelector() throws JMSException
  {
    checkValidity();
    return consumer.getMessageSelector();
  }

  /** 
   * Delegates the call to the wrapped JMS consumer.
   */
  public javax.jms.Message receive(long timeOut) throws JMSException
  {
    checkValidity();
    return consumer.receive(timeOut);
  }

  /** 
   * Delegates the call to the wrapped JMS consumer.
   */
  public javax.jms.Message receive() throws JMSException
  {
    checkValidity();
    return consumer.receive();
  }

  /** 
   * Delegates the call to the wrapped JMS consumer.
   */
  public javax.jms.Message receiveNoWait() throws JMSException
  {
    checkValidity();
    if (!session.isStarted())
      return null;
    return consumer.receiveNoWait();
  }

  /**
   * Delegates the call to the wrapped JMS consumer.
   */
  public void close() throws JMSException
  {
    valid = false;
    consumer.close();
  }

  /** Checks the validity of the subscriber instance. */
  protected void checkValidity() throws IllegalStateException
  {
    session.checkValidity();

    if (! valid)
     throw new IllegalStateException("Invalid call on a closed producer.");
  }
}
