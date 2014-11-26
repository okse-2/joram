/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 - ScalAgent Distributed Technologies
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

import javax.jms.ConnectionConsumer;
import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.XAQueueConnection;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * An <code>OutboundQueueConnection</code> instance is a handler for a
 * physical PTP connection to an underlying JORAM server, allowing a
 * component to transparently use this physical connection possibly within
 * a transaction (local or global).
 */
public class OutboundQueueConnection extends OutboundConnection implements QueueConnection {
  
  public static Logger logger = Debug.getLogger(OutboundQueueConnection.class.getName());
  
  /**
   * Constructs an <code>OutboundQueueConnection</code> instance.
   *
   * @param managedCx  The managed connection building the handle.
   * @param xac        The underlying physical PTP connection to handle.
   */
  OutboundQueueConnection(ManagedConnectionImpl managedCx, XAQueueConnection xac) {
    super(managedCx, xac);
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "OutboundQueueConnection(" + managedCx + ", " + xac + ")");
  }

 
  /**
   * Returns the unique authorized JMS session per connection wrapped in
   * an <code>OutboundQueueSession</code> instance.
   *
   * @exception javax.jms.IllegalStateException  If the handle is invalid.
   * @exception javax.jms.JMSException           Generic exception.
   */
  public QueueSession createQueueSession(boolean transacted, int acknowledgeMode) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " createQueueSession(" + transacted + ", " + acknowledgeMode +  ")");

    if (! valid)
      throw new javax.jms.IllegalStateException("Invalid connection handle.");

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                                    this + " createQueueSession sess = " +  managedCx.session);
    Session sess = managedCx.session;
    if (sess == null)
      sess = xac.createSession(false, acknowledgeMode);

    return new OutboundQueueSession(sess, this, transacted);
  }

  /**
   * Forbidden call on an application or component's outbound connection,
   * throws a <code>IllegalStateException</code> instance.
   */
  public ConnectionConsumer createConnectionConsumer(Queue queue,
                                  String messageSelector,
                                  ServerSessionPool sessionPool,
                                  int maxMessages) throws JMSException {
    throw new IllegalStateException("Forbidden call on a component's "
                                    + "connection.");
  }
  
  public boolean cnxEquals(Object obj) {
    return (obj instanceof QueueConnection)
           && xac.equals(obj);
  }
}
