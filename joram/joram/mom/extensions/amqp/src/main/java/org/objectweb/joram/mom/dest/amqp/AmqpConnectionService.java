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
import java.util.List;
import java.util.StringTokenizer;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import com.rabbitmq.client.ConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.util.management.MXWrapper;

public class AmqpConnectionService {

  private static final Logger logger = Debug.getLogger(AmqpConnectionService.class.getName());

  private static AmqpConnections singleton;

  public synchronized static AmqpConnections getInstance() {
    if (singleton == null) {
      singleton = new AmqpConnections();
      try {
        MXWrapper.registerMBean(singleton, "AMQP#" + AgentServer.getServerId(), "type=Connections");
      } catch (Exception e) {
        logger.log(BasicLevel.DEBUG, "registerMBean", e);
      }
    }
    return singleton;
  }

  /**
   * Adds an AMQP server and starts a live connection with it, accessible via
   * the host and port provided. A server is uniquely identified by the given
   * name. Adding an existing server won't do anything.
   * 
   * @param name the name identifying the server
   * @param host host of the added server
   * @param port port of the added server
   */
  public static void addServer(String name, String host, int port) {
    getInstance().addServer(name, host, port);
  }

  /**
   * Adds an AMQP server and starts a live connection with it, accessible via
   * the host and port provided. A server is uniquely identified by the given
   * name. Adding an existing server won't do anything.
   * 
   * @param name the name identifying the server
   * @param host host of the added server
   * @param port port of the added server
   * @param user user name
   * @param pass user password
   */
  public static void addServer(String name, String host, int port, String user, String pass) {
    getInstance().addServer(name, host, port, user, pass);
  }

  /**
   * Removes the live connection to the specified AMQP server.
   * 
   * @param name the name identifying the server
   */
  public static void deleteServer(String name) {
    getInstance().deleteServer(name);
  }

  /**
   * Gets the list of known servers.
   */
  public static String[] getServerNames() {
    return getInstance().getServerNames();
  }

  /**
   * Initializes the service. Starts a connection with one server.
   */
  public static void init(String args, boolean firstTime) throws Exception {
    if (firstTime) {
      String host = ConnectionFactory.DEFAULT_HOST;
      int port = ConnectionFactory.DEFAULT_AMQP_PORT;
      String name = "default";
      if (args != null) {
        StringTokenizer st = new StringTokenizer(args);
        if (st.hasMoreTokens()) {
          host = st.nextToken();
        }
        if (st.hasMoreTokens()) {
          port = Integer.parseInt(st.nextToken());
        }
        if (st.hasMoreTokens()) {
          name = st.nextToken();
        }
      }
      getInstance().addServer(name, host, port);
    } else {
      getInstance().readSavedConf();
    }

  }

  /**
   * Stops all connections to AMQP servers.
   */
  public static void stopService() {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Stopping AmqpConnectionHandler service.");
    }
    getInstance().stop();
  }

  public static List<String> convertToList(final String value) {
    String[] values = value.split(",");
    List<String> injection = null;

    // We should have at least 1 real value
    if (!((values.length == 1) && ("".equals(values[0])))) {
      injection = new ArrayList<String>();
      for (int i = 0; i < values.length; i++) {
        String part = values[i];
        injection.add(part.trim());
      }
    }
    return injection;
  }

}
