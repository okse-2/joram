/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 * The present code contributor is ScalAgent Distributed Technologies.
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
   * @exception JMSSecurityException  If the client is not a WRITER on the
   *              topic.
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
   * @exception JMSSecurityException  If the client is not a READER on the
   *              topic.
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
   * @exception JMSSecurityException  If the client is not a READER on the
   *              topic.
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
