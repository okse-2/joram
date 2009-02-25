/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2008 ScalAgent Distributed Technologies
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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package org.objectweb.joram.client.jms.admin;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.List;

import fr.dyade.aaa.util.management.MXWrapper;
import org.objectweb.joram.shared.JoramTracing;
import org.objectweb.util.monolog.api.BasicLevel;

/**
 *
 */
public class PlatformAdmin
  implements PlatformAdminMBean {

  public PlatformAdmin() 
    throws ConnectException, AdminException {
    connect("root", "root", 60);
    registerMBean();
  }
  
  public PlatformAdmin(String hostName,
                       int port,
                       String name,
                       String password,
                       int cnxTimer,
                       String reliableClass) 
    throws UnknownHostException, ConnectException, AdminException {
    connect(hostName,port,name,password,cnxTimer,reliableClass);
    registerMBean();
  }

  public PlatformAdmin(String hostName,
                       int port,
                       String name,
                       String password,
                       int cnxTimer) 
    throws UnknownHostException, ConnectException, AdminException {
    connect(hostName,port,name,password,cnxTimer,
            "org.objectweb.joram.client.jms.tcp.ReliableTcpClient");
    registerMBean();
  }

  public PlatformAdmin(String name,
                       String password) 
    throws ConnectException, AdminException {
    collocatedConnect(name,password);
    registerMBean();
  }

  public PlatformAdmin(javax.jms.TopicConnectionFactory cnxFact,
                       String name,
                       String password) 
  throws ConnectException, AdminException {
    connect(cnxFact,name,password);
    registerMBean();
  }
  
  public PlatformAdmin(javax.jms.TopicConnectionFactory cnxFact,
                       String name,
                       String password,
                       String identityClassName) 
    throws ConnectException, AdminException {
    connect(cnxFact,name,password,identityClassName);
    registerMBean();
  }

  private void registerMBean() {
    try {
      MXWrapper.registerMBean(this,
                              "joramClient",
                              "type=PlatformAdmin");
    } catch (Exception e) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(BasicLevel.DEBUG,
                                   "registerMBean",e);
    }
  }

  private void unregisterMBean() {
    try {
      MXWrapper.unregisterMBean("joramClient",
                                "type=PlatformAdmin");
    } catch (Exception e) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(BasicLevel.DEBUG,
                                   "unregisterMBean",e);
    }
  }


  /**
   * Opens a connection dedicated to administering with the Joram server
   * which parameters are wrapped by a given
   * <code>TopicConnectionFactory</code>.
   *
   * @param cnxFact  The TopicConnectionFactory to use for connecting.
   * @param name  Administrator's name.
   * @param password  Administrator's password.
   *
   * @exception ConnectException  If connecting fails.
   * @exception AdminException  If the administrator identification is
   *              incorrect.
   */
  public void connect(javax.jms.TopicConnectionFactory cnxFact, 
                      String name,
                      String password)
    throws ConnectException, AdminException {
    AdminModule.connect(cnxFact,name,password);
  }
  
  /**
   * Opens a connection dedicated to administering with the Joram server
   * which parameters are wrapped by a given
   * <code>TopicConnectionFactory</code>.
   *
   * @param cnxFact  The TopicConnectionFactory to use for connecting.
   * @param name  Administrator's name.
   * @param password  Administrator's password.
   * @param indentityClass identity class name.
   *
   * @exception ConnectException  If connecting fails.
   * @exception AdminException  If the administrator identification is
   *              incorrect.
   */
  public void connect(javax.jms.TopicConnectionFactory cnxFact, 
                      String name,
                      String password,
                      String indentityClass)
    throws ConnectException, AdminException {
    AdminModule.connect(cnxFact,name,password,indentityClass);
  }
   
  /**
   * Opens a TCP connection with the Joram server running on a given host and
   * listening to a given port.
   *
   * @param host  The name or IP address of the host the server is running on.
   * @param port  The number of the port the server is listening to.
   * @param name  Administrator's name.
   * @param password  Administrator's password.
   * @param cnxTimer  Timer in seconds during which connecting to the server
   *          is attempted.
   * @param reliableClass  Reliable class name.
   *
   * @exception UnknownHostException  If the host is invalid.
   * @exception ConnectException  If connecting fails.
   * @exception AdminException  If the administrator identification is
   *              incorrect.
   */
  public void connect(String hostName,
                      int port,
                      String name,
                      String password,
                      int cnxTimer,
                      String reliableClass)
    throws UnknownHostException, ConnectException, AdminException {
    AdminModule.connect(hostName,port,name,password,cnxTimer,reliableClass);
  }
  
  /**
   * Opens a TCP connection with the Joram server running on a given host and
   * listening to a given port.
   *
   * @param host  The name or IP address of the host the server is running on.
   * @param port  The number of the port the server is listening to.
   * @param name  Administrator's name.
   * @param password  Administrator's password.
   * @param cnxTimer  Timer in seconds during which connecting to the server
   *          is attempted.
   * @param reliableClass  Reliable class name.
   * @param indentityClass identity class name.
   *
   * @exception UnknownHostException  If the host is invalid.
   * @exception ConnectException  If connecting fails.
   * @exception AdminException  If the administrator identification is
   *              incorrect.
   */
  public void connect(String hostName,
                      int port,
                      String name,
                      String password,
                      int cnxTimer,
                      String reliableClass,
                      String indentityClass)
    throws UnknownHostException, ConnectException, AdminException {
    AdminModule.connect(hostName,port,name,password,cnxTimer,reliableClass,indentityClass);
  }
  
  /**
   * Opens a TCP connection with the Joram server running on the default
   * "locahost" host and listening to the default 16010 port.
   *
   * @param name  Administrator's name.
   * @param password  Administrator's password.
   * @param cnxTimer  Timer in seconds during which connecting to the server
   *          is attempted.
   *
   * @exception UnknownHostException  Never thrown.
   * @exception ConnectException  If connecting fails.
   * @exception AdminException  If the administrator identification is
   *              incorrect.
   */
  public void connect(String name, String password, int cnxTimer)
    throws ConnectException, AdminException {
    try {
      connect("localhost", 
              16010, 
              name, 
              password, 
              cnxTimer, 
              "org.objectweb.joram.client.jms.tcp.ReliableTcpClient");
    } catch (UnknownHostException exc) {
      throw new AdminException(exc.getMessage());
    }
  }

  /**
   * Opens a connection with the collocated JORAM server.
   *
   * @param name  Administrator's name.
   * @param password  Administrator's password.
   *
   * @exception ConnectException  If connecting fails.
   * @exception AdminException  If the administrator identification is
   *              incorrect.
   */
  public void collocatedConnect(String name, String password)
    throws ConnectException, AdminException {
    AdminModule.collocatedConnect(name,password);
  }
  
  /**
   * Opens a connection with the collocated JORAM server.
   *
   * @param name  Administrator's name.
   * @param password  Administrator's password.
   * @param indentityClass identity class name.
   *
   * @exception ConnectException  If connecting fails.
   * @exception AdminException  If the administrator identification is
   *              incorrect.
   */
  public void collocatedConnect(String name, String password, String indentityClass)
    throws ConnectException, AdminException {
    AdminModule.collocatedConnect(name,password,indentityClass);
  }

  /** Closes the administration connection. */
  public void disconnect() {
    AdminModule.disconnect();
  }
  
  /** Closes the administration connection
   * and unregister the MBean.
   */
  public void exit() {
    disconnect();
    unregisterMBean();
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
  public void stopServer(int serverId)
    throws ConnectException, AdminException {
    AdminModule.stopServer(serverId);
  }

  /**
   * Stops the platform local server.
   * 
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void stopServer() 
    throws ConnectException, AdminException {
    AdminModule.stopServer();
  }

  /**
   * Adds a server to the platform.
   *
   * @param serverId Id of the added server
   * @param hostName Address of the host where the added server is started
   * @param domainName Name of the domain where the server is added
   * @param port Listening port of the server in the specified domain
   * @param serverName Name of the added server
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void addServer(int sid,
                        String hostName,
                        String domainName,
                        int port,
                        String serverName)
    throws ConnectException, AdminException {
    AdminModule.addServer(sid,hostName,domainName,port,serverName);
  }

  /**
   * Removes a server from the platform.
   *
   * @param sid Id of the removed server
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void removeServer(int sid)
    throws ConnectException, AdminException {
    AdminModule.removeServer(sid);
  }

  /**
   * Adds a domain to the platform.
   *
   * @param domainName Name of the added domain
   * @param sid Id of the router server that
   *            gives access to the added domain
   * @param port Listening port in the added domain of the
   *             router server
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void addDomain(String domainName,
                        int sid,
                        int port)
    throws ConnectException, AdminException {
    AdminModule.addDomain(domainName,sid,port);
  }

  /**
   * Removes a domain from the platform.
   *
   * @param domainName Name of the added domain
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void removeDomain(String domainName)
    throws ConnectException, AdminException {
    AdminModule.removeDomain(domainName);
  }

  /**
   * Returns the current servers configuration (a3servers.xml).
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public String getConfiguration()
    throws ConnectException, AdminException {
    return AdminModule.getConfiguration();
  }

  /**
   * Returns the list of the platform's servers' identifiers.
   */
  public List getServersIds() {
    try {
      return AdminModule.getServersIds();
    } catch (Exception exc) {
    return null;
    }
  }

  /**
   * Returns the list of the servers' identifiers that belong
   * to the specified domain
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public List getServersIds(String domainName) 
    throws ConnectException, AdminException {
    return AdminModule.getServersIds(domainName);
  }

  /**
   * Returns the list of the domain names that
   * contains the specified server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public String[] getDomainNames(int serverId) 
    throws ConnectException, AdminException {
    return AdminModule.getDomainNames(serverId);
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
  public void setDefaultThreshold(int serverId, int threshold)
    throws ConnectException, AdminException {
    AdminModule.setDefaultThreshold(serverId,threshold);
  }

  /**
   * Sets a given value as the default threshold for the local server (-1 for
   * unsetting previous value).
   *
   * @param threshold  The threshold value to be set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void setDefaultThreshold(int threshold)
    throws ConnectException, AdminException {
    AdminModule.setDefaultThreshold(threshold);
  }

  /**
   * Returns the default threshold value for a given server, -1 if not set.
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public int getDefaultThreshold(int serverId)
    throws ConnectException, AdminException {
    return AdminModule.getDefaultThreshold(serverId);
  }

  /**
   * Returns the default threshold value for the local server, -1 if not set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public int getDefaultThreshold()
    throws ConnectException, AdminException {
    return AdminModule.getDefaultThreshold();
  }

  /**
   * Returns the identifier of the server the module is connected to.
   *
   * @exception ConnectException  If the admin connection is not established.
   */
  public int getLocalServerId() 
    throws ConnectException {
    return AdminModule.getLocalServerId();
  }

  /**
   * Returns the host name of the server the module is connected to.
   *
   * @exception ConnectException  If the admin connection is not established.
   */
  public String getLocalHost() 
    throws ConnectException {
    return AdminModule.getLocalHost();
  }

  /**
   * Returns the port number of the server the module is connected to.
   *
   * @exception ConnectException  If the admin connection is not established.
   */
  public int getLocalPort() 
    throws ConnectException {
    return AdminModule.getLocalPort();
  }
}
