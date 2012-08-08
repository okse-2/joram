/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2012 ScalAgent Distributed Technologies
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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.FactoryParameters;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.shared.admin.AdminReply;
import org.objectweb.joram.shared.admin.AdminRequest;
import org.objectweb.joram.shared.security.SimpleIdentity;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * The <code>AdminModule</code> class allows to set an administrator
 * connection to a given JORAM server, and provides administration and
 * monitoring methods at a server/platform level.
 * <p>
 * The <code>AdminModule</code> class uses a unique static connection to
 * the Joram server, the connection is opened through connect method and
 * closed by calling disconnect.
 * 
 * @see AdminWrapper
 */
public final class AdminModule {
  public static final String ADM_NAME_PROPERTY = "JoramAdminXML";
  public static final String DEFAULT_ADM_NAME = "default";

  /** The host name or IP address of the server the module is connected to. */
  protected static String localHost;
  /** The port number of the client connection. */
  protected static int localPort;

  /** Lock object used to avoid multiple connections in case of concurrent connect. */
  private static Object lock = new Object();
  
  /** The connection used to link the administrator and the platform. */
  private static Connection cnx = null;

  /** The administration wrapper needed to interact with the Joram server. */
  static AdminWrapper wrapper;

  /**
   * Returns the administration wrapper.
   * 
   * @return The administration wrapper.
   * @throws ConnectException if no wrapper is defined.
   */
  public static AdminWrapper getWrapper() throws ConnectException {
    if (wrapper == null)
      throw new ConnectException();
    
    return wrapper;
  }
  
  public static Logger logger = Debug.getLogger(AdminModule.class.getName());

  /**
   * This method execute the XML script file that the path is given in
   * parameter.
   *
   * @since 4.3.12
   */
  public static void main(String[] args) {
    try {
      executeXMLAdmin(args[0]);
    } catch (Exception exc) {
      exc.printStackTrace();
    }
  }

  /**
   * Opens a connection dedicated to administering with the Joram server
   * which parameters are wrapped by a given <code>ConnectionFactory</code>.
   * Default administrator login name and password are used for connection
   * as defined in {@link AbstractConnectionFactory#getDefaultRootLogin()}
   * and {@link AbstractConnectionFactory#getDefaultRootPassword()}.
   *
   * @param cf        The Joram's ConnectionFactory to use for connecting.
   *
   * @exception ConnectException  If connecting fails.
   * @exception AdminException  If the administrator identification is incorrect.
   * @exception ClassCastException If the ConnectionFactory is not a Joram ConnectionFactory.
   */
  public static void connect(javax.jms.ConnectionFactory cf) throws ConnectException, AdminException {
    doConnect((AbstractConnectionFactory) cf,
              AbstractConnectionFactory.getDefaultRootLogin(),
              AbstractConnectionFactory.getDefaultRootPassword(),
              SimpleIdentity.class.getName());
  }

  /**
   * Opens a connection dedicated to administering with the Joram server
   * which parameters are wrapped by a given <code>ConnectionFactory</code>.
   *
   * @param cf        The Joram's ConnectionFactory to use for connecting.
   * @param name      Administrator's name.
   * @param password  Administrator's password.
   *
   * @exception ConnectException  If connecting fails.
   * @exception AdminException  If the administrator identification is incorrect.
   * @exception ClassCastException If the ConnectionFactory is not a Joram ConnectionFactory.
   */
  public static void connect(javax.jms.ConnectionFactory cf,
                             String name,
                             String password) throws ConnectException, AdminException {
    doConnect((AbstractConnectionFactory) cf,
              name, password,
              SimpleIdentity.class.getName());
  }

  /**
   * Opens a connection dedicated to administering with the Joram server
   * which parameters are wrapped by a given <code>ConnectionFactory</code>.
   *
   * @param cf            The Joram's ConnectionFactory to use for connecting.
   * @param name          Administrator's name.
   * @param password      Administrator's password.
   * @param identityClass identity class name.
   *
   * @exception ConnectException  If connecting fails.
   * @exception AdminException  If the administrator identification is incorrect.
   * @exception ClassCastException If the ConnectionFactory is not a Joram ConnectionFactory.
   */
  public static void connect(javax.jms.ConnectionFactory cf,
                             String name,
                             String password,
                             String identityClass) throws ConnectException, AdminException {
    doConnect((AbstractConnectionFactory) cf, name, password, identityClass);
  }

