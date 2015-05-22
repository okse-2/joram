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

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;

/**
 * Implements a pooled connection.
 */
public class PooledConnection implements Connection {
  /** The pooled ConnectionFactory */
  PooledConnectionFactory pcf;
  /** The underlying connection to Joram's server */
  org.objectweb.joram.client.jms.Connection delegate;
  /** The identity of the authentified user for this connection */
  String name, password;
  
  /**
   * Creates a new pooled connection.
   * 
   * @param pcf       The pooled ConnectionFactory.
   * @param name      The name of the authentified user for this connection.
   * @param password  The password of the authentified user for this connection.
   * @throws JMSException An error occurs during the connection.
   */
  public PooledConnection(PooledConnectionFactory pcf, String name, String password) throws JMSException {
    this.pcf = pcf;
    this.name = name;
    this.password = password;
    this.delegate = (org.objectweb.joram.client.jms.Connection) pcf.cf.createConnection(name, password);
  }

  /**
   * Close the pooled connection, depending of the pool state this can results
   * in the real closing of the connection of the inserting in the pool of idle
   * connections.
   * 
   * @see javax.jms.Connection#close()
   */
  public void close() throws JMSException {
    // Cleans the connection, remove the ExceptionListener, stops it..
    delegate.cleanup();
    delegate.setExceptionListener(null);
    delegate.stop();
    // .. then inserts it in the pool.
    pcf.free(this);
  }

  /**
   * @see javax.jms.Connection#createConnectionConsumer(javax.jms.Destination, java.lang.String, javax.jms.ServerSessionPool, int)
   */
  public ConnectionConsumer createConnectionConsumer(Destination dest,
                                                     String selector,
                                                     ServerSessionPool sessionPool,
                                                     int maxMessages) throws JMSException {
    throw new IllegalStateException("Forbidden call on a PooledConnection.");
  }

  /**
   * @see javax.jms.Connection#createDurableConnectionConsumer(javax.jms.Topic, java.lang.String, java.lang.String, javax.jms.ServerSessionPool, int)
   */
  public ConnectionConsumer createDurableConnectionConsumer(Topic topic, 
                                                            String subName,
                                                            String selector,
                                                            ServerSessionPool sessPool,
                                                            int maxMessages) throws JMSException {
    throw new IllegalStateException("Forbidden call on a PooledConnection.");
  }

  /**
   * @see javax.jms.Connection#createSession(boolean, int)
   */
  public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
    return delegate.createSession(transacted, acknowledgeMode);
  }

  /**
   * @see javax.jms.Connection#getClientID()
   */
  public String getClientID() throws JMSException {
    return delegate.getClientID();
  }

  /**
   * @see javax.jms.Connection#getExceptionListener()
   */
  public ExceptionListener getExceptionListener() throws JMSException {
    return delegate.getExceptionListener();
  }

  /**
   * @see javax.jms.Connection#getMetaData()
   */
  public ConnectionMetaData getMetaData() throws JMSException {
    return delegate.getMetaData();
  }

  /**
   * @see javax.jms.Connection#setClientID(java.lang.String)
   */
  public void setClientID(String clientID) throws JMSException {
    delegate.setClientID(clientID);
  }

  /**
   * @see javax.jms.Connection#setExceptionListener(javax.jms.ExceptionListener)
   */
  public void setExceptionListener(ExceptionListener listener) throws JMSException {
    delegate.setExceptionListener(listener);
  }

  /**
   * @see javax.jms.Connection#start()
   */
  public void start() throws JMSException {
    delegate.start();
  }

  /**
   * @see javax.jms.Connection#stop()
   */
  public void stop() throws JMSException {
    delegate.stop();
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    StringBuffer strbuf = new StringBuffer();
    strbuf.append('(').append(super.toString());
    strbuf.append(",delegate=").append(delegate);
    strbuf.append(",name=").append(name);
    strbuf.append(",pcf=").append(pcf);
    strbuf.append("]");
    return strbuf.toString();
  }

  public Session createSession(int sessionMode) throws JMSException {
	  //TODO
	  throw new JMSException("not yet implemented.");
  }

  public Session createSession() throws JMSException {
	  //TODO
	  throw new JMSException("not yet implemented.");
  }

  public ConnectionConsumer createSharedConnectionConsumer(Topic topic,
		  String subscriptionName, String messageSelector,
		  ServerSessionPool sessionPool, int maxMessages) throws JMSException {
	  //TODO
	  throw new JMSException("not yet implemented.");
  }

  public ConnectionConsumer createSharedDurableConnectionConsumer(Topic topic,
		  String subscriptionName, String messageSelector,
		  ServerSessionPool sessionPool, int maxMessages) throws JMSException {
	  //TODO
	  throw new JMSException("not yet implemented.");
  }
}
