/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
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
 * Contributor(s): ScalAgent Distributed Technologies
 * Contributor(s): Alex Porras (MediaOcean)
 */
package org.objectweb.joram.mom.proxies.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.StringTokenizer;
import java.util.Vector;

import org.objectweb.joram.mom.proxies.ConnectionManager;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Debug;

/**
 * Starts a TCP entry point for MOM clients.
 */
public class TcpProxyService implements TcpProxyServiceMBean {
  /** logger */
  public static Logger logger = Debug.getLogger(TcpProxyService.class.getName());

  /**
   * Name the property that allow to fix the TCP SO_TIMEOUT property for the
   * client's connections.
   */
  public static final String SO_TIMEOUT_PROP = "org.objectweb.joram.mom.proxies.tcp.soTimeout";

  /**
   * Default value for the TCP SO_TIMEOUT property.
   */
  public static final int DEFAULT_SO_TIMEOUT = 10000;

  /**
   * Name the property that allow to fix the pool size for the
   * connection's listener.
   */
  public static final String POOL_SIZE_PROP = "org.objectweb.joram.mom.proxies.tcp.poolSize";

  /**
   * Default value for the pool size.
   */
  public static final int DEFAULT_POOL_SIZE = 1;

  /**
   * Name the property that allow to fix the TCP BACKLOG property for the
   * client's connections.
   */
  public static final String BACKLOG_PROP = "org.objectweb.joram.mom.proxies.tcp.backlog";

  /**
   * Default value for the TCP BACKLOG property.
   */
  public static final int DEFAULT_BACKLOG = 10;

  /**
   * Default value for the TCP port of the listen socket.
   */
  public static final int DEFAULT_PORT = 16010;

  /**
   * Default IP address for binding the listen socket.
   */
  public static final String DEFAULT_BINDADDRESS = "0.0.0.0"; // all

  private static final String MBEAN_NAME = "type=Connection,mode=tcp";

  /**
   * The proxy service reference (used to stop it).
   */
  private static TcpProxyService proxyService;

  int port;
  int backlog;
  String address;

  /**
   * The server socket listening to connections from the JMS clients.
   */
  private ServerSocket serverSocket = null;

  /**
   * Gets the listening socket.
   * 
   * @return the listening socket.
   */
  ServerSocket getServerSocket() {
    return serverSocket;
  }

  /**
   * Closes the listening socket and sets the variable to null.
   */
  protected void resetServerSocket() {
    if (serverSocket == null) return;

    try {
      serverSocket.close();
    } catch (IOException exc) {}
    serverSocket = null;
  }

  /**
   * Initialize the listening socket.
   *  
   * @param port
   * @param backlog
   * @param address
   * @return The initialized socket.
   * 
   * @throws Exception
   */
  protected ServerSocket createServerSocket(int port, int backlog, String address) throws Exception {
  	if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "TcpProxyService.createServerSocket(" + port + ',' + backlog + ',' + address + ')');
  	
