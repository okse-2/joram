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


/**
 * An <code>OutboundConnection</code> instance is a handler for a physical
 * connection to an underlying JORAM server, allowing a component to
 * transparently use this physical connection possibly within a transaction
 * (local or global).
 */
public class OutboundConnection implements javax.jms.Connection
{
  /** The managed connection this "handle" belongs to. */
  ManagedConnectionImpl managedCx;
  /** The physical connection this "handle" handles. */
  XAConnection xac;
  
  /** <code>true</code> if this "handle" is valid. */
  boolean valid = true;

 
  /**
   * Constructs an <code>OutboundConnection</code> instance.
   *
   * @param managedCx  The managed connection building the handle.
   * @param xac        The underlying physical connection to handle.
   */
  OutboundConnection(ManagedConnectionImpl managedCx, XAConnection xac)
  {
    this.managedCx = managedCx;
    this.xac = xac;
  }

 
  /**
   * Forbidden call on an application or component's outbound connection,
   * throws a <code>JMSException</code> instance.
   */
  public void setClientID(String clientID) throws JMSException
  {
    if (! valid)
      throw new javax.jms.IllegalStateException("Invalid connection handle.");

    throw new JMSException("Forbidden call on an application or component's "
                           + "session.");
  }

  /**
   * Forbidden call on an application or component's outbound connection,
   * throws a <code>JMSException</code> instance.
   */
  public void setExceptionListener(ExceptionListener listener)
              throws JMSException
  {
    if (! valid)
      throw new javax.jms.IllegalStateException("Invalid connection handle.");

    throw new JMSException("Forbidden call on an application or component's "
                           + "session.");
  }
 
  /**
   * Returns the unique authorized JMS session per connection wrapped
   * in an <code>OutboundSession</code> instance.
   *
   * @exception javax.jms.IllegalStateException  If the handle is invalid.
   * @exception javax.jms.JMSException           Generic exception.
   */
  public Session createSession(boolean transacted, int acknowledgeMode)
                 throws JMSException
  {
    if (! valid)
      throw new javax.jms.IllegalStateException("Invalid connection handle.");

    if (managedCx.session == null)
      managedCx.session = xac.createSession(false, Session.AUTO_ACKNOWLEDGE);

    return new OutboundSession(managedCx.session, this);
  }

  /**
   * Forbidden call on an application or component's outbound connection,
   * throws a <code>JMSException</code> instance.
   */
  public String getClientID() throws JMSException
  {
    if (! valid)
      throw new javax.jms.IllegalStateException("Invalid connection handle.");

    throw new JMSException("Forbidden call on an application or component's "
                           + "session.");
  }
  
  /**
   * Delegates the call to the wrapped JMS connection.
   *
   * @exception javax.jms.IllegalStateException  If the handle is invalid.
   * @exception javax.jms.JMSException           Generic exception.
   */
  public ConnectionMetaData getMetaData() throws JMSException
  {
    if (! valid)
      throw new javax.jms.IllegalStateException("Invalid connection handle.");

    return xac.getMetaData();
  }

  /**
   * Forbidden call on an application or component's outbound connection,
   * throws a <code>JMSException</code> instance.
   */
  public ExceptionListener getExceptionListener() throws JMSException
  {
    if (! valid)
      throw new javax.jms.IllegalStateException("Invalid connection handle.");

    throw new JMSException("Forbidden call on an application or component's "
                           + "session.");
  }
  
  /**
   * Delegates the call to the wrapped JMS connection.
   *
   * @exception javax.jms.IllegalStateException  If the handle is invalid.
   * @exception javax.jms.JMSException           Generic exception.
   */
  public void start() throws JMSException
  {
    if (! valid)
      throw new javax.jms.IllegalStateException("Invalid connection handle.");

    xac.start();
  }

  /**
   * Forbidden call on an application or component's outbound connection,
   * throws a <code>JMSException</code> instance.
   */
  public void stop() throws JMSException
  {
    if (! valid)
      throw new javax.jms.IllegalStateException("Invalid connection handle.");

    throw new JMSException("Forbidden call on an application or component's "
                           + "session.");
  }

  /**
   * Forbidden call on an application or component's outbound connection,
   * throws a <code>JMSException</code> instance.
   */
  public ConnectionConsumer
         createConnectionConsumer(Destination destination,
                                  String messageSelector,
                                  ServerSessionPool sessionPool,
                                  int maxMessages)
         throws JMSException
  {
    if (! valid)
      throw new javax.jms.IllegalStateException("Invalid connection handle.");

    throw new JMSException("Forbidden call on an application or component's "
                           + "session.");
  }

  /**
   * Forbidden call on an application or component's outbound connection,
   * throws a <code>JMSException</code> instance.
   */
  public ConnectionConsumer
         createDurableConnectionConsumer(Topic topic,
                                         String subscriptionName,
                                         String messageSelector,
                                         ServerSessionPool sessionPool,
                                         int maxMessages)
         throws JMSException
  {
    if (! valid)
      throw new javax.jms.IllegalStateException("Invalid connection handle.");

    throw new JMSException("Forbidden call on an application or component's "
                           + "session.");
  }

  /**
   * Requests to close the physical connection.
   *
   * @exception javax.jms.IllegalStateException  If the handle is invalid.
   * @exception javax.jms.JMSException           Generic exception.
   */
  public synchronized void close() throws JMSException
  {
    valid = false;
    managedCx.closeHandle(this);
  }
}
