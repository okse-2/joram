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
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;
import com.sun.net.ssl.*;
import java.security.KeyStore;

class SSLNetwork extends PoolCnxNetwork {
  final static String SSLCONTEXT = "fr.dyade.aaa.agent.SSLNetwork.SSLContext";
  final static String KMGRFACT = "fr.dyade.aaa.agent.SSLNetwork.KeyMgrFact";
  final static String KTYPE = "fr.dyade.aaa.agent.SSLNetwork.KeyStoreType";
  final static String KPROVIDER = "fr.dyade.aaa.agent.SSLNetwork.KeyStoreProvider";

  final static String PASS = "fr.dyade.aaa.agent.SSLNetwork.pass";
  final static String KEYFILE = "fr.dyade.aaa.agent.SSLNetwork.KeyFile";

  SSLSocketFactory socketFactory = null;
  SSLServerSocketFactory serverSocketFactory = null;

  SSLNetwork() throws Exception {
    super();
    name = "SSLNetwork#" + AgentServer.getServerId();

    // AF: For testing
    java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
    // AF: End

    SSLContext ctx;
    KeyManagerFactory kmf;
    KeyStore ks;

    char[] pass;
    String keyFile;

    ctx = SSLContext.getInstance(
      AgentServer.getProperty(SSLCONTEXT, "SSL"));
    kmf = KeyManagerFactory.getInstance(
      AgentServer.getProperty(KMGRFACT, "SunX509"));
//     ks = KeyStore.getInstance(
//       AgentServer.getProperty(KTYPE, "jks"),
//       AgentServer.getProperty(KPROVIDER, "SunJSSE"));
    ks = KeyStore.getInstance("JKS");
    pass =  AgentServer.getProperty(PASS, "A3TBJAP").toCharArray();
    keyFile = AgentServer.getProperty(KEYFILE, "./keyfile");
    ks.load(new FileInputStream(keyFile), pass);
    kmf.init(ks, pass);
    ctx.init(kmf.getKeyManagers(), null, null);

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
  ServerSocket createServerSocket() throws IOException {
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
  static void setSocketOption(Socket sock) throws SocketException {
    // Don't use TCP data coalescing - ie Nagle's algorithm
    sock.setTcpNoDelay(true);
    // Read operation will block indefinitely until requested data arrives
    sock.setSoTimeout(0);
    // Set Linger-on-Close timeout.
    sock.setSoLinger(true, 60);
  }

}
