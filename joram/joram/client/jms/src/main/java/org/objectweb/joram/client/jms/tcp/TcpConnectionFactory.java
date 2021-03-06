/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2014 ScalAgent Distributed Technologies
 * Copyright (C) 2004 Bull SA
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
import org.objectweb.joram.client.jms.connection.RequestChannel;

import org.objectweb.joram.shared.security.Identity;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * A <code>TcpConnectionFactory</code> instance is a factory of TCP connections.
 * The created ConnectionFactory can be configured using theFactoryParameters.
 */
public class TcpConnectionFactory extends ConnectionFactory {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  private static Logger logger = Debug.getLogger(TcpConnectionFactory.class.getName());

  /**
   * Constructs an empty <code>TcpConnectionFactory</code> instance.
   * Should only be used for internal purposes.
   */
  public TcpConnectionFactory() {}

  /**
   * Constructs a <code>TcpConnectionFactory</code> instance.
   * By default the connectingTimer property is set to 60 seconds.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   */
  private TcpConnectionFactory(String host, int port) {
    super(host, port);
    params.connectingTimer = 60;
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
   * Administration method creating a <code>ConnectionFactory</code>
   * instance for creating TCP connections with the default server.
   * 
   * @see ConnectionFactory#getDefaultServerHost()
   * @see ConnectionFactory#getDefaultServerPort()
   */ 
  public static ConnectionFactory create() {
    return create(getDefaultServerHost(), getDefaultServerPort());
  }

  /**
   * Administration method creating a <code>ConnectionFactory</code>
   * instance for creating TCP connections with a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   */ 
  public static ConnectionFactory create(String host, int port) {
    return create(host, port, "org.objectweb.joram.client.jms.tcp.ReliableTcpClient");
  }

  /**
   * Administration method creating a <code>ConnectionFactory</code>
   * instance for creating TCP connections with a given server.
   *
   * @param host           Name or IP address of the server's host.
   * @param port           Server's listening port.
   * @param reliableClass  Reliable class name.
   */ 
  public static ConnectionFactory create(String host, int port, String reliableClass) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "TcpConnectionFactory.create(" + host + ',' + port + ',' + reliableClass +')');

    TcpConnectionFactory cf = new TcpConnectionFactory(host, port);
    cf.setReliableClass(reliableClass);
    return cf;
  }
}
