/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.proxies.tcp;

import org.objectweb.joram.mom.proxies.ConnectionManagerMBean;

/**
 * Adds JMX monitoring for tcp connections.
 */
public interface TcpProxyServiceMBean extends ConnectionManagerMBean {

  /**
   * Gets the socket address of the server.
   * 
   * @return the server socket address.
   */
  public String getServerAddress();

  /**
   * Gets the number of threads listening for incoming tcp connections.
   * 
   * @return the tcp listeners pool size.
   */
  public int getTcpListenersPoolSize();

  /**
   * Gets the number of connections rejected due to a wrong protocol header.
   * 
   * @return the number of connections rejected due to a wrong protocol header.
   */
  public int getProtocolErrorCount();

}
