/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2009 ScalAgent Distributed Technologies
 * Copyright (C) 2007 France Telecom R&D
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
 *                 Benoit Pelletier (Bull SA)
 */
package org.objectweb.joram.client.jms;

import javax.jms.JMSException;
import javax.jms.JMSSecurityException;

import org.objectweb.joram.client.jms.admin.AbstractConnectionFactory;
import org.objectweb.joram.client.jms.connection.RequestChannel;
import org.objectweb.joram.client.jms.tcp.TcpRequestChannel;
import org.objectweb.joram.shared.JoramTracing;
import org.objectweb.joram.shared.security.Identity;
import org.objectweb.util.monolog.api.BasicLevel;

/**
 * Implements all the <code>javax.jms.ConnectionFactory</code> interfaces.
 * <p>
 * A ConnectionFactory object encapsulates a set of configuration parameters defined by
 * an administrator. A client needs to use it to create a connection with a Joram server.
 * 
 * @see javax.jms.ConnectionFactory
 * @see javax.jms.QueueConnectionFactory
 * @see javax.jms.TopicConnectionFactory
 * @see javax.jms.XAConnectionFactory
 * @see javax.jms.XAQueueConnectionFactory
 * @see javax.jms.XATopicConnectionFactory
 */
/**
 *
 */
