/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2006 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): David Feliot (ScalAgent DT)
 * Contributor(s): Alex Porras (MediaOcean)
 */
package org.objectweb.joram.mom.proxies.tcp;

import fr.dyade.aaa.agent.*;
import fr.dyade.aaa.util.*;

import org.objectweb.joram.mom.MomTracing;

import java.net.*;
import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * Starts a TCP entry point for MOM clients.
 */
public class TcpProxyService {
  /**
   * Name the property that allow to fix the TCP SO_TIMEOUT property for the
   * client's connections.
   */
  public static final String SO_TIMEOUT_PROP = 
      "org.objectweb.joram.mom.proxies.tcp.soTimeout";

  /**
   * Default value for the TCP SO_TIMEOUT property.
   */
  public static final int DEFAULT_SO_TIMEOUT = 10000;

  /**
   * Name the property that allow to fix the pool size for the
   * connection's listener.
   */
  public static final String POOL_SIZE_PROP = 
      "org.objectweb.joram.mom.proxies.tcp.poolSize";

  /**
   * Default value for the pool size.
   */
  public static final int DEFAULT_POOL_SIZE = 1;

  /**
   * Name the property that allow to fix the TCP BACKLOG property for the
   * client's connections.
   */
  public static final String BACKLOG_PROP = 
      "org.objectweb.joram.mom.proxies.tcp.backlog";

  /**
   * Default value for the TCP BACKLOG property.
   */
  public static final int DEFAULT_BACKLOG = 10;

  /**
   * Default value for the TCP port of the listen socket.
   */
  public static final int DEFAULT_PORT = 16010;

  public static final String DEFAULT_BINDADDRESS = "0.0.0.0"; // all


  /**
   * The proxy service reference (used to stop it).
   */
  protected static TcpProxyService proxyService;

  private static int port;

  public static final int getListenPort() {
    return port;
  }

  private static String address;

  public static final String getListenAddress() {
    return address;
  }

  /**
   * Initializes the TCP entry point by creating a server socket listening
   * to the specified port.
   * 
   * @param args stringified listening port
   * @param firstTime <code>true</code>  when the agent server starts.   
   */
  public static void init(String args, boolean firstTime) 
    throws Exception {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, "TcpProxyService.init(" + 
        args + ',' + firstTime + ')');

    port =  DEFAULT_PORT;;
    address = DEFAULT_BINDADDRESS;
    if (args != null) {
      StringTokenizer st = new StringTokenizer(args);      
      port = Integer.parseInt(st.nextToken());
      if (st.hasMoreTokens()) {
        address = st.nextToken();
      }
    }
    
    int backlog = Integer.getInteger(
      BACKLOG_PROP, DEFAULT_BACKLOG).intValue();

    // Create the socket here in order to throw an exception
    // if the socket can't be created (even if firstTime is false).
    ServerSocket serverSocket;

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, "SSLTcpProxyService.init() - binding to address " + address + ", port " + port);

    if (address.equals("0.0.0.0")) {
      serverSocket = new ServerSocket(port);
    }
    else {
      serverSocket = new ServerSocket(port, backlog, InetAddress.getByName(address));
    }

    int poolSize = Integer.getInteger(
      POOL_SIZE_PROP, DEFAULT_POOL_SIZE).intValue();

    int timeout = Integer.getInteger(
      SO_TIMEOUT_PROP, DEFAULT_SO_TIMEOUT).intValue();

    proxyService = new TcpProxyService(
      serverSocket, poolSize, timeout);
    proxyService.start();
  }

  /**
   * Stops the service.
   */ 
  public static void stopService() {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, "TcpProxyService.stop()");
    proxyService.stop();
  }

  /**
   * The listening server socket
   */
  private ServerSocket serverSocket;

  /**
   * The list of opened connections
   */
  private Vector connections;

  /**
   * The thread listening to incoming
   * TCP connections.
   */
  private TcpConnectionListener[] connectionListeners;

  public TcpProxyService(ServerSocket serverSocket,
                         int poolSize,
                         int timeout) {
    this.serverSocket = serverSocket;
    this.connections = new Vector();
    connectionListeners = new TcpConnectionListener[poolSize];
    for (int i = 0; i < poolSize; i++) {
      connectionListeners[i] = new TcpConnectionListener(serverSocket,
                                                         this,
                                                         timeout);
    }
  }

  protected void start() {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, "TcpProxyService.start()");
    for (int i = 0; i < connectionListeners.length; i++) {
      connectionListeners[i].start();
    }
  }

  void registerConnection(TcpConnection tcpConnection) {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, "TcpProxyService.registerConnection(" +
        tcpConnection + ')');
    connections.addElement(tcpConnection);
  }

  void unregisterConnection(TcpConnection tcpConnection) {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, "TcpProxyService.unregisterConnection(" +
        tcpConnection + ')');
    connections.removeElement(tcpConnection);
  }

  TcpConnection getConnection(AgentId proxyId, int key) {
    for (int i = 0; i < connections.size(); i++) {
      TcpConnection tc = (TcpConnection)connections.elementAt(i);
      if (tc.getProxyId() == proxyId &&
          tc.getKey() == key) {
        return tc;
      }
    }
    return null;
  }

  private void stop() {
    Vector stopList = (Vector)connections.clone();
    for (int i = 0; i < stopList.size(); i++) {
      TcpConnection tc = 
        (TcpConnection)stopList.elementAt(i);
      tc.close();
    }
    for (int i = 0; i < connectionListeners.length; i++) {
      connectionListeners[i].stop();
    }
  }
}