/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 - 2012 ScalAgent Distributed Technologies
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

  static final String SAVE_FILE_NAME = "JmsCnx";

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
  public void addServer(String name, String cnxFactoryName) {
    addServer(name, cnxFactoryName, null, null);
  }

  /** {@inheritDoc} */
  public void addServer(String name, String cnxFactoryName, String jndiFactoryClass, String jndiUrl) {
    addServer(name, cnxFactoryName, jndiFactoryClass, jndiUrl, null, null);
  }

  /** {@inheritDoc} */
  public void addServer(String name, String cnxFactoryName, String jndiFactoryClass, String jndiUrl, String user,
      String password) {
    addServer(name, cnxFactoryName, jndiFactoryClass, jndiUrl, user, password, null);
  }

  /** {@inheritDoc} */
  public void addServer(String name,
                        String cnxFactoryName,
                        String jndiFactoryClass,
                        String jndiUrl,
                        String user,
                        String password,
                        String clientID) {
  	if (logger.isLoggable(BasicLevel.DEBUG)) {
			logger.log(BasicLevel.DEBUG, "JMSConnection.addServer(" + name + ", " + 
					cnxFactoryName + ", " + jndiFactoryClass + ", " + jndiUrl + ", " + user + ", ****, " + clientID + ')');
  	}
  	
    if (name == null) {
      int i = 0;
      do {
        name = "cnx" + i; i += 1;
      } while (servers.containsKey(name));
      logger.log(BasicLevel.WARN,
                 "Cannot add an unamed JMSConnection, set name to \"" + name  + "\".");
    }
			
    synchronized (servers) {
      if (!servers.containsKey(name)) {
        JMSModule cnx = new JMSModule(name, cnxFactoryName, jndiFactoryClass, jndiUrl, user, password, clientID);
        cnx.startLiveConnection();
        servers.put(name, cnx);
      } else {
        logger.log(BasicLevel.ERROR,
                   "Cannot add a JMSConnection with an already defined name: " + name  + ".");
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
      logger.log(BasicLevel.ERROR, "Error while persisting new connection " + cnxFactoryName, exc);
    }
  }

  /** {@inheritDoc} */
  public void deleteServer(String name) {
  	if (logger.isLoggable(BasicLevel.DEBUG)) {
			logger.log(BasicLevel.DEBUG, "JMSConnection.deleteServer(" + name + ')');
  	}
    synchronized (servers) {
      JMSModule cnx = servers.remove(name);
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
          logger.log(BasicLevel.ERROR, "Error while deleting connection: " + name, exc);
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
