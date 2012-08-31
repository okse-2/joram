/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 ScalAgent Distributed Technologies
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
package org.objectweb.joram.client.jms.admin;

import java.net.ConnectException;
import java.util.Hashtable;
import java.util.Properties;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.shared.admin.AdminReply;
import org.objectweb.joram.shared.admin.AdminRequest;

/**
 * The <code>AdminItf</code> interface defines the set of methods needed
 * for administration and monitoring of the  Joram platform.
 */
public interface AdminItf {
  /**
   * Set the maximum time in ms before aborting request.
   * 
   * @param timeOut the maximum time in ms before aborting request.
   * @throws ConnectException if the connection is not established.
   */
  public void setTimeOutToAbortRequest(long timeOut) throws ConnectException;

  /**
   * Returns the maximum time in ms before aborting request.
   * 
   * @return the maximum time in ms before aborting request.
   * @throws ConnectException if the connection is not established.
   */
  public long getTimeOutToAbortRequest() throws ConnectException;

  /**
   * Closes the underlying requestor.
   */
  public void close();

  /**
   * Returns true if the underlying requestor is closed.
   * 
   * @return true if the underlying requestor is closed.
   */
  public boolean isClosed();
  
  /**
   * Stops the platform local server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException    If the request fails.
   * 
   * @see #stopServer(int)
   */
  public void stopServer() throws ConnectException, AdminException;
 
  /**
   * Stops a given server of the platform.
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @param serverId  Identifier of the server to stop.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void stopServer(int serverId) throws ConnectException, AdminException;

  /**
   * Adds a server to the platform.
   * <p>
   * The server is configured without any service.
   *
   * @param sid     Id of the added server
   * @param host    Address of the host where the added server is started
   * @param domain  Name of the domain where the server is added
   * @param port    Listening port of the server in the specified domain
   * @param server  Name of the added server
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   * 
   * @see #addServer(int, String, String, int, String, String[], String[])
   */
  public void addServer(int sid,
                              String host,
                              String domain,
                              int port,
                              String server) throws ConnectException, AdminException;
  
  /**
   * Adds a server to the platform.
   *
   * @param sid       Id of the added server
   * @param host      Address of the host where the added server is started
   * @param domain    Name of the domain where the server is added
   * @param port      Listening port of the server in the specified domain
   * @param server    Name of the added server
   * @param services  Names of the service to start within the server
   * @param args      Services' arguments
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void addServer(int sid,
                              String host,
                              String domain,
                              int port,
                              String server,
                              String[] services,
                              String[] args) throws ConnectException, AdminException;

  /**
   * Removes a server from the platform.
   *
   * @param sid Id of the removed server
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void removeServer(int sid) throws ConnectException, AdminException;

  /**
   * Adds a domain to the platform.
   * <p>
   * The domain will use the default network component "SimpleNetwork".
   * 
   * @param domain    Name of the added domain.
   * @param sid       Id of the router server that gives access to the added domain.
   * @param port      Listening port in the added domain of the router server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void addDomain(String domain,
                              int sid,
                              int port) throws ConnectException, AdminException;

  /**
   * Adds a domain to the platform using a specific network component.
   *
   * @param domain      Name of the added domain.
   * @param network     Classname of the network component to use.
   * @param sid         Id of the router server that gives access to the added domain.
   * @param port        Listening port in the added domain of the router server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void addDomain(String domain,
                              String network,
                              int sid,
                              int port) throws ConnectException, AdminException;

  /**
   * Removes a domain from the platform.
   *
   * @param domain Name of the domain to remove
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void removeDomain(String domain) throws ConnectException, AdminException;

  /**
   * Returns the current servers configuration (a3servers.xml).
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public String getConfiguration() throws ConnectException, AdminException;

  /**
   * Returns statistics for the local server.
   *
   * @return  statistics for the local server.
   *          
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   * 
   * @see #getStatistics(int)
   */
  public Hashtable getStatistics() throws ConnectException, AdminException;

