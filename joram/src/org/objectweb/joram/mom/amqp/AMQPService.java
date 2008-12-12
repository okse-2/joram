/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 - ScalAgent Distributed Technologies
 * Copyright (C) 2008 CNES
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
package org.objectweb.joram.mom.amqp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.StringTokenizer;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.util.Debug;

public class AMQPService {

  public static Logger logger = Debug.getLogger(AMQPService.class.getName());
  
  /**
   * Default value for the TCP SO_TIMEOUT property.
   */
  public static final int DEFAULT_SO_TIMEOUT = 100000;
  /**
   * Name the property that allow to fix the TCP SO_TIMEOUT property for the
   * client's connections.
   */
  public static final String SO_TIMEOUT_PROP = "AMQP.soTimeout";
  /**
   * Default value for the TCP port of the listen socket.
   */
  public static final int DEFAULT_PORT = 5672;
  public static final String DEFAULT_BINDADDRESS = "0.0.0.0"; // all
  /**
   * Name the property that allow to fix the pool size for the
   * connection's listener.
   */
  public static final String POOL_SIZE_PROP = "AMQP.poolSize";

  /**
   * Default value for the pool size.
   */
  public static final int DEFAULT_POOL_SIZE = 2;
  /**
   * Default value for the TCP BACKLOG property.
   */
  public static final int DEFAULT_BACKLOG = 10;
  /**
   * Name the property that allow to fix the TCP BACKLOG property for the
   * client's connections.
   */
  public static final String BACKLOG_PROP = "AMQP.backlog";
  /**
   * The proxy reference (used to stop it).
   */
  protected static AMQPService amqpService;
  
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
  public static void init(String args, boolean firstTime) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AMQPService.init(" + args + ',' + firstTime + ')');

    port = DEFAULT_PORT;
    address = DEFAULT_BINDADDRESS;
    if (args != null) {
      StringTokenizer st = new StringTokenizer(args);      

      if (st.hasMoreTokens())
        port = Integer.parseInt(st.nextToken());
      
      if (st.hasMoreTokens()) {
        address = st.nextToken();
      }
    }
 
    int backlog = AgentServer.getInteger(BACKLOG_PROP, DEFAULT_BACKLOG).intValue();
    
    // Create the socket here in order to throw an exception
    // if the socket can't be created (even if firstTime is false).
    ServerSocket serverSocket;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AMQPService.init() - binding to " +
          address + ", port " + port);

    if (address.equals("0.0.0.0")) {
      serverSocket = new ServerSocket(port, backlog);
    } else {
      serverSocket = new ServerSocket(port, backlog, InetAddress.getByName(address));
    }

    int poolSize = AgentServer.getInteger(POOL_SIZE_PROP, DEFAULT_POOL_SIZE).intValue();
    int timeout = AgentServer.getInteger(SO_TIMEOUT_PROP, DEFAULT_SO_TIMEOUT).intValue();

    amqpService = new AMQPService(serverSocket, poolSize, timeout);
    amqpService.start();
  }
  

  /**
   * Stops the service.
   */ 
  public static void stopService() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AMQPService.stopService()");
    amqpService.stop();
  }

  /**
   * The listening server socket
   */
  private ServerSocket serverSocket;

  /**
   * The thread listening to incoming
   * TCP connections.
   */
  private AMQPConnectionListener[] connectionListeners;

  public AMQPService(ServerSocket serverSocket, int poolSize, int timeout) {
    this.serverSocket = serverSocket;
    connectionListeners = new AMQPConnectionListener[poolSize];
    for (int i = 0; i < poolSize; i++) {
      MOMHandler momHandler = new JoramMOMHandler();
      connectionListeners[i] = new AMQPConnectionListener(serverSocket, timeout, momHandler);
      momHandler.setConsumer(connectionListeners[i]);
    }
  }

  protected void start() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AMQPService.start()");
    for (int i = 0; i < connectionListeners.length; i++) {
      connectionListeners[i].start();
    }
  }

  private void stop() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "stopping connection listener");
    try {
      if (serverSocket != null)
        serverSocket.close();
    } catch (IOException exc) {
    }
    serverSocket = null;
    for (int i = 0; i < connectionListeners.length; i++) {
      connectionListeners[i].stop();
    }
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "stopped connection listener");
  }
}
