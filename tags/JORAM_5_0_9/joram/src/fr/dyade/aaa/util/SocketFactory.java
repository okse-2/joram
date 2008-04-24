/*
 * Copyright (C) 2007 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.util;

import java.net.Socket;
import java.net.InetAddress;
import java.io.IOException;
import java.lang.reflect.Method;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

/**
 * This class wraps multiples implementations of the java.net.Socket class.
 */
public abstract class SocketFactory {
  /**
   * Logger statique des objets de la classe SocketFactory.
   */
  static Logger logger = Debug.getLogger(SocketFactory.class.getName());

  /**
   * The default implementation of the SocketFactory interface is for 
   * JDK since 1.4.
   */
  public static final String DefaultFactory = "fr.dyade.aaa.util.SocketFactory14";

  /**
   * Returns the SocketFactory singleton for the specified default class.
   *
   * @return The SocketFactory singleton for the default class.
   */
  public static SocketFactory getDefaultFactory() {
    return SocketFactory14.getFactory();
  }

  /**
   * Returns the SocketFactory singleton for the specified class.
   *
   * @param  The classname for SocketFactory class.
   * @return The SocketFactory singleton for the specified class.
   */
  public static SocketFactory getFactory(String sfcn) {
    SocketFactory socketFactory = null;
    try {
      Class factoryClass = Class.forName(sfcn);
      Method method = factoryClass.getMethod("getFactory", null);
      socketFactory = (SocketFactory) method.invoke(null, null);
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR,
                 "Unable to instantiate SocketFactory: " + sfcn, exc);
      socketFactory = getDefaultFactory();
    }
    return socketFactory;
  }

  /**
   *  Creates a stream socket and connects it to the specified port number at
   * the specified IP address. Try to establish the connection to the server
   * with a specified timeout value. A timeout of zero is interpreted as an
   * infinite timeout. The connection will then block until established or an
   * error occurs.
   *
   * @param addr	the IP address.
   * @param port	the port number.
   * @param timeout	the timeout value to be used in milliseconds.
   */
  public abstract Socket createSocket(InetAddress addr, int port,
                                      int timeout) throws IOException;

  /**
   *  Creates a socket and connects it to the specified remote host on the
   * specified remote port. The Socket will also bind() to the local address
   * and port supplied. Try to establish the connection to the server
   * with a specified timeout value. A timeout of zero is interpreted as an
   * infinite timeout. The connection will then block until established or an
   * error occurs.
   *
   * @param addr	the IP address of the remote host
   * @param port	the remote port
   * @param localAddr	the local address the socket is bound to
   * @param localPort	the local port the socket is bound to 
   * @param timeout	the timeout value to be used in milliseconds.
   */
  public abstract Socket createSocket(InetAddress addr, int port,
                                      InetAddress localAddr, int localPort,
                                      int timeout) throws IOException;
}