  /**
   * Returns statistics for the the specified server.
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @param serverId Unique identifier of the server.
   * @return  the statistics for the the specified server.
   * 
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public Hashtable getStatistics(int serverId) throws ConnectException, AdminException;
  
  /**
   * Returns the unique identifier of the default dead message queue for the local
   * server, null if not set.
   *
   * @return  The unique identifier of the default dead message queue for the local
   *          server, null if not set.
   *          
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   * 
   * @see #getDefaultDMQId(int)
   */
  public String getDefaultDMQId() throws ConnectException, AdminException;

  /**
   * Returns the unique identifier of the default dead message queue for a given
   * server, null if not set.
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @param serverId Unique identifier of the server.
   * @return  The unique identifier of the default dead message queue for the local
   *          server, null if not set.
   * 
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public String getDefaultDMQId(int serverId) throws ConnectException, AdminException;

  /**
   * Sets a given dead message queue as the default DMQ for the local server
   * (<code>null</code> for unsetting previous DMQ).
   *
   * @param dmqId  The dmqId (AgentId) to be set as the default one.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   * 
   * @see #setDefaultDMQId(int, String)
   */
  public void setDefaultDMQId(String dmqId) throws ConnectException, AdminException;

  /**
   * Sets a given dead message queue as the default DMQ for a given server
   * (<code>null</code> for unsetting previous DMQ).
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @param serverId  The identifier of the server.
   * @param dmqId  The dmqId (AgentId) to be set as the default one.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void setDefaultDMQId(int serverId, String dmqId) throws ConnectException, AdminException;
  
  /**
   * Returns the default dead message queue for the local server, null if not
   * set.
   *
   * @return  The default dead message queue for the local server, null if not set.
   * 
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   * 
   * @see #getDefaultDMQ(int)
   */
  public Queue getDefaultDMQ() throws ConnectException, AdminException;

  /**
   * Returns the default dead message queue for a given server, null if not set.
   * <p>
   * The request fails if the target server does not belong to the platform.
   * 
   * @param serverId Unique identifier of the server.
   * @return  The default dead message queue for the local server, null if not set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public Queue getDefaultDMQ(int serverId) throws ConnectException, AdminException;

  /**
   * Sets a given dead message queue as the default DMQ for the local server
   * (<code>null</code> for unsetting previous DMQ).
   *
   * @param dmq  The dmq to be set as the default one.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   * 
   * @see #setDefaultDMQ(int, Queue)
   */
  public void setDefaultDMQ(Queue dmq) throws ConnectException, AdminException;

  /**
   * Sets a given dead message queue as the default DMQ for a given server
   * (<code>null</code> for unsetting previous DMQ).
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @param serverId  The identifier of the server.
   * @param dmq  The dmq to be set as the default one.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void setDefaultDMQ(int serverId, Queue dmq) throws ConnectException, AdminException;

  /**
   * Returns the default threshold value for the local server, -1 if not set.
   *
   * @return The default threshold value for the local server, -1 if not set.
   * 
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   * 
   * @see #getDefaultThreshold(int)
   */
  public int getDefaultThreshold() throws ConnectException, AdminException;

  /**
   * Returns the default threshold value for a given server, -1 if not set.
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @return The default threshold value for the local server, -1 if not set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public int getDefaultThreshold(int serverId) throws ConnectException, AdminException;

  /**
   * Sets a given value as the default threshold for the local server (-1 for
   * unsetting previous value).
   *
   * @param threshold  The threshold value to be set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   * 
   * @see #setDefaultThreshold(int, int)
   */
  public void setDefaultThreshold(int threshold) throws ConnectException, AdminException;

  /**
   * Sets a given value as the default threshold for a given server (-1 for
   * unsetting previous value).
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @param serverId  The identifier of the server.
   * @param threshold  The threshold value to be set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void setDefaultThreshold(int serverId, int threshold) throws ConnectException, AdminException;

  /**
   * Returns the list of the platform's servers' identifiers.
   *
   * @return An array containing the list of server's identifiers.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   * 
   * @see #getServers(String)
   */
  public int[] getServersIds() throws ConnectException, AdminException;

