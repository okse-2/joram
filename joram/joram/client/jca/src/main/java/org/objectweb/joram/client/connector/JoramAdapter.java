/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2011 ScalAgent Distributed Technologies
 * Copyright (C) 2004 - 2006 Bull SA
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
package org.objectweb.joram.client.connector;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.XAConnection;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.CommException;
import javax.resource.spi.IllegalStateException;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.XAResource;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.ConnectionMetaData;
import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.ha.local.HALocalConnectionFactory;
import org.objectweb.joram.client.jms.ha.tcp.HATcpConnectionFactory;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.shared.security.SimpleIdentity;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import com.scalagent.jmx.JMXServer;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 * A <code>JoramAdapter</code> instance manages connectivities to an underlying
 * JORAM server: outbound connectivity (JCA connection management contract) and
 * inbound connectivity (asynchronous message delivery as specified by the JCA
 * message inflow contract).
 */
public final class JoramAdapter implements ResourceAdapter, JoramAdapterMBean, ExceptionListener {
  /** Define serialVersionUID for interoperability. */
  private static final long serialVersionUID = 1L;

  public static Logger logger = Debug.getLogger(JoramAdapter.class.getName());

  /** <code>true</code> if the adapter has been started. */
  private boolean started = false;
  /** <code>true</code> if the adapter has been stopped. */
  private boolean stopped = false;
  
  /** <code>true</code> if admin connection connection is active. */
  private boolean isActive = false;
  /** The duration of admin connection before change state.*/
  private long adminDurationState = 0;

  // ------------------------------------------
  // --- JavaBean setter and getter methods ---
  // ------------------------------------------

  /** <code>true</code> if the underlying JORAM server is collocated. */
  boolean collocated = false;

  public void setCollocatedServer(Boolean collocatedServer) {
    collocated = collocatedServer.booleanValue();
  }

  public Boolean getCollocatedServer() {
    return new Boolean(collocated);
  }

  /** Host name or IP of the underlying JORAM server. */
  String hostName = "localhost";

  public String getHostName() {
    return hostName;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  /** Port number of the underlying JORAM server. */
  int serverPort = 16010;

  public Integer getServerPort() {
    return new Integer(serverPort);
  }

  public void setServerPort(Integer serverPort) {
    this.serverPort = serverPort.intValue();
  }

  /** URL hajoram (for collocated mode). */
  String haURL = null;

  public String getHAURL() {
    return haURL;
  }

  public void setHAURL(String haURL) {
    this.haURL = haURL;
  }

  /** login name for administrator. */
  String rootName = "root";

  public String getRootName() {
    return rootName;
  }

  public void setRootName(String rn) {
    rootName = rn;
  }

  /** password for administrator */
  String rootPasswd = "root";
  
  public String getRootPasswd() { 
    return rootPasswd;
  }

  public void setRootPasswd(String rp) { 
    rootPasswd = rp;
  }
  
  /** Identity class needed for authentication */
  String identityClass = SimpleIdentity.class.getName();

  public String getIdentityClass() {
    return identityClass;
  }

  public void setIdentityClass(String identityClass) { 
    this.identityClass = identityClass;
  }

  /** Identifier of the JORAM server to start. */
  short serverId = 0;

  public Short getServerId() {
    return new Short(serverId);
  }
  
  public void setServerId(Short serverId) {
    this.serverId = serverId.shortValue();
  }

  /** Name of the JORAM server to start. */
  private String serverName = "s0";

  public String getServerName() {
    return serverName;
  }

  public void setServerName(String serverName) {
    this.serverName = serverName;
  }

  /** Identifier of the JORAM replica to start in case of HA. */
  short clusterId = AgentServer.NULL_ID;

  /** <code>true</code> if the underlying a JORAM HA server is defined */
  boolean isHa = false;

  public Short getClusterId() {
    return new Short(clusterId);
  }
  
  public void setClusterId(Short clusterId) {
    this.clusterId = clusterId.shortValue();
    if (this.clusterId != AgentServer.NULL_ID){
      this.isHa = true;
    }
  }

  /**
   * Path to the directory containing JORAM's configuration files
   * (<code>a3servers.xml</code>, <code>a3debug.cfg</code>
   * and admin file), needed when starting the collocated JORAM server.
   */
  private String platformConfigDir;

  public String getPlatformConfigDir() {
    return platformConfigDir;
  }
  
  public void setPlatformConfigDir(String platformConfigDir) {
    this.platformConfigDir = platformConfigDir;
  }

  /** <code>true</code> if the JORAM server to start is persistent. */
  private boolean persistentPlatform = false;

  public Boolean getPersistentPlatform() {
    return new Boolean(persistentPlatform);
  }

  public void setPersistentPlatform(Boolean persistentPlatform) {
    this.persistentPlatform = persistentPlatform.booleanValue();
  }

  /**
   * Path to the XML file containing a description of the administered objects to
   * create and bind.
   */
  private String adminFileXML = "joramAdmin.xml";
  
  /**
   * Returns the path of XML the file containing a description of the administered
   * objects to create and bind at starting.
   */
  public String getAdminFileXML() {
    return adminFileXML;
  }

  /**
   * Sets the path of the XML file containing a description of the administered
   * objects to create and bind at starting.
   */
  public void setAdminFileXML(String adminFileXML) {
    this.adminFileXML = adminFileXML;
  }

  /**
   * Path to the XML file containing a description of the exported administered
   * objects (destination) from the platform.
   */
  private String adminFileExportXML = "joramAdminExport.xml";

  /**
   * Returns the path of XML the file containing a description of the exported
   * administered objects (destination) from the platform.
   */
  public String getAdminFileExportXML() {
    return adminFileExportXML;
  }

  /**
   * Sets the path of the XML file containing a description of the exported
   * administered objects (destination) from the platform.
   */
  public void setAdminFileExportXML(String adminFileExportXML) {
    this.adminFileExportXML = adminFileExportXML;
  }
  
  /**
   * Duration in seconds during which connecting is attempted (connecting
   * might take time if the server is temporarily not reachable); the 0 value
   * is set for connecting only once and aborting if connecting failed.
   */
  public int connectingTimer = 0;

  public Integer getConnectingTimer() {
    return new Integer(connectingTimer);
  }

  public void setConnectingTimer(Integer connectingTimer) {
    this.connectingTimer = connectingTimer.intValue();
  }

  /**
   * Duration in seconds during which a JMS transacted (non XA) session might
   * be pending; above that duration the session is rolled back and closed;
   * the 0 value means "no timer".
   */
  public int txPendingTimer = 0;

  public Integer getTxPendingTimer() {
    return new Integer(txPendingTimer);
  }
  
  public void setTxPendingTimer(Integer txPendingTimer) {
    this.txPendingTimer = txPendingTimer.intValue();
  }

  /**
   * Period in milliseconds between two ping requests sent by the client
   * connection to the server; if the server does not receive any ping
   * request during more than 2 * cnxPendingTimer, the connection is
   * considered as dead and processed as required.
   */
  public int cnxPendingTimer = 0;

  public Integer getCnxPendingTimer() {
    return new Integer(cnxPendingTimer);
  }

  public void setCnxPendingTimer(Integer cnxPendingTimer) {
    this.cnxPendingTimer = cnxPendingTimer.intValue();
  }

  /**
   * The maximum number of messages that can be
   * read at once from a queue.
   *
   * Default value is 2 in order to compensate
   * the former subscription mechanism.
   */
  public int queueMessageReadMax = 2;

  public Integer getQueueMessageReadMax() {
    return new Integer(queueMessageReadMax);
  }

  public void setQueueMessageReadMax(Integer queueMessageReadMax) {
    this.queueMessageReadMax = queueMessageReadMax.intValue();
  }

  /**
   * The maximum number of acknowledgements
   * that can be buffered in
   * Session.DUPS_OK_ACKNOWLEDGE mode when listening to a topic.
   * Default is 0.
   */
  public int topicAckBufferMax = 0;

  public Integer getTopicAckBufferMax() {
    return new Integer(topicAckBufferMax);
  }

  public void setTopicAckBufferMax(Integer topicAckBufferMax) {
    this.topicAckBufferMax = topicAckBufferMax.intValue();
  }

  /**
   * This threshold is the maximum messages
   * number over
   * which the subscription is passivated.
   * Default is Integer.MAX_VALUE.
   */
  public int topicPassivationThreshold = Integer.MAX_VALUE;

  public Integer getTopicPassivationThreshold() {
    return new Integer(topicPassivationThreshold);
  }

  public void setTopicPassivationThreshold(Integer topicPassivationThreshold) {
    this.topicPassivationThreshold = topicPassivationThreshold.intValue();
  }

  /**
   * This threshold is the minimum
   * messages number below which
   * the subscription is activated.
   * Default is 0.
   */
  public int topicActivationThreshold = 0;

  public Integer getTopicActivationThreshold() {
    return new Integer(topicActivationThreshold);
  }

  public void setTopicActivationThreshold(Integer topicActivationThreshold) {
    this.topicActivationThreshold = topicActivationThreshold.intValue();
  }

  /**
   * Determines whether the produced messages are asynchronously
   * sent or not (without or with acknowledgement)
   * Default is false (with ack).
   */
  public boolean asyncSend = false;

  public Boolean getAsyncSend() {
    return new Boolean(asyncSend);
  }

  public void setAsyncSend(Boolean asyncSend) {
    this.asyncSend = asyncSend.booleanValue();
  }

  /**
   * Determines whether client threads
   * which are using the same connection
   * are synchronized in order to group
   * together the requests they send.
   * Default is false.
   */
  public boolean multiThreadSync = false;

  public Boolean getMultiThreadSync() {
    return new Boolean(multiThreadSync);
  }

  public void setMultiThreadSync(Boolean multiThreadSync) {
    this.multiThreadSync = multiThreadSync.booleanValue();
  }

  /**
   * The maximum time the threads hang if 'multiThreadSync' is true.
   * Either they wake up (wait time out) or they are notified (by the
   * first woken up thread).
   * <p>
   * Default is 1 ms.
   */
  public int multiThreadSyncDelay = 1;

  public Integer getMultiThreadSyncDelay() {
    return new Integer(multiThreadSyncDelay);
  }

  public void setMultiThreadSyncDelay(Integer multiThreadSyncDelay) {
    this.multiThreadSyncDelay = multiThreadSyncDelay.intValue();
  }

  /**
   * Determine whether durable subscription must be deleted or not
   * at close time of the InboundConsumer.
   * <p>
   * Default is false.
   */
  public boolean deleteDurableSubscription  = false;

  /**
   * Returns the deleteDurableSubscription attribute.
   * 
   * @return the DeleteDurableSubscription
   * 
   * @see #deleteDurableSubscription
   */
  public Boolean  getDeleteDurableSubscription() {
    return new Boolean(deleteDurableSubscription);
  }

  /**
   * Set the deleteDurableSubscription attribute.
   * 
   * @param flg to set deleteDurableSubscription
   * 
   * @see #deleteDurableSubscription
   */
  public void setDeleteDurableSubscription(Boolean flg) {
    this.deleteDurableSubscription = flg.booleanValue();
  }

  public JMXServer jmxServer;
  
  public void setJmxServer(MBeanServer jmxServer) {
    this.jmxServer = new JMXServer(jmxServer);
  }

  /** Name of the root in the MBean tree */
  private static String jmxRootName = "joramClient";

  /** Names of the bound objects. */
  private static Vector boundNames = new Vector();

  /** <code>WorkManager</code> instance provided by the application server. */
  private transient WorkManager workManager;

  public void setWorkManager(WorkManager workManager) {
    this.workManager = workManager;
  }

  /**
   * Table holding the adapter's <code>InboundConsumer</code> instances,
   * for inbound messaging.
   * <p>
   * <b>Key:</b> <code>ActivationSpec</code> instance<br>
   * <b>Value:</b> <code>InboundConsumer</code> instance
   */
  private transient Hashtable consumers = new Hashtable();

  /**
   * Vector holding the <code>ManagedConnectionImpl</code> instances for
   * managed outbound messaging.
   */
  private transient Vector producers = new Vector();

  /**
   * Table holding the adapter's <code>XAConnection</code> instances used for
   * recovering the XA resources.
   * <p>
   * <b>Key:</b> user name<br>
   * <b>Value:</b> <code>XAConnection</code> instance
   */
  private transient Hashtable connections;

  /**
   * Constructs a <code>JoramAdapter</code> instance.
   */
  public JoramAdapter() {
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "JORAM adapter instantiated.");

    java.util.ArrayList array = MBeanServerFactory.findMBeanServer(null);
    if (!array.isEmpty()) {
      setJmxServer((MBeanServer) array.get(0));
    }
  }

