/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
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
 * Initial developer(s): Frederic Maistre (Bull SA)
 * Contributor(s):
 */
package org.objectweb.joram.client.connector;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import javax.jms.Session;


/**
 * An <code>OutboundTopicSession</code> instance wraps a JMS TopicSession
 * (XA or not) for a component involved in PubSub outbound messaging.
 */
public class OutboundTopicSession extends OutboundSession
                                  implements javax.jms.TopicSession
{
  /**
   * Constructs an <code>OutboundTopicSession</code> instance.
   *
   * @param sess  The JMS session (XA or not) to wrap.
   */
  OutboundTopicSession(Session sess)
  {
    super(sess);
  }


  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.TopicPublisher createPublisher(Topic topic)
         throws JMSException
  {
    return new OutboundPublisher(sess.createProducer(topic));
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public TopicSubscriber createSubscriber(Topic topic,
                                          String selector,
                                          boolean noLocal)
         throws JMSException
  {
    MessageConsumer cons = sess.createConsumer(topic, selector, noLocal);
    return new OutboundSubscriber(topic, noLocal, cons);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public TopicSubscriber createSubscriber(Topic topic, String selector)
         throws JMSException
  {
    MessageConsumer cons = sess.createConsumer(topic, selector);
    return new OutboundSubscriber(topic, false, cons);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public TopicSubscriber createSubscriber(Topic topic) throws JMSException
  {
    return new OutboundSubscriber(topic, false, sess.createConsumer(topic));
  }
}
