/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
 * Copyright (C) 2004 - ScalAgent Distributed Technologies
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
 */
package org.objectweb.joram.client.jms.ha.tcp;

import org.objectweb.joram.client.jms.XAConnection;

import javax.naming.NamingException;


/**
 * An <code>XAHATcpConnectionFactory</code> instance is a factory of
 * tcp connections dedicated to XA HA communication.
 */
public class XAHATcpConnectionFactory
    extends org.objectweb.joram.client.jms.XAConnectionFactory
{

  private String url;

  /**
   * Constructs an <code>XATcpConnectionFactory</code> instance.
   */
  public XAHATcpConnectionFactory(String url)
  {
    super("", -1);
    this.url = url;
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
      url, params, name, password);
    return new XAConnection(params, lc);
  }

  
  /**
   * Admin method creating a <code>javax.jms.XAConnectionFactory</code>
   * instance for creating tcp connections.
   */ 
  public static javax.jms.XAConnectionFactory create(String url)
  {
    return new XAHATcpConnectionFactory(url);
  }
}
