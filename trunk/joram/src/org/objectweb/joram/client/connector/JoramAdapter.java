/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
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
 */
package org.objectweb.joram.client.connector;

import fr.dyade.aaa.agent.AgentServer;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;
import org.objectweb.joram.client.jms.local.QueueLocalConnectionFactory;
import org.objectweb.joram.client.jms.local.TopicLocalConnectionFactory;
import org.objectweb.joram.client.jms.local.XALocalConnectionFactory;
import org.objectweb.joram.client.jms.tcp.QueueTcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TopicTcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.XATcpConnectionFactory;

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
public class JoramAdapter implements javax.resource.spi.ResourceAdapter,
                                     java.io.Serializable
{
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
  /** Registered MBeans. */
  private static Vector mbeans = new Vector();

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

  /**
   * Constructs a <code>JoramAdapter</code> instance. 
   */
  public JoramAdapter()
  {
    AdapterTracing.debugINFO("JORAM adapter instanciated.");

    consumers = new Hashtable();
    producers = new Vector();
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

    AdapterTracing.debugINFO("JORAM adapter starting deployment...");

    workManager = ctx.getWorkManager();

    // Collocated mode: starting the JORAM server. 
    if (collocated) {

      AdapterTracing.debugINFO("  - Collocated JORAM server is starting...");

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
        AdapterTracing.debugINFO("  - Collocated JORAM server has successfully started.");
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
      serverId = (new Integer(AdminModule.getLocalServer())).shortValue();
    }
    catch (Exception exc) {
      AdapterTracing.debugWARN("  - JORAM server not administerable: " + exc);
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

      AdapterTracing.debugINFO("  - Reading the provided admin file: " + file);

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
                  AdapterTracing.debugDEBUG("  - Missing password for user [" + name + "]");
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
          AdapterTracing.debugERROR("  CREATION FAILED: " + exc);
        }
        // JNDI error.
        catch (NamingException exc) {
          AdapterTracing.debugERROR("  BINDING FAILED: " + exc);
        }
      }
    }
    // No destination to deploy.
    catch (java.io.FileNotFoundException fnfe) { 
      AdapterTracing.debugINFO("  - No administration task requested.");
    }

    AdapterTracing.debugINFO("Server port is " + serverPort);

    // Registering MBeans...    
    register(new LocalServer(this));

    try {
      platformServersIds = AdminModule.getServersIds();
      Iterator it = platformServersIds.iterator();
      short id;
      while (it.hasNext()) {
        id = ((Short) it.next()).shortValue();
        if (id != serverId)
          register(new RemoteServer(id));
      }
    }
    catch (Exception exc) {}

    started = true;

    AdapterTracing.debugINFO("JORAM adapter successfully deployed.");
  }

  /**	
   * Notifies the adapter to terminate the connections it manages, and if
   * needed, to shut down the collocated JORAM server.
   */
  public synchronized void stop()
  {
    AdapterTracing.debugINFO("JORAM adapter stopping...");

    if (! started || stopped)
      return;

    // Unregistering the MBeans...
    while (! mbeans.isEmpty())
      unregister(mbeans.remove(0));

    // Unbinds the bound objects...
    while (! boundNames.isEmpty())
      unbind((String) boundNames.remove(0));

    // Finishing the admin session.
    AdminModule.disconnect();

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

    AdapterTracing.debugINFO("JORAM adapter successfully stopped.");
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
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, this + " endpointActivation(" + endpointFactory + 
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

    AdapterTracing.debugDEBUG("Activating Endpoint on JORAM adapter.");

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
    catch (NamingException exc) {
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

    AdapterTracing.debugDEBUG("Deactivating Endpoint on JORAM adapter.");

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

  /**
   * Creates or retrieves a user on the underlying JORAM server.
   *
   * @exception AdminException   If the creation fails.
   */
  void createUser(String name, String password) throws AdminException
  {
    try {
      User.create(name, password);
      AdapterTracing.debugINFO("  - User [" + name + "] has been created.");
    }
    catch (ConnectException exc) {
      throw new AdminException("createUser() failed: admin connection "
                               + "has been lost.");
    }
  }

  /**
   * Creates a non managed connection factory and binds it to JNDI.
   *
   * @exception NamingException  If the binding fails.
   */
  void createCF(String name) throws NamingException
  {
    ManagedConnectionFactoryImpl mcf = 
      new ManagedConnectionFactoryImpl();

    try {
      mcf.setResourceAdapter(this);
      mcf.setCollocated(new Boolean(false));

      Object factory = mcf.createConnectionFactory();
      bind(name, factory);
      AdapterTracing.debugINFO("  - ConnectionFactory [" + name
                               + "] has been created and bound.");
    }
    catch (NamingException exc) {
      throw exc;
    }
    catch (Exception exc) {}
  }

  /**
   * Creates a non managed PTP connection factory and binds it to JNDI.
   *
   * @exception NamingException  If the binding fails.
   */
  void createQCF(String name) throws NamingException
  {
    ManagedConnectionFactoryImpl mcf = 
      new ManagedQueueConnectionFactoryImpl();

    try {
      mcf.setResourceAdapter(this);
      mcf.setCollocated(new Boolean(false));

      Object factory = mcf.createConnectionFactory();
      bind(name, factory);
      AdapterTracing.debugINFO("  - QueueConnectionFactory [" + name
                               + "] has been created and bound.");
    }
    catch (NamingException exc) {
      throw exc;
    }
    catch (Exception exc) {}
  }

  /**
   * Creates a non managed PubSub connection factory and binds it to JNDI.
   *
   * @exception NamingException  If the binding fails.
   */
  void createTCF(String name) throws NamingException
  {
    ManagedConnectionFactoryImpl mcf = 
      new ManagedTopicConnectionFactoryImpl();

    try {
      mcf.setResourceAdapter(this);
      mcf.setCollocated(new Boolean(false));

      Object factory = mcf.createConnectionFactory();
      bind(name, factory);
      AdapterTracing.debugINFO("  - TopicConnectionFactory [" + name
                               + "] has been created and bound.");
    }
    catch (NamingException exc) {
      throw exc;
    }
    catch (Exception exc) {}
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

      AdminModule.connect(factory, "root", "root");
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
   * @exception AdminException   If the creation fails.
   * @exception NamingException  If the binding fails.
   */
  static Destination createQueue(String name)
    throws AdminException, NamingException {
    Context ctx = new InitialContext();
    try {
      return (Queue) ctx.lookup(name);
    } catch (javax.naming.NamingException exc) {
      try {
        String shortName = removePrefix(name);
        Queue queue = Queue.create(shortName); 
        queue.setFreeReading();
        queue.setFreeWriting();
        AdapterTracing.debugINFO("  - Queue [" + shortName + "] has been created.");
        bind(name, queue);
        register(new LocalQueue(queue));
        return queue;
      } catch (ConnectException exc2) {
        throw new AdminException("createQueue() failed: admin connection "
                                 + "has been lost.");
      }
    }
  }

  /**
   * Creates or retrieves a topic destination on the underlying JORAM server,
   * (re)binds the corresponding <code>Topic</code> instance.
   *
   * @exception AdminException   If the creation fails.
   * @exception NamingException  If the binding fails.
   */
  static Destination createTopic(String name)
    throws AdminException, NamingException {
    Context ctx = new InitialContext();
    try {
      return (Topic) ctx.lookup(name);
    } catch (javax.naming.NamingException exc) {
      try {
        String shortName = removePrefix(name);
        Topic topic = Topic.create(shortName);
        topic.setFreeReading();
        topic.setFreeWriting();
        AdapterTracing.debugINFO("  - Topic [" + shortName + "] has been created.");
        bind(name, topic);
        register(new LocalTopic(topic));
        return topic;
      } catch (ConnectException exc2) {
        throw new AdminException("createTopic() failed: admin connection "
                                 + "has been lost.");
      }
    }
  }

  /** Binds an object to the JNDI context. */
  static void bind(String name, Object obj) throws NamingException
  {
    Context ctx = new InitialContext();
    ctx.rebind(name, obj);
    if (! boundNames.contains(name))
      boundNames.add(name);
  }

  /** Unbinds an object from the JNDI context. */
  static void unbind(String name)
  {
    try {
      Context ctx = new InitialContext();
      ctx.unbind(name);
      boundNames.remove(name);
    }
    catch (Exception exc) {}
  }

  /** Registers an MBean to the MBean server. */
  static void register(Object bean)
  {
    try {
      if (mbs == null)
        mbs = (MBeanServer) MBeanServerFactory.findMBeanServer(null).get(0);

      if (mbs == null)
        throw new Exception("Could not retrieve local MBean server.");

      ObjectName name = getObjectName(bean);
      mbs.registerMBean(bean, name);
      mbeans.add(bean);

      // Registering a standard JMSResource MBean.
      if (bean instanceof LocalServer) {
        String defaultDomain = mbs.getDefaultDomain();
        java.util.Set oNames =
          mbs.queryNames(new ObjectName(defaultDomain
                                        + ":j2eeType=J2EEServer,*"), null);
        Iterator it = oNames.iterator();
        if (it.hasNext()) {
          ObjectName oName = (ObjectName) it.next();
          String serverName = oName.getKeyProperty("name");
          jmsResourceON = new ObjectName(defaultDomain
                                         + ":j2eeType=JMSResource,"
                                         + "name=JORAMlocalServer,"
                                         + "J2EEServer="
                                         + serverName);
          ((LocalServer) bean).setObjectName(jmsResourceON.toString());
          mbs.registerMBean(bean, jmsResourceON);
        }
      }
    }
    catch (Exception exc) {
      AdapterTracing.debugWARN("  - Could not register MBean ["
                               + bean.getClass().getName()
                               + "] to MBean server: "
                               + exc);
    }
  }

  /** Unregisters an MBean from the MBean server. */
  static void unregister(Object bean)
  {
    try {
      mbeans.remove(bean);
      ObjectName name = getObjectName(bean);
      mbs.unregisterMBean(name);

      // Unregistering the standard JMSResource MBean.
      if (bean instanceof LocalServer && jmsResourceON != null)
        mbs.unregisterMBean(jmsResourceON);
    }
    catch (Exception exc) {
      AdapterTracing.debugWARN("  - Could not unregister MBean ["
                               + bean.getClass().getName()
                               + "] from MBean server: "
                               + exc);
    }
  }

  /** Constructs an ObjectName for a given MBean. */
  static ObjectName getObjectName(Object bean) throws Exception
  {
    if (bean instanceof LocalServer)
      return new ObjectName("joram:type=JMSlocalServer");
    else if (bean instanceof RemoteServer)
      return new ObjectName("joram:type=JMSremoteServer,id="
                            + ((RemoteServer) bean).getRemoteServerId());
    else if (bean instanceof LocalQueue)
      return new ObjectName("joram:type=JMSqueue,name="
                            + ((LocalQueue) bean).getJndiName());
    else if (bean instanceof LocalTopic)
      return new ObjectName("joram:type=JMStopic,name="
                            + ((LocalTopic) bean).getJndiName());
    else
      throw new Exception("Unknown MBean: " + bean.getClass().getName());
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
