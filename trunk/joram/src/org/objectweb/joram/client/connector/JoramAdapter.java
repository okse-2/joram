/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - ScalAgent Distributed Technologies
 * Copyright (C) 2004 - Bull SA
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
 * Contributor(s): Nicolas Tachker (Bull SA)
 *                 ScalAgent Distributed Technologies
 */
package org.objectweb.joram.client.connector;

import fr.dyade.aaa.agent.AgentServer;
import com.scalagent.jmx.JMXServer;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.JoramAdmin;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.admin.DeadMQueue;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;
import org.objectweb.joram.client.jms.local.QueueLocalConnectionFactory;
import org.objectweb.joram.client.jms.local.TopicLocalConnectionFactory;
import org.objectweb.joram.client.jms.local.XALocalConnectionFactory;
import org.objectweb.joram.client.jms.tcp.QueueTcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TopicTcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.XATcpConnectionFactory;
import org.objectweb.joram.client.jms.ConnectionMetaData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.TopicConnectionFactory;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.CommException;
import javax.resource.spi.IllegalStateException;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.XAResource;

import org.objectweb.util.monolog.api.BasicLevel;

/** 
 * A <code>JoramAdapter</code> instance manages connectivities to an
 * underlying JORAM server: outbound connectivity (JCA connection
 * management contract) and inbound connectivity (asynchronous message
 * delivery as specified by the JCA message inflow contract).
 */
