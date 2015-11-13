/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 - 2015 ScalAgent Distributed Technologies
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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.shared.DestinationConstants;
import org.objectweb.joram.shared.admin.AddDomainRequest;
import org.objectweb.joram.shared.admin.AddServerRequest;
import org.objectweb.joram.shared.admin.AdminCommandConstant;
import org.objectweb.joram.shared.admin.AdminCommandReply;
import org.objectweb.joram.shared.admin.AdminReply;
import org.objectweb.joram.shared.admin.AdminRequest;
import org.objectweb.joram.shared.admin.AdminCommandRequest;
import org.objectweb.joram.shared.admin.CreateDestinationReply;
import org.objectweb.joram.shared.admin.CreateDestinationRequest;
import org.objectweb.joram.shared.admin.CreateUserReply;
import org.objectweb.joram.shared.admin.CreateUserRequest;
import org.objectweb.joram.shared.admin.GetConfigRequest;
import org.objectweb.joram.shared.admin.GetDomainNames;
import org.objectweb.joram.shared.admin.GetDomainNamesRep;
import org.objectweb.joram.shared.admin.GetJMXAttsReply;
import org.objectweb.joram.shared.admin.GetJMXAttsRequest;
import org.objectweb.joram.shared.admin.GetLocalServer;
import org.objectweb.joram.shared.admin.GetLocalServerRep;
import org.objectweb.joram.shared.admin.GetDMQSettingsRequest;
import org.objectweb.joram.shared.admin.GetDMQSettingsReply;
import org.objectweb.joram.shared.admin.GetDestinationsRequest;
import org.objectweb.joram.shared.admin.GetDestinationsReply;
import org.objectweb.joram.shared.admin.GetServersIdsRequest;
import org.objectweb.joram.shared.admin.GetServersIdsReply;
import org.objectweb.joram.shared.admin.GetStatsRequest;
import org.objectweb.joram.shared.admin.GetStatsReply;
import org.objectweb.joram.shared.admin.GetUsersReply;
import org.objectweb.joram.shared.admin.GetUsersRequest;
import org.objectweb.joram.shared.admin.RemoveDomainRequest;
import org.objectweb.joram.shared.admin.RemoveServerRequest;
import org.objectweb.joram.shared.admin.SetDMQRequest;
import org.objectweb.joram.shared.admin.SetThresholdRequest;
import org.objectweb.joram.shared.admin.StopServerRequest;
import org.objectweb.joram.shared.security.Identity;
import org.objectweb.joram.shared.security.SimpleIdentity;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * The <code>AdminWrapper</code> class allows to set an administrator
 * connection to a given JORAM server, and provides administration and
 * monitoring methods at a server/platform level.
 */
public class AdminWrapper implements AdminItf {
  /** The description of the server the module is connected to. */
  private Server server = null;
  
  /** The requestor for sending the synchronous requests. */
  private AdminRequestor requestor;
  
  public static final String ADM_NAME_PROPERTY = "JoramAdminXML";
  public final static String DEFAULT_ADM_NAME = "default";
  
  /**
   * Set the maximum time in ms before aborting request.
   * 
   * @param timeOut the maximum time in ms before aborting request.
   * @throws ConnectException if the connection is not established.
   */
  public final void setTimeOutToAbortRequest(long timeOut) throws ConnectException {
    if (requestor == null)
      throw new ConnectException("Connection not established.");
    
    requestor.setRequestTimeout(timeOut);
  }

  /**
   * Returns the maximum time in ms before aborting request.
   * 
   * @return the maximum time in ms before aborting request.
   * @throws ConnectException if the connection is not established.
   */
  public final long getTimeOutToAbortRequest() throws ConnectException {
    if (requestor == null)
      throw new ConnectException("Connection not established.");

    return requestor.getRequestTimeout();
  }

  public static Logger logger = Debug.getLogger(AdminWrapper.class.getName());

  /**
   * Creates an administration wrapper for a Joram server.
   * Be careful, if the connection is not started this method will failed with
   * a ConnectException.
   * 
   * @param cnx A valid connection to the Joram server.
   * @throws JMSException A problem occurs during initialization.
   */
  public AdminWrapper(Connection cnx) throws JMSException, ConnectException, AdminException {
    requestor = new AdminRequestor(cnx);
    // Get basic informations about local server. 
    getLocalServer();
  }

  /**
   * Closes the underlying requestor.
   */
  public void close() {
    if (requestor != null) requestor.close();
    requestor = null;
  }

