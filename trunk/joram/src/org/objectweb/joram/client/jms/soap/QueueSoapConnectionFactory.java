/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2009 ScalAgent Distributed Technologies
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
package org.objectweb.joram.client.jms.soap;

import java.net.ConnectException;

import javax.jms.JMSSecurityException;

import org.objectweb.joram.client.jms.Connection;
import org.objectweb.joram.client.jms.QueueConnection;
import org.objectweb.joram.client.jms.QueueConnectionFactory;
import org.objectweb.joram.client.jms.admin.AdminModule;

/**
 * A <code>QueueSoapConnectionFactory</code> instance is a factory of
 * SOAP connections for PTP communication.
 */
public class QueueSoapConnectionFactory extends QueueConnectionFactory {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /**
   * Constructs a <code>QueueSoapConnectionFactory</code> instance.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   * @param timeout  Duration in seconds during which a SOAP connection might
   *          be inactive before being considered as dead (0 for never).
   */
  public QueueSoapConnectionFactory(String host, int port, int timeout) {
    super(host, port);
    params.cnxPendingTimer = timeout * 1000;
  }

  /**
   * Constructs an empty <code>QueueSoapConnectionFactory</code> instance.
   */
  public QueueSoapConnectionFactory() {
  }

  /**
   * Method inherited from the <code>QueueConnectionFactory</code> class.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.QueueConnection createQueueConnection(String name, String password) throws javax.jms.JMSException {
    initIdentity(name, password);
    return new QueueConnection(params, new SoapRequestChannel(params, identity));
  }

  /**
   * Method inherited from the <code>ConnectionFactory</code> class.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.Connection createConnection(String name, String password) throws javax.jms.JMSException {
    initIdentity(name, password);
    return new Connection(params, new SoapRequestChannel(params, identity));
  }

  /**
   * Admin method creating a <code>javax.jms.QueueConnectionFactory</code>
   * instance for creating SOAP connections with a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   * @param timeout  Duration in seconds during which a SOAP connection might
   *          be inactive before being considered as dead (0 for never).
   */ 
  public static javax.jms.QueueConnectionFactory create(String host, int port, int timeout) {
    return new QueueSoapConnectionFactory(host, port, timeout);
  }

  /**
   * Admin method creating a <code>javax.jms.QueueConnectionFactory</code>
   * instance for creating SOAP connections with the local server.
   *
   * @param timeout  Duration in seconds during which a SOAP connection might
   *          be inactive before being considered as dead (0 for never).
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   */ 
  public static javax.jms.QueueConnectionFactory create(int timeout) throws java.net.ConnectException {
    return create(AdminModule.getLocalHost(), 
                  AdminModule.getLocalPort(),
                  timeout);
  }
}
