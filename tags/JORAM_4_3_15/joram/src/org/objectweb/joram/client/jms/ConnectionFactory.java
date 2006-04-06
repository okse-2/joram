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

import java.util.Vector;
import java.util.Hashtable;

import javax.jms.JMSException;
import javax.naming.*;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * Implements the <code>javax.jms.ConnectionFactory</code> interface.
 */
public abstract class ConnectionFactory
                extends org.objectweb.joram.client.jms.admin.AdministeredObject
                implements javax.jms.ConnectionFactory
{
  /** Object containing the factory's parameters. */
  protected FactoryParameters params;

  /** Reliable class name, for exemple use by ssl. */
  protected String reliableClass = null;

  /**
   * Constructs a <code>ConnectionFactory</code> dedicated to a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   */
  public ConnectionFactory(String host, int port) {
    params = new FactoryParameters(host, port);

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": created.");
  }

  /**
   * Constructs an empty <code>ConnectionFactory</code>.
   */
  public ConnectionFactory() {}

  /** Returns a string view of the connection factory. */
  public String toString() {
    return "CF:" + params.getHost() + "-" + params.getPort();
  }

  public void setReliableClass(String reliableClass) {
    this.reliableClass = reliableClass;
  }

  /**
   * API method, implemented according to the communication protocol.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public abstract javax.jms.Connection
                  createConnection(String name, String password)
                  throws JMSException;

  /**
   * Default login name for connection, default value is "anonymous".
   * This value can be adjusted through the <tt>JoramDfltLogin</tt> property.
   */
  final static String dfltLogin = "anonymous";
  /**
   * Default login password for connection, default value is "anonymous".
   * This value can be adjusted through the <tt>JoramDfltPassword</tt>
   * property.
   */
  final static String dfltPassword = "anonymous";
 
  /**
   * Returns default login name for connection.
   * Default value "anonymous" can be adjusted by setting the
   * <tt>JoramDfltLogin</tt> property.
   */
  public static String getDefaultLogin() {
    return System.getProperty("JoramDfltLogin", dfltLogin);
  }

  /**
   * Returns the default login password for connection.
   * Default value "anonymous" can be adjusted by setting the
   * <tt>JoramDfltPassword</tt> property.
   */
  public static String getDefaultPassword() {
    return System.getProperty("JoramDfltPassword", dfltPassword);
  }
 
   /**
   * API method.
   *
   * @exception JMSSecurityException  If the default identification is
   *              incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.Connection createConnection() throws JMSException {
    return createConnection(getDefaultLogin(), getDefaultPassword());
  }


  /** Returns the factory's configuration parameters. */
  public FactoryParameters getParameters() {
    return params;
  } 

  
  /** Sets the naming reference of a connection factory. */
  public Reference getReference() throws NamingException {
    Reference ref = super.getReference();
    ref.add(new StringRefAddr("cFactory.host", params.getHost()));
    ref.add(new StringRefAddr("cFactory.port",
                              new Integer(params.getPort()).toString()));
    ref.add(new StringRefAddr("cFactory.cnxT",
                              new Integer(params.connectingTimer).toString()));
    ref.add(new StringRefAddr("cFactory.txT",
                              new Integer(params.txPendingTimer).toString()));
    ref.add(new StringRefAddr("cFactory.cnxPT",
                              new Integer(params.cnxPendingTimer).toString()));
    ref.add(new StringRefAddr("reliableClass", reliableClass));
    return ref;
  }


  /**
   * Codes a <code>ConnectionFactory</code> as a Hashtable for travelling
   * through the SOAP protocol.
   */
  public Hashtable code() {
    Hashtable h = new Hashtable();
    h.put("host", params.getHost());
    h.put("port", new Integer(params.getPort()));
    h.put("connectingTimer", new Integer(params.connectingTimer));
    h.put("txPendingTimer", new Integer(params.txPendingTimer));
    h.put("cnxPendingTimer", new Integer(params.cnxPendingTimer));
    return h;
  }

  /**
   * Implements the <code>decode</code> abstract method defined in the
   * <code>fr.dyade.aaa.jndi2.soap.SoapObjectItf</code> interface.
   * <p>
   * Actual implementation of the method is located in the 
   * tcp and soap sub classes.
   */
  public void decode(Hashtable h) {
    params = new FactoryParameters((String) h.get("host"),
                                   ((Integer) h.get("port")).intValue());
    params.connectingTimer = ((Integer) h.get("connectingTimer")).intValue();
    params.txPendingTimer = ((Integer) h.get("txPendingTimer")).intValue();
    params.cnxPendingTimer = ((Integer) h.get("cnxPendingTimer")).intValue();
  }
}
