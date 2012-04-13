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
 */
package org.objectweb.joram.client.connector;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XATopicConnectionFactory;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.resource.ResourceException;
import javax.resource.spi.CommException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.SecurityException;

import org.objectweb.joram.client.jms.ha.local.XAHALocalConnectionFactory;
import org.objectweb.joram.client.jms.ha.local.XATopicHALocalConnectionFactory;
import org.objectweb.joram.client.jms.ha.tcp.XAHATcpConnectionFactory;
import org.objectweb.joram.client.jms.ha.tcp.XATopicHATcpConnectionFactory;
import org.objectweb.joram.client.jms.local.XALocalConnectionFactory;
import org.objectweb.joram.client.jms.local.XATopicLocalConnectionFactory;
import org.objectweb.joram.client.jms.tcp.XATcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.XATopicTcpConnectionFactory;
import org.objectweb.util.monolog.api.BasicLevel;

/**
 * A <code>ManagedTopicConnectionFactoryImpl</code> instance manages
 * PubSub outbound connectivity to a given JORAM server.
 */
public class ManagedTopicConnectionFactoryImpl extends ManagedConnectionFactoryImpl {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a <code>ManagedTopicConnectionFactoryImpl</code> instance.
   */
  public ManagedTopicConnectionFactoryImpl() {
  }

  /**
   * Method called by an application server (managed case) for creating an
   * <code>OutboundTopicConnectionFactory</code> instance.
   *
   * @param cxManager  Application server's connections pooling manager.
   *
   * @exception ResourceException  Never thrown.
   */
  public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG,
                                    this + " createConnectionFactory(" + cxManager + ")");

    return new OutboundTopicConnectionFactory(this, cxManager);
  }

  /**
   * Method called in the non managed case for creating an
   * <code>OutboundTopicConnectionFactory</code> instance.
   *
   * @exception ResourceException  Never thrown.
   */
  public Object createConnectionFactory() throws ResourceException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, this + " createConnectionFactory()");

    OutboundConnectionFactory factory =
      new OutboundTopicConnectionFactory(this, DefaultConnectionManager.getRef());

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

  @Override
  protected XAConnectionFactory createFactory(ConnectionRequestInfo cxRequest) throws ResourceException {
  	XAConnectionFactory factory = null;

  	String hostName = getHostName();
  	int serverPort = getServerPort();
  	if (isCollocated()) {
  		hostName = "localhost";
  		serverPort = -1;
  	}

  	if (isHa()) {
  		if (isCollocated()) {
  			if (getHAURL() != null) {
  				if (cxRequest instanceof TopicConnectionRequest) {
  					factory = XATopicHATcpConnectionFactory.create(getHAURL());
  				} else {
  					factory = XAHATcpConnectionFactory.create(getHAURL());
  				}
  			} else {          
  				if (cxRequest instanceof TopicConnectionRequest) {
  					factory = XATopicHALocalConnectionFactory.create();
  				} else {
  					factory = XAHALocalConnectionFactory.create();
  				}
  			}
  		} else {
  			String urlHa = "hajoram://" + hostName + ":" + serverPort;
  			if (cxRequest instanceof TopicConnectionRequest) {
  				factory = XATopicHATcpConnectionFactory.create(urlHa);
  			} else {
  				factory = XAHATcpConnectionFactory.create(urlHa);
  			}
  		}
  	} else {
  		if (isCollocated()) {
  			if (cxRequest instanceof TopicConnectionRequest) {
  				factory = XATopicLocalConnectionFactory.create();
  			} else {
  				factory = XALocalConnectionFactory.create();
  			}
  		} else {
  			if (cxRequest instanceof TopicConnectionRequest) {
  				factory = XATopicTcpConnectionFactory.create(hostName, serverPort);
  			} else {
  				factory = XATcpConnectionFactory.create(hostName, serverPort);
  			}
  		}
  	}

  	if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
  		AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, this + " createFactory factory = " + factory);
  	
  	return factory;
  }

  @Override
  protected XAConnection createXAConnection(XAConnectionFactory factory, String userName, String password) throws ResourceException {
  	XAConnection cnx = null;
 	 try {
 		 if (factory instanceof XATopicConnectionFactory)
 			 cnx = ((XATopicConnectionFactory) factory).createXATopicConnection(userName, password);
 		 else
 			 cnx = factory.createXAConnection(userName, password);
      if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
        AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG,
                                      this + " createXAConnection cnx = " + cnx);
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

  /** Returns a code depending on the managed factory configuration. */
  public int hashCode() {
  	return ("PubSub:"
  			+ getHostName()
  			+ ":"
  			+ getServerPort()
  			+ "-"
  			+ getUserName()).hashCode();
  }

  /** Compares managed factories according to their configuration. */
  public boolean equals(Object o) {
    if (! (o instanceof ManagedTopicConnectionFactoryImpl))
      return false;

    ManagedConnectionFactoryImpl other = (ManagedConnectionFactoryImpl) o;
    return super.equals(o);
  }
}
