/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies
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
package org.objectweb.joram.client.jms;

import javax.jms.JMSException;

/**
 * Implements the <code>javax.jms.XAQueueConnectionFactory</code> interface.
 */
public abstract class XAQueueConnectionFactory
                extends XAConnectionFactory
                implements javax.jms.XAQueueConnectionFactory
{
  /**
   * Constructs an <code>XAQueueConnectionFactory</code> dedicated to a
   * given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   */
  public XAQueueConnectionFactory(String host, int port)
  {
    super(host, port);
  }

 
  /** Returns a string view of the connection factory. */
  public String toString()
  {
    return "XAQCF:" + params.getHost() + "-" + params.getPort();
  }

  /**
   * API method, implemented according to the communication protocol.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public abstract javax.jms.XAQueueConnection
                  createXAQueueConnection(String name, String password)
                  throws JMSException;

  /**
   * API method.
   *
   * @exception JMSSecurityException  If the default identification is
   *              incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.XAQueueConnection createXAQueueConnection()
         throws JMSException
  {
    return createXAQueueConnection(ConnectionFactory.getDefaultLogin(),
                                   ConnectionFactory.getDefaultPassword());
  }

  /**
   * Method inherited from interface <code>QueueConnectionFactory</code>,
   * implemented according to the communication protocol.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public abstract javax.jms.QueueConnection
                  createQueueConnection(String name, String password)
                  throws JMSException;

  /**
   * Method inherited from interface <code>QueueConnectionFactory</code>.
   *
   * @exception JMSSecurityException  If the default identification is
   *              incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.QueueConnection createQueueConnection() throws JMSException
  {
    return createQueueConnection(ConnectionFactory.getDefaultLogin(),
                                 ConnectionFactory.getDefaultPassword());
  }

  /**
   * Method inherited from interface <code>ConnectionFactory</code>,
   * implemented according to the communication protocol..
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public abstract javax.jms.Connection
                  createConnection(String name, String password)
                  throws JMSException;

  /**
   * Method inherited from interface <code>ConnectionFactory</code>.
   *
   * @exception JMSSecurityException  If the default identification is
   *              incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.Connection createConnection() throws JMSException
  {
    return createConnection(ConnectionFactory.getDefaultLogin(),
                            ConnectionFactory.getDefaultPassword());
  }
}
