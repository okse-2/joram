/*
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies
 * Copyright (C) 2004 - France Telecom R&D
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
 *
 * Initial developer(s): Dyade
 * Contributor(s): ScalAgent Distributed Technologies
 */
package fr.dyade.aaa.agent;

import java.io.*;
import java.util.Vector;
import java.util.Enumeration;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.ConnectException;
import java.net.BindException;
import java.net.UnknownHostException;
import java.net.SocketException;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.SocketAddress;

/**
 *  <code>StreamNetwork</code> is a base implementation of <code>Network</code>
 * class for TCP sockets.
 */
public abstract class StreamNetwork extends Network {
  /**
   *  Numbers of attempt to bind the server's socket before aborting,
   * default value is 3.
   *  This value can be adjusted for all network components by setting
   * <code>CnxRetry</code> global property or for a particular network
   * by setting <code>\<DomainName\>.CnxRetry</code> specific property.
   * <p>
   *  Theses properties can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  int CnxRetry = 3;
  /**
   *  The maximum queue length for incoming connection indications,
   * default value is 5.
   *  This value can be adjusted for all network components by setting
   * <code>backlog</code> global property or for a particular network
   * by setting <code>\<DomainName\>.backlog</code> specific property.
   * <p>
   *  Theses properties can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  int backlog = 5;
  /**
   *  Enable/disable TCP_NODELAY (disable/enable Nagle's algorithm),
   * default value is false.
   *  This value can be adjusted for all network components by setting
   * <code>TcpNoDelay</code> global property or for a particular network
   * by setting <code>\<DomainName\>.TcpNoDelay</code> specific property.
   * <p>
   *  Theses properties can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  boolean TcpNoDelay = false;
  /**
   *  Enable SO_LINGER with the specified linger time in seconds, if the
   * value is less than 0 then it disables SO_LINGER. Default value is -1.
   *  This value can be adjusted for all network components by setting
   * <code>TcpNoDelay</code> global property or for a particular network
   * by setting <code>\<DomainName\>.TcpNoDelay</code> specific property.
   * <p>
   *  Theses properties can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  int SoLinger = -1;
  /**
   *  Enable/disabl SO_TIMEOUT with the specified timeout in milliseconds.
   * The timeout must be > 0. A timeout of zero is interpreted as an infinite
   * timeout.
   *  This value can be adjusted for all network components by setting
   * <code>SoTimeout</code> global property or for a particular network
   * by setting <code>\<DomainName\>.SoTimeout</code> specific property.
   * <p>
   *  Theses properties can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  int SoTimeout = 0;

  /** Creates a new Network component */
  public StreamNetwork() {
    super();
  }

  /**
   * Initializes a new <tt>StreamNetwork</tt> component.
   *
   * @see Network
   *
   * @param name	The domain name.
   * @param port	The listen port.
   * @param servers	The list of servers directly accessible from this
   *			network interface.
   */
  public void init(String name, int port, short[] servers) throws Exception {
    super.init(name, port, servers);

    CnxRetry = Integer.getInteger("CnxRetry", CnxRetry).intValue();
    CnxRetry = Integer.getInteger(name + ".CnxRetry", CnxRetry).intValue();

    backlog = Integer.getInteger("backlog", backlog).intValue();
    backlog = Integer.getInteger(name + ".backlog", backlog).intValue();

    TcpNoDelay = Boolean.getBoolean(name + ".TcpNoDelay");
    if (! TcpNoDelay) TcpNoDelay = Boolean.getBoolean("TcpNoDelay");

    SoLinger = Integer.getInteger("SoLinger", SoLinger).intValue();
    SoLinger = Integer.getInteger(name + ".SoLinger", SoLinger).intValue();

    SoTimeout = Integer.getInteger("SoTimeout", SoTimeout).intValue();
    SoTimeout = Integer.getInteger(name + ".SoTimeout", SoTimeout).intValue();
  }

  /**
   *  This method creates and returns a socket connected to a
   * specified server. It may be overloaded in subclass, in order
   * to create particular subclasses of sockets.
   *
   * @param server	the server descriptor.
   * @return		a socket connected to a ServerSocket at the specified
   *			network address and port.
   *
   * @exception IOException	if the connection can't be established
   */
  final Socket createSocket(ServerDesc server) throws IOException {
    for (Enumeration e = server.getSockAddrs(); e.hasMoreElements();) {
      SocketAddress sa = (SocketAddress) e.nextElement();

      if (this.logmon.isLoggable(BasicLevel.DEBUG))
        this.logmon.log(BasicLevel.DEBUG,
                        this.getName() + ", try to connect server#" +
                        server.getServerId() +
                        ", addr=" + sa.getHostname() +
                        ", port=" + sa.getPort());
                  
      try {
        Socket socket = createSocket(sa);

        if (this.logmon.isLoggable(BasicLevel.DEBUG))
          this.logmon.log(BasicLevel.DEBUG, this.getName() + ", connected");

        // Memorize the right address for next try.
        server.moveToFirst(sa);
        return socket;
      } catch (IOException exc) {
        this.logmon.log(BasicLevel.DEBUG,
                        this.getName() + ", connection refused, try next element");
        continue;
      }
    }
           
    throw new ConnectException("Cannot connect to server#" + server.getServerId());
  }

  /**
   *  This method creates and returns a socket connected to a ServerSocket at
   * the specified socket address. If it fails it resets the address in order
   * to take in account dynamic DNS.
   *
   * @param addr	the socket address.
   * @return		a socket connected to a ServerSocket at the specified
   *			network address and port.
   *
   * @exception IOException	if the connection can't be established

   */
  final Socket createSocket(SocketAddress addr) throws IOException {
    try {
      return createSocket(addr.getAddress(), addr.getPort());
    } catch (IOException exc) {
      this.logmon.log(BasicLevel.DEBUG,
                      this.getName() + ", connection refused, reset addr");
      addr.resetAddr();
      return createSocket(addr.getAddress(), addr.getPort());
    }
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
   * interfaces on the host, and is bound to the specified port.
   *
   * @return		a server socket bound to the specified port.
   *
   * @exception IOException	for networking errors
   */
  final ServerSocket createServerSocket() throws IOException {
    for (int i=0; ; i++) {
      try {
        return createServerSocket(port);
      } catch (BindException exc) {
        if (i > CnxRetry) throw exc;
        try {
          Thread.sleep(i * 200);
        } catch (InterruptedException e) {}
      }
    }
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
  ServerSocket createServerSocket(int port) throws IOException {
    return new ServerSocket(port, backlog);
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
  void setSocketOption(Socket sock) throws SocketException {
    // TCP data coalescing - ie Nagle's algorithm
    sock.setTcpNoDelay(TcpNoDelay);
    // Read operation will block indefinitely until requested data arrives
    sock.setSoTimeout(SoTimeout);
    // Linger-on-Close timeout.
    if (SoLinger >= 0)
      sock.setSoLinger(true, SoLinger);
    else
      sock.setSoLinger(false, 0);
  }
}
