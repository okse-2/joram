/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.dest.amqp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 * The {@link AmqpConnectionHandler} service handles the list of known AMQP
 * servers, in order to keep live connections with them.
 */
public class AmqpConnectionHandler implements AmqpConnectionHandlerMBean {

  private static final Logger logger = Debug.getLogger(AmqpConnectionHandler.class.getName());

  private static final String MBEAN_NAME = "type=Connections";

  private static AmqpConnectionHandler singleton;

  private static Set<LiveServerConnection> servers = new HashSet<LiveServerConnection>();

  private AmqpConnectionHandler() {
  }

  public synchronized static AmqpConnectionHandler getInstance() {
    if (singleton == null) {
      singleton = new AmqpConnectionHandler();
      try {
        MXWrapper.registerMBean(singleton, "AMQP#" + AgentServer.getServerId(), MBEAN_NAME);
      } catch (Exception e) {
        logger.log(BasicLevel.DEBUG, "registerMBean", e);
      }
    }
    return singleton;
  }

  /**
   * Initializes the service. Starts a connection with one server.
   */
  public static void init(String args, boolean firstTime) throws Exception {

    String host = ConnectionFactory.DEFAULT_HOST;
    int port = ConnectionFactory.DEFAULT_AMQP_PORT;
    if (args != null) {
      StringTokenizer st = new StringTokenizer(args);
      if (st.hasMoreTokens()) {
        host = st.nextToken();
      }
      if (st.hasMoreTokens()) {
        port = Integer.parseInt(st.nextToken());
      }
    }

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(host);
    factory.setPort(port);

    getInstance().addServer(factory);

  }

  /**
   * Adds an AMQP server and starts a live connection with it, accessible via
   * the {@link ConnectionFactory} provided. A server is uniquely identified
   * with its host and port. Adding an existing server won't do anything.
   * 
   * @param factory the factory used to access the server, configured properly
   *          (host, port, login, password...)
   */
  public void addServer(ConnectionFactory factory) {
    synchronized (servers) {
      servers.add(new LiveServerConnection(factory));
    }
  }

  /**
   * Adds an AMQP server and starts a live connection with it, accessible via
   * the {@link ConnectionFactory} provided. A server is uniquely identified
   * with its host and port. Adding an existing server won't do anything.
   * 
   * @param host host of the added server
   * @param port port of the added server
   */
  public void addServer(String host, int port) {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(host);
    factory.setPort(port);
    addServer(factory);
  }

  /**
   * Adds an AMQP server and starts a live connection with it, accessible via
   * the {@link ConnectionFactory} provided. A server is uniquely identified
   * with its host and port. Adding an existing server won't do anything.
   * 
   * @param host host of the added server
   * @param port port of the added server
   * @param user user name
   * @param pass user password
   */
  public void addServer(String host, int port, String user, String pass) {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(host);
    factory.setPort(port);
    factory.setUsername(user);
    factory.setPassword(pass);
    addServer(factory);
  }

  /**
   * Removes the live connection to the specified AMQP server.
   * 
   * @param host host of the removed server
   * @param port port of the removed server
   */
  public void deleteServer(String host, int port) {
    synchronized (servers) {
      Iterator<LiveServerConnection> iterator = servers.iterator();
      while (iterator.hasNext()) {
        LiveServerConnection cnx = iterator.next();
        if (cnx.getConnectionFactory().getHost().equals(host) && cnx.getConnectionFactory().getPort() == port) {
          cnx.stopLiveConnection();
          iterator.remove();
          break;
        }
      }
    }
  }

  /**
   * Stops all connections to AMQP servers.
   */
  public static void stopService() {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Stopping AmqpConnectionHandler service.");
    }
    synchronized (servers) {
      for (LiveServerConnection server : servers) {
        server.stopLiveConnection();
      }
    }
  }

  /**
   * Gets the list of currently opened connections.
   * 
   * @return the list of usable connections.
   */
  public static List<Connection> getConnections() {
    List<Connection> connections = new ArrayList<Connection>();
    synchronized (servers) {
      for (LiveServerConnection server : servers) {
        if (server.isConnectionOpen()) {
          connections.add(server.getConnection());
        }
      }
    }
    return connections;
  }

}
