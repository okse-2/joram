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
package org.objectweb.joram.client.jms.tcp;

import javax.jms.JMSException;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.FactoryParameters;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.connection.RequestChannel;
import org.objectweb.joram.shared.security.Identity;

/**
 * An <code>XATcpConnectionFactory</code> instance is a factory of
 * TCP connections dedicated to XA communication.
 *  
 * @deprecated Replaced next to Joram 5.2.1 by {@link TcpConnectionFactory}.
 */
public class XATcpConnectionFactory extends org.objectweb.joram.client.jms.XAConnectionFactory {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /**
   * Constructs an <code>iXATcpConnectionFactory</code> instance.
   * This empty constructor is needed for JNDI.
   * Should only be used for internal purposes.
   */
  public XATcpConnectionFactory() {
    super();
  }
  
  /**
   * Constructs an <code>iXATcpConnectionFactory</code> instance.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   */
  private XATcpConnectionFactory(String host, int port) {
    super(host, port);
  }

  /**
   * Creates the <code>TcpRequestChannel</code> object specific to the protocol used.
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
    return new TcpRequestChannel(params, identity, reliableClass);
  }

  /**
   * Admin method creating a <code>javax.jms.XAConnectionFactory</code> 
   * instance for creating TCP connections with a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   */ 
  public static javax.jms.XAConnectionFactory create(String host, int port) {
    return create(host, port, "org.objectweb.joram.client.jms.tcp.ReliableTcpClient");
  }

  /**
   * Admin method creating a <code>javax.jms.XAConnectionFactory</code> 
   * instance for creating TCP connections with a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   * @param reliableClass  Reliable class name.
   */ 
  public static javax.jms.XAConnectionFactory create(String host, int port, String reliableClass) {
    XATcpConnectionFactory cf = new XATcpConnectionFactory(host, port);
    cf.setReliableClass(reliableClass);
    return cf;
  }

  /**
   * Admin method creating a <code>javax.jms.XAConnectionFactory</code> 
   * instance for creating TCP connections with the local server.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   */ 
  public static javax.jms.XAConnectionFactory create() throws java.net.ConnectException  {
    return create(AdminModule.getLocalHost(), AdminModule.getLocalPort());
  }
}
