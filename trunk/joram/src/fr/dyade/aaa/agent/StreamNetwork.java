/*
 * Copyright (C) 2001 - 2009 ScalAgent Distributed Technologies
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

import java.io.IOException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.util.ServerSocketFactory;
import fr.dyade.aaa.util.SocketAddress;
import fr.dyade.aaa.util.SocketFactory;

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
   * <code>SoLinger</code> global property or for a particular network
   * by setting <code>\<DomainName\>.SoLinger</code> specific property.
   * <p>
   *  Theses properties can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  int SoLinger = -1;
  /**
   *  Enable/disable SO_TIMEOUT with the specified timeout in milliseconds.
   * The timeout must be > 0. A timeout of zero is interpreted as an infinite
   * timeout. Default value is 0.
   *  This value can be adjusted for all network components by setting
   * <code>SoTimeout</code> global property or for a particular network
   * by setting <code>\<DomainName\>.SoTimeout</code> specific property.
   * <p>
   *  Theses properties can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  int SoTimeout = 0;
  /**
   *  Defines in milliseconds the timeout used during socket connection.
   * The timeout must be > 0. A timeout of zero is interpreted as an infinite
   * timeout. Default value is 0.
   *  This value can be adjusted for all network components by setting
   * <code>ConnectTimeout</code> global property or for a particular network
   * by setting <code>\<DomainName\>.ConnectTimeout</code> specific property.
   * <p>
   *  Theses properties can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  int ConnectTimeout = 0;

  /**
   *  The local address the listen ServerSocket is bound to. A null address
   * will assign the wildcard address. Default value is null.
   *  This value can be adjusted for all network components by setting
   * <code>InLocalAddress</code> global property or for a particular network
   * by setting <code>\<DomainName\>.InLocalAddress</code> specific property.
   * <p>
   *  Theses properties can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  InetAddress inLocalAddr = null;

  /**
   *  The local port the sockets are bound to. A valid port value is between 0
   * and 65535. A port number of zero will let the system pick up an ephemeral
   * port in a bind operation. Default value is 0.
   *  This value can be adjusted for all network components by setting
   * <code>OutLocalPort</code> global property or for a particular network
   * by setting <code>\<DomainName\>.OutLocalPort</code> specific property.
   * <p>
   *  Theses properties can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  int outLocalPort = 0;

  /**
   *  The local address the sockets are bound to. A null address will assign
   * the wildcard address. Default value is null.
   *  This value can be adjusted for all network components by setting
   * <code>OutLocalAddress</code> global property or for a particular network
   * by setting <code>\<DomainName\>.OutLocalAddress</code> specific property.
   * <p>
   *  Theses properties can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  InetAddress outLocalAddr = null;

  /**
   * Allows to define a specific factory for ServerSocket in order to by-pass
   * compatibility problem between JDK version.
   * Currently there is two factories, The default factory one for JDK
   * since 1.4, and "fr.dyade.aaa.util.ServerSocketFactory13" for JDK prior
   * to 1.4.
   *  This value can be adjusted for all network components by setting
   * <code>ServerSocketFactory</code> global property or for a particular
   * network by setting <code>\<DomainName\>.ServerSocketFactory</code>
   * specific property.
   * <p>
   *  Theses properties can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  ServerSocketFactory serverSocketFactory = null;

  /**
   * Allows to define a specific factory for Socket in order to by-pass
   * compatibility problem between JDK version.
   * Currently there is two factories, The default factory one for JDK
   * since 1.4, and "fr.dyade.aaa.util.SocketFactory13" for JDK prior to 1.4.
   *  This value can be adjusted for all network components by setting
   * <code>SocketFactory</code> global property or for a particular network
   * by setting <code>\<DomainName\>.SocketFactory</code> specific property.
   * <p>
   *  Theses properties can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  SocketFactory socketFactory = null;

  /** Creates a new Network component */
  public StreamNetwork() {
    super();
  }

  /**
   * Initializes a new <tt>StreamNetwork</tt> component.
   *
   * @param name	The domain name.
   * @param port	The listen port.
   * @param servers	The list of servers directly accessible from this
   *			network interface.
   *
   * @see Network
   */
  public void init(String name, int port, short[] servers) throws Exception {
    super.init(name, port, servers);
  }
  
  /**
   * Set the properties of the network.
   * Inherited from Network class, can be extended by subclasses.
   */
  public void setProperties() throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, domain + ", StreamNetwork.setProperties()");
    super.setProperties();

    CnxRetry = AgentServer.getInteger("CnxRetry", CnxRetry).intValue();
    CnxRetry = AgentServer.getInteger(domain + ".CnxRetry", CnxRetry).intValue();

    backlog = AgentServer.getInteger("backlog", backlog).intValue();
    backlog = AgentServer.getInteger(domain + ".backlog", backlog).intValue();

    TcpNoDelay = AgentServer.getBoolean(domain + ".TcpNoDelay");
    if (!TcpNoDelay)
      TcpNoDelay = AgentServer.getBoolean("TcpNoDelay");

    SoLinger = AgentServer.getInteger("SoLinger", SoLinger).intValue();
    SoLinger = AgentServer.getInteger(domain + ".SoLinger", SoLinger).intValue();

    SoTimeout = AgentServer.getInteger("SoTimeout", SoTimeout).intValue();
    SoTimeout = AgentServer.getInteger(domain + ".SoTimeout", SoTimeout).intValue();

    ConnectTimeout = AgentServer.getInteger("ConnectTimeout", ConnectTimeout).intValue();
    ConnectTimeout = AgentServer.getInteger(domain + ".ConnectTimeout", ConnectTimeout).intValue();

    String inLocalAddressStr = null;
    inLocalAddressStr = AgentServer.getProperty("InLocalAddress", inLocalAddressStr);
    inLocalAddressStr = AgentServer.getProperty(domain + ".InLocalAddress", inLocalAddressStr);
    if (inLocalAddressStr != null)
      inLocalAddr = InetAddress.getByName(inLocalAddressStr);

    String outLocalAddressStr = null;
    outLocalAddressStr = AgentServer.getProperty("OutLocalAddress", outLocalAddressStr);
    outLocalAddressStr = AgentServer.getProperty(domain + ".OutLocalAddress", outLocalAddressStr);
    if (outLocalAddressStr != null)
      outLocalAddr = InetAddress.getByName(outLocalAddressStr);

    outLocalPort = AgentServer.getInteger("OutLocalPort", outLocalPort).intValue();
    outLocalPort = AgentServer.getInteger(domain + ".OutLocalPort", outLocalPort).intValue();

    String sfcn = AgentServer.getProperty("SocketFactory", SocketFactory.DefaultFactory);
    sfcn = AgentServer.getProperty(domain + ".SocketFactory", sfcn);
    socketFactory = SocketFactory.getFactory(sfcn);

    String ssfcn = AgentServer.getProperty("ServerSocketFactory", ServerSocketFactory.DefaultFactory);
    ssfcn = AgentServer.getProperty(domain + ".ServerSocketFactory", ssfcn);
    serverSocketFactory = ServerSocketFactory.getFactory(ssfcn);
    
    if (logmon.isLoggable(BasicLevel.DEBUG)) {
      StringBuffer strbuf = new StringBuffer();
      strbuf.append(" setProperties(");
      strbuf.append("CnxRetry=").append(CnxRetry);
      strbuf.append(", backlog=").append(backlog);
      strbuf.append(", TcpNoDelay=").append(TcpNoDelay);
      strbuf.append(", SoLinger=").append(SoLinger);
      strbuf.append(", SoTimeout=").append(SoTimeout);
      strbuf.append(", ConnectTimeout=").append(ConnectTimeout);
      strbuf.append(", inLocalAddressStr=").append(inLocalAddressStr);
      strbuf.append(", outLocalAddressStr=").append(outLocalAddressStr);
      strbuf.append(", outLocalPort=").append(outLocalPort);
      strbuf.append(", ssfcn=").append(ssfcn);
      strbuf.append(", sfcn=").append(sfcn);
      strbuf.append(')');
      
      logmon.log(BasicLevel.DEBUG, getName() + strbuf.toString());
    }
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
   *  This method creates and returns a socket connected to a ServerSocket
   * at the specified network address and port. It may be overloaded in
   * subclass, in order to use particular implementation of sockets.
   * <p>
   *  Due to polymorphism of both factories and sockets, different kinds of
   * sockets can be used by the same application code. The sockets returned
   * to the application can be subclasses of <a href="java.net.Socket">
   * Socket</a>, so that they can directly expose new APIs for features such
   * as compression, security, or firewall tunneling.
   *
   * @param addr	the server address.
   * @param port	the server port.
   * @return		a socket connected to a ServerSocket at the specified
   *			network address and port.
   *
   * @exception IOException	if the connection can't be established
   */
  Socket createSocket(InetAddress addr, int port) throws IOException {
    if (addr == null) throw new UnknownHostException();

    return socketFactory.createSocket(addr, port,
                                      outLocalAddr, outLocalPort,
                                      ConnectTimeout);
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
    return serverSocketFactory.createServerSocket(port, backlog, inLocalAddr);
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
