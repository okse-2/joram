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
import javax.jms.MessageConsumer;
import javax.jms.Topic;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * An <code>OutboundSubscriber</code> instance wraps a JMS PubSub consumer
 * for a component involved in outbound messaging. 
 */
public class OutboundSubscriber extends OutboundConsumer implements javax.jms.TopicSubscriber {
  
  public static Logger logger = Debug.getLogger(OutboundSubscriber.class.getName());
  
  /** Topic instance to consume messages from. */
  private Topic topic;
  /** NoLocal parameter. */
  private boolean noLocal;


  /**
   * Constructs an <code>OutboundSubscriber</code> instance.
   */
  OutboundSubscriber(Topic topic, 
                     boolean noLocal,
                     MessageConsumer consumer,
                     OutboundSession session) {
    super(consumer, session);
    this.topic = topic;
    this.noLocal = noLocal;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "OutboundSubscriber(" + topic +
                                    ", " + noLocal +
                                    ", " + consumer +
                                    ", " + session + ")");
  }


  /** Returns the consumer's topic. */
  public Topic getTopic() throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " getTopic()");
    
    checkValidity();
    return topic;
  }

  /** Returns the noLocal parameter. */
  public boolean getNoLocal() throws JMSException {
   if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " getNoLocal()");

    checkValidity();
    return noLocal;
  }
}
