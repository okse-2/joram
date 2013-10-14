/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 - 2013 ScalAgent Distributed Technologies
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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.JMSSecurityException;
import javax.jms.JMSSecurityRuntimeException;

import org.objectweb.joram.client.jms.JMSContext;

/**
 * A ConnectionFactory which pools Connection for reuse.
 */
public class PooledConnectionFactory implements ConnectionFactory {
  // TODO (AF): Periodic cleanup of idle connections.
  
  /** The underlying ConnectionFactory */
  org.objectweb.joram.client.jms.ConnectionFactory cf;

  /**
   * Returns the underlying ConnectionFactory used to create the connections.
   * This ConnectionFactory object allows to configure the created connections.
   * 
   * @return the underlying ConnectionFactory.
   */
  public org.objectweb.joram.client.jms.ConnectionFactory getConnectionFactory() {
    return cf;
  }

  /** The pool of connections */
  ConnectionPool pool;
  
  /** The maximum number of free connections for an identity in the pool. */
  int maxFreeConnections;
  
  /**
   * Returns the maximum number of free connections for an identity in the pool.
   * @return The maximum number of free connections in the pool.
   */
  public int getMaxFreeConnections() {
    return maxFreeConnections;
  }

  /**
   * Sets the maximum number of free connections for an identity in the pool.
   * @param maxFreeConnections the maximum number of free connections to set
   */
  public void setMaxFreeConnections(int maxFreeConnections) {
    this.maxFreeConnections = maxFreeConnections;
  }

  /**
   * Creates a new pool for the specified ConnectionFactory.
   * 
   * @param cf  The ConnectionFactory used to really create the connections.
   */
  public PooledConnectionFactory(ConnectionFactory cf) {
    this(cf, 10);
  }
  
  /**
   * Creates a new pool for the specified ConnectionFactory.
   * 
   * @param cf  The ConnectionFactory used to really create the connections.
   * @param maxFreeConnections The maximum number of free connections for an identity in the pool.
   */
  public PooledConnectionFactory(ConnectionFactory cf, int maxFreeConnections) {
    try {
      this.cf = (org.objectweb.joram.client.jms.ConnectionFactory) cf;
      this.maxFreeConnections = maxFreeConnections;
    } catch (ClassCastException cce) {
      throw new IllegalArgumentException("Only Joram ConnectionFactory can be pooled");
    }
  }

  /**
   * API method, creates a connection with the default user identity.
   * The connection is created in stopped mode.
   *
   * @return  a newly created connection.
   * 
   * @see javax.jms.ConnectionFactory#createConnection()
   * @exception JMSSecurityException  If the default identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public Connection createConnection() throws JMSException {
    return createConnection(cf.getDefaultLogin(), cf.getDefaultPassword());
  }

  /**
   * API method, creates a connection with the specified user identity.
   * The connection is created in stopped mode.
   *
   * @param name      the caller's user name.
   * @param password  the caller's password.
   * @return          a newly created connection.
   * 
   * @see javax.jms.ConnectionFactory#createConnection(String, String)
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public synchronized Connection createConnection(String name,
                                                  String password) throws JMSException {
    if (pool == null)
      pool = createConnectionPool(maxFreeConnections);
    
    PooledConnection cnx = pool.alloc(name, password);
    
    if (cnx == null) {
      cnx = new PooledConnection(this, name, password);
    }
 
    return cnx;
  }
  
  /**
   * @param cnx
   * @throws JMSException
   */
  synchronized void free(PooledConnection cnx) throws JMSException {
    pool.free(cnx);
  }
  
  /**
   * @param maxFreeConnections The maximum number of free connections for an identity in the pool.
   * @return The 
   */
  protected ConnectionPool createConnectionPool(int maxFreeConnections) {
    return new ConnectionPool(maxFreeConnections);
  }

  public javax.jms.JMSContext createContext() {
    try {
      return new JMSContext((Connection) createConnection());
    } catch (JMSSecurityException e) {
      throw new JMSSecurityRuntimeException("Unable to create JMSContext", e.getMessage(), e);
    } catch (JMSException e) {
      throw new JMSRuntimeException ("Unable to create JMSContext", e.getMessage(), e);
    }
  }

  public javax.jms.JMSContext createContext(String userName, String password) {
    try {
      return new JMSContext((Connection) createConnection(userName, password));
    } catch (JMSSecurityException e) {
      throw new JMSSecurityRuntimeException("Unable to create JMSContext", e.getMessage(), e);
    } catch (JMSException e) {
      throw new JMSRuntimeException ("Unable to create JMSContext", e.getMessage(), e);
    }
  }

  public javax.jms.JMSContext createContext(String userName, String password, int sessionMode) {
    try {
      return new JMSContext((Connection) createConnection(userName, password), sessionMode);
    } catch (JMSSecurityException e) {
      throw new JMSSecurityRuntimeException("Unable to create JMSContext", e.getMessage(), e);
    } catch (JMSException e) {
      throw new JMSRuntimeException ("Unable to create JMSContext", e.getMessage(), e);
    }
  }

  public javax.jms.JMSContext createContext(int sessionMode) {
    try {
      return new JMSContext((Connection) createConnection(), sessionMode);
    } catch (JMSSecurityException e) {
      throw new JMSSecurityRuntimeException("Unable to create JMSContext", e.getMessage(), e);
    } catch (JMSException e) {
      throw new JMSRuntimeException ("Unable to create JMSContext", e.getMessage(), e);
    }
  }
}

