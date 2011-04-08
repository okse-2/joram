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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rabbitmq.client.ConnectionFactory;

/**
 * The {@link AmqpConnections} service handles the list of known AMQP
 * servers, in order to keep live connections with them.
 */
public class AmqpConnections implements AmqpConnectionsMBean {

  private Map<String, LiveServerConnection> servers = new HashMap<String, LiveServerConnection>();

  AmqpConnections() {
  }

  /**
   * Adds an AMQP server and starts a live connection with it, accessible via
   * the {@link ConnectionFactory} provided. A server is uniquely identified
   * by the given name. Adding an existing server won't do anything.
   * 
   * @param name the name identifying the server
   * @param factory the factory used to access the server, configured properly
   *          (host, port, login, password...)
   */
  public void addServer(String name, ConnectionFactory factory) {
    synchronized (servers) {
      if (!servers.containsKey(name)) {
        servers.put(name, new LiveServerConnection(name, factory));
      }
    }
  }

  /** {@inheritDoc} */
  public void addServer(String name, String host, int port) {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(host);
    factory.setPort(port);
    addServer(name, factory);
  }

  /** {@inheritDoc} */
  public void addServer(String name, String host, int port, String user, String pass) {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(host);
    factory.setPort(port);
    factory.setUsername(user);
    factory.setPassword(pass);
    addServer(name, factory);
  }

  /** {@inheritDoc} */
  public void deleteServer(String name) {
    synchronized (servers) {
      LiveServerConnection cnx = servers.remove(name);
      if (cnx != null) {
        cnx.stopLiveConnection();
      }
    }
  }

  public String[] getConnectionNames() {
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
