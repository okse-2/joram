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
 * Contributor(s): Benoit Pelletier (Bull SA)
 *                 Nicolas Tachker (ScalAgent DT)
 */
package org.objectweb.joram.client.jms.ha.tcp;

import javax.jms.JMSSecurityException;

import org.objectweb.joram.client.jms.Connection;

/**
 * An <code>XATcpConnectionFactory</code> instance is a factory of
 * tcp connections dedicated to HA communication.
 */
public class HATcpConnectionFactory extends org.objectweb.joram.client.jms.ConnectionFactory {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  /**
   * Constructs an <code>HATcpConnectionFactory</code> instance.
   */
  public HATcpConnectionFactory(String url) {
    super(url);
  }

  /**
   * Constructs an empty <code>HATcpConnectionFactory</code>.
   * Needed by ObjectFactory.
   */
  public HATcpConnectionFactory() {
	super();  
  }
  
  /**
   * Method inherited from the <code>ConnectionFactory</code> class.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.Connection
      createConnection(String name, String password)
    throws javax.jms.JMSException {
    initIdentity(name, password);
      HATcpRequestChannel lc = new HATcpRequestChannel(
        params.getUrl(), params, identity, reliableClass);
      return new Connection(params, lc);
    }

  /**
   * Admin method creating a <code>javax.jms.ConnectionFactory</code>
   * instance for creating HA TCP connections with a given list of servers.
   *
   * @param url URL of the HA Joram server
   */
  public static javax.jms.ConnectionFactory create(String url) {
    return create(url, "org.objectweb.joram.client.jms.tcp.ReliableTcpClient");
  }
  /**
   * Admin method creating a <code>javax.jms.ConnectionFactory</code>
   * instance for creating HA TCP connections with a given list of servers.
   *
   * @param url URL of the HA Joram server
   * @param reliableClass  Reliable class name.
   */
  public static javax.jms.ConnectionFactory
      create(String url, String reliableClass) {
    HATcpConnectionFactory cf = new HATcpConnectionFactory(url);
    cf.setReliableClass(reliableClass);
    return cf;
  }
}
