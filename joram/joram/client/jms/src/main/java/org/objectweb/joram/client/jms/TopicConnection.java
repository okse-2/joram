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
 * Implements the <code>javax.jms.TopicConnection</code> interface.
 */
public class TopicConnection extends Connection implements javax.jms.TopicConnection {

  /**
   * Creates a <code>TopicConnection</code> instance.
   *
   */
  public TopicConnection() {
    super();
  }

  /**
   * API method.
   * Creates a connection consumer for this connection, this is an expert facility needed
   * for applications servers.
   * 
   * @param topic       the topic to access.
   * @param selector    only messages with properties matching the message selector expression
   *                    are delivered. A value of null or an empty string indicates that there is
   *                    no message selector for this message consumer.
   * @param sessionPool the server session pool to associate with this connection consumer.
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
  public javax.jms.ConnectionConsumer createConnectionConsumer(javax.jms.Topic topic,
                                                               String selector,
                                                               javax.jms.ServerSessionPool sessionPool,
                                                               int maxMessages) throws JMSException {
    return super.createConnectionConsumer(topic, selector, sessionPool, maxMessages);
  }

  /**
   * API method.
   * Creates a TopicSession object.
   *    
   * @param transacted      indicates whether the session is transacted.
   * @param acknowledgeMode indicates whether the consumer or the client will acknowledge any
   *                        messages it receives; ignored if the session is transacted. Legal
   *                        values are Session.AUTO_ACKNOWLEDGE, Session.CLIENT_ACKNOWLEDGE, and
   *                        Session.DUPS_OK_ACKNOWLEDGE.
   * @return A newly created session.

   * @exception IllegalStateException  If the connection is closed.
   * @exception JMSException  In case of an invalid acknowledge mode.
   */
  public javax.jms.TopicSession createTopicSession(boolean transacted,
                                                   int acknowledgeMode) throws JMSException {
    checkClosed();
    TopicSession ts = new TopicSession(this, 
                                       transacted, acknowledgeMode, 
                                       getRequestMultiplexer());
    addSession(ts);
    return ts;
  }

  /**
   * API method.
   * 
   * @exception IllegalStateException  If the connection is closed.
   * @exception InvalidSelectorException  If the selector syntax is wrong.
   * @exception InvalidDestinationException  If the target topic does
   *              not exist.
   * @exception JMSException  If the method fails for any other reason.
   */
  public javax.jms.ConnectionConsumer createDurableConnectionConsumer(javax.jms.Topic topic, 
                                                                      String subName,
                                                                      String selector,
                                                                      javax.jms.ServerSessionPool sessPool,
                                                                      int maxMessages) throws JMSException {
    return super.createDurableConnectionConsumer(topic, subName, selector, sessPool, maxMessages);
  }
}
