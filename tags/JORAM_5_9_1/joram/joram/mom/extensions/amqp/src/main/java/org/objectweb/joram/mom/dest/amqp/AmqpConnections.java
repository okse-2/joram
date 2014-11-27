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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Debug;

/**
 * The {@link AmqpConnections} service handles the list of known AMQP
 * servers, in order to keep live connections with them.
 */
public class AmqpConnections implements AmqpConnectionsMBean {

  private static final Logger logger = Debug.getLogger(AmqpConnections.class.getName());

  static final String SAVE_FILE_NAME = "Amqp_Cnx";

  private Map<String, LiveServerConnection> servers = new HashMap<String, LiveServerConnection>();

  AmqpConnections() {
  }

  void readSavedConf() {
    try {
      servers = (Map<String, LiveServerConnection>) AgentServer.getTransaction().load(SAVE_FILE_NAME);
      Iterator<LiveServerConnection> iter = servers.values().iterator();
      while (iter.hasNext()) {
        iter.next().startLiveConnection();
      }
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, "Error while loading persisted servers", exc);
      servers = new HashMap<String, LiveServerConnection>();
    }
  }

  /** {@inheritDoc} */
  public void addServer(String name, String host, int port) {
    addServer(name, host, port, null, null);
  }

  /** {@inheritDoc} */
  public void addServer(String name, String host, int port, String user, String pass) {
    synchronized (servers) {
      if (!servers.containsKey(name)) {
        LiveServerConnection cnx = new LiveServerConnection(name, host, port, user, pass);
        cnx.startLiveConnection();
        servers.put(name, cnx);
      }
    }
    try {
      if (! AgentServer.isEngineThread()) {
        AgentServer.getTransaction().begin();
      }
      AgentServer.getTransaction().save((HashMap) servers, SAVE_FILE_NAME);
      if (! AgentServer.isEngineThread()) {
        AgentServer.getTransaction().commit(true);
      }
    } catch (IOException exc) {
      logger.log(BasicLevel.ERROR, "Error while persisting new server " + name + ": " + host + ':' + port, exc);
    }
  }

  /** {@inheritDoc} */
  public void deleteServer(String name) {
    synchronized (servers) {
      LiveServerConnection cnx = servers.remove(name);
      if (cnx != null) {
        cnx.stopLiveConnection();
        try {
          if (! AgentServer.isEngineThread()) {
            AgentServer.getTransaction().begin();
          }
          AgentServer.getTransaction().save((HashMap) servers, SAVE_FILE_NAME);
          if (! AgentServer.isEngineThread()) {
            AgentServer.getTransaction().commit(true);
          }
        } catch (IOException exc) {
          logger.log(BasicLevel.ERROR, "Error while deleting server " + name, exc);
        }
      }
    }
  }

  /** {@inheritDoc} */
  public String[] getServerNames() {
    return servers.keySet().toArray(new String[servers.size()]);
  }

  /**
   * Gets the list of currently opened connections.
   * 
   * @return the list of usable connections.
   */
  public List<LiveServerConnection> getConnections() {
    List<LiveServerConnection> connections = new ArrayList<LiveServerConnection>();
    synchronized (servers) {
      for (LiveServerConnection server : servers.values()) {
        if (server.isConnectionOpen()) {
          connections.add(server);
        }
      }
    }
    return connections;
  }

  public void stop() {
    synchronized (servers) {
      for (LiveServerConnection server : servers.values()) {
        server.stopLiveConnection();
      }
    }
  }

}
