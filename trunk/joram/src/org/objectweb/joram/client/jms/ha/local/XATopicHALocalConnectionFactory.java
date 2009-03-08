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
 */
package org.objectweb.joram.client.jms.ha.local;

import javax.jms.JMSSecurityException;

import org.objectweb.joram.client.jms.Connection;
import org.objectweb.joram.client.jms.TopicConnection;
import org.objectweb.joram.client.jms.XAConnection;
import org.objectweb.joram.client.jms.XATopicConnection;
import org.objectweb.joram.client.jms.XATopicConnectionFactory;

/**
 * An <code>XATopicHALocalConnectionFactory</code> instance is a factory of
 * local connections for XA Pub/Sub HA communication.
 *  
 * @deprecated Replaced next to Joram 5.2.1 by {@link HALocalConnectionFactory}.
 */
public class XATopicHALocalConnectionFactory extends XATopicConnectionFactory {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /**
   * Constructs an <code>XATopicLocalConnectionFactory</code> instance.
   */
  public XATopicHALocalConnectionFactory() {
    super("localhost", -1);
  }

  /**
   * Method inherited from the <code>XATopicConnectionFactory</code> class..
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   */
  public javax.jms.XATopicConnection createXATopicConnection(String name, String password) throws javax.jms.JMSException {
    initIdentity(name, password);
    HALocalRequestChannel lc = new HALocalRequestChannel(identity);
    return new XATopicConnection(params, lc);
  }

  /**
   * Method inherited from the <code>XAConnectionFactory</code> class.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   */
  public javax.jms.XAConnection createXAConnection(String name, String password) throws javax.jms.JMSException {
    initIdentity(name, password);
    HALocalRequestChannel lc = new HALocalRequestChannel(identity);
    return new XAConnection(params, lc);
  }

  /**
   * Method inherited from the <code>TopicConnectionFactory</code> class.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   */
  public javax.jms.TopicConnection createTopicConnection(String name, String password) throws javax.jms.JMSException {
    initIdentity(name, password);
    HALocalRequestChannel lc = new HALocalRequestChannel(identity);
    return new TopicConnection(params, lc);
  }

  /**
   * Method inherited from the <code>ConnectionFactory</code> class.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.Connection createConnection(String name, String password) throws javax.jms.JMSException {
    initIdentity(name, password);
    HALocalRequestChannel lc = new HALocalRequestChannel(identity);
    return new Connection(params, lc);
  }

  /**
   * Admin method creating a <code>javax.jms.XATopicConnectionFactory</code>
   * instance for creating local connections.
   */ 
  public static javax.jms.XATopicConnectionFactory create() {
    return new XATopicHALocalConnectionFactory();
  }
}
