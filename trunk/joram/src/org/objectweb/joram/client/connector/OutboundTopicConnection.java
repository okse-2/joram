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

import javax.jms.*;


/**
 * An <code>OutboundTopicConnection</code> instance is a handler for a
 * physical PubSub connection to an underlying JORAM server, allowing a
 * component to transparently use this physical connection possibly within
 * a transaction (local or global).
 */
public class OutboundTopicConnection
             extends OutboundConnection
             implements javax.jms.TopicConnection
{
  /**
   * Constructs an <code>OutboundTopicConnection</code> instance.
   *
   * @param managedCx  The managed connection building the handle.
   * @param xac        The underlying physical PubSub connection to handle.
   */
  OutboundTopicConnection(ManagedConnectionImpl managedCx,
                          XATopicConnection xac)
  {
    super(managedCx, xac);
  }

 
  /**
   * Returns the unique authorized JMS session per connection wrapped in
   * an <code>OutboundTopicSession</code> instance.
   *
   * @exception javax.jms.IllegalStateException  If the handle is invalid.
   * @exception javax.jms.JMSException           Generic exception.
   */
  public TopicSession
         createTopicSession(boolean transacted, int acknowledgeMode)
         throws JMSException
  {
    if (! valid)
      throw new javax.jms.IllegalStateException("Invalid connection handle.");

    if (managedCx.session == null)
      managedCx.session = xac.createSession(false, Session.AUTO_ACKNOWLEDGE);

    return new OutboundTopicSession(managedCx.session);
  }

  /**
   * Forbidden call on an application or component's outbound connection,
   * throws a <code>JMSException</code> instance.
   */
  public ConnectionConsumer
         createConnectionConsumer(Topic topic,
                                  String messageSelector,
                                  ServerSessionPool sessionPool,
                                  int maxMessages)
         throws JMSException
  {
    throw new JMSException("Forbidden call on an application or component's "
                           + "session.");
  }
}
