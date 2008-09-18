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
import javax.jms.Queue;
import javax.jms.Session;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * An <code>OutboundQueueSession</code> instance wraps a JMS QueueSession
 * (XA or not) for a component involved in PTP outbound messaging.
 */
public class OutboundQueueSession extends OutboundSession
                                  implements javax.jms.QueueSession
{
  /**
   * Constructs an <code>OutboundQueueSession</code> instance.
   */
  OutboundQueueSession(Session sess, OutboundConnection cnx) {
    super(sess, cnx);
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                    "OutboundQueueSession(" + sess + 
                                    ", " + cnx + ")");
  }

  /**
   * Constructs an <code>OutboundQueueSession</code> instance.
   */
  OutboundQueueSession(Session sess, 
                       OutboundConnection cnx,
                       boolean transacted) {
    super(sess, cnx, transacted);
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                    "OutboundQueueSession(" + sess + 
                                    ", " + cnx + ")");
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.QueueSender createSender(Queue queue)
    throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, this + " createSender(" + queue + ")");

    checkValidity();
    return new OutboundSender(sess.createProducer(queue), this);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.QueueReceiver createReceiver(Queue queue, String selector)
    throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                    this + " createReceiver(" + queue + 
                                    ", " + selector + ")");

    checkValidity();
    return new OutboundReceiver(queue,
                                sess.createConsumer(queue, selector),
                                this);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.QueueReceiver createReceiver(Queue queue)
    throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, this + " createReceiver(" + queue + ")");

    checkValidity();
    return new OutboundReceiver(queue, sess.createConsumer(queue), this);
  }

  /** 
   * API method.
   *
   * @exception javax.jms.IllegalStateException  Systematically.
   */
  public javax.jms.TopicSubscriber
      createDurableSubscriber(javax.jms.Topic topic, 
                              String name,
                              String selector,
                              boolean noLocal) 
    throws JMSException  {
    throw new javax.jms.IllegalStateException("Forbidden call on a OutboundQueueSession.");
  }

  /** 
   * API method.
   *
   * @exception javax.jms.IllegalStateException  Systematically.
   */
  public javax.jms.TopicSubscriber
         createDurableSubscriber(javax.jms.Topic topic, 
                                 String name)
         throws JMSException {
    throw new javax.jms.IllegalStateException("Forbidden call on a OutboundQueueSession.");
  }

  /**
   * API method.
   *
   * @exception javax.jms.IllegalStateException  Systematically.
   */
  public javax.jms.Topic createTopic(String topicName) 
    throws JMSException {
    throw new javax.jms.IllegalStateException("Forbidden call on a OutboundQueueSession.");
  }

  /**
   * API method.
   *
   * @exception javax.jms.IllegalStateException  Systematically.
   */
  public javax.jms.TemporaryTopic createTemporaryTopic() 
    throws JMSException {
    throw new javax.jms.IllegalStateException("Forbidden call on a OutboundQueueSession.");
  }

  /**
   * API method.
   *
   * @exception javax.jms.IllegalStateException  Systematically.
   */
  public void unsubscribe(String name) 
    throws JMSException {
    throw new javax.jms.IllegalStateException("Forbidden call on a OutboundQueueSession.");
  }    

  public String toString() {
    if (sess != null)
      return sess.toString();
    return null;
  }
}
