/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
 * Copyright (C) 2004 Bull SA
 * Copyright (C) 1996 - 2000 Dyade
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
 * Contributor(s):
 */
package org.objectweb.joram.client.jms;

import javax.jms.JMSException;

/**
 * Implements the <code>javax.jms.XAQueueSession</code> interface.
 */
public class XAQueueSession extends XASession implements javax.jms.XAQueueSession {
  /**
   * Constructs an <code>XAQueueSession</code> instance.
   *
   * @param cnx  The connection the session belongs to.
   * @param rm   The resource manager.
   *
   * @exception JMSException  Actually never thrown.
   */
  XAQueueSession(XAQueueConnection cnx, 
                 QueueSession qs, 
                 XAResourceMngr rm) throws JMSException {
    super(cnx, qs, rm);
  }
  
  /** Returns a String image of this session. */
  public String toString() {
    return "XAQueueSess:" + sess.getId();
  }

  /**
   * API method.
   * Gets the queue session associated with this XAQueueSession.
   * 
   * @return the queue session object.
   * 
   * @exception JMSException if an internal error occurs. 
   */ 
  public javax.jms.QueueSession getQueueSession() throws JMSException {
    return (QueueSession) sess;
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.QueueSender createSender(Queue queue) throws JMSException {
    return ((QueueSession) sess).createSender(queue);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.QueueReceiver createReceiver(Queue queue, String selector) throws JMSException {
    return ((QueueSession) sess).createReceiver(queue, selector);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.QueueReceiver createReceiver(Queue queue) throws JMSException {
    return ((QueueSession) sess).createReceiver(queue);
  }
}
