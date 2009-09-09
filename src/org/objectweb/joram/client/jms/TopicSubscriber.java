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

import javax.jms.IllegalStateException;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;


/**
 * Implements the <code>javax.jms.TopicSubscriber</code> interface.
 */
public class TopicSubscriber extends MessageConsumer implements javax.jms.TopicSubscriber {
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
   * @exception InvalidDestinationException if an invalid destination is specified.
   * @exception IllegalStateException  If the connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  TopicSubscriber(Session sess, Destination topic, String name, String selector, boolean noLocal) throws JMSException {
    super(sess, topic, selector, name, noLocal);
  }

   /** Returns a string view of this receiver. */
  public String toString() {
    return "TopicSub:" + targetName;
  }

  /** 
   * API method.
   *
   * @exception IllegalStateException  If the subscriber is closed.
   */
  public boolean getNoLocal() throws JMSException {
    checkClosed();
    return noLocal;
  }

  /** 
   * API method.
   *
   * @exception IllegalStateException  If the subscriber is closed.
   */
  public javax.jms.Topic getTopic() throws JMSException {
    checkClosed();
    return (javax.jms.Topic) dest;
  }
}
