/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2011 ScalAgent Distributed Technologies
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

import org.objectweb.joram.client.jms.FactoryParameters;
import org.objectweb.joram.client.jms.admin.AbstractConnectionFactory;
import org.objectweb.joram.client.jms.ha.local.XAHALocalConnectionFactory;
import org.objectweb.joram.client.jms.ha.tcp.XAHATcpConnectionFactory;
import org.objectweb.joram.client.jms.local.XALocalConnectionFactory;
import org.objectweb.joram.client.jms.tcp.XATcpConnectionFactory;
import org.objectweb.joram.shared.security.SimpleIdentity;
import org.objectweb.util.monolog.api.BasicLevel;

/**
 * A <code>ManagedConnectionFactoryImpl</code> instance manages
 * outbound connectivity to a given JORAM server.
 */
public class ManagedConnectionFactoryImpl
             implements javax.resource.spi.ManagedConnectionFactory,
                        javax.resource.spi.ResourceAdapterAssociation,
                        javax.resource.spi.ValidatingManagedConnectionFactory,
                        java.io.Serializable {
  /** Define serialVersionUID for interoperability. */
  private static final long serialVersionUID = 1L;

  /** Out stream for error logging and tracing. */
  protected transient PrintWriter out = null;

  /** Resource adapter central authority. */
  transient JoramAdapter ra = null;

  /** <code>true</code> for collocated outbound connectivity. */
  boolean collocated;

  /** <code>true</code> for ha mode */
  boolean isHa;

  /** Underlying JORAM server host name. */
  String hostName;
  /** Underlying JORAM server port number. */
  int serverPort;

  /** Default user identification. */
  String userName = "anonymous";
  /** Default user password. */
  String password = "anonymous";
  /** Default identityClass*/
  String identityClass = SimpleIdentity.class.getName();

  /**
   * Duration in seconds during which connecting is attempted (connecting
   * might take time if the server is temporarily not reachable); the 0 value
   * is set for connecting only once and aborting if connecting failed.
   */
  public int connectingTimer = 0;
  /**
   * Duration in seconds during which a JMS transacted (non XA) session might
   * be pending; above that duration the session is rolled back and closed;
   * the 0 value means "no timer".
   */
  public int txPendingTimer = 0;
  /**
   * Period in milliseconds between two ping requests sent by the client
   * connection to the server; if the server does not receive any ping
   * request during more than 2 * cnxPendingTimer, the connection is
   * considered as dead and processed as required.
   */
  public int cnxPendingTimer = 0;

  /**
   * Determines whether the produced messages are asynchronously
   * sent or not (without or with acknowledgement)
   * Default is false (with ack).
   */
  public boolean asyncSend;

  /**
   * Determines whether client threads
   * which are using the same connection
   * are synchronized
   * in order to group together the requests they
   * send.
   */
  public boolean multiThreadSync;

  /**
   * The maximum time the threads hang if 'multiThreadSync' is true.
   * Either they wake up (wait time out) or they are notified (by the
   * first woken up thread).
   *
   */
  public int multiThreadSyncDelay = -1;

  /**
   * This is the local IP address on which the TCP connection is activated. The
   * value can either be a machine name, such as "java.sun.com", or a textual
   * representation of its IP address.
   */
  public String outLocalAddress = null;

  /**
   * This is the local IP address port on which the TCP connection is activated
   */
  public int outLocalPort = 0;
  
  /**
   * Comma separated list of IN interceptors.
   */
  private String inInterceptors = null;

  /**
   * Comma separated list of OUT interceptors.
   */
  private String outInterceptors = null;

  /**
   * Constructs a <code>ManagedConnectionFactoryImpl</code> instance.
   */
  public ManagedConnectionFactoryImpl()
  {}

  public int getConnectingTimer() {
    return connectingTimer;
  }

  public int getCnxPendingTimer() {
    return cnxPendingTimer;
  }

  public int getTxPendingTimer() {
    return txPendingTimer;
  }

  public boolean isAsyncSend() {
    return asyncSend;
  }

  public boolean isMultiThreadSync() {
    return multiThreadSync;
  }

  public int getMultiThreadSyncDelay() {
    return multiThreadSyncDelay;
  }

  protected void setParameters(Object factory) {
    FactoryParameters fp = null;
    if (factory instanceof org.objectweb.joram.client.jms.ConnectionFactory) {
      org.objectweb.joram.client.jms.ConnectionFactory f =
        (org.objectweb.joram.client.jms.ConnectionFactory) factory;
      fp = f.getParameters();
    } else if (factory instanceof org.objectweb.joram.client.jms.XAConnectionFactory) {
      org.objectweb.joram.client.jms.XAConnectionFactory f =
        (org.objectweb.joram.client.jms.XAConnectionFactory) factory;
      fp = f.getParameters();
    }
    if (fp != null) {
      fp.connectingTimer = connectingTimer;
      fp.cnxPendingTimer = cnxPendingTimer;
      fp.txPendingTimer = txPendingTimer;
      if (asyncSend) {
        fp.asyncSend = asyncSend;
      }
      if (multiThreadSync) {
        fp.multiThreadSync = multiThreadSync;
      }
      if (multiThreadSyncDelay > 0) {
        fp.multiThreadSyncDelay = multiThreadSyncDelay;
      }
      if (outLocalPort > 0) {
        fp.outLocalPort = outLocalPort;
      }
      if (outLocalAddress != null) {
        fp.outLocalAddress = outLocalAddress;
      }
      if (inInterceptors != null) {
    	String[] interceptorArray = inInterceptors.split(",");
    	if (interceptorArray != null) {
    	  for (String interceptorClassName : interceptorArray) {
            String interceptorName = interceptorClassName.trim();
    		if (interceptorName.length() > 0) {
    		  fp.addInInterceptor(interceptorName);
    	    }
          }
        }
      }
      if (outInterceptors != null) {
        String[] interceptorArray = outInterceptors.split(",");
        if (interceptorArray != null) {
          for (String interceptorClassName : interceptorArray) {
            String interceptorName = interceptorClassName.trim();
            if (interceptorName.length() > 0) {
    		  fp.addOutInterceptor(interceptorName);
    		}
          }
        }
      }      
      
    }
  }

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
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, this + " createConnectionFactory(" + cxManager + ")");

    return new OutboundConnectionFactory(this, cxManager);
  }

  /**
   * Method called in the non managed case for creating an
   * <code>OutboundConnectionFactory</code> instance.
   *
   * @exception ResourceException  Never thrown.
   */
  public Object createConnectionFactory() throws ResourceException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, this + " createConnectionFactory()");

    OutboundConnectionFactory factory =
      new OutboundConnectionFactory(this, null);
    
    Reference ref =
      new Reference(factory.getClass().getName(),
                    "org.objectweb.joram.client.connector.ObjectFactoryImpl",
                    null);
    ref.add(new StringRefAddr("hostName", hostName));
    ref.add(new StringRefAddr("serverPort", "" + serverPort));
    ref.add(new StringRefAddr("userName", userName));
    ref.add(new StringRefAddr("password", password));
    ref.add(new StringRefAddr("identityClass", identityClass));

    factory.setReference(ref);
    return factory;
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
  public ManagedConnection
  createManagedConnection(Subject subject,
                          ConnectionRequestInfo cxRequest)
  throws ResourceException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG,
                                    this + " createManagedConnection(" + subject +
                                    ", " + cxRequest + ")");

    String userName;
    String password;
    String identityClass;

    String hostName = this.hostName;
    int serverPort = this.serverPort;

    // For XA recovery, connecting to the JORAM server with the default user
    // identity.
    if (cxRequest == null) {
      userName = this.userName;
      password = this.password;
      identityClass = this.identityClass;
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

    XAConnectionFactory factory;
    XAConnection cnx = null;

    if (collocated) {
      hostName = "localhost";
      serverPort = -1;
    }

    if (isHa) {
      if (collocated) {
        if (ra.haURL != null) {
          factory = XAHATcpConnectionFactory.create(ra.haURL);
        } else {
          factory = XAHALocalConnectionFactory.create();
        }
      } else {
        String urlHa = "hajoram://" + hostName + ":" + serverPort;
        factory = XAHATcpConnectionFactory.create(urlHa);
      }
    } else {
      if (collocated) {
        factory = XALocalConnectionFactory.create();
      } else {
        factory = XATcpConnectionFactory.create(hostName, serverPort);
      }
    }

    setParameters(factory);
    
    ((AbstractConnectionFactory) factory).setIdentityClassName(identityClass);

    try {
      cnx = factory.createXAConnection(userName, password);

      if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
        AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG,
                                      this + " createManagedConnection cnx = " + cnx);
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

    ManagedConnectionImpl managedCx = new ManagedConnectionImpl(ra,
                                                                cnx,
                                                                hostName,
                                                                serverPort,
                                                                userName);
    managedCx.setLogWriter(out);
    // for reconnection 
    managedCx.subject = subject;
    managedCx.cxRequest = cxRequest;
    

    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG,
                                    this + " createManagedConnection managedCx = " + managedCx);
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
  public ManagedConnection
  matchManagedConnections(Set connectionSet,
                          Subject subject,
                          ConnectionRequestInfo cxRequest)
  throws ResourceException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG,
                                    this + " matchManagedConnections(" + connectionSet +
                                    ", " + subject + ", " + cxRequest + ")");

    String userName;

    // No user identification provided, using the default one.
    String mode = "Unified";

    if (cxRequest == null)
      userName = this.userName;
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

    String hostName = this.hostName;
    int serverPort = this.serverPort;

    if (collocated) {
      hostName = "localhost";
      serverPort = -1;
    }


    while (! matching && it.hasNext()) {
      try {
        managedCx = (ManagedConnectionImpl) it.next();

        matching =
          managedCx.matches(hostName, serverPort, userName, mode);
      }
      catch (ClassCastException exc) {
      }
    }

    if (matching) {
      if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
        AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG,
                                      this + " matchManagedConnections managedCx = " + managedCx);
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
        + hostName
        + ":"
        + serverPort
        + "-"
        + userName).hashCode();
  }

  /** Compares managed factories according to their configuration. */
  public boolean equals(Object o) {
    if (! (o instanceof ManagedConnectionFactoryImpl)
        || o instanceof ManagedQueueConnectionFactoryImpl
        || o instanceof ManagedTopicConnectionFactoryImpl)
      return false;

    ManagedConnectionFactoryImpl other = (ManagedConnectionFactoryImpl) o;

    boolean res =
      hostName.equals(other.hostName)
      && serverPort == other.serverPort
      && userName.equals(other.userName);

    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG,
                                    this + " equals " + res);
    return res;
  }

  /** Returns the resource adapter central authority instance. */
  public ResourceAdapter getResourceAdapter() {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG,
                                    this + " getResourceAdapter() = " + ra);
    return ra;
  }

  /**
   * Sets the resource adapter central authority.
   *
   * @exception ResourceException  If the adapter could not be set.
   */
  public void setResourceAdapter(ResourceAdapter ra) throws ResourceException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG,
                                    this + " setResourceAdapter(" + ra + ")");

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
    collocated = this.ra.collocated;
    isHa = this.ra.isHa;
    hostName = this.ra.hostName;
    serverPort = this.ra.serverPort;
    connectingTimer = this.ra.connectingTimer;
    txPendingTimer = this.ra.txPendingTimer;
    cnxPendingTimer = this.ra.cnxPendingTimer;
    asyncSend = this.ra.asyncSend;
    multiThreadSync = this.ra.multiThreadSync;
    multiThreadSyncDelay = this.ra.multiThreadSyncDelay;

    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG,
                                    this + " setResourceAdapter collocated = " + collocated +
                                    ", isHa = " + isHa +
                                    ", hostName = " + hostName +
                                    ", serverPort = " + serverPort +
                                    ", connectingTimer = " + connectingTimer +
                                    ", txPendingTimer = " + txPendingTimer +
                                    ", cnxPendingTimer = " + cnxPendingTimer);
  }

  /**
   * From a set of managed connections, returns the set of invalid ones.
   */
  public Set getInvalidConnections(Set connectionSet) throws ResourceException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG,
                                    this + " getInvalidConnections(" + connectionSet + ")");

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

  /** Deserializing method. */
  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    in.defaultReadObject();
  }

  // ------------------------------------------
  // --- JavaBean setter and getter methods ---
  // ------------------------------------------

  public void setCollocated(Boolean collocated) {
    this.collocated = collocated.booleanValue();
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  public void setServerPort(Integer serverPort) {
    this.serverPort = serverPort.intValue();
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setIdentityClass(java.lang.String identityClass) {
    this.identityClass = identityClass;  
  }
  
  public java.lang.Boolean getCollocated() {
    return new Boolean(collocated);
  }

  public String getHostName() {
    return hostName;
  }

  public Integer getServerPort() {
    return new Integer(serverPort);
  }

  public String getUserName() {
    return userName;
  }

  public String getPassword() {
    return password;
  }
  
  public java.lang.String getIdentityClass() {
    return identityClass;
  }
  
  public java.lang.String getOutLocalAddress() {
    return outLocalAddress;
  }

  public Integer getOutLocalPort() {
    return new Integer(outLocalPort);
  }

  public void setOutLocalAddress(String outLocalAddress) {
    this.outLocalAddress = null;
    if ((outLocalAddress != null) && (outLocalAddress.length() > 0))
      this.outLocalAddress = outLocalAddress;
  }

  public void setOutLocalPort(Integer outLocalPort) {
    this.outLocalPort = outLocalPort.intValue();
  }

  public void setOutInterceptors(String outInterceptors) {
    this.outInterceptors = outInterceptors;
  }

  public void setInInterceptors(String inInterceptors) {
    this.inInterceptors = inInterceptors;
  }
}
