/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
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
 * Initial developer(s): Frederic Maistre (Bull SA)
 * Contributor(s): Nicolas Tachker (Bull SA)
 */
package org.objectweb.joram.client.connector;

import javax.jms.*;
import javax.jms.IllegalStateException;
import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * An <code>OutboundConnection</code> instance is a handler for a physical
 * connection to an underlying JORAM server, allowing a component to
 * transparently use this physical connection possibly within a transaction
 * (local or global).
 */
public class OutboundConnection implements javax.jms.Connection
{
  /** The managed connection this "handle" belongs to. */
  ManagedConnectionImpl managedCx;
  /** The physical connection this "handle" handles. */
  XAConnection xac;
  /** <code>true</code> if this "handle" is valid. */
  boolean valid = true;
  /** Vector of the connection's sessions. */
  Vector sessions;
 
  /**
   * Constructs an <code>OutboundConnection</code> instance.
   *
   * @param managedCx  The managed connection building the handle.
   * @param xac        The underlying physical connection to handle.
   */
  OutboundConnection(ManagedConnectionImpl managedCx, 
                     XAConnection xac) {

    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                    "OutboundConnection(" + managedCx + 
                                    ", " + xac + ")");

    this.managedCx = managedCx;
    this.xac = xac;
    sessions = new Vector();
  }

  /**
   * Forbidden call on an application or component's outbound connection,
   * throws a <code>IllegalStateException</code> instance.
   */
  public void setClientID(String clientID) throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                    this + " setClientID(" + clientID + ")");
    if (! valid)
      throw new javax.jms.IllegalStateException("Invalid connection handle.");

    throw new IllegalStateException("Forbidden call on a component's "
                                    + "connection.");
  }

  /**
   * Forbidden call on an application or component's outbound connection,
   * throws a <code>IllegalStateException</code> instance.
   */
  public void setExceptionListener(ExceptionListener listener)
    throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                    this + " setExceptionListener(" + listener + ")");

    if (! valid)
      throw new javax.jms.IllegalStateException("Invalid connection handle.");

    throw new IllegalStateException("Forbidden call on a component's "
                                    + "connection.");
  }
 
  /**
   * Returns the unique authorized JMS session per connection wrapped
   * in an <code>OutboundSession</code> instance.
   *
   * @exception javax.jms.IllegalStateException  If the handle is invalid.
   * @exception javax.jms.JMSException           Generic exception.
   */
  public Session createSession(boolean transacted, int acknowledgeMode)
    throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                    this + " createSession(" + transacted + 
                                    ", " + acknowledgeMode + ")");

    if (! valid)
      throw new javax.jms.IllegalStateException("Invalid connection handle.");

    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                    this + " createSession sess = " + managedCx.session);

    Session sess = managedCx.session;
    if (sess == null)
      sess = xac.createSession(false, acknowledgeMode);

    return new OutboundSession(sess, this, transacted);
  }

  /**
   * Forbidden call on an application or component's outbound connection,
   * throws a <code>IllegalStateException</code> instance.
   */
  public String getClientID() throws JMSException
  {
    if (! valid)
      throw new javax.jms.IllegalStateException("Invalid connection handle.");

    throw new IllegalStateException("Forbidden call on a component's "
                                    + "connection.");
  }
  
  /**
   * Delegates the call to the wrapped JMS connection.
   *
   * @exception javax.jms.IllegalStateException  If the handle is invalid.
   * @exception javax.jms.JMSException           Generic exception.
   */
  public ConnectionMetaData getMetaData() throws JMSException
  {
    if (! valid)
      throw new javax.jms.IllegalStateException("Invalid connection handle.");

    return xac.getMetaData();
  }

  /**
   * Forbidden call on an application or component's outbound connection,
   * throws a <code>IllegalStateException</code> instance.
   */
  public ExceptionListener getExceptionListener() throws JMSException
  {
    if (! valid)
      throw new javax.jms.IllegalStateException("Invalid connection handle.");

    throw new IllegalStateException("Forbidden call on a component's "
                                    + "connection.");
  }
  
  /**
   * Delegates the call to the wrapped JMS connection.
   *
   * @exception javax.jms.IllegalStateException  If the handle is invalid.
   * @exception javax.jms.JMSException           Generic exception.
   */
  public void start() throws JMSException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, this + " start()");

    if (! valid)
      throw new javax.jms.IllegalStateException("Invalid connection handle.");

    xac.start();

    for (int i = 0; i < sessions.size(); i++) {
      OutboundSession session = (OutboundSession) sessions.get(i);
      session.start();

      if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
        AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, this + " start session = " + session);
    }
  }

  /**
   * Forbidden call on an application or component's outbound connection,
   * throws a <code>IllegalStateException</code> instance.
   */
  public void stop() throws JMSException {

    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, this + " stop()");

    if (! valid)
      throw new javax.jms.IllegalStateException("Invalid connection handle.");

    throw new IllegalStateException("Forbidden call on a component's "
                                    + "connection.");
  }

  /**
   * Forbidden call on an application or component's outbound connection,
   * throws a <code>IllegalStateException</code> instance.
   */
  public ConnectionConsumer
      createConnectionConsumer(Destination destination,
                               String messageSelector,
                               ServerSessionPool sessionPool,
                               int maxMessages)
    throws JMSException {
    if (! valid)
      throw new javax.jms.IllegalStateException("Invalid connection handle.");

    throw new IllegalStateException("Forbidden call on a component's "
                                    + "connection.");
  }

  /**
   * Forbidden call on an application or component's outbound connection,
   * throws a <code>IllegalStateException</code> instance.
   */
  public ConnectionConsumer
         createDurableConnectionConsumer(Topic topic,
                                         String subscriptionName,
                                         String messageSelector,
                                         ServerSessionPool sessionPool,
                                         int maxMessages)
         throws JMSException
  {
    if (! valid)
      throw new javax.jms.IllegalStateException("Invalid connection handle.");

    throw new IllegalStateException("Forbidden call on a component's "
                                    + "connection.");
  }

  /**
   * Requests to close the physical connection.
   *
   * @exception javax.jms.IllegalStateException  If the handle is invalid.
   * @exception javax.jms.JMSException           Generic exception.
   */
  public synchronized void close() throws JMSException {
    valid = false;

    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, this + " close()");

    for (int i = 0; i < sessions.size(); i++) {
      OutboundSession session = (OutboundSession) sessions.get(i);
      if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
        AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, this + " close() session = " + session);

      session.close();
    }

    managedCx.closeHandle(this);
  }

  /**
   *  returns <code>true</code> if the
   * parameter is a <code>Connection</code> instance sharing the same
   * proxy identifier and connection key.
   */
  public boolean cnxEquals(Object obj) {
    return (obj instanceof Connection)
           && xac.equals(obj);
  }

  /**
   * close all session.
   */
  public void cleanup() {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, this + " cleanup()");
    org.objectweb.joram.client.jms.Connection cnx = 
      (org.objectweb.joram.client.jms.Connection) xac;
    cnx.cleanup();
  }

  public String toString()
  {
    return "OutboundConnection[" + xac.toString() + "]";
  }
}
