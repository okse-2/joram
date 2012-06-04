/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2012 ScalAgent Distributed Technologies
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

import java.util.Vector;

import javax.jms.ConnectionConsumer;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.jms.ServerSession;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.XAConnection;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.CommException;
import javax.resource.spi.SecurityException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.WorkManager;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.util.management.MXWrapper;


/**
 * An <code>InboundConsumer</code> instance is responsible for consuming
 * messages from a given JORAM destination and through a given JORAM
 * connection.
 */
public class InboundConsumer implements ServerSessionPool, InboundConsumerMBean {
  
  public static Logger logger = Debug.getLogger(InboundConsumer.class.getName());
  
  /** Application server's <code>WorkManager</code> instance. */
  private WorkManager workManager;
  /** Application's endpoints factory. */
  MessageEndpointFactory endpointFactory;

  /** The provided connection to the underlying JORAM server. */
  private XAConnection cnx;
  /** The durable subscription name, if provided. */
  private String subName = null;

  /** <code>true</code> if message consumption occurs in a transaction. */
  private boolean transacted;

  /** Maximum number of Work instances to be submitted (0 for infinite). */
  private int maxWorks;

  private int ackMode;

  /**  for closing durable subscription */
  private boolean closeDurSub;

  /** Wrapped <code>ConnectionConsumer</code> instance. */
  private ConnectionConsumer cnxConsumer;
  /** Number of created server sessions. */
  private int serverSessions = 0;

  /** Pool of server sessions. */
  private Vector pool;