public abstract class ConnectionFactory extends AbstractConnectionFactory
  implements javax.jms.ConnectionFactory, javax.jms.QueueConnectionFactory, javax.jms.TopicConnectionFactory,
             javax.jms.XAConnectionFactory, javax.jms.XAQueueConnectionFactory, javax.jms.XATopicConnectionFactory{
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /**
   * Constructs an empty <code>ConnectionFactory</code>.
   * Needed by ObjectFactory, should only be used for internal purposes.
   */
  public ConnectionFactory() {
    super();
  }

  /**
   * Constructs a <code>ConnectionFactory</code> dedicated to a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   */
  protected ConnectionFactory(String host, int port) {
    super(host, port);
  }

  /**
   * Constructs a <code>ConnectionFactory</code> dedicated to a given server.
   *
   * @param url  joram url.
   */
  protected ConnectionFactory(String url) {
    super(url);
  }

  /** Returns a string view of the connection factory. */
  public String toString() {
    return "CF:" + params.getHost() + "-" + params.getPort();
  }

  /**
   * Creates the <code>RequestChannel</code> object specific to the protocol used.
   * 
   * @param params          Connection configuration parameters.
   * @param identity        Client's identity.
   * @param reliableClass   The protocol specific class.
   * @return                The <code>RequestChannel</code> object specific to the protocol used.
   * 
   * @exception JMSException  A problem occurs during the connection.
   */
  protected abstract RequestChannel createRequestChannel(FactoryParameters params,
                                                         Identity identity,
                                                         String reliableClass) throws JMSException;

  /**
   * API method, creates a connection with the default user identity.
   * The connection is created in stopped mode.
   *
   * @return  a newly created connection.
   * 
   * @see javax.jms.ConnectionFactory.createConnection()
   * @exception JMSSecurityException  If the default identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.Connection createConnection() throws JMSException {
    return createConnection(getDefaultLogin(), getDefaultPassword());
  }
  
  /**
   * API method, creates a connection with the specified user identity.
   * The connection is created in stopped mode.
   *
   * @param name      the caller's user name.
   * @param password  the caller's password.
   * @return          a newly created connection.
   * 
   * @see javax.jms.ConnectionFactory.createConnection(String, String)
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.Connection createConnection(String name,
                                               String password) throws JMSException {
    initIdentity(name, password);
    return new Connection(params, createRequestChannel(params, identity, reliableClass));
  }

  /**
   * API method, creates a queue connection with the default user identity.
   * The connection is created in stopped mode.
   *
   * @return  a newly created queue connection.
   * 
   * @see javax.jms.ConnectionFactory.createQueueConnection()
   * @exception JMSSecurityException  If the default identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.QueueConnection createQueueConnection() throws JMSException {
    return createQueueConnection(getDefaultLogin(), getDefaultPassword());
  }

  /**
   * API method, creates a queue connection with the specified user identity.
   * The connection is created in stopped mode.
   *
   * @param name      the caller's user name.
   * @param password  the caller's password.
   * @return          a newly created queue connection.
   * 
   * @see javax.jms.ConnectionFactory.createQueueConnection(String, String)
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.QueueConnection createQueueConnection(String name,
                                                         String password) throws JMSException {
    initIdentity(name, password);
    return new QueueConnection(params, new TcpRequestChannel(params, identity, reliableClass));
  }

  /**
   * API method, creates a topic connection with the default user identity.
   * The connection is created in stopped mode.
   *
   * @return  a newly created topic connection.
   * 
   * @see javax.jms.ConnectionFactory.createTopicConnection()
   * @exception JMSSecurityException  If the default identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.TopicConnection createTopicConnection() throws JMSException {
    return createTopicConnection(getDefaultLogin(), getDefaultPassword());
  }

  /**
   * API method, creates a topic connection with the specified user identity.
   * The connection is created in stopped mode.
   *
   * @param name      the caller's user name.
   * @param password  the caller's password.
   * @return          a newly created topic connection.
   * 
   * @see javax.jms.ConnectionFactory.createTopicConnection(String, String)
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.TopicConnection createTopicConnection(String name,
                                                         String password) throws JMSException {
    initIdentity(name, password);
    return new TopicConnection(params, createRequestChannel(params, identity, reliableClass));
  }

  /**
   * API method, creates an XA connection with the default user identity.
   * The connection is created in stopped mode.
   *
   * @return  a newly created XA connection..
   *
   * @see javax.jms.ConnectionFactory.createXAConnection()
   * @exception JMSSecurityException  If the default identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.XAConnection createXAConnection() throws JMSException {
    return createXAConnection(getDefaultLogin(), getDefaultPassword());
  }

  /**
   * API method, creates an XA connection with the specified user identity.
   * The connection is created in stopped mode.
   *
   * @param name      the caller's user name.
   * @param password  the caller's password.
   * @return          a newly created XA connection.
   *
   * @see javax.jms.ConnectionFactory.createXAConnection(String, String)
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.XAConnection createXAConnection(String name, String password) throws javax.jms.JMSException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, 
                                 "TcpConnectionFactory.createXAConnection(" + name + ',' + password + ") reliableClass=" + reliableClass);

    initIdentity(name, password);
    return new XAConnection(params, new TcpRequestChannel(params, identity, reliableClass));
  }

  /**
   * API method, creates an XA queue connection with the default user identity.
   * The connection is created in stopped mode.
   *
   * @return  a newly created XA queue connection..
   *
   * @see javax.jms.ConnectionFactory.createXAQueueConnection()
   * @exception JMSSecurityException  If the default identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.XAQueueConnection createXAQueueConnection() throws JMSException {
    return createXAQueueConnection(getDefaultLogin(), getDefaultPassword());
  }

  /**
   * API method, creates an XA queue connection with the specified user identity.
   * The connection is created in stopped mode.
   *
   * @param name      the caller's user name.
   * @param password  the caller's password.
   * @return          a newly created XA queue connection.
   *
   * @see javax.jms.ConnectionFactory.createXAQueueConnection(String, String)
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  
  public javax.jms.XAQueueConnection createXAQueueConnection(String name, String password) throws javax.jms.JMSException {
    initIdentity(name, password);
    return new XAQueueConnection(params, new TcpRequestChannel(params, identity, reliableClass));
  }

  /**
   * API method, creates an XA topic connection with the default user identity.
   * The connection is created in stopped mode.
   *
   * @return  a newly created XA topic connection..
   *
   * @exception JMSSecurityException  If the default identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.XATopicConnection createXATopicConnection() throws JMSException {
    return createXATopicConnection(getDefaultLogin(), getDefaultPassword());
  }

  /**
   * API method, creates an XA topic connection with the specified user identity.
   * The connection is created in stopped mode.
   *
   * @param name      the caller's user name.
   * @param password  the caller's password.
   * @return          a newly created XA topic connection.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */

  public javax.jms.XATopicConnection createXATopicConnection(String name, String password) throws javax.jms.JMSException {
    initIdentity(name, password);
    return new XATopicConnection(params, new TcpRequestChannel(params, identity, reliableClass));
  }
}
