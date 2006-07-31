/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2006 ScalAgent Distributed Technologies
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
 * Contributor(s): Benoit Pelletier (Bull SA)
 */
package org.objectweb.joram.client.jms.ha.tcp;

import org.objectweb.joram.client.jms.XAConnection;
import org.objectweb.joram.client.jms.XAQueueConnection;
import org.objectweb.joram.client.jms.Connection;
import org.objectweb.joram.client.jms.QueueConnection;

import javax.naming.NamingException;

/**
 * An <code>XAQueueHATcpConnectionFactory</code> instance is a factory of
 * tcp connections for XA PTP HA communication.
 */
public class XAQueueHATcpConnectionFactory
    extends org.objectweb.joram.client.jms.XAQueueConnectionFactory
{

  private String url;

  /**
   * Constructs an <code>XAQueueHATcpConnectionFactory</code> instance.
   */
  public XAQueueHATcpConnectionFactory(String url)
    {
      super(url);
      this.url = url;
    }

  /**
   * Method inherited from the <code>XAQueueConnectionFactory</code> class.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   */
  public javax.jms.XAQueueConnection
      createXAQueueConnection(String name, String password)
    throws javax.jms.JMSException
    {
      HATcpConnection lc = new HATcpConnection(
        url, params, name, password, reliableClass);
      return new XAQueueConnection(params, lc);
    }

  /**
   * Method inherited from the <code>XAConnectionFactory</code> class.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   */
  public javax.jms.XAConnection
      createXAConnection(String name, String password)
    throws javax.jms.JMSException
    {
      HATcpConnection lc = new HATcpConnection(
        url, params, name, password, reliableClass);
      return new XAConnection(params, lc);
    }

  /**
   * Method inherited from the <code>QueueConnectionFactory</code> class.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   */
  public javax.jms.QueueConnection
      createQueueConnection(String name, String password)
    throws javax.jms.JMSException
    {
      HATcpConnection lc = new HATcpConnection(
        url, params, name, password, reliableClass);
      return new QueueConnection(params, lc);
    }

  /**
   * Method inherited from the <code>ConnectionFactory</code> class.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   */
  public javax.jms.Connection createConnection(String name, String password)
    throws javax.jms.JMSException
    {
      HATcpConnection lc = new HATcpConnection(
        url, params, name, password, reliableClass);
      return new Connection(params, lc);
    }


  /**
   * Admin method creating a <code>javax.jms.XAQueueConnectionFactory</code>
   * instance for creating tcp connections.
   */
  public static javax.jms.XAQueueConnectionFactory create(String url) {
    return create(url,
                  "org.objectweb.joram.client.jms.tcp.ReliableTcpClient");
  }

  /**
   * Admin method creating a <code>javax.jms.XAQueueConnectionFactory</code>
   * instance for creating tcp connections.
   */
  public static javax.jms.XAQueueConnectionFactory
      create(String url, String reliableClass) {
    XAQueueHATcpConnectionFactory cf = new XAQueueHATcpConnectionFactory(url);
    cf.setReliableClass(reliableClass);
    return cf;
  }
}