  /**
   * Returns true if the underlying requestor is closed.
   * 
   * @return true if the underlying requestor is closed.
   */
  public boolean isClosed() {
    return (requestor == null);
  }
  
  /**
   * Stops the platform local server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException    If the request fails.
   * 
   * @see #stopServer(int)
   */
  public final void stopServer() throws ConnectException, AdminException {
    stopServer(getLocalServerId());
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
  public final void stopServer(int serverId) throws ConnectException, AdminException {
    try {
      doRequest(new StopServerRequest(serverId));
    } catch (ConnectException exc) {
      // In many case the reply to a StopServerRequest is not transmitted.
      // Ignore the underlying error.
    } finally {
      if (serverId == getLocalServerId()) close();
    }
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
   * 
   * @see #addServer(int, String, String, int, String, String[], String[])
   */
  public final void addServer(int sid,
                              String host,
                              String domain,
                              int port,
                              String server) throws ConnectException, AdminException {
    addServer(sid, host, domain, port, server, new String[]{}, new String[]{});
  }

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
  public final void addServer(int sid,
                              String host,
                              String domain,
                              int port,
                              String server,
                              String[] services,
                              String[] args) throws ConnectException, AdminException {
    if (services == null) throw new AdminException("Expected service names");
    if (args == null) throw new AdminException("Expected service arguments");
    if (services.length != args.length)
      throw new AdminException("Same number of service names and arguments expected");

    doRequest(new AddServerRequest(sid, host, domain, port, server, services, args));
  }

  /**
   * Removes a server from the platform.
   *
   * @param sid Id of the removed server
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public final void removeServer(int sid) throws ConnectException, AdminException {
    doRequest(new RemoveServerRequest(sid));
  }

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
  public final void addDomain(String domain,
                              int sid,
                              int port) throws ConnectException, AdminException {
    doRequest(new AddDomainRequest(domain, sid, port));
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
  public final void addDomain(String domain,
                              String network,
                              int sid,
                              int port) throws ConnectException, AdminException {
    doRequest(new AddDomainRequest(domain, network, sid, port));
  }

  /**
   * Removes a domain from the platform.
   *
   * @param domain Name of the domain to remove
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public final void removeDomain(String domain) throws ConnectException, AdminException {
    doRequest(new RemoveDomainRequest(domain));
  }

  /**
   * Returns the current servers configuration (a3servers.xml).
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public final String getConfiguration() throws ConnectException, AdminException {
    return doRequest(new GetConfigRequest()).getInfo();
  }

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
  public final Hashtable getStatistics() throws ConnectException, AdminException {
    return getStatistics(getLocalServerId());
  }

  /**
   * Returns statistics for the specified server.
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @param serverId Unique identifier of the server.
   * @return  the statistics for the the specified server.
   * 
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public final Hashtable getStatistics(int serverId) throws ConnectException, AdminException {
    GetStatsRequest request = new GetStatsRequest(DestinationConstants.getNullId(serverId));
    GetStatsReply reply = (GetStatsReply) doRequest(request);
    return  reply.getStats();
  }

  /**
   * Returns JMX attribute value for the local server.
   *
   * @return  Corresponding JMX attribute value for the local server.
   *          
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   * 
   * @see #getStatistics(int)
   */
  public final Hashtable getJMXAttribute(String attname) throws ConnectException, AdminException {
    return getJMXAttribute(getLocalServerId(), attname);
  }

  /**
   * Returns JMX attribute value for the specified server.
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @param serverId Unique identifier of the server.
   * @return  the statistics for the the specified server.
   * 
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public final Hashtable getJMXAttribute(int serverId, String attname) throws ConnectException, AdminException {
    GetJMXAttsRequest request = new GetJMXAttsRequest(DestinationConstants.getNullId(serverId), attname);
    GetJMXAttsReply reply = (GetJMXAttsReply) doRequest(request);
    return  reply.getStats();
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
  public final String getDefaultDMQId() throws ConnectException, AdminException {
    return getDefaultDMQId(getLocalServerId());
  }

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
  public final String getDefaultDMQId(int serverId) throws ConnectException, AdminException {
    GetDMQSettingsRequest request = new GetDMQSettingsRequest(DestinationConstants.getNullId(serverId));
    GetDMQSettingsReply reply = (GetDMQSettingsReply) doRequest(request);

    if (reply.getDMQName() == null) return null;

    return reply.getDMQName();
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
  public final void setDefaultDMQId(String dmqId) throws ConnectException, AdminException {
    setDefaultDMQId(getLocalServerId(), dmqId);
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
  public final void setDefaultDMQId(int serverId, String dmqId) throws ConnectException, AdminException {
    doRequest(new SetDMQRequest(DestinationConstants.getNullId(serverId), dmqId));
  }

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
  public final Queue getDefaultDMQ() throws ConnectException, AdminException {
    return getDefaultDMQ(getLocalServerId());
  }

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
  public final Queue getDefaultDMQ(int serverId) throws ConnectException, AdminException {
    String reply = getDefaultDMQId(serverId);
    if (reply == null) return null;

    return new Queue(reply);
  }

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
  public final void setDefaultDMQ(Queue dmq) throws ConnectException, AdminException {
    setDefaultDMQ(getLocalServerId(), dmq);
  }

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
  public final void setDefaultDMQ(int serverId, Queue dmq) throws ConnectException, AdminException {
    doRequest(new SetDMQRequest(DestinationConstants.getNullId(serverId), (dmq==null)?null:dmq.getName()));
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
  public final int getDefaultThreshold() throws ConnectException, AdminException {
    return getDefaultThreshold(getLocalServerId());
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
  public final int getDefaultThreshold(int serverId) throws ConnectException, AdminException {
    GetDMQSettingsRequest request = new GetDMQSettingsRequest(DestinationConstants.getNullId(serverId));
    GetDMQSettingsReply reply = (GetDMQSettingsReply) doRequest(request);

    return reply.getThreshold();
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
  public final void setDefaultThreshold(int threshold) throws ConnectException, AdminException {
    setDefaultThreshold(getLocalServerId(), threshold);
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
  public final void setDefaultThreshold(int serverId, int threshold) throws ConnectException, AdminException {
    doRequest(new SetThresholdRequest(DestinationConstants.getNullId(serverId), threshold));
  }

  /**
   * Returns the list of the platform's servers' identifiers.
   *
   * @return An array containing the list of server's identifiers.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   * 
   * @see #getServers(String)
   */
  public final int[] getServersIds() throws ConnectException, AdminException {
    return getServersIds(null);
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
  public final int[] getServersIds(String domain) throws ConnectException, AdminException {
    GetServersIdsRequest request = new GetServersIdsRequest(getLocalServerId(), domain);
    GetServersIdsReply reply = (GetServersIdsReply) doRequest(request);

    return reply.getIds();
  }

  /**
   * Returns the list of the platform's servers' names.
   *
   * @return An array containing the list of server's names.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   * 
   * @see #getServers(String)
   */
  public final String[] getServersNames() throws ConnectException, AdminException {
    return getServersNames(null);
  }

  /**
   * Returns the list of the servers' names that belong to the specified domain
   *
   * @param domain  Name of the domain.
   * @return An array containing the list of server's names of the specified domain.
   * 
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public final String[] getServersNames(String domain) throws ConnectException, AdminException {
    GetServersIdsRequest request = new GetServersIdsRequest(getLocalServerId(), domain);
    GetServersIdsReply reply = (GetServersIdsReply) doRequest(request);

    return reply.getNames();
  }
  
  /**
   * Returns the list of the platform's servers' identifiers.
   * 
   * @return An array containing the description of all servers.
   * 
   * @throws ConnectException
   * @throws AdminException
   * 
   * @see #getServers(String)
   */
  public final Server[] getServers() throws ConnectException, AdminException {
    return getServers(null);
  }

  /**
   * Returns the list of the servers' that belong to the specified domain.
   *
   * @param domain  Name of the domain.
   * @return An array containing the description of all servers.
   * 
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public final Server[] getServers(String domain) throws ConnectException, AdminException {
    GetServersIdsRequest request = new GetServersIdsRequest(getLocalServerId(), domain);
    GetServersIdsReply reply = (GetServersIdsReply) doRequest(request);

    int[] serverIds = reply.getIds();
    String[] serverNames = reply.getNames();
    String[] serverHostNames = reply.getHostNames();
    Server[] servers = new Server[serverIds.length];
    for (int i = 0; i < serverIds.length; i++) {
      servers[i] = new Server(serverIds[i], serverNames[i], serverHostNames[i]);
    }
    return servers;
  }
  
  /**
   * Returns the list of the domain names that contains the specified server.
   * 
   * @param serverId Unique identifier of the server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public final String[] getDomainNames(int serverId) throws ConnectException, AdminException {
    GetDomainNames request = new GetDomainNames(serverId);
    GetDomainNamesRep reply = (GetDomainNamesRep) doRequest(request);
    return reply.getDomainNames();
  }

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
  public final Destination[] getDestinations() throws ConnectException, AdminException {
    return getDestinations(getLocalServerId());
  }

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
  public final Destination[] getDestinations(int serverId) throws ConnectException, AdminException {
    Destination[] dest = null;

    GetDestinationsRequest request = new GetDestinationsRequest(serverId);
    GetDestinationsReply reply = (GetDestinationsReply) doRequest(request);

    String[] ids = reply.getIds();
    if ((ids != null) && (ids.length > 0)) {
      String[] names = reply.getNames();
      byte[] types = reply.getTypes();

      dest = new Destination[ids.length];
      for (int i=0; i<ids.length; i++) {
        dest[i] = Destination.newInstance(ids[i], names[i], types[i]);
      }
    }
    return dest;
  }

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
  public Destination createQueue(String name) throws AdminException, ConnectException {
    return createQueue(getLocalServerId(), name, "org.objectweb.joram.mom.dest.Queue", null);
  }

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
  public Destination createQueue(int serverId, String name) throws AdminException, ConnectException {
    return createQueue(serverId, name, "org.objectweb.joram.mom.dest.Queue", null);
  }

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
                                 Properties prop) throws ConnectException, AdminException {
    CreateDestinationRequest cdr = new CreateDestinationRequest(serverId, name, className, prop, Queue.QUEUE_TYPE);
    CreateDestinationReply reply = (CreateDestinationReply) doRequest(cdr);
    
    Queue queue = Queue.createQueue(reply.getId(), name);
    
    if (AdminModule.wrapper != this)
      queue.setWrapper(this);

    return queue;
  }

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
  public Destination createTopic(String name) throws AdminException, ConnectException {
    return createTopic(getLocalServerId(), name);
  }

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
  public Destination createTopic(int serverId, String name) throws AdminException, ConnectException {
    return createTopic(serverId, name, "org.objectweb.joram.mom.dest.Topic", null);
  }

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
                                 Properties prop) throws ConnectException, AdminException {
    CreateDestinationRequest cdr = new CreateDestinationRequest(serverId, name, className, prop, Topic.TOPIC_TYPE);
    CreateDestinationReply reply = (CreateDestinationReply) doRequest(cdr);
    
    Topic topic = Topic.createTopic(reply.getId(), name);

    if (AdminModule.wrapper != this)
      topic.setWrapper(this);

    return topic;
  }

  /**
   * Creates or retrieves a DeadMessageQueue destination on a given JORAM server.
   * <p>
   * First a destination with the specified name is searched on the given
   * server, if it does not exist it is created.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployment fails server side.
   *
   * @param serverId   The identifier of the server where deploying the topic.
   * @param name       The name of the queue.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException    If the request fails.
   * 
   * @deprecated No longer needed, any queue can be used as DMQ.
   */
  public Queue createDeadMQueue(int serverId, String name) throws ConnectException, AdminException {
    CreateDestinationRequest cdr = new CreateDestinationRequest(serverId,
                                                                name, 
                                                                "org.objectweb.joram.mom.dest.Queue",
                                                                null,
                                                                Queue.QUEUE_TYPE);
    CreateDestinationReply reply = (CreateDestinationReply) doRequest(cdr);
    
    Queue dmq = DeadMQueue.createDeadMQueue(reply.getId(), name);
    
    if (AdminModule.wrapper != this)
      dmq.setWrapper(this);

    return dmq;
  }

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
  public final User[] getUsers() throws ConnectException, AdminException {
    return getUsers(getLocalServerId());
  }

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
  public final User[] getUsers(int serverId) throws ConnectException, AdminException {
    User[] list = null;

    // TODO (AF): Changes the GetUsersReply class to return 2 arrays.
    // TODO (AF): Same work with GetRightsReply !
    GetUsersRequest request = new GetUsersRequest(serverId);
    GetUsersReply reply = (GetUsersReply) doRequest(request);

    Hashtable users = reply.getUsers();
    list = new User[users.size()];
    String name;
    int i = 0;
    for (Enumeration names = users.keys(); names.hasMoreElements();) {
      name = (String) names.nextElement();
      list[i++] = new User(name, (String) users.get(name));
    }
    return list;
  }

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
  public User createUser(String name, String password) throws ConnectException, AdminException {
    return createUser(name, password, getLocalServerId(), SimpleIdentity.class.getName());
  }

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
  public User  createUser(String name, String password, int serverId) throws ConnectException, AdminException {
    return createUser(name, password, serverId, SimpleIdentity.class.getName());
  }

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
                         String identityClass) throws AdminException, ConnectException {
    return createUser(name, password, getLocalServerId(), identityClass);
  }
  
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
                         String identityClassName) throws ConnectException, AdminException {
  	return createUser(name, password, serverId, identityClassName, null);
  }
  
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
                         Properties prop) throws ConnectException, AdminException {
    if ((name == null) || name.equals(""))
      throw new AdminException("User name can not be null or empty");
    
    Identity identity = createIdentity(name, password, identityClassName);
    AdminReply reply = doRequest(new CreateUserRequest(identity, serverId, prop));
    User user = new User(name, ((CreateUserReply) reply).getProxId());
    
    if (AdminModule.wrapper != this)
      user.setWrapper(this);

    return user;
  }
  
