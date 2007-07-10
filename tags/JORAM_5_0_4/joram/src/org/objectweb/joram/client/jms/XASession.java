/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies
 * Copyright (C) 2004 - Bull SA
 * Copyright (C) 1996 - 2000 Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): ScalAgent Distributed Technologies
 *                 Nicolas Tachker (Bull SA)
 */
package org.objectweb.joram.client.jms;

import javax.jms.JMSException;
import javax.jms.IllegalStateException;
import javax.jms.TransactionInProgressException;

import org.objectweb.joram.shared.client.*;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * Implements the <code>javax.jms.XASession</code> interface.
 * <p>
 * An XA session actually extends the behaviour of a normal session by
 * providing an XA resource representing it to a Transaction Manager, so that
 * it is part of a distributed transaction. The XASession wraps what looks like
 * a "normal"Session object. This object takes care of producing and
 * consuming messages, the actual sendings and acknowledgement being managed
 * by this XA wrapper.
 */
public class XASession implements javax.jms.XASession {

  /** The XA resource representing the session to the transaction manager. */
  private javax.transaction.xa.XAResource xaResource;
  
  protected Session sess;

  /**
   * Constructs an <code>XASession</code>.
   * <p>
   * This constructor is called by subclasses.
   *
   * @param cnx   The connection the session belongs to.
   * @param sess  The wrapped "regular" session.
   * @param rm    The resource manager.
   *
   * @exception JMSException  Actually never thrown.
   */
  public XASession(Connection cnx, 
                   Session sess, 
                   XAResourceMngr rm)
    throws JMSException {
    this.sess = sess;
    xaResource = new XAResource(rm, sess);
  }

  public final Session getDelegateSession() {
    return sess;
  }

   /** Returns a String image of this session. */
  public String toString()
  {
    return "XASess:" + sess.getId();
  }
  
  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed.
   */
  public javax.jms.Session getSession() throws JMSException
  {
    return sess;
  }
 
  /** API method. */  
  public javax.transaction.xa.XAResource getXAResource()
  {
    return xaResource;
  }

  /**
   * API method. 
   *
   * @exception IllegalStateException  If the session is closed.
   */
  public boolean getTransacted() throws JMSException
  {
    return sess.getTransacted();
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.QueueBrowser
         createBrowser(javax.jms.Queue queue, String selector)
         throws JMSException
  {
    return sess.createBrowser(queue, selector);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.QueueBrowser createBrowser(javax.jms.Queue queue)
         throws JMSException
  {
    return sess.createBrowser(queue);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.MessageProducer createProducer(javax.jms.Destination dest)
         throws JMSException
  {
    return sess.createProducer(dest);
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
    return sess.createConsumer(dest, selector, noLocal);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.MessageConsumer
         createConsumer(javax.jms.Destination dest, String selector)
         throws JMSException
  {
    return sess.createConsumer(dest, selector);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.MessageConsumer createConsumer(javax.jms.Destination dest)
         throws JMSException
  {
    return sess.createConsumer(dest);
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
    return sess.createDurableSubscriber(topic, name, selector, noLocal);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.TopicSubscriber
         createDurableSubscriber(javax.jms.Topic topic, String name)
         throws JMSException
  {
    return sess.createDurableSubscriber(topic, name);
  }

  /**
   * API method inherited from session, but intercepted here for
   * forbidding its use in the XA context (as defined by the API).
   *
   * @exception IllegalStateException  Systematically thrown.
   */
  public void commit() throws JMSException
  {
    throw new IllegalStateException("Forbidden call on an XA session.");
  }

  /**
   * API method inherited from session, but intercepted here for
   * forbidding its use in the XA context (as defined by the API).
   *
   * @exception IllegalStateException  Systematically thrown.
   */
  public void rollback() throws JMSException
  {
    throw new IllegalStateException("Forbidden call on an XA session.");
  }

  /**
   * API method inherited from session, but intercepted here for
   * forbidding its use in the XA context (as defined by the API).
   *
   * @exception IllegalStateException  Systematically thrown.
   */
  public void recover() throws JMSException
  {
    throw new IllegalStateException("Forbidden call on an XA session.");
  }

  /**
   *
   * @exception JMSException  Actually never thrown.
   */
  public void close() throws JMSException {
    sess.close();
  }
  
  /** 
   *
   */
  public void run() {
    sess.run();
  }

  public void unsubscribe(String name)
    throws JMSException {
    sess.unsubscribe(name);
  }

  public synchronized javax.jms.TemporaryQueue createTemporaryQueue() 
    throws JMSException {
    return sess.createTemporaryQueue();
  }

  public synchronized javax.jms.TemporaryTopic createTemporaryTopic() 
    throws JMSException {
    return sess.createTemporaryTopic();
  }

  public synchronized javax.jms.Topic createTopic(
    String topicName) 
    throws JMSException {
    return sess.createTopic(topicName);
  }

  public javax.jms.Queue createQueue(String queueName) 
    throws JMSException {
    return sess.createQueue(queueName);
  }

  public void setMessageListener(
    javax.jms.MessageListener messageListener)
    throws JMSException {
    sess.setMessageListener(messageListener);
  }

  public javax.jms.MessageListener 
      getMessageListener() 
    throws JMSException {
    return sess.getMessageListener();
  }

  public int getAcknowledgeMode() 
    throws JMSException {
    return sess.getAcknowledgeMode();
  }

  public javax.jms.TextMessage createTextMessage() 
    throws JMSException {
    return sess.createTextMessage();
  }

  public javax.jms.TextMessage createTextMessage(String text)
    throws JMSException {
    return sess.createTextMessage(text);
  }

  public javax.jms.StreamMessage createStreamMessage()
    throws JMSException {
    return sess.createStreamMessage();
  }

  public javax.jms.ObjectMessage createObjectMessage()
    throws JMSException {
    return sess.createObjectMessage();
  }

  public javax.jms.ObjectMessage createObjectMessage(java.io.Serializable obj)
    throws JMSException {
    return sess.createObjectMessage(obj);
  }

  public javax.jms.Message createMessage() 
    throws JMSException {
    return sess.createMessage();
  }

  public javax.jms.MapMessage createMapMessage()
    throws JMSException {
    return sess.createMapMessage();
  }

  public javax.jms.BytesMessage createBytesMessage()
    throws JMSException {
    return sess.createBytesMessage();
  }
}
