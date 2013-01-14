/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2009 ScalAgent Distributed Technologies
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
package org.objectweb.joram.client.connector;

import java.net.ConnectException;

import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;

public interface JoramAdapterMBean {
	 /**
	  * return the activity of the Joram administration connection.
	  * @return true if connection admin connection is active.
	  */
	public boolean isActive();
	
	/**
	 * get the duration of admin connection before change state.
	 * @return the duration of admin connection before change state.
	 */
	public long getAdminDurationBeforeChangeState();
	
	/**
	 * get the duration of admin connection before change state.
	 * @return the duration of admin connection before change state.
	 */
	public String getAdminDurationBeforeChangeStateDate();
	
  /**
   * Gets the JMS API version.
   * 
   * @return The JMS API version.
   */
  public String getJMSVersion();

  /**
   * Get the provider name: Joram.
   * 
   * @return The provider name: Joram.
   */
  public String getJMSProviderName();

  /**
   * Gets the Joram's implementation version.
   * 
   * @return The Joram's implementation version.
   */
  public String getProviderVersion();

  /**
   * Returns the unique identifier of the Joram server.
   * 
   * @return The unique identifier of the Joram server.
   */
  public Short getServerId();

  /**
   * Returns the name of the resource adapter.
   * 
   * @return The name of the resource adapter.
   */
  public String getName();

  /**
   * Returns the location of the Joram server.
   * 
   * @return The location of the Joram server.
   */
  public String getHostName();

  /**
   * Returns the listening port of the Joram server.
   * 
   * @return The listening port of the Joram server.
   */
  public Integer getServerPort();

//  /**
//   * Returns <code>true</code> if the Joram server is persistent.
//   * 
//   * @return <code>true</code> if the Joram server is persistent.
//   */
//  public Boolean getPersistentPlatform();

  /**
   * Returns <code>true</code> if the Joram server is collocated.
   * 
   * @return <code>true</code> if the Joram server is collocated.
   */
  public Boolean getCollocated();


  /**
   * Duration in seconds during which connecting is attempted (connecting
   * might take time if the server is temporarily not reachable); the 0 value
   * is set for connecting only once and aborting if connecting failed.
   */
  public Integer getConnectingTimer();

//  /**
//   * Duration in seconds during which a JMS transacted (non XA) session might
//   * be pending; above that duration the session is rolled back and closed;
//   * the 0 value means "no timer".
//   */
//  public Integer getTxPendingTimer();
//
//  /**
//   * Period in milliseconds between two ping requests sent by the client
//   * connection to the server; if the server does not receive any ping
//   * request during more than 2 * cnxPendingTimer, the connection is
//   * considered as dead and processed as required.
//   */
//  public Integer getCnxPendingTimer();

  /**
   * Sets timeout before abort a request.
   * 
   * @param timeOut timeout before abort a request.
   * @throws ConnectException 
   * 
   * @see AdminModule#setTimeOutToAbortRequest(long)
   */
  public void setTimeOutToAbortRequest(long timeOut) throws ConnectException;

  /**
   * Gets timeout before abort a request.
   * 
   * @return timeout before abort a request.
   * @throws ConnectException 
   * 
   * @see AdminModule#getTimeOutToAbortRequest()
   */
  public long getTimeOutToAbortRequest() throws ConnectException;


  /**
   * Returns the unique identifier of the default dead message queue for the local
   * server, null if not set.
   *
   * @return The unique identifier of the dead message queue of the local
   *         server or null if none exists.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public String getDefaultDMQId() throws ConnectException, AdminException;

  /**
   * Returns the unique identifier of the default dead message queue for a given
   * server, null if not set.
   * <p>
   * The request fails if the target server does not belong to the platform.
   * 
   * @param serverId Unique identifier of the given server.
   * @return The unique identifier of the dead message queue of the given
   *         server or null if none exists.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public String getDefaultDMQId(short serverId) throws ConnectException, AdminException;

  /**
   * Unset the default dead message queue for the given server.
   * 
   * @param serverId Unique identifier of the hiven server.
   * @throws ConnectException
   * @throws AdminException
   */
  public void resetDefaultDMQ(short serverId) throws ConnectException, AdminException;


  /**
   * Returns the default threshold of the Joram server.
   * 
   * @return the default threshold of the Joram server.
   * @see AdminModule#getDefaultThreshold()
   */
  public int getDefaultThreshold() throws ConnectException, AdminException;

  /**
   * Returns the default threshold of the given Joram server.
   * 
   * @param serverId  Unique identifier of the given Joram server.
   * @return the default threshold of the given Joram server.
   * @see AdminModule#getDefaultThreshold(int)
   */
  public int getDefaultThreshold(short serverId) throws ConnectException, AdminException;