  /**
   * Returns the list of the servers' identifiers that belong to the specified domain
   *
   * @param domain  Name of the domain.
   * @return An array containing the list of server's identifiers of the specified domain.
   * 
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public int[] getServersIds(String domain) throws ConnectException, AdminException;

  /**
   * Returns the list of the platform's servers' names.
   *
   * @return An array containing the list of server's names.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   * 
   * @see #getServers(String)
   */
  public String[] getServersNames() throws ConnectException, AdminException;

  /**
   * Returns the list of the servers' names that belong to the specified domain
   *
   * @param domain  Name of the domain.
   * @return An array containing the list of server's names of the specified domain.
   * 
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public String[] getServersNames(String domain) throws ConnectException, AdminException;
  
  /**
   * Returns the list of the platform's servers' descriptions.
   * 
   * @return An array containing the description of all servers.
   * 
   * @throws ConnectException
   * @throws AdminException
   * 
   * @see #getServers(String)
   */
  public Server[] getServers() throws ConnectException, AdminException;

  /**
   * Returns the list of the servers' that belong to the specified domain.
   *
   * @param domain  Name of the domain.
   * @return An array containing the description of the corresponding servers.
   * 
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public Server[] getServers(String domain) throws ConnectException, AdminException;
  
  /**
   * Returns the list of the domain names that contains the specified server.
   * 
   * @param serverId Unique identifier of the server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public String[] getDomainNames(int serverId) throws ConnectException, AdminException;

  /**
   * Returns the list of all destinations that exist on the local server.
   *
   * @return  An array containing all destinations defined on the given server
   *          or null if none exists.
   * 
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  Never thrown.
   * 
   * @see #getDestinations(int)
   */
  public Destination[] getDestinations() throws ConnectException, AdminException;

  /**
   * Returns the list of all destinations that exist on a given server.
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @param serverId  Unique identifier of the server.
   * @return  An array containing all destinations defined on the given server
   *          or null if none exists.
   * 
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public Destination[] getDestinations(int serverId) throws ConnectException, AdminException;

  /**
   * Creates or retrieves a queue destination on a given JORAM server.
   * <p>
   *
   * @param name       The name of the queue.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException    If the request fails.
   * 
   * @see #createQueue(int, String, String, Properties)
   */
  public Destination createQueue(String name) throws AdminException, ConnectException;
  
  /**
   * Creates or retrieves a queue destination on a given JORAM server.
   * <p>
   *
   * @param serverId   The identifier of the server where deploying the topic.
   * @param name       The name of the queue.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException    If the request fails.
   * 
   * @see #createQueue(int, String, String, Properties)
   */
  public Destination createQueue(int serverId, String name) throws AdminException, ConnectException;

  /**
   * Creates or retrieves a queue destination on a given JORAM server.
   * <p>
   * First a destination with the specified name is searched on the given
   * server, if it does not exist it is created.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployment fails server side.
   *
   * @param serverId   The identifier of the server where deploying the queue.
   * @param name       The name of the queue.
   * @param className  The queue class name.
   * @param prop       The queue properties.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException    If the request fails.
   */
  public Destination createQueue(int serverId,
                                 String name,
                                 String className,
                                 Properties prop) throws ConnectException, AdminException;

  /**
   * Creates or retrieves a topic destination on the underlying JORAM server.
   *
   * @param name       The name of the topic.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException    If the request fails.
   * 
   * @see #createTopic(int, String, String, Properties)
   */
  public Destination createTopic(String name) throws AdminException, ConnectException;

  /**
   * Creates or retrieves a topic destination on a given JORAM server.
   *
   * @param serverId   The identifier of the server where deploying the topic.
   * @param name       The name of the topic.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException    If the request fails.
   * 
   * @see #createTopic(int, String, String, Properties)
   */
  public Destination createTopic(int serverId, String name) throws AdminException, ConnectException;

  /**
   * Creates or retrieves a topic destination on a given JORAM server.
   * <p>
   * First a destination with the specified name is searched on the given
   * server, if it does not exist it is created.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployment fails server side.
   *
   * @param serverId   The identifier of the server where deploying the topic.
   * @param name       The name of the topic.
   * @param className  The topic class name.
   * @param prop       The topic properties.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException    If the request fails.
   */
  public Destination createTopic(int serverId,
                                 String name,
                                 String className,
                                 Properties prop) throws ConnectException, AdminException;

