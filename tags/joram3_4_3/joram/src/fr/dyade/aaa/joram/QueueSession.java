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

import javax.jms.IllegalStateException;
import javax.jms.JMSException;

/**
 * Implements the <code>javax.jms.QueueSession</code> interface.
 */
public class QueueSession extends Session implements javax.jms.QueueSession
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
  public javax.jms.QueueSender createSender(javax.jms.Queue queue)
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
  public javax.jms.QueueReceiver
         createReceiver(javax.jms.Queue queue, String selector)
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
  public javax.jms.QueueReceiver createReceiver(javax.jms.Queue queue)
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
  public javax.jms.TopicSubscriber
         createDurableSubscriber(javax.jms.Topic topic, String name,
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
  public javax.jms.TopicSubscriber
         createDurableSubscriber(javax.jms.Topic topic, String name)
         throws JMSException
  {
    throw new IllegalStateException("Forbidden call on a QueueSession.");
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  Systematically.
   */
  public javax.jms.Topic createTopic(String topicName) throws JMSException
  {
    throw new IllegalStateException("Forbidden call on a QueueSession.");
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  Systematically.
   */
  public javax.jms.TemporaryTopic createTemporaryTopic() throws JMSException
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
