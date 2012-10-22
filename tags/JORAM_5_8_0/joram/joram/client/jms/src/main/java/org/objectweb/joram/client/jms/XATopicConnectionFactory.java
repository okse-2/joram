/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2009 ScalAgent Distributed Technologies
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

import org.objectweb.joram.client.jms.admin.AbstractConnectionFactory;

/**
 * Implements the <code>javax.jms.XATopicConnectionFactory</code> interface.
 *  
 * @deprecated Replaced next to Joram 5.2.1 by ConnectionFactory.
 */
public abstract class XATopicConnectionFactory extends AbstractConnectionFactory implements javax.jms.XATopicConnectionFactory {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /**
   * Constructs an empty <code>XATopicConnectionFactory</code>.
   * Needed by ObjectFactory, should only be used for internal purposes.
   */
  public XATopicConnectionFactory() {
    super();
  }

  /**
   * Constructs an <code>XATopicConnectionFactory</code> dedicated to a given server.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   */
  public XATopicConnectionFactory(String host, int port) {
    super(host, port);
  }

  /**
   * Constructs an <code>XATopicConnectionFactory</code> dedicated to a given server.
   *
   * @param url : joram url
   */
  public XATopicConnectionFactory(String url) {
    super(url);
  }

  /** Returns a string view of the connection factory. */
  public String toString() {
    return "XATCF:" + params.getHost() + "-" + params.getPort();
  }
}
