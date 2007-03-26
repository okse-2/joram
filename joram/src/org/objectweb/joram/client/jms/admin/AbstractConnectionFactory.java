/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2007 ScalAgent Distributed Technologies
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

import javax.naming.*;
import javax.jms.JMSException;

import org.objectweb.joram.client.jms.FactoryParameters;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.joram.shared.JoramTracing;

/**
 * Implements the <code>javax.jms.ConnectionFactory</code> interface.
 */
public abstract class AbstractConnectionFactory extends AdministeredObject {
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
  public AbstractConnectionFactory(String host, int port) {
    params = new FactoryParameters(host, port);

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": created.");
  }

  /**
   * Constructs a <code>ConnectionFactory</code> dedicated to a given server.
   *
   * @param url  joram ha url.
   */
  public AbstractConnectionFactory(String url) {
    params = new FactoryParameters(url);

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": created.");
  }

  /**
   * Constructs an empty <code>ConnectionFactory</code>.
   */
  public AbstractConnectionFactory() {
    params = new FactoryParameters();
  }

  public void setReliableClass(String reliableClass) {
    this.reliableClass = reliableClass;
  }

  /**
   * Default administrator login name for connection, default value is
   * "root".
   * This value can be adjusted through the <tt>JoramDfltRootLogin</tt>
   * property.
   */
  final static String dfltRootLogin = "root";
  /**
   * Default administrator login password for connection, default value is
   * "root".
   * This value can be adjusted through the <tt>JoramDfltRootPassword</tt>
   * property.
   */
  final static String dfltRootPassword = "root";
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
   * Returns default administrator login name for connection.
   * Default value "root" can be adjusted by setting the
   * <tt>JoramDfltRootLogin</tt> property.
   */
  public static String getDefaultRootLogin() {
    return System.getProperty("JoramDfltRootLogin", dfltRootLogin);
  }

  /**
   * Returns the default administrator login password for connection.
   * Default value "root" can be adjusted by setting the
   * <tt>JoramDfltRootPassword</tt> property.
   */
  public static String getDefaultRootPassword() {
    return System.getProperty("JoramDfltRootPassword", dfltRootPassword);
  }

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

  /** Returns the factory's configuration parameters. */
  public FactoryParameters getParameters() {
    return params;
  }

  /** Sets the naming reference of an administered object. */
  public final void toReference(Reference ref) throws NamingException {
    toReference(ref, "cf");
  }

  /** Sets the clustered naming reference of a connection factory. */
  public void toReference(Reference ref, String prefix) {
    if (prefix == null) prefix = "cf";

    params.toReference(ref, prefix);
    ref.add(new StringRefAddr(prefix + ".reliableClass", reliableClass));
  }

  /** Restores the administered object from a naming reference. */
  public final void fromReference(Reference ref) throws NamingException {
    fromReference(ref, "cf");
  }

  /** Restores the administered object from a clustered naming reference. */
  public void fromReference(Reference ref, String prefix) {
    if (prefix == null) prefix = "cf";

    reliableClass = (String) ref.get(prefix + ".reliableClass").getContent();
    params.fromReference(ref, prefix);
  }
}
