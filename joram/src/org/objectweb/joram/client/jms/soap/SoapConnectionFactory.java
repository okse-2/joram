/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
 * Copyright (C) 2004 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Contributor(s): Nicolas Tachker (ScalAgent DT)
 */
package org.objectweb.joram.client.jms.soap;

import org.objectweb.joram.client.jms.Connection;
import org.objectweb.joram.client.jms.FactoryParameters;
import org.objectweb.joram.client.jms.admin.AdminModule;

import java.util.Hashtable;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;


/**
 * A <code>SoapConnectionFactory</code> instance is a factory of SOAP
 * connections.
 */
public class SoapConnectionFactory extends org.objectweb.joram.client.jms.ConnectionFactory
{
  /**
   * Constructs a <code>SoapConnectionFactory</code> instance.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   * @param timeout  Duration in seconds during which a SOAP connection might
   *          be inactive before being considered as dead (0 for never).
   */
  public SoapConnectionFactory(String host, int port, int timeout)
  {
    super(host, port);
    params.cnxPendingTimer = timeout * 1000;
  }

  /**
   * Constructs an empty <code>SoapConnectionFactory</code> instance.
   */
  public SoapConnectionFactory()
  {}


  /**
   * Method inherited from the <code>ConnectionFactory</code> class.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.Connection createConnection(String name, String password)
         throws javax.jms.JMSException
  {
    return new Connection(params, new SoapConnection(params, name, password));
  }

  /**
   * Admin method creating a <code>javax.jms.ConnectionFactory</code>
   * instance for creating SOAP connections with a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   * @param timeout  Duration in seconds during which a SOAP connection might
   *          be inactive before being considered as dead (0 for never).
   */ 
  public static javax.jms.ConnectionFactory
                create(String host, int port, int timeout)
  {
    return new SoapConnectionFactory(host, port, timeout);
  }

  /**
   * Admin method creating a <code>javax.jms.ConnectionFactory</code> instance
   * for creating SOAP connections with the local server.
   *
   * @param timeout  Duration in seconds during which a SOAP connection might
   *          be inactive before being considered as dead (0 for never).
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   */ 
  public static javax.jms.ConnectionFactory create(int timeout)
                throws java.net.ConnectException
  {
    return create(AdminModule.getLocalHost(), 
                  AdminModule.getLocalPort(),
                  timeout);
  }
}
