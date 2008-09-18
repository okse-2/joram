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


public class QueueSession extends Session
{
  /**
   * Constructs a queue session.
   *
   * @param cnx  The connection the session belongs to.
   * @param transacted  <code>true</code> for a transacted session.
   * @param acknowledgeMode  1 (auto), 2 (client) or 3 (dups ok).
   *
   * @exception JMSException  In case of an invalid acknowledge mode.
   */
  QueueSession(Connection cnx, boolean transacted,
               int acknowledgeMode) throws JMSException
  {
    super(cnx, transacted, acknowledgeMode);
  }

  /** Returns a String image of this session. */
  public String toString()
  {
    return "QueueSess:" + ident;
  }


  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed or if the 
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public QueueSender createSender(Queue queue)
       throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");

    return new QueueSender(this, (Queue) queue);
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public QueueReceiver
         createReceiver(Queue queue, String selector)
         throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");

    return new QueueReceiver(this, (Queue) queue, selector);
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public QueueReceiver createReceiver(Queue queue)
         throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");

    return new QueueReceiver(this, (Queue) queue, null);
  }

  /** 
   * API method.
   *
   * @exception IllegalStateException  Systematically.
   */
  public TopicSubscriber
         createDurableSubscriber(Topic topic, String name,
                                 String selector,
                                 boolean noLocal) throws JMSException
  {
    throw new IllegalStateException("Forbidden call on a QueueSession.");
  }

  /** 
   * API method.
   *
   * @exception IllegalStateException  Systematically.
   */
  public TopicSubscriber
         createDurableSubscriber(Topic topic, String name)
         throws JMSException
  {
    throw new IllegalStateException("Forbidden call on a QueueSession.");
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  Systematically.
   */
  public Topic createTopic(String topicName) throws JMSException
  {
    throw new IllegalStateException("Forbidden call on a QueueSession.");
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  Systematically.
   */
  public TemporaryTopic createTemporaryTopic() throws JMSException
  {
    throw new IllegalStateException("Forbidden call on a QueueSession.");
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  Systematically.
   */
  public void unsubscribe(String name) throws JMSException
  {
    throw new IllegalStateException("Forbidden call on a QueueSession.");
  }
}
