/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2007 ScalAgent Distributed Technologies
 * Copyright (C) 2007 France Telecom R&D
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
 * Contributor(s): ScalAgent Distributed Technologies
 *                 Benoit Pelletier (Bull SA)
 */
package org.objectweb.joram.client.jms;

import javax.naming.*;
import javax.jms.JMSException;

import org.objectweb.joram.client.jms.admin.AbstractConnectionFactory;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.joram.shared.JoramTracing;

/**
 * Implements the <code>javax.jms.XAConnectionFactory</code> interface.
 */
public abstract class XAConnectionFactory
                extends AbstractConnectionFactory
                implements javax.jms.XAConnectionFactory {
  /**
   * Constructs an <code>XAConnectionFactory</code> dedicated to a given
   * server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   */
  public XAConnectionFactory(String host, int port) {
    super(host, port);
  }

  /**
   * Constructs an <code>XAConnectionFactory</code> dedicated to a given
   * server.
   *
   * @param url joram ha url
   */
  public XAConnectionFactory(String url) {
    super(url);
  }

  /**
   * Constructs an empty <code>ConnectionFactory</code>.
   */
  public XAConnectionFactory() {
    super();
  }

  /** Returns a string view of the connection factory. */
  public String toString() {
    return "XACF:" + params.getHost() + "-" + params.getPort();
  }

  /**
   * API method, implemented according to the communication protocol.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public abstract javax.jms.XAConnection
      createXAConnection(String name, String password) throws JMSException;

  /**
   * API method.
   *
   * @exception JMSSecurityException  If the default identification is
   *              incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.XAConnection createXAConnection() throws JMSException {
    return createXAConnection(getDefaultLogin(), getDefaultPassword());
  }
}