/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2008 ScalAgent Distributed Technologies
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
 */
package org.objectweb.joram.client.connector;

import java.util.Iterator;
import java.util.Set;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XAQueueConnectionFactory;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.resource.ResourceException;
import javax.resource.spi.CommException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.SecurityException;
import javax.security.auth.Subject;

import org.objectweb.joram.client.jms.admin.AbstractConnectionFactory;
import org.objectweb.joram.client.jms.ha.local.XAHALocalConnectionFactory;
import org.objectweb.joram.client.jms.ha.local.XAQueueHALocalConnectionFactory;
import org.objectweb.joram.client.jms.ha.tcp.XAHATcpConnectionFactory;
import org.objectweb.joram.client.jms.ha.tcp.XAQueueHATcpConnectionFactory;
import org.objectweb.joram.client.jms.local.XALocalConnectionFactory;
import org.objectweb.joram.client.jms.local.XAQueueLocalConnectionFactory;
import org.objectweb.joram.client.jms.tcp.XAQueueTcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.XATcpConnectionFactory;
import org.objectweb.util.monolog.api.BasicLevel;

/**
 * A <code>ManagedQueueConnectionFactoryImpl</code> instance manages
 * PTP outbound connectivity to a given JORAM server.
 */
public class ManagedQueueConnectionFactoryImpl extends ManagedConnectionFactoryImpl {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a <code>ManagedQueueConnectionFactoryImpl</code> instance.
   */
  public ManagedQueueConnectionFactoryImpl() {
  }

