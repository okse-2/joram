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

public interface AmqpConnectionsMBean {

  /**
   * Adds an AMQP server and starts a live connection with it, accessible via
   * the host and port provided. A server is uniquely identified by the given
   * name. Adding an existing server won't do anything.

   * @param name the name identifying the server
   * @param host host of the added server
   * @param port port of the added server
   */
  public void addServer(String name, String host, int port);

  /**
   * Adds an AMQP server and starts a live connection with it, accessible via
   * the host and port provided. A server is uniquely identified by the given
   * name. Adding an existing server won't do anything.
   * 
   * @param name the name identifying the server
   * @param host host of the added server
   * @param port port of the added server
   * @param user user name
   * @param pass user password
   */
  public void addServer(String name, String host, int port, String user, String pass);

  /**
   * Removes the live connection to the specified AMQP server.
   * 
   * @param name the name identifying the server
   */
  public void deleteServer(String name);

  /**
   * Gets the list of known servers.
   */
  public String[] getServerNames();

}