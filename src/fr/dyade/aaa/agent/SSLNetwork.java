/*
 * Copyright (C) 2003 - 2008 SCALAGENT
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

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * A network component using SSL Sockets.
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

  /**
   *
   */
  SSLSocketFactory socketFactory = null;
  /**
   *
   */
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
   *  This method creates and returns a SSL server socket which is bound to
   * the specified port.
   *
   * @param port	the port to listen to.
   * @return		a server socket bound to the specified port.
   *
   * @exception IOException	for networking errors
   */
  ServerSocket createServerSocket(int port) throws IOException {
    ServerSocket serverSocket =
      serverSocketFactory.createServerSocket(port, backlog, inLocalAddr);
    ((SSLServerSocket) serverSocket).setNeedClientAuth(true);
    return serverSocket;
  }

  /**
   *  This method creates and returns a SSL socket connected to a ServerSocket
   * at the specified network address and port.
   *
   * @param addr	the server address.
   * @param port	the server port.
   * @return		a socket connected to a ServerSocket at the specified
   *			network address and port.
   *
   * @exception IOException	if the connection can't be established
   */
  Socket createSocket(InetAddress addr, int port) throws IOException {
    if (addr == null)
      throw new UnknownHostException();

    // AF: Be careful SSLSocketFactory don't allow to use ConnectTimeout
    return socketFactory.createSocket(addr, port,
                                      outLocalAddr, outLocalPort);
  }
}
