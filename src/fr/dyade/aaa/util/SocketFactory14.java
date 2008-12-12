/*
 * Copyright (C) 2007 - 2008 ScalAgent Distributed Technologies
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
import java.net.InetSocketAddress;
import java.io.IOException;

/**
 * This class implements the SocketFactory interface for JDK since 1.4.
 */
public class SocketFactory14 extends SocketFactory {
  /**
   * The SocketFactory singleton for this class.
   */
  static SocketFactory factory;

  /**
   * Returns the SocketFactory singleton for this class.
   *
   * @return The SocketFactory singleton for this class.
   */
  public static SocketFactory getFactory() {
    if (factory == null)
      factory = new SocketFactory14();
    return factory;
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
  public Socket createSocket(InetAddress addr, int port,
                             int timeout) throws IOException {
    Socket socket = new Socket();
    InetSocketAddress endpoint = new InetSocketAddress(addr, port);
    socket.connect(endpoint, timeout);

    return socket;
  }

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
  public Socket createSocket(InetAddress addr, int port,
                             InetAddress localAddr, int localPort,
                             int timeout) throws IOException {
    Socket socket = new Socket();
    InetSocketAddress endpoint = new InetSocketAddress(addr, port);
    InetSocketAddress bindpoint = new InetSocketAddress(localAddr, localPort);
    socket.bind(bindpoint);
    socket.connect(endpoint, timeout);

    return socket;
  }
}
