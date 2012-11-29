/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2012 ScalAgent Distributed Technologies
 * Copyright (C) 2004 - 2006 Bull SA
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
 * Contributor(s): ScalAgent Distributed Technologies
 *                 Benoit Pelletier (Bull SA)
 *                 Florent Benoit (Bull SAS)
 */
package org.objectweb.joram.client.connector;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.resource.ResourceException;
import javax.resource.spi.CommException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.SecurityException;
import javax.security.auth.Subject;

import org.objectweb.joram.client.jms.admin.AbstractConnectionFactory;
import org.objectweb.joram.client.jms.local.XALocalConnectionFactory;
import org.objectweb.joram.client.jms.tcp.XATcpConnectionFactory;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * A <code>ManagedConnectionFactoryImpl</code> instance manages
 * outbound connectivity to a given JORAM server.
 */
public class ManagedConnectionFactoryImpl extends ManagedConnectionFactoryConfig
             implements javax.resource.spi.ManagedConnectionFactory,
                        javax.resource.spi.ResourceAdapterAssociation,
                        javax.resource.spi.ValidatingManagedConnectionFactory,
                        java.io.Serializable {
  
  public static Logger logger = Debug.getLogger(ManagedConnectionFactoryImpl.class.getName());
  
  /** Define serialVersionUID for interoperability. */
  private static final long serialVersionUID = 1L;

  /** Out stream for error logging and tracing. */
  protected transient PrintWriter out = null;

  /** Resource adapter central authority. */
  transient JoramResourceAdapter ra = null;

  /**
   * Constructs a <code>ManagedConnectionFactoryImpl</code> instance.
   */
  public ManagedConnectionFactoryImpl() {}

  /**
   * Method called by an application server (managed case) for creating an
   * <code>OutboundConnectionFactory</code> instance.
   *
   * @param cxManager  Application server's connections pooling manager.
   *
   * @exception ResourceException  Never thrown.
   */
  public Object createConnectionFactory(ConnectionManager cxManager)
  throws ResourceException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " createConnectionFactory(" + cxManager + ")");

    return new OutboundConnectionFactory(this, cxManager);
  }

  /**
   * Method called in the non managed case for creating an
   * <code>OutboundConnectionFactory</code> instance.
   *
   * @exception ResourceException  Never thrown.
   */
  public Object createConnectionFactory() throws ResourceException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " createConnectionFactory()");

    OutboundConnectionFactory factory =
      new OutboundConnectionFactory(this, null);
    
    Reference ref =
      new Reference(factory.getClass().getName(),
                    "org.objectweb.joram.client.connector.ObjectFactoryImpl",
                    null);
    ref.add(new StringRefAddr("hostName", getHostName()));
    ref.add(new StringRefAddr("serverPort", "" + getServerPort()));
    ref.add(new StringRefAddr("userName", getUserName()));
    ref.add(new StringRefAddr("password", getPassword()));
    ref.add(new StringRefAddr("identityClass", getIdentityClass()));

    factory.setReference(ref);
    return factory;
  }

  protected XAConnectionFactory createFactory(ConnectionRequestInfo cxRequest) throws ResourceException {
  	XAConnectionFactory factory = null;

  	String hostName = getHostName();
  	int serverPort = getServerPort();
//  	if (isCollocated()) {
//  		hostName = "localhost";
//  		serverPort = -1;
//  	}
  	if (logger.isLoggable(BasicLevel.DEBUG))
  	  logger.log(BasicLevel.DEBUG, this + " createFactory hostName = " + hostName + ", serverPort = " + serverPort);

  	if (isCollocated()) {
  	  factory = XALocalConnectionFactory.create();
  	} else {
  	  factory = XATcpConnectionFactory.create(hostName, serverPort);
  	}

  	((AbstractConnectionFactory) factory).setCnxJMXBeanBaseName(ra.jmxRootName+"#"+ra.getName());

  	return factory;
  }
  
  protected XAConnection createXAConnection(XAConnectionFactory factory, String userName, String password) throws ResourceException {
  	XAConnection cnx = null;
  	 try {
       cnx = factory.createXAConnection(userName, password);

       if (logger.isLoggable(BasicLevel.DEBUG))
         logger.log(BasicLevel.DEBUG, this + " createManagedConnection cnx = " + cnx);
     } catch (IllegalStateException exc) {
       if (out != null)
         out.print("Could not access the JORAM server: " + exc);
       throw new CommException("Could not access the JORAM server: " + exc);
     } catch (JMSSecurityException exc) {
       if (out != null)
         out.print("Invalid user identification: " + exc);
       throw new SecurityException("Invalid user identification: " + exc);
     } catch (JMSException exc) {
       if (out != null)
         out.print("Failed connecting process: " + exc);
       throw new ResourceException("Failed connecting process: " + exc);
     }
     return cnx;
  }
  
  /**
   * Creates a new physical connection to the underlying JORAM server,
   * and returns a <code>ManagedConnectionImpl</code> instance for a
   * managed environment.
   *
   * @param subject        Security data, not taken into account.
   * @param cxRequest      User identification data, may be <code>null</code>.
   *
   * @exception CommException          If the JORAM server is not reachable.
   * @exception SecurityException      If the connecting is not allowed.
   * @exception IllegalStateException  If the central Joram adapter state is
   *                                    invalid.
   * @exception ResourceException      If the provided user info is invalid,
   *                                   or if connecting fails for any other
   *                                   reason.
   */
  public final ManagedConnection
  createManagedConnection(Subject subject, ConnectionRequestInfo cxRequest)
  throws ResourceException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " createManagedConnection(" + subject + ", " + cxRequest + ")");

    String userName;
    String password;
    String identityClass;

    String hostName = getHostName();
    int serverPort = getServerPort();

    // For XA recovery, connecting to the JORAM server with the default user
    // identity.
    if (cxRequest == null) {
      userName = getUserName();
      password = getPassword();
      identityClass = getIdentityClass();
    } else {
      if (! (cxRequest instanceof ConnectionRequest)) {
        if (out != null)
          out.print("Provided ConnectionRequestInfo instance is not a JORAM object.");
        throw new ResourceException("Provided ConnectionRequestInfo instance "
                                    + "is not a JORAM object.");
      }

      userName = ((ConnectionRequest) cxRequest).getUserName();
      password = ((ConnectionRequest) cxRequest).getPassword();
      identityClass = ((ConnectionRequest) cxRequest).getIdentityClass();
    }

    XAConnectionFactory factory = createFactory(cxRequest);
    setParameters(factory);
    ((AbstractConnectionFactory) factory).setIdentityClassName(identityClass);
    
    XAConnection cnx = createXAConnection(factory, userName, password);

    ManagedConnectionImpl managedCx = new ManagedConnectionImpl(ra,
    		                                                        this,
                                                                cnx,
                                                                hostName,
                                                                serverPort,
                                                                userName);
    managedCx.setLogWriter(out);
    // for reconnection 
    managedCx.subject = subject;
    managedCx.cxRequest = cxRequest;
    

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " createManagedConnection managedCx = " + managedCx);
    return managedCx;
  }

  /**
   * Finds a matching connection from the candidate set of connections and
   * returns a <code>ManagedConnectionImpl</code> instance.
   *
   * @param connectionSet  Set of connections to test.
   * @param subject        Security data, not taken into account.
   * @param cxRequest      User identification data, may be <code>null</code>.
   *
   * @exception ResourceException  If the provided connection request info is
   *                               invalid.
   */
  public final ManagedConnection
  matchManagedConnections(Set connectionSet,
                          Subject subject,
                          ConnectionRequestInfo cxRequest)
  throws ResourceException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " matchManagedConnections(" + connectionSet + ", " + subject + ", " + cxRequest + ")");

    String userName;

    // No user identification provided, using the default one.
    String mode = "Unified";

    if (cxRequest == null)
      userName = getUserName();
    else {
      if (! (cxRequest instanceof ConnectionRequest)) {
        out.print("Provided ConnectionRequestInfo instance is not a JORAM object.");
        throw new ResourceException("Provided ConnectionRequestInfo instance "
                                    + "is not a JORAM object.");
      }

      if (cxRequest instanceof QueueConnectionRequest)
        mode = "PTP";
      else if (cxRequest instanceof TopicConnectionRequest)
        mode = "PubSub";

      userName = ((ConnectionRequest) cxRequest).getUserName();
    }

    ManagedConnectionImpl managedCx = null;
    boolean matching = false;

    Iterator it = connectionSet.iterator();

    String hostName = getHostName();
    int serverPort = getServerPort();

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " hostName = " +  hostName + ", serverPort = " + serverPort);
    if (isCollocated()) {
      hostName = "localhost";
      serverPort = -1;
    }

    while (! matching && it.hasNext()) {
      try {
        managedCx = (ManagedConnectionImpl) it.next();
        matching = managedCx.matches(hostName, serverPort, userName, mode);
      }
      catch (ClassCastException exc) {
      }
    }

    if (matching) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, this + " matchManagedConnections managedCx = " + managedCx);
      managedCx.setLogWriter(out);
      return managedCx;
    }

    return null;
  }



  /**
   * Sets the log writer for this <code>ManagedConnectionFactoryImpl</code>
   * instance.
   */
  public void setLogWriter(PrintWriter out) throws ResourceException {
    this.out = out;
  }

  /**
   * Gets the log writer of this <code>ManagedConnectionFactoryImpl</code>
   * instance.
   */
  public PrintWriter getLogWriter() throws ResourceException {
    return out;
  }

  /** Returns a code depending on the managed factory configuration. */
  public int hashCode() {
    return ("Unified:"
        + getHostName()
        + ":"
        + getServerPort()
        + "-"
        + getUserName()).hashCode();
  }

  /** Compares managed factories according to their configuration. */
  public boolean equals(Object o) {
    if (! (o instanceof ManagedConnectionFactoryImpl)
        || o instanceof ManagedQueueConnectionFactoryImpl
        || o instanceof ManagedTopicConnectionFactoryImpl)
      return false;

    ManagedConnectionFactoryImpl other = (ManagedConnectionFactoryImpl) o;

    boolean res =
    	getHostName().equals(other.getHostName())
      && getServerPort() == other.getServerPort()
      && getUserName().equals(other.getUserName());

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " equals " + res);
    return res;
  }

  /** Returns the resource adapter central authority instance. */
  public ResourceAdapter getResourceAdapter() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " getResourceAdapter() = " + ra);
    return ra;
  }

  /**
   * Sets the resource adapter central authority.
   *
   * @exception ResourceException  If the adapter could not be set.
   */
  public void setResourceAdapter(ResourceAdapter ra) throws ResourceException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " setResourceAdapter(" + ra + ")");

    if (this.ra != null) {
      out.print("ResourceAdapter instance already associated.");
      throw new javax.resource.spi.IllegalStateException("ResourceAdapter "
                                                         + "instance "
                                                         + "already "
                                                         + "associated.");
    }

    if (! (ra instanceof JoramAdapter)) {
      out.print("Provided ResourceAdapter is not a JORAM ResourceAdapter object: "
                + ra.getClass().getName());
      throw new ResourceException("Provided ResourceAdapter is not a JORAM "
                                  + "ResourceAdapter object: "
                                  + ra.getClass().getName());
    }

    this.ra = (JoramAdapter) ra;
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " setResourceAdapter isCollocated = " + ((JoramAdapter) ra).collocated + ", serverPort = " + ((JoramAdapter) ra).getServerPort());
    
    if (((JoramAdapter) ra).collocated) {
      if (getServerPort() < 0) {
        setServerPort(((JoramAdapter) ra).getServerPort());
      }
    }
    
