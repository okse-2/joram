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
package org.objectweb.joram.mom.dest.jms;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.util.management.MXWrapper;

public class JMSConnectionService {

  private static final Logger logger = Debug.getLogger(JMSConnectionService.class.getName());

  private static JMSConnections singleton;

  public synchronized static JMSConnections getInstance() {
    if (singleton == null) {
      singleton = new JMSConnections();
      try {
        MXWrapper.registerMBean(singleton, "JMS#" + AgentServer.getServerId(), "type=Connections");
      } catch (Exception e) {
        logger.log(BasicLevel.DEBUG, "registerMBean", e);
      }
    }
    return singleton;
  }

  public static void addServer(String cnxFactoryName) {
    getInstance().addServer(cnxFactoryName);
  }

  public static void addServer(String cnxFactoryName, String jndiFactoryClass, String jndiUrl) {
    getInstance().addServer(cnxFactoryName, jndiFactoryClass, jndiUrl);
  }

  public static void addServer(String cnxFactoryName, String jndiFactoryClass, String jndiUrl, String user,
      String password) {
    getInstance().addServer(cnxFactoryName, jndiFactoryClass, jndiUrl, user, password);
  }

  public static void addServer(String cnxFactoryName, String jndiFactoryClass, String jndiUrl, String user,
      String password, String clientID) {
    getInstance().addServer(cnxFactoryName, jndiFactoryClass, jndiUrl, user, password, clientID);
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
      String cf = "cf";
      String jndiFactoryClass = null;
      String jndiUrl = null;
      String user = null;
      String password = null;
      String clientID = null;
      if (args != null && args.length() > 0) {
        StringTokenizer st = new StringTokenizer(args);
        if (st.hasMoreTokens()) {
          cf = st.nextToken();
        }
        if (st.hasMoreTokens()) {
          jndiFactoryClass = st.nextToken();
        }
        if (st.hasMoreTokens()) {
          jndiUrl = st.nextToken();
        }
        if (st.hasMoreTokens()) {
          user = st.nextToken();
        }
        if (st.hasMoreTokens()) {
          password = st.nextToken();
        }
        if (st.hasMoreTokens()) {
          clientID = st.nextToken();
        }
        getInstance().addServer(cf, jndiFactoryClass, jndiUrl, user, password, clientID);
      }
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
