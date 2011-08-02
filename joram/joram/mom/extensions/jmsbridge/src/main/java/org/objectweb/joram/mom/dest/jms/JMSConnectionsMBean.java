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
package org.objectweb.joram.mom.dest.jms;

public interface JMSConnectionsMBean {

  /**
   * Adds a JMS server and starts a live connection with it, accessible using
   * the connection factory retrieved on the JNDI server. A server is uniquely
   * identified by the name of the connection factory. Adding an existing server
   * won't do anything.
   * 
   * @param cnxFactoryName the name identifying the connection factory on the
   *          JNDI server
   */
  public void addServer(String cnxFactoryName);

  /**
   * Adds a JMS server and starts a live connection with it, accessible using
   * the connection factory retrieved on the JNDI server. A server is uniquely
   * identified by the name of the connection factory. Adding an existing server
   * won't do anything.
   * 
   * @param cnxFactoryName the name identifying the connection factory on the
   *          JNDI server
   * @param jndiFactoryClass the JNDI connection factory class name
   * @param jndiUrl the JNDI URL
   */
  public void addServer(String cnxFactoryName, String jndiFactoryClass, String jndiUrl);

  /**
   * Adds a JMS server and starts a live connection with it, accessible using
   * the connection factory retrieved on the JNDI server. A server is uniquely
   * identified by the name of the connection factory. Adding an existing server
   * won't do anything.
   * 
   * @param cnxFactoryName the name identifying the connection factory on the
   *          JNDI server
   * @param jndiFactoryClass the JNDI connection factory class name
   * @param jndiUrl the JNDI URL
   * @param user user name
   * @param password user password
   */
  public void addServer(String cnxFactoryName, String jndiFactoryClass, String jndiUrl, String user,
      String password);

  /**
   * Adds a JMS server and starts a live connection with it, accessible using
   * the connection factory retrieved on the JNDI server. A server is uniquely
   * identified by the name of the connection factory. Adding an existing server
   * won't do anything.
   * 
   * @param cnxFactoryName the name identifying the connection factory on the
   *          JNDI server
   * @param jndiFactoryClass the JNDI connection factory class name
   * @param jndiUrl the JNDI URL
   * @param user user name
   * @param password user password
   * @param clientID the client identifier for this connection
   */
  public void addServer(String cnxFactoryName, String jndiFactoryClass, String jndiUrl, String user,
      String password, String clientID);

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