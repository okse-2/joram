/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2008 ScalAgent Distributed Technologies
 * Copyright (C) 2004 Bull SA
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
 * Initial developer(s): Frederic Maistre (Bull SA)
 * Contributor(s): ScalAgent Distributed Technologies
 *                 Benoit Pelletier (Bull SA)
 */
package org.objectweb.joram.client.jms.admin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryTopic;
import javax.jms.TopicConnection;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.ha.local.TopicHALocalConnectionFactory;
import org.objectweb.joram.client.jms.ha.tcp.TopicHATcpConnectionFactory;
import org.objectweb.joram.client.jms.local.TopicLocalConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TopicTcpConnectionFactory;
import org.objectweb.joram.shared.JoramTracing;
import org.objectweb.joram.shared.admin.AddDomainRequest;
import org.objectweb.joram.shared.admin.AddServerRequest;
import org.objectweb.joram.shared.admin.AdminReply;
import org.objectweb.joram.shared.admin.AdminRequest;
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
import org.objectweb.util.monolog.api.BasicLevel;

/**
 * The <code>AdminModule</code> class allows to set an administrator
 * connection to a given JORAM server, and provides administration and
 * monitoring methods at a server/platform level.
 */
public class AdminModule {
  public static final String ADM_NAME_PROPERTY = "JoramAdminXML";
  public static final String DEFAULT_ADM_NAME = "default";

  public static final String REQUEST_TIMEOUT_PROP =
      "org.objectweb.joram.client.jms.admin.requestTimeout";

  public static final long DEFAULT_REQUEST_TIMEOUT = 120000;

  /** The identifier of the server the module is connected to. */
  private static int localServer;
  /** The host name or IP address this client is connected to. */
  protected static String localHost;
  /** The port number of the client connection. */
  protected static int localPort;

  /** The connection used to link the administrator and the platform. */
  private static TopicConnection cnx = null;

  /** The requestor for sending the synchronous requests. */
  private static AdminRequestor requestor;

  /** AdminMessage sent to the platform. */
  private static AdminMessage requestMsg;
  /** AdminMessage received from the platform. */
  private static AdminMessage replyMsg;

  /** Reply object received from the platform. */
  protected static AdminReply reply;

  private static long requestTimeout =
      Long.getLong(REQUEST_TIMEOUT_PROP,
                   DEFAULT_REQUEST_TIMEOUT).longValue();

  /** <code>true</code> if the underlying a JORAM HA server is defined */
  private static boolean isHa = false;

