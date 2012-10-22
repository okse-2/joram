/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 - 2012 ScalAgent Distributed Technologies
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

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.resource.spi.ConnectionManager;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * An <code>OutboundQueueConnectionFactory</code> instance is used for
 * getting a PTP connection to an underlying JORAM server.
 */
public class OutboundQueueConnectionFactory extends OutboundConnectionFactory implements
    javax.jms.QueueConnectionFactory {
  
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  public static Logger logger = Debug.getLogger(OutboundQueueConnectionFactory.class.getName());

  /**
   * Constructs an <code>OutboundQueueConnectionFactory</code> instance.
   *
   * @param mcf        Central manager for outbound connectivity.
   * @param cxManager  Manager for connection pooling.
   */
  OutboundQueueConnectionFactory(ManagedConnectionFactoryImpl mcf,
                                 ConnectionManager cxManager) {
    super(mcf, cxManager);

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "OutboundQueueConnectionFactory(" + mcf + ", " + cxManager + ")");
  }


  /**
   * Requests a PTP connection for the default user, eventually returns an
   * <code>OutboundQueueConnection</code> instance.
   *
   * @exception JMSSecurityException   If connecting is not allowed.
   * @exception IllegalStateException  If the underlying JORAM server
   *                                   is not reachable.
   * @exception JMSException           Generic exception.
   */
  public javax.jms.QueueConnection createQueueConnection() throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " createQueueConnection()");

    return createQueueConnection(mcf.getUserName(), mcf.getPassword());
  }

  /**
   * Requests a PTP connection for a given user, eventually returns an
   * <code>OutboundConnection</code> instance.
   *
   * @exception JMSSecurityException   If connecting is not allowed.
   * @exception IllegalStateException  If the underlying JORAM server
   *                                   is not reachable.
   * @exception JMSException           Generic exception.
   */
  public javax.jms.QueueConnection createQueueConnection(String userName, String password)
    throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " createQueueConnection(" + userName + ", " + password + ")");

    try {
      QueueConnectionRequest cxRequest =
        new QueueConnectionRequest(userName, password, mcf.getIdentityClass());

      Object o = cxManager.allocateConnection(mcf, cxRequest);

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, this + " createQueueConnection connection = " + o);

      return (javax.jms.QueueConnection) o;
    } catch (javax.resource.spi.SecurityException exc) {
      throw new JMSSecurityException("Invalid user identification: " + exc);
    } catch (javax.resource.spi.CommException exc) {
      throw new IllegalStateException("Could not connect to the JORAM server: "
                                      + exc);
    } catch (javax.resource.ResourceException exc) {
      throw new JMSException("Could not create connection: " + exc);
    }
  }
}
