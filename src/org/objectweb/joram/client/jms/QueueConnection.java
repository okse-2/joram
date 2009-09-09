/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2009 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.client.jms;

import javax.jms.JMSException;
import javax.jms.IllegalStateException;

import org.objectweb.joram.client.jms.connection.RequestChannel;

/**
 * Implements the <code>javax.jms.QueueConnection</code> interface.
 */
public class QueueConnection extends Connection implements javax.jms.QueueConnection {
  /**
   * Creates a <code>QueueConnection</code> instance.
   *
   * @param factoryParameters  The factory parameters.
   * @param requestChannel     The actual connection to wrap.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public QueueConnection(FactoryParameters factoryParameters,
                         RequestChannel requestChannel) throws JMSException {
    super(factoryParameters, requestChannel);
  }

  /**
   * API method.
   * 
   * @exception IllegalStateException  If the connection is closed.
   * @exception InvalidSelectorException  If the selector syntax is wrong.
   * @exception InvalidDestinationException  If the target destination does
   *              not exist.
   * @exception JMSException  If the method fails for any other reason.
   */
  public javax.jms.ConnectionConsumer createConnectionConsumer(javax.jms.Queue queue, 
                                                               String selector,
                                                               javax.jms.ServerSessionPool sessionPool,
                                                               int maxMessages) throws JMSException {
    return super.createConnectionConsumer(queue, selector, sessionPool, maxMessages);
  }

  /**
   * API method.
   * 
   * @exception IllegalStateException  If the connection is closed.
   * @exception JMSException  In case of an invalid acknowledge mode.
   */
  public synchronized javax.jms.QueueSession createQueueSession(boolean transacted,
                                                                int acknowledgeMode) throws JMSException {
    checkClosed();
    QueueSession qs = new QueueSession(this, 
                                       transacted, acknowledgeMode,
                                       getRequestMultiplexer());
    addSession(qs);
    return qs;
  }

  /** 
   * API method.
   *
   * @exception IllegalStateException  Systematically.
   */
  public javax.jms.ConnectionConsumer createDurableConnectionConsumer(javax.jms.Topic topic,
                                                                      String subname,
                                                                      String selector,
                                                                      javax.jms.ServerSessionPool sessPool,
                                                                      int maxMessages) throws JMSException {
    throw new IllegalStateException("Forbidden call on a QueueConnection.");
  }
}