  /**
   * Create a user Identity.
   * 
   * @param user              Name of the user.
   * @param passwd            Password of the user.
   * @param identityClassName identity class name (simple, jaas).
   * @return identity user Identity.
   * @throws AdminException
   */
  private Identity createIdentity(String user, String passwd, String identityClassName) throws AdminException {
    Identity identity = null;
    try {
      identity = (Identity) Class.forName(identityClassName).newInstance();
      if (passwd != null)
        identity.setIdentity(user, passwd);
      else
        identity.setUserName(user);
    } catch (Exception e) {
      throw new AdminException(e.getMessage());
    }
    return identity;
  }

  /**
   * Returns the information about the current server: unique identifier, symbolic name and hostname.
   *
   * @return The description of the server.
   * 
   * @exception ConnectException  If the connection fails.
   * @exception AdminException    If the request fails.
   */
  public final Server getLocalServer() throws ConnectException, AdminException {
    if (server == null) {
      GetLocalServerRep reply = (GetLocalServerRep) doRequest(new GetLocalServer());
      server = new Server(reply.getId(), reply.getName(), reply.getHostName());
    }
    return server;
  }

  /**
   * Returns the identifier of the server the module is connected to.
   *
   * @exception ConnectException  If the admin connection is not established.
   * @exception AdminException    If the request fails.
   * 
   * @see #getLocalServer()
   */
  public final int getLocalServerId() throws ConnectException, AdminException {
    if (requestor == null)
      throw new ConnectException("Administrator not connected.");

    if (server == null) getLocalServer();
    return server.getId();
  }

