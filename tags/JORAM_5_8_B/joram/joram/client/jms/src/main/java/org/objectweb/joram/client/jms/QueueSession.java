/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
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
 * Contributor(s):
 */
package org.objectweb.joram.client.jms;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;

import org.objectweb.joram.client.jms.connection.RequestMultiplexer;

/**
 * Implements the <code>javax.jms.QueueSession</code> interface.
 */
public class QueueSession extends Session implements javax.jms.QueueSession {
  /**
   * Constructs a queue session.
   *
   * @param cnx  The connection the session belongs to.
   * @param transacted  <code>true</code> for a transacted session.
   * @param acknowledgeMode  1 (auto), 2 (client) or 3 (dups ok).
   *
   * @exception JMSException  In case of an invalid acknowledge mode.
   */
  QueueSession(Connection cnx, 
               boolean transacted,
               int acknowledgeMode,
               RequestMultiplexer mtpx) throws JMSException {
    super(cnx, transacted, acknowledgeMode, mtpx);
  }

  /** Returns a String image of this session. */
  public String toString() {
    return "QueueSess:" + getId();
  }


  /**
   * API method.
   * Creates a QueueSender object to send messages to the specified queue.
   * 
   * @param queue the queue to send to, or null if this is a sender which does
   *              not have a specified destination.
   * @return the created QueueSender object.
   *
   * @exception IllegalStateException  If the session is closed or if the 
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public synchronized javax.jms.QueueSender createSender(javax.jms.Queue queue) throws JMSException {
    checkClosed();
    QueueSender qc = new QueueSender(this, (Destination) queue);
    addProducer(qc);
    return qc;
  }

  /**
   * API method.
   * Creates a QueueReceiver object to receive messages from the specified queue using a
   * message selector.
   * 
   * @param queue     the queue to access.
   * @param selector  The selector allowing to filter messages.
   * @return the created QueueReceiver object.
   *
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public javax.jms.QueueReceiver createReceiver(javax.jms.Queue queue, String selector) throws JMSException {
    checkClosed();
    QueueReceiver qr = new QueueReceiver(this, (Destination) queue, selector);
    addConsumer(qr);
    return qr;
  }

  /**
   * API method.
   * Creates a QueueReceiver object to receive messages from the specified queue.
   * 
   * @param queue     the queue to access.
   * @return the created QueueReceiver object.
   *
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public javax.jms.QueueReceiver createReceiver(javax.jms.Queue queue) throws JMSException {
    checkClosed(); 
    QueueReceiver qr = new QueueReceiver(this, (Destination) queue, null);
    addConsumer(qr);
    return qr;
  }

  /** 
   * API method.
   *
   * @exception IllegalStateException  Systematically.
   */
  public javax.jms.TopicSubscriber
         createDurableSubscriber(javax.jms.Topic topic, String name,
                                 String selector,
                                 boolean noLocal) throws JMSException {
    throw new IllegalStateException("Forbidden call on a QueueSession.");
  }

  /** 
   * API method.
   *
   * @exception IllegalStateException  Systematically.
   */
  public javax.jms.TopicSubscriber
         createDurableSubscriber(javax.jms.Topic topic, String name) throws JMSException{
    throw new IllegalStateException("Forbidden call on a QueueSession.");
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  Systematically.
   */
  public javax.jms.Topic createTopic(String topicName) throws JMSException {
    throw new IllegalStateException("Forbidden call on a QueueSession.");
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  Systematically.
   */
  public javax.jms.TemporaryTopic createTemporaryTopic() throws JMSException {
    throw new IllegalStateException("Forbidden call on a QueueSession.");
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  Systematically.
   */
  public void unsubscribe(String name) throws JMSException {
    throw new IllegalStateException("Forbidden call on a QueueSession.");
  }
}
