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
package fr.dyade.aaa.joram;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;


/**
 * Implements the <code>javax.jms.TopicSession</code> interface.
 */
public class TopicSession extends Session implements javax.jms.TopicSession
{
  /**
   * Constructs a topic session.
   *
   * @param cnx  The connection the session belongs to.
   * @param transacted  <code>true</code> for a transacted session.
   * @param acknowledgeMode  1 (auto), 2 (client) or 3 (dups ok).
   *
   * @exception JMSException  In case of an invalid acknowledge mode.
   */
  TopicSession(Connection cnx, boolean transacted,
               int acknowledgeMode) throws JMSException
  {
    super(cnx, transacted, acknowledgeMode);
  }


  /** Returns a String image of this session. */
  public String toString()
  {
    return "TopicSess:" + ident;
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed or if the 
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public javax.jms.TopicPublisher
         createPublisher(javax.jms.Topic topic) throws JMSException
  {
    return new TopicPublisher(this, (Topic) topic);
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed or if the 
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public javax.jms.TopicSubscriber
         createSubscriber(javax.jms.Topic topic, String selector,
                          boolean noLocal) throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");

    return new TopicSubscriber(this, (Topic) topic, null, selector, noLocal);
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed or if the 
   *              connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  public javax.jms.TopicSubscriber
         createSubscriber(javax.jms.Topic topic) throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");

    return new TopicSubscriber(this, (Topic) topic, null, null, false);
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  Systematically.
   */
  public javax.jms.QueueBrowser
         createBrowser(javax.jms.Queue queue, String selector)
         throws JMSException
  {
    throw new IllegalStateException("Forbidden call on a TopicSession.");
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  Systematically.
   */
  public javax.jms.QueueBrowser createBrowser(javax.jms.Queue queue)
         throws JMSException
  {
    throw new IllegalStateException("Forbidden call on a TopicSession.");
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  Systematically.
   */
  public javax.jms.Queue createQueue(String queueName) throws JMSException
  {
    throw new IllegalStateException("Forbidden call on a TopicSession.");
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  Systematically.
   */
  public javax.jms.TemporaryQueue createTemporaryQueue() throws JMSException
  {
    throw new IllegalStateException("Forbidden call on a TopicSession.");
  }

}
