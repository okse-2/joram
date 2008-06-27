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
import javax.jms.Session;
import javax.jms.TopicSubscriber;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * An <code>OutboundSession</code> instance wraps a JMS session (XA or not)
 * for a component involved in outbound messaging.
 */
public class OutboundSession implements javax.jms.Session
{
  /** The <code>OutboundConnection</code> the session belongs to. */
  protected OutboundConnection cnx;
  /** The wrapped JMS session. */
  Session sess;

  /** <code>true</code> if this "handle" is valid. */
  boolean valid = true;

  /** <code>true</code> if the session is started. */
  boolean started = false;

  protected boolean transacted;

  /**
   * Constructs an <code>OutboundSession</code> instance.
   */
  OutboundSession(Session sess, OutboundConnection cnx) {
    this.sess = sess;
    this.cnx = cnx;
    cnx.sessions.add(this);

    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                    "OutboundSession(" + sess + 
                                    ", " + cnx + ")" + 
                                    "  cnx.sessions = " +  cnx.sessions);
  }
 
  /**
   * Constructs an <code>OutboundSession</code> instance.
   */
  OutboundSession(Session sess, 
                  OutboundConnection cnx,
                  boolean transacted) {
    this.sess = sess;
    this.cnx = cnx;
    this.transacted = transacted;
    cnx.sessions.add(this);

    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                    "OutboundSession(" + sess + 
                                    ", " + cnx + ")" + 
                                    "  cnx.sessions = " +  cnx.sessions);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public int getAcknowledgeMode() throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                    this + " getAcknowledgeMode() = " + sess.getAcknowledgeMode());
    
    checkValidity();
    if (transacted)
      return Session.SESSION_TRANSACTED;
    return sess.getAcknowledgeMode();
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public boolean getTransacted() throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG,
                                    this + " getTransacted() = " + sess.getTransacted());

    checkValidity();
    return sess.getTransacted();
  }

  /**
   * Forbidden call on a component's outbound session, throws a 
   * <code>IllegalStateException</code> instance.
   */
  public void setMessageListener(javax.jms.MessageListener messageListener)
              throws JMSException
  {
    checkValidity();
    throw new IllegalStateException("Forbidden call on a component's session.");
  }

  /**
   * Forbidden call on a component's outbound session, throws a 
   * <code>IllegalStateException</code> instance.
   */
  public javax.jms.MessageListener getMessageListener() throws JMSException
  {
    checkValidity();
    throw new IllegalStateException("Forbidden call on a component's session.");
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.Message createMessage() throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG,
                                    this + " createMessage()");

    checkValidity();
    return sess.createMessage();
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.TextMessage createTextMessage() throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG,
                                    this + " createTextMessage()");

    checkValidity();
    return sess.createTextMessage();
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.TextMessage createTextMessage(String text)
    throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG,
                                    this + " createTextMessage(" + text + ")");

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
    throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, this + " createProducer(" + dest + ")");
    
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
    throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, this + " createConsumer(" + dest +
                                    ", " + selector +
                                    ", " + noLocal + ")");

    checkValidity();
    return new OutboundConsumer(sess.createConsumer(dest, selector, noLocal),
                                this);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.MessageConsumer
         createConsumer(javax.jms.Destination dest, String selector)
         throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, this + " createConsumer(" + dest +
                                    ", " + selector + ")");

    checkValidity();
    return new OutboundConsumer(sess.createConsumer(dest, selector), this);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.MessageConsumer createConsumer(javax.jms.Destination dest)
    throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, this + " createConsumer(" + dest + ")");

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
    throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                    this + " createDurableSubscriber(" + topic +
                                    ", " + name +
                                    ", " + selector +
                                    ", " + noLocal + ")");

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
    throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                    this + " createDurableSubscriber(" + topic +
                                    ", " + name + ")");

    checkValidity();

    TopicSubscriber sub = sess.createDurableSubscriber(topic, name);
    return new OutboundSubscriber(topic, false, sub, this);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.Queue createQueue(String queueName) throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                    this + " createQueue(" + queueName + ")");

    checkValidity();
    return sess.createQueue(queueName);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.Topic createTopic(String topicName) throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                    this + " createTopic(" + topicName + ")");

    checkValidity();
    return sess.createTopic(topicName);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.TemporaryQueue createTemporaryQueue() throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                    this + " createTemporaryQueue()");

    checkValidity();
    return sess.createTemporaryQueue();
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.TemporaryTopic createTemporaryTopic() throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                    this + " createTemporaryTopic()");

    checkValidity();
    return sess.createTemporaryTopic();
  }

  /** Method never used by a component, does nothing. */
  public void run()
  {}

  /**
   * Forbidden call on a component's outbound session, throws a 
   * <code>IllegalStateException</code> instance.
   */
  public void commit() throws JMSException
  {
    checkValidity();
    throw new IllegalStateException("Forbidden call on a component's session.");
  }

  /**
   * Forbidden call on a component's outbound session, throws a 
   * <code>IllegalStateException</code> instance.
   */
  public void rollback() throws JMSException
  {
    checkValidity();
    throw new IllegalStateException("Forbidden call on a component's session.");
  }

  /** 
   * Delegates the call to the wrapped JMS session.
   */
  public void recover() throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                    this + " recover()");

    checkValidity();
    sess.recover();
  }


  /**
   * Delegates the call to the wrapped JMS session.
   */
  public void unsubscribe(String name) throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                    this + " unsubscribe(" + name + ")");

    checkValidity();
    sess.unsubscribe(name);
  }

  /** 
   * set started = true 
   */
  void start() {
   if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                    this + " start() started = true");

    started = true;
  }

  /** 
   * Actually does nothing, closing of the session occurs while closing
   * the component's connection.
   */
  public void close() throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                    this + " close()");

    valid = false;
    cnx.sessions.remove(this);
    started = false;
  }

  /**
   * return started value.
   */
  public boolean isStarted() {
    return started;
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