// TODO remove
//    collocated = this.ra.collocated;
//    isHa = this.ra.isHa;
//    hostName = this.ra.hostName;
//    serverPort = this.ra.serverPort;
//    connectingTimer = this.ra.connectingTimer;
//    txPendingTimer = this.ra.txPendingTimer;
//    cnxPendingTimer = this.ra.cnxPendingTimer;
//    asyncSend = this.ra.asyncSend;
//    multiThreadSync = this.ra.multiThreadSync;
//    multiThreadSyncDelay = this.ra.multiThreadSyncDelay;
//
//    if (logger.isLoggable(BasicLevel.DEBUG))
//      logger.log(BasicLevel.DEBUG,
//                                    this + " setResourceAdapter collocated = " + collocated +
//                                    ", isHa = " + isHa +
//                                    ", hostName = " + hostName +
//                                    ", serverPort = " + serverPort +
//                                    ", connectingTimer = " + connectingTimer +
//                                    ", txPendingTimer = " + txPendingTimer +
//                                    ", cnxPendingTimer = " + cnxPendingTimer);
  }

  /**
   * From a set of managed connections, returns the set of invalid ones.
   */
  public Set getInvalidConnections(Set connectionSet) throws ResourceException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " getInvalidConnections(" + connectionSet + ")");

    Iterator it = connectionSet.iterator();
    ManagedConnectionImpl managedCx;

    java.util.HashSet invalidConnections = new java.util.HashSet();
    while (it.hasNext()) {
      try {
        managedCx = (ManagedConnectionImpl) it.next();
        if (!managedCx.isValid())
          invalidConnections.add(managedCx);
      }
      catch (ClassCastException exc) {}
    }

    return invalidConnections; 
  }

//  /** Deserializing method. */
//  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
//    in.defaultReadObject();
//  }
}