  /**
   * Returns the host name of the server the module is connected to.
   *
   * @exception ConnectException  If the admin connection is not established.
   * @exception AdminException    If the request fails.
   * 
   * @see #getLocalServer()
   */
  public final String getLocalHost() throws ConnectException, AdminException {
    if (requestor == null)
      throw new ConnectException("Administrator not connected.");

    if (server == null) getLocalServer();
    return server.getHostName();
  }

  /**
   * Returns the name of the server the module is connected to.
   *
   * @exception ConnectException  If the admin connection is not established.
   * @exception AdminException    If the request fails.
   * 
   * @see #getLocalServer()
   */
  public final String getLocalName() throws ConnectException, AdminException {
    if (requestor == null)
      throw new ConnectException("Administrator not connected.");

    if (server == null) getLocalServer();
    return server.getName();
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
  	AdminCommandRequest request = new AdminCommandRequest(targetId, command, prop);
  	AdminReply reply = (AdminReply) doRequest(request);
    return reply;
  }
  
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
  public AdminReply doRequest(AdminRequest request) throws AdminException, ConnectException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Admin.doRequest(" + request + ')');

    if (requestor == null)
      throw new ConnectException("Admin connection not established.");

    return requestor.request(request);
  }

  public void abortRequest() throws ConnectException {
    if (requestor == null)
      throw new ConnectException("Admin connection not established.");

    requestor.abort();
  }
  
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
   * @return the result of the invoked method after applying the toString method
   * @throws ConnectException If the connection fails.
   * @throws AdminException If the invocation can't be done or fails
   */
  public String invokeStaticServerMethod(int serverId, String className, String methodName,
      Class<?>[] parameterTypes, Object[] args) throws ConnectException, AdminException {

    if (parameterTypes == null && (args != null && args.length > 0)) {
      throw new AdminException("Parameter types array is null while args array is not null or empty.");
    }
    if (args == null && (parameterTypes != null && parameterTypes.length > 0)) {
      throw new AdminException("Args array is null while parameter types array is not null or empty.");
    }
    if (parameterTypes != null && args != null && parameterTypes.length != args.length) {
      throw new AdminException("Parameter types array size do not match args array size.");
    }
    Properties props = new Properties();
    props.setProperty(AdminCommandConstant.INVOKE_CLASS_NAME, className);
    props.setProperty(AdminCommandConstant.INVOKE_METHOD_NAME, methodName);
    if (parameterTypes != null) {
      for (int i = 0; i < parameterTypes.length; i++) {
        props.setProperty(AdminCommandConstant.INVOKE_METHOD_ARG + i, parameterTypes[i].getName());
        if (args[i] != null) {
          props.setProperty(AdminCommandConstant.INVOKE_METHOD_ARG_VALUE + i, args[i].toString());
        }
      }
    }
    AdminCommandReply reply = null;
    reply = (AdminCommandReply) processAdmin(DestinationConstants.getNullId(serverId),
          AdminCommandConstant.CMD_INVOKE_STATIC, props);
    if (reply.getProp() == null) {
      return null;
    }
    return reply.getProp().getProperty(AdminCommandConstant.INVOKE_METHOD_RESULT);
  }
  
