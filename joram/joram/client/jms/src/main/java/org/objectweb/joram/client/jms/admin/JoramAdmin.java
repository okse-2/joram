/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2011 ScalAgent Distributed Technologies
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
import java.util.Iterator;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.shared.admin.AdminReply;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 * JoramAdmin is the implementation of the interface JoramAdminMBean.
 * It must only be used to allow administration through JMX.
 * 
 * @see AdminModule
 * @see AdminWrapper
 */
public class JoramAdmin implements JoramAdminMBean {

  public static Logger logger = Debug.getLogger(JoramAdmin.class.getName());

  public AdminWrapper wrapper = null;
  
  /**
   * Creates a MBean to administer Joram using the default basename for JMX
   * registering ({@link #JoramAdmin(Connection, String)}).
   * Be careful, if the connection is not started this method will failed with
   * a ConnectException.
   * 
   * @param cnx A valid connection to the Joram server.
   * @throws JMSException A problem occurs during initialization.
   * 
   * @see AdminWrapper#AdminWrapper(Connection)
   */
  public JoramAdmin(Connection cnx) throws ConnectException, AdminException, JMSException {
    this(cnx, "JoramClient");
  }

  /**
   * Creates a MBean to administer Joram using the given basename for JMX registering.
   * Be careful, if the connection is not started this method will failed with a
   * ConnectException.
   * 
   * @param cnx   A valid connection to the Joram server.
   * @param base  the basename for registering the MBean.
   * 
   * @throws JMSException A problem occurs during initialization.
   * 
   * @see AdminWrapper#AdminWrapper(Connection)
   */
  public JoramAdmin(Connection cnx, String base) throws ConnectException, AdminException, JMSException {
    wrapper = new AdminWrapper(cnx);
    registerMBean(base);
  }

