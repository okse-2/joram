/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
 * Copyright (C) 2004 Bull SA
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

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.IllegalStateException;
import javax.jms.Destination;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.QueueBrowser;
import javax.jms.MessageProducer;
import javax.jms.MessageConsumer;
import javax.jms.TopicSubscriber;
import javax.jms.TemporaryTopic;
import javax.jms.TemporaryQueue;

/**
 * Implements the <code>javax.jms.XASession</code> interface.
 * <p>
 * An XA session actually extends the behaviour of a normal session by
 * providing an XA resource representing it to a Transaction Manager, so that
 * it is part of a distributed transaction. The XASession wraps what looks like
 * a "normal" Session object. This object takes care of producing and
 * consuming messages, the actual sendings and acknowledgement being managed
 * by this XA wrapper.
 * <p>
 * This class offers support to transactional environments. Client programs are
 * strongly encouraged to use the transactional support available in their environment,
 * rather than use these XA interfaces directly. 
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
                   XAResourceMngr rm) throws JMSException {
    this.sess = sess;
    xaResource = new XAResource(rm, sess);
  }

  public final Session getDelegateSession() {
    return sess;
  }

   /** Returns a String image of this session. */
  public String toString() {
    return "XASess:" + sess.getId();
  }
  
  /**
   * API method.
   * Gets the session associated with this XASession.
   * 
   * @return the session object.
   *
   * @exception IllegalStateException  If the session is closed.
   */
  public javax.jms.Session getSession() throws JMSException {
    return sess;
  }
 
  /**
   * API method.
   * Returns an XA resource to the caller.
   * 
   * @return an XA resource.
   */  
  public javax.transaction.xa.XAResource getXAResource() {
    return xaResource;
  }

  /**
   * API method.
   * Indicates whether the session is in transacted mode.
   *
   * @return true
   * 
   * @exception IllegalStateException  If the session is closed.
   */
  public boolean getTransacted() throws JMSException {
    return sess.getTransacted();
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public QueueBrowser createBrowser(Queue queue,
                                              String selector) throws JMSException {
    return sess.createBrowser(queue, selector);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public QueueBrowser createBrowser(Queue queue) throws JMSException {
    return sess.createBrowser(queue);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public MessageProducer createProducer(Destination dest) throws JMSException {
    return sess.createProducer(dest);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public MessageConsumer createConsumer(Destination dest,
                                        String selector,
                                        boolean noLocal) throws JMSException {
    return sess.createConsumer(dest, selector, noLocal);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public MessageConsumer createConsumer(Destination dest,
                                        String selector) throws JMSException {
    return sess.createConsumer(dest, selector);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public MessageConsumer createConsumer(Destination dest) throws JMSException {
    return sess.createConsumer(dest);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public TopicSubscriber createDurableSubscriber(Topic topic,
                                                 String name,
                                                 String selector,
                                                 boolean noLocal) throws JMSException {
    return sess.createDurableSubscriber(topic, name, selector, noLocal);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public TopicSubscriber createDurableSubscriber(Topic topic,
                                                 String name) throws JMSException {
    return sess.createDurableSubscriber(topic, name);
  }

  /**
   * API method inherited from session, but intercepted here for
   * forbidding its use in the XA context (as defined by the API).
   *
   * @exception IllegalStateException  Systematically thrown.
   */
  public void commit() throws JMSException {
    throw new IllegalStateException("Forbidden call on an XA session.");
  }

  /**
   * API method inherited from session, but intercepted here for
   * forbidding its use in the XA context (as defined by the API).
   *
   * @exception IllegalStateException  Systematically thrown.
   */
  public void rollback() throws JMSException {
    throw new IllegalStateException("Forbidden call on an XA session.");
  }

  /**
   * API method inherited from session, but intercepted here for
   * forbidding its use in the XA context (as defined by the API).
   *
   * @exception IllegalStateException  Systematically thrown.
   */
  public void recover() throws JMSException {
    throw new IllegalStateException("Forbidden call on an XA session.");
  }

  /**
   * API method. Closes the session.
   * 
   * @exception JMSException  Actually never thrown.
   * @see Session.close
   */
  public void close() throws JMSException {
    sess.close();
  }
  
  /**
   * Delegates the call to the wrapped JMS session.
   */
  public void run() {
    sess.run();
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public void unsubscribe(String name) throws JMSException {
    sess.unsubscribe(name);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public synchronized TemporaryQueue createTemporaryQueue() throws JMSException {
    return sess.createTemporaryQueue();
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public synchronized TemporaryTopic createTemporaryTopic() throws JMSException {
    return sess.createTemporaryTopic();
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public synchronized Topic createTopic(String topicName) throws JMSException {
    return sess.createTopic(topicName);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public Queue createQueue(String queueName) throws JMSException {
    return sess.createQueue(queueName);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public void setMessageListener(MessageListener messageListener)throws JMSException {
    sess.setMessageListener(messageListener);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public MessageListener getMessageListener() throws JMSException {
    return sess.getMessageListener();
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public int getAcknowledgeMode() throws JMSException {
    return sess.getAcknowledgeMode();
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public TextMessage createTextMessage() throws JMSException {
    return sess.createTextMessage();
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public TextMessage createTextMessage(String text) throws JMSException {
    return sess.createTextMessage(text);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public StreamMessage createStreamMessage() throws JMSException {
    return sess.createStreamMessage();
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public ObjectMessage createObjectMessage() throws JMSException {
    return sess.createObjectMessage();
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public ObjectMessage createObjectMessage(java.io.Serializable obj) throws JMSException {
    return sess.createObjectMessage(obj);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public Message createMessage() throws JMSException {
    return sess.createMessage();
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public MapMessage createMapMessage() throws JMSException {
    return sess.createMapMessage();
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public BytesMessage createBytesMessage() throws JMSException {
    return sess.createBytesMessage();
  }
}
