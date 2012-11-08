/*
 * Copyright (C) 2005 - 2005 ScalAgent Distributed Technologies
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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.net.UnknownHostException;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * <tt>HttpNetwork</tt> is a specialization of <tt>HttpNetwork</tt>
 * for SSL.
 */
public final class HttpsNetwork extends HttpNetwork {
  /**
   * Name of property that allow to fix the keystore's password:
   * "HttpsNetwork.pass". By default the password is "changeit".
   * This property can be fixed either from <code>java</code> launching
   * command (-Dname=value), or by in <code>a3servers.xml</code> configuration
   * file (property element).
   */
  public final static String PASS = "HttpsNetwork.pass";
  /**
   * Name of property that allow to fix the keystore's pathname:
   * "HttpsNetwork.keyfile". By default the key file is ".keystore".
   * This property can be fixed either from <code>java</code> launching
   * command (-Dname=value), or by in <code>a3servers.xml</code> configuration
   * file (property element).
   */
  public final static String KEYFILE = "HttpsNetwork.keyfile";

  /**
   *
   */
  SSLSocketFactory socketFactory = null;
  /**
   *
   */
  SSLServerSocketFactory serverSocketFactory = null;

  public HttpsNetwork() throws Exception {
    super();
  }

  SSLSocketFactory getSocketFactory() throws IOException {
    if (socketFactory == null) {
      try {
        char[] pass =  AgentServer.getProperty(PASS, "changeit").toCharArray();
        String keyFile = AgentServer.getProperty(KEYFILE, ".keystore");
 
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(keyFile), pass);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, tmf.getTrustManagers(), null);
 
        socketFactory = ctx.getSocketFactory();
      } catch (IOException exc) {
        throw exc;
      } catch (Exception exc) {
        logmon.log(BasicLevel.ERROR,
                   this.getName() + ", cannot initialize SSLSocketFactory", exc);
        throw new IOException(exc.getMessage());
      }
    }
    return socketFactory;
  }

  SSLServerSocketFactory getServerSocketFactory() throws IOException {
    if (serverSocketFactory == null) {
      try {
        char[] pass =  AgentServer.getProperty(PASS, "changeit").toCharArray();
        String keyFile = AgentServer.getProperty(KEYFILE, ".keystore");
 
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(keyFile), pass);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, pass);

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), null, null);
 
        serverSocketFactory = ctx.getServerSocketFactory();
      } catch (IOException exc) {
        throw exc;
      } catch (Exception exc) {
        logmon.log(BasicLevel.ERROR,
                   this.getName() + ", cannot initialize SSLServerSocketFactory", exc);
        throw new IOException(exc.getMessage());
      }
    }
    return serverSocketFactory;
  }

  /**
   *  This method creates and returns a SSL server socket which is bound to
   * the specified port.
   *
   * @param port	the port to listen to.
   * @return		a SSL server socket bound to the specified port.
   *
   * @exception IOException	for networking errors
   */
  ServerSocket createServerSocket(int port) throws IOException {
    ServerSocket serverSocket = 
      getServerSocketFactory().createServerSocket(port, backlog, inLocalAddr);
    ((SSLServerSocket) serverSocket).setNeedClientAuth(false);

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
    return getSocketFactory().createSocket(addr, port,
                                           outLocalAddr, outLocalPort);
  }

  /**
   * This method creates a tunnelling socket if a proxy is used.
   *
   * @param host	the server host.
   * @param port	the server port.
   * @param proxy	the proxy host.
   * @param proxyport	the proxy port.
   * @return		a socket connected to a ServerSocket at the specified
   *			network address and port.
   *
   * @exception IOException	if the connection can't be established
   */
  Socket createTunnelSocket(InetAddress host, int port,
                            InetAddress proxy, int proxyport) throws IOException {
    // Set up a socket to do tunneling through the proxy.
    // Start it off as a regular socket, then layer SSL over the top of it.
    Socket tunnel = new Socket(proxy, proxyport);
    doTunnelHandshake(tunnel, host, port);

    // Ok, let's overlay the tunnel socket with SSL.
    SSLSocket socket =
      (SSLSocket) getSocketFactory().createSocket(tunnel,host.getHostName(), port, true);
    // register a callback for handshaking completion event
    socket.addHandshakeCompletedListener(
      new HandshakeCompletedListener() {
        public void handshakeCompleted(HandshakeCompletedEvent event) {
        }
      });

    return socket;
  }

  private void doTunnelHandshake(Socket tunnel, InetAddress host, int port)
    throws IOException {
    OutputStream out = tunnel.getOutputStream();
    String msg = "CONNECT " + host.getHostName() + ":" + port + " HTTP/1.0\n"
      + "User-Agent: "
      + sun.net.www.protocol.http.HttpURLConnection.userAgent
      + "\r\n\r\n";
    byte b[];
    try {
      // We really do want ASCII7 -- the http protocol doesn't change
      // with locale.
      b = msg.getBytes("ASCII7");
    } catch (UnsupportedEncodingException ignored) {
      // If ASCII7 isn't there, something serious is wrong, but
      // Paranoia Is Good (tm)
      b = msg.getBytes();
    }
    out.write(b);
    out.flush();

    // We need to store the reply so we can create a detailed
    // error message to the user.
    byte		reply[] = new byte[200];
    int		replyLen = 0;
    int		newlinesSeen = 0;
    boolean		headerDone = false;	/* Done on first newline */

    InputStream	in = tunnel.getInputStream();

    while (newlinesSeen < 2) {
      int i = in.read();
      if (i < 0) {
        throw new IOException("Unexpected EOF from proxy");
      }
      if (i == '\n') {
        headerDone = true;
        ++newlinesSeen;
      } else if (i != '\r') {
        newlinesSeen = 0;
        if (!headerDone && replyLen < reply.length) {
          reply[replyLen++] = (byte) i;
        }
      }
    }

    // Converting the byte array to a string is slightly wasteful
    // in the case where the connection was successful, but it's
    // insignificant compared to the network overhead.
    String replyStr;
    try {
      replyStr = new String(reply, 0, replyLen, "ASCII7");
    } catch (UnsupportedEncodingException ignored) {
      replyStr = new String(reply, 0, replyLen);
    }

    /* We asked for HTTP/1.0, so we should get that back */
    if (! (replyStr.startsWith("HTTP/1.0 200") ||
          (replyStr.startsWith("HTTP/1.1 200")))) {
      throw new IOException("Unable to tunnel , proxy returns \"" + replyStr + "\"");
    }

    // tunneling Handshake was successful!
  }
}

