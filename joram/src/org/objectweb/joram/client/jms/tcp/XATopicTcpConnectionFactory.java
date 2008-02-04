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
 * Contributor(s): Nicolas Tachker (ScalAgent)
 */
package org.objectweb.joram.client.jms.tcp;

import org.objectweb.joram.client.jms.XAConnection;
import org.objectweb.joram.client.jms.XATopicConnection;
import org.objectweb.joram.client.jms.Connection;
import org.objectweb.joram.client.jms.TopicConnection;
import org.objectweb.joram.client.jms.admin.AdminModule;

import java.util.Vector;

import javax.naming.NamingException;


/**
 * An <code>XATopicTcpConnectionFactory</code> instance is a factory of
 * TCP connections for XA Pub/Sub communication.
 */
public class XATopicTcpConnectionFactory
             extends org.objectweb.joram.client.jms.XATopicConnectionFactory
{
  /**
   * Constructs an <code>XATopicTcpConnectionFactory</code> instance.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   */
  public XATopicTcpConnectionFactory(String host, int port)
  {
    super(host, port);
  }


  /**
   * Method inherited from the <code>XATopicConnectionFactory</code> class..
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.XATopicConnection
      createXATopicConnection(String name, String password)
    throws javax.jms.JMSException {
    return new XATopicConnection(params, 
                                 new TcpConnection(params, 
                                                   name, 
                                                   password,
                                                   reliableClass));
  }

  /**
   * Method inherited from the <code>XAConnectionFactory</code> class.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.XAConnection
      createXAConnection(String name, String password)
    throws javax.jms.JMSException {
    return new XAConnection(params,
                            new TcpConnection(params, 
                                              name, 
                                              password,
                                              reliableClass));
  }

  /**
   * Method inherited from the <code>TopicConnectionFactory</code> class.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.TopicConnection
      createTopicConnection(String name, String password)
    throws javax.jms.JMSException {
    return new TopicConnection(params, 
                               new TcpConnection(params, 
                                                 name, 
                                                 password,
                                                 reliableClass));
  }

  /**
   * Method inherited from the <code>ConnectionFactory</code> class.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.Connection createConnection(String name, String password)
    throws javax.jms.JMSException {
    return new Connection(params,
                          new TcpConnection(params, 
                                            name, 
                                            password,
                                            reliableClass));
  }

  /**
   * Admin method creating a <code>javax.jms.XATopicConnectionFactory</code> 
   * instance for creating TCP connections with a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   */ 
  public static javax.jms.XATopicConnectionFactory
      create(String host, int port) {
    return create(host, 
                  port, 
                  "org.objectweb.joram.client.jms.tcp.ReliableTcpClient");
  }

  /**
   * Admin method creating a <code>javax.jms.XATopicConnectionFactory</code> 
   * instance for creating TCP connections with a given server.
   *
   * @param host           Name or IP address of the server's host.
   * @param port           Server's listening port.
   * @param reliableClass  Reliable class name.
   */ 
  public static javax.jms.XATopicConnectionFactory
      create(String host, 
             int port,
             String reliableClass) {
    XATopicTcpConnectionFactory cf = new XATopicTcpConnectionFactory(host, port);
    cf.setReliableClass(reliableClass);
    return cf;
  }

  /**
   * Admin method creating a <code>javax.jms.XATopicConnectionFactory</code> 
   * instance for creating TCP connections with the local server.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   */ 
  public static javax.jms.XATopicConnectionFactory create()
                throws java.net.ConnectException
  {
    return create(AdminModule.getLocalHost(), AdminModule.getLocalPort());
  }
}
