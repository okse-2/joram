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
 * The {@link JMSConnections} service handles the list of known AMQP
 * servers, in order to keep live connections with them.
 */
public class JMSConnections implements JMSConnectionsMBean {

  private static final Logger logger = Debug.getLogger(JMSConnections.class.getName());

  static final String SAVE_FILE_NAME = "Jms_Cnx";

  private Map<String, JMSModule> servers = new HashMap<String, JMSModule>();

  JMSConnections() {
  }

  void readSavedConf() {
    try {
      servers = (Map<String, JMSModule>) AgentServer.getTransaction().load(SAVE_FILE_NAME);
      Iterator<JMSModule> iter = servers.values().iterator();
      while (iter.hasNext()) {
        iter.next().startLiveConnection();
      }
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, "Error while loading persisted servers", exc);
      servers = new HashMap<String, JMSModule>();
    }
  }

  /** {@inheritDoc} */
  public void addServer(String cnxFactoryName) {
    addServer(cnxFactoryName, null, null);
  }

  /** {@inheritDoc} */
  public void addServer(String cnxFactoryName, String jndiFactoryClass, String jndiUrl) {
    addServer(cnxFactoryName, jndiFactoryClass, jndiUrl, null, null);
  }

  /** {@inheritDoc} */
  public void addServer(String cnxFactoryName, String jndiFactoryClass, String jndiUrl, String user,
      String password) {
    addServer(cnxFactoryName, jndiFactoryClass, jndiUrl, user, password, null);
  }

  /** {@inheritDoc} */
  public void addServer(String cnxFactoryName, String jndiFactoryClass, String jndiUrl, String user,
      String password, String clientID) {
    synchronized (servers) {
      if (!servers.containsKey(cnxFactoryName)) {
        JMSModule cnx = new JMSModule(cnxFactoryName, jndiFactoryClass, jndiUrl, user, password, clientID);
        cnx.startLiveConnection();
        servers.put(cnxFactoryName, cnx);
      }
    }
    try {
      if (Thread.currentThread() != AgentServer.getEngineThread()) {
        AgentServer.getTransaction().begin();
      }
      AgentServer.getTransaction().save((HashMap) servers, SAVE_FILE_NAME);
      if (Thread.currentThread() != AgentServer.getEngineThread()) {
        AgentServer.getTransaction().commit(true);
      }
    } catch (IOException exc) {
      logger.log(BasicLevel.ERROR, "Error while persisting new connection " + cnxFactoryName, exc);
    }
  }

  /** {@inheritDoc} */
  public void deleteServer(String name) {
    synchronized (servers) {
      JMSModule cnx = servers.remove(name);
      if (cnx != null) {
        cnx.stopLiveConnection();
        try {
          if (Thread.currentThread() != AgentServer.getEngineThread()) {
            AgentServer.getTransaction().begin();
          }
          AgentServer.getTransaction().save((HashMap) servers, SAVE_FILE_NAME);
          if (Thread.currentThread() != AgentServer.getEngineThread()) {
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
  public List<JMSModule> getConnections() {
    List<JMSModule> connections = new ArrayList<JMSModule>();
    synchronized (servers) {
      for (JMSModule server : servers.values()) {
        if (server.isConnectionOpen()) {
          connections.add(server);
        }
      }
    }
    return connections;
  }

  public void stop() {
    synchronized (servers) {
      for (JMSModule server : servers.values()) {
        server.stopLiveConnection();
      }
    }
  }

}
