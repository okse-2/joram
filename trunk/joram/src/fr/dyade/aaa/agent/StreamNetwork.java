/*
 * Copyright (C) 2001 - 2003 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 */
package fr.dyade.aaa.agent;

import java.io.*;
import java.net.*;

/**
 *  <code>StreamNetwork</code> is a base implementation of <code>Network</code>
 * class for stream sockets.
 */
public abstract class StreamNetwork extends CausalNetwork {

  /** Creates a new Network component */
  public StreamNetwork() {
    super();
  }

  /**
   * Numbers of attempt to bind the server's socket before aborting.
   */
  final static int CnxRetry = 3;

  /**
   *  This method creates and returns a socket connected to a ServerSocket at
   * the specified network address and port. It may be overloaded in subclass,
   * in order to create particular subclasses of sockets.
   * <p>
   *  Due to polymorphism of both factories and sockets, different kinds of
   * sockets can be used by the same application code. The sockets returned
   * to the application can be subclasses of <a href="java.net.Socket">
   * Socket</a>, so that they can directly expose new APIs for features such
   * as compression, security, or firewall tunneling.
   *
   * @param host	the server host.
   * @param port	the server port.
   * @return		a socket connected to a ServerSocket at the specified
   *			network address and port.
   *
   * @exception IOException	if the connection can't be established
   */
  Socket createSocket(InetAddress host, int port) throws IOException {
    if (host == null)
      throw new UnknownHostException();
    return new Socket(host, port);
  }

  /**
   *  This method creates and returns a server socket which uses all network
   * interfaces on the host, and is bound to the specified port. It may be
   * overloaded in subclass, in order to create particular subclasses of
   * server sockets.
   *
   * @param port	the port to listen to.
   * @return		a server socket bound to the specified port.
   *
   * @exception IOException	for networking errors
   */
  ServerSocket createServerSocket() throws IOException {
    for (int i=0; ; i++) {
      try {
        return new ServerSocket(port);
      } catch (BindException exc) {
        if (i > CnxRetry) throw exc;
        try {
          Thread.sleep(i * 200);
        } catch (InterruptedException e) {}
      }
    }
  }

  /**
   *  Configures this socket using the socket options established for this
   * factory. It may be overloaded in subclass, in order to handle particular
   * subclasses of sockets
   *
   * @param Socket	the socket.
   *
   * @exception IOException	for networking errors
   */ 
  static void setSocketOption(Socket sock) throws SocketException {
    // Don't use TCP data coalescing - ie Nagle's algorithm
    sock.setTcpNoDelay(true);
    // Read operation will block indefinitely until requested data arrives
    sock.setSoTimeout(0);
    // Set Linger-on-Close timeout.
    sock.setSoLinger(true, 60);
  }

  /**
   *  Returns an <code>ObjectInputStream</code> for this socket. This
   * method may be overloaded in subclass, transforming the data along
   * the way or providing additional functionality (ie cyphering).
   *
   * @param sock	the socket.
   * @return		an input stream for reading object from this socket.
   *
   * @exception	IOException	if an I/O error occurs when creating the
   *				input stream.
   */
  static ObjectInputStream
  getInputStream(Socket sock) throws IOException {
    return new ObjectInputStream(sock.getInputStream());
  }

  /**
   *  Returns an <code>ObjectOutputStream</code> for this socket. This
   * method may be overloaded in subclass, transforming the data along
   * the way or providing additional functionality (ie cyphering).
   *
   * @param sock	the socket.
   * @return		an output stream for writing object to this socket.
   *
   * @exception	IOException	if an I/O error occurs when creating the
   *				output stream.
   */
  static ObjectOutputStream
  getOutputStream(Socket sock) throws IOException {
    return new ObjectOutputStream(sock.getOutputStream());
  }
}
