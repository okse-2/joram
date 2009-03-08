/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2009 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): Frederic Maistre (Bull SA)
 */
package org.objectweb.joram.client.jms.local;

import javax.jms.JMSSecurityException;

import org.objectweb.joram.client.jms.Connection;
import org.objectweb.joram.client.jms.QueueConnection;
import org.objectweb.joram.client.jms.TopicConnection;
import org.objectweb.joram.client.jms.XAConnection;
import org.objectweb.joram.client.jms.XAQueueConnection;
import org.objectweb.joram.client.jms.XATopicConnection;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

/**
 * An <code>XALocalConnectionFactory</code> instance is a factory of
 * local connections dedicated to XA communication.
 *  
 * @deprecated Replaced next to Joram 5.2.1 by {@link LocalConnectionFactory}.
 */
public class XALocalConnectionFactory extends org.objectweb.joram.client.jms.XAConnectionFactory {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /**
   * Constructs an <code>XALocalConnectionFactory</code> instance.
   */
  public XALocalConnectionFactory() {
    super("localhost", -1);
  }

  /**
   * Method inherited from the <code>ConnectionFactory</code> class.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.Connection createConnection(String name, String password) throws javax.jms.JMSException {
    initIdentity(name, password);
    LocalRequestChannel lc = new LocalRequestChannel(identity);
    return new Connection(params, lc);
  }

  /**
   * Method inherited from the <code>QueueConnectionFactory</code> class.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   */
  public javax.jms.QueueConnection createQueueConnection(String name, String password) throws javax.jms.JMSException {
    initIdentity(name, password);
    LocalRequestChannel lc = new LocalRequestChannel(identity);
    return new QueueConnection(params, lc);
  }

  /**
   * Method inherited from the <code>TopicConnectionFactory</code> class.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   */
  public javax.jms.TopicConnection createTopicConnection(String name, String password) throws javax.jms.JMSException {
    initIdentity(name, password);
    LocalRequestChannel lc = new LocalRequestChannel(identity);
    return new TopicConnection(params, lc);
  }

  /**
   * Method inherited from the <code>XAConnectionFactory</code> class.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   */
  public javax.jms.XAConnection createXAConnection(String name, String password) throws javax.jms.JMSException {
    initIdentity(name, password);
    LocalRequestChannel lc = new LocalRequestChannel(identity);
    return new XAConnection(params, lc);
  }

  /**
   * Method inherited from the <code>XAQueueConnectionFactory</code> class.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   */
  public javax.jms.XAQueueConnection createXAQueueConnection(String name, String password) throws javax.jms.JMSException {
    initIdentity(name, password);
    LocalRequestChannel lc = new LocalRequestChannel(identity);
    return new XAQueueConnection(params, lc);
  }

  /**
   * Method inherited from the <code>XATopicConnectionFactory</code> class..
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   */
  public javax.jms.XATopicConnection createXATopicConnection(String name, String password) throws javax.jms.JMSException {
    initIdentity(name, password);
    LocalRequestChannel lc = new LocalRequestChannel(identity);
    return new XATopicConnection(params, lc);
  }

  /**
   * Admin method creating a <code>javax.jms.XAConnectionFactory</code>
   * instance for creating local connections.
   */ 
  public static javax.jms.XAConnectionFactory create() {
    return new XALocalConnectionFactory();
  }
}