  /**
   * Adds an AMQP server and starts a live connection with it, accessible via
   * the url provided. A server is uniquely identified by the given
   * name. Adding an existing server won't do anything.
   * 
   * @param serverId the serverId
   * @param urls the amqp url list identifying the servers separate by space, for example:
   * "amqp://user:pass@localhost:5672/?name=serv1 amqp://user:pass@localhost:5678/?name=serv2"
   * 
   * @return the result of the method
   * @throws ConnectException If the connection fails.
   * @throws AdminException If the invocation can't be done or fails
   */
  public String addAMQPBridgeConnection(int serverId, String urls) throws ConnectException, AdminException {
  	return invokeStaticServerMethod(
  			serverId,
  			"org.objectweb.joram.mom.dest.amqp.AmqpConnectionService",
  			"addServer",
  			new Class[] { String.class },
  			new Object[] { urls });
  }
  
  /**
   * Removes the live connection to the specified AMQP server.
   * 
   * @param serverId the serverId
   * @param names the name identifying the server or list of name separate by space
   * @return the result of the method
   * @throws ConnectException If the connection fails.
   * @throws AdminException If the invocation can't be done or fails
   */
  public String deleteAMQPBridgeConnection(int serverId, String names) throws ConnectException, AdminException {
  	return invokeStaticServerMethod(
  			serverId,
  			"org.objectweb.joram.mom.dest.amqp.AmqpConnectionService",
  			"deleteServer",
  			new Class[] { String.class },
  			new Object[] { names });
  }
  
