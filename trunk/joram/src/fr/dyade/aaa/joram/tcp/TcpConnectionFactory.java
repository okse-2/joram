/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
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
 * Contributor(s):
 */
package fr.dyade.aaa.joram.tcp;

import fr.dyade.aaa.joram.Connection;
import fr.dyade.aaa.joram.FactoryParameters;

import java.util.Vector;

import javax.naming.NamingException;


/**
 * A <code>TcpConnectionFactory</code> instance is a factory of
 * TCP connections.
 */
public class TcpConnectionFactory extends fr.dyade.aaa.joram.ConnectionFactory
{
  /**
   * Constructs a <code>TcpConnectionFactory</code> instance.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   */
  public TcpConnectionFactory(String host, int port)
  {
    super(host, port);
  }

  /**
   * Constructs an empty <code>TcpConnectionFactory</code> instance.
   */
  public TcpConnectionFactory()
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
    return new Connection(params, new TcpConnection(params, name, password));
  }
}