  /**
   * This method execute the XML script file that the path is given in
   * parameter.
   *
   * @since 4.3.12
   */
  public static void main(String[] args) {
    try {
      AdminModule.executeXMLAdmin(args[0]);
    } catch (Exception exc) {
      exc.printStackTrace();
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
  public static void connect(javax.jms.TopicConnectionFactory cnxFact,
                             String name,
                             String password)
    throws ConnectException, AdminException {
    connect(cnxFact, name, password, Identity.SIMPLE_IDENTITY_CLASS);
    }
  
  /**
   * Opens a connection dedicated to administering with the Joram server
   * which parameters are wrapped by a given
   * <code>TopicConnectionFactory</code>.
   *
   * @param cnxFact  The TopicConnectionFactory to use for connecting.
   * @param name  Administrator's name.
   * @param password  Administrator's password.
   * @param identityClass identity class name.
   *
   * @exception ConnectException  If connecting fails.
   * @exception AdminException  If the administrator identification is
   *              incorrect.
   */
  public static void connect(javax.jms.TopicConnectionFactory cnxFact,
                             String name,
                             String password,
                             String identityClass)
    throws ConnectException, AdminException {
    if (cnx != null)
      return;
    
    //  set identity className
    ((AbstractConnectionFactory) cnxFact).setIdentityClassName(identityClass);

    
    try {
      cnx = cnxFact.createTopicConnection(name, password);
      requestor = new AdminRequestor(cnx);

      cnx.start();

      org.objectweb.joram.client.jms.FactoryParameters params = null;

      if (cnxFact instanceof javax.jms.XATopicConnectionFactory)
        params = ((org.objectweb.joram.client.jms.XAConnectionFactory)
                  cnxFact).getParameters();
      else
        params = ((org.objectweb.joram.client.jms.ConnectionFactory)
                  cnxFact).getParameters();

      localHost = params.getHost();
      localPort = params.getPort();

      // Getting the id of the local server:
      localServer = requestor.getLocalServerId();
    } catch (JMSSecurityException exc) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(
          BasicLevel.DEBUG, "", exc);
      throw new AdminException(exc.getMessage());
    } catch (JMSException exc) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(
          BasicLevel.DEBUG, "", exc);
      throw new ConnectException("Connecting failed: " + exc);
    }
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
   *
   * @exception UnknownHostException  If the host is invalid.
   * @exception ConnectException  If connecting fails.
   * @exception AdminException  If the administrator identification is
   *              incorrect.
   */
  public static void connect(String hostName,
                             int port,
                             String name,
                             String password,
                             int cnxTimer)
    throws UnknownHostException, ConnectException, AdminException {
    connect(hostName,port,name,password,cnxTimer,
            "org.objectweb.joram.client.jms.tcp.ReliableTcpClient");
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
  public static void connect(String hostName,
                             int port,
                             String name,
                             String password,
                             int cnxTimer,
                             String reliableClass)
    throws UnknownHostException, ConnectException, AdminException {
    connect(hostName, port, hostName, password, cnxTimer, reliableClass, Identity.SIMPLE_IDENTITY_CLASS);
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
   * @param identityClass identity class name.
   *
   * @exception UnknownHostException  If the host is invalid.
   * @exception ConnectException  If connecting fails.
   * @exception AdminException  If the administrator identification is
   *              incorrect.
   */
  public static void connect(String hostName,
                             int port,
                             String name,
                             String password,
                             int cnxTimer,
                             String reliableClass,
                             String identityClass)
    throws UnknownHostException, ConnectException, AdminException {
    javax.jms.TopicConnectionFactory cnxFact =null;

    if (isHa) {
      String urlHa = "hajoram://" + hostName + ":" + port;
      cnxFact = TopicHATcpConnectionFactory.create(urlHa);

    } else {
      cnxFact = TopicTcpConnectionFactory.create(hostName, port, reliableClass);
    }

    ((org.objectweb.joram.client.jms.ConnectionFactory)
     cnxFact).getParameters().connectingTimer = cnxTimer;

    connect(cnxFact, name, password, identityClass);
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
  public static void connect(String name, String password, int cnxTimer)
    throws UnknownHostException, ConnectException, AdminException {
      connect("localhost", 16010, name, password, cnxTimer);
    }

  /**
   * Opens a TCP connection with the Joram server running on the default
   * "locahost" host and listening to the default 16010 port.
   *
   * @param name  Administrator's name.
   * @param password  Administrator's password.
   * @param cnxTimer  Timer in seconds during which connecting to the server
   *          is attempted.
   * @param reliableClass  Reliable class name.
   *
   * @exception UnknownHostException  Never thrown.
   * @exception ConnectException  If connecting fails.
   * @exception AdminException  If the administrator identification is
   *              incorrect.
   */
  public static void connect(String name,
                             String password,
                             int cnxTimer,
                             String reliableClass)
    throws UnknownHostException, ConnectException, AdminException {
    connect("localhost", 16010, name, password, cnxTimer, reliableClass);
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
  public static void collocatedConnect(String name, String password)
         throws ConnectException, AdminException {
    collocatedConnect(name, password, Identity.SIMPLE_IDENTITY_CLASS);
  }

  /**
   * Opens a connection with the collocated JORAM server.
   *
   * @param name  Administrator's name.
   * @param password  Administrator's password.
   * @param identityClass identity class name.
   *
   * @exception ConnectException  If connecting fails.
   * @exception AdminException  If the administrator identification is
   *              incorrect.
   */
  public static void collocatedConnect(String name, String password, String identityClass)
         throws ConnectException, AdminException {
    JoramTracing.dbgClient.log(BasicLevel.DEBUG, "isHa=" + isHa);
    if (isHa) {
      connect(TopicHALocalConnectionFactory.create(), name, password, identityClass);
    } else {
      connect(TopicLocalConnectionFactory.create(), name, password, identityClass);
    }
  }

  /** Closes the administration connection. */
  public static void disconnect() {
    try {
      if (cnx == null) return;
      
      cnx.close();
    } catch (JMSException exc) {}

    cnx = null;
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
  public static void stopServer(int serverId)
    throws ConnectException, AdminException {
    try {
      doRequest(new StopServerRequest(serverId));

      if (serverId == localServer)
        cnx = null;
    } catch (ConnectException exc) {
      // ConnectException is intercepted if stopped server is local server.
      if (serverId != localServer)  throw exc;

      cnx = null;
    }
  }

  /**
   * Stops the platform local server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public static void stopServer() throws ConnectException, AdminException {
    stopServer(localServer);
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
  public static void addServer(int sid,
                               String hostName,
                               String domainName,
                               int port,
                               String serverName)
    throws ConnectException, AdminException {
    addServer(sid,
              hostName, domainName, port, serverName,
              new String[]{}, new String[]{});
  }

  /**
   * Adds a server to the platform.
   *
   * @param serverId Id of the added server
   * @param hostName Address of the host where the added server is started
   * @param domainName Name of the domain where the server is added
   * @param port Listening port of the server in the specified domain
   * @param serverName Name of the added server
   * @param serviceNames Names of the service to start within the server
   * @param args Services' arguments
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public static void addServer(int sid,
                               String hostName,
                               String domainName,
                               int port,
                               String serverName,
                               String[] serviceNames,
                               String[] serviceArgs)
    throws ConnectException, AdminException {
    if (serviceNames != null &&
        serviceArgs != null) {
      if (serviceNames.length != serviceArgs.length)
        throw new AdminException(
          "Same number of service names and arguments expected");
    } else {
      if (serviceNames == null) throw new AdminException(
        "Expected service names");
      if (serviceArgs == null) throw new AdminException(
        "Expected service arguments");
    }
    doRequest(new AddServerRequest(
                sid,
                hostName,
                domainName,
                port,
                serverName,
                serviceNames,
                serviceArgs));
  }

  /**
   * Removes a server from the platform.
   *
   * @param sid Id of the removed server
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public static void removeServer(int sid)
    throws ConnectException, AdminException {
    doRequest(new RemoveServerRequest(sid));
  }

  /**
   * Adds a domain to the platform.
   *
   * @param domainName Name of the added domain.
   * @param sid Id of the router server that gives access to the added domain.
   * @param port Listening port in the added domain of the router server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public static void addDomain(String domainName,
                               int sid,
                               int port)
    throws ConnectException, AdminException {
    doRequest(new AddDomainRequest(
                domainName,
                sid,
                port));
  }

  /**
   * Adds a domain to the platform using a specific network component.
   *
   * @param domainName Name of the added domain.
   * @param network    Classname of the network component to use.
   * @param sid Id of the router server that gives access to the added domain.
   * @param port Listening port in the added domain of the router server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public static void addDomain(String domainName,
                               String network,
                               int sid,
                               int port)
    throws ConnectException, AdminException {
    doRequest(new AddDomainRequest(
                domainName,
                network,
                sid,
                port));
  }

  /**
   * Removes a domain from the platform.
   *
   * @param domainName Name of the added domain
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public static void removeDomain(String domainName)
    throws ConnectException, AdminException {
    doRequest(new RemoveDomainRequest(
                domainName));
  }

  /**
   * Returns the current servers configuration (a3servers.xml).
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public static String getConfiguration()
    throws ConnectException, AdminException {
    return doRequest(new GetConfigRequest()).getInfo();
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
  public static void setDefaultDMQ(int serverId, DeadMQueue dmq)
  throws ConnectException, AdminException {
    if (dmq != null) {
      doRequest(new SetDefaultDMQ(serverId, dmq.getName()));
    }
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
  public static void setDefaultDMQId(int serverId, String dmqId)
    throws ConnectException, AdminException {
      doRequest(new SetDefaultDMQ(serverId, dmqId));
    }

  /**
   * Sets a given dead message queue as the default DMQ for the local server
   * (<code>null</code> for unsetting previous DMQ).
   *
   * @param dmq  The dmq to be set as the default one.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public static void setDefaultDMQ(DeadMQueue dmq)
    throws ConnectException, AdminException {
      setDefaultDMQ(localServer, dmq);
    }

  /**
   * Sets a given dead message queue as the default DMQ for the local server
   * (<code>null</code> for unsetting previous DMQ).
   *
   * @param dmqId  The dmqId (AgentId) to be set as the default one.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public static void setDefaultDMQId(String dmqId)
    throws ConnectException, AdminException {
      setDefaultDMQId(localServer, dmqId);
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
  public static void setDefaultThreshold(int serverId, int threshold)
    throws ConnectException, AdminException
    {
      doRequest(new SetDefaultThreshold(serverId, threshold));
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
  public static void setDefaultThreshold(int threshold)
    throws ConnectException, AdminException
    {
      setDefaultThreshold(localServer, threshold);
    }

  /**
   * Returns the list of the platform's servers' identifiers.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public static List getServersIds() throws ConnectException, AdminException
    {
      return getServersIds(null);
    }

  /**
   * Returns the list of the servers' identifiers that belong
   * to the specified domain
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public static List getServersIds(String domainName)
    throws ConnectException, AdminException {
    Monitor_GetServersIds request =
      new Monitor_GetServersIds(
        AdminModule.getLocalServerId(),
        domainName);
    Monitor_GetServersIdsRep reply =
      (Monitor_GetServersIdsRep) doRequest(request);
    int[] serverIds = reply.getIds();
    Vector res = new Vector();
    for (int i = 0; i < serverIds.length; i++) {
      res.addElement(new Integer(serverIds[i]));
    }
    return res;
  }

  public static Server[] getServers()
    throws ConnectException, AdminException {
    return getServers(null);
  }

  public static Server[] getServers(String domainName)
    throws ConnectException, AdminException {
    Monitor_GetServersIds request =
      new Monitor_GetServersIds(
        AdminModule.getLocalServerId(),
        domainName);
    Monitor_GetServersIdsRep reply =
      (Monitor_GetServersIdsRep) doRequest(request);
    int[] serverIds = reply.getIds();
    String[] serverNames = reply.getNames();
    String[] serverHostNames = reply.getHostNames();
    Server[] servers = new Server[serverIds.length];
    for (int i = 0; i < serverIds.length; i++) {
      servers[i] = new Server(serverIds[i],
                              serverNames[i],
                              serverHostNames[i]);
    }
    return servers;
  }

  public static Server getLocalServer()
    throws ConnectException, AdminException {
    GetLocalServerRep reply =  (GetLocalServerRep)doRequest(
      new GetLocalServer());
    return new Server(reply.getId(),
                      reply.getName(),
                      reply.getHostName());
  }

  /**
   * Returns the list of the domain names that
   * contains the specified server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public static String[] getDomainNames(int serverId)
    throws ConnectException, AdminException {
    GetDomainNames request =
      new GetDomainNames(serverId);
    GetDomainNamesRep reply =
      (GetDomainNamesRep) doRequest(request);
    return reply.getDomainNames();
  }

  /**
   * Returns the default dead message queue for a given server, null if not
   * set.
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public static DeadMQueue getDefaultDMQ(int serverId)
    throws ConnectException, AdminException {
    String reply = getDefaultDMQId(serverId);
    if (reply == null)
        return null;
      else
        return new DeadMQueue(reply);
    }

  /**
   * Returns the default dead message queue for the local server, null if not
   * set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public static DeadMQueue getDefaultDMQ()
    throws ConnectException, AdminException {
      return getDefaultDMQ(localServer);
    }
  
  /**
   * Returns the default dead message queue for the local server, null if not
   * set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public static String getDefaultDMQId()
    throws ConnectException, AdminException {
      return getDefaultDMQId(localServer);
    }
  
  /**
   * Returns the default dead message queue for a given server, null if not
   * set.
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public static String getDefaultDMQId(int serverId)
    throws ConnectException, AdminException {
      Monitor_GetDMQSettings request = new Monitor_GetDMQSettings(serverId);
      Monitor_GetDMQSettingsRep reply;
      reply = (Monitor_GetDMQSettingsRep) doRequest(request);

      if (reply.getDMQName() == null)
        return null;
      else
        return reply.getDMQName();
    }

  /**
   * Returns the default threshold value for a given server, -1 if not set.
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public static int getDefaultThreshold(int serverId)
    throws ConnectException, AdminException
    {
      Monitor_GetDMQSettings request = new Monitor_GetDMQSettings(serverId);
      Monitor_GetDMQSettingsRep reply;
      reply = (Monitor_GetDMQSettingsRep) doRequest(request);

      if (reply.getThreshold() == null)
        return -1;
      else
        return reply.getThreshold().intValue();
    }

  /**
   * Returns the default threshold value for the local server, -1 if not set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public static int getDefaultThreshold()
    throws ConnectException, AdminException
    {
      return getDefaultThreshold(localServer);
    }

  /**
   * Returns the list of all destinations that exist on a given server,
   * or an empty list if none exist.
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static List getDestinations(int serverId)
    throws ConnectException, AdminException
    {
      Monitor_GetDestinations request = new Monitor_GetDestinations(serverId);
      Monitor_GetDestinationsRep reply =
        (Monitor_GetDestinationsRep) doRequest(request);

      Vector list = new Vector();
      String[] ids = reply.getIds();
      String[] names = reply.getNames();
      String[] types = reply.getTypes();
      for (int i = 0; i < types.length; i++) {
        list.addElement(Destination.newInstance(
                          ids[i], names[i], types[i]));
      }
      return list;
    }

  /**
   * Returns the list of all destinations that exist on the local server,
   * or an empty list if none exist.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  Never thrown.
   */
  public static List getDestinations() throws ConnectException, AdminException
    {
      return getDestinations(localServer);
    }

  /**
   * Returns the list of all destinations that exist on a given server,
   * or an empty list if none exist.
   * The request is abort after delay.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static List getDestinations(int serverId, long delay)
    throws ConnectException, AdminException {

    Monitor_GetDestinations request = new Monitor_GetDestinations(serverId);
    Monitor_GetDestinationsRep reply =
      (Monitor_GetDestinationsRep) doRequest(request,delay);

    Vector list = new Vector();
    String[] ids = reply.getIds();
    String[] names = reply.getNames();
    String[] types = reply.getTypes();
    for (int i = 0; i < types.length; i++) {
      list.addElement(Destination.newInstance(
                        ids[i], names[i], types[i]));
    }
    return list;
  }

  /**
   * Returns the list of all users that exist on a given server, or an empty
   * list if none exist.
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public static List getUsers(int serverId)
    throws ConnectException, AdminException
    {
      Monitor_GetUsers request = new Monitor_GetUsers(serverId);
      Monitor_GetUsersRep reply = (Monitor_GetUsersRep) doRequest(request);

      Vector list = new Vector();
      Hashtable users = reply.getUsers();
      String name;
      for (Enumeration names = users.keys(); names.hasMoreElements();) {
        name = (String) names.nextElement();
        list.add(new User(name, (String) users.get(name)));
      }
      return list;
    }

  /**
   * Returns the list of all users that exist on a given server, or an empty
   * list if none exist.
   * The request is abort after delay.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public static List getUsers(int serverId, long delay)
    throws ConnectException, AdminException {

    Monitor_GetUsers request = new Monitor_GetUsers(serverId);
    Monitor_GetUsersRep reply = (Monitor_GetUsersRep) doRequest(request,delay);

    Vector list = new Vector();
    Hashtable users = reply.getUsers();
    String name;
    for (Enumeration names = users.keys(); names.hasMoreElements();) {
      name = (String) names.nextElement();
      list.add(new User(name, (String) users.get(name)));
    }
    return list;
  }

  /**
   * Returns the list of all users that exist on the local server, or an empty
   * list if none exist.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public static List getUsers() throws ConnectException, AdminException
    {
      return getUsers(localServer);
    }


  /**
   * Returns the identifier of the server the module is connected to.
   *
   * @exception ConnectException  If the admin connection is not established.
   */
  public static int getLocalServerId() throws ConnectException
    {
      if (cnx == null)
        throw new ConnectException("Administrator not connected.");

      return localServer;
    }

  /**
   * Returns the host name of the server the module is connected to.
   *
   * @exception ConnectException  If the admin connection is not established.
   */
  public static String getLocalHost() throws ConnectException
    {
      if (cnx == null)
        throw new ConnectException("Administrator not connected.");

      return localHost;
    }

  /**
   * Returns the port number of the server the module is connected to.
   *
   * @exception ConnectException  If the admin connection is not established.
   */
  public static int getLocalPort() throws ConnectException
    {
      if (cnx == null)
        throw new ConnectException("Administrator not connected.");

      return localPort;
    }

  /**
   * Method actually sending an <code>AdminRequest</code> instance to
   * the platform and getting an <code>AdminReply</code> instance.
   *
   * @exception ConnectException  If the connection to the platform fails.
   * @exception AdminException  If the platform's reply is invalid, or if
   *              the request failed.
   */
  public static AdminReply doRequest(AdminRequest request)
    throws AdminException, ConnectException {
    return doRequest(request,requestTimeout);
  }

  /**
   * Method actually sending an <code>AdminRequest</code> instance to
   * the platform and getting an <code>AdminReply</code> instance.
   *
   * @exception ConnectException  If the connection to the platform fails.
   * @exception AdminException  If the platform's reply is invalid, or if
   *              the request failed.
   */
  public static AdminReply doRequest(AdminRequest request, long timeout)
    throws AdminException, ConnectException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, "AdminModule.doRequest(" + request + ')');

    if (cnx == null)
      throw new ConnectException("Admin connection not established.");

    if (timeout < 1)
      timeout = requestTimeout;

    try {
      replyMsg = (AdminMessage) requestor.request(request, timeout);
      reply = (AdminReply) replyMsg.getAdminMessage();

      if (! reply.succeeded()) {
        switch (reply.getErrorCode()) {
        case AdminReply.NAME_ALREADY_USED:
          throw new NameAlreadyUsedException(reply.getInfo());
        case AdminReply.START_FAILURE:
          throw new StartFailureException(reply.getInfo());
        case AdminReply.SERVER_ID_ALREADY_USED:
          throw new ServerIdAlreadyUsedException(reply.getInfo());
        case AdminReply.UNKNOWN_SERVER:
          throw new UnknownServerException(reply.getInfo());
        default:
          throw new AdminException(reply.getInfo());
        }
      } else {
        return reply;
      }
    } catch (JMSException exc) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(
          BasicLevel.DEBUG, "", exc);
      throw new ConnectException("Connection failed: " + exc.getMessage());
    } catch (ClassCastException exc) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(
          BasicLevel.DEBUG, "", exc);
      throw new AdminException("Invalid server reply: " + exc.getMessage());
    }
  }

  public static void abortRequest() throws JMSException {
    if (requestor != null) {
      requestor.abort();
    } else throw new JMSException("Not connected");
  }

  public static class AdminRequestor {
    private javax.jms.TopicSession sess;
    private javax.jms.Topic topic;
    private TemporaryTopic tmpTopic;
    private MessageProducer producer;
    private MessageConsumer consumer;

    public AdminRequestor(javax.jms.TopicConnection cnx)
      throws JMSException {
      sess = cnx.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
      topic = sess.createTopic("#AdminTopic");
      producer = sess.createProducer(topic);
      tmpTopic = sess.createTemporaryTopic();
      consumer = sess.createConsumer(tmpTopic);
    }

    public javax.jms.Message request(AdminRequest request,
                                     long timeout) throws JMSException {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(
          BasicLevel.DEBUG,
          "AdminModule.AdminRequestor.request(" +
          request + ',' + timeout + ')');

      requestMsg = new AdminMessage();
      requestMsg.setAdminMessage(request);
      requestMsg.setJMSReplyTo(tmpTopic);
      producer.send(requestMsg);
      String correlationId = requestMsg.getJMSMessageID();
      while (true) {
        javax.jms.Message reply = consumer.receive(timeout);
        if (reply == null) {
          throw new JMSException("Interrupted request");
        } else {
          if (correlationId.equals(
                reply.getJMSCorrelationID())) {
            return reply;
          } else {
            if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
              JoramTracing.dbgClient.log(
                BasicLevel.DEBUG,
                "reply id (" + reply.getJMSCorrelationID() +
                ") != request id (" + correlationId + ")");
            continue;
          }
        }
      }
    }

    public void abort() throws JMSException {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(
          BasicLevel.DEBUG, "AdminModule.AdminRequestor.abort()");
      consumer.close();
      consumer = sess.createConsumer(tmpTopic);
    }

    public int getLocalServerId() {
      try {
        String topicName = topic.getTopicName();
        int ind0 = topicName.indexOf(".");
        int ind1 = topicName.indexOf(".", ind0 + 1);
        return Integer.parseInt(topicName.substring(ind0 + 1, ind1));
      } catch (JMSException exc) {
        return -1;
      }
    }

    public void close() throws JMSException {
      consumer.close();
      producer.close();
      tmpTopic.delete();
      sess.close();
    }
  }

  /**
   * This method execute the XML script file that the location is given
   * in parameter.
   *
   * @param cfgDir    The directory containing the file.
   * @param cfgFileName    The script filename.
   *
   * @since 4.3.10
   */
  public static boolean executeXMLAdmin(String cfgDir,
                                        String cfgFileName)
    throws Exception {
    return executeXMLAdmin(new File(cfgDir, cfgFileName).getPath());
  }

  /**
   * This method execute the XML script file that the pathname is given
   * in parameter.
   *
   * @param path    The script pathname.
   *
   * @since 4.3.10
   */
  public static boolean executeXMLAdmin(String path) throws Exception {
    if (JoramTracing.dbgAdmin.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgAdmin.log(BasicLevel.DEBUG,"executeXMLAdmin(" + path + ")");

    boolean res = false;
    Reader reader = null;

    // 1st, search XML configuration file in directory.
    File cfgFile = new File(path);
    try {
      if (!cfgFile.exists() || !cfgFile.isFile() || (cfgFile.length() == 0)) {
        throw new IOException();
      }
      reader = new FileReader(cfgFile);
    } catch (IOException exc) {
      // configuration file seems not exist, search it from the
      // search path used to load classes.
      if (JoramTracing.dbgAdmin.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgAdmin.log(BasicLevel.DEBUG,
                                  "Unable to find Joram Admin configuration file \"" +
                                  cfgFile.getPath() + "\".");
      reader = null;
    }

    // 2nd, search XML configuration file in path used to load classes.
    if (reader == null) {
      ClassLoader classLoader = null;
      InputStream is = null;
      try {
        classLoader = AdminModule.class.getClassLoader();
        if (classLoader != null) {
          if (JoramTracing.dbgAdmin.isLoggable(BasicLevel.DEBUG))
            JoramTracing.dbgAdmin.log(BasicLevel.DEBUG,
                                      "Trying to find [" + path + "] using " +
                                      classLoader + " class loader.");
          is = classLoader.getResourceAsStream(path);
        }
      } catch (Throwable t) {
        if (JoramTracing.dbgAdmin.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgAdmin.log(BasicLevel.DEBUG,
                                    "Can't find [" + path + "] using " +
                                    classLoader + " class loader.",
                                    t);
        is = null;
      }

      if (is == null) {
        // Last ditch attempt: get the resource from the class path.
        if (JoramTracing.dbgAdmin.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgAdmin.log(BasicLevel.DEBUG,
                                    "Trying to find [" + path +
                                    "] using ClassLoader.getSystemResource().");
        is = ClassLoader.getSystemResourceAsStream(path);
      }
      if (is != null) {
        res = executeAdmin(new InputStreamReader(is));
      }
    } else {
      res = executeAdmin(reader);
    }

    if (!res)
      throw new FileNotFoundException("xml Joram Admin configuration file not found.");

    return res;
  }

  public static boolean executeAdmin(Reader reader)
    throws Exception {
    if (JoramTracing.dbgAdmin.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgAdmin.log(BasicLevel.DEBUG, "executeAdmin(" + reader + ")");

    String cfgName = System.getProperty(AdminModule.ADM_NAME_PROPERTY,
                                        AdminModule.DEFAULT_ADM_NAME);

    JoramSaxWrapper wrapper = new JoramSaxWrapper();
    return wrapper.parse(reader,cfgName);
  }


  public static void setHa(boolean isHa) {
    AdminModule.isHa = isHa;
  }
}
