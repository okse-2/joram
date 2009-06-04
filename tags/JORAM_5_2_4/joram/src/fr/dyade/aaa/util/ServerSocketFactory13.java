/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
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

package fr.dyade.aaa.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

/**
 * This class implements the ServerSocketFactory interface for JDK prior to 1.4.
 */
public class ServerSocketFactory13 extends ServerSocketFactory {
  /**
   * The SocketFactory singleton for this class.
   */
  static ServerSocketFactory factory;

  /**
   * Returns the ServerSocketFactory singleton for this class.
   *
   * @return The ServerSocketFactory singleton for this class.
   */
  public static ServerSocketFactory getFactory() {
    if (factory == null)
      factory = new ServerSocketFactory13();
    return factory;
  }

  /**
   *  Creates a server socket and binds it to the specified local port number, with
   * the specified backlog. A port number of 0 creates a socket on any free port.
   *
   * @param port      the specified port, or 0 to use any free port.
   * @param backlog   the maximum length of the queue, or 0 to use the default value.
   * 
   * @see ServerSocketFactory#createServerSocket(java.net.InetAddress, int, int)
   */
  public ServerSocket createServerSocket(int port, int backlog) throws IOException {
    return new ServerSocket(port, backlog);
  }

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
   * @param addr    the local InetAddress the server will bind to.
   * 
   * @see ServerSocketFactory#createServerSocket(int, int, InetAddress)
   */
  public ServerSocket createServerSocket(int port,
                                         int backlog,
                                         InetAddress addr) throws IOException {
    return new ServerSocket(port, backlog, addr);
  }
}
