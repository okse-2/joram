/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2016 ScalAgent Distributed Technologies
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
import java.net.ConnectException;
import java.util.Date;
import java.util.Dictionary;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.management.MBeanServer;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapterInternalException;

import org.objectweb.joram.client.jms.ConnectionMetaData;
import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminItf;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.JoramAdmin;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.mom.proxies.tcp.TcpProxyService;
import org.objectweb.joram.shared.security.SimpleIdentity;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.ServerDesc;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.osgi.Activator;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 * A <code>JoramAdapter</code> instance manages connectivities to an underlying
 * JORAM server: outbound connectivity (JCA connection management contract) and
 * inbound connectivity (asynchronous message delivery as specified by the JCA
 * message inflow contract).
 */
public final class JoramAdapter extends JoramResourceAdapter implements JoramAdapterMBean, ExceptionListener {
  /** Define serialVersionUID for interoperability. */
  private static final long serialVersionUID = 1L;

  public static Logger logger = Debug.getLogger(JoramAdapter.class.getName());
  
  /** <code>true</code> if admin connection connection is active. */
  private boolean isActive = false;
  /** The duration of admin connection before change state.*/
  private long adminDurationState = 0;
  
  private JoramAdmin wrapper = null;
  private ServerDesc serverDesc = null;
  private ServiceRegistration registration;
  
  // ------------------------------------------
  // --- JavaBean setter and getter methods ---
  // ------------------------------------------

  /** <code>true</code> if the underlying JORAM server is collocated. */
  boolean collocated = false;

  public void setCollocated(Boolean collocated) {
    this.collocated = collocated.booleanValue();
  }

  public Boolean getCollocated() {
    return new Boolean(collocated);
  }

  /** true start the JoramServer */
  boolean startJoramServer = false;
  
  public void setStartJoramServer(Boolean startJoramServer) {
  	this.startJoramServer = startJoramServer.booleanValue();
  }

  public Boolean getStartJoramServer() {
    return new Boolean(startJoramServer);
  }
  
  /** Host name or IP of the underlying JORAM server. localhost if collocated. */
  String hostName = "localhost";

  public String getHostName() {
    return hostName;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  /** Port number of the underlying JORAM server. -1 if collocated */
  int serverPort = -1;

  public Integer getServerPort() {
    return new Integer(serverPort);
  }

  public void setServerPort(Integer serverPort) {
    this.serverPort = serverPort.intValue();
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

  /** 
   * the persistence directory of the JORAM server to start,
   * needed when StartJoramServer is set to true. 
   */
  private String storage = "s0";

  public String getStorage() {
    return storage;
  }

  public void setStorage(String storage) {
    this.storage = storage;
  }

  /**
   * Path to the directory containing JORAM's configuration files
   * (<code>a3servers.xml</code>, <code>a3debug.cfg</code>
   * and admin file), needed when StartJoramServer is set to true.
   */
  private String platformConfigDir;

  public String getPlatformConfigDir() {
    return platformConfigDir;
  }
  
  public void setPlatformConfigDir(String platformConfigDir) {
    this.platformConfigDir = platformConfigDir;
  }

  /**
   * Path to the XML file (joramAdmin.xml) containing a description of the administered objects to
   * create and bind.
   */
  private String adminFileXML = null;
  
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
   * Constructs a <code>JoramAdapter</code> instance.
   */
  public JoramAdapter() {
   super();
   if (logger.isLoggable(BasicLevel.INFO))
     logger.log(BasicLevel.INFO, "JORAM adapter instantiated.");
  }

  /**
   * Constructs a <code>JoramAdapter</code> instance.
   */
  public JoramAdapter(MBeanServer jmxServer) {
  	super(jmxServer);
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "JORAM adapter instantiated.");
  }

  private boolean isJoramServerRun() {
  	if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "isJoramServerRun status = " + AgentServer.getStatusInfo());
  	return (AgentServer.getStatus() == AgentServer.Status.STARTING ||
  			AgentServer.getStatus() == AgentServer.Status.STARTED);
  }
  
