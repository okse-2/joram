/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
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
 * Contributor(s):
 */
package org.objectweb.joram.client.connector;

import javax.jms.*;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.CommException;
import javax.resource.spi.SecurityException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.WorkManager;

import java.util.Vector;


/**
 * An <code>InboundConsumer</code> instance is responsible for consuming
 * messages from a given JORAM destination and through a given JORAM
 * connection.
 */
class InboundConsumer implements javax.jms.ServerSessionPool
{
  /** Application server's <code>WorkManager</code> instance. */
  private WorkManager workManager;
  /** Application's endpoints factory. */
  private MessageEndpointFactory endpointFactory; 

  /** The provided connection to the underlying JORAM server. */
  private XAConnection cnx;
  /** The durable subscription name, if provided. */
  private String subName = null;

  /** <code>true</code> if message consumption occurs in a transaction. */
  private boolean transacted;

  /** Maximum number of Work instances to be submitted (0 for infinite). */
  private int maxWorks;

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
  InboundConsumer(WorkManager workManager,
                  MessageEndpointFactory endpointFactory,
                  XAConnection cnx,
                  Destination dest,
                  String selector,
                  boolean durable,
                  String subName,
                  boolean transacted,
                  int maxWorks) throws ResourceException
  {
    this.workManager = workManager;
    this.endpointFactory = endpointFactory;
    this.cnx = cnx;
    this.transacted = transacted;

    if (maxWorks < 0)
      this.maxWorks = 0;
    else {
      this.maxWorks = maxWorks;
      pool = new Vector(maxWorks);
    }

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
                                              1);
      } 
      else
        cnxConsumer = cnx.createConnectionConsumer(dest,
                                                   selector,
                                                   this,
                                                   1);
    }
    catch (JMSSecurityException exc) {
      throw new SecurityException("Target destination not readble: "
                                  + exc);
    }
    catch (javax.jms.IllegalStateException exc) {
      throw new CommException("Connection with the JORAM server is lost.");
    }
    catch (JMSException exc) {
      throw new ResourceException("Could not set asynchronous consumer: "
                                  + exc);
    }
  }

  /**
   * Provides a new <code>InboundSession</code> instance for processing
   * incoming messages.
   *
   * @exception JMSException  Never thrown.
   */
  public ServerSession getServerSession() throws JMSException
  {
    // No limit to work 
    if (maxWorks <= 0)
      return new InboundSession(this,
                                workManager,
                                endpointFactory,
                                cnx,
                                transacted);

    try {
      synchronized (pool) {
        if (pool.isEmpty() && (serverSessions < maxWorks)) {
          serverSessions++;
          return new InboundSession(this,
                                    workManager,
                                    endpointFactory,
                                    cnx,
                                    transacted);
        }
        else if (pool.isEmpty())
          pool.wait();

        return (ServerSession) pool.remove(0);
      }
    }
    catch (Exception exc) {
      throw new JMSException("Error while getting server session from pool: "
                             + exc);
    }
  }


  /** Releases an <code>InboundSession</code> instance. */
  void releaseSession(InboundSession session)
  {
    try {
      synchronized (pool) {
        pool.add(session);
        pool.notify();
      }
    }
    catch (Exception exc) {}
  }

  /** Closes the consumer. */
  void close()
  {
    try {
      cnxConsumer.close();

      if (subName != null) {
        Session session = cnx.createSession(true, 0);
        session.unsubscribe(subName);
      }

      cnx.close();
    }
    catch (JMSException exc) {}
  }
}
