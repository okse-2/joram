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

import fr.dyade.aaa.joram.XAConnection;
import fr.dyade.aaa.joram.XATopicConnection;
import fr.dyade.aaa.joram.Connection;
import fr.dyade.aaa.joram.TopicConnection;

import java.util.Vector;

import javax.naming.NamingException;


/**
 * An <code>XATopicTcpConnectionFactory</code> instance is a factory of
 * TCP connections for XA Pub/Sub communication.
 */
public class XATopicTcpConnectionFactory
             extends fr.dyade.aaa.joram.XATopicConnectionFactory
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
         throws javax.jms.JMSException
  {
    return new XATopicConnection(params, 
                                 new TcpConnection(params, name, password));
  }

  /**
   * Method inherited from the <code>XAConnectionFactory</code> class.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.XAConnection
         createXAConnection(String name, String password)
         throws javax.jms.JMSException
  {
    return new XAConnection(params, new TcpConnection(params, name, password));
  }

  /**
   * Method inherited from the <code>TopicConnectionFactory</code> class.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.TopicConnection
         createTopicConnection(String name, String password)
         throws javax.jms.JMSException
  {
    return new TopicConnection(params, 
                               new TcpConnection(params, name, password));
  }

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
