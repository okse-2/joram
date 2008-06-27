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
 * Contributor(s): Nicolas Tachker (ScalAgent)
 */
package com.scalagent.kjoram;

import com.scalagent.kjoram.excepts.IllegalStateException;
import com.scalagent.kjoram.excepts.JMSException;


public class QueueReceiver extends MessageConsumer
{
  /**
   * Constructs a receiver.
   *
   * @param sess  The session the receiver belongs to.
   * @param queue  The queue the receiver consumes on.
   * @param selector  The selector for filtering messages.
   *
   * @exception IllegalStateException  If the connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  QueueReceiver(QueueSession sess, Queue queue,
                String selector) throws JMSException
  {
    super(sess, queue, selector);
  }

  /** Returns a string view of this receiver. */
  public String toString()
  {
    return "QueueRec:" + sess.ident;
  }

  /** 
   * API method.
   *
   * @exception IllegalStateException  If the receiver is closed.
   */
  public Queue getQueue() throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed receiver.");

    return (Queue) dest;
  }
}