  /**
   * Opens a connection dedicated to administering with the Joram server
   * which parameters are wrapped by a given <code>TopicConnectionFactory</code>.
   *
   * @param cf        The TopicConnectionFactory to use for connecting.
   * @param name      Administrator's name.
   * @param password  Administrator's password.
   *
   * @exception ConnectException   If connecting fails.
   * @exception AdminException     If the administrator identification is incorrect.
   * @exception ClassCastException If the ConnectionFactory is not a Joram ConnectionFactory.
   * 
   * @deprecated No longer use TopicConnectionFactory next to Joram 5.2.
   */
  public static void connect(javax.jms.TopicConnectionFactory cf,
                             String name,
                             String password) throws ConnectException, AdminException {
    doConnect((AbstractConnectionFactory) cf, name, password, SimpleIdentity.class.getName());
  }

  /**
   * Opens a connection dedicated to administering with the Joram server
   * which parameters are wrapped by a given <code>TopicConnectionFactory</code>.
   *
   * @param cf            The TopicConnectionFactory to use for connecting.
   * @param name          Administrator's name.
   * @param password      Administrator's password.
   * @param identityClass identity class name.
   *
   * @exception ConnectException   If connecting fails.
   * @exception AdminException     If the administrator identification is incorrect.
   * @exception ClassCastException If the ConnectionFactory is not a Joram ConnectionFactory.
   * 
   * @deprecated No longer use TopicConnectionFactory next to Joram 5.2.
   */
  public static void connect(javax.jms.TopicConnectionFactory cf,
                             String name,
                             String password,
                             String identityClass) throws ConnectException, AdminException {
    doConnect((AbstractConnectionFactory) cf, name, password, identityClass);
  }

  /**
   * Opens a TCP connection with the Joram server running on the default
   * "localhost" host and listening to the default 16010 port.
   * Default administrator login name and password are used for connection
   * as defined in {@link AbstractConnectionFactory#getDefaultRootLogin()}
   * and {@link AbstractConnectionFactory#getDefaultRootPassword()}.
   *
   * @throws UnknownHostException Never thrown.
   * @throws ConnectException     If connecting fails.
   * @throws AdminException       If the administrator identification is incorrect.
   */
  public static void connect() throws UnknownHostException, ConnectException, AdminException {
    doConnect("localhost", 16010,
              AbstractConnectionFactory.getDefaultRootLogin(),
              AbstractConnectionFactory.getDefaultRootPassword(),
              0,
              "org.objectweb.joram.client.jms.tcp.ReliableTcpClient",
              SimpleIdentity.class.getName());
  }

  /**
   * Opens a TCP connection with the Joram server running on the default
   * "localhost" host and listening to the default 16010 port.
   *
   * @param name      Administrator's name.
   * @param password  Administrator's password.
   * 
   * @throws UnknownHostException Never thrown.
   * @throws ConnectException     If connecting fails.
   * @throws AdminException       If the administrator identification is incorrect.
   */
  public static void connect(String name,
                             String password) throws UnknownHostException, ConnectException, AdminException {
    doConnect("localhost", 16010, name, password, 0,
              "org.objectweb.joram.client.jms.tcp.ReliableTcpClient",
              SimpleIdentity.class.getName());
  }

  /**
   * Opens a TCP connection with the Joram server running on the default
   * "localhost" host and listening to the default 16010 port.
   *
   * @param name      Administrator's name.
   * @param password  Administrator's password.
   * @param cnxTimer  Timer in seconds during which connecting to the server is attempted.
   *
   * @exception UnknownHostException  Never thrown.
   * @exception ConnectException      If connecting fails.
   * @exception AdminException        If the administrator identification is incorrect.
   * 
   * @deprecated  Next to Joram 5.2 use connect methods with ConnectionFactory.
   */
  public static void connect(String name,
                             String password,
                             int cnxTimer) throws UnknownHostException, ConnectException, AdminException {
    doConnect("localhost", 16010, name, password, cnxTimer,
              "org.objectweb.joram.client.jms.tcp.ReliableTcpClient",
              SimpleIdentity.class.getName());
  }