  /**
   * Closes the administration connection and unregister the MBean.
   */
  public void exit() {
    try {
      Iterator mbeans = MXWrapper.queryNames(JMXBaseName + ":type=User,location=*,name=*").iterator();
      while (mbeans.hasNext()) {
        String name = (String) mbeans.next();
        System.out.println("unregisterMBean: " + name);
        MXWrapper.unregisterMBean(name);
      }

      mbeans = MXWrapper.queryNames(JMXBaseName + ":type=Queue,location=*,name=*").iterator();
      while (mbeans.hasNext()) {
        String name = (String) mbeans.next();
        System.out.println("unregisterMBean: " + name);
        MXWrapper.unregisterMBean(name);
      }

      mbeans = MXWrapper.queryNames(JMXBaseName + ":type=Topic,location=*,name=*").iterator();
      while (mbeans.hasNext()) {
        String name = (String) mbeans.next();
        System.out.println("unregisterMBean: " + name);
        MXWrapper.unregisterMBean(name);
      }
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "JoramAdmin.unregisterMBean",exc);
    }
    unregisterMBean();
    wrapper.close();
  }
  
  /**
   * Specifies how much time a command has to complete before If the command
   * does not complete within the specified time, it is canceled and an exception
   * is generated.
   * <p>
   * Be careful, the value can be changed prior to the connection only using
   * the <code>AdminRequestor.REQUEST_TIMEOUT_PROP</code> property.
   * 
   * @param timeOut the maximum time before a command is canceled.
   * @throws ConnectException if the connection is not established.
   */
  public void setTimeOutToAbortRequest(long timeOut) throws ConnectException {
    wrapper.setTimeOutToAbortRequest(timeOut);
  }

  /**
   * Gets the maximum time a command has to complete before it is canceled.
   * 
   * @return the maximum time before a command is canceled
   * @throws ConnectException if the connection is not established.
   */
  public long getTimeOutToAbortRequest() throws ConnectException {
    return wrapper.getTimeOutToAbortRequest();
  }
  
  /**
   * Stops the platform local server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException    If the request fails.
   * 
   * @see #stopServer(int)
   */
  public void stopServer() throws ConnectException, AdminException {
    wrapper.stopServer();
  }
 
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
  public void stopServer(int serverId) throws ConnectException, AdminException {
    wrapper.stopServer(serverId);
  }

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
   */
  public void addServer(int sid,
                        String host,
                        String domain,
                        int port,
                        String server) throws ConnectException, AdminException {
    wrapper.addServer(sid, host, domain, port, server);
  }

  /**
   * Removes a server from the platform.
   *
   * @param sid Id of the removed server
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void removeServer(int sid) throws ConnectException, AdminException {
    wrapper.removeServer(sid);
  }

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
                        int port) throws ConnectException, AdminException {
    wrapper.addDomain(domain, sid, port);
  }

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
                        int port) throws ConnectException, AdminException {
    wrapper.addDomain(domain, network, sid, port);
  }

  /**
   * Removes a domain from the platform.
   *
   * @param domain Name of the domain to remove
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void removeDomain(String domain) throws ConnectException, AdminException {
    wrapper.removeDomain(domain);
  }

  /**
   * Returns the current servers configuration (equivalent to the a3servers.xml file).
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public String getConfiguration() throws ConnectException, AdminException {
    return wrapper.getConfiguration();
  }
  
  /**
   * Returns the list of the platform's servers' identifiers.
   *
   * @return An array containing the list of server's identifiers.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   * 
   * @see #getServersIds(String)
   */
  public int[] getServersIds() throws ConnectException, AdminException {
    return wrapper.getServersIds();
  }

  /**
   * Returns the list of the servers' identifiers that belong to the specified domain
   *
   * @param domain  Name of the domain.
   * @return An array containing the list of server's identifiers of the specified domain.
   * 
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public int[] getServersIds(String domain) throws ConnectException, AdminException {
    return wrapper.getServersIds(domain);
  }
  
  /**
   * Returns the list of the domain names that contains the specified server.
   * 
   * @param serverId Unique identifier of the server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public String[] getDomainNames(int serverId) throws ConnectException, AdminException {
    return wrapper.getDomainNames(serverId);
  }
  
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
  public String getDefaultDMQId() throws ConnectException, AdminException {
    return wrapper.getDefaultDMQId();
  }

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
  public String getDefaultDMQId(int serverId) throws ConnectException, AdminException {
    return wrapper.getDefaultDMQId(serverId);
  }

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
  public void setDefaultDMQId(String dmqId) throws ConnectException, AdminException {
    wrapper.setDefaultDMQId(dmqId);
  }

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
  public void setDefaultDMQId(int serverId, String dmqId) throws ConnectException, AdminException {
    wrapper.setDefaultDMQId(serverId, dmqId);
  }

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
  public void setDefaultThreshold(int threshold) throws ConnectException, AdminException {
    wrapper.setDefaultThreshold(threshold);
  }
  
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
  public void setDefaultThreshold(int serverId, int threshold) throws ConnectException, AdminException {
    wrapper.setDefaultThreshold(serverId, threshold);
  }

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
  public int getDefaultThreshold() throws ConnectException, AdminException {
    return wrapper.getDefaultThreshold();
  }

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
  public int getDefaultThreshold(int serverId) throws ConnectException, AdminException {
    return wrapper.getDefaultThreshold(serverId);
  }
  
  /**
   * This method creates and registers MBeans for all the destinations on
   * the local server.
   *
   * @exception ConnectException  If the connection is closed or broken.
   * @exception AdminException    Never thrown.
   * 
   * @see #getDestinations(int)
   */
  public void getDestinations() throws ConnectException, AdminException {
    wrapDestinations(wrapper.getDestinations());
  }

  /**
   * This method creates and registers MBeans for all the destinations of
   * the selected server.
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @exception ConnectException  If the connection is closed or broken.
   * @exception AdminException    Never thrown.
   * 
   * @see #getDestinations()
   */
  public void getDestinations(int serverId) throws ConnectException, AdminException {
    wrapDestinations(wrapper.getDestinations(serverId));
  }
  
  private final void wrapDestinations(Destination[] destinations) {
    if (destinations == null) return;
    
    for (int i=0; i<destinations.length; i++) {
      wrapDestination(destinations[i]);
    }
  }
  
  /**
   * Creates or retrieves a queue destination on the underlying JORAM server,
   * (re)binds the corresponding <code>Queue</code> instance.
   *
   * @param name       The name of the queue.
   *
   * @exception AdminException   If the creation fails.
   * @exception ConnectException if the connection is closed or broken
   * 
   * @see #createQueue(int, String)
   */
  public void createQueue(String name) throws AdminException, ConnectException {
    createQueue(wrapper.getLocalServerId(), name);
  }

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
  public void createQueue(int serverId, String name) throws AdminException, ConnectException {
    wrapDestination(wrapper.createQueue(serverId, name));
  }

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
  public void createTopic(String name) throws AdminException, ConnectException {
    createTopic(wrapper.getLocalServerId(), name); 
  }

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
  public void createTopic(int serverId, String name) throws AdminException, ConnectException {
    wrapDestination(wrapper.createTopic(serverId, name));
  }

  private final void wrapDestination(Destination destination) {
    if (destination == null) return;
    
    destination.setWrapper(wrapper);
    destination.registerMBean(JMXBaseName);
  }
  
  /**
   * This method creates and registers MBeans for all the users on
   * the local server.
   * 
   * @exception ConnectException  If the connection fails.
   * @exception AdminException    Never thrown.
   * 
   * @see #getUsers(int)
   */
  public void getUsers() throws ConnectException, AdminException {
    getUsers(wrapper.getLocalServerId());
  }

  /**
   * This method creates and registers MBeans for all the users of
   * the selected server.
   * <p>
   * The request fails if the target server does not belong to the platform.
   * 
   * @param serverId  Unique identifier of the given server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException    If the request fails.
   */
  public void getUsers(int serverId) throws ConnectException, AdminException {
    wrapUsers(wrapper.getUsers(serverId));
  }
  
  private final void wrapUsers(User[] users) {
    if (users == null) return;
    
    for (int i=0; i<users.length; i++) {
      wrapUser(users[i]);
    }
  }
  
  /**
   * Creates or retrieves a user on the underlying JORAM server.
   *
   * @param name      The login name of the user.
   * @param password  The password of the user.
   * 
   * @exception AdminException    If the creation fails.
   * @exception ConnectException  If the connection fails.
   * 
   * @see #createUser(String, String, int, String)
   */
  public void createUser(String name, String password) throws AdminException, ConnectException {
    wrapUser(wrapper.createUser(name, password));
  }

  /**
   * Creates or retrieves a user on the underlying JORAM server.
   *
   * @param name          The login name of the user.
   * @param password      The password of the user.
   * @param identityClass The identity class used for authentication.
   * 
   * @exception AdminException    If the creation fails.
   * @exception ConnectException  If the connection fails.
   * 
   * @see #createUser(String, String, int, String)
   */
  public void createUser(String name, String password,
                         String identityClass) throws AdminException, ConnectException {
    wrapUser(wrapper.createUser(name, password, identityClass));
  }


  /**
   * Creates or retrieves a user on the given JORAM server.
   *
   * @param name      The login name of the user.
   * @param password  The password of the user.
   * @param serverId  The unique identifier of the Joram server.
   * 
   * @exception AdminException    If the creation fails.
   * @exception ConnectException  If the connection fails.
   * 
   * @see #createUser(String, String, int, String)
   */
  public void createUser(String name, String password,
                         int serverId) throws AdminException, ConnectException {
    wrapUser(wrapper.createUser(name, password, serverId));
  }


  /**
   * Creates or retrieves a user on the underlying JORAM server.
   *
   * @param name          The login name of the user.
   * @param password      The password of the user.
   * @param serverId      The unique identifier of the Joram server.
   * @param identityClass The identity class used for authentication.
   * 
   * @exception AdminException    If the creation fails.
   * @exception ConnectException  If the connection fails.
   */
  public void createUser(String name, String password,
                         int serverId,
                         String identityClass) throws ConnectException, AdminException {
    wrapUser(wrapper.createUser(name, password, serverId, identityClass));
  }

  private final void wrapUser(User user) {
    if (user == null) return;
    
    user.setWrapper(wrapper);
    user.registerMBean(JMXBaseName);
  }

  transient protected String JMXBaseName = null;

  public void registerMBean(String base) {
    JMXBaseName = base;
    try {
      MXWrapper.registerMBean(this, JMXBaseName, "type=JoramAdmin");
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "JoramAdmin.registerMBean", e);
    }
  }

  public void unregisterMBean() {
    if (JMXBaseName == null) {
      return;
    }
    try {
      MXWrapper.unregisterMBean(JMXBaseName, "type=JoramAdmin");
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "JoramAdmin.unregisterMBean",e);
    }
  }

  /**
   * Opens a connection dedicated to administering with the Joram server
   * which parameters are wrapped by a given <code>ConnectionFactory</code>.
   *
   * @param cf       The Joram's ConnectionFactory to use for connecting.
   * @param name          Administrator's name.
   * @param password      Administrator's password.
   * @param identityClass identity class name.
   *
   * @exception ConnectException  If connecting fails.
   * @exception AdminException  If the administrator identification is incorrect.
   */
  public static JoramAdmin doCreate(AbstractConnectionFactory cf,
                             String name,
                             String password,
                             String identityClass) throws ConnectException, AdminException {
    Connection cnx = null;

    //  set identity className
    cf.setIdentityClassName(identityClass);
    try {
      cnx = cf.createConnection(name, password);
      cnx.start();

      return new JoramAdmin(cnx);
    } catch (JMSSecurityException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "JoramAdmin - error during creation", exc);
      throw new AdminException(exc.getMessage());
    } catch (JMSException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "JoramAdmin - error during creation", exc);
      throw new ConnectException("Connecting failed: " + exc);
    }
  }

  /**
   * This method execute the XML script file that the location is given
   * in parameter.
   * <p>
   * Be careful, currently this method use the static administration connection
   * through the AdminModule Class.
   *
   * @param cfgDir        The directory containing the file.
   * @param cfgFileName   The script filename.
   */
  public void executeXMLAdmin(String cfgDir,
                                 String cfgFileName) throws Exception {
    AdminModule.executeXMLAdmin(cfgDir, cfgFileName);
  }

  /**
   * This method execute the XML script file that the pathname is given
   * in parameter.
   * <p>
   * Be careful, currently this method use the static administration connection
   * through the AdminModule Class.
   *
   * @param path    The script pathname.
   */
  public void executeXMLAdmin(String path) throws Exception {
    AdminModule.executeXMLAdmin(path);
  }

  /**
   * Export the repository content to an XML file
   * - only the destinations objects are retrieved in this version
   * - xml script format of the admin objects (joramAdmin.xml)
   * <p>
   * Be careful, currently this method use the static administration connection
   * through the AdminModule Class.
   * 
   * @param exportDir       target directory where the export file will be put
   * @param exportFilename  filename of the export file
   * @throws AdminException if an error occurs
   */
  public void exportRepositoryToFile(String exportDir,
                                     String exportFilename) throws AdminException {
    AdminModule.exportRepositoryToFile(exportDir, exportFilename);
  }
  
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
  public AdminReply processAdmin(String targetId, int command, Properties prop) throws ConnectException, AdminException {
  	return wrapper.processAdmin(targetId, command, prop);
  }
}
