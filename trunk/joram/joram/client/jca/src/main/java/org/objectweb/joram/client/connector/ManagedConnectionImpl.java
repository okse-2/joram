/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2012 ScalAgent Distributed Technologies
 * Copyright (C) 2004 Bull SA
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
 * Contributor(s): Nicolas Tachker (ScalAgent D.T.)
 *                 Florent Benoit (Bull SA)
 */
package org.objectweb.joram.client.connector;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Vector;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.XAConnection;
import javax.jms.XAQueueConnection;
import javax.jms.XASession;
import javax.jms.XATopicConnection;
import javax.resource.ResourceException;
import javax.resource.spi.CommException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.IllegalStateException;
import javax.resource.spi.LocalTransactionException;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * A <code>ManagedConnectionImpl</code> instance wraps a physical connection
 * to an underlying JORAM server, and provides "handles" for handling this
 * physical connection.
 */
public class ManagedConnectionImpl
             implements javax.resource.spi.ManagedConnection,
                        javax.resource.spi.LocalTransaction,
                        javax.jms.ExceptionListener {
  
  public static Logger logger = Debug.getLogger(ManagedConnectionImpl.class.getName());
  
  /** Central adapter authority. */
  private JoramResourceAdapter ra;
  /** Ref to the managed connection factory (use to reconnect) */
  ManagedConnectionFactoryImpl mcf;

  /** Physical connection to the JORAM server. */
  private XAConnection cnx = null;
  /** Vector of connection handles. */
  private Vector handles;
  /** Vector of condition event listeners. */
  private Vector listeners;
  /** <code>true</code> if a local transaction has been started. */
  private boolean startedLocalTx = false;
  /** Connection meta data. */
  private ManagedConnectionMetaDataImpl metaData = null;
  /** Out stream for error logging and tracing. */
  private PrintWriter out = null;

  /** <code>true</code> if the connection is valid. */
  private boolean valid = false;
  
  /** hashCode */
  private int hashCode = -1;

  /** Underlying JORAM server host name. */
  String hostName;
  /** Underlying JORAM server port number. */
  int serverPort;
  /** Messaging mode (PTP or PubSub or Unified). */
  String mode;
  /** User identification. */
  String userName;
  /**
   * Unique session for the use of managed components, involved in local or
   * distributed transactions.
   */
  Session session = null;
  
  /** only used for reconnection */
  Subject subject;
  /** only used for reconnection */
  ConnectionRequestInfo cxRequest;
  
  /** Use for reconnection synchronization */
  private Object lock = new Object();
  /** The waiting time in ms for reconnection. */
  private long timeWaitReconnect = 240000;

  /**
   * Creates a <code>ManagedConnectionImpl</code> instance wrapping a
   * physical connection to the underlying JORAM server.
   *
   * @param ra          Central adapter authority.
   * @param mcf         The Managed Connection Factory
   * @param cnx         Physical connection to the JORAM server.
   * @param hostName    JORAM server host name.
   * @param serverPort  JORAM server port number.
   * @param userName    User identification.
   */
  ManagedConnectionImpl(JoramResourceAdapter ra,
  		                  ManagedConnectionFactoryImpl mcf,
                        XAConnection cnx,
                        String hostName,
                        int serverPort,
                        String userName) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "ManagedConnectionImpl(" + ra +
                                    ", " + mcf +
                                    ", " + cnx +
                                    ", " + hostName +
                                    ", " + serverPort +
                                    ", " + userName + ")");

    this.ra = ra;
    this.mcf = mcf;
    this.cnx = cnx;
    this.hostName = hostName;
    this.serverPort = serverPort;
    this.userName = userName;

    if (cnx instanceof XAQueueConnection)
      mode = ManagedConnectionFactoryConfig.MODE_PTP;
    else if (cnx instanceof XATopicConnection)
      mode = ManagedConnectionFactoryConfig.MODE_PUBSUB;
    else
      mode = ManagedConnectionFactoryConfig.MODE_UNIFIED;

    try {
      cnx.setExceptionListener(this);
    } catch (JMSException exc) {}

    handles = new Vector();
    listeners = new Vector();

    valid = true;

    hashCode = -1;

    ra.addProducer(this);
    
    if (ra instanceof JoramAdapter) {
    	if (((JoramAdapter)ra).getConnectingTimer() > 0)
    		timeWaitReconnect = ((JoramAdapter)ra).getConnectingTimer() * 1000;
    }
  }


  /**
   * Returns a new <code>OutboundConnection</code> instance for handling the
   * physical connection.
   *
   * @exception CommException  If the wrapped physical connection is lost.
   */
  public Object getConnection(javax.security.auth.Subject subject,
                              ConnectionRequestInfo cxRequestInfo)
                throws ResourceException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " getConnection(" + subject + ", " + cxRequestInfo + ")");

    if (! isValid()) {
        if (out != null)
            out.print("Physical connection to the underlying JORAM server has been lost.");
      throw new CommException("Physical connection to the underlying JORAM server has been lost.");
    }

    OutboundConnection handle;

    if (cnx instanceof XAQueueConnection)
      handle = new OutboundQueueConnection(this, (XAQueueConnection) cnx);
    else if (cnx instanceof XATopicConnection)
      handle = new OutboundTopicConnection(this, (XATopicConnection) cnx);
    else
      handle = new OutboundConnection(this, cnx);

    handles.add(handle);

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " getConnection handles = " + handles);
    return handle;
  }

  /**
   * Dissociates a given connection handle and associates it to this
   * managed connection.
   *
   * @exception CommException      If the wrapped physical connection is lost.
   * @exception ResourceException  If the provided handle is invalid.
   */
  public void associateConnection(Object connection)
    throws ResourceException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " associateConnection(" + connection + ")");

    if (! isValid()) {
        if (out != null)
            out.print("Physical connection to the underlying JORAM server has been lost.");
      throw new CommException("Physical connection to the underlying JORAM server has been lost.");
    }

    if (! (connection instanceof OutboundConnection)) {
        if (out != null)
            out.print("The provided connection handle is not a JORAM handle.");
      throw new ResourceException("The provided connection handle is not a JORAM handle.");
    }

    OutboundConnection newConn = (OutboundConnection) connection;

    newConn.managedCx = this;
    newConn.xac = cnx;
  }

  /** Adds a connection event listener. */
  public void addConnectionEventListener(ConnectionEventListener listener) {
    listeners.add(listener);
  }

  /** Removes a connection event listener. */
  public void removeConnectionEventListener(ConnectionEventListener listener) {
    listeners.remove(listener);
  }

  /**
   * Provides a <code>XAResource</code> instance for managing distributed
   * transactions.
   *
   * @exception CommException                     If the physical connection
   *                                              is lost.
   * @exception IllegalStateException             If the managed connection is
   *                                              involved in a local
   *                                              transaction.
   * @exception ResourceAdapterInternalException  If the XA resource can't be
   *                                              retrieved.
   */
  public XAResource getXAResource() throws ResourceException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " getXAResource()");

    if (! isValid()) {
        if (out != null)
            out.print("Physical connection to the underlying JORAM server has been lost.");
      throw new CommException("Physical connection to the underlying JORAM server has been lost.");
    }

    try {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, this + " getXAResource session = " + session);

      if (session == null) {
        OutboundConnection outboundCnx = null;
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, this + " getXAResource handles = " + handles);
        for (java.util.Enumeration e = handles.elements(); e.hasMoreElements(); ) {
          outboundCnx = (OutboundConnection) e.nextElement();
          if (outboundCnx.cnxEquals(cnx)) {
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, this + " getXAResource : outboundCnx found in handles table.");
            break;
          }
        }

        if (outboundCnx == null)
          outboundCnx = (OutboundConnection) getConnection(null,null);

        if (outboundCnx != null) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, this + " getXAResource  outboundCnx = " + outboundCnx +
                "\n  outboundCnx.sess = " + outboundCnx.sessions);

          OutboundSession outboundSession = null;
          if (outboundCnx.sessions.size() > 0) {
            outboundSession = (OutboundSession) outboundCnx.sessions.get(0);
            
            if (!(outboundSession.sess instanceof XASession)) {
              if (logger.isLoggable(BasicLevel.DEBUG))
                logger.log(BasicLevel.DEBUG, this + " getXAResource  outboundSession.sess = " + outboundSession.sess);

              // getXARessourceManager (create by XAConnection)
              org.objectweb.joram.client.jms.XAResourceMngr xaResourceMngr = null;
              if (cnx instanceof org.objectweb.joram.client.jms.XAConnection) {
                xaResourceMngr = ((org.objectweb.joram.client.jms.XAConnection) cnx).getXAResourceMngr();
              } else if (cnx instanceof org.objectweb.joram.client.jms.XAQueueConnection) {
                xaResourceMngr = ((org.objectweb.joram.client.jms.XAQueueConnection) cnx).getXAResourceMngr();
              } else if (cnx instanceof org.objectweb.joram.client.jms.XATopicConnection) {
                xaResourceMngr = ((org.objectweb.joram.client.jms.XATopicConnection) cnx).getXAResourceMngr();
              }

              if (xaResourceMngr == null)
                xaResourceMngr = new org.objectweb.joram.client.jms.XAResourceMngr(
                    (org.objectweb.joram.client.jms.Connection) outboundCnx.xac);

              if (logger.isLoggable(BasicLevel.DEBUG))
                logger.log(BasicLevel.DEBUG, this + " getXAResource  xaResourceMngr = " + xaResourceMngr);

              org.objectweb.joram.client.jms.Session sess =
                  (org.objectweb.joram.client.jms.Session) outboundSession.sess;
              // set Session transacted = true
              sess.setTransacted(true);

              session = new org.objectweb.joram.client.jms.XASession(
                  (org.objectweb.joram.client.jms.Connection) outboundCnx.xac,
                  sess,
                  xaResourceMngr);

              if (logger.isLoggable(BasicLevel.DEBUG))
                logger.log(BasicLevel.DEBUG, this + " getXAResource  session = " + session +
                    "\noutboundSession.sess = " + outboundSession.sess + ", sess.class = " + outboundSession.sess.getClass().getName() + 
                    ", getAcknowledgeMode = " + outboundSession.getAcknowledgeMode() + ", getTransacted = " + outboundSession.getTransacted() + 
                    ", isStarted = " + outboundSession.isStarted());
            } else {
              // the outboundSession.sess is an instance of a XASession
              if (logger.isLoggable(BasicLevel.DEBUG))
                logger.log(BasicLevel.DEBUG, this + " getXAResource outboundSession.sess = " + outboundSession.sess + 
                    ", sess.class = " + outboundSession.sess.getClass().getName() + ", getAcknowledgeMode = " + outboundSession.getAcknowledgeMode() + 
                    ", getTransacted = " + outboundSession.getTransacted() + ", isStarted = " + outboundSession.isStarted());
            }
            
          } else {
            // No session available, create a new XASession.
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, this + " getXAResource createXASession");
             session = cnx.createXASession();

            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, this + " getXAResource session = " + session + ", session.class = " + session.getClass().getName() + 
                  ", getAcknowledgeMode = " + session.getAcknowledgeMode() + ", getTransacted = " + session.getTransacted());
          }
          
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, this + " getXAResource xaresource = " + ((org.objectweb.joram.client.jms.XASession) session).getXAResource() + ", transacted = " + ((org.objectweb.joram.client.jms.XASession) session).getTransacted());

        } else {
          // never arrived.
          if (logger.isLoggable(BasicLevel.WARN))
            logger.log(BasicLevel.WARN, this + " getXAResource  outboundCnx = null.");
        }
      } else if (session instanceof org.objectweb.joram.client.jms.XASession) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, this + " getXAResource session is XASession and not null");
        // set Session transacted = true
        ((org.objectweb.joram.client.jms.XASession)session).getDelegateSession().setTransacted(true);

        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, this + " getXAResource session = " + session + ", session.class = " + session.getClass().getName() + 
              ", getAcknowledgeMode = " + session.getAcknowledgeMode() + ", getTransacted = " + session.getTransacted());
        
        // TODO
        // cnx.sessions.add((org.objectweb.joram.client.jms.Session) session);
      } else if (! (session instanceof javax.jms.XASession)) {
          if (out != null)
              out.print("Managed connection not involved in a local transaction.");
        throw new IllegalStateException("Managed connection not involved "
                                        + "in a local transaction.");
      }


      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, this + " getXAResource  return = " + ((XASession) session).getXAResource());

      return ((XASession) session).getXAResource();
    } catch (JMSException exc) {
        if (out != null)
            out.print("Could not get XA resource: " + exc);
      throw new ResourceAdapterInternalException("Could not get XA resource: " + exc);
    }
  }

  /**
   * Returns this managed connection instance as a
   * <code>LocalTransaction</code> instance for managing local transactions.
   *
   * @exception CommException              If the physical connection is lost.
   * @exception IllegalStateException      If the managed connection is
   *                                       involved in a distributed
   *                                       transaction.
   * @exception LocalTransactionException  If the LocalTransaction resource
   *                                       can't be created.
   */
  public javax.resource.spi.LocalTransaction getLocalTransaction()
    throws ResourceException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " getLocalTransaction()");

    if (! isValid()) {
        if (out != null)
            out.print("Physical connection to the underlying JORAM server has been lost.");
      throw new CommException("Physical connection to the underlying JORAM server has been lost.");
    }
    try {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, this + " getLocalTransaction session = " + session);
      if (session == null)
        session = cnx.createSession(true, 0);
      else if (session instanceof javax.jms.XASession) {
          if (out != null)
              out.print("Managed connection already involved in a distributed transaction.");
        throw new IllegalStateException("Managed connection already involved in a distributed transaction.");
      }

      return this;
    } catch (JMSException exc) {
        if (out != null)
            out.print("Could not build underlying transacted JMS session: " + exc);
      throw new LocalTransactionException("Could not build underlying transacted JMS session: " + exc);
    }
  }

  /**
   * Returns the metadata information for the underlying JORAM server.
   *
   * @exception ResourceException  Never thrown.
   */
  public ManagedConnectionMetaData getMetaData() throws ResourceException {
    if (metaData == null)
      metaData = new ManagedConnectionMetaDataImpl(userName);

    return metaData;
  }

  /**
   * Sets the log writer for this <code>ManagedConnectionImpl</code>
   * instance.
   *
   * @exception ResourceException  Never thrown.
   */
  public void setLogWriter(PrintWriter out) throws ResourceException {
    this.out = out;
  }

  /**
   * Gets the log writer for this <code>ManagedConnectionImpl</code>
   * instance.
   *
   * @exception ResourceException  Never thrown.
   */
  public PrintWriter getLogWriter() throws ResourceException {
    return out;
  }

  /**
   * Remove the created handles and prepares the physical connection
   * to be put back into a connection pool.
   *
   * @exception ResourceException  Never thrown.
   */
  public synchronized void cleanup()
    throws ResourceException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " cleanup()");

    OutboundConnection handle;
    Iterator it = handles.iterator();
    while (it.hasNext()) {
      handle = (OutboundConnection) it.next();
      handle.cleanup();
    }
    session = null;
    try {
      // Clear the handles. 
      handles.clear();
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, e);
    }
  }

  /**
   * Destroys the physical connection to the underlying JORAM server.
   *
   * @exception ResourceException  Never thrown.
   */
  public synchronized void destroy()
    throws ResourceException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " destroy()");

    cleanup();

    try {
      cnx.close();
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, this + " destroy()", exc);
    }

    ra.removeProducer(this);

    valid = false;
  }

  /**
   * Returns a code based on the JORAM server and user identification
   * parameters.
   */
  public int hashCode() {
    if (hashCode == -1)
      hashCode = (mode  + ":" + hostName  + ":"  + ":" + serverPort + "-" + userName).hashCode();
    return hashCode;
  }

  /**
   * Compares <code>ManagedConnectionImpl</code> instances according to their
   * server and user identification parameters.
   */
  public boolean equals(Object o) {
    if (! (o instanceof ManagedConnectionImpl))
      return false;

    ManagedConnectionImpl other = (ManagedConnectionImpl) o;

    boolean res =
      mode.equals(other.mode)
      && ((hostName.equals(other.hostName) && serverPort == other.serverPort) ||
          (mcf.isCollocated() && other.serverPort == -1))
      && userName.equals(other.userName)
      && cnx.equals(other.cnx);

//    if (logger.isLoggable(BasicLevel.DEBUG))
//      logger.log(BasicLevel.DEBUG, this + " equals = " + res);

    return res;
  }

  /** Notifies that the wrapped physical connection has been lost. */
  public synchronized void onException(JMSException exc) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " onException(" + exc + ')');

    // Physical connection already invalid: doing nothing.
    if (! isValid())
      return;

    // Asynchronous JORAM exception does not notify of a connection loss:
    // doing nothing.
    if (! (exc instanceof javax.jms.IllegalStateException))
      return;

    ConnectionEvent event =
      new ConnectionEvent(this, ConnectionEvent.CONNECTION_ERROR_OCCURRED);

    ConnectionEventListener listener;
    for (int i = 0; i < listeners.size(); i++) {
      listener = (ConnectionEventListener) listeners.get(i);
      listener.connectionErrorOccurred(event);
    }

    try {
    	cleanup();
    } catch (ResourceException e) {
    	if (logger.isLoggable(BasicLevel.DEBUG))
    		logger.log(BasicLevel.DEBUG, this + " onException.cleanup exception " + e);
    }
    
    
    try {
    	if (logger.isLoggable(BasicLevel.DEBUG))
    		logger.log(BasicLevel.DEBUG, this + " onException: call ra.reconnect()");
    	ra.reconnect();
    	reconnect();
    } catch (Exception e) {
    }
  }

  void reconnect() {
  	if (logger.isLoggable(BasicLevel.DEBUG))
  		logger.log(BasicLevel.DEBUG, this + " reconnect(), ra.isActive() = " + ((JoramAdapter) ra).isActive());
  	try {
  		valid = true;
  		synchronized (lock) {
  			lock.notifyAll();
  		}
  	}	catch (Exception e) {
  		if (logger.isLoggable(BasicLevel.DEBUG))
  			logger.log(BasicLevel.DEBUG, this + " reconnect exception ", e);
  	}
  }

  boolean isReconnected() {
  	synchronized (lock) {
  		try {
  			lock.wait(timeWaitReconnect);
  			return true;
  		} catch (InterruptedException e) {
  		}
  	}
  	return false;
  }
  
  /**
   * Notifies that the local transaction is beginning.
   *
   * @exception CommException              If the wrapped physical connection
   *                                       is lost.
   * @exception LocalTransactionException  If a local transaction has already
   *                                       begun.
   */
  public synchronized void begin() throws ResourceException {

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " begin()");

    if (! isValid())
      throw new CommException("Physical connection to the underlying "
                              + "JORAM server has been lost.");

    if (startedLocalTx)
      throw new LocalTransactionException("Local transaction has "
                                          + "already begun.");

    ConnectionEvent event =
      new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_STARTED);

    ConnectionEventListener listener;
    for (int i = 0; i < listeners.size(); i++) {
      listener = (ConnectionEventListener) listeners.get(i);
      listener.localTransactionStarted(event);
    }

    startedLocalTx = true;
  }

  /**
   * Commits the local transaction.
   *
   * @exception CommException              If the wrapped physical connection
   *                                       is lost.
   * @exception LocalTransactionException  If the local transaction has not
   *                                       begun, or if the commit fails.
   */
  public synchronized void commit() throws ResourceException {

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " commit()");

    if (! isValid())
      throw new CommException("Physical connection to the underlying "
                              + "JORAM server has been lost.");

    if (! startedLocalTx)
      throw new LocalTransactionException("Local transaction has not begun.");

    try {
      session.commit();
    }
    catch (JMSException exc) {
      throw new LocalTransactionException("Commit of the transacted JMS "
                                          + "session failed: " + exc);
    }

    ConnectionEvent event =
      new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_COMMITTED);

    ConnectionEventListener listener;
    for (int i = 0; i < listeners.size(); i++) {
      listener = (ConnectionEventListener) listeners.get(i);
      listener.localTransactionCommitted(event);
    }

    startedLocalTx = false;
  }

  /**
   * Rollsback the local transaction.
   *
   * @exception CommException              If the wrapped physical connection
   *                                       is lost.
   * @exception LocalTransactionException  If the local transaction has not
   *                                       begun, or if the rollback fails.
   */
  public synchronized void rollback() throws ResourceException {

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " rollback()");

    if (! isValid())
      throw new CommException("Physical connection to the underlying JORAM server has been lost.");

    if (! startedLocalTx)
      throw new LocalTransactionException("Local transaction has not begun.");

    try {
      session.rollback();
    }
    catch (JMSException exc) {
      throw new LocalTransactionException("Rollback of the transacted JMS session failed: " + exc);
    }

    ConnectionEvent event =
      new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK);

    ConnectionEventListener listener;
    for (int i = 0; i < listeners.size(); i++) {
      listener = (ConnectionEventListener) listeners.get(i);
      listener.localTransactionRolledback(event);
    }

    startedLocalTx = false;
  }


  /**
   * Returns <code>true</code> if this managed connection matches given
   * parameters.
   */
  boolean matches(String hostName,
                  int serverPort,
                  String userName,
                  String mode) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "matches: " + hostName + " = " + this.hostName + ", " +
          serverPort + " = " + this.serverPort + ", " + userName + " = " + this.userName + ", " +
          mode + " = " + this.mode + ", isColocated = " + mcf.isCollocated());
      
    return ((this.hostName.equals(hostName)
           && this.serverPort == serverPort) ||
           (mcf.isCollocated()&& serverPort == -1))
           && this.userName.equals(userName)
           && this.mode.equals(mode);
  }

  /**
   * Returns <code>false</code> if the wrapped physical connection has been
   * lost or destroyed, <code>true</code> if it is still valid.
   */
  boolean isValid()
  {
    return valid;
  }

  /** Notifies of the closing of one of the connection handles. */
  void closeHandle(OutboundConnection handle) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " closeHandle(" + handle + ")");
    
    // remove handle from handles table.
    handles.remove(handle);

    ConnectionEvent event =
      new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
    event.setConnectionHandle(handle);

    ConnectionEventListener listener;
    for (int i = 0; i < listeners.size(); i++) {
      listener = (ConnectionEventListener) listeners.get(i);
      listener.connectionClosed(event);
    }
  }
}
