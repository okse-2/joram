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

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.XAConnection;
import javax.jms.XASession;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.XAResource;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * An <code>InboundSession</code> instance is responsible for processing
 * delivered messages within a <code>javax.resource.spi.Work</code> instance,
 * and passing them to a set of application server endpoints.
 */
class InboundSession implements javax.jms.ServerSession,
                                javax.resource.spi.work.Work,
                                javax.jms.MessageListener {
  
  public static Logger logger = Debug.getLogger(InboundSession.class.getName());
  
  /** <code>InboundConsumer</code> instance this session belongs to. */
  private InboundConsumer consumer;

  /** Application server's <code>WorkManager</code> instance. */
  private WorkManager workManager;
  /** Application's endpoints factory. */
  private MessageEndpointFactory endpointFactory; 

  /**
   * <code>javax.jms.Session</code> instance dedicated to processing
   * the delivered messages.
   */
  private Session session;
  /** <code>XAResource</code> instance, if any. */
  private XAResource xaResource = null;


  /**
   * Constructs an <code>InboundSession</code> instance.
   *
   * @param consumer         InboundConsumer creating this session.
   * @param workManager      Application server's <code>WorkManager</code>
   *                         instance.
   * @param endpointFactory  Application's endpoints factory.
   * @param cnx              Connection to the underlying JORAM server.
   * @param transacted       <code>true</code> if deliveries occur within a 
   *                         XA transaction.
   */
  InboundSession(InboundConsumer consumer,
                 WorkManager workManager,
                 MessageEndpointFactory endpointFactory,
                 XAConnection cnx,
                 boolean transacted,
                 int ackMode) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "InboundSession(" + consumer +
                                    "," + workManager +
                                    "," + endpointFactory +
                                    "," + cnx +
                                    "," + transacted + 
                                    "," + ackMode + ")");
    
    this.consumer = consumer;
    this.workManager = workManager;
    this.endpointFactory = endpointFactory;

    try {
      if (transacted) {
        session = cnx.createXASession();
        xaResource = ((XASession) session).getXAResource();
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "InboundSession xaResource = " + xaResource);
      } else {
        session = cnx.createSession(false, ackMode);
      }

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "InboundSession session = " + session);
      session.setMessageListener(this);
    } catch (JMSException exc) {}
  }


  /**
   * Provides the wrapped <code>javax.jms.Session</code> instance for
   * processing delivered messages.
   *
   * @exception JMSException  Never thrown.
   */
  public Session getSession() throws JMSException {
    return session;
  }

  /**
   * Notifies that the messages are ready to be processed.
   *
   * @exception JMSException  If submitting the processing work fails.
   */
  public void start() throws JMSException {
    try {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "ServerSession submits Work instance.");
      workManager.scheduleWork(this);
    } catch (Exception exc) {
      throw new JMSException("Can't start the adapter session for processing "
                             + "the delivered messages: " + exc);
    }
  }

  /** <code>javax.resource.spi.Work</code> method, not effective. */
  public void release() {
    try {
      session.close();
    } catch (JMSException exc) { }
  }

  /** Runs the wrapped session for processing the messages. */
  public void run() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "ServerSession runs wrapped Session.");
    session.run();
    consumer.releaseSession(this);
  }

  /** Forwards a processed message to an endpoint. */
  public void onMessage(javax.jms.Message message) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " onMessage(" + message + ")");

    MessageEndpoint endpoint = null;
    try {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "ServerSession passes message to listener.");
      endpoint = endpointFactory.createEndpoint(xaResource);
      ((javax.jms.MessageListener) endpoint).onMessage(message);
      endpoint.release();
    } catch (Exception exc) {
      try {
        // try to clean the context for next invocation
        if (endpoint != null) endpoint.release();
      } catch (Exception e) {
        // ignore the exception
      }
      throw new java.lang.IllegalStateException("Could not get endpoint instance: " + exc);
    }
  }
}
