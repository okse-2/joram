/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 - ScalAgent Distributed Technologies
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
 * Contributor(s): Nicolas Tachker (Bull SA)
 */
package org.objectweb.joram.client.connector;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Topic;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * An <code>OutboundPublisher</code> instance wraps a JMS producer
 * for a component involved in PubSub outbound messaging. 
 */
public class OutboundPublisher extends OutboundProducer
                               implements javax.jms.TopicPublisher {
  
  public static Logger logger = Debug.getLogger(OutboundPublisher.class.getName());
  
  /**
   * Constructs an <code>OutboundPublisher</code> instance.
   *
   * @param producer  The JMS producer to wrap.
   * @param session   The OutboundSession the publisher belongs to.
   */
  OutboundPublisher(MessageProducer producer, OutboundSession session) {
    super(producer, session);

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "OutboundPublisher(" + producer + ", " + session + ")");
  }

 
  /** Delegates the call to the wrapped producer. */
  public Topic getTopic() throws JMSException
  {
    checkValidity();
    return (Topic) producer.getDestination();
  }

  /** Delegates the call to the wrapped producer. */
  public void publish(Message message,
                      int deliveryMode, 
                      int priority,
                      long timeToLive)
    throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " publish(" + message +
                                    ", " + deliveryMode +
                                    ", " + priority +
                                    ", " + timeToLive + ")");

    checkValidity();
    producer.send(message, deliveryMode, priority, timeToLive);
  }

  /** Delegates the call to the wrapped producer. */
  public void publish(Message message) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " publish(" + message + ")");

    checkValidity();
    producer.send(message);
  }

  /** Delegates the call to the wrapped producer. */
  public void publish(Topic topic,
                      Message message,
                      int deliveryMode, 
                      int priority,
                      long timeToLive)
    throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " publish(" + topic +
                                    ", " + message +
                                    ", " + deliveryMode +
                                    ", " + priority +
                                    ", " + timeToLive + ")");

    checkValidity();
    producer.send(topic, message, deliveryMode, priority, timeToLive);
  }

  /** Delegates the call to the wrapped producer. */
  public void publish(Topic topic, Message message) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " publish(" + topic +
                                    ", " + message + ")");

    checkValidity();
    producer.send(topic, message);
  }
}
