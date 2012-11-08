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
 * Contributor(s): 
 */
package org.objectweb.joram.client.jms.admin;


import java.net.ConnectException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.Connection;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.shared.admin.AddDomainRequest;
import org.objectweb.joram.shared.admin.AddServerRequest;
import org.objectweb.joram.shared.admin.AdminReply;
import org.objectweb.joram.shared.admin.AdminRequest;
import org.objectweb.joram.shared.admin.CreateDestinationReply;
import org.objectweb.joram.shared.admin.CreateDestinationRequest;
import org.objectweb.joram.shared.admin.CreateUserReply;
import org.objectweb.joram.shared.admin.CreateUserRequest;
import org.objectweb.joram.shared.admin.GetConfigRequest;
import org.objectweb.joram.shared.admin.GetDomainNames;
import org.objectweb.joram.shared.admin.GetDomainNamesRep;
import org.objectweb.joram.shared.admin.GetLocalServer;
import org.objectweb.joram.shared.admin.GetLocalServerRep;
import org.objectweb.joram.shared.admin.Monitor_GetDMQSettings;
import org.objectweb.joram.shared.admin.Monitor_GetDMQSettingsRep;
import org.objectweb.joram.shared.admin.Monitor_GetDestinations;
import org.objectweb.joram.shared.admin.Monitor_GetDestinationsRep;
import org.objectweb.joram.shared.admin.Monitor_GetServersIds;
import org.objectweb.joram.shared.admin.Monitor_GetServersIdsRep;
import org.objectweb.joram.shared.admin.Monitor_GetUsers;
import org.objectweb.joram.shared.admin.Monitor_GetUsersRep;
import org.objectweb.joram.shared.admin.RemoveDomainRequest;
import org.objectweb.joram.shared.admin.RemoveServerRequest;
import org.objectweb.joram.shared.admin.SetDefaultDMQ;
import org.objectweb.joram.shared.admin.SetDefaultThreshold;
import org.objectweb.joram.shared.admin.StopServerRequest;
import org.objectweb.joram.shared.security.Identity;
import org.objectweb.joram.shared.security.SimpleIdentity;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Debug;

/**
 * The <code>Admin</code> class allows to set an administrator
 * connection to a given JORAM server, and provides administration and
 * monitoring methods at a server/platform level.
 */
public class AdminWrapper {
  /** The description of the server the module is connected to. */
  private Server server = null;

  /** The requestor for sending the synchronous requests. */
  private AdminRequestor requestor;
  
  public static final String ADM_NAME_PROPERTY = "JoramAdminXML";
  public final static String DEFAULT_ADM_NAME = "default";
  
  public static final String REQUEST_TIMEOUT_PROP = "org.objectweb.joram.client.jms.admin.requestTimeout";

  public final static long DEFAULT_REQUEST_TIMEOUT = 60000;

  private long requestTimeout = DEFAULT_REQUEST_TIMEOUT;
  
  /**
   * Set the maximum time in ms before aborting request.
   * 
   * @param timeOut the maximum time in ms before aborting request.
   * @throws ConnectException 
   */
  public final void setTimeOutToAbortRequest(long timeOut) throws ConnectException {
    if (requestor == null)
      throw new ConnectException("Admin connection not established.");
    
    requestor.setRequestTimeout(timeOut);
  }

  /**
   * Returns the maximum time in ms before aborting request.
   * 
   * @return the maximum time in ms before aborting request.
   * @throws ConnectException 
   */
  public final long getTimeOutToAbortRequest() throws ConnectException {
    if (requestor == null)
      throw new ConnectException("Admin connection not established.");

    return requestor.getRequestTimeout();
  }

  public static Logger logger = Debug.getLogger(AdminWrapper.class.getName());