  /**
   * Method called by an application server (managed case) for creating an
   * <code>OutboundQueueConnectionFactory</code> instance.
   *
   * @param cxManager  Application server's connections pooling manager.
   *
   * @exception ResourceException  Never thrown.
   */
  public Object createConnectionFactory(ConnectionManager cxManager)
    throws ResourceException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, this + " createConnectionFactory(" + cxManager + ")");

    return new OutboundQueueConnectionFactory(this, cxManager);
  }

  /**
   * Method called in the non managed case for creating an
   * <code>OutboundQueueConnectionFactory</code> instance.
   *
   * @exception ResourceException  Never thrown.
   */
  public Object createConnectionFactory()
    throws ResourceException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, this + " createConnectionFactory()");

    OutboundConnectionFactory factory =
      new OutboundQueueConnectionFactory(this,
                                         DefaultConnectionManager.getRef());

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
   * Creates a new PTP physical connection to the underlying JORAM server,
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
    }
    else {
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

    XAConnection cnx = null;

    if (collocated) {
        hostName = "localhost";
        serverPort = -1;
    }

    try {

        if (isHa) {
          if (collocated) {
            if (ra.haURL != null) {
              if (cxRequest instanceof QueueConnectionRequest) {
                XAQueueConnectionFactory factory = XAQueueHATcpConnectionFactory.create(ra.haURL);
                setParameters(factory);
                ((AbstractConnectionFactory) factory).setIdentityClassName(identityClass);
                cnx = factory.createXAQueueConnection(userName, password);
              } else {
                XAConnectionFactory factory = XAHATcpConnectionFactory.create(ra.haURL);
                setParameters(factory);
                ((AbstractConnectionFactory) factory).setIdentityClassName(identityClass);
                cnx = factory.createXAConnection(userName, password);
              }
            } else {          
              if (cxRequest instanceof QueueConnectionRequest) {
                XAQueueConnectionFactory factory = XAQueueHALocalConnectionFactory.create();
                setParameters(factory);
                ((AbstractConnectionFactory) factory).setIdentityClassName(identityClass);
                cnx = factory.createXAQueueConnection(userName, password);
              } else {
                XAConnectionFactory factory = XAHALocalConnectionFactory.create();
                setParameters(factory);
                cnx = factory.createXAConnection(userName, password);
              }
            }
          } else {
            String urlHa = "hajoram://" + hostName + ":" + serverPort;
            if (cxRequest instanceof QueueConnectionRequest) {
              XAQueueConnectionFactory factory = XAQueueHATcpConnectionFactory.create(urlHa);
              setParameters(factory);
              ((AbstractConnectionFactory) factory).setIdentityClassName(identityClass);
              cnx = factory.createXAQueueConnection(userName, password);
            } else {
              XAConnectionFactory factory = XAHATcpConnectionFactory.create(urlHa);
              setParameters(factory);
              ((AbstractConnectionFactory) factory).setIdentityClassName(identityClass);
              cnx = factory.createXAConnection(userName, password);
            }
          }
        } else {
            if (collocated) {
                if (cxRequest instanceof QueueConnectionRequest) {
                    XAQueueConnectionFactory factory = XAQueueLocalConnectionFactory.create();
                    setParameters(factory);
                    ((AbstractConnectionFactory) factory).setIdentityClassName(identityClass);
                    cnx = factory.createXAQueueConnection(userName, password);
                } else {
                    XAConnectionFactory factory = XALocalConnectionFactory.create();
                    setParameters(factory);
                    ((AbstractConnectionFactory) factory).setIdentityClassName(identityClass);
                    cnx = factory.createXAConnection(userName, password);
                }
            } else {
                if (cxRequest instanceof QueueConnectionRequest) {
                    XAQueueConnectionFactory factory = XAQueueTcpConnectionFactory.create(hostName, serverPort);
                    setParameters(factory);
                    ((AbstractConnectionFactory) factory).setIdentityClassName(identityClass);
                    cnx = factory.createXAQueueConnection(userName, password);
                } else {
                    XAConnectionFactory factory = XATcpConnectionFactory.create(hostName, serverPort);
                    setParameters(factory);
                    ((AbstractConnectionFactory) factory).setIdentityClassName(identityClass);
                    cnx = factory.createXAConnection(userName, password);
                }
            }
        }

        if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
            AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, this + " createManagedConnection cnx = " + cnx);

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

    ManagedConnection managedCx = new ManagedConnectionImpl(ra,
                                                            cnx,
                                                            hostName,
                                                            serverPort,
                                                            userName);
    managedCx.setLogWriter(out);

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
  public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject,
      ConnectionRequestInfo cxRequest) throws ResourceException {

    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG,
                                    this + " matchManagedConnections(" + connectionSet +
                                    ", " + subject + ", " + cxRequest + ")");

    String userName;
    String mode = "Unified";

    // No user identification provided, using the default one.
    if (cxRequest == null)
      userName = this.userName;
    else {
      if (! (cxRequest instanceof ConnectionRequest)) {
          if (out != null)
              out.print("Provided ConnectionRequestInfo instance is not a JORAM object.");
        throw new ResourceException("Provided ConnectionRequestInfo instance "
                                    + "is not a JORAM object.");
      }

      userName = ((ConnectionRequest) cxRequest).getUserName();

      if (cxRequest instanceof QueueConnectionRequest)
        mode = "PTP";
    }

    String hostName = this.hostName;
    int serverPort = this.serverPort;

    if (collocated) {
        hostName = "localhost";
        serverPort = -1;
    }

    ManagedConnectionImpl managedCx = null;
    boolean matching = false;

    Iterator it = connectionSet.iterator();
    while (! matching && it.hasNext()) {
      try {
        managedCx = (ManagedConnectionImpl) it.next();
        matching = managedCx.matches(hostName, serverPort, userName, mode);
      }
      catch (ClassCastException exc) {}
    }

    if (matching) {
      if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
        AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG,
                                      this + " matchManagedConnections match " + managedCx);
      managedCx.setLogWriter(out);
      return managedCx;
    }
    return null;
  }

  /** Returns a code depending on the managed factory configuration. */
  public int hashCode() {
    return ("PTP:"
            + hostName
            + ":"
            + serverPort
            + "-"
            + userName).hashCode();
  }

  /** Compares managed factories according to their configuration. */
  public boolean equals(Object o) {
    if (! (o instanceof ManagedQueueConnectionFactoryImpl))
      return false;

    ManagedConnectionFactoryImpl other = (ManagedConnectionFactoryImpl) o;

    boolean res =
      hostName.equals(other.hostName)
      && serverPort == other.serverPort
      && userName.equals(other.userName);

    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG,
                                    this + " equals = " + res);
    return res;
  }
}
