/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2008 ScalAgent Distributed Technologies
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
 * Initial developer(s): David Feliot (ScalAgent DT)
 * Contributor(s): Frederic Maistre (Bull SA)
 */
package org.objectweb.joram.client.jms.local;

import javax.jms.JMSSecurityException;

import org.objectweb.joram.client.jms.Connection;
import org.objectweb.joram.client.jms.QueueConnection;
import org.objectweb.joram.client.jms.QueueConnectionFactory;

/**
 * A <code>QueueLocalConnectionFactory</code> instance is a factory of
 * local connections for PTP communication.
 */
public class QueueLocalConnectionFactory extends QueueConnectionFactory {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /**
   * Constructs a <code>QueueLocalConnectionFactory</code> instance.
   */
  public QueueLocalConnectionFactory() {
    super("localhost", -1);
  }

  /**
   * Method inherited from the <code>QueueConnectionFactory</code> class.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   */
  public javax.jms.QueueConnection createQueueConnection(String name, String password)
  throws javax.jms.JMSException {
    initIdentity(name, password);
    LocalConnection lc = new LocalConnection(identity);
    return new QueueConnection(params, lc);
  }

  /**
   * Method inherited from the <code>ConnectionFactory</code> class.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   */
  public javax.jms.Connection createConnection(String name, String password)
  throws javax.jms.JMSException {
    initIdentity(name, password);
    LocalConnection lc = new LocalConnection(identity);
    return new Connection(params, lc);
  }

  /**
   * Admin method creating a <code>javax.jms.QueueConnectionFactory</code>
   * instance for creating local connections.
   */ 
  public static javax.jms.QueueConnectionFactory create() {
    return new QueueLocalConnectionFactory();
  }
}
