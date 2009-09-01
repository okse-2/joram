/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2009 ScalAgent Distributed Technologies
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
 * Contributor(s): Benoit Pelletier (Bull SA)
 *                 Nicolas Tachker (ScalAgent DT)
 */
package org.objectweb.joram.client.jms.ha.tcp;

import javax.jms.JMSException;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.FactoryParameters;
import org.objectweb.joram.client.jms.connection.RequestChannel;
import org.objectweb.joram.shared.security.Identity;

/**
 * An <code>XATcpConnectionFactory</code> instance is a factory of
 * tcp connections dedicated to HA communication.
 */
public class HATcpConnectionFactory extends org.objectweb.joram.client.jms.ConnectionFactory {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /**
   * Constructs an empty <code>HATcpConnectionFactory</code>.
   * Needed by ObjectFactory, should only be used for internal purposes.
   */
  public HATcpConnectionFactory() {
    super();  
  }

  /**
   * Constructs an <code>HATcpConnectionFactory</code> instance.
   * 
   * @param url The Joram HA URL.
   */
  private HATcpConnectionFactory(String url) {
    super(url);
  }

  /**
   * Creates the <code>HATcpRequestChannel</code> object needed to connect to the
   * remote HA server.
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
    return new HATcpRequestChannel(params.getUrl(), params, identity, reliableClass);
  }

  /**
   * Admin method creating a <code>javax.jms.ConnectionFactory</code>
   * instance for creating HA TCP connections with a given list of servers.
   *
   * @param url URL of the HA Joram server
   */
  public static ConnectionFactory create(String url) {
    return create(url, "org.objectweb.joram.client.jms.tcp.ReliableTcpClient");
  }

  /**
   * Admin method creating a <code>javax.jms.ConnectionFactory</code>
   * instance for creating HA TCP connections with a given list of servers.
   *
   * @param url URL of the HA Joram server
   * @param reliableClass  Reliable class name.
   */
  public static ConnectionFactory create(String url, String reliableClass) {
    HATcpConnectionFactory cf = new HATcpConnectionFactory(url);
    cf.setReliableClass(reliableClass);
    return cf;
  }
}
