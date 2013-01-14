/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
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

import javax.jms.IllegalStateException;
import javax.jms.InvalidDestinationException;
import javax.jms.InvalidSelectorException;
import javax.jms.JMSException;

/**
 * Implements the <code>javax.jms.QueueConnection</code> interface.
 */
public class QueueConnection extends Connection implements javax.jms.QueueConnection {
  /**
   * Creates a <code>QueueConnection</code> instance.
   */
  public QueueConnection() {
    super();
  }

  /**
   * API method.
   * Creates a connection consumer for this connection, this is an expert facility needed
   * for applications servers.
   * 
   * @param queue       the queue to access.
   * @param selector    only messages with properties matching the message selector expression
   *                    are delivered. A value of null or an empty string indicates that there
   *                    is no message selector for this message consumer.
   * @param sessionPool the server session pool to associate with this connection consumer
   * @param maxMessages the maximum number of messages that can be assigned to a server session
   *                    at one time.
   * @return The connection consumer.
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
   * Creates a QueueSession object.
   * 
   * @param transacted      indicates whether the session is transacted.
   * @param acknowledgeMode indicates whether the consumer or the client will acknowledge any
   *                        messages it receives; ignored if the session is transacted. Legal
   *                        values are Session.AUTO_ACKNOWLEDGE, Session.CLIENT_ACKNOWLEDGE, and
   *                        Session.DUPS_OK_ACKNOWLEDGE.
   * @return A newly created session.
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