  /**
   * Opens a TCP connection with the Joram server running on a given host and
   * listening to a given port.
   *
   * @param host      The name or IP address of the host the server is running on.
   * @param port      The number of the port the server is listening to.
   * @param name      Administrator's name.
   * @param password  Administrator's password.
   *
   * @exception UnknownHostException  If the host is invalid.
   * @exception ConnectException      If connecting fails.
   * @exception AdminException        If the administrator identification is incorrect.
   */
  public static void connect(String host,
                             int port,
                             String name,
                             String password) throws UnknownHostException, ConnectException, AdminException {
    doConnect(host, port,
              name, password,
              0,
              "org.objectweb.joram.client.jms.tcp.ReliableTcpClient",
              SimpleIdentity.class.getName());
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
   * 
   * @deprecated  Next to Joram 5.2 use connect methods with ConnectionFactory.
   */
  public static void connect(String host,
                             int port,
                             String name,
                             String password,
                             int cnxTimer) throws UnknownHostException, ConnectException, AdminException {
    doConnect(host, port,
              name, password,
              cnxTimer,
              "org.objectweb.joram.client.jms.tcp.ReliableTcpClient",
              SimpleIdentity.class.getName());
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
   * 
   * @deprecated  Next to Joram 5.2 use connect methods with ConnectionFactory.
   */
  public static void connect(String name,
                             String password,
                             int cnxTimer,
                             String reliableClass) throws UnknownHostException, ConnectException, AdminException {
    doConnect("localhost", 16010,
              name, password,
              cnxTimer,
              reliableClass,
              SimpleIdentity.class.getName());
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
   * 
   * @deprecated  Next to Joram 5.2 use connect methods with ConnectionFactory.
   */
  public static void connect(String host,
                             int port,
                             String name,
                             String password,
                             int cnxTimer,
                             String reliableClass) throws UnknownHostException, ConnectException, AdminException {
    doConnect(host, port,
              name, password,
              cnxTimer,
              reliableClass,
              SimpleIdentity.class.getName());
  }

  /**
   * Opens a TCP connection with the Joram server running on a given host and
   * listening to a given port.
   *
   * @param host  The name or IP address of the host the server is running on.
   * @param port  The number of the port the server is listening to.
   * @param name  Administrator's name.
   * @param password  Administrator's password.
   * @param cnxTimer  Timer in seconds during which connecting to the server is attempted.
   * @param reliableClass  Reliable class name.
   * @param identityClass identity class name.
   *
   * @exception UnknownHostException  If the host is invalid.
   * @exception ConnectException  If connecting fails.
   * @exception AdminException  If the administrator identification is
   *              incorrect.
   * 
   * @deprecated  Next to Joram 5.2 use connect methods with ConnectionFactory.
   */
  public static void connect(String host,
                             int port,
                             String name,
                             String password,
                             int cnxTimer,
                             String reliableClass,
                             String identityClass) throws UnknownHostException, ConnectException, AdminException {
    doConnect(host, port,
              name, password,
              cnxTimer,
              reliableClass,
              identityClass);
  }

  /**
   * Opens a TCP connection with the Joram server running on a given host and
   * listening to a given port.
   *
   * @param host  The name or IP address of the host the server is running on.
   * @param port  The number of the port the server is listening to.
   * @param name  Administrator's name.
   * @param password  Administrator's password.
   * @param cnxTimer  Timer in seconds during which connecting to the server is attempted.
   * @param reliableClass  Reliable class name.
   * @param identityClass identity class name.
   *
   * @exception UnknownHostException  If the host is invalid.
   * @exception ConnectException  If connecting fails.
   * @exception AdminException  If the administrator identification is
   *              incorrect.
   */
  private static void doConnect(String host,
                                int port,
                                String name,
                                String password,
                                int cnxTimer,
                                String reliableClass,
                                String identityClass) throws UnknownHostException, ConnectException, AdminException {
    ConnectionFactory cf = TcpConnectionFactory.create(host, port, reliableClass);
    cf.getParameters().connectingTimer = cnxTimer;
    doConnect(cf, name, password, identityClass);
  }

  /**
   * Opens a connection with the collocated Joram server.
   * <p>
   * Default administrator login name and password are used for connection
   * as defined in {@link AbstractConnectionFactory#getDefaultRootLogin()}
   * and {@link AbstractConnectionFactory#getDefaultRootPassword()}.
   *
   * @throws UnknownHostException Never thrown.
   * @throws ConnectException     If connecting fails.
   * @throws AdminException       If the administrator identification is incorrect.
   */
  public static void collocatedConnect() throws ConnectException, AdminException {
    doCollocatedConnect(AbstractConnectionFactory.getDefaultRootLogin(),
                        AbstractConnectionFactory.getDefaultRootPassword(),
                        SimpleIdentity.class.getName());
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
  public static void collocatedConnect(String name, String password) throws ConnectException, AdminException {
    doCollocatedConnect(name, password, SimpleIdentity.class.getName());
  }

  /**
   * Opens a connection with the collocated JORAM server.
   *
   * @param name  Administrator's name.
   * @param password  Administrator's password.
   * @param identityClass identity class name.
   *
   * @exception ConnectException  If connecting fails.
   * @exception AdminException  If the administrator identification is incorrect.
   * 
   * @deprecated  Next to Joram 5.2 use connect methods with ConnectionFactory.
   */
  public static void collocatedConnect(String name, String password,
                                       String identityClass) throws ConnectException, AdminException {
    doCollocatedConnect(name, password, identityClass);
  }
  
  /**
   * Opens a connection with the collocated JORAM server.
   *
   * @param name  Administrator's name.
   * @param password  Administrator's password.
   * @param identityClass identity class name.
   *
   * @exception ConnectException  If connecting fails.
   * @exception AdminException  If the administrator identification is incorrect.
   */
  public static void doCollocatedConnect(String name, String password,
                                         String identityClass) throws ConnectException, AdminException {
    doConnect(LocalConnectionFactory.create(), name, password, identityClass);
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
  private static void doConnect(AbstractConnectionFactory cf,
                                String name,
                                String password,
                                String identityClass) throws ConnectException, AdminException {
    synchronized(lock) {
      if (wrapper != null) {
        // We should throw an exception, the asked connection may use a different CF in
        // order to connect to another server!
        logger.log(BasicLevel.DEBUG, "AdminModule.doConnect: Already connected.");
        throw new ConnectException("Already connected.");
      }

      //  set identity className
      cf.setIdentityClassName(identityClass);

      try {
        cnx = cf.createConnection(name, password);
        cnx.start();
        wrapper = new AdminWrapper(cnx);

        FactoryParameters params = cf.getParameters();
        localHost = params.getHost();
        localPort = params.getPort();
      } catch (JMSSecurityException exc) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "AdminModule.doConnect", exc);
        throw new AdminException(exc.getMessage());
      } catch (JMSException exc) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "AdminModule.doConnect", exc);
        throw new ConnectException("Connecting failed: " + exc);
      }
    }
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
   * 
   * @deprecated
   */
  public static AdminReply doRequest(AdminRequest request) throws AdminException, ConnectException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Admin.doRequest(" + request + ')');

    if (wrapper == null)
      throw new ConnectException("Admin connection not established.");

    return wrapper.doRequest(request);
  }

  /** Closes the administration connection. */
  public static void disconnect() {
    if (wrapper != null) wrapper.close();
    wrapper = null;
    
    if (cnx != null) {
      try {
        cnx.close();
      } catch (JMSException exc) {}
      cnx = null;
    }
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
  public static void stopServer(int serverId) throws ConnectException, AdminException {
    if (wrapper == null)
      throw new ConnectException("Administration connection is closed.");
    
    boolean local = (serverId == getLocalServerId());
    wrapper.stopServer(serverId);
    if (local) disconnect();
  }

  /**
   * Stops the platform local server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public static void stopServer() throws ConnectException, AdminException {
    stopServer(getLocalServerId());
  }

  /**
   * Adds a server to the platform.
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
  public static void addServer(int sid,
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
  public static void addServer(int sid,
                               String host,
                               String domain,
                               int port,
                               String server,
                               String[] services,
                               String[] args) throws ConnectException, AdminException {
    if (wrapper == null)
      throw new ConnectException("Administrator not connected.");

    wrapper.addServer(sid, host, domain, port, server, services, args);
  }

  /**
   * Removes a server from the platform.
   *
   * @param sid Id of the removed server
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public static void removeServer(int sid) throws ConnectException, AdminException {
    if (wrapper == null)
      throw new ConnectException("Administrator not connected.");

    wrapper.removeServer(sid);
  }

  /**
   * Adds a domain to the platform.
   *
   * @param domain Name of the added domain.
   * @param sid Id of the router server that gives access to the added domain.
   * @param port Listening port in the added domain of the router server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public static void addDomain(String domain,
                               int sid,
                               int port) throws ConnectException, AdminException {
    if (wrapper == null)
      throw new ConnectException("Administrator not connected.");

    wrapper.addDomain(domain, sid, port);
  }

  /**
   * Adds a domain to the platform using a specific network component.
   *
   * @param domain Name of the added domain.
   * @param network    Classname of the network component to use.
   * @param sid Id of the router server that gives access to the added domain.
   * @param port Listening port in the added domain of the router server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public static void addDomain(String domain,
                               String network,
                               int sid,
                               int port) throws ConnectException, AdminException {
    if (wrapper == null)
      throw new ConnectException("Administrator not connected.");

    wrapper.addDomain(domain, network, sid, port);
  }

  /**
   * Removes a domain from the platform.
   *
   * @param domain Name of the added domain
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public static void removeDomain(String domain) throws ConnectException, AdminException {
    if (wrapper == null)
      throw new ConnectException("Administrator not connected.");

    wrapper.removeDomain(domain);
  }

  /**
   * Returns the current servers configuration (a3servers.xml).
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public static String getConfiguration() throws ConnectException, AdminException {
    if (wrapper == null)
      throw new ConnectException("Administrator not connected.");

    return wrapper.getConfiguration();
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
  public static Hashtable getStatistics() throws ConnectException, AdminException {
    return getStatistics(getLocalServerId());
  }

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
  public static Hashtable getStatistics(int serverId) throws ConnectException, AdminException {
    if (wrapper == null)
      throw new ConnectException("Administrator not connected.");

    return wrapper.getStatistics(serverId);
  }
  
  /**
   * Returns the default dead message queue for the local server, null if not
   * set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public static Queue getDefaultDMQ() throws ConnectException, AdminException {
    return getDefaultDMQ(getLocalServerId());
  }

  /**
   * Returns the default dead message queue for a given server, null if not set.
   * <p>
   * The request fails if the target server does not belong to the platform.
   * 
   * @param serverId Unique identifier of the server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public static Queue getDefaultDMQ(int serverId) throws ConnectException, AdminException {
    if (wrapper == null)
      throw new ConnectException("Administrator not connected.");

    return wrapper.getDefaultDMQ(serverId);
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
  public static void setDefaultDMQ(Queue dmq) throws ConnectException, AdminException {
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
  public static void setDefaultDMQ(int serverId, Queue dmq) throws ConnectException, AdminException {
    if (wrapper == null)
      throw new ConnectException("Administrator not connected.");

    wrapper.setDefaultDMQ(serverId, dmq);
  }

  /**
   * Returns the default dead message queue for the local server, null if not
   * set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public static String getDefaultDMQId() throws ConnectException, AdminException {
    return getDefaultDMQId(getLocalServerId());
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
  public static String getDefaultDMQId(int serverId) throws ConnectException, AdminException {
    if (wrapper == null)
      throw new ConnectException("Administrator not connected.");

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
   */
  public static void setDefaultDMQId(String dmqId) throws ConnectException, AdminException {
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
  public static void setDefaultDMQId(int serverId, String dmqId) throws ConnectException, AdminException {
    if (wrapper == null)
      throw new ConnectException("Administrator not connected.");

    wrapper.setDefaultDMQId(serverId, dmqId);
  }

  /**
   * Returns the default threshold value for the local server, -1 if not set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public static int getDefaultThreshold() throws ConnectException, AdminException {
    return getDefaultThreshold(getLocalServerId());
  }

  /**
   * Returns the default threshold value for a given server, -1 if not set.
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public static int getDefaultThreshold(int serverId) throws ConnectException, AdminException {
    if (wrapper == null)
      throw new ConnectException("Administrator not connected.");

    return wrapper.getDefaultThreshold(serverId);
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
  public static void setDefaultThreshold(int threshold) throws ConnectException, AdminException {
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
  public static void setDefaultThreshold(int serverId, int threshold) throws ConnectException, AdminException {
    if (wrapper == null)
      throw new ConnectException("Administrator not connected.");

    wrapper.setDefaultThreshold(serverId, threshold);
  }

  // TODO (AF): Removes the deprecated getServersIds methods, adds getServersIds and
  // getServersNames as defined in AdminWrapper.
  
  /**
   * Returns the list of the platform's servers' identifiers.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   * 
   * @deprecated No longer supported next to Joram 5.2
   */
  public static List getServersIds() throws ConnectException, AdminException {
    return getServersIds(null);
  }

  /**
   * Returns the list of the servers' identifiers that belong to the specified domain
   *
   * @param domain  Name of the domain.
   * 
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   * 
   * @deprecated No longer supported next to Joram 5.2
   */
  public static List getServersIds(String domain) throws ConnectException, AdminException {
    if (wrapper == null)
      throw new ConnectException("Administrator not connected.");

    int[] serverIds = wrapper.getServersIds(domain);
    
    Vector res = new Vector();
    for (int i = 0; i < serverIds.length; i++) {
      res.addElement(new Integer(serverIds[i]));
    }
    return res;
  }

  public static Server[] getServers() throws ConnectException, AdminException {
    return getServers(null);
  }

  /**
   * Returns the list of the servers' that belong to the specified domain
   *
   * @param domain  Name of the domain.
   * @return An array containing the list of the servers of the specified domain.
   * 
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public static Server[] getServers(String domain) throws ConnectException, AdminException {
    if (wrapper == null)
      throw new ConnectException("Administrator not connected.");

    return wrapper.getServers(domain);
  }

  /**
   * Returns the list of the domain names that contains the specified server.
   * 
   * @param serverId Unique identifier of the server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public static String[] getDomainNames(int serverId) throws ConnectException, AdminException {
    return wrapper.getDomainNames(serverId);
  }

  /**
   * Returns the list of all destinations that exist on the local server,
   * or an empty list if none exist.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  Never thrown.
   * 
   * @deprecated No longer supported next to Joram 5.2
   */
  public static List getDestinationsList() throws ConnectException, AdminException {
    return getDestinationsList(getLocalServerId());
  }
  
  /**
   * Returns the list of all destinations that exist on a given server,
   * or an empty list if none exist.
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   * 
   * @deprecated No longer supported next to Joram 5.2
   */
  public static List getDestinationsList(int serverId) throws ConnectException, AdminException {
    if (wrapper == null)
      throw new ConnectException("Administrator not connected.");
    
    Destination[] dest = wrapper.getDestinations(serverId);

    Vector list = new Vector();
    for (int i=0; i < dest.length; i++) {
      list.addElement(dest[i]);
    }
    return list;
  }

  /**
   * Returns the list of all destinations that exist on the local server,
   * or null if none exist.
   *
   * @return An array containing the list of all destinations of the local server
   *         or null if none exists.
   * 
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  Never thrown.
   */
  public static Destination[] getDestinations() throws ConnectException, AdminException {
    return getDestinations(getLocalServerId());
  }
  
  /**
   * Returns the list of all destinations that exist on a given server,
   * or null if none exist.
   * <p>
   * The request fails if the target server does not belong to the platform.
   * 
   * @param serverId The unique identifier of the selected server.
   * @return An array containing the list of all destinations of the local server
   *         or null if none exists.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static Destination[] getDestinations(int serverId) throws ConnectException, AdminException {
    if (wrapper == null)
      throw new ConnectException("Administrator not connected.");
    
    return wrapper.getDestinations(serverId);
  }

  /**
   * Creates or retrieves a queue destination on a given JORAM server.
   *
   * @param serverId   The identifier of the server where deploying the queue.
   * @param name       The name of the queue.
   * @param className  The queue class name.
   * @param prop       The queue properties.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException    If the request fails.
   * 
   * @see AdminWrapper#createQueue(int, String, String, Properties)
   */
  public static Destination createQueue(int serverId,
                                 String name,
                                 String className,
                                 Properties prop) throws ConnectException, AdminException {
    if (wrapper == null)
      throw new ConnectException("Administrator not connected.");
    
    return wrapper.createQueue(serverId, name, className, prop);
  }

  /**
   * Creates or retrieves a topic destination on a given JORAM server.
   *
   * @param serverId   The identifier of the server where deploying the topic.
   * @param name       The name of the topic.
   * @param className  The topic class name.
   * @param prop       The topic properties.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException    If the request fails.
   * 
   * @see AdminWrapper#createTopic(int, String, String, Properties)
   */
  public static Destination createTopic(int serverId,
                                 String name,
                                 String className,
                                 Properties prop) throws ConnectException, AdminException {
    if (wrapper == null)
      throw new ConnectException("Administrator not connected.");
    
    return wrapper.createTopic(serverId, name, className, prop);
  }

  /**
   * Returns the list of all users that exist on the local server, or an empty
   * list if none exist.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   * 
   * @deprecated No longer supported next to Joram 5.2
   */
  public static List getUsersList() throws ConnectException, AdminException {
    return getUsersList(getLocalServerId());
  }

  /**
   * Returns the list of all users that exist on a given server, or an empty
   * list if none exist.
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   * 
   * @deprecated No longer supported next to Joram 5.2
   */
  public static List getUsersList(int serverId) throws ConnectException, AdminException {
    if (wrapper == null)
      throw new ConnectException("Administrator not connected.");

    User[] users = wrapper.getUsers(serverId);

    Vector list = new Vector();
    for (int i=0; i < users.length; i++) {
      list.add(users[i]);
    }
    return list;
  }

  /**
   * Returns the list of all users that exist on the local server, or null if none exist.
   *
   * @return  An array containing all users defined on the local server, or null if none exist.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public static User[] getUsers() throws ConnectException, AdminException {
    return getUsers(getLocalServerId());
  }

  /**
   * Returns the list of all users that exist on a given server, or null if none exist.
   * <p>
   * The request fails if the target server does not belong to the platform.
   * 
   * @param serverId  Unique identifier of the given server.
   * @return  An array containing all users defined on the local server, or null if none exist.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public static User[] getUsers(int serverId) throws ConnectException, AdminException {
    if (wrapper == null)
      throw new ConnectException("Administrator not connected.");

    return wrapper.getUsers(serverId);
  }

  public static Server getLocalServer() throws ConnectException, AdminException {
    if (wrapper == null)
      throw new ConnectException("Administrator not connected.");

    return wrapper.getLocalServer();
  }
  
  /**
   * Admin method creating a user for a given server and instantiating the
   * corresponding <code>User</code> object.
   *
   * @param name                Name of the user.
   * @param password            Password of the user.
   * @param serverId            The identifier of the user's server.
   * @param identityClassName   By default user/password for SimpleIdentity.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException    If the request fails.
   * 
   * @see AdminWrapper#createUser(String, String, int, String)
   */ 
  public static User createUser(String name, String password,
                         int serverId,
                         String identityClassName) throws ConnectException, AdminException {
    if (wrapper == null)
      throw new ConnectException("Administrator not connected.");

    return wrapper.createUser(name, password, serverId, identityClassName);
  }

  /**
   * Returns the identifier of the server the module is connected to.
   *
   * @exception ConnectException  If the admin connection is not established.
   * @throws AdminException 
   */
  public static int getLocalServerId() throws ConnectException, AdminException {
    if (wrapper == null)
      throw new ConnectException("Administrator not connected.");

    return wrapper.getLocalServerId();
  }

  /**
   * Returns the host name of the server the module is connected to.
   *
   * @exception ConnectException  If the admin connection is not established.
   */
  public static String getLocalHost() throws ConnectException {
    if (wrapper == null)
      throw new ConnectException("Administrator not connected.");

    return localHost;
  }

  /**
   * Returns the port number of the server the module is connected to.
   *
   * @exception ConnectException  If the admin connection is not established.
   */
  public static int getLocalPort() throws ConnectException {
    if (wrapper == null)
      throw new ConnectException("Administrator not connected.");

    return localPort;
  }

  public static void abortRequest() throws ConnectException {
    if (wrapper == null)
      throw new ConnectException("Administrator not connected.");

    wrapper.abortRequest();
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
  public static void executeXMLAdmin(String cfgDir,
                                        String cfgFileName) throws Exception {
    executeXMLAdmin(new File(cfgDir, cfgFileName).getPath());
  }

  /**
   * This method execute the XML script file that the pathname is given
   * in parameter.
   *
   * @param path    The script pathname.
   *
   * @since 4.3.10
   */
  public static void executeXMLAdmin(String path) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AdminModule.executeXMLAdmin(" + path + ")");
    

    // 1st, search XML configuration file in directory.
    Reader reader = null;
    File cfgFile = new File(path);
    try {
      if (!cfgFile.exists() || !cfgFile.isFile() || (cfgFile.length() == 0)) {
        throw new IOException();
      }
      reader = new FileReader(cfgFile);
    } catch (IOException exc) {
      // configuration file seems not exist, search it from the
      // search path used to load classes.
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
                   "Unable to find Joram Admin configuration file \"" + cfgFile.getPath() + "\".");
      reader = null;
    }
    if (reader != null) {
      executeAdmin(reader);
      return;
    }

    // 2nd, search XML configuration file in path used to load classes.
    InputStream is = null;
    ClassLoader classLoader = null;
    try {
      classLoader = AdminModule.class.getClassLoader();
      if (classLoader != null) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG,
                     "Trying to find [" + path + "] using AdminModule class loader.");
        is = classLoader.getResourceAsStream(path);
      }
    } catch (Throwable t) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
                   "Can't find [" + path + "] using AdminModule class loader.", t);
      is = null;
    }