  /**
   * Adds a JMS server and starts a live connection with it, accessible via
   * the url provided. A server is uniquely identified by the given
   * name. Adding an existing server won't do anything.
   * 
   * @param serverId the serverId
   * @param urls the jms url list identifying the servers separate by space, for example:
   * "jndi_url/?name=cnx1&cf=cfName&jndiFactoryClass=com.xxx.yyy&user=user1&pass=pass1&clientID=clientID"
   * 
   * @return the result of the method
   * @throws ConnectException If the connection fails.
   * @throws AdminException If the invocation can't be done or fails
   */
  public String addJMSBridgeConnection(int serverId, String urls) throws ConnectException, AdminException {
  	return invokeStaticServerMethod(
  			serverId,
  			"org.objectweb.joram.mom.dest.jms.JMSConnectionService",
  			"addServer",
  			new Class[] { String.class },
  			new Object[] { urls });
  }
  
  /**
   * Removes the live connection to the specified JMS server.
   * 
   * @param serverId the serverId
   * @param names the name identifying the server or list of name separate by space
   * @return the result of the method
   * @throws ConnectException If the connection fails.
   * @throws AdminException If the invocation can't be done or fails
   */
  public String deleteJMSPBridgeConnection(int serverId, String names) throws ConnectException, AdminException {
  	return invokeStaticServerMethod(
  			serverId,
  			"org.objectweb.joram.mom.dest.jms.JMSConnectionService",
  			"deleteServer",
  			new Class[] { String.class },
  			new Object[] { names });
  }
}
