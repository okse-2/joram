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
 * Implements the <code>javax.jms.TopicSubscriber</code> interface.
 */
public class TopicSubscriber extends MessageConsumer
                             implements javax.jms.TopicSubscriber
{
  /**
   * Constructs a subscriber.
   *
   * @param sess  The session the subscriber belongs to.
   * @param topic  The topic the subscriber subscribes to.
   * @param name  The subscription name, for durable subs only.
   * @param selector  The selector for filtering messages.
   * @param noLocal <code>true</code> if the subscriber does not wish to
   *          consume messages published through the same connection.
   *
   * @exception IllegalStateException  If the connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  TopicSubscriber(Session sess, Topic topic, String name, String selector,
                  boolean noLocal) throws JMSException
  {
    super(sess, topic, selector, name, noLocal);
  }

   /** Returns a string view of this receiver. */
  public String toString()
  {
    return "TopicSub:" + targetName;
  }


  /** 
   * API method.
   *
   * @exception IllegalStateException  If the subscriber is closed.
   */
  public boolean getNoLocal() throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed"
                                      + " subscriber.");
    return noLocal;
  }

  /** 
   * API method.
   *
   * @exception IllegalStateException  If the subscriber is closed.
   */
  public javax.jms.Topic getTopic() throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed"
                                      + " subscriber.");
    return (javax.jms.Topic) dest;
  }
}