public class JoramAdapter 
  implements javax.resource.spi.ResourceAdapter,
             java.io.Serializable, JoramAdapterMBean {
  /** <code>WorkManager</code> instance provided by the application server. */
  private transient WorkManager workManager;

  /**
   * Table holding the adapter's <code>InboundConsumer</code> instances,
   * for inbound messaging.
   * <p>
   * <b>Key:</b> <code>ActivationSpec</code> instance<br>
   * <b>Value:</b> <code>InboundConsumer</code> instance
   */
  private transient Hashtable consumers;
  /**
   * Vector holding the <code>ManagedConnectionImpl</code> instances for
   * managed outbound messaging.
   */
  private transient Vector producers;
  /**
   * Table holding the adapter's <code>XAConnection</code> instances used for
   * recovering the XA resources.
   * <p>
   * <b>Key:</b> user name<br>
   * <b>Value:</b> <code>XAConnection</code> instance
   */
  private transient Hashtable connections;

  /** <code>true</code> if the adapter has been started. */
  private boolean started = false;
  /** <code>true</code> if the adapter has been stopped. */
  private boolean stopped = false;

  /** <code>true</code> if the underlying JORAM server is collocated. */
  boolean collocated = false;
  /** Host name or IP of the underlying JORAM server. */
  String hostName = "localhost";
  /** Port number of the underlying JORAM server. */
  int serverPort = 16010;

  /** Identifier of the JORAM server to start. */
  short serverId = 0;

  /** Platform servers identifiers. */
  List platformServersIds = null;

  /**
   * Path to the directory containing JORAM's configuration files
   * (<code>a3servers.xml</code>, <code>a3debug.cfg</code>
   * and admin file), needed when starting the collocated JORAM server.
   */
  private String platformConfigDir;
  /** <code>true</code> if the JORAM server to start is persistent. */
  private boolean persistentPlatform = false;
  /**
   * Path to the file containing a description of the administered objects to
   * create and bind.
   */
  private String adminFile = "joram-admin.cfg";

  /** Name of the JORAM server to start. */
  private String serverName = "s0";

  /** Names of the bound objects. */
  private static Vector boundNames = new Vector();
  /** Standard JMSResource MBean ObjectName. */
  private static ObjectName jmsResourceON;
  /** Local MBean server. */
  private static MBeanServer mbs = null;

  /**
   * Duration in seconds during which connecting is attempted (connecting
   * might take time if the server is temporarily not reachable); the 0 value
   * is set for connecting only once and aborting if connecting failed.
   */
  public int connectingTimer = 0;
  /**
   * Duration in seconds during which a JMS transacted (non XA) session might
   * be pending; above that duration the session is rolled back and closed;
   * the 0 value means "no timer".
   */
  public int txPendingTimer = 0;
  /** 
   * Period in milliseconds between two ping requests sent by the client
   * connection to the server; if the server does not receive any ping
   * request during more than 2 * cnxPendingTimer, the connection is
   * considered as dead and processed as required.
   */
  public int cnxPendingTimer = 0;


  public JMXServer jmxServer;

  private transient JoramAdmin joramAdmin;

  /**
   * Constructs a <code>JoramAdapter</code> instance. 
   */
  public JoramAdapter() {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.INFO)) 
      AdapterTracing.dbgAdapter.log(BasicLevel.INFO,
                                    "JORAM adapter instanciated.");

    consumers = new Hashtable();
    producers = new Vector();

    java.util.ArrayList array = MBeanServerFactory.findMBeanServer(null);
    if (!array.isEmpty())
      mbs = (MBeanServer) array.get(0);
    jmxServer = new JMXServer(mbs,"JoramAdapter");
  }

  /**
   * Initializes the adapter; starts, if needed, a collocated JORAM server, 
   * and if needed again, administers it.
   *
   * @exception ResourceAdapterInternalException  If the adapter could not be
   *                                              initialized.
   */
  public synchronized void start(BootstrapContext ctx)
                           throws ResourceAdapterInternalException
  {
    if (started)
      throw new ResourceAdapterInternalException("Adapter already started.");
    if (stopped)
      throw new ResourceAdapterInternalException("Adapter has been stopped.");

    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.INFO)) 
      AdapterTracing.dbgAdapter.log(BasicLevel.INFO,
                                    "JORAM adapter starting deployment...");

    workManager = ctx.getWorkManager();

    // Collocated mode: starting the JORAM server. 
    if (collocated) {

      if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.INFO)) 
        AdapterTracing.dbgAdapter.log(BasicLevel.INFO,
                                      "  - Collocated JORAM server is starting...");

      if (persistentPlatform)
        System.setProperty("Transaction", "fr.dyade.aaa.util.NTransaction");   
      else {
        System.setProperty("Transaction", "fr.dyade.aaa.util.NullTransaction");
        System.setProperty("NbMaxAgents", "" + Integer.MAX_VALUE);
      }

      if (platformConfigDir != null) { 
        System.setProperty("fr.dyade.aaa.agent.A3CONF_DIR", platformConfigDir);
        System.setProperty("fr.dyade.aaa.DEBUG_DIR", platformConfigDir);
      }

      try {
        String[] args = {"" + serverId, serverName};
        AgentServer.init(args);
        AgentServer.start();
        if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.INFO)) 
          AdapterTracing.dbgAdapter.log(BasicLevel.INFO,
                                        "  - Collocated JORAM server has successfully started.");
      }
      catch (Exception exc) {
        throw new ResourceAdapterInternalException("Could not start "
                                                   + "collocated JORAM "
                                                   + " instance: " + exc);
      }
    }

    // Starting an admin session...
    try {
      adminConnect();
      serverId = (short) joramAdmin.getPlatformAdmin().getLocalServerId();
    }
    catch (Exception exc) {
      if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.WARN)) 
        AdapterTracing.dbgAdapter.log(BasicLevel.WARN,
                                      "  - JORAM server not administerable: " + exc);
    }

    // Administering as specified in the properties file.
    try {
      File file = null;

      if (platformConfigDir == null) {
        java.net.URL url = ClassLoader.getSystemResource(adminFile);
        file = new File(url.getFile());
      }
      else
        file = new File(platformConfigDir, adminFile);

      FileReader fileReader = new FileReader(file);
      BufferedReader reader = new BufferedReader(fileReader);

      if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.INFO)) 
        AdapterTracing.dbgAdapter.log(BasicLevel.INFO,
                                      "  - Reading the provided admin file: " + file);

      boolean end = false;
      String line;
      StringTokenizer tokenizer;
      String firstToken;
      String name = null;

      while (! end) {
        try {
          line = reader.readLine();

          if (line == null)
            end = true;
          else {
            tokenizer = new StringTokenizer(line);

            if (tokenizer.hasMoreTokens()) {
              firstToken = tokenizer.nextToken();
              if (firstToken.equalsIgnoreCase("Host")) {
                if (tokenizer.hasMoreTokens())
                  hostName = tokenizer.nextToken();
              }
              else if (firstToken.equalsIgnoreCase("Port")) {
                if (tokenizer.hasMoreTokens())
                  serverPort = Integer.parseInt(tokenizer.nextToken());
              }
              else if (firstToken.equalsIgnoreCase("Queue")) {
                if (tokenizer.hasMoreTokens()) {
                  name = tokenizer.nextToken();
                  createQueue(name);
                }
              }
              else if (firstToken.equalsIgnoreCase("Topic")) {
                if (tokenizer.hasMoreTokens()) {
                  name = tokenizer.nextToken();
                  createTopic(name);
                }
              }
              else if (firstToken.equalsIgnoreCase("User")) {
                if (tokenizer.hasMoreTokens())
                  name = tokenizer.nextToken();
                if (tokenizer.hasMoreTokens()) {
                  String password = tokenizer.nextToken();
                  createUser(name, password);
                }
                else
                  if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
                    AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG,
                                                  "  - Missing password for user [" + name + "]");
              }
              else if (firstToken.equalsIgnoreCase("CF")) {
                if (tokenizer.hasMoreTokens()) {
                  name = tokenizer.nextToken();
                  createCF(name);
                }
              }
              else if (firstToken.equalsIgnoreCase("QCF")) {
                if (tokenizer.hasMoreTokens()) {
                  name = tokenizer.nextToken();
                  createQCF(name);
                }
              }
              else if (firstToken.equalsIgnoreCase("TCF")) {
                if (tokenizer.hasMoreTokens()) {
                  name = tokenizer.nextToken();
                  createTCF(name);
                }
              }
            }
          }
        }
        // Error while reading one line.
        catch (IOException exc) {}
        // Error while creating the destination.
        catch (AdminException exc) {
          AdapterTracing.dbgAdapter.log(BasicLevel.ERROR,
                                        "Creation failed",exc);
        }
      }
    }
    // No destination to deploy.
    catch (java.io.FileNotFoundException fnfe) { 
      if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.INFO)) 
        AdapterTracing.dbgAdapter.log(BasicLevel.INFO,
                                      "  - No administration task requested.");
    }
    
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.INFO)) 
      AdapterTracing.dbgAdapter.log(BasicLevel.INFO,
                                    "Server port is " + serverPort);

    started = true;

    // Registering MBeans...   
    try {
      jmxServer.registerMBean(this, 
                              "joramClient", 
                              "type=JoramAdapter,version=" + 
                              ConnectionMetaData.providerVersion);
    } catch (Exception e) {
      if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.WARN)) 
        AdapterTracing.dbgAdapter.log(BasicLevel.WARN,
                                      "  - Could not register JoramAdapterMBean",
                                      e);
    }

    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.INFO)) 
      AdapterTracing.dbgAdapter.log(BasicLevel.INFO,
                                    "JORAM adapter " + 
                                    ConnectionMetaData.providerVersion + 
                                    " successfully deployed.");
  }

  /**	
   * Notifies the adapter to terminate the connections it manages, and if
   * needed, to shut down the collocated JORAM server.
   */
  public synchronized void stop()
  {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.INFO)) 
      AdapterTracing.dbgAdapter.log(BasicLevel.INFO,
                                    "JORAM adapter stopping...");

    if (! started || stopped)
      return;

    // Unbinds the bound objects...
    while (! boundNames.isEmpty())
      unbind((String) boundNames.remove(0));

    // Finishing the admin session.
    joramAdmin.getPlatformAdmin().disconnect();

    // Closing the outbound connections, if any.
    while (! producers.isEmpty()) {
      try {
        ((ManagedConnectionImpl) producers.remove(0)).destroy();
      }
      catch (Exception exc) {}
    }

    // Closing the inbound connections, if any.
    for (Enumeration keys = consumers.keys(); keys.hasMoreElements();)
      ((InboundConsumer) consumers.get(keys.nextElement())).close();

    // Browsing the recovery connections, if any.
    if (connections != null) {
      for (Enumeration keys = connections.keys(); keys.hasMoreElements();) {
        try {
          ((XAConnection) connections.get(keys.nextElement())).close();
        }
        catch (Exception exc) {}
      }
    }
    
    // If JORAM server is collocated, stopping it.
    if (collocated) {
      try {
        AgentServer.stop();
      }
      catch (Exception exc) {}
    }

    stopped = true;

    try {
      jmxServer.unregisterMBean("joramClient", 
                                "type=JoramAdapter,version=" +
                                ConnectionMetaData.providerVersion);
    } catch (Exception e) {
      if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.WARN)) 
        AdapterTracing.dbgAdapter.log(BasicLevel.WARN,
                                      "unregisterMBean",
                                      e);
    }


    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.INFO)) 
      AdapterTracing.dbgAdapter.log(BasicLevel.INFO,
                                    "JORAM adapter successfully stopped.");
  }

  /**
   * Notifies the adapter to setup asynchronous message delivery for an
   * application server endoint.
   *
   * @exception IllegalStateException  If the adapter is either not started,
   *                                   or stopped.
   * @exception NotSupportedException  If the provided activation parameters 
   *                                   are invalid.
   * @exception CommException          If the JORAM server is not reachable.
   * @exception SecurityException      If connecting is not allowed.
   * @exception ResourceException      Generic exception.
   */
  public void endpointActivation(MessageEndpointFactory endpointFactory,
                                 ActivationSpec spec)
              throws ResourceException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                    this + " endpointActivation(" + endpointFactory + 
                                    ", " + spec + ")");

    if (! started)
      throw new IllegalStateException("Non started resource adapter.");
    if (stopped)
      throw new IllegalStateException("Stopped resource adapter.");

    if (! (spec instanceof ActivationSpecImpl))
      throw new ResourceException("Provided ActivationSpec instance is not "
                                  + "a JORAM activation spec.");

    ActivationSpecImpl specImpl = (ActivationSpecImpl) spec;

    if (! specImpl.getResourceAdapter().equals(this))
      throw new ResourceException("Supplied ActivationSpec instance "
                                  + "associated to an other ResourceAdapter.");

    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG,
                                    "Activating Endpoint on JORAM adapter.");

    boolean durable =
      specImpl.getSubscriptionDurability() != null
      && specImpl.getSubscriptionDurability().equalsIgnoreCase("Durable");

    boolean transacted = false;
    try {
      Class listenerClass = Class.forName("javax.jms.MessageListener");
      Class[] parameters = { Class.forName("javax.jms.Message") };
      Method meth = listenerClass.getMethod("onMessage", parameters);
      transacted = endpointFactory.isDeliveryTransacted(meth);
    }
    catch (Exception exc) {
      throw new ResourceException("Could not determine transactional "
                                  + "context: " + exc);
    }

    int maxWorks = 0;
    try {
      maxWorks = Integer.parseInt(specImpl.getMaxNumberOfWorks());
    }
    catch (Exception exc) {
      throw new ResourceException("Invalid max number of works instances "
                                  + "number: " + exc);
    }

    String destType = specImpl.getDestinationType();
    String destName = specImpl.getDestination();

    try {
      Destination dest;

      if (destType.equals("javax.jms.Queue"))
        dest = createQueue(destName);
      else if (destType.equals("javax.jms.Topic"))
        dest = createTopic(destName);
      else
        throw new NotSupportedException("Invalid destination type provided "
                                        + "as activation parameter: "
                                        + destType);

      String userName = specImpl.getUserName();
      String password = specImpl.getPassword();

      createUser(userName, password);

      XAConnectionFactory connectionFactory = null;

      if (collocated)
        connectionFactory = XALocalConnectionFactory.create();
      else
        connectionFactory =
          XATcpConnectionFactory.create(hostName, serverPort);

      ((org.objectweb.joram.client.jms.XAConnectionFactory) connectionFactory).getParameters().connectingTimer = connectingTimer;
      ((org.objectweb.joram.client.jms.XAConnectionFactory) connectionFactory).getParameters().cnxPendingTimer = cnxPendingTimer;
      ((org.objectweb.joram.client.jms.XAConnectionFactory) connectionFactory).getParameters().txPendingTimer = txPendingTimer;

      XAConnection cnx =
        connectionFactory.createXAConnection(userName, password);

      if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
        AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                      this + " endpointActivation cnx = " + cnx);

      // Creating and registering a consumer instance for this endpoint.
      InboundConsumer consumer =
        new InboundConsumer(workManager,
                            endpointFactory,
                            cnx,
                            dest,
                            specImpl.getMessageSelector(),
                            durable,
                            specImpl.getSubscriptionName(),
                            transacted,
                            maxWorks);

      consumers.put(specImpl, consumer);
    }
    catch (javax.jms.JMSSecurityException exc) {
      throw new SecurityException("Invalid user identification: " + exc);
    }
    catch (javax.jms.JMSException exc) {
      throw new CommException("Could not connect to the JORAM server: "
                              + exc);
    }
    catch (AdminException exc) {
      throw new ResourceException("Problem when handling the JORAM "
                                  + "destinations: " + exc);
    }
  }

  /**
   * Notifies the adapter to deactivate message delivery for a given endpoint.
   */
  public void endpointDeactivation(MessageEndpointFactory endpointFactory,
                                   ActivationSpec spec) {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                    this + " endpointDeactivation(" + endpointFactory + ", " + spec + ")");
    if (! started || stopped)
      return;

    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG,
                                    "Deactivating Endpoint on JORAM adapter.");

    ((InboundConsumer) consumers.remove(spec)).close();
  }

  /**
   * Returns XA resources given an array of ActivationSpec instances.
   *
   * @exception IllegalStateException  If the adapter is either not started,
   *                                   or stopped.
   * @exception NotSupportedException  If provided activation parameters 
   *                                   are invalid.
   * @exception CommException          If the JORAM server is not reachable.
   * @exception SecurityException      If connecting is not allowed.
   * @exception ResourceException      Generic exception.
   */
  public XAResource[] getXAResources(ActivationSpec[] specs)
    throws ResourceException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                    this + " getXAResources(" + specs + ")");
    
    if (! started)
      throw new IllegalStateException("Non started resource adapter.");
    if (stopped)
      throw new IllegalStateException("Stopped resource adapter.");

    ActivationSpecImpl specImpl;
    String userName;
    String password;
    XAConnectionFactory connectionFactory = null;
    XAConnection connection;
    Vector resources = new Vector();

    if (connections == null)
      connections = new Hashtable();

    try {
      for (int i = 0; i < specs.length; i++) {
        if (! (specs[i] instanceof ActivationSpecImpl))
          throw new ResourceException("Provided ActivationSpec instance is "
                                      + "not a JORAM activation spec.");

        specImpl = (ActivationSpecImpl) specs[i];

        if (! specImpl.getResourceAdapter().equals(this))
          throw new ResourceException("Supplied ActivationSpec instance "
                                      + "associated to an other "
                                      + "ResourceAdapter.");

        userName = specImpl.getUserName();

        // The connection does not already exist: creating it.
        if (! connections.containsKey(userName)) {
          password = specImpl.getPassword();

          if (collocated)
            connectionFactory = XALocalConnectionFactory.create();
          else
            connectionFactory =
              XATcpConnectionFactory.create(hostName, serverPort);

          ((org.objectweb.joram.client.jms.XAConnectionFactory) connectionFactory).getParameters().connectingTimer = connectingTimer;
          ((org.objectweb.joram.client.jms.XAConnectionFactory) connectionFactory).getParameters().cnxPendingTimer = cnxPendingTimer;
          ((org.objectweb.joram.client.jms.XAConnectionFactory) connectionFactory).getParameters().txPendingTimer = txPendingTimer;

          connection =
            connectionFactory.createXAConnection(userName, password);
  
          connections.put(userName, connection);
  
          resources.add(connection.createXASession().getXAResource());
        }
        if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
          AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                        this + " getXAResources resources = " + resources);
      }
    }
    catch (javax.jms.JMSSecurityException exc) {
      throw new SecurityException("Invalid user identification: " + exc);
    }
    catch (javax.jms.JMSException exc) {
      throw new CommException("Could not connect to the JORAM server: "
                              + exc);
    }

    return (XAResource[]) resources.toArray(new XAResource[resources.size()]);
  }


  /** Returns a code depending on the adapter properties. */
  public int hashCode()
  {
    return (collocated + " " + hostName + " " + serverPort).hashCode();
  }

  /** Compares adapters according to their properties. */
  public boolean equals(Object o)
  {
    if (! (o instanceof JoramAdapter))
      return false;

    JoramAdapter other = (JoramAdapter) o;

    boolean res = 
      collocated == other.collocated
      && hostName.equals(other.hostName)
      && serverPort == other.serverPort;

    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                    this + " equals = " + res);
    return res;
  }

  public List getDestinations() {
    return joramAdmin.getDestinations();
  }

  public List getDestinations(int serverId) {
    return joramAdmin.getDestinations(serverId);
  }

  public List getUsers() {
    return joramAdmin.getUsers();
  }

  public List getUsers(int serverId) {
    return joramAdmin.getUsers(serverId);
  }

  public List getPlatformServersIds() {
    return joramAdmin.getPlatformAdmin().getServersIds();
  }

  public List getLocalUsers() {
    return joramAdmin.getUsers(serverId);
  }

  public void setDefaultDMQ(int serverId, DeadMQueue dmq)
    throws ConnectException, AdminException {
    joramAdmin.setDefaultDMQ(serverId,dmq);
  }

  public DeadMQueue getDefaultDMQ(int serverId)
    throws ConnectException, AdminException {
    return joramAdmin.getDefaultDMQ(serverId);
  }

  public DeadMQueue getDefaultDMQ()
    throws ConnectException, AdminException {
    return joramAdmin.getDefaultDMQ();
  }
  
  public void exit() {
    joramAdmin.exit();
  }

  /**
   * wait before abort a request.
   */
  public void setTimeOutToAbortRequest(long timeOut) {
    joramAdmin.setTimeOutToAbortRequest(timeOut);
  }

  /**
   * wait before abort a request.
   */
  public long getTimeOutToAbortRequest() {
    return joramAdmin.getTimeOutToAbortRequest();
  }

  /**
   * Creates or retrieves a user on the underlying JORAM server.
   *
   * @exception AdminException   If the creation fails.
   */
  public void createUser(String name, String password) 
    throws AdminException {
    try {
      User.create(name, password);
      if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.INFO)) 
        AdapterTracing.dbgAdapter.log(BasicLevel.INFO,
                                      "  - User [" + name + "] has been created.");
    }
    catch (ConnectException exc) {
      throw new AdminException("createUser() failed: admin connection "
                               + "has been lost.");
    }
  }

  /**
   * Creates or retrieves a user on the underlying JORAM server.
   *
   * @exception AdminException   If the creation fails.
   */
  public void createUser(String name, String password, int serverId) 
    throws AdminException {
    try {
      User.create(name, password,serverId);
      if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.INFO)) 
        AdapterTracing.dbgAdapter.log(BasicLevel.INFO,
                                      "  - User [" + name + "] has been created.");
    }
    catch (ConnectException exc) {
      throw new AdminException("createUser() failed: admin connection "
                               + "has been lost.");
    }
  }

  /**
   * Creates a non managed connection factory and binds it to JNDI.
   */
  public void createCF(String name) {
    ManagedConnectionFactoryImpl mcf = 
      new ManagedConnectionFactoryImpl();

    try {
      mcf.setResourceAdapter(this);
      mcf.setCollocated(new Boolean(false));

      Object factory = mcf.createConnectionFactory();
      bind(name, factory);
      if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.INFO)) 
        AdapterTracing.dbgAdapter.log(BasicLevel.INFO,
                                      "  - ConnectionFactory [" + name
                                      + "] has been created and bound.");
    } catch (Exception exc) {}
  }

  /**
   * Creates a non managed PTP connection factory and binds it to JNDI.
   */
  public void createQCF(String name) {
    ManagedConnectionFactoryImpl mcf = 
      new ManagedQueueConnectionFactoryImpl();

    try {
      mcf.setResourceAdapter(this);
      mcf.setCollocated(new Boolean(false));

      Object factory = mcf.createConnectionFactory();
      bind(name, factory);
      if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.INFO)) 
        AdapterTracing.dbgAdapter.log(BasicLevel.INFO,
                                      "  - QueueConnectionFactory [" + name
                                      + "] has been created and bound.");
    } catch (Exception exc) {}
  }

  /**
   * Creates a non managed PubSub connection factory and binds it to JNDI.
   */
  public void createTCF(String name) {
    ManagedConnectionFactoryImpl mcf = 
      new ManagedTopicConnectionFactoryImpl();

    try {
      mcf.setResourceAdapter(this);
      mcf.setCollocated(new Boolean(false));

      Object factory = mcf.createConnectionFactory();
      bind(name, factory);
      if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.INFO)) 
        AdapterTracing.dbgAdapter.log(BasicLevel.INFO,
                                      "  - TopicConnectionFactory [" + name
                                      + "] has been created and bound.");
    } catch (Exception exc) {}
  }

  /**
   * Initiates an admin session.
   *
   * @exception AdminException  If the admin session could not be started.
   */
  void adminConnect() throws AdminException
  {
    try {
      TopicConnectionFactory factory;
      if (collocated)
        factory = TopicLocalConnectionFactory.create();
      else
        factory = TopicTcpConnectionFactory.create(hostName, serverPort);
   
      ((org.objectweb.joram.client.jms.ConnectionFactory) factory)
        .getParameters().connectingTimer = 60;

      joramAdmin = new JoramAdmin(factory, "root", "root");
    }
    catch (ConnectException exc) {
      throw new AdminException("Admin connection can't be established: " 
                               + exc.getMessage());
    }
  }

  /** Adds a given managed connection to the list of producers. */
  void addProducer(ManagedConnectionImpl managedCx)
  {
    producers.add(managedCx);
  }

  /** Removes a given managed connection from the list of producers. */
  void removeProducer(ManagedConnectionImpl managedCx)
  {
    producers.remove(managedCx);
  }
  
  /** remove prefix name scn:comp/ */
  private static String removePrefix(String name) {
    String PREFIX_NAME = "scn:comp/";
    try {
      if (name.startsWith(PREFIX_NAME))
        return name.substring(PREFIX_NAME.length());
      else
        return name;
    } catch (Exception e) {
      return name;
    }
  }

  /**
   * Creates or retrieves a queue destination on the underlying JORAM server,
   * (re)binds the corresponding <code>Queue</code> instance.
   *
   * @param serverId   The identifier of the server where deploying the queue.
   * @param name       The name of the queue.
   * @param className  The queue class name.
   * @param prop       The queue properties.
   *
   * @exception AdminException   If the creation fails.
   */
  public Destination createQueue(int serverId,
                                 String name,
                                 String className,
                                 Properties prop)
    throws AdminException {
    try {
      Context ctx = new InitialContext();
      return (Queue) ctx.lookup(name);
    } catch (javax.naming.NamingException exc) {
      try {
        String shortName = removePrefix(name);
        Queue queue = Queue.create(serverId,
                                   shortName,
                                   className,
                                   prop); 
        queue.setFreeReading();
        queue.setFreeWriting();
        if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.INFO)) 
          AdapterTracing.dbgAdapter.log(BasicLevel.INFO,
                                        "  - Queue [" + shortName + "] has been created.");
        bind(name, queue);
        return queue;
      } catch (ConnectException exc2) {
        throw new AdminException("createQueue() failed: admin connection "
                                 + "has been lost.");
      }
    }
  }
  
  /**
   * Creates or retrieves a queue destination on the underlying JORAM server,
   * (re)binds the corresponding <code>Queue</code> instance.
   *
   * @param serverId   The identifier of the server where deploying the queue.
   * @param name       The name of the queue.
   *
   * @exception AdminException   If the creation fails.
   */
  public Destination createQueue(int serverId, String name)
    throws AdminException {
    return createQueue(serverId, 
                       name, 
                       "org.objectweb.joram.mom.dest.Queue", 
                       null);
  }

  /**
   * Creates or retrieves a queue destination on the underlying JORAM server,
   * (re)binds the corresponding <code>Queue</code> instance.
   *
   * @param name       The name of the queue.
   *
   * @exception AdminException   If the creation fails.
   */
  public Destination createQueue(String name)
    throws AdminException {
    try {
      return createQueue(joramAdmin.getPlatformAdmin().getLocalServerId(),
                         name,
                         "org.objectweb.joram.mom.dest.Queue",
                         null);
    } catch (ConnectException exc2) {
      throw new AdminException("createQueue() failed: admin connection "
                               + "has been lost.");
    }
  }

  /**
   * Creates or retrieves a topic destination on the underlying JORAM server,
   * (re)binds the corresponding <code>Topic</code> instance.
   *
   * @param serverId   The identifier of the server where deploying the topic.
   * @param name       The name of the topic.
   * @param className  The topic class name.
   * @param prop       The topic properties.
   *
   * @exception AdminException   If the creation fails.
   */
  public Destination createTopic(int serverId,
                                 String name,
                                 String className,
                                 Properties prop)
    throws AdminException {
    try {
      Context ctx = new InitialContext();
      return (Topic) ctx.lookup(name);
    } catch (javax.naming.NamingException exc) {
      try {
        String shortName = removePrefix(name);
        Topic topic = Topic.create(serverId,
                                   shortName,
                                   className,
                                   prop);
        topic.setFreeReading();
        topic.setFreeWriting();
        if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.INFO)) 
          AdapterTracing.dbgAdapter.log(BasicLevel.INFO,
                                        "  - Topic [" + shortName + "] has been created.");
        bind(name, topic);
        return topic;
      } catch (ConnectException exc2) {
        throw new AdminException("createTopic() failed: admin connection "
                                 + "has been lost.");
      }
    }
  }

  /**
   * Creates or retrieves a topic destination on the underlying JORAM server,
   * (re)binds the corresponding <code>Topic</code> instance.
   *
   * @param serverId   The identifier of the server where deploying the topic.
   * @param name       The name of the topic.
   *
   * @exception AdminException   If the creation fails.
   */
  public Destination createTopic(int serverId, String name)
    throws AdminException {
    return createTopic(serverId, 
                       name, 
                       "org.objectweb.joram.mom.dest.Topic", 
                       null);
  }

  /**
   * Creates or retrieves a topic destination on the underlying JORAM server,
   * (re)binds the corresponding <code>Topic</code> instance.
   *
   * @param name       The name of the topic.
   *
   * @exception AdminException   If the creation fails.
   */
  public Destination createTopic(String name)
    throws AdminException {
    try {
      return createTopic(joramAdmin.getPlatformAdmin().getLocalServerId(),
                         name,
                         "org.objectweb.joram.mom.dest.Topic",
                         null);
    } catch (ConnectException exc2) {
      throw new AdminException("createTopic() failed: admin connection "
                               + "has been lost.");
    }
  }

  /**
   * Remove a destination on the underlying JORAM server
   *
   * @param name       The name of the destination.
   */
  public void removeDestination(String name) 
    throws AdminException {
    try {
      Context ctx = new InitialContext();
      Destination dest = (Destination) ctx.lookup(name);
      ctx.close();

      if (dest instanceof org.objectweb.joram.client.jms.Destination)
        ((org.objectweb.joram.client.jms.Destination) dest).delete();      
      unbind(name);
    } catch (Exception exc) {
      throw new AdminException("removeDestination(" + name + 
                               ") failed: use Destination.delete()");
    }
  }

  /** Binds an object to the JNDI context. */
  void bind(String name, Object obj) {
    try {
      Context ctx = new InitialContext();
      ctx.rebind(name, obj);
      if (! boundNames.contains(name))
        boundNames.add(name);
    } catch (Exception e) {
      if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.WARN)) 
        AdapterTracing.dbgAdapter.log(BasicLevel.WARN,
                                      "Binding failed:  bind(" + name +"," + obj +")", e);
    }
  }

  /** Unbinds an object from the JNDI context. */
  void unbind(String name) {
    try {
      Context ctx = new InitialContext();
      ctx.unbind(name);
      boundNames.remove(name);
    }
    catch (Exception exc) {}
  }

  /** Deserializing method. */
  private void readObject(java.io.ObjectInputStream in)
          throws java.io.IOException, ClassNotFoundException
  {
    in.defaultReadObject();
    consumers = new Hashtable();
    producers = new Vector();
  }

 
  // ------------------------------------------
  // --- JavaBean setter and getter methods ---
  // ------------------------------------------
  public void setPlatformConfigDir(java.lang.String platformConfigDir)
  {
    this.platformConfigDir = platformConfigDir;
  }

  public void setPersistentPlatform(java.lang.Boolean persistentPlatform)
  {
    this.persistentPlatform = persistentPlatform.booleanValue();
  }

  public void setServerId(java.lang.Short serverId)
  {
    this.serverId = serverId.shortValue();
  }

  public void setServerName(java.lang.String serverName)
  {
    this.serverName = serverName;
  }

  public void setAdminFile(java.lang.String adminFile)
  {
    this.adminFile = adminFile;
  }

  public void setCollocatedServer(java.lang.Boolean collocatedServer)
  {
    collocated = collocatedServer.booleanValue();
  }

  public void setHostName(java.lang.String hostName)
  {
    this.hostName = hostName;
  }

  public void setServerPort(java.lang.Integer serverPort)
  {
    this.serverPort = serverPort.intValue();
  }
  
  public void setConnectingTimer(java.lang.Integer connectingTimer) {
    this.connectingTimer = connectingTimer.intValue();
  }

  public void setTxPendingTimer(java.lang.Integer txPendingTimer) {
    this.txPendingTimer = txPendingTimer.intValue();
  }

  public void setCnxPendingTimer(java.lang.Integer cnxPendingTimer) {
    this.cnxPendingTimer = cnxPendingTimer.intValue();
  }

  public java.lang.String getPlatformConfigDir()
  {
    return platformConfigDir;
  }

  public java.lang.Boolean getPersistentPlatform()
  {
    return new Boolean(persistentPlatform);
  }

  public Short getServerId()
  {
    return new Short(serverId);
  }

  public java.lang.String getServerName()
  {
    return serverName;
  }

  public java.lang.String getAdminFile()
  {
    return adminFile;
  }

  public java.lang.Boolean getCollocatedServer()
  {
    return new Boolean(collocated);
  }

  public java.lang.String getHostName()
  {
    return hostName;
  }

  public java.lang.Integer getServerPort()
  {
    return new Integer(serverPort);
  }

  public java.lang.Integer getConnectingTimer() {
    return new Integer(connectingTimer);
  }

  public java.lang.Integer getTxPendingTimer() {
    return new Integer(txPendingTimer);
  }

  public java.lang.Integer getCnxPendingTimer() {
    return new Integer(cnxPendingTimer);
  }
}