  /**
   * Creates an administration wrapper for a Joram server.
   * 
   * @param cnx A valid connection to the Joram server.
   * @throws JMSException A problem occurs during initialization.
   */
  public AdminWrapper(Connection cnx) throws JMSException {
    requestTimeout = Long.getLong(REQUEST_TIMEOUT_PROP, requestTimeout).longValue();
    requestor = new AdminRequestor(cnx);
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
      if (serverId == getLocalServerId()) close();
    } catch (ConnectException exc) {
      close();
      // ConnectException is intercepted if stopped server is local server.
      if (serverId != getLocalServerId()) throw exc;
    }
  }

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
   * The domain will use the default network component "Simplenetwork".
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
   * Returns the unique identifier of the default dead message queue for the local
   * server, null if not set.
   *
   * @return  The unique identifier of the default dead message queue for the local
   *          server, null if not set.
   *          
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   * 
   * @see #getDefaultDMQId()
   */
  public final String getDefaultDMQId() throws ConnectException, AdminException {
    return getDefaultDMQId(getLocalServerId());
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
  public final String getDefaultDMQId(int serverId) throws ConnectException, AdminException {
    Monitor_GetDMQSettings request = new Monitor_GetDMQSettings(serverId);
    Monitor_GetDMQSettingsRep reply;
    reply = (Monitor_GetDMQSettingsRep) doRequest(request);

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
    doRequest(new SetDefaultDMQ(serverId, dmqId));
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
  public final DeadMQueue getDefaultDMQ() throws ConnectException, AdminException {
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
  public final DeadMQueue getDefaultDMQ(int serverId) throws ConnectException, AdminException {
    String reply = getDefaultDMQId(serverId);
    if (reply == null) return null;

    return new DeadMQueue(reply);
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
   * @see #setDefaultDMQ(int, DeadMQueue)
   */
  public final void setDefaultDMQ(DeadMQueue dmq) throws ConnectException, AdminException {
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
  public final void setDefaultDMQ(int serverId, DeadMQueue dmq) throws ConnectException, AdminException {
    doRequest(new SetDefaultDMQ(serverId, (dmq==null)?null:dmq.getName()));
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
    Monitor_GetDMQSettings request = new Monitor_GetDMQSettings(serverId);
    Monitor_GetDMQSettingsRep reply;
    reply = (Monitor_GetDMQSettingsRep) doRequest(request);

    // TODO (AF): Changes threshold type to int.
    if (reply.getThreshold() == null) return -1;

    return reply.getThreshold().intValue();
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
    doRequest(new SetDefaultThreshold(serverId, threshold));
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
    Monitor_GetServersIds request = new Monitor_GetServersIds(getLocalServerId(), domain);
    Monitor_GetServersIdsRep reply = (Monitor_GetServersIdsRep) doRequest(request);

    return reply.getIds();
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
    Monitor_GetServersIds request = new Monitor_GetServersIds(getLocalServerId(), domain);
    Monitor_GetServersIdsRep reply = (Monitor_GetServersIdsRep) doRequest(request);

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

    Monitor_GetDestinations request = new Monitor_GetDestinations(serverId);
    Monitor_GetDestinationsRep reply = (Monitor_GetDestinationsRep) doRequest(request);

    String[] ids = reply.getIds();
    if ((ids != null) && (ids.length > 0)) {
      String[] names = reply.getNames();
      String[] types = reply.getTypes();

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
   * @param serverId   The identifier of the server where deploying the topic.
   * @param name       The name of the queue.
   * @param className  The topic class name.
   * @param prop       The topic properties.
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
    
    Queue queue = Queue.createQueue(reply.getId(), name, reply.getType());
    
    if (AdminModule.wrapper != this)
      queue.setWrapper(this);

    queue.registerMBean("joramClient");

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
    
    Topic topic = Topic.createTopic(reply.getId(), name, reply.getType());

    if (AdminModule.wrapper != this)
      topic.setWrapper(this);

    topic.registerMBean("joramClient");

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
   * @param className  The topic class name.
   * @param prop       The topic properties.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException    If the request fails.
   */
  public DeadMQueue createDeadMQueue(int serverId, String name) throws ConnectException, AdminException {
    CreateDestinationRequest cdr = new CreateDestinationRequest(serverId,
                                                                name, 
                                                                "org.objectweb.joram.mom.dest.DeadMQueue",
                                                                null,
                                                                DeadMQueue.QUEUE_TYPE);
    CreateDestinationReply reply = (CreateDestinationReply) doRequest(cdr);
    
    DeadMQueue dmq = DeadMQueue.createDeadMQueue(reply.getId(), name);
    
    if (AdminModule.wrapper != this)
      dmq.setWrapper(this);

    dmq.registerMBean("joramClient");

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

    // TODO (AF): Changes the Monitor_GetUsersRep class to return 2 arrays.
    Monitor_GetUsers request = new Monitor_GetUsers(serverId);
    Monitor_GetUsersRep reply = (Monitor_GetUsersRep) doRequest(request);

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
   * @param name                Name of the user.
   * @param password            Password of the user.
   * @param identityClassName   By default user/password for SimpleIdentity.
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
    Identity identity = createIdentity(name, password, identityClassName);
    AdminReply reply = doRequest(new CreateUserRequest(identity, serverId));
    User user = new User(name, ((CreateUserReply) reply).getProxId());
    
    if (AdminModule.wrapper != this)
      user.setWrapper(this);

    user.registerMBean("joramClient");

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
      if (passwd != null) identity.setIdentity(user, passwd);
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
      GetLocalServerRep reply =  (GetLocalServerRep)doRequest(new GetLocalServer());
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
   * Returns the port number of the server the module is connected to.
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
}
