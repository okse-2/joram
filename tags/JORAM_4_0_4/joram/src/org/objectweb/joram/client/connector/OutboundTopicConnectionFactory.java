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

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;


/**
 * An <code>OutboundTopicConnectionFactory</code> instance is used for
 * getting a PubSub connection to an underlying JORAM server.
 */
public class OutboundTopicConnectionFactory
             extends OutboundConnectionFactory
             implements javax.jms.TopicConnectionFactory,
                        java.io.Serializable,
                        javax.resource.Referenceable
{
  /**
   * Constructs an <code>OutboundTopicConnectionFactory</code> instance.
   *
   * @param mcf        Central manager for outbound connectivity.
   * @param cxManager  Manager for connection pooling.
   */
  OutboundTopicConnectionFactory(ManagedConnectionFactoryImpl mcf,
                                 ConnectionManager cxManager)
  {
    super(mcf, cxManager);
  }


  /**
   * Requests a PubSub connection for the default user, eventually returns an
   * <code>OutboundTopicConnection</code> instance.
   *
   * @exception JMSSecurityException   If connecting is not allowed.
   * @exception IllegalStateException  If the underlying JORAM server
   *                                   is not reachable.
   * @exception JMSException           Generic exception.
   */
  public javax.jms.TopicConnection createTopicConnection() throws JMSException
  {
    return createTopicConnection(mcf.userName, mcf.password);
  }

  /**
   * Requests a PubSub connection for a given user, eventually returns an
   * <code>OutboundConnection</code> instance.
   *
   * @exception JMSSecurityException   If connecting is not allowed.
   * @exception IllegalStateException  If the underlying JORAM server
   *                                   is not reachable.
   * @exception JMSException           Generic exception.
   */
  public javax.jms.TopicConnection
         createTopicConnection(String userName, String password)
         throws JMSException
  {
    try {
      TopicConnectionRequest cxRequest =
        new TopicConnectionRequest(userName, password);

      Object o = cxManager.allocateConnection(mcf, cxRequest);
      return (javax.jms.TopicConnection) o;
    }
    catch (javax.resource.spi.SecurityException exc) {
      throw new JMSSecurityException("Invalid user identification: " + exc);
    }
    catch (javax.resource.spi.CommException exc) {
      throw new IllegalStateException("Could not connect to the JORAM server: "
                                      + exc);
    }
    catch (javax.resource.ResourceException exc) {
      throw new JMSException("Could not create connection: " + exc);
    }
  }
}
