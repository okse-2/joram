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

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import javax.jms.Session;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * An <code>OutboundTopicSession</code> instance wraps a JMS TopicSession
 * (XA or not) for a component involved in PubSub outbound messaging.
 */
public class OutboundTopicSession extends OutboundSession
                                  implements javax.jms.TopicSession
{
  /**
   * Constructs an <code>OutboundTopicSession</code> instance.
   */
  OutboundTopicSession(Session sess, OutboundConnection cnx) {
    super(sess, cnx);

    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG,
                                    "OutboundTopicSession(" + sess +
                                    ", " + cnx + ")");
  }

  /**
   * Constructs an <code>OutboundTopicSession</code> instance.
   */
  OutboundTopicSession(Session sess, 
                       OutboundConnection cnx, 
                       boolean transacted) {
    super(sess, cnx, transacted);

    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG,
                                    "OutboundTopicSession(" + sess +
                                    ", " + cnx + ")");
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.TopicPublisher createPublisher(Topic topic)
    throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG,
                                    this + " createPublisher(" + topic + ")");

    checkValidity();
    return new OutboundPublisher(sess.createProducer(topic), this);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public TopicSubscriber createSubscriber(Topic topic,
                                          String selector,
                                          boolean noLocal)
    throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG,
                                    this + " createSubscriber(" + topic + 
                                    ", " + selector + 
                                    ", " + noLocal + ")");

    checkValidity();
    MessageConsumer cons = sess.createConsumer(topic, selector, noLocal);
    return new OutboundSubscriber(topic, noLocal, cons, this);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public TopicSubscriber createSubscriber(Topic topic, String selector)
    throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG,
                                    this + " createSubscriber(" + topic + 
                                    ", " + selector + ")");

    checkValidity();
    MessageConsumer cons = sess.createConsumer(topic, selector);
    return new OutboundSubscriber(topic, false, cons, this);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public TopicSubscriber createSubscriber(Topic topic) 
    throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG,
                                    this + " createSubscriber(" + topic + ")");

    checkValidity();
    return new OutboundSubscriber(topic,
                                  false,
                                  sess.createConsumer(topic),
                                  this);
  }

  /**
   * API method.
   *
   * @exception javax.jms.IllegalStateException  Systematically.
   */
  public javax.jms.QueueBrowser
      createBrowser(javax.jms.Queue queue, 
                    String selector)
    throws JMSException {
    throw new javax.jms.IllegalStateException("Forbidden call on a TopicSession.");
  }
  
  /**
   * API method.
   *
   * @exception javax.jms.IllegalStateException  Systematically.
   */
  public javax.jms.QueueBrowser createBrowser(javax.jms.Queue queue)
    throws JMSException {
    throw new javax.jms.IllegalStateException("Forbidden call on a TopicSession.");
  }
  
  /**
   * API method.
   *
   * @exception javax.jms.IllegalStateException  Systematically.
   */
  public javax.jms.Queue createQueue(String queueName) 
    throws JMSException {
    throw new javax.jms.IllegalStateException("Forbidden call on a TopicSession.");
  }
  
  /**
   * API method.
   *
   * @exception javax.jms.IllegalStateException  Systematically.
   */
  public javax.jms.TemporaryQueue createTemporaryQueue() 
    throws JMSException {
    throw new javax.jms.IllegalStateException("Forbidden call on a TopicSession.");
  }
}
