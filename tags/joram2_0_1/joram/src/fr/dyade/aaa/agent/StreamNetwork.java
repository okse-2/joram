/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * The contents of this file are subject to the Dyade Public License,
 * as defined by the file JORAM_LICENSE_ADDENDUM.html
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Dyade web site (www.dyade.fr).
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific terms governing rights and
 * limitations under the License.
 *
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released April 20, 2000.
 *
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */
package fr.dyade.aaa.agent;

import java.io.*;
import java.net.*;

/**
 *  <code>StreamNetwork</code> is a base implementation of <code>Network</code>
 * class for stream sockets.
 */
abstract class StreamNetwork extends Network {
  /** RCS version number of this file: $Revision: 1.3 $ */
  public static final String RCS_VERSION="@(#)$Id: StreamNetwork.java,v 1.3 2001-05-14 16:26:43 tachkeni Exp $";

  /** Creates a new Network component */
  StreamNetwork() {
    super();
  }
  
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
    return new ServerSocket(port);
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