  private void waitAgentServerStarted() throws ResourceAdapterInternalException {
  	try {
  		if (Activator.context != null) {
  			ServiceTracker serviceTracker = new ServiceTracker(Activator.context, ServerDesc.class.getName(), null);
  			// open the service tracker
  			serviceTracker.open();
  			serverDesc = (ServerDesc) serviceTracker.waitForService(10000);
  			serviceTracker.close();
  		}
  	} catch (Exception e) {
  		if (logger.isLoggable(BasicLevel.DEBUG))
  			logger.log(BasicLevel.DEBUG, "waitAgentServerStarted::" + Activator.context, e);
  	}

  	int i = 0;
  	while (AgentServer.getStatus() != AgentServer.Status.STARTED) {
  		try {
  			if (i == 10) {
  				StringBuffer buff = new StringBuffer();
  				buff.append("\n\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
  				buff.append("\nThe Joram collocated server is not started !");
  				buff.append("\nA Joram running server is required to start the resource adapter."); 
  				buff.append("\nThe Joram server status is : " + AgentServer.getStatusInfo() + "\n");
  				buff.append("\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n\n");
  				if (logger.isLoggable(BasicLevel.ERROR))
  	  			logger.log(BasicLevel.ERROR, buff.toString());
  				throw new ResourceAdapterInternalException(buff.toString());
  			}
  			//TODO set/get timer
  			Thread.sleep(1000);
  			i++;
  		} catch (InterruptedException e) {
  			return;
  		}
  	}
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
   super.start();
   status.value = Status.STARTING;
   
    if (logger.isLoggable(BasicLevel.INFO))
  		logger.log(BasicLevel.INFO, "JORAM adapter:: Start the Joram server : " + startJoramServer);

    String joramPort = null;
    if (startJoramServer) {
    	if (logger.isLoggable(BasicLevel.INFO))
    		logger.log(BasicLevel.INFO, "JORAM adapter starting deployment...");

    	if (isJoramServerRun()) {
    		throw new ResourceAdapterInternalException("Restriction: only one server can run in the VM"); 
    	}
    	
    	// Collocated mode: starting the JORAM server.
    	if (collocated) {
    		if (logger.isLoggable(BasicLevel.INFO))
    			logger.log(BasicLevel.INFO, " - Collocated JORAM server is starting...");

    		// TODO (AF): Setting system properties forbids the launching of multiples
    		// servers in an OSGi platform.
//    		if (persistentPlatform) {
//    			System.setProperty("Transaction", "fr.dyade.aaa.util.NTransaction");
//    			System.setProperty("NTNoLockFile", "true");
//    		} else {
//    			System.setProperty("Transaction", "fr.dyade.aaa.util.NullTransaction");
//    			System.setProperty("NbMaxAgents", "" + Integer.MAX_VALUE);
//    		}

    		if (platformConfigDir != null) {
    			System.setProperty(AgentServer.CFG_DIR_PROPERTY, platformConfigDir);
    			System.setProperty(Debug.DEBUG_DIR_PROPERTY, platformConfigDir);
    		}

    		try {
    			AgentServer.init(serverId, storage, null);
    			AgentServer.start();
    			joramPort = AgentServer.getServiceArgs(AgentServer.getServerId(), TcpProxyService.class.getName());
    			if (serverPort < 0 && joramPort != null && joramPort.length() > 0)
    			  serverPort = new Integer(joramPort).intValue();
    			//TODO
    			//String jndiPort = AgentServer.getServiceArgs(AgentServer.getServerId(), JndiServer.class.getName());
    			
    			if (logger.isLoggable(BasicLevel.INFO))
    				logger.log(BasicLevel.INFO, "JoramAdapter - Collocated JORAM server has successfully started.");
    		} catch (Exception exc) {
    			if (logger.isLoggable(BasicLevel.DEBUG))
      			logger.log(BasicLevel.DEBUG, "EXCEPTION:: AgentServer", exc);
    			AgentServer.stop();
    			AgentServer.reset(true);
    			throw new ResourceAdapterInternalException("Could not start collocated JORAM instance: " + exc);
    		}
    	}
    } else {
    	if (collocated) {
    		waitAgentServerStarted();
    		serverId = AgentServer.getServerId();
    		try {
	        hostName = AgentServer.getHostname(serverId);
	        joramPort = AgentServer.getServiceArgs(serverId, TcpProxyService.class.getName());
	        if (serverPort < 0 && joramPort != null && joramPort.length() > 0)
            serverPort = new Integer(joramPort).intValue();
        } catch (Exception e) { }
    	}
    }

    if (joramPort != null) {
    	if (logger.isLoggable(BasicLevel.INFO))
    		logger.log(BasicLevel.INFO, "JoramAdapter - JORAM server listen on port " + joramPort);
    }
    
    // Starting an admin session...
    try {
      adminConnect();
      serverId = (short) wrapper.getLocalServerId();
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, " - JORAM server not administerable: " + exc);
    }

    if (adminFileXML != null) {
    	// Execute the XML script of configuration.
    	try {
    		if (logger.isLoggable(BasicLevel.INFO))
    			logger.log(BasicLevel.INFO,
    					"JoramAdapter - Reading the provided admin file: " + adminFileXML);
    		AdminModule.executeXMLAdmin(platformConfigDir, adminFileXML);
    	} catch (FileNotFoundException exc) {
    		if (logger.isLoggable(BasicLevel.INFO))
    			logger.log(BasicLevel.INFO,
    					"JoramAdapter - problem during XML configuration: " + adminFileXML + " file not found.");
    	} catch (Exception exc) {
    		if (logger.isLoggable(BasicLevel.ERROR))
    			logger.log(BasicLevel.ERROR,
    					"JoramAdapter - problem during XML configuration: " + adminFileXML, exc);
    	}

    }
    
    if (adminFileExportXML != null) {
    	// Execute the XML script corresponding to the export of the configuration.
    	try {
    		if (logger.isLoggable(BasicLevel.INFO))
    			logger.log(BasicLevel.INFO,
    					"JoramAdapter - Reading the provided admin file: " + adminFileExportXML);
    		AdminModule.executeXMLAdmin(platformConfigDir, adminFileExportXML);
    	} catch (FileNotFoundException exc) {
    		if (logger.isLoggable(BasicLevel.INFO))
    			logger.log(BasicLevel.INFO,
    					"JoramAdapter - problem during XML configuration: " + adminFileExportXML + " file not found.");
    	} catch (Exception exc) {
    		if (logger.isLoggable(BasicLevel.ERROR))
    			logger.log(BasicLevel.ERROR,
    					"JoramAdapter - problem during XML configuration: " + adminFileExportXML, exc);
    	}
    }

    if (collocated) {
    	if (logger.isLoggable(BasicLevel.INFO))
    		logger.log(BasicLevel.INFO, "Collocated server.");
    } else {
    	if (logger.isLoggable(BasicLevel.INFO))
    		logger.log(BasicLevel.INFO, "Server port is " + serverPort);
    }

    status.value = Status.STARTED;

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

      if (collocated)
        cf = LocalConnectionFactory.create();
      else
        cf = TcpConnectionFactory.create(hostName, serverPort);

      if (logger.isLoggable(BasicLevel.DEBUG))
  			logger.log(BasicLevel.DEBUG, "adminConnect: cf = " + cf);
      
      if (connectingTimer == 0)
      	cf.getParameters().connectingTimer = 60;
      else
      	cf.getParameters().connectingTimer = connectingTimer;

      cf.setIdentityClassName(identityClass);
      cf.setCnxJMXBeanBaseName(jmxRootName+"#"+getName());
      Connection cnx = cf.createConnection(rootName, rootPasswd);
      cnx.start();
      wrapper = new JoramAdmin(cnx);
      if (logger.isLoggable(BasicLevel.DEBUG))
  			logger.log(BasicLevel.DEBUG, "adminConnect: wrapper = " + wrapper);

      // register wrapper as a service
      try {
      	if (Activator.context != null) {
      		Properties props = new Properties();
      		props.setProperty("name", getName());
      		props.setProperty("host", hostName);
      		props.setProperty("port", ""+serverPort);
      		props.setProperty("user", rootName);

      		registration =  Activator.context.registerService(
      				AdminItf.class.getName(),
      				wrapper,
      				(Dictionary) props);
      		if (logger.isLoggable(BasicLevel.DEBUG))
      			logger.log(BasicLevel.DEBUG, "Bundle context " + Activator.context + " registerService AdminWrapper " + getName());
      	}
      } catch (Exception e) {
      	 if (logger.isLoggable(BasicLevel.WARN))
      		 logger.log(BasicLevel.WARN, "adminConnect: register wrapper in context " + Activator.context, e);
      }
      
      if (!isActive)
      	adminDurationState = System.currentTimeMillis();
      isActive = true;
      
      // Registering MBeans...
      try {
        MXWrapper.registerMBean(this, getMBeanName());
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "  - Could not register JoramAdapterMBean", e);
      }
    } catch (ConnectException exc) {
    	if (isActive)
    		adminDurationState = System.currentTimeMillis();
    	isActive = false;
      throw new AdminException("Admin connection can't be established: " + exc.getMessage());
    } catch (JMSException e) {
    	if (isActive)
    		adminDurationState = System.currentTimeMillis();
    	isActive = false;
      throw new AdminException("Admin connection can't be established: " + e.getMessage());
		}
  }

  public String getMBeanName() {
    StringBuffer strbuf = new StringBuffer();
    
    //for compatibility with JOnAS 5.2 admin GUI
    if (Boolean.getBoolean("joram.ra.oldMBeanName"))
      strbuf.append("joramClient");
    else
      strbuf.append(jmxRootName).append("#").append(getName());
    
    strbuf.append(':');
    strbuf.append("type=JoramAdapter");
    return strbuf.toString();
  }
  
  void adminDisconnect() {
  	//unregister wrapper as a service
  	if (registration != null)
  		registration.unregister();
  	
  	// Finishing the admin session.
    wrapper.close();
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

  	if (getStatus() != Status.STARTED || getStatus() == Status.STOPPED || 
  			AgentServer.getStatus() != AgentServer.Status.STARTED ||
  			AgentServer.getStatus() != AgentServer.Status.STARTING) {
  	  status.value = Status.STOPPED;
  	  if (logger.isLoggable(BasicLevel.INFO))
        logger.log(BasicLevel.INFO, "JORAM adapter successfully stopped.");
  		return;
  	}

  	super.stop();

  	// Finishing the admin session.
  	adminDisconnect();

  	try {
  		MXWrapper.unregisterMBean(getMBeanName());
  	} catch (Exception e) {
  		if (logger.isLoggable(BasicLevel.DEBUG))
  			logger.log(BasicLevel.DEBUG, "unregisterMBean: " + getMBeanName(), e);
  	}

  	if (startJoramServer) {
  		// If JORAM server is collocated, stopping it.
  		if (collocated) {
  			try {
  				AgentServer.stop();
  			} catch (Exception exc) {
  				if (logger.isLoggable(BasicLevel.WARN))
  					logger.log(BasicLevel.WARN, "Error during AgentServer stopping", exc);

  			}
  		}
  	}

  	status.value = Status.STOPPED;

  	if (logger.isLoggable(BasicLevel.INFO))
  		logger.log(BasicLevel.INFO, "JORAM adapter successfully stopped.");
  }
  
  public void reconnect() throws Exception {
  	if (logger.isLoggable(BasicLevel.DEBUG))
  		logger.log(BasicLevel.DEBUG, "JoramAdapter: reconnect()");
  	
  	boolean connected = false;
  	if (getStatus() != Status.STARTED || getStatus() == Status.STOPPED || getStatus() == Status.STOPPING)
  		return;
  	
  	try {
  		wrapper.getConfiguration();
  		connected = true;
  	} catch (Exception e1) {
  	  if (getStatus() == Status.STOPPING || getStatus() == Status.STOPPED) return;
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
    
    // reconnect producers and consumers.
    super.reconnect();
    
  	if (logger.isLoggable(BasicLevel.DEBUG))
  		logger.log(BasicLevel.DEBUG, "JoramAdapter: is reconnected = " + connected);
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
    	name == other.name
      && collocated == other.collocated
      && hostName.equals(other.hostName)
      && serverPort == other.serverPort;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 this + " equals = " + res);
    return res;
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
   * Gets timeout in ms before abort a request.
   * 
   * @return timeout before abort a request.
   * @throws ConnectException 
   * 
   * @see wrapper#getTimeOutToAbortRequest()
   * @since 5.2.2
   */
  public long getTimeOutToAbortRequest() throws ConnectException {
    return wrapper.getTimeOutToAbortRequest();
  }

  /**
   * Sets timeout in ms before abort a request.
   * 
   * @param timeOut timeout before abort a request.
   * @throws ConnectException 
   * 
   * @see wrapper#setTimeOutToAbortRequest(long)
   * @since 5.2.2
   */
  public void setTimeOutToAbortRequest(long timeOut) throws ConnectException {
    wrapper.setTimeOutToAbortRequest(timeOut);
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
   * @see wrapper#getDefaultDMQId()
   */
  public String getDefaultDMQId() throws ConnectException, AdminException {
    return wrapper.getDefaultDMQId();
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
   * @see wrapper#getDefaultDMQId()
   */
  public String getDefaultDMQId(short serverId) throws ConnectException, AdminException {
    return wrapper.getDefaultDMQId(serverId);
  }

  /**
   * Unset the default dead message queue for the given server.
   * 
   * @param serverId Unique identifier of the given server.
   * @throws ConnectException
   * @throws AdminException
   */
  public void resetDefaultDMQ(short serverId) throws ConnectException, AdminException {
    wrapper.setDefaultDMQ(serverId, null);
  }

  
  /**
   * Returns the default threshold of the Joram server.
   * 
   * @return the default threshold of the Joram server.
   * @see wrapper#getDefaultThreshold()
   */
    public int getDefaultThreshold() throws ConnectException, AdminException {
    return wrapper.getDefaultThreshold();
  }

  /**
   * Returns the default threshold of the given Joram server.
   * 
   * @param serverId  Unique identifier of the given Joram server.
   * @return the default threshold of the given Joram server.
   * @see wrapper#getDefaultThreshold(int)
   */
  public int getDefaultThreshold(short serverId) throws ConnectException, AdminException {
    return wrapper.getDefaultThreshold(serverId);
  }

  /**
   * Sets the default threshold of the Joram server.
   * 
   * @param threshold the default threshold of the Joram server.
   * @see wrapper#setDefaultThreshold(int)
   */
  public void setDefaultThreshold(int threshold) throws ConnectException, AdminException {
    wrapper.setDefaultThreshold(threshold);
  }

  /**
   * Sets the default threshold of the given Joram server.
   * 
   * @param serverId  Unique identifier of the given Joram server.
   * @param threshold the default threshold of the given Joram server.
   * @see wrapper#setDefaultThreshold(int, int)
   */
  public void setDefaultThreshold(short serverId, int threshold) throws ConnectException, AdminException {
    wrapper.setDefaultThreshold(serverId, threshold);
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
    return getDestinations((short) wrapper.getLocalServerId());
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
    Destination[] destinations = wrapper.getDestinations(serverId);
    String[] names = new String[destinations.length];
    
    for (int i=0; i<destinations.length; i++) {
      names[i] = destinations[i].registerMBean(wrapper.getJMXBaseName());
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
  @Override
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
      queue = (Queue) jndiHelper.lookup(name);
    } catch (javax.naming.NamingException exc) {
      String shortName = removePrefix(name);
      queue = (Queue) wrapper.createQueue(serverId, shortName, className, prop);
      queue.setFreeReading();
      queue.setFreeWriting();

      if (logger.isLoggable(BasicLevel.INFO))
        logger.log(BasicLevel.INFO,
                   "  - Queue [" + shortName + "] has been created.");

      bind(name, queue);
    }

    return Destination.getJMXBeanName(wrapper.getJMXBaseName(), queue);
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
  @Override
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
      topic = (Topic) jndiHelper.lookup(name);
    } catch (javax.naming.NamingException exc) {
      String shortName = removePrefix(name);
      topic = (Topic) wrapper.createTopic(serverId, shortName, className, prop);
      topic.setFreeReading();
      topic.setFreeWriting();

      if (logger.isLoggable(BasicLevel.INFO))
        logger.log(BasicLevel.INFO,
                   "  - Topic [" + shortName + "] has been created.");

      bind(name, topic);
    }
    return Destination.getJMXBeanName(wrapper.getJMXBaseName(), topic);
  }

  /**
   * Remove a destination specified by its JNDI name on the underlying
   * JORAM platform.
   *
   * @param name       The JNDI name of the destination.
   */
  public void removeDestination(String name) throws AdminException {
    try {
      Destination dest = (Destination) jndiHelper.lookup(name);
      dest.setWrapper(wrapper);
      try {
        MXWrapper.unregisterMBean(dest.getJMXBeanName(wrapper.getJMXBaseName(), dest));
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "unregisterMBean: " + name, e);
      }
      dest.delete();
      unbind(name);
    } catch (Exception exc) {
      logger.log(BasicLevel.WARN, "removeDestination failed: " + name, exc);
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
    return getUsers((short) wrapper.getLocalServerId());
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
    User[] users = wrapper.getUsers(serverId);
    String[] names = new String[users.length];
    
    for (int i=0; i<users.length; i++) {
      names[i] = users[i].registerMBean(wrapper.getJMXBaseName());
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
    return createUser(name, password, (short) wrapper.getLocalServerId(), SimpleIdentity.class.getName());
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
  @Override
  public String createUser(String name,
                           String password,
                           String identityClass) throws AdminException, ConnectException {
    return createUser(name, password, (short) wrapper.getLocalServerId(), identityClass);
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
  	User user = wrapper.createUser(name, password, serverId, identityClass);
  	return User.getJMXBeanName(wrapper.getJMXBaseName(), user);
  }

  /**
   * Creates a non managed connection factory and binds it to JNDI.
   * 
   * @param name Name of created connection factory.
   */
  public void createCF(String name) {
    ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
    //TODO use wrapper information
    Properties props = new Properties();
    props.setProperty("name", name);
    props.setProperty("HostName", hostName);
    props.setProperty("ServerPort", ""+serverPort);
    props.setProperty("UserName", getRootName());
    props.setProperty("Password", getRootPasswd());
    props.setProperty("IdentityClass", getIdentityClass());
    mcf.setManagedConnectionFactoryConfig(props);
    
    createCF(name, mcf);
  }

  /**
   * Creates a non managed PTP connection factory and binds it to JNDI.
   * 
   * @param name Name of created connection factory.
   */
  public void createQueueCF(String name) {
  	ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
  	//TODO use wrapper information
  	Properties props = new Properties();
  	props.setProperty("name", name);
    props.setProperty("HostName", hostName);
    props.setProperty("ServerPort", ""+serverPort);
    props.setProperty("UserName", "anonymous");
    props.setProperty("Password", "anonymous");
    props.setProperty("IdentityClass", "org.objectweb.joram.shared.security.SimpleIdentity");
    mcf.setManagedConnectionFactoryConfig(props);
    
  	createQueueCF(name, mcf);
  }

  /**
   * Creates a non managed PubSub connection factory and binds it to JNDI.
   * 
   * @param name Name of created connection factory.
   */
  public void createTopicCF(String name) {
  	ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
  	//TODO use wrapper information
  	Properties props = new Properties();
  	props.setProperty("name", name);
    props.setProperty("HostName", hostName);
    props.setProperty("ServerPort", ""+serverPort);
    props.setProperty("UserName", "anonymous");
    props.setProperty("Password", "anonymous");
    props.setProperty("IdentityClass", "org.objectweb.joram.shared.security.SimpleIdentity");
    mcf.setManagedConnectionFactoryConfig(props);
    
  	createTopicCF(name, mcf);
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
    int[] sids = wrapper.getServersIds();
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
    return wrapper.getServersNames(null);
  }
  
  /**
   * Returns the current servers configuration (a3servers.xml).
   *
   * @return The current servers configuration.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public final String getConfiguration() throws ConnectException, AdminException {
    return wrapper.getConfiguration();
  }
  
  /**
   * The admin wrapper.
   * 
   * @return the admin wrapper of this resource adapter.
   */
  public JoramAdmin getWrapper() {
  	return wrapper;
  }
  
  // *****************************************************************************************
  // Code to remove in future

  /**
   * Returns true if the server is colocated.
   * 
   * @see org.objectweb.joram.client.connector.JoramAdapterMBean#getCollocatedServer()
   * @deprecated only needed for compatibility with JOnAS 5.2 administration GUI
   */
  public Boolean getCollocatedServer() {
    return new Boolean(collocated);
  }
  
  /**
   * Returns the server name.
   * 
   * @see org.objectweb.joram.client.connector.JoramAdapterMBean#getServerName()
   * @deprecated only needed for compatibility with JOnAS 5.2 administration GUI
   */
  public String getServerName() {
    return name;
  }
  
  //*****************************************************************************************
}
