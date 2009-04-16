/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2007 - 2009 ScalAgent Distributed Technologies
 * Copyright (C) 2007 France Telecom R&D
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
 * Contributor(s):
 */
package org.objectweb.joram.client.jms.admin;

import java.util.Hashtable;

import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.objectweb.joram.client.jms.Connection;
import org.objectweb.joram.client.jms.FactoryParameters;
import org.objectweb.joram.client.jms.QueueConnection;
import org.objectweb.joram.client.jms.TopicConnection;
import org.objectweb.joram.client.jms.XAConnection;
import org.objectweb.joram.client.jms.XAQueueConnection;
import org.objectweb.joram.client.jms.XATopicConnection;
import org.objectweb.joram.client.jms.connection.RequestChannel;
import org.objectweb.joram.shared.security.Identity;
import org.objectweb.joram.shared.security.SimpleIdentity;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Debug;

/**
 * Implements the <code>javax.jms.ConnectionFactory</code> interface.
 */
public abstract class AbstractConnectionFactory extends AdministeredObject {
  
  private static Logger logger = Debug.getLogger(AbstractConnectionFactory.class.getName());
  
  /** Object containing the factory's parameters. */
  protected FactoryParameters params;

  /** Reliable class name, for example use by ssl. */
  protected String reliableClass = null;
  
  /** Authentication identity. */
  protected Identity identity = null;
  
  protected String identityClassName = SimpleIdentity.class.getName();