  /**
   * Returns the list of all users that exist on the local server.
   *
   * @return  An array containing all users defined on the local
   *          server, or null if none exist.
   * 
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   * 
   * @see #getUsers(int)
   */
  public User[] getUsers() throws ConnectException, AdminException;

  /**
   * Returns the list of all users that exist on a given server.
   * <p>
   * The request fails if the target server does not belong to the platform.
   * 
   * @param serverId  Unique identifier of the given server.
   * @return  An array containing all users defined on the local
   *          server, or null if none exist.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public User[] getUsers(int serverId) throws ConnectException, AdminException;
  
  /**
   * Creates or retrieves a user on the underlying JORAM server.
   *
   * @param name                Name of the user.
   * @param password            Password of the user.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException    If the request fails.
   * 
   * @see #createUser(String, String, int, String)
   */
  public User createUser(String name, String password) throws ConnectException, AdminException;

  /**
   * Creates or retrieves a user on the underlying JORAM server.
   *
   * @param name                Name of the user.
   * @param password            Password of the user.
   * @param serverId            The identifier of the user's server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException    If the request fails.
   * 
   * @see #createUser(String, String, int, String)
   */
  public User  createUser(String name, String password, int serverId) throws ConnectException, AdminException;

  /**
   * Creates or retrieves a user on the underlying JORAM server.
   *
   * @param name            Name of the user.
   * @param password        Password of the user.
   * @param identityClass   Classname for authentication, by default SimpleIdentity for user/password.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   * 
   * @see #createUser(String, String, int, String)
   */
  public User createUser(String name, String password,
                         String identityClass) throws AdminException, ConnectException;
  
  /**
   * Admin method creating a user for a given server and instantiating the
   * corresponding <code>User</code> object.
   * <p>
   * If the user has already been set on this server, the method simply
   * returns the corresponding <code>User</code> object. Its fails if the
   * target server does not belong to the platform, or if a proxy could not
   * be deployed server side for a new user. 
   *
   * @param name                Name of the user.
   * @param password            Password of the user.
   * @param serverId            The identifier of the user's server.
   * @param identityClassName   By default user/password for SimpleIdentity.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException    If the request fails.
   */ 
  public User createUser(String name, String password,
                         int serverId,
                         String identityClassName) throws ConnectException, AdminException;
  
  /**
   * Admin method creating a user for a given server and instantiating the
   * corresponding <code>User</code> object.
   * <p>
   * If the user has already been set on this server, the method simply
   * returns the corresponding <code>User</code> object. Its fails if the
   * target server does not belong to the platform, or if a proxy could not
   * be deployed server side for a new user. 
   *
   * @param name                Name of the user.
   * @param password            Password of the user.
   * @param serverId            The identifier of the user's server.
   * @param identityClassName   By default user/password for SimpleIdentity.
   * @param prop								properties
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException    If the request fails.
   */ 
  public User createUser(String name, String password,
                         int serverId,
                         String identityClassName,
                         Properties prop) throws ConnectException, AdminException;
  
//  /**
//   * Create a user Identity.
//   * 
//   * @param user              Name of the user.
//   * @param passwd            Password of the user.
//   * @param identityClassName identity class name (simple, jaas).
//   * @return identity user Identity.
//   * @throws AdminException
//   */
//  private Identity createIdentity(String user, String passwd, String identityClassName);

  /**
   * Returns the information about the current server: unique identifier, symbolic name and hostname.
   *
   * @return The description of the server.
   * 
   * @exception ConnectException  If the connection fails.
   * @exception AdminException    If the request fails.
   */
  public Server getLocalServer() throws ConnectException, AdminException;

  /**
   * Returns the identifier of the server the module is connected to.
   *
   * @exception ConnectException  If the admin connection is not established.
   * @exception AdminException    If the request fails.
   * 
   * @see #getLocalServer()
   */
  public int getLocalServerId() throws ConnectException, AdminException;

  /**
   * Returns the host name of the server the module is connected to.
   *
   * @exception ConnectException  If the admin connection is not established.
   * @exception AdminException    If the request fails.
   * 
   * @see #getLocalServer()
   */
  public String getLocalHost() throws ConnectException, AdminException;

