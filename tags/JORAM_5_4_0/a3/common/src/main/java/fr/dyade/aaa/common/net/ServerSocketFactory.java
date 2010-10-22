/*
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.common.net;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;


/**
 * This class wraps multiples implementations of the java.net.Socket class.
 */
public abstract class ServerSocketFactory {
  /**
   * Logger statique des objets de la classe ServerSocketFactory.
   */
  static Logger logger = Debug.getLogger(ServerSocketFactory.class.getName());

  /**
   * The default implementation of the ServerSocketFactory interface is for 
   * JDK since 1.4.
   */
  public static final String DefaultFactory = ServerSocketFactory13.class.getName();

  /**
   * Returns the ServerSocketFactory singleton for the specified default class.
   *
   * @return The ServerSocketFactory singleton for the default class.
   */
  public final static ServerSocketFactory getDefaultFactory() {
    ServerSocketFactory serverSocketFactory = null;
    try {
      Class factoryClass = Class.forName(DefaultFactory);
      Method method = factoryClass.getMethod("getFactory", (Class[]) null);
      serverSocketFactory = (ServerSocketFactory) method.invoke(null, (Object[]) null);
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR,
                 "Unable to instantiate default SocketFactory: " + DefaultFactory, exc);
    }
    return serverSocketFactory;
  }

  /**
   * Returns the ServerSocketFactory singleton for the specified class.
   * If the specified class can not be instantiated the default one is used.
   *
   * @param  ssfcn The classname for SocketFactory class.
   * @return The ServerSocketFactory singleton for the specified class.
   */
  public final static ServerSocketFactory getFactory(String ssfcn) {
  	ServerSocketFactory serverSocketFactory = null;
    try {
      Class factoryClass = Class.forName(ssfcn);
      Method method = factoryClass.getMethod("getFactory", (Class[]) null);
      serverSocketFactory = (ServerSocketFactory) method.invoke(null, (Object[]) null);
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR,
                 "Use default SocketFactory, unable to instantiate : " + ssfcn, exc);
      serverSocketFactory = getDefaultFactory();
    }
    return serverSocketFactory;
  }

  /**
   * Creates a server socket and binds it to the specified local port number, with
   * the specified backlog. A port number of 0 creates a socket on any free port.
   *
   * @param port      the specified port, or 0 to use any free port.
   * @param backlog   the maximum length of the queue, or 0 to use the default value.
   * 
   * @see java.net.ServerSocket#ServerSocket(int, int)
   */
  public abstract ServerSocket createServerSocket(int port, int backlog) throws IOException;

  /**
   *  Create a server with the specified port, listen backlog, and local IP address to
   * bind to.
   * <p>
   *  The addr argument can be used on a multi-homed host for a ServerSocket that will
   * only accept connect requests to one of its addresses. If addr is null, it will default
   * accepting connections on any/all local addresses.
   * 
   * @param port    the local TCP port, it must be between 0 and 65535, inclusive.
   * @param backlog the maximum length of the queue, or 0 to use the default value.
   * @param addr    the local InetAddress the server will bind to 
   * 
   * @see java.net.ServerSocket#ServerSocket(int, int, InetAddress)
   */
  public abstract ServerSocket createServerSocket(int port,
                                                  int backlog,
                                                  InetAddress addr) throws IOException;
}
