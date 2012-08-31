/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2012 ScalAgent Distributed Technologies
 * Copyright (C) 2004 Bull SA
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): Frederic Maistre (Bull SA)
 */
package org.objectweb.joram.client.jms.local;

import javax.jms.JMSException;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.FactoryParameters;
import org.objectweb.joram.client.jms.XAQueueConnectionFactory;
import org.objectweb.joram.client.jms.connection.RequestChannel;
import org.objectweb.joram.shared.security.Identity;

/**
 * An <code>XAQueueLocalConnectionFactory</code> instance is a factory of
 * local connections for XA PTP communication.
 *  
 * @deprecated Replaced next to Joram 5.2.1 by {@link LocalConnectionFactory}.
 */
public class XAQueueLocalConnectionFactory extends XAQueueConnectionFactory {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /**
   * Constructs an <code>XAQueueLocalConnectionFactory</code> instance.
   * Should only be used for internal purposes.
   */
  public XAQueueLocalConnectionFactory() {
    super("localhost", -1);
  }

  /**
   * Creates the <code>LocalRequestChannel</code> object needed to connect to the
   * colocated server.
   * 
   * @param params          Connection configuration parameters.
   * @param identity        Client's identity.
   * @param reliableClass   The protocol specific class.
   * @return                The <code>RequestChannel</code> object specific to the protocol used.
   * 
   * @exception JMSException  A problem occurs during the connection.
   * 
   * @see ConnectionFactory#createRequestChannel(FactoryParameters, Identity, String)
   */
  protected RequestChannel createRequestChannel(FactoryParameters params,
                                                Identity identity,
                                                String reliableClass) throws JMSException {
    return new LocalRequestChannel(identity);
  }

  /**
   * Administration method creating a <code>javax.jms.XAQueueConnectionFactory</code>
   * instance for creating local connections.
   */ 
  public static javax.jms.XAQueueConnectionFactory create() {
    return new XAQueueLocalConnectionFactory();
  }
}