  /**
   * Returns the port number of the server the module is connected to.
   *
   * @exception ConnectException  If the admin connection is not established.
   * @exception AdminException    If the request fails.
   * 
   * @see #getLocalServer()
   */
  public String getLocalName() throws ConnectException, AdminException;

  /**
   * The method send the admin JMS message on JORAM server (AdminTopic).
   * 
   * @param targetId agent Id target.
   * @param command the command to execute.
   * @param prop the properties.
   * @return the reply.
   * @exception AdminException    
   * @exception ConnectException  If the connection fails.
   */
  public AdminReply processAdmin(String targetId, int command, Properties prop) throws ConnectException, AdminException;
  
  /**
   * Method actually sending an <code>AdminRequest</code> instance to
   * the platform and getting an <code>AdminReply</code> instance.
   * 
   * @param request the administration request to send
   * @return  the reply message
   *
   * @exception ConnectException  If the connection to the platform fails.
   * @exception AdminException  If the platform's reply is invalid, or if
   *              the request failed.
   */
  public AdminReply doRequest(AdminRequest request) throws AdminException, ConnectException;

  public void abortRequest() throws ConnectException;
  
  /**
   * Invokes the specified static method with the specified parameters on the
   * chosen server. The parameters types of the invoked method must be java
   * primitive types, the java objects wrapping them or String type.
   * 
   * @param serverId the identifier of the server.
   * @param className the name of the class holding the static method
   * @param methodName the name of the invoked method
   * @param parameterTypes the list of parameters
   * @param args the arguments used for the method call
   * @return the result of the invoked method after applying
   *         {@link Object#toString()} method
   * @throws ConnectException If the connection fails.
   * @throws AdminException If the invocation can't be done or fails
   */
  public String invokeStaticServerMethod(int serverId, String className, String methodName,
      Class<?>[] parameterTypes, Object[] args) throws ConnectException, AdminException;
  
  /**
   * Adds an AMQP server and starts a live connection with it, accessible via
   * the url provided. A server is uniquely identified by the given
   * name. Adding an existing server won't do anything.
   * 
   * @param serverId the serverId
   * @param urls the amqp url list identifying the servers separate by space.
   * ex: amqp://user:pass@localhost:5672/?name=serv1 amqp://user:pass@localhost:5678/?name=serv2
   * 
   * @return the result of the method
   * @throws ConnectException If the connection fails.
   * @throws AdminException If the invocation can't be done or fails
   */
  public String addAMQPBridgeConnection(int serverId, String urls) throws ConnectException, AdminException;
  
  /**
   * Removes the live connection to the specified AMQP server.
   * 
   * @param serverId the serverId
   * @param names the name identifying the server or list of name separate by space
   * @return the result of the method
   * @throws ConnectException If the connection fails.
   * @throws AdminException If the invocation can't be done or fails
   */
  public String deleteAMQPBridgeConnection(int serverId, String names) throws ConnectException, AdminException;
  
  /**
   * Adds a JMS server and starts a live connection with it, accessible via
   * the url provided. A server is uniquely identified by the given
   * name. Adding an existing server won't do anything.
   * 
   * @param serverId the serverId
   * @param urls the jms url list identifying the servers separate by space.
   * ex: jndi_url/?name=cnx1&cf=cfName&jndiFactoryClass=com.xxx.yyy&user=user1&pass=pass1&clientID=clientID 
   * 
   * @return the result of the method
   * @throws ConnectException If the connection fails.
   * @throws AdminException If the invocation can't be done or fails
   */
  public String addJMSBridgeConnection(int serverId, String urls) throws ConnectException, AdminException;
  
  /**
   * Removes the live connection to the specified AMQP server.
   * 
   * @param serverId the serverId
   * @param names the name identifying the server or list of name separate by space
   * @return the result of the method
   * @throws ConnectException If the connection fails.
   * @throws AdminException If the invocation can't be done or fails
   */
  public String deleteJMSPBridgeConnection(int serverId, String names) throws ConnectException, AdminException;
}
