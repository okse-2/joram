/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
 * Copyright (C) 2004 - 2012 ScalAgent Distributed Technologies
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

import java.util.Vector;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.XAConnection;
import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 * An <code>OutboundConnection</code> instance is a handler for a physical
 * connection to an underlying JORAM server, allowing a component to
 * transparently use this physical connection possibly within a transaction
 * (local or global).
 */
public class OutboundConnection implements Connection, OutboundConnectionMBean {
  
  public static Logger logger = Debug.getLogger(OutboundConnection.class.getName());
  
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
  OutboundConnection(ManagedConnectionImpl managedCx, XAConnection xac) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "OutboundConnection(" + managedCx + ", " + xac + ")");

    this.managedCx = managedCx;
    this.xac = xac;
    sessions = new Vector();
    
    registerMBean();
  }

  /**
   * Forbidden call on an application or component's outbound connection,
   * throws a <code>IllegalStateException</code> instance.
   */
  public void setClientID(String clientID) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " setClientID(" + clientID + ")");
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
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " setExceptionListener(" + listener + ")");

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
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " createSession(" + transacted + 
                                    ", " + acknowledgeMode + ")");

    if (! valid)
      throw new javax.jms.IllegalStateException("Invalid connection handle.");

    if (logger.isLoggable(BasicLevel.DEBUG))
    	logger.log(BasicLevel.DEBUG, this + " createSession managedCx.session = " + managedCx.session);

    Session sess = managedCx.session;
    if (sess == null) {
    	try {
    		sess = xac.createSession(false, acknowledgeMode);
    	} catch (IllegalStateException e) {
    		if (logger.isLoggable(BasicLevel.DEBUG))
    			logger.log(BasicLevel.DEBUG, this + " createSession (IllegalStateException)" + e);
    		logger.log(BasicLevel.WARN, this + " createSession reconnection in progress...");
    		try {
    			if (managedCx.isReconnected()) {
    				ManagedConnection mc = managedCx.mcf.createManagedConnection(managedCx.subject, managedCx.cxRequest);
    				OutboundConnection outboundConnection = (OutboundConnection) mc.getConnection(managedCx.subject, managedCx.cxRequest);
    				outboundConnection.managedCx.associateConnection(this);
    			} else {
    				if (logger.isLoggable(BasicLevel.DEBUG))
    					logger.log(BasicLevel.DEBUG, this + " createSession : managed connection is not reconnected.");
    				throw new JMSException(this + " createSession : managed connection is not reconnected.");
    			}
    			sess = xac.createSession(false, acknowledgeMode);
    		} catch (ResourceException exc) {
    			if (logger.isLoggable(BasicLevel.WARN))
    				logger.log(BasicLevel.WARN, this + " createSession (ResourceException)", exc);
    		}
    	}
    	if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, this + " createSession sess = " + sess);
    }

    return new OutboundSession(sess, this, transacted);
  }

  /**
   * Forbidden call on an application or component's outbound connection,
   * throws a <code>IllegalStateException</code> instance.
   */
  public String getClientID() throws JMSException {
    if (! valid)
      throw new javax.jms.IllegalStateException("Invalid connection handle.");

    throw new IllegalStateException("Forbidden call on a component's connection.");
  }
  
  /**
   * Delegates the call to the wrapped JMS connection.
   *
   * @exception javax.jms.IllegalStateException  If the handle is invalid.
   * @exception javax.jms.JMSException           Generic exception.
   */
  public ConnectionMetaData getMetaData() throws JMSException {
    if (! valid)
      throw new javax.jms.IllegalStateException("Invalid connection handle.");

    return xac.getMetaData();
  }

  /**
   * Forbidden call on an application or component's outbound connection,
   * throws a <code>IllegalStateException</code> instance.
   */
  public ExceptionListener getExceptionListener() throws JMSException {
    if (! valid)
      throw new javax.jms.IllegalStateException("Invalid connection handle.");

    throw new IllegalStateException("Forbidden call on a component's connection.");
  }
  
  /**
   * Delegates the call to the wrapped JMS connection.
   *
   * @exception javax.jms.IllegalStateException  If the handle is invalid.
   * @exception javax.jms.JMSException           Generic exception.
   */
  public void start() throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " start()");

    if (! valid)
      throw new javax.jms.IllegalStateException("Invalid connection handle.");

    xac.start();

    for (int i = 0; i < sessions.size(); i++) {
      OutboundSession session = (OutboundSession) sessions.get(i);
      session.start();

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, this + " start session = " + session);
    }
  }

  /**
   * Forbidden call on an application or component's outbound connection,
   * throws a <code>IllegalStateException</code> instance.
   */
  public void stop() throws JMSException {

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " stop()");

    if (! valid)
      throw new javax.jms.IllegalStateException("Invalid connection handle.");

    throw new IllegalStateException("Forbidden call on a component's connection.");
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

    throw new IllegalStateException("Forbidden call on a component's connection.");
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
         throws JMSException {
    if (! valid)
      throw new javax.jms.IllegalStateException("Invalid connection handle.");

    throw new IllegalStateException("Forbidden call on a component's connection.");
  }

  /**
   * Requests to close the physical connection.
   *
   * @exception javax.jms.IllegalStateException  If the handle is invalid.
   * @exception javax.jms.JMSException           Generic exception.
   */
  public synchronized void close() throws JMSException {
    valid = false;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " close()");

    for (int i = 0; i < sessions.size(); i++) {
      OutboundSession session = (OutboundSession) sessions.get(i);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, this + " close() session = " + session);

      session.close();
    }

    managedCx.closeHandle(this);
    
    unregisterMBean();
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
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " cleanup()");
    org.objectweb.joram.client.jms.Connection cnx = 
      (org.objectweb.joram.client.jms.Connection) xac;
    cnx.cleanup();
    
    // unregister the OutboundConnection mbean created by ManagedConnectionImpl.getConnection(null,null).
    unregisterMBean();
  }

  public String toString() {
    return "OutboundConnection[" + xac.toString() + "]@" + hashCode();
  }
  
  public String getJMXBeanName(XAConnection cnx) {
    if (! (cnx instanceof org.objectweb.joram.client.jms.Connection)) return null;
    StringBuffer buf = new StringBuffer();
    buf.append(((org.objectweb.joram.client.jms.Connection) cnx).getJMXBeanName());
    buf.append(",location=OutboundConnection");
    buf.append(",OutboundConnection=").append("OutboundConnection@").append(hashCode());
    return buf.toString();
  }

  public String registerMBean() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "OutboundConnection.registerMBean: " + this);
    String JMXBeanName = getJMXBeanName(xac);
    try {
      if (JMXBeanName != null)
        MXWrapper.registerMBean(this, JMXBeanName);
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "OutboundConnection.registerMBean: " + JMXBeanName, e);
    }

    return JMXBeanName;
  }

  public void unregisterMBean() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "OutboundConnection.unregisterMBean: " + this);
    try {
      MXWrapper.unregisterMBean(getJMXBeanName(xac));
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "OutboundConnection.unregisterMBean: " + getJMXBeanName(xac), e);
    }
  }

  public int getNumberOfSession() {
    return sessions.size();
  }

  public String[] getSessions() {
    String[] sessTab = new String[sessions.size()];
    for (int i = 0; i < sessions.size(); i++) {
      OutboundSession outboundSess = (OutboundSession) sessions.get(i);
      try {
        sessTab[i] = outboundSess.sess.toString();
      } catch (Exception e) {
        sessTab[i] = "unknown";
      }
    }
    return sessTab;
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
