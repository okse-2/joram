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

import javax.jms.JMSException;
import javax.jms.IllegalStateException;


/**
 * Implements the <code>javax.jms.QueueSender</code> interface.
 */
public class QueueSender extends MessageProducer implements javax.jms.QueueSender {
  /**
   * Constructs a sender.
   *
   * @param sess  The session the sender belongs to.
   * @param queue The queue the sender sends messages to.
   *
   * @exception IllegalStateException  If the connection is broken.
   * @exception JMSException  If the creation fails for any other reason. 
   */
  QueueSender(QueueSession sess, Destination queue) throws JMSException {
    super(sess, queue);
  }

  /** Returns a string view of this receiver. */
  public String toString() {
    return "QueueSend:" + sess.getId();
  }

  /** 
   * API method.
   * Gets the queue associated with this queue sender.
   * 
   * @return this sender's Queue.
   *
   * @exception IllegalStateException  If the sender is closed.
   */
  public javax.jms.Queue getQueue() throws JMSException {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed sender.");

    return (javax.jms.Queue) super.dest;
  }

  /**
   * API method.
   * Sends a message to a queue for an unidentified queue sender with default delivery parameters.
   * <p>
   * Typically, a queue sender is assigned a queue at creation time; however, the JMS API also
   * supports unidentified queue sender, which require that the queue be supplied every time a
   * message is sent.
   * 
   * @param queue         the queue to send this message to.
   * @param message       the message to send.

   *
   * @exception UnsupportedOperationException  When the sender did not
   *              properly identify itself.
   * @exception JMSSecurityException  If the user if not a WRITER on the
   *              specified queue.
   * @exception IllegalStateException  If the sender is closed, or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public void send(javax.jms.Queue queue, javax.jms.Message message) throws JMSException {
    super.send(queue, message);
  }

  /**
   * API method.
   * Sends a message to a queue for an unidentified queue sender with given delivery parameters.
   * <p>
   * Typically, a queue sender is assigned a queue at creation time; however, the JMS API also
   * supports unidentified queue sender, which require that the queue be supplied every time a
   * message is sent.
   * 
   * @param queue         the queue to send this message to.
   * @param message       the message to send.
   * @param deliveryMode  the delivery mode to use.
   * @param priority      the priority for this message.
   * @param timeToLive    the message's lifetime in milliseconds.
   *
   * @exception UnsupportedOperationException  When the sender did not
   *              properly identify itself.
   * @exception JMSSecurityException  If the user if not a WRITER on the
   *              specified queue.
   * @exception IllegalStateException  If the sender is closed, or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public void send(javax.jms.Queue queue,
                   javax.jms.Message message,
                   int deliveryMode,
                   int priority,
                   long timeToLive) throws JMSException {
    super.send(queue, message, deliveryMode, priority, timeToLive);
  }
}
