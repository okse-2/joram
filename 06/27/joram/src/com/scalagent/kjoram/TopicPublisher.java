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

public class TopicPublisher extends MessageProducer
{
  /**
   * Constructs a publisher.
   *
   * @param sess  The session the publisher belongs to.
   * @param topic The topic the publisher publishs messages on.
   *
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
  public Topic getTopic() throws JMSException
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
  public void publish(Message message, int deliveryMode,
                      int priority, long timeToLive) throws JMSException
  {
    super.send(message, deliveryMode, priority, timeToLive);
  }
    
  /** 
   * API method.
   *
   * @exception IllegalStateException  If the publisher is closed, or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public void publish(Message message) throws JMSException
  {
    super.send(message);
  }

  /** 
   * API method.
   *
   * @exception IllegalStateException  If the publisher is closed, or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public void publish(Topic topic, Message message)
            throws JMSException
  {
    super.send(topic, message);
  }

  /** 
   * API method.
   *
   * @exception IllegalStateException  If the publisher is closed, or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public void publish(Topic topic, Message message,
                      int deliveryMode, int priority,
                      long timeToLive) throws JMSException
  {
    super.send(topic, message, deliveryMode, priority, timeToLive);
  }
}