  /**
   * Sets the default threshold of the Joram server.
   * 
   * @param threshold the default threshold of the Joram server.
   * @see AdminModule#setDefaultThreshold(int)
   */
  public void setDefaultThreshold(int threshold) throws ConnectException, AdminException;

  /**
   * Sets the default threshold of the given Joram server.
   * 
   * @param serverId  Unique identifier of the given Joram server.
   * @param threshold the default threshold of the given Joram server.
   * @see AdminModule#setDefaultThreshold(int, int)
   */
  public void setDefaultThreshold(short serverId, int threshold) throws ConnectException, AdminException;

  /**
   * Returns the list of all destinations that exist on the local server.
   * This method creates and registers MBeans for all the destinations of
   * the selected servers.
   *
   * @return  An array containing the object name of all destinations defined
   *          on the given server or null if none exists.
   * 
   * @exception ConnectException  If the connection is closed or broken.
   * @exception AdminException    Never thrown.
   * 
   * @see #getDestinations(short)
   */
  public String[] getDestinations() throws ConnectException, AdminException;

  /**
   * Returns the list of all destinations that exist on the given server.
   * This method creates and registers MBeans for all the destinations of
   * the selected servers.
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @return  An array containing the object name of all destinations defined
   *          on the given server or null if none exists.
   * 
   * @exception ConnectException  If the connection is closed or broken.
   * @exception AdminException    Never thrown.
   * 
   * @see #getDestinations(short)
   */
  public String[] getDestinations(short serverId) throws ConnectException, AdminException;

  /**
   * Creates or retrieves a queue destination on the underlying JORAM server,
   * (re)binds the corresponding <code>Queue</code> instance.
   *
   * @param name       The name of the queue.
   *
   * @exception AdminException   If the creation fails.
   * @exception ConnectException if the connection is closed or broken
   * 
   * @see #createQueue(short, String)
   */
  public String createQueue(String name) throws AdminException, ConnectException;

  /**
   * Creates or retrieves a queue destination on the underlying JORAM server,
   * (re)binds the corresponding <code>Queue</code> instance.
   *
   * @param serverId   The identifier of the server where deploying the queue.
   * @param name       The name of the queue.
   *
   * @exception AdminException   If the creation fails.
   * @exception ConnectException if the connection is closed or broken
   */
  public String createQueue(short serverId, String name) throws AdminException, ConnectException;


  /**
   * Creates or retrieves a topic destination on the underlying JORAM server,
   * (re)binds the corresponding <code>Topic</code> instance.
   *
   * @param name       The name of the topic.
   *
   * @exception AdminException   If the creation fails.
   * @exception ConnectException if the connection is closed or broken
   * 
   * @see #createTopic(int, String)
   */
  public String createTopic(String name) throws AdminException, ConnectException;

  /**
   * Creates or retrieves a topic destination on the underlying JORAM server,
   * (re)binds the corresponding <code>Topic</code> instance.
   *
   * @param serverId   The identifier of the server where deploying the topic.
   * @param name       The name of the topic.
   *
   * @exception AdminException   If the creation fails.
   * @exception ConnectException if the connection is closed or broken
   */
  public String createTopic(short serverId, String name) throws AdminException, ConnectException;


  /**
   * Remove a destination specified by its JNDI name on the underlying
   * JORAM platform.
   *
   * @param name       The JNDI name of the destination.
   */
  public void removeDestination(String name) throws AdminException;

  /**
   * Returns the list of all users that exist on the local server.
   * This method creates and registers MBeans for all the users of
   * the selected servers.
   *
   * @return  An array containing the object name of all users defined
   *          on the given server or null if none exists.
   * 
   * @exception ConnectException  If the connection fails.
   * @exception AdminException    Never thrown.
   * 
   * @see #getUsers(short)
   */
  public String[] getUsers() throws ConnectException, AdminException;

  /**
   * Returns the list of all users that exist on a given server.
   * This method creates and registers MBeans for all the users of
   * the selected servers.
   * <p>
   * The request fails if the target server does not belong to the platform.
   * 
   * @param serverId  Unique identifier of the given server.
   * @return  An array containing the object name of all users defined
   *          on the given server or null if none exists.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException    If the request fails.
   */
  public String[] getUsers(short serverId) throws ConnectException, AdminException;

  /**
   * Creates or retrieves a user on the underlying JORAM server.
   *
   * @param name      The login name of the user.
   * @param password  The password of the user.
   * @return The object name of created user.
   * 
   * @exception AdminException    If the creation fails.
   * @exception ConnectException  If the connection fails.
   * 
   * @see #createUser(String, String, short, String)
   */
  public String createUser(String name, String password) throws AdminException, ConnectException;

  /**
   * Creates or retrieves a user on the underlying JORAM server.
   *
   * @param name          The login name of the user.
   * @param password      The password of the user.
   * @param identityClass The identity class used for authentication.
   * @return The object name of created user.
   * 
   * @exception AdminException    If the creation fails.
   * @exception ConnectException  If the connection fails.
   * 
   * @see #createUser(String, String, short, String)
   */
  public String createUser(String name, String password,
                           String identityClass) throws AdminException, ConnectException;

  /**
   * Creates or retrieves a user on the given JORAM server.
   *
   * @param name      The login name of the user.
   * @param password  The password of the user.
   * @param serverId  The unique identifier of the Joram server.
   * @return The object name of created user.
   * 
   * @exception AdminException    If the creation fails.
   * @exception ConnectException  If the connection fails.
   * 
   * @see #createUser(String, String, short, String)
   */
  public String createUser(String name, String password,
                           short serverId) throws AdminException, ConnectException;

  /**
   * Creates or retrieves a user on the underlying JORAM server.
   *
   * @param name          The login name of the user.
   * @param password      The password of the user.
   * @param serverId      The unique identifier of the Joram server.
   * @param identityClass The identity class used for authentication.
   * @return The object name of created user.
   * 
   * @exception AdminException    If the creation fails.
   * @exception ConnectException  If the connection fails.
   */
  public String createUser(String name, String password,
                           short serverId,
                           String identityClass) throws ConnectException, AdminException;

  /**
   * Creates a non managed connection factory and binds it to JNDI.
   * 
   * @param name Name of created connection factory.
   */
  public void createCF(String name) throws Exception;

  /**
   * Creates a non managed PTP connection factory and binds it to JNDI.
   * 
   * @param name Name of created connection factory.
   */
  public void createQueueCF(String name);

  /**
   * Creates a non managed PubSub connection factory and binds it to JNDI.
   * 
   * @param name Name of created connection factory.
   */
  public void createTopicCF(String name);

  /**
   * Path to the directory containing JORAM's configuration files
   * (<code>a3servers.xml</code>, <code>a3debug.cfg</code>
   * and admin file), needed when StartJoramServer is set to true.
   */
  public String getPlatformConfigDir();

  /**
   * Path to the XML file containing a description of the administered objects
   * to create and bind.
   */
  public String getAdminFileXML();

  public String getAdminFileExportXML();

  public void executeXMLAdmin(String path) throws Exception;

  /**
   * Export the repository content to an XML file with default filename.
   * - only the destinations objects are retrieved in this version
   * - xml script format of the admin objects (joramAdmin.xml)
   * 
   * @param exportDir       target directory where the export file will be put
   * @throws AdminException if an error occurs
   */
  public void exportRepositoryToFile(String exportDir) throws AdminException;

  /**
   * Export the repository content to an XML file
   * - only the destinations objects are retrieved in this version
   * - xml script format of the admin objects (joramAdmin.xml)
   * 
   * @param exportDir       target directory where the export file will be put
   * @param exportFilename  filename of the export file
   * @throws AdminException if an error occurs
   */
  public void exportRepositoryToFile(String exportDir,
                                     String exportFilename) throws AdminException;

  /**
   * Returns the list of the platform's servers' identifiers.
   * 
   * @return an array containing the list of the platform's servers' identifiers.
   * @throws AdminException If the request fails.
   * @throws ConnectException If the connection fails.
   */
  public Short[] getServersIds() throws ConnectException, AdminException;

  /**
   * Returns the list of the platform's servers' names.
   *
   * @return An array containing the list of server's names.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public String[] getServersNames() throws ConnectException, AdminException;

  /**
   * Returns the current servers configuration (a3servers.xml).
   *
   * @return The current servers configuration.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public String getConfiguration() throws ConnectException, AdminException;
  
  /**
   * The persistence directory of the JORAM server to start,
   * needed when StartJoramServer is set to true.
   * 
   * @return the persistence directory.
   */
  public String getStorage();
  
  /** 
   * The Joram resource adapter jndi name.
   * 
   * @return the Joram resource adapter jndi name. 
   */
  public String getJndiName();
  
  /** 
   * login name for administrator. 
   */
  public String getRootName();
  
  /** 
   * Identity class needed for authentication 
   */
  public String getIdentityClass();
  
  /** 
   * true if the resource adapter start the JoramServer 
   */
  public Boolean getStartJoramServer();
  
  // *****************************************************************************************
  // Code to remove in future

  /**
   * Returns true if the server is colocated.
   * 
   * @see org.objectweb.joram.client.connector.JoramAdapterMBean#getCollocatedServer()
   * @deprecated only needed for compatibility with JOnAS 5.2 administration GUI
   */
  public Boolean getCollocatedServer();
  
  /**
   * Returns the server name.
   * 
   * @see org.objectweb.joram.client.connector.JoramAdapterMBean#getServerName()
   * @deprecated only needed for compatibility with JOnAS 5.2 administration GUI
   */
  public String getServerName();
  
  //*****************************************************************************************
}
