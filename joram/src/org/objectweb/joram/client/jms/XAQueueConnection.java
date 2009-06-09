/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): Nicolas Tachker (Bull SA)
 */
package org.objectweb.joram.client.jms;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;

import org.objectweb.joram.client.jms.connection.RequestChannel;

/**
 * Implements the <code>javax.jms.XAQueueConnection</code> interface.
 */
public class XAQueueConnection extends QueueConnection
    implements javax.jms.XAQueueConnection {

  /** Resource manager instance. */
  private XAResourceMngr rm;

  /**
   * Creates an <code>XAQueueConnection</code> instance.
   *
   * @param factoryParameters  The factory parameters.
   * @param requestChannel     The actual connection to wrap.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public XAQueueConnection(FactoryParameters factoryParameters,
                           RequestChannel requestChannel) throws JMSException
  {
    super(factoryParameters, requestChannel);
    rm = new XAResourceMngr(this);
  }
  
  /**
   * API method.
   * 
   * @exception IllegalStateException  If the connection is closed.
   * @exception JMSException  In case of an invalid acknowledge mode.
   */
  public javax.jms.QueueSession
         createQueueSession(boolean transacted, int acknowledgeMode)
    throws JMSException {
    return super.createQueueSession(transacted, acknowledgeMode);
  }

  /** 
   * API method.
   *
   * @exception IllegalStateException  If the connection is closed.
   */
  public javax.jms.XAQueueSession createXAQueueSession() throws JMSException
  {
    checkClosed();
    QueueSession s = new QueueSession(this, true, 0, getRequestMultiplexer());
    XAQueueSession xas = new XAQueueSession(this, s, rm);
    addSession(s);
    return xas;
  }

  /** 
   * Method inherited from interface <code>XAConnection</code>.
   *
   * @exception IllegalStateException  If the connection is closed.
   * @exception JMSException  In case of an invalid acknowledge mode.
   */
  public javax.jms.Session
         createSession(boolean transacted, int acknowledgeMode)
         throws JMSException
  {
    return super.createSession(transacted, acknowledgeMode);
  }

  /** 
   * Method inherited from interface <code>XAConnection</code>.
   *
   * @exception IllegalStateException  If the connection is closed.
   */
  public javax.jms.XASession createXASession() throws JMSException
  {
    checkClosed();
    Session s = new Session(this, true, 0, getRequestMultiplexer());
    XASession xas = new XASession(this, s, rm);
    addSession(s);
    return xas;
  }

  /**
   * return XAResourceMngr of this connection.
   * see connector
   */
  public XAResourceMngr getXAResourceMngr() {
    return rm;
  }
}
