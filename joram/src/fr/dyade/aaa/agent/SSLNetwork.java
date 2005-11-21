/*
 * Copyright (C) 2003 - 2004 SCALAGENT
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
package fr.dyade.aaa.agent;

import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;
import java.security.KeyStore;
import java.security.SecureRandom;

/**
 *
 */
public final class SSLNetwork extends PoolNetwork {
  public final static String SSLCONTEXT = "fr.dyade.aaa.agent.SSLNetwork.SSLContext";
  public final static String KTYPE = "fr.dyade.aaa.agent.SSLNetwork.KeyStoreType";

  /**
   * Name of property that allow to fix the keystore's password:
   * "SSLNetwork.pass". By default the password is "changeit".
   * This property can be fixed either from <code>java</code> launching
   * command (-Dname=value), or by in <code>a3servers.xml</code> configuration
   * file (property element).
   */
  public final static String PASS = "SSLNetwork.pass";
  /**
   * Name of property that allow to fix the keystore's pathname:
   * "SSLNetwork.keyfile". By default the key file is ".keystore".
   * This property can be fixed either from <code>java</code> launching
   * command (-Dname=value), or by in <code>a3servers.xml</code> configuration
   * file (property element).
   */
  public final static String KEYFILE = "SSLNetwork.keyfile";

  SSLSocketFactory socketFactory = null;
  SSLServerSocketFactory serverSocketFactory = null;

  public SSLNetwork() throws Exception {
    super();
    name = "SSLNetwork#" + AgentServer.getServerId();

    char[] pass =  AgentServer.getProperty(PASS, "changeit").toCharArray();
    String keyFile = AgentServer.getProperty(KEYFILE, ".keystore");

    KeyStore keystore = KeyStore.getInstance(AgentServer.getProperty(KTYPE, "JKS"));
    keystore.load(new FileInputStream(keyFile), pass);

    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
    kmf.init(keystore, pass);

    TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
    tmf.init(keystore);

    SSLContext ctx = SSLContext.getInstance(AgentServer.getProperty(SSLCONTEXT, "TLS"));
    ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

    socketFactory = ctx.getSocketFactory();
    serverSocketFactory = ctx.getServerSocketFactory();
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
    return socketFactory.createSocket(host, port);
  }

  /**
   *  This method creates and returns a server socket which uses all network
   * interfaces on the host, and is bound to the specified port. It may be
   * overloaded in subclass, in order to create particular subclasses of
   * server sockets.
   *
   * @return		a server socket bound to the specified port.
   *
   * @exception IOException	for networking errors
   */
  ServerSocket createServerSocket(int port) throws IOException {
    ServerSocket serverSocket = null;
    serverSocket = serverSocketFactory.createServerSocket(port);
    ((SSLServerSocket) serverSocket).setNeedClientAuth(true);

    return serverSocket;
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
    // Don't use TCP data coalescing - ie Nagle's algorithm
    sock.setTcpNoDelay(true);
    // Read operation will block indefinitely until requested data arrives
    sock.setSoTimeout(0);
    // Set Linger-on-Close timeout.
    sock.setSoLinger(true, 60);
  }

}