  /**
   * Constructs a <code>ConnectionFactory</code> dedicated to a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   */
  public AbstractConnectionFactory(String host, int port) {
    params = new FactoryParameters(host, port);
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + ": created.");
  }

  /**
   * Constructs a <code>ConnectionFactory</code> dedicated to a given server.
   *
   * @param url  joram ha url.
   */
  public AbstractConnectionFactory(String url) {
    params = new FactoryParameters(url);
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + ": created.");
  }

  /**
   * Constructs an empty <code>ConnectionFactory</code>.
   * Needed by ObjectFactory.
   */
  public AbstractConnectionFactory() {
    params = new FactoryParameters();
  }

  
  private boolean isSetIdentityClassName = false;
  
  /**
   * set indentity class name
   * @param identityClassName default Identity.SIMPLE_IDENTITY_CLASS (user/passwd).
   */
  public void setIdentityClassName(String identityClassName) {
    this.identityClassName = identityClassName;
    isSetIdentityClassName = true;
  }
  
  /**
   * initialize the user identity.
   * @param user    user name
   * @param passwd  user password
   * @throws JMSException
   */
  protected void initIdentity(String user, String passwd) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "initIdentity(" + user + ", ****)");
    try {
      if (!isSetIdentityClassName) {
        identityClassName = System.getProperty("org.objectweb.joram.Identity", SimpleIdentity.class.getName());
      }
      Class clazz = Class.forName(identityClassName);
      identity = (Identity) clazz.newInstance();
      identity.setIdentity(user, passwd);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "initIdentity : identity = " + identity);
    } catch (ClassNotFoundException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "EXCEPTION:: initIdentity", e);
      throw new JMSException(e.getClass() + ":: " + e.getMessage());
    } catch (InstantiationException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "EXCEPTION:: initIdentity", e);
      throw new JMSException(e.getClass() + ":: " + e.getMessage());
    } catch (IllegalAccessException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "EXCEPTION:: initIdentity", e);
      throw new JMSException(e.getClass() + ":: " + e.getMessage());
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "EXCEPTION:: initIdentity", e);
      throw new JMSException(e.getClass() + ":: " + e.getMessage());
    }
  }
  
  public void setReliableClass(String reliableClass) {
    this.reliableClass = reliableClass;
  }

  /**
   * Default server's hostname for connection, default value is "localhost".
   * This value can be adjusted through the <tt>JoramDfltServerHost</tt>
   * property.
   */
  final static String dfltServerHost = "localhost";

  /**
   * Returns default server's hostname for connection.
   * Default value "localhost" can be adjusted by setting the
   * <tt>JoramDfltServerHost</tt> property.
   */
  public static String getDefaultServerHost() {
    return System.getProperty("JoramDfltServerHost", dfltServerHost);
  }

  /**
   * Default server's port for connection, default value is 16010.
   * This value can be adjusted through the <tt>JoramDfltServerPort</tt>
   * property.
   */
  final static int dfltServerPort = 16010;

  /**
   * Returns default server's port for connection.
   * Default value 16010 can be adjusted by setting the
   * <tt>JoramDfltServerPort</tt> property.
   */
  public static int getDefaultServerPort() {
    return Integer.getInteger("JoramDfltServerPort", dfltServerPort).intValue();
  }
  
  /**
   * Default administrator login name for connection, default value is
   * "root".
   * This value can be adjusted through the <tt>JoramDfltRootLogin</tt>
   * property.
   */
  final static String dfltRootLogin = "root";

  /**
   * Returns default administrator login name for connection.
   * Default value "root" can be adjusted by setting the
   * <tt>JoramDfltRootLogin</tt> property.
   */
  public static String getDefaultRootLogin() {
    return System.getProperty("JoramDfltRootLogin", dfltRootLogin);
  }
  
  /**
   * Default administrator login password for connection, default value is
   * "root".
   * This value can be adjusted through the <tt>JoramDfltRootPassword</tt>
   * property.
   */
  final static String dfltRootPassword = "root";

  /**
   * Returns the default administrator login password for connection.
   * Default value "root" can be adjusted by setting the
   * <tt>JoramDfltRootPassword</tt> property.
   */
  public static String getDefaultRootPassword() {
    return System.getProperty("JoramDfltRootPassword", dfltRootPassword);
  }

  /**
   * Default login name for connection, default value is "anonymous".
   * This value can be adjusted through the <tt>JoramDfltLogin</tt> property.
   */
  final static String dfltLogin = "anonymous";

  /**
   * Returns default login name for connection.
   * Default value "anonymous" can be adjusted by setting the
   * <tt>JoramDfltLogin</tt> property.
   */
  public static String getDefaultLogin() {
    return System.getProperty("JoramDfltLogin", dfltLogin);
  }

  /**
   * Default login password for connection, default value is "anonymous".
   * This value can be adjusted through the <tt>JoramDfltPassword</tt>
   * property.
   */
  final static String dfltPassword = "anonymous";

  /**
   * Returns the default login password for connection.
   * Default value "anonymous" can be adjusted by setting the
   * <tt>JoramDfltPassword</tt> property.
   */
  public static String getDefaultPassword() {
    return System.getProperty("JoramDfltPassword", dfltPassword);
  }

  /** Returns the factory's configuration parameters. */
  public FactoryParameters getParameters() {
    return params;
  }

  /*
   * ConnectionFactory interfaces implementation.
   */
  
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
    return new QueueConnection(params, createRequestChannel(params, identity, reliableClass));
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
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "TcpConnectionFactory.createXAConnection(" + name + ',' + password
          + ") reliableClass=" + reliableClass);

    initIdentity(name, password);
    return new XAConnection(params, createRequestChannel(params, identity, reliableClass));
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
    return new XAQueueConnection(params, createRequestChannel(params, identity, reliableClass));
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
    return new XATopicConnection(params, createRequestChannel(params, identity, reliableClass));
  }

  /*
   * Referenceable interface implementation.
   */
  
  /** Sets the naming reference of an administered object. */
  public final void toReference(Reference ref) throws NamingException {
    toReference(ref, "cf");
  }

  /** Sets the clustered naming reference of a connection factory. */
  public void toReference(Reference ref, String prefix) {
    if (prefix == null) prefix = "cf";

    params.toReference(ref, prefix);
    ref.add(new StringRefAddr(prefix + ".reliableClass", reliableClass));
    ref.add(new StringRefAddr(prefix + ".identityClassName", identityClassName));
  }

  /** Restores the administered object from a naming reference. */
  public final void fromReference(Reference ref) throws NamingException {
    fromReference(ref, "cf");
  }

  /** Restores the administered object from a clustered naming reference. */
  public void fromReference(Reference ref, String prefix) {
    if (prefix == null) prefix = "cf";

    reliableClass = (String) ref.get(prefix + ".reliableClass").getContent();
    setIdentityClassName((String) ref.get(prefix + ".identityClassName").getContent());
    params.fromReference(ref, prefix);
  }

  /*
   * SoapItf interface implementation.
   */

  /**
   * Codes a <code>ConnectionFactory</code> as a Hashtable for traveling
   * through the SOAP protocol.
   */
  public Hashtable code() {
    return code(new Hashtable(), "cf");
  }

  public Hashtable code(Hashtable h, String prefix) {
    if (reliableClass != null)
      h.put(prefix + ".reliableClass", reliableClass);
    h.put(prefix + ".identityClassName", identityClassName);
    return params.code(h, prefix);
  }

  /**
   * Implements the <code>decode</code> abstract method defined in the
   * <code>fr.dyade.aaa.jndi2.soap.SoapObjectItf</code> interface.
   */
  public void decode(Hashtable h) {
    decode(h, "cf");
  }

  public void decode(Hashtable h, String prefix) {
    reliableClass = (String) h.get(prefix + ".reliableClass");
    identityClassName = (String) h.get(prefix + ".identityClassName");
    params.decode(h, prefix);
  }
}
