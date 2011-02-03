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
 * Implements the <code>javax.jms.TopicPublisher</code> interface.
 */
public class TopicPublisher extends MessageProducer
                            implements javax.jms.TopicPublisher
{
  /**
   * Constructs a publisher.
   *
   * @param sess  The session the publisher belongs to.
   * @param topic The topic the publisher publishs messages on.
   *
   * @exception JMSSecurityException  If the client is not a WRITER on the
   *              topic.
   * @exception IllegalStateException  If the connection is broken.
   * @exception JMSException  If the creation fails for any other reason. 
   */
  TopicPublisher(TopicSession sess, Topic topic) throws JMSException
  {
    super(sess, topic);
  }

  /** Returns a string view of this receiver. */
  public String toString()
  {
    return "TopicPub:" + sess.ident;
  }

  /** 
   * API method.
   *
   * @exception IllegalStateException  If the publisher is closed.
   */
  public javax.jms.Topic getTopic() throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed publisher.");

    return (Topic) super.dest;
  }

  /** 
   * API method.
   *
   * @exception IllegalStateException  If the publisher is closed, or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public void publish(javax.jms.Message message, int deliveryMode,
                      int priority, long timeToLive) throws JMSException
  {
    super.produce(message, deliveryMode, priority, timeToLive);
  }
    
  /** 
   * API method.
   *
   * @exception IllegalStateException  If the publisher is closed, or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public void publish(javax.jms.Message message) throws JMSException
  {
    super.produce(message);
  }

  /** 
   * API method.
   *
   * @exception IllegalStateException  If the publisher is closed, or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public void publish(javax.jms.Topic topic, javax.jms.Message message)
            throws JMSException
  {
    super.produce((Topic) topic, message);
  }

  /** 
   * API method.
   *
   * @exception IllegalStateException  If the publisher is closed, or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public void publish(javax.jms.Topic topic, javax.jms.Message message,
                      int deliveryMode, int priority,
                      long timeToLive) throws JMSException
  {
    super.produce((Topic) topic, message, deliveryMode, priority, timeToLive);
  }
}