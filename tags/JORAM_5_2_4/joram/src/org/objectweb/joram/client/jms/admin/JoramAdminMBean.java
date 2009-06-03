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
 * Contributor(s): Benoit Pelletier (Bull SA)
 */
package org.objectweb.joram.client.jms.admin;

import java.net.ConnectException;
import java.util.Properties;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.shared.admin.AddDomainRequest;
import org.objectweb.joram.shared.admin.AddServerRequest;
import org.objectweb.joram.shared.admin.GetConfigRequest;
import org.objectweb.joram.shared.admin.RemoveDomainRequest;
import org.objectweb.joram.shared.admin.RemoveServerRequest;
import org.objectweb.joram.shared.admin.StopServerRequest;

/**
 *
 */
public interface JoramAdminMBean {
  public void setTimeOutToAbortRequest(long timeOut) throws ConnectException;

  public long getTimeOutToAbortRequest() throws ConnectException;

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
   * @param serverId Id of the added server
   * @param host Address of the host where the added server is started
   * @param domain Name of the domain where the server is added
   * @param port Listening port of the server in the specified domain
   * @param server Name of the added server
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
   * @param serverId Id of the added server
   * @param host Address of the host where the added server is started
   * @param domain Name of the domain where the server is added
   * @param port Listening port of the server in the specified domain
   * @param server Name of the added server
   * @param services Names of the service to start within the server
   * @param args Services' arguments
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
   * The domain will use the default network component "Simplenetwork".
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
   * Returns the current servers configuration (equivalent to the a3servers.xml file).
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public String getConfiguration() throws ConnectException, AdminException;
  
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
   * Returns the list of the domain names that contains the specified server.
   * 
   * @param serverId Unique identifier of the server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public String[] getDomainNames(int serverId) throws ConnectException, AdminException;

  /**
   * Returns the unique identifier of the default dead message queue for the local
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
  public void setDefaultDMQId(String dmqId) throws ConnectException, AdminException ;
  
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

  public Destination[] getDestinations(int serverId) throws ConnectException, AdminException;

  public Destination[] getDestinations() throws ConnectException, AdminException;

  public Destination createQueue(String name) throws ConnectException, AdminException;

  public Destination createQueue(int serverId, String name) throws ConnectException, AdminException;

  public Destination createQueue(int serverId,
                                 String name,
                                 String className,
                                 Properties prop) throws ConnectException, AdminException;

  public Destination createTopic(String name) throws ConnectException, AdminException;

  public Destination createTopic(int serverId, String name) throws ConnectException, AdminException;

  public Destination createTopic(int serverId,
                                 String name,
                                 String className,
                                 Properties prop) throws ConnectException, AdminException;


  public User[] getUsers(int serverId) throws ConnectException, AdminException;

  public User[] getUsers() throws ConnectException, AdminException;

  public User createUser(String name, String password) throws ConnectException, AdminException;

  public User createUser(String name, String password,
                         int serverId) throws ConnectException, AdminException;

  public User createUser(String name, String password,
                         int serverId,
                         String identityClass) throws ConnectException, AdminException;

  public User createUser(String name, String password,
                         String identityClass) throws ConnectException, AdminException;

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
   * This method execute the XML script file that the location is given
   * in parameter.
   * <p>
   * Be careful, currently this method use the static administration connection
   * through the AdminModule Class.
   *
   * @param cfgDir        The directory containing the file.
   * @param cfgFileName   The script filename.
   * @return
   */
  public boolean executeXMLAdmin(String cfgDir,
                                 String cfgFileName) throws Exception;

  /**
   * This method execute the XML script file that the pathname is given
   * in parameter.
   * <p>
   * Be careful, currently this method use the static administration connection
   * through the AdminModule Class.
   *
   * @param path    The script pathname.
   */
  public boolean executeXMLAdmin(String path) throws Exception;
}
