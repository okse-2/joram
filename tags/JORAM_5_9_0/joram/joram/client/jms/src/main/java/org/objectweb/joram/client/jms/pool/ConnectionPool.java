/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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
package org.objectweb.joram.client.jms.pool;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.jms.JMSException;

/**
 * Implements a pool of connection resulting of a unique ConnectionFactory object.
 * Connections are sorted by identity.
 */
public class ConnectionPool {
  /** Maximum number of free connections for an identity in the pool */
  int maxFreeConnections = 10;

  /**
   * Gets the maximum number of free connections for an identity in the pool.
   * @return the maxFreeConnections
   */
  public int getMaxFreeConnections() {
    return maxFreeConnections;
  }

  /**
   * Returns the maximum number of free connections for an identity in the pool.
   * @param maxFreeConnections the maxFreeConnections to set
   */
  public void setMaxFreeConnections(int maxFreeConnections) {
    this.maxFreeConnections = maxFreeConnections;
  }
  
  /**
   * Creates a new pool of connections.
   * 
   * @param maxFreeConnections the maximum number of free connections for an identity in the pool.
   */
  public ConnectionPool(int maxFreeConnections) {
    this.maxFreeConnections = maxFreeConnections;
  }

  /** Map containing the lists of connections for each identity */
  private Map<ConnectionKey, LinkedList<PooledConnection>> cache = new HashMap<ConnectionKey, LinkedList<PooledConnection>>();

  /**
   * Allocates a Connection for the specified identity.
   * 
   * @param name      name of user.
   * @param password  password of user.
   * @return          A connection for the specified identity.
   */
  public PooledConnection alloc(String name, String password) {
    ConnectionKey key = new ConnectionKey(name, password);
    LinkedList<PooledConnection> pools = cache.get(key);
    
    if ((pools == null) || pools.isEmpty())
      return null;
    
    return pools.removeFirst();
  }
  
  /**
   * Frees the specified connection, depending of the number of free connections
   * in the pool this connection is closed or inserted in the pool.
   * 
   * @param cnx The connection to free.
   * @throws JMSException an error occurs during the cleanup of the connection. 
   */
  public void free(PooledConnection cnx) throws JMSException {
    ConnectionKey key = new ConnectionKey(cnx.name, cnx.password);
    LinkedList<PooledConnection> pools = cache.get(key);
    
    if (pools == null) {
      pools = new LinkedList<PooledConnection>();
      cache.put(key, pools);
    }

    if (pools.size() == maxFreeConnections) {
      // Really closes the connection ..
      cnx.delegate.close();
      // .. then let the gc work
      return;
    }

    pools.add(cnx);
  }
}
