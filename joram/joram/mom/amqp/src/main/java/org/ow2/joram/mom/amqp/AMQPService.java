/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 - 2011 ScalAgent Distributed Technologies
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
package org.ow2.joram.mom.amqp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.StringTokenizer;
import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.ow2.joram.mom.amqp.exceptions.TransactionException;
import org.ow2.joram.mom.amqp.marshalling.AMQP;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.util.Transaction;
import org.ow2.joram.mom.amqp.messages.MessageReceived;

/**
 * AMQP service used to open the server socket and start the connection
 * listeners.
 */
public class AMQPService {

  public static Logger logger = Debug.getLogger(AMQPService.class.getName());
  
  public static Naming naming;
  
  /** Default value in seconds for the server heartbeat property. */
  public static final int DEFAULT_HEARTBEAT = 10;

  /**
   * Name of the property that allows to set the server requested heartbeat.
   * Socket timeout (SO_TIMEOUT) property will be 2 times the desired heartbeat.
   */
  public static final String HEARTBEAT_PROP = "AMQP.heartbeat";

  /** Default value for the TCP port of the listen socket. */
  public static final int DEFAULT_PORT = AMQP.PROTOCOL.PORT;

  public static final String DEFAULT_BINDADDRESS = "0.0.0.0"; // all
  
  /** Default value for the TCP BACKLOG property. */
  public static final int DEFAULT_BACKLOG = 10;

  /**
   * Name of the property that allow to fix the TCP BACKLOG property for the
   * client's connections.
   */
  public static final String BACKLOG_PROP = "AMQP.backlog";
  
  public static int heartbeat;
  
  /** The proxy reference (used to stop it). */
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

    // Mix some method numbers to set up a server harshly supporting AMQP 0.8 if needed
    if (args != null) {
      StringTokenizer st = new StringTokenizer(args);

      String param = st.nextToken();
      if (param.equals("0-8")) {
        AMQP.PROTOCOL.MAJOR = 8;
        AMQP.PROTOCOL.MINOR = 0;
        AMQP.Connection.Close.INDEX = 60;
        AMQP.Connection.CloseOk.INDEX = 61;
        AMQP.Connection.mids = new int[] { 10, 11, 20, 21, 30, 31, 40, 41, 60, 61 };

        if (st.hasMoreTokens())
          port = Integer.parseInt(st.nextToken());

        if (st.hasMoreTokens()) {
          address = st.nextToken();
        }
      } else {
        port = Integer.parseInt(param);

        if (st.hasMoreTokens()) {
          address = st.nextToken();
        }
      }

    }
 
    int backlog = AgentServer.getInteger(BACKLOG_PROP, DEFAULT_BACKLOG).intValue();
    
    // Create the socket here in order to throw an exception
    // if the socket can't be created (even if firstTime is false).
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AMQPService.init() - binding to " +
          address + ", port " + port);

    if (address.equals("0.0.0.0")) {
      serverSocket = new ServerSocket(port, backlog);
    } else {
      serverSocket = new ServerSocket(port, backlog, InetAddress.getByName(address));
    }

    heartbeat = AgentServer.getInteger(HEARTBEAT_PROP, DEFAULT_HEARTBEAT).intValue();
     
    // Initialization Naming 
    naming = new Naming();
    
    if (firstTime) {
      Naming.createDefaults();
    } else {
      // Load ....
      loads();
    }
     
    // Initialization transient Agent AMQP
    AMQPAgent agentAMQP = new AMQPAgent();
    agentAMQP.deploy();
    
    amqpService = new AMQPService();
    amqpService.start();
  }

  private static void loads() throws TransactionException, IOException, ClassNotFoundException {
    String[] list = null;
    Transaction transaction = AgentServer.getTransaction();
    
    // load queue names
    list = transaction.getList(Queue.PREFIX_QUEUE);
    // load queues...
    if (list != null) {
      for (int i = 0; i < list.length; i++) {
        Queue.loadQueue(list[i]);
      }
    }
    
    // load exchanges
    list = transaction.getList(IExchange.PREFIX_EXCHANGE);
    // load exchanges...
    if (list != null) {
      for (int i = 0; i < list.length; i++) {
        IExchange.loadExchange(list[i]);
      }
    }
  }
  
  /**
   * Stops the service.
   */ 
  public static void stopService() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AMQPService.stopService()");
    amqpService.stop();
    Naming.clearAll();
  }

  /**
   * The listening server socket
   */
  private static ServerSocket serverSocket;

  /**
   * The thread listening to incoming
   * TCP connections.
   */
  public static Vector<AMQPConnectionListener> connectionListeners;
  private static Vector<AMQPMessageListener> messageListeners;
  
  /**
   * @throws IOException 
   * @throws Exception 
   */
  public AMQPService() throws IOException {
    connectionListeners = new Vector<AMQPConnectionListener>();
    AMQPConnectionListener cnxListener = new AMQPConnectionListener(serverSocket, heartbeat);
    connectionListeners.add(cnxListener);
    messageListeners = new Vector<>();
  }

  protected void start() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AMQPService.start()");
    for (int i = 0; i < connectionListeners.size(); i++) {
      connectionListeners.get(i).start();
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
    for (int i = 0; i < connectionListeners.size(); i++) {
      AMQPConnectionListener cnxListener = connectionListeners.get(i);
      cnxListener.stop();
      connectionListeners.remove(cnxListener);
    }
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "stopped connection listener");
  }

  public static void createConnectionListener() throws IOException {
    AMQPConnectionListener cnxListener = new AMQPConnectionListener(serverSocket, heartbeat);
    connectionListeners.add(cnxListener);
    cnxListener.start();
  }
  
  public static void removeConnectionListener(AMQPConnectionListener cnxListener) {
    connectionListeners.remove(cnxListener);
  }

  public static void addMessageListener(AMQPMessageListener messageListener) {
    messageListeners.add(messageListener);
  }

  public static void notifyMessageReceived(MessageReceived messageReceived) {
    for (int i = 0; i < messageListeners.size(); i++) {
      AMQPMessageListener messageListener = messageListeners.get(i);
      messageListener.onMessageReceived(messageReceived);
    }
  }
}