  /**
   * Constructs a <code>JoramAdapter</code> instance.
   */
  public JoramAdapter(MBeanServer jmxServer) {
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "JORAM adapter instantiated.");
    setJmxServer(jmxServer);
  }

  /**
   * Initializes the adapter; starts, if needed, a collocated JORAM server,
   * and if needed again, administers it.
   *
   * @exception ResourceAdapterInternalException  If the adapter could not be
   *                                              initialized.
   */
  public synchronized void start(BootstrapContext ctx) throws ResourceAdapterInternalException {
    setWorkManager(ctx.getWorkManager());
    start();
  }
    
  public synchronized void start() throws ResourceAdapterInternalException {
    if (started)
      throw new ResourceAdapterInternalException("Adapter already started.");
    if (stopped)
      throw new ResourceAdapterInternalException("Adapter has been stopped.");

    if (workManager == null) {
      throw new ResourceAdapterInternalException("WorkManager has not been set.");
    }

    // set HA mode if needed
    AdminModule.setHa(isHa);

    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "JORAM adapter starting deployment...");

    // Collocated mode: starting the JORAM server.
    if (collocated) {
      if (logger.isLoggable(BasicLevel.INFO))
        logger.log(BasicLevel.INFO, " - Collocated JORAM server is starting...");

      // TODO (AF): Setting system properties forbids the launching of multiples
      // servers in an OSGi platform.
      if (persistentPlatform) {
        System.setProperty("Transaction", "fr.dyade.aaa.util.NTransaction");
        System.setProperty("NTNoLockFile", "true");
      } else {
        System.setProperty("Transaction", "fr.dyade.aaa.util.NullTransaction");
        System.setProperty("NbMaxAgents", "" + Integer.MAX_VALUE);
      }

      if (platformConfigDir != null) {
        System.setProperty(AgentServer.CFG_DIR_PROPERTY, platformConfigDir);
        System.setProperty(Debug.DEBUG_DIR_PROPERTY, platformConfigDir);
      }

      try {
        AgentServer.init(serverId, serverName, null, clusterId);
        AgentServer.start();
        if (logger.isLoggable(BasicLevel.INFO))
          logger.log(BasicLevel.INFO, "JoramAdapter - Collocated JORAM server has successfully started.");
      } catch (Exception exc) {
        AgentServer.stop();
        AgentServer.reset(true);

        throw new ResourceAdapterInternalException("Could not start collocated JORAM instance: " + exc);
      }
    }

    // Starting an admin session...
    try {
      adminConnect();
      serverId = (short) AdminModule.getLocalServerId();
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, " - JORAM server not administerable: " + exc);
    }

    // Execute the XML script of configuration.
    try {
      if (logger.isLoggable(BasicLevel.INFO))
        logger.log(BasicLevel.INFO, "  - Reading the provided admin file: " + adminFileXML);
      AdminModule.executeXMLAdmin(platformConfigDir, adminFileXML);
    } catch (FileNotFoundException exc) {
      if (logger.isLoggable(BasicLevel.INFO))
        logger.log(BasicLevel.INFO, "JoramAdapter - problem during XML configuration: " + adminFileExportXML);
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "JoramAdapter - problem during XML configuration: " + adminFileExportXML, exc);
    }

    // Execute the XML script corresponding to the export of the configuration.
    try {
      if (logger.isLoggable(BasicLevel.INFO))
        logger.log(BasicLevel.INFO, "  - Reading the provided admin file: " + adminFileExportXML);
      AdminModule.executeXMLAdmin(platformConfigDir, adminFileExportXML);
    } catch (FileNotFoundException exc) {
      if (logger.isLoggable(BasicLevel.INFO))
        logger.log(BasicLevel.INFO, "JoramAdapter - problem during XML configuration: " + adminFileExportXML);
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "JoramAdapter - problem during XML configuration: " + adminFileExportXML, exc);
    }

    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "Server port is " + serverPort);

    started = true;

    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "JORAM adapter " + ConnectionMetaData.providerVersion + " successfully deployed.");
  }

  /**
   * Initiates an admin session.
   *
   * @exception AdminException  If the admin session could not be started.
   */
  void adminConnect() throws AdminException {
    try {
      org.objectweb.joram.client.jms.ConnectionFactory cf;

      if (isHa) {
        if (collocated) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "haURL = " + haURL);
          if (haURL != null) {
            cf = HATcpConnectionFactory.create(haURL);
          } else {
            cf = HALocalConnectionFactory.create();
          }
        } else {
          String urlHa = "hajoram://" + hostName + ":" + serverPort;
          cf = HATcpConnectionFactory.create(urlHa);
        }
      } else {
        if (collocated)
          cf = LocalConnectionFactory.create();
        else
          cf = TcpConnectionFactory.create(hostName, serverPort);
      }

      if (connectingTimer == 0)
      	cf.getParameters().connectingTimer = 60;
      else
      	cf.getParameters().connectingTimer = connectingTimer;

      AdminModule.connect(cf, rootName, rootPasswd, identityClass);
      if (!isActive)
      	adminDurationState = System.currentTimeMillis();
      isActive = true;
      
      // Registering MBeans...
      try {
        jmxServer.registerMBean(this, MXWrapper.objectName(jmxRootName, "type=JoramAdapter"));
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "  - Could not register JoramAdapterMBean", e);
      }
    } catch (ConnectException exc) {
    	if (isActive)
    		adminDurationState = System.currentTimeMillis();
    	isActive = false;
      throw new AdminException("Admin connection can't be established: " + exc.getMessage());
    }
  }

  void adminDisconnect() {
  	// Finishing the admin session.
    AdminModule.disconnect();
    if (isActive)
    	adminDurationState = System.currentTimeMillis();
    isActive = false;
  }
  
  /**
   * Notifies the adapter to terminate the connections it manages, and if
   * needed, to shutdown the collocated JORAM server.
   */
  public synchronized void stop() {
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "JORAM adapter stopping...");

    if (! started || stopped)
      return;

    // Unbinds the bound objects...
    while (! boundNames.isEmpty())
      unbind((String) boundNames.remove(0));

    // Finishing the admin session.
    adminDisconnect();

    try {
      jmxServer.unregisterMBean(MXWrapper.objectName(jmxRootName, "type=JoramAdapter"));
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, "unregisterMBean", e);
    }

    // Closing the outbound connections, if any.
    while (! producers.isEmpty()) {
      try {
        ((ManagedConnectionImpl) producers.remove(0)).destroy();
      } catch (Exception exc) {}
    }

    // Closing the inbound connections, if any.
    for (Enumeration keys = consumers.keys(); keys.hasMoreElements();)
      ((InboundConsumer) consumers.get(keys.nextElement())).close();

    // Browsing the recovery connections, if any.
    if (connections != null) {
      for (Enumeration keys = connections.keys(); keys.hasMoreElements();) {
        try {
          ((XAConnection) connections.get(keys.nextElement())).close();
        } catch (Exception exc) {}
      }
    }

    // If JORAM server is collocated, stopping it.
    if (collocated) {
      try {
        AgentServer.stop();
      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "Error during AgentServer stopping", exc);

      }
    }

    stopped = true;

    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "JORAM adapter successfully stopped.");
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
                                 ActivationSpec spec) throws ResourceException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 this + " endpointActivation(" + endpointFactory + ", " + spec + ")");

    if (! started)
      throw new IllegalStateException("Non started resource adapter.");
    if (stopped)
      throw new IllegalStateException("Stopped resource adapter.");

    if (! (spec instanceof ActivationSpecImpl))
      throw new ResourceException("Provided ActivationSpec instance is not a JORAM activation spec.");
    ActivationSpecImpl specImpl = (ActivationSpecImpl) spec;

    if (! specImpl.getResourceAdapter().equals(this))
      throw new ResourceException("Supplied ActivationSpec instance associated to an other ResourceAdapter.");

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Activating Endpoint on JORAM adapter.");

    boolean durable =
      specImpl.getSubscriptionDurability() != null
      && specImpl.getSubscriptionDurability().equalsIgnoreCase("Durable");

    boolean transacted = false;
    try {
      Class listenerClass = Class.forName("javax.jms.MessageListener");
      Class[] parameters = { Class.forName("javax.jms.Message") };
      Method meth = listenerClass.getMethod("onMessage", parameters);
      transacted = endpointFactory.isDeliveryTransacted(meth);
    } catch (Exception exc) {
      throw new ResourceException("Could not determine transactional context: " + exc);
    }

    int maxWorks = 10;
    try {
      maxWorks = Integer.parseInt(specImpl.getMaxNumberOfWorks());
    } catch (Exception exc) {
      throw new ResourceException("Invalid max number of works instances number: " + exc);
    }

    int maxMessages = 10;
    try {
      maxMessages = Integer.parseInt(specImpl.getMaxMessages());
    } catch (Exception exc) {
      throw new ResourceException("Invalid max messages number: " + exc);
    }

    int ackMode;
    try {
      if (ActivationSpecImpl.AUTO_ACKNOWLEDGE.equals(specImpl.getAcknowledgeMode())) {
        ackMode = Session.AUTO_ACKNOWLEDGE;
      } else if (ActivationSpecImpl.DUPS_OK_ACKNOWLEDGE.equals(specImpl.getAcknowledgeMode())) {
        ackMode = Session.DUPS_OK_ACKNOWLEDGE;
      } else {
        ackMode = Session.AUTO_ACKNOWLEDGE;
      }
    }  catch (Exception exc) {
      throw new ResourceException("Invalid acknowledge mode: " + exc);
    }

    String destType = specImpl.getDestinationType();
    String destName = specImpl.getDestination();

    try {
      Destination dest;
      
      try {
        Context ctx = new InitialContext();
        dest = (Destination) ctx.lookup(destName);
      } catch (javax.naming.NamingException exc) {
        String shortName = removePrefix(destName);
        if ("javax.jms.Queue".equals(destType))
          dest = AdminModule.createQueue(serverId,
                                         shortName,
                                         "org.objectweb.joram.mom.dest.Queue",
                                         null);
        else if ("javax.jms.Topic".equals(destType))
          dest = AdminModule.createTopic(serverId,
                                         shortName,
                                         "org.objectweb.joram.mom.dest.Topic",
                                         null);
        else
          throw new NotSupportedException("Invalid destination type provided as activation parameter: " + destType);
        
        dest.setFreeReading();
        dest.setFreeWriting();

        if (logger.isLoggable(BasicLevel.INFO))
          logger.log(BasicLevel.INFO,
                     "  - Destination [" + shortName + "] has been created.");

        bind(destName, dest);
      }

      if ("javax.jms.Queue".equals(destType)) {
        if (! (dest instanceof javax.jms.Queue))
          throw new NotSupportedException("Existing destination " + destName  + " does not provide correct type.");
      } else if ("javax.jms.Topic".equals(destType)) {
        if (! (dest instanceof javax.jms.Topic))
          throw new NotSupportedException("Existing destination " + destName  + " does not provide correct type.");
      } else
        throw new NotSupportedException("Invalid destination type provided as activation parameter: " + destType);

      String userName = specImpl.getUserName();
      String password = specImpl.getPassword();
      String identityClass = specImpl.getIdentityClass();

      createUser(userName, password, identityClass);

      ConnectionFactory cf = null;

      if (isHa) {
        if (collocated) {
          if (haURL != null) {
            cf = HATcpConnectionFactory.create(haURL);
          } else {
            cf = HALocalConnectionFactory.create();
          }
        } else {
          cf = HATcpConnectionFactory.create("hajoram://" + hostName + ':' + serverPort);
        }
      }  else {
        if (collocated)
          cf = LocalConnectionFactory.create();
        else
          cf = TcpConnectionFactory.create(hostName, serverPort);
      }

      cf.getParameters().connectingTimer = connectingTimer;
      cf.getParameters().cnxPendingTimer = cnxPendingTimer;
      cf.getParameters().txPendingTimer = txPendingTimer;

      if (queueMessageReadMax > 0) {
        cf.getParameters().queueMessageReadMax = queueMessageReadMax;
      }

      if (topicAckBufferMax > 0) {
        cf.getParameters().topicAckBufferMax = topicAckBufferMax;
      }

      if (topicPassivationThreshold > 0) {
        cf.getParameters().topicPassivationThreshold = topicPassivationThreshold;
      }

      if (topicActivationThreshold > 0) {
        cf.getParameters().topicActivationThreshold = topicActivationThreshold;
      }

      // set identity class for this connectionFactory.
      cf.setIdentityClassName(identityClass);

      XAConnection cnx = cf.createXAConnection(userName, password);
      
      // set Exception listener
      cnx.setExceptionListener(this);
      
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, this + " endpointActivation cnx = " + cnx);

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
                            maxWorks,
                            maxMessages,
                            ackMode,
                            deleteDurableSubscription);

      consumers.put(specImpl, consumer);
    } catch (javax.jms.JMSSecurityException exc) {
      throw new SecurityException("Invalid user identification: " + exc);
    } catch (javax.jms.JMSException exc) {
      throw new CommException("Could not connect to the JORAM server: " + exc);
    } catch (ConnectException exc) {
      throw new ResourceException("Problem when handling the JORAM destinations: " + exc);
    } catch (AdminException exc) {
      throw new ResourceException("Problem when handling the JORAM destinations: " + exc);
    }
  }

  public void onException(JMSException exception) {
  	if (logger.isLoggable(BasicLevel.DEBUG))
  		logger.log(BasicLevel.DEBUG, "JoramAdapter: onException " + exception);
  	while (true) {
  		try {
  			if (logger.isLoggable(BasicLevel.WARN))
  				logger.log(BasicLevel.WARN, "JoramAdapter: try to reconnect...");
  			reconnect();
  			if (logger.isLoggable(BasicLevel.WARN))
  				logger.log(BasicLevel.WARN, "JoramAdapter: reconnected.");
  			break;
  		} catch (Exception e) {
  			continue;
  		}
  	}
  }
  
  public synchronized void reconnect() throws Exception {
  	if (logger.isLoggable(BasicLevel.DEBUG))
  		logger.log(BasicLevel.DEBUG, "JoramAdapter: reconnect()");
  	boolean connected = false;
  	if (!started || stopped)
  		return;

  	try {
  		AdminModule.getConfiguration();
  		connected = true;
  	} catch (Exception e1) {
  		try {
	    	adminDisconnect();
	    } catch (Exception e) {
	    	if (logger.isLoggable(BasicLevel.DEBUG))
	    		logger.log(BasicLevel.DEBUG, "JoramAdapter: reconnect " + e);
      }
	    try {
	      adminConnect();
      } catch (AdminException e) {
      	if (logger.isLoggable(BasicLevel.DEBUG))
	    		logger.log(BasicLevel.DEBUG, "JoramAdapter: reconnect " + e);
      	throw e;
      }
    }
    
    if (connected)
    	return;
    
    // consumers
    Hashtable copyConsumers = (Hashtable) consumers.clone();
    
  	Set keys = copyConsumers.entrySet();
  	Iterator it = keys.iterator();
  	while (it.hasNext()) {
  		Map.Entry entry = (Map.Entry) it.next();
	    
	    MessageEndpointFactory endpointFactory = ((InboundConsumer)entry.getValue()).endpointFactory;
      ActivationSpec spec = (ActivationSpec) entry.getKey();
      try {
      	endpointDeactivation(endpointFactory, spec);
	      endpointActivation(endpointFactory, spec);
      } catch (ResourceException e) {
      	if (logger.isLoggable(BasicLevel.INFO))
	    		logger.log(BasicLevel.INFO, "JoramAdapter: reconnect spec = " + spec, e);
      }
    }
  	
  	// producers
  	it = ((Vector) producers.clone()).iterator();
  	while (it.hasNext()) {
  		ManagedConnectionImpl mci = (ManagedConnectionImpl) it.next();
  		mci.reconnect();
  	}
  }
  
  /**
   * Notifies the adapter to deactivate message delivery for a given endpoint.
   */
  public void endpointDeactivation(MessageEndpointFactory endpointFactory,
                                   ActivationSpec spec) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 this + " endpointDeactivation(" + endpointFactory + ", " + spec + ")");
    if (! started || stopped)
      return;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
      "Deactivating Endpoint on JORAM adapter.");

    InboundConsumer consumer = (InboundConsumer) consumers.remove(spec);
    if (consumer != null) {
    	consumer.close();
    }
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
  public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 this + " getXAResources(" + specs + ")");

    if (! started)
      throw new IllegalStateException("Non started resource adapter.");
    if (stopped)
      throw new IllegalStateException("Stopped resource adapter.");

    ActivationSpecImpl specImpl;
    String userName;
    ConnectionFactory cf = null;
    XAConnection connection;
    Vector resources = new Vector();

    if (connections == null)
      connections = new Hashtable();

    try {
      for (int i = 0; i < specs.length; i++) {
        if (! (specs[i] instanceof ActivationSpecImpl))
          throw new ResourceException("Provided ActivationSpec instance is not a JORAM activation spec.");

        specImpl = (ActivationSpecImpl) specs[i];

        if (! specImpl.getResourceAdapter().equals(this))
          throw new ResourceException("Supplied ActivationSpec instance associated to an other ResourceAdapter.");

        userName = specImpl.getUserName();

        // The connection does not already exist: creating it.
        if (! connections.containsKey(userName)) {
          String password = specImpl.getPassword();
          String identityClass = specImpl.getIdentityClass();

          if (isHa) {
            if (collocated) {
              if (logger.isLoggable(BasicLevel.DEBUG))
                logger.log(BasicLevel.DEBUG, "haURL = " + haURL);
              if (haURL != null) {
                cf = HATcpConnectionFactory.create(haURL);
              } else {
                cf = HALocalConnectionFactory.create();
              }
            } else {
              String urlHa = "hajoram://" + hostName + ":" + serverPort;
              cf = HATcpConnectionFactory.create(urlHa);
            }
          }  else {
            if (collocated)
              cf = LocalConnectionFactory.create();
            else
              cf = TcpConnectionFactory.create(hostName, serverPort);
          }

          cf.getParameters().connectingTimer = connectingTimer;
          cf.getParameters().cnxPendingTimer = cnxPendingTimer;
          cf.getParameters().txPendingTimer = txPendingTimer;

          // set identity class for this connectionFactory.
          cf.setIdentityClassName(identityClass);

          connection = cf.createXAConnection(userName, password);

          connections.put(userName, connection);

          resources.add(connection.createXASession().getXAResource());
        }
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG,
                     this + " getXAResources resources = " + resources);
      }
    } catch (javax.jms.JMSSecurityException exc) {
      throw new SecurityException("Invalid user identification: " + exc);
    } catch (javax.jms.JMSException exc) {
      throw new CommException("Could not connect to the JORAM server: " + exc);
    }

    return (XAResource[]) resources.toArray(new XAResource[resources.size()]);
  }

  // TODO (AF): Is it really needed?
  /** @deprecated */
  public void exit() {
    adminDisconnect();
  }

  /** Returns a code depending on the adapter properties. */
  public int hashCode() {
    return (collocated + " " + hostName + " " + serverPort).hashCode();
  }

  /** Compares adapters according to their properties. */
  public boolean equals(Object o) {
    if (! (o instanceof JoramAdapter))
      return false;

    JoramAdapter other = (JoramAdapter) o;

    boolean res =
      collocated == other.collocated
      && hostName.equals(other.hostName)
      && serverPort == other.serverPort;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 this + " equals = " + res);
    return res;
  }

  /** Adds a given managed connection to the list of producers. */
  void addProducer(ManagedConnectionImpl managedCx) {
    producers.add(managedCx);
  }

  /** Removes a given managed connection from the list of producers. */
  void removeProducer(ManagedConnectionImpl managedCx) {
    producers.remove(managedCx);
  }

  // Implementation of MBean's interfaces

  /**
	  * return the activity of the Joram administration connection.
	  * @return true if connection is active.
	  */
	public boolean isActive() {
		return isActive;
	}
	
	/**
	 * get duration of admin connection before change state.
	 * @return the duration of admin change connection state.
	 */
	public long getAdminDurationBeforeChangeState() {
		return adminDurationState;
	}
	
	/**
	 * get duration of admin connection before change state.
	 * @return the duration of admin connection before change state.
	 */
	public String getAdminDurationBeforeChangeStateDate() {
		return new Date(adminDurationState).toString();
	}
  
  /**
   * Gets the JMS API version.
   * 
   * @return The JMS API version.
   */
  public String getJMSVersion() {
    return ConnectionMetaData.jmsVersion;
  }
  
  /**
   * Get the provider name: Joram.
   * 
   * @return The provider name: Joram.
   */
  public String getJMSProviderName() {
    return ConnectionMetaData.providerName;
  }
  
  /**
   * Gets the Joram's implementation version.
   * 
   * @return The Joram's implementation version.
   */
  public String getProviderVersion() {
    return ConnectionMetaData.providerVersion;
  }


  /**
   * Gets timeout before abort a request.
   * 
   * @return timeout before abort a request.
   * @throws ConnectException 
   * 
   * @see AdminModule#getTimeOutToAbortRequest()
   */
  public long getTimeOutToAbortRequest() throws ConnectException {
    return AdminModule.getTimeOutToAbortRequest();
  }

  /**
   * Sets timeout before abort a request.
   * 
   * @param timeOut timeout before abort a request.
   * @throws ConnectException 
   * 
   * @see AdminModule#setTimeOutToAbortRequest(long)
   */
  public void setTimeOutToAbortRequest(long timeOut) throws ConnectException {
    AdminModule.setTimeOutToAbortRequest(timeOut);
  }
 
  /**
   * Returns the unique identifier of the default dead message queue for the local
   * server, null if not set.
   *
   * @return The unique identifier of the dead message queue of the local server or null
   *         if none exists.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   * 
   * @see AdminModule#getDefaultDMQId()
   */
  public String getDefaultDMQId() throws ConnectException, AdminException {
    return AdminModule.getDefaultDMQId();
  }
  
  /**
   * Returns the unique identifier of the default dead message queue for a given
   * server, null if not set.
   * <p>
   * The request fails if the target server does not belong to the platform.
   * 
   * @param serverId Unique identifier of the server.
   * @return The unique identifier of the dead message queue of the given server or null
   *         if none exists.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   * 
   * @see AdminModule#getDefaultDMQId()
   */
  public String getDefaultDMQId(short serverId) throws ConnectException, AdminException {
    return AdminModule.getDefaultDMQId(serverId);
  }

  /**
   * Unset the default dead message queue for the local server.
   * 
   * @throws ConnectException
   * @throws AdminException
   */
  public void resetDefaultDMQ() throws ConnectException, AdminException {
    AdminModule.setDefaultDMQ(null);
  }

  /**
   * Unset the default dead message queue for the given server.
   * 
   * @param serverId Unique identifier of the given server.
   * @throws ConnectException
   * @throws AdminException
   */
  public void resetDefaultDMQ(short serverId) throws ConnectException, AdminException {
    AdminModule.setDefaultDMQ(serverId, null);
  }

  
  /**
   * Returns the default threshold of the Joram server.
   * 
   * @return the default threshold of the Joram server.
   * @see AdminModule#getDefaultThreshold()
   */
    public int getDefaultThreshold() throws ConnectException, AdminException {
    return AdminModule.getDefaultThreshold();
  }

  /**
   * Returns the default threshold of the given Joram server.
   * 
   * @param serverId  Unique identifier of the given Joram server.
   * @return the default threshold of the given Joram server.
   * @see AdminModule#getDefaultThreshold(int)
   */
  public int getDefaultThreshold(short serverId) throws ConnectException, AdminException {
    return AdminModule.getDefaultThreshold(serverId);
  }

  /**
   * Sets the default threshold of the Joram server.
   * 
   * @param threshold the default threshold of the Joram server.
   * @see AdminModule#setDefaultThreshold(int)
   */
  public void setDefaultThreshold(int threshold) throws ConnectException, AdminException {
    AdminModule.setDefaultThreshold(threshold);
  }

  /**
   * Sets the default threshold of the given Joram server.
   * 
   * @param serverId  Unique identifier of the given Joram server.
   * @param threshold the default threshold of the given Joram server.
   * @see AdminModule#setDefaultThreshold(int, int)
   */
  public void setDefaultThreshold(short serverId, int threshold) throws ConnectException, AdminException {
    AdminModule.setDefaultThreshold(serverId, threshold);
  }

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
  public String[] getDestinations() throws ConnectException, AdminException {
    return getDestinations((short) AdminModule.getLocalServerId());
  }

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
   */
  public String[] getDestinations(short serverId) throws ConnectException, AdminException {
    Destination[] destinations = AdminModule.getDestinations(serverId);
    String[] names = new String[destinations.length];
    
    for (int i=0; i<destinations.length; i++) {
      names[i] = destinations[i].registerMBean(jmxRootName);
    }
    return names;
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
   * @see #createQueue(short, String, String, Properties)
   */
  public String createQueue(String name) throws AdminException, ConnectException {
    return createQueue(serverId, name, Destination.QUEUE, null);
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
   * 
   * @see #createQueue(short, String, String, Properties)
   */
  public String createQueue(short serverId, String name) throws AdminException, ConnectException {
    return createQueue(serverId, name, Destination.QUEUE, null);
  }

  /**
   * First tries to retrieve a queue destination on the underlying JORAM server first
   * using JNDI then the Joram's internal name service. Finally, if the destination does
   * not exist it is created. Anyway at the end of this method the destination is bound
   * in the JNDI repository.
   *
   * @param serverId   The identifier of the server where deploying the queue.
   * @param name       The name of the queue.
   * @param className  The queue class name.
   * @param prop       The queue properties.
   *
   * @exception AdminException   If the creation fails.
   * @exception ConnectException if the connection is closed or broken
   */
  public String createQueue(short serverId,
                            String name,
                            String className,
                            Properties prop) throws AdminException, ConnectException {
    Queue queue = null;

    try {
      Context ctx = new InitialContext();
      queue = (Queue) ctx.lookup(name);
    } catch (javax.naming.NamingException exc) {
      String shortName = removePrefix(name);
      queue = (Queue) AdminModule.createQueue(serverId, shortName, className, prop);
      queue.setFreeReading();
      queue.setFreeWriting();

      if (logger.isLoggable(BasicLevel.INFO))
        logger.log(BasicLevel.INFO,
                   "  - Queue [" + shortName + "] has been created.");

      bind(name, queue);
    }

    return queue.registerMBean(jmxRootName);
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
   * @see #createTopic(short, String, String, Properties)
   */
  public String createTopic(String name) throws AdminException, ConnectException {
    return createTopic(serverId, name, Destination.TOPIC, null);
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
   * 
   * @see #createTopic(short, String, String, Properties)
   */
  public String createTopic(short serverId, String name) throws AdminException, ConnectException {
    return createTopic(serverId, name, Destination.TOPIC, null);
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
   * @exception ConnectException if the connection is closed or broken
   */
  public String createTopic(short serverId,
                            String name,
                            String className,
                            Properties prop) throws AdminException, ConnectException {
    Topic topic = null;

    try {
      Context ctx = new InitialContext();
      topic = (Topic) ctx.lookup(name);
    } catch (javax.naming.NamingException exc) {
      String shortName = removePrefix(name);
      topic = (Topic) AdminModule.createTopic(serverId, shortName, className, prop);
      topic.setFreeReading();
      topic.setFreeWriting();

      if (logger.isLoggable(BasicLevel.INFO))
        logger.log(BasicLevel.INFO,
                   "  - Topic [" + shortName + "] has been created.");

      bind(name, topic);
    }

    return topic.registerMBean(jmxRootName);
  }

  /**
   * Remove a destination specified by its JNDI name on the underlying
   * JORAM platform.
   *
   * @param name       The JNDI name of the destination.
   */
  public void removeDestination(String name) throws AdminException {
    try {
      Context ctx = new InitialContext();
      Destination dest = (Destination) ctx.lookup(name);
      ctx.close();

      dest.delete();
      unbind(name);
    } catch (Exception exc) {
      logger.log(BasicLevel.WARN,
                 "removeDestination failed: " + name, exc);
      throw new AdminException("removeDestination(" + name + ") failed.");
    }
  }

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
  public String[] getUsers() throws ConnectException, AdminException {
    return getUsers((short) AdminModule.getLocalServerId());
  }

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
  public String[] getUsers(short serverId) throws ConnectException, AdminException {
    User[] users = AdminModule.getUsers(serverId);
    String[] names = new String[users.length];
    
    for (int i=0; i<users.length; i++) {
      names[i] = users[i].registerMBean(jmxRootName);
    }
    return names;
  }

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
  public String createUser(String name,
                           String password) throws AdminException, ConnectException {
    return createUser(name, password, (short) AdminModule.getLocalServerId(), SimpleIdentity.class.getName());
  }

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
  public String createUser(String name,
                           String password,
                           String identityClass) throws AdminException, ConnectException {
    return createUser(name, password, (short) AdminModule.getLocalServerId(), identityClass);
  }

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
  public String createUser(String name,
                           String password,
                           short serverId) throws AdminException, ConnectException {
    return createUser(name, password, serverId, SimpleIdentity.class.getName());
  }

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
  public String createUser(String name,
                         String password,
                         short serverId,
                         String identityClass) throws AdminException, ConnectException {
    User user = AdminModule.createUser(name, password, serverId, identityClass);
    return user.registerMBean(jmxRootName);
  }

  /**
   * Creates a non managed connection factory and binds it to JNDI.
   * 
   * @param name Name of created connection factory.
   */
  public void createCF(String name) {
    ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();

    try {
      mcf.setResourceAdapter(this);
      mcf.setCollocated(new Boolean(false));

      Object factory = mcf.createConnectionFactory();
      bind(name, factory);
      
      if (logger.isLoggable(BasicLevel.INFO))
        logger.log(BasicLevel.INFO,
                   "  - ConnectionFactory [" + name + "] has been created and bound.");
    } catch (Exception exc) {}
  }

  /**
   * Creates a non managed PTP connection factory and binds it to JNDI.
   * 
   * @param name Name of created connection factory.
   */
  public void createQueueCF(String name) {
    ManagedConnectionFactoryImpl mcf = new ManagedQueueConnectionFactoryImpl();

    try {
      mcf.setResourceAdapter(this);
      mcf.setCollocated(new Boolean(false));

      Object factory = mcf.createConnectionFactory();
      bind(name, factory);
      if (logger.isLoggable(BasicLevel.INFO))
        logger.log(BasicLevel.INFO,
                   "  - QueueConnectionFactory [" + name
                   + "] has been created and bound.");
    } catch (Exception exc) {}
  }

  /**
   * Creates a non managed PubSub connection factory and binds it to JNDI.
   * 
   * @param name Name of created connection factory.
   */
  public void createTopicCF(String name) {
    ManagedConnectionFactoryImpl mcf = new ManagedTopicConnectionFactoryImpl();

    try {
      mcf.setResourceAdapter(this);
      mcf.setCollocated(new Boolean(false));

      Object factory = mcf.createConnectionFactory();
      bind(name, factory);
      if (logger.isLoggable(BasicLevel.INFO))
        logger.log(BasicLevel.INFO,
                   "  - TopicConnectionFactory [" + name
                   + "] has been created and bound.");
    } catch (Exception exc) {}
  }

  /** remove prefix name scn:comp/ */
  private static String removePrefix(String name) {
    String PREFIX_NAME = "scn:comp/";
    try {
      if (name.startsWith(PREFIX_NAME))
        return name.substring(PREFIX_NAME.length());
    } catch (Exception e) {}
    return name;
  }

  /** Binds an object to the JNDI context. */
  void bind(String name, Object obj) {
    try {
      Context ctx = new InitialContext();
      ctx.rebind(name, obj);
      if (! boundNames.contains(name))
        boundNames.add(name);
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN,
                   "Binding failed:  bind(" + name +"," + obj +")", e);
    }
  }

  /** Unbinds an object from the JNDI context. */
  void unbind(String name) {
    try {
      Context ctx = new InitialContext();
      ctx.unbind(name);
      boundNames.remove(name);
    } catch (Exception exc) {}
  }

  /**
   * Executes the XML configuration file.
   * 
   * @param path the path for the joramAdmin file
   * @throws AdminException if an error occurs
   */
  public void executeXMLAdmin(String path) throws Exception {
    AdminModule.executeXMLAdmin(path);
  }

  /**
   * Export the repository content to an XML file with default filename.
   * - only the destinations objects are retrieved in this version
   * - xml script format of the admin objects (joramAdmin.xml)
   * 
   * @param exportDir       target directory where the export file will be put
   * @throws AdminException if an error occurs
   */
  public void exportRepositoryToFile(String exportDir) throws AdminException {
    AdminModule.exportRepositoryToFile(exportDir, adminFileExportXML);
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
  public void exportRepositoryToFile(String exportDir,
                                     String exportFilename) throws AdminException {
    AdminModule.exportRepositoryToFile(exportDir, exportFilename);
  }

  /**
   * Returns the list of the platform's servers' identifiers.
   * 
   * @return an array containing the list of the platform's servers' identifiers.
   * @throws AdminException If the request fails.
   * @throws ConnectException If the connection fails.
   */
  public Short[] getServersIds() throws ConnectException, AdminException {
    // TODO (AF): next to 5.2, directly use  AdminModule.getServersIds()
    int[] sids = AdminModule.getWrapper().getServersIds();
    Short serversIds[] = new Short[sids.length];
    for (int i=0; i<sids.length; i++)
      serversIds[i] = new Short((short) sids[i]);
    return serversIds;
  }

  /**
   * Returns the list of the platform's servers' names.
   *
   * @return An array containing the list of server's names.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public final String[] getServersNames() throws ConnectException, AdminException {
    // TODO (AF): next to 5.2, directly use  AdminModule.getServersIds()
    return AdminModule.getWrapper().getServersNames(null);
  }
  
  /**
   * Returns the current servers configuration (a3servers.xml).
   *
   * @return The current servers configuration.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public final String getConfiguration() throws ConnectException, AdminException {
    return AdminModule.getConfiguration();
  }
}
