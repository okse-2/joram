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
 * Contributor(s):
 */
package org.objectweb.joram.client.connector;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TopicSubscriber;


/**
 * An <code>OutboundSession</code> instance wraps a JMS session (XA or not)
 * for a component involved in outbound messaging.
 */
public class OutboundSession implements javax.jms.Session
{
  /** The <code>OutboundConnection</code> the session belongs to. */
  protected OutboundConnection cnx;
  /** The wrapped JMS session. */
  protected Session sess;

  /** <code>true</code> if this "handle" is valid. */
  boolean valid = true;



  /**
   * Constructs an <code>OutboundSession</code> instance.
   */
  OutboundSession(Session sess, OutboundConnection cnx)
  {
    this.sess = sess;
    this.cnx = cnx;
  }
 

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public int getAcknowledgeMode() throws JMSException
  {
    checkValidity();
    return sess.getAcknowledgeMode();
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public boolean getTransacted() throws JMSException
  {
    checkValidity();
    return sess.getTransacted();
  }

  /**
   * Forbidden call on a component's outbound session, throws a 
   * <code>JMSException</code> instance.
   */
  public void setMessageListener(javax.jms.MessageListener messageListener)
              throws JMSException
  {
    checkValidity();
    throw new JMSException("Forbidden call on a component's session.");
  }

  /**
   * Forbidden call on a component's outbound session, throws a 
   * <code>JMSException</code> instance.
   */
  public javax.jms.MessageListener getMessageListener() throws JMSException
  {
    checkValidity();
    throw new JMSException("Forbidden call on a component's session.");
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.Message createMessage() throws JMSException
  {
    checkValidity();
    return sess.createMessage();
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.TextMessage createTextMessage() throws JMSException
  {
    checkValidity();
    return sess.createTextMessage();
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.TextMessage createTextMessage(String text)
         throws JMSException
  {
    checkValidity();
    return sess.createTextMessage(text);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.BytesMessage createBytesMessage() throws JMSException
  {
    checkValidity();
    return sess.createBytesMessage();
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.MapMessage createMapMessage() throws JMSException
  {
    checkValidity();
    return sess.createMapMessage();
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.ObjectMessage createObjectMessage() throws JMSException
  {
    checkValidity();
    return sess.createObjectMessage();
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.ObjectMessage createObjectMessage(java.io.Serializable obj)
         throws JMSException
  {
    checkValidity();
    return sess.createObjectMessage(obj);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.StreamMessage createStreamMessage()
         throws JMSException
  {
    checkValidity();
    return sess.createStreamMessage();
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.QueueBrowser
         createBrowser(javax.jms.Queue queue, String selector)
         throws JMSException
  {
    checkValidity();
    return sess.createBrowser(queue, selector);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.QueueBrowser createBrowser(javax.jms.Queue queue)
         throws JMSException
  {
    checkValidity();
    return sess.createBrowser(queue);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.MessageProducer createProducer(javax.jms.Destination dest)
         throws JMSException
  {
    checkValidity();
    return new OutboundProducer(sess.createProducer(dest), this);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.MessageConsumer
         createConsumer(javax.jms.Destination dest,
                        String selector,
                        boolean noLocal)
         throws JMSException
  {
    checkValidity();
    return new OutboundConsumer(sess.createConsumer(dest, selector, noLocal),
                                this);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.MessageConsumer
         createConsumer(javax.jms.Destination dest, String selector)
         throws JMSException
  {
    checkValidity();
    return new OutboundConsumer(sess.createConsumer(dest, selector), this);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.MessageConsumer createConsumer(javax.jms.Destination dest)
         throws JMSException
  {
    checkValidity();
    return new OutboundConsumer(sess.createConsumer(dest), this);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.TopicSubscriber
         createDurableSubscriber(javax.jms.Topic topic,
                                 String name,
                                 String selector,
                                 boolean noLocal)
         throws JMSException
  {
    checkValidity();

    TopicSubscriber sub =
      sess.createDurableSubscriber(topic, name, selector, noLocal);

    return new OutboundSubscriber(topic, noLocal, sub, this);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.TopicSubscriber
         createDurableSubscriber(javax.jms.Topic topic, String name)
         throws JMSException
  {
    checkValidity();

    TopicSubscriber sub = sess.createDurableSubscriber(topic, name);
    return new OutboundSubscriber(topic, false, sub, this);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.Queue createQueue(String queueName) throws JMSException
  {
    checkValidity();
    return sess.createQueue(queueName);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.Topic createTopic(String topicName) throws JMSException
  {
    checkValidity();
    return sess.createTopic(topicName);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.TemporaryQueue createTemporaryQueue() throws JMSException
  {
    checkValidity();
    return sess.createTemporaryQueue();
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.TemporaryTopic createTemporaryTopic() throws JMSException
  {
    checkValidity();
    return sess.createTemporaryTopic();
  }

  /** Method never used by a component, does nothing. */
  public void run()
  {}

  /**
   * Forbidden call on a component's outbound session, throws a 
   * <code>JMSException</code> instance.
   */
  public void commit() throws JMSException
  {
    checkValidity();
    throw new JMSException("Forbidden call on a component's session.");
  }

  /**
   * Forbidden call on a component's outbound session, throws a 
   * <code>JMSException</code> instance.
   */
  public void rollback() throws JMSException
  {
    checkValidity();
    throw new JMSException("Forbidden call on a component's session.");
  }

  /** 
   * Forbidden call on a component's outbound session, throws a 
   * <code>JMSException</code> instance.
   */
  public void recover() throws JMSException
  {
    checkValidity();
    throw new JMSException("Forbidden call on a component's session.");
  }


  /**
   * Delegates the call to the wrapped JMS session.
   */
  public void unsubscribe(String name) throws JMSException
  {
    checkValidity();
    sess.unsubscribe(name);
  }

  /** 
   * Actually does nothing, closing of the session occurs while closing
   * the component's connection.
   */
  public void close() throws JMSException
  {
    valid = false;
  }


  /** Checks the validity of the session. */
  void checkValidity() throws IllegalStateException
  {
    boolean validity;

    if (! valid)
      validity = false;
    else
      validity = cnx.valid;

   if (! validity)
     throw new IllegalStateException("Invalid state: session is closed.");
  }
}
