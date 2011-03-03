/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 ScalAgent Distributed Technologies
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
 * Contributor(s): 
 */
package org.objectweb.joram.mom.dest.amqp;

import com.rabbitmq.client.ConnectionFactory;

public interface AmqpConnectionHandlerMBean {

  /**
   * Adds an AMQP server and starts a live connection with it, accessible via
   * the {@link ConnectionFactory} provided. A server is uniquely identified
   * with its host and port. Adding an existing server won't do anything.
   * 
   * @param factory the factory used to access the server, configured properly
   *          (host, port, login, password...)
   */
  public void addServer(ConnectionFactory factory);

  /**
   * Adds an AMQP server and starts a live connection with it, accessible via
   * the {@link ConnectionFactory} provided. A server is uniquely identified
   * with its host and port. Adding an existing server won't do anything.
   * 
   * @param host host of the added server
   * @param port port of the added server
   */
  public void addServer(String host, int port);

  /**
   * Adds an AMQP server and starts a live connection with it, accessible via
   * the {@link ConnectionFactory} provided. A server is uniquely identified
   * with its host and port. Adding an existing server won't do anything.
   * 
   * @param host host of the added server
   * @param port port of the added server
   * @param user user name
   * @param pass user password
   */
  public void addServer(String host, int port, String user, String pass);

  /**
   * Removes the live connection to the specified AMQP server.
   * 
   * @param host host of the removed server
   * @param port port of the removed server
   */
  public void deleteServer(String host, int port);

}