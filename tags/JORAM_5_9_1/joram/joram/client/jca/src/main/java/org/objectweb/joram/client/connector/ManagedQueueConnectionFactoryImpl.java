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
import javax.jms.XAQueueConnectionFactory;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.resource.ResourceException;
import javax.resource.spi.CommException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.SecurityException;

import org.objectweb.joram.client.jms.admin.AbstractConnectionFactory;
import org.objectweb.joram.client.jms.local.XALocalConnectionFactory;
import org.objectweb.joram.client.jms.local.XAQueueLocalConnectionFactory;
import org.objectweb.joram.client.jms.tcp.XAQueueTcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.XATcpConnectionFactory;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * A <code>ManagedQueueConnectionFactoryImpl</code> instance manages
 * PTP outbound connectivity to a given JORAM server.
 */
public class ManagedQueueConnectionFactoryImpl extends ManagedConnectionFactoryImpl {

  private static final long serialVersionUID = 1L;
  
  public static Logger logger = Debug.getLogger(ManagedQueueConnectionFactoryImpl.class.getName());

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
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " createConnectionFactory(" + cxManager + ")");

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
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " createConnectionFactory()");

    OutboundConnectionFactory factory =
      new OutboundQueueConnectionFactory(this, DefaultConnectionManager.getRef());

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
//  	if (isCollocated()) {
//  		hostName = "localhost";
//  		serverPort = -1;
//  	}

  	if (isCollocated()) {
  	  if (cxRequest instanceof QueueConnectionRequest) {
  	    factory = XAQueueLocalConnectionFactory.create();
  	  } else {
  	    factory = XALocalConnectionFactory.create();
  	  }
  	} else {
  	  if (cxRequest instanceof QueueConnectionRequest) {
  	    factory = XAQueueTcpConnectionFactory.create(hostName, serverPort);
  	  } else {
  	    factory = XATcpConnectionFactory.create(hostName, serverPort);
  	  }
  	}
  	
  	((AbstractConnectionFactory) factory).setCnxJMXBeanBaseName(ra.jmxRootName+"#"+ra.getName());

  	if (logger.isLoggable(BasicLevel.DEBUG))
  		logger.log(BasicLevel.DEBUG, this + " createFactory factory = " + factory);
  	
  	return factory;
  }

  @Override
  protected XAConnection createXAConnection(XAConnectionFactory factory, String userName, String password) throws ResourceException {
  	XAConnection cnx = null;
 	 try {
 		 if (factory instanceof XAQueueConnectionFactory)
 			 cnx = ((XAQueueConnectionFactory) factory).createXAQueueConnection(userName, password);
 		 else
 			 cnx = factory.createXAConnection(userName, password);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, this + " createXAConnection cnx = " + cnx);
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
  	return ("PTP:"
  			+ getHostName()
  			+ ":"
  			+ getServerPort()
  			+ "-"
  			+ getUserName()).hashCode();
  }

  /** Compares managed factories according to their configuration. */
  public boolean equals(Object o) {
    if (! (o instanceof ManagedQueueConnectionFactoryImpl))
      return false;

    return super.equals(o);
  }
}