    if (is == null) {
      // Last ditch attempt: get the resource from the class path.
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
                   "Trying to find [" + path + "] using ClassLoader.getSystemResource().");
      is = ClassLoader.getSystemResourceAsStream(path);
    }
    
    if (is == null)
      throw new FileNotFoundException("XML Joram configuration file \"" + path + "\" not found.");

    executeAdmin(new InputStreamReader(is));
  }

  public static void executeAdmin(Reader reader) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "executeAdmin(Reader)");

    String cfgName = System.getProperty(ADM_NAME_PROPERTY, DEFAULT_ADM_NAME);

    JoramSaxWrapper wrapper = new JoramSaxWrapper();
    wrapper.parse(reader,cfgName);
  }
  
  /**
   * Export the repository content to an XML file
   * - only the destinations objects are retrieved in this version
   * - xml script format of the admin objects (joramAdmin.xml)
   * 
   * @param exportDir       target directory where the export file will be put
   * @param exportFilename  filename of the export file
   * @throws AdminException if an error occurs
   */
  public static void exportRepositoryToFile(String exportDir,
                                            String exportFilename) throws AdminException {

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "export repository to " + exportDir.toString());

    
    StringBuffer strbuf = new StringBuffer();
    int indent = 0;
    strbuf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                  "<!--\n" +
                  " Exported JMS objects : \n" +
                  " - destinations : Topic/Queue \n" +
                  " The file can be reloaded through the admin interface (joramAdmin.executeXMLAdmin())\n" +
                  "-->\n" +
    "<JoramAdmin>\n");
    indent += 2;

    // Get the list of servers
    int[] servers;
    try {
      servers = wrapper.getServersIds();
    } catch (Exception exc) {
      throw new AdminException("exportRepositoryToFile() failed - " + exc);
    }

    if (servers != null) {
      // For each server
      for (int i=0; i<servers.length; i++) {
        try {
          // Export the JMS destinations
          Destination[] dest = wrapper.getDestinations(servers[i]);
          for (int j=0; j<dest.length; j++) {
            strbuf.append(dest[j].toXml(indent, servers[i]));
          }
        } catch (Exception exc) {
          throw new AdminException("exportRepositoryToFile() failed - " + exc);
        }
      }

      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "exported objects : \n" + strbuf.toString());
      }
    }

    indent -= 2;
    strbuf.append("</JoramAdmin>");

    // Flush the file in the specified directory
    File exportFile = null;
    FileOutputStream fos = null;

    try {
      exportFile = new File(exportDir, exportFilename);
      fos = new FileOutputStream(exportFile);
      fos.write(strbuf.toString().getBytes());
    } catch(Exception ioe) {
      throw new AdminException("exportRepositoryToFile() failed - " + ioe);
    } finally {
      try {
        exportFile = null;
        fos.close();
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG, "Unable to close the file  : " + fos);
        }
      }
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "File : " + exportDir + "/" + exportFilename + " created");
      }
    }
  }

  /**
   * Sets the timeout in ms before abortion of administration requests.
   * <p>
   * Be careful, the value can be changed prior to the connection only using
   * the <code>AdminRequestor.REQUEST_TIMEOUT_PROP</code> property.
   * 
   * @param timeOut The timeout
   * @throws ConnectException if the connection is not established.
   * 
   * @since 5.2.2
   */
  public static void setTimeOutToAbortRequest(long timeOut) throws ConnectException {
    if (wrapper == null)
      throw new ConnectException("Administrator not connected.");
    
    wrapper.setTimeOutToAbortRequest(timeOut);
  }

  /**
   * Gets the timeout in ms before abortion of administration requests.
   * 
   * @return the timeout
   * @throws ConnectException if the connection is not established.
   * 
   * @since 5.2.2
   */
  public static long getTimeOutToAbortRequest() throws ConnectException {
    if (wrapper == null)
      throw new ConnectException("Administrator not connected.");
    
    return wrapper.getTimeOutToAbortRequest();
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
  public static AdminReply processAdmin(String targetId, int command, Properties prop) throws ConnectException, AdminException {
  	if (wrapper == null)
      throw new ConnectException("Administrator not connected.");
  	
  	return wrapper.processAdmin(targetId, command, prop);
  }

  /**
   * Invokes the specified static method with the specified parameters on the
   * local server. The parameters types of the invoked method must be java
   * primitive types, the java objects wrapping them or String type.
   * 
   * @param className the name of the class holding the static method
   * @param methodName the name of the invoked method
   * @param parameterTypes the list of parameters
   * @param args the arguments used for the method call
   * @return the result of the invoked method after applying
   *         {@link Object#toString()} method
   * @throws ConnectException If the connection fails.
   * @throws AdminException If the invocation can't be done or fails
   */
  public static String invokeStaticServerMethod(String className,
                                                String methodName,
                                                Class<?>[] parameterTypes,
                                                Object[] args) throws ConnectException, AdminException {
    return invokeStaticServerMethod(getLocalServerId(), className, methodName, parameterTypes, args);
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
   * @return the result of the invoked method after applying
   *         {@link Object#toString()} method
   * @throws ConnectException If the connection fails.
   * @throws AdminException If the invocation can't be done or fails
   */
  public static String invokeStaticServerMethod(int serverId,
                                                String className,
                                                String methodName,
                                                Class<?>[] parameterTypes,
                                                Object[] args) throws ConnectException, AdminException {
    if (wrapper == null)
      throw new ConnectException("Administration connection is closed.");

   return wrapper.invokeStaticServerMethod(serverId, className, methodName, parameterTypes, args);
  }
  
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
  public static String addAMQPBridgeConnection(int serverId, String urls) throws ConnectException, AdminException {
  	if (wrapper == null)
      throw new ConnectException("Administration connection is closed.");
  	return wrapper.addAMQPBridgeConnection(serverId, urls);
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
  public static String deleteAMQPBridgeConnection(int serverId, String names) throws ConnectException, AdminException {
  	if (wrapper == null)
      throw new ConnectException("Administration connection is closed.");
  	return wrapper.deleteAMQPBridgeConnection(serverId, names);
  }
  
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
  public static String addJMSBridgeConnection(int serverId, String urls) throws ConnectException, AdminException {
  	if (wrapper == null)
      throw new ConnectException("Administration connection is closed.");
  	return wrapper.addJMSBridgeConnection(serverId, urls);
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
  public static String deleteJMSPBridgeConnection(int serverId, String names) throws ConnectException, AdminException {
  	if (wrapper == null)
      throw new ConnectException("Administration connection is closed.");
  	return wrapper.deleteJMSPBridgeConnection(serverId, names);
  }
}