  /**
   * Constructs an <code>InboundConsumer</code> instance.
   *
   * @param workManager      Application server's <code>WorkManager</code>
   *                         instance.
   * @param endpointFactory  Application's endpoints factory.
   * @param cnx              Connection to the JORAM server.
   * @param dest             Destination to get messages from.
   * @param selector         Selector for filtering messages.
   * @param durable          <code>true</code> for durably subscribing.
   * @param subName          Durable subscription name.
   * @param transacted       <code>true</code> if deliveries will occur in a
   *                         XA transaction.
   * @param maxWorks         Max number of Work instances to be submitted.
   *
   * @exception NotSupportedException  If the activation parameters are
   *                                   invalid.
   * @exception SecurityException      If the target destination is not
   *                                   readable.
   * @exception CommException          If the connection with the JORAM server
   *                                   is lost.
   * @exception ResourceException      Generic exception.
   */
  public InboundConsumer(WorkManager workManager,
                  MessageEndpointFactory endpointFactory,
                  XAConnection cnx,
                  Destination dest,
                  String selector,
                  boolean durable,
                  String subName,
                  boolean transacted,
                  int maxWorks,
                  int maxMessages,
                  int ackMode,
                  boolean closeDurSub) throws ResourceException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "InboundConsumer(" + workManager +
                                    ", " + endpointFactory +
                                    ", " + cnx +
                                    ", " + dest +
                                    ", " + selector +
                                    ", " + durable +
                                    ", " + subName +
                                    ", " + transacted +
                                    ", " + maxWorks +
                                    ", " + maxMessages +
                                    ","  + ackMode +
                                    ","  + closeDurSub + ")");

    this.workManager = workManager;
    this.endpointFactory = endpointFactory;
    this.cnx = cnx;
    this.transacted = transacted;
    this.ackMode = ackMode;
    this.closeDurSub = closeDurSub;

    if (maxWorks < 0) maxWorks = 0;
    this.maxWorks = maxWorks;

    pool = new Vector(maxWorks);

    try {
      if (durable) {
        if (! (dest instanceof javax.jms.Topic))
          throw new NotSupportedException("Can't set a durable subscription "
                                          + "on a JMS queue.");

        if (subName == null)
          throw new NotSupportedException("Missing durable "
                                          + "subscription name.");

        this.subName = subName;

        cnxConsumer =
          cnx.createDurableConnectionConsumer((javax.jms.Topic) dest,
                                              subName,
                                              selector,
                                              this,
                                              maxMessages);
      } else {
        cnxConsumer = cnx.createConnectionConsumer(dest,
                                                   selector,
                                                   this,
                                                   maxMessages);
      }
      // start the connection
      cnx.start();
      // register the MBean
      registerMBean();
      
    } catch (JMSSecurityException exc) {
      throw new SecurityException("Target destination not readble: " + exc);
    } catch (javax.jms.IllegalStateException exc) {
      throw new CommException("Connection with the JORAM server is lost.");
    } catch (JMSException exc) {
      throw new ResourceException("Could not set asynchronous consumer: "+ exc);
    }
  }
  
  public String getJMXBeanName(XAConnection cnx) {
    if (! (cnx instanceof org.objectweb.joram.client.jms.Connection)) return null;
    StringBuffer buf = new StringBuffer();
    buf.append(((org.objectweb.joram.client.jms.Connection) cnx).getJMXBeanName());
    buf.append(",location=InboundConsumer");
    buf.append(",InboundConsumer=").append(endpointFactory.getClass().getSimpleName()).append("@").append(endpointFactory.hashCode());
    return buf.toString();
  }

  public String registerMBean() {
    String JMXBeanName = getJMXBeanName(cnx);
    try {
      if (JMXBeanName != null)
        MXWrapper.registerMBean(this, JMXBeanName);
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "InboundConsumer.registerMBean: " + JMXBeanName, e);
    }

    return JMXBeanName;
  }

  public void unregisterMBean() {
    try {
      MXWrapper.unregisterMBean(getJMXBeanName(cnx));
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "InboundConsumer.unregisterMBean: " + getJMXBeanName(cnx), e);
    }
  }

  /**
   * Provides a new <code>InboundSession</code> instance for processing
   * incoming messages.
   *
   * @exception JMSException  Never thrown.
   */
  public ServerSession getServerSession()
    throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " getServerSession()");

    try {
      synchronized (pool) {
        if (pool.isEmpty()) {
          if (maxWorks > 0) {
            if (serverSessions < maxWorks) {
              // Allocates a new ServerSession
              return newSession();
            }
            // Wait for a free ServerSession
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, "ServerSessionPool waits for a free ServerSession.");
            pool.wait();
            return (ServerSession) pool.remove(0);
          }
          // Allocates a new ServerSession
          return newSession();
        }
        return (ServerSession) pool.remove(0);
      }
    } catch (Exception exc) {
      throw new JMSException("Error while getting server session from pool: " + exc);
    }
  }

  private InboundSession newSession() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "ServerSessionPool provides new ServerSession.");
    serverSessions++;
    return new InboundSession(this,
                              workManager,
                              endpointFactory,
                              cnx,
                              transacted,
                              ackMode);
  }

  /** Releases an <code>InboundSession</code> instance. */
  void releaseSession(InboundSession session) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " releaseSession(" + session + ")");

    try {
      synchronized (pool) {
        pool.add(session);
        pool.notify();
      }
    } catch (Exception exc) {}
  }

  /** Closes the consumer. */
  void close() {
      if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, this + " close() unsubscribe subscription: "+ closeDurSub);

      try {
        cnx.setExceptionListener(null);
      } catch (JMSException e) { }
      
      // unregister the MBean
      unregisterMBean();
      
      try {
          cnxConsumer.close();

          if (closeDurSub) {
              if (subName != null) {
                  Session session = cnx.createSession(true, 0);
                  session.unsubscribe(subName);
              }
          }

          cnx.close();
      } catch (JMSException exc) { }
  }
  
  /* (non-Javadoc)
   * @see org.objectweb.joram.client.connector.InboundConsumerMBean#getSubName()
   */
  public String getSubName() {
    return subName;
  }

  /* (non-Javadoc)
   * @see org.objectweb.joram.client.connector.InboundConsumerMBean#getTransacted()
   */
  public boolean getTransacted() {
    return transacted;
  }

  /* (non-Javadoc)
   * @see org.objectweb.joram.client.connector.InboundConsumerMBean#getMaxWorks()
   */
  public int getMaxWorks() {
    return maxWorks;
  }

  /* (non-Javadoc) 
   * @see org.objectweb.joram.client.connector.InboundConsumerMBean#getAckMode()
   */
  public String getAckMode() {
    switch (ackMode) {
    case Session.AUTO_ACKNOWLEDGE:
      return "AUTO_ACKNOWLEDGE";
    case Session.CLIENT_ACKNOWLEDGE:
      return "CLIENT_ACKNOWLEDGE";
    case Session.DUPS_OK_ACKNOWLEDGE:
      return "DUPS_OK_ACKNOWLEDGE";
    case Session.SESSION_TRANSACTED:
      return "SESSION_TRANSACTED";
    default:
      return "unknown";
    }
  }

  /* (non-Javadoc)
   * @see org.objectweb.joram.client.connector.InboundConsumerMBean#getCloseDurSub()
   */
  public boolean getCloseDurSub() {
    return closeDurSub;
  }

  /* (non-Javadoc)
   * @see org.objectweb.joram.client.connector.InboundConsumerMBean#getServerSessions()
   */
  public int getServerSessions() {
    return serverSessions;
  }
  
  /* (non-Javadoc)
   * @see org.objectweb.joram.client.connector.InboundConsumerMBean#getPoolSize()
   */
  public int getPoolSize() {
    return pool.size();
  }
  
  public String[] getSessions() {
    String[] sessionsName = new String[pool.size()];
    for (int i = 0; i < pool.size(); i++) {
      InboundSession sess = (InboundSession) pool.get(i);
      try {
      sessionsName[i] = sess.getSession().toString();
      } catch (Exception e) {
        sessionsName[i] = "unknown";
      }
    }
    return sessionsName;
  }
}
