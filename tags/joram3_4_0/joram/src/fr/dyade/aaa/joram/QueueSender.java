/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package fr.dyade.aaa.joram;

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
  QueueSender(QueueSession sess, Queue queue) throws JMSException
  {
    super(sess, queue);
  }

  /** Returns a string view of this receiver. */
  public String toString()
  {
    return "QueueSend:" + sess.ident;
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
