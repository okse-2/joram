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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): Benoit Pelletier (Bull SA)
 *                 Nicolas Tachker (ScalAgent DT)
 */
package org.objectweb.joram.client.jms.ha.tcp;

import javax.jms.JMSSecurityException;

import org.objectweb.joram.client.jms.XAConnection;

/**
 * An <code>XAHATcpConnectionFactory</code> instance is a factory of
 * tcp connections dedicated to XA HA communication.
 */
public class XAHATcpConnectionFactory
  extends org.objectweb.joram.client.jms.XAConnectionFactory {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * Constructs an <code>XAHATcpConnectionFactory</code> instance.
   */
  public XAHATcpConnectionFactory(String url) {
    super(url);
  }

  public XAHATcpConnectionFactory() {
	  super();
  }

  /**
   * Method inherited from the <code>XAConnectionFactory</code> class.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   */
  public javax.jms.XAConnection
      createXAConnection(String name, String password)
    throws javax.jms.JMSException {
      HATcpConnection lc = new HATcpConnection(
        params.getUrl(), params, name, password, reliableClass);
      return new XAConnection(params, lc);
    }

  /**
   * Admin method creating a <code>javax.jms.XAConnectionFactory</code>
   * instance for creating HA TCP connections with a given list of servers.
   *
   * @param url URL of the HA Joram server
   */
  public static javax.jms.XAConnectionFactory create(String url) {
    return create(url, "org.objectweb.joram.client.jms.tcp.ReliableTcpClient");
  }

  /**
   * Admin method creating a <code>javax.jms.XAConnectionFactory</code>
   * instance for creating HA TCP connections with a given list of servers.
   *
   * @param url URL of the HA Joram server
   * @param reliableClass  Reliable class name.
   */
  public static javax.jms.XAConnectionFactory
      create(String url, String reliableClass) {
    XAHATcpConnectionFactory cf = new XAHATcpConnectionFactory(url);
    cf.setReliableClass(reliableClass);
    return cf;
  }
}
