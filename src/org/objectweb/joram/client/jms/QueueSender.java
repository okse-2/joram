/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
public class QueueSender extends MessageProducer
                         implements javax.jms.QueueSender
{
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
  public String toString()
  {
    return "QueueSend:" + sess.getId();
  }

  /** 
   * API method.
   *
   * @exception IllegalStateException  If the sender is closed.
   */
  public javax.jms.Queue getQueue() throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed sender.");

    return (javax.jms.Queue) super.dest;
  }

  /**
   * API method.
   *
   * @exception UnsupportedOperationException  When the sender did not
   *              properly identify itself.
   * @exception JMSSecurityException  If the user if not a WRITER on the
   *              specified queue.
   * @exception IllegalStateException  If the sender is closed, or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public void send(javax.jms.Queue queue, javax.jms.Message message)
            throws JMSException
  {
    super.send(queue, message);
  }

  /**
   * API method.
   *
   * @exception UnsupportedOperationException  When the sender did not
   *              properly identify itself.
   * @exception JMSSecurityException  If the user if not a WRITER on the
   *              specified queue.
   * @exception IllegalStateException  If the sender is closed, or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public void send(javax.jms.Queue queue, javax.jms.Message message,
                   int deliveryMode, int priority,
                   long timeToLive) throws JMSException
  {
    super.send(queue, message, deliveryMode, priority, timeToLive);
  }
}
