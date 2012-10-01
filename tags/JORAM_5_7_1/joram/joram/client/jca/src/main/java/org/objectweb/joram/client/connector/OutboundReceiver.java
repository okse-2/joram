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
 * Contributor(s): Nicolas Tachker (Bull SA)
 */
package org.objectweb.joram.client.connector;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.JMSSecurityException;

import org.objectweb.joram.client.jms.TemporaryQueue;
import javax.jms.Connection;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * An <code>OutboundReceiver</code> instance wraps a JMS PTP consumer
 * for a component involved in outbound messaging. 
 */
public class OutboundReceiver extends OutboundConsumer
                              implements javax.jms.QueueReceiver
{
  /** Queue instance to consume messages from. */
  private Queue queue;


  /**
   * Constructs an <code>OutboundReceiver</code> instance.
   */
  OutboundReceiver(Queue queue,
                   MessageConsumer consumer,
                   OutboundSession session)
    throws JMSException {
    super(consumer, session);
    this.queue = queue;
    
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                    "OutboundReceiver(" + queue + 
                                    ", " + consumer + 
                                    ", " + session + ")");
    
    if (queue instanceof TemporaryQueue) {
      Connection tempQCnx = ((TemporaryQueue) queue).getCnx();

      if (tempQCnx == null || !session.cnx.cnxEquals(tempQCnx))
        throw new JMSSecurityException("Forbidden consumer on this "
                                       + "temporary destination.");
    }
  }


  /** Returns the consumer's queue. */
  public Queue getQueue() throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, this + " getQueue() = " + queue);

    checkValidity();
    return queue;
  }
}