  	ServerSocket serverSocket;
  	try {
  	  // TODO (AF): Use the ServerSocketFactory
  	  if (address.equals("0.0.0.0")) {
  	    serverSocket = new ServerSocket(port, backlog);
  	  } else {
  	    serverSocket = new ServerSocket(port, backlog, InetAddress.getByName(address));
  	  }
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR,
                 "TcpProxyService.createServerSocket(" + port + ',' + backlog + ',' + address + ')', exc);
      throw exc;
    }
  	
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "TcpProxyService.createServerSocket serverSocket = " + serverSocket);
    
    return serverSocket;
  }

  /**
   * Initializes the TCP entry point by creating a server socket listening
   * to the specified port.
   * 
   * @param args stringified listening port
   * @param firstTime <code>true</code>  when the agent server starts.   
   */
  public static void init(String args, boolean firstTime) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "TcpProxyService.init(" + args + ',' + firstTime + ')');

    int port = DEFAULT_PORT;
    String address = DEFAULT_BINDADDRESS;
    if (args != null) {
      StringTokenizer st = new StringTokenizer(args);      
      port = Integer.parseInt(st.nextToken());
      if (st.hasMoreTokens()) {
        address = st.nextToken();
      }
    }

    int backlog = AgentServer.getInteger(BACKLOG_PROP, DEFAULT_BACKLOG).intValue();

    // Create the socket here in order to throw an exception
    // if the socket can't be created (even if firstTime is false).
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "TcpProxyService.init() - binding to " + address + ", port " + port);
    proxyService = new TcpProxyService(port, backlog, address);
    proxyService.start();

  }

  /**
   * Stops the service.
   */ 
  public static void stopService() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "TcpProxyService.stop()");
    proxyService.stop();
  }

  /**
   * The list of opened connections
   */
  private Vector connections;

  /**
   * The thread listening to incoming
   * TCP connections.
   */
  private TcpConnectionListener[] connectionListeners;

  private boolean activated = false;

  public TcpProxyService(int port, int backlog, String address) throws Exception {
    // Memorizes values for future socket creation (see activate).
    this.port = port;
    this.backlog = backlog;
    this.address = address;

    this.serverSocket = createServerSocket(port, backlog, address);

    int poolSize = AgentServer.getInteger(POOL_SIZE_PROP, DEFAULT_POOL_SIZE).intValue();
    int timeout = AgentServer.getInteger(SO_TIMEOUT_PROP, DEFAULT_SO_TIMEOUT).intValue();

    this.connections = new Vector();
    connectionListeners = new TcpConnectionListener[poolSize];
    for (int i = 0; i < poolSize; i++) {
      connectionListeners[i] = new TcpConnectionListener(this, timeout);
    }
  }

  protected void start() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "TcpProxyService.start()");
    ConnectionManager.getCurrentInstance().addManager(this);
    activate();
  }

  public String getMBeanName() {
    return MBEAN_NAME;
  }

  void registerConnection(TcpConnection tcpConnection) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "TcpProxyService.registerConnection(" + tcpConnection + ')');
    connections.addElement(tcpConnection);
  }

  void unregisterConnection(TcpConnection tcpConnection) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "TcpProxyService.unregisterConnection(" + tcpConnection + ')');
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
    deactivate();
    closeAllConnections();
    ConnectionManager.getCurrentInstance().removeManager(this);
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "TcpProxyService.stoped.");
  }

  public void activate() {
  	if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "TcpProxyService.activate()");
  	
    try {
      if (serverSocket == null)
        serverSocket = createServerSocket(port, backlog, address);

      for (int i = 0; i < connectionListeners.length; i++) {
        if (!connectionListeners[i].isRunning())
          connectionListeners[i].start();
      }
      activated = true;
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "TcpProxyService.activated.");
      
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "TcpProxyService.activate()", exc);
    }
  }

  public void closeAllConnections() {
    Vector stopList = (Vector) connections.clone();
    for (int i = 0; i < stopList.size(); i++) {
      TcpConnection tc = (TcpConnection) stopList.elementAt(i);
      tc.close();
    }
  }

  public void deactivate() {
    for (int i = 0; i < connectionListeners.length; i++) {
      connectionListeners[i].stop();
    }
    activated = false;
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "TcpProxyService.activated = false");
  }

  public boolean isActivated() {
    return activated;
  }

  public int getRunningConnectionsCount() {
    return connections.size();
  }

  public int getTcpListenersPoolSize() {
    return connectionListeners.length;
  }

  public String getServerAddress() {
    return address.toString();
  }

  public int getFailedLoginCount() {
    int failedCount = 0;
    for (int i = 0; i < connectionListeners.length; i++) {
      failedCount += connectionListeners[i].getFailedLoginCount();
    }
    return failedCount;
  }

  public int getInitiatedConnectionCount() {
    int initCount = 0;
    for (int i = 0; i < connectionListeners.length; i++) {
      initCount += connectionListeners[i].getInitiatedConnectionCount();
    }
    return initCount;
  }

  public int getProtocolErrorCount() {
    int errorCount = 0;
    for (int i = 0; i < connectionListeners.length; i++) {
      errorCount += connectionListeners[i].getProtocolErrorCount();
    }
    return errorCount;
  }
}
