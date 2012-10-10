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
 */
package org.objectweb.joram.client.connector;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.naming.Reference;
import javax.resource.spi.ConnectionManager;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * An <code>OutboundConnectionFactory</code> instance is used for
 * getting a connection to an underlying JORAM server.
 */
public class OutboundConnectionFactory implements javax.jms.ConnectionFactory,
                                                  java.io.Serializable,
                                                  javax.resource.Referenceable {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  public static Logger logger = Debug.getLogger(OutboundConnectionFactory.class.getName());
  
  /** Central manager for outbound connectivity. */
  protected ManagedConnectionFactoryImpl mcf;
  /** Manager for connection pooling. */
  protected ConnectionManager cxManager;

  /** Naming reference of this instance. */
  protected Reference reference;


  /**
   * Constructs an <code>OutboundConnectionFactory</code> instance.
   *
   * @param mcf        Central manager for outbound connectivity.
   * @param cxManager  Manager for connection pooling.
   */
  OutboundConnectionFactory(ManagedConnectionFactoryImpl mcf,
                            ConnectionManager cxManager) {

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "OutboundConnectionFactory(" + mcf + 
                                    ", " + cxManager + ")");

    this.mcf = mcf;
    if (cxManager != null) {
      this.cxManager = cxManager;
    } else {
      this.cxManager = DefaultConnectionManager.getRef();
    }
  }


  /**
   * Requests a connection for the default user, eventually returns an
   * <code>OutboundConnection</code> instance.
   *
   * @exception JMSSecurityException   If connecting is not allowed.
   * @exception IllegalStateException  If the underlying JORAM server
   *                                   is not reachable.
   * @exception JMSException           Generic exception.
   */
  public javax.jms.Connection createConnection() 
    throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " createConnection()");

    return createConnection(mcf.getUserName(), mcf.getPassword());
  }

  /**
   * Requests a connection for a given user, eventually returns an
   * <code>OutboundConnection</code> instance.
   *
   * @exception JMSSecurityException   If connecting is not allowed.
   * @exception IllegalStateException  If the underlying JORAM server
   *                                   is not reachable.
   * @exception JMSException           Generic exception.
   */
  public javax.jms.Connection
      createConnection(String userName, String password)
    throws JMSException {

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " createConnection(" + userName + 
                                    ", " + password + ")");

    try {
      ConnectionRequest cxRequest = null;
      if (mcf instanceof ManagedQueueConnectionFactoryImpl)
        cxRequest = new QueueConnectionRequest(userName, password, mcf.getIdentityClass());
      else if (mcf instanceof ManagedTopicConnectionFactoryImpl)
        cxRequest = new TopicConnectionRequest(userName, password, mcf.getIdentityClass());
      else
        cxRequest = new ConnectionRequest(userName, password, mcf.getIdentityClass());

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, this + " createConnection cxManager = " + cxManager);

      Object o = cxManager.allocateConnection(mcf, cxRequest);

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, this + " createConnection connection = " + o);

      return (javax.jms.Connection) o;
    } catch (javax.resource.spi.SecurityException exc) {
      throw new JMSSecurityException("Invalid user identification: " + exc);
    } catch (javax.resource.spi.CommException exc) {
      throw new IllegalStateException("Could not connect to the JORAM server: "
                                      + exc);
    } catch (javax.resource.ResourceException exc) {
      throw new JMSException("Could not create connection: " + exc);
    }
  }

  /** Sets the naming reference of this factory. */
  public void setReference(Reference ref)
  {
    this.reference = ref;
  }

  /** Returns the naming reference of this factory. */
  public Reference getReference()
  {
    return reference;
  }
}
