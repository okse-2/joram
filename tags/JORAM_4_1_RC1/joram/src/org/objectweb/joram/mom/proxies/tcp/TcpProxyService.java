/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
  public static final int DEFAULT_PORT = 16010;

  /**
   * The proxy service reference
   * (used to stop it).
   */
  private static TcpProxyService proxyService;

  /**
   * Initializes the TCP entry point by creating a
   * server socket listening to the specified port.
   * 
   * @param args stringified listening port
   * @param firstTime <code>true</code> 
   * when the agent server starts.   
   */
  public static void init(String args, boolean firstTime) 
    throws Exception {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, "TcpProxyService.init(" + 
        args + ',' + firstTime + ')');
    int port;
    if (args != null) {
      StringTokenizer st = new StringTokenizer(args);      
      port = Integer.parseInt(st.nextToken());
    } else {
      port = DEFAULT_PORT;
    }
    // Create the socket here in order to throw an exception
    // if the socket can't be created (even if firstTime is false).
    ServerSocket serverSocket = new ServerSocket(port);

    proxyService = new TcpProxyService(serverSocket);
    proxyService.start();
  }

  /**
   * Stops the service.
   */ 
  public static void stopService() {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG,
        "TcpProxyService.stop()");
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
  private TcpConnectionListener connectionListener;

  public TcpProxyService(ServerSocket serverSocket) {
    this.serverSocket = serverSocket;
    this.connections = new Vector();
    connectionListener = 
      new TcpConnectionListener(
        serverSocket, this);
  }

  private void start() {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, "TcpProxyService.start()");
    connectionListener.start();
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

  private void stop() {
    Vector stopList = (Vector)connections.clone();
    for (int i = 0; i < stopList.size(); i++) {
      TcpConnection tc = 
        (TcpConnection)stopList.elementAt(i);
      tc.close();
    }
    connectionListener.stop();
  }
}
