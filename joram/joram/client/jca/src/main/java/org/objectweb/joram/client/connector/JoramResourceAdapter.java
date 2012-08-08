/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2012 ScalAgent Distributed Technologies
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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
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
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.client.osgi.JndiHelper;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import com.scalagent.jmx.JMXServer;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 * A <code>JoramResourceAdapter</code> instance manages connectivities to an underlying
 * JORAM server: outbound connectivity (JCA connection management contract) and
 * inbound connectivity (asynchronous message delivery as specified by the JCA
 * message inflow contract).
 */
public class JoramResourceAdapter implements ResourceAdapter, ExceptionListener, Serializable {
  /** Define serialVersionUID for interoperability. */
  private static final long serialVersionUID = 1L;

  public static Logger logger = Debug.getLogger(JoramResourceAdapter.class.getName());

  /** the jndi helper */
  protected JndiHelper jndiHelper = null;
  
  public static class Status {
    public static final int NONE = 0;
    public static final int STARTING = 1;
    public static final int STARTED = 2;
    public static final int STOPPING = 3;
    public static final int STOPPED = 4;

    int value = NONE;

    public static String[] info = {"none",
                                   "starting", "started",
                                   "stopping", "stopped"};
  }

  /** The resource adapter status. */
  protected Status status;

  public int getStatus() {
    return status.value;
  }

  public String getStatusInfo() {
    return Status.info[status.value];
  }
  
  public void setJmxServer(MBeanServer jmxServer) {
    MXWrapper.setMXServer(new JMXServer(jmxServer));
  }

  /** Name of the root in the MBean tree */
  protected static String jmxRootName = "JoramAdapter";

  /** Names of the bound objects. */
  private Vector<String> boundNames;

  /** <code>WorkManager</code> instance provided by the application server. */
  private transient WorkManager workManager;

  public void setWorkManager(WorkManager workManager) {
    this.workManager = workManager;
  }

  /** the Joram resource adapter name */
  protected String name;
  
  /**
   * @return the name
   */
  public String getName() {
  	return name;
  }

	/**
   * @param name the name to set
   */
  public void setName(String name) {
  	this.name = name;
  }
  
  /** the Joram resource adapter jndi name */
  protected String jndiName;
  
  /**
   * @return the jndiName
   */
  public String getJndiName() {
  	return jndiName;
  }

	/**
   * @param jndiName the jndiName to set
   */
  public void setJndiName(String jndiName) {
  	this.jndiName = jndiName;
  }
  
  /**
   * Table holding the adapter's <code>InboundConsumer</code> instances,
   * for inbound messaging.
   * <p>
   * <b>Key:</b> <code>ActivationSpec</code> instance<br>
   * <b>Value:</b> <code>InboundConsumer</code> instance
   */
  private transient Hashtable<ActivationSpecImpl, InboundConsumer> consumers = new Hashtable<ActivationSpecImpl, InboundConsumer>();

  /**
   * Vector holding the <code>ManagedConnectionImpl</code> instances for
   * managed outbound messaging.
   */
  private transient Vector<ManagedConnectionImpl> producers = new Vector<ManagedConnectionImpl>();

  /**
   * Table holding the adapter's <code>XAConnection</code> instances used for
   * recovering the XA resources.
   * <p>
   * <b>Key:</b> user name<br>
   * <b>Value:</b> <code>XAConnection</code> instance
   */
  private transient Hashtable<String, XAConnection> connections;

  /**
   * Constructs a <code>JoramResourceAdapter</code> instance.
   */
  public JoramResourceAdapter() {
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "JORAM adapter instantiated.");

    ArrayList<MBeanServer> array = MBeanServerFactory.findMBeanServer(null);
    if (!array.isEmpty()) {
      setJmxServer((MBeanServer) array.get(0));
    }
    boundNames = new Vector<String>();
    jndiHelper = new JndiHelper();
    
    status = new Status();
  }

  /**
   * Constructs a <code>JoramResourceAdapter</code> instance.
   */
  public JoramResourceAdapter(MBeanServer jmxServer) {
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "JORAM adapter instantiated.");
    setJmxServer(jmxServer);
    boundNames = new Vector<String>();
    jndiHelper = new JndiHelper();
    
    status = new Status();
  }

  /**
   * Initializes the adapter
   *
   * @exception ResourceAdapterInternalException  If the adapter could not be
   *                                              initialized.
   */
  public synchronized void start(BootstrapContext ctx) throws ResourceAdapterInternalException {
    setWorkManager(ctx.getWorkManager());
    start();
  }
    
  public synchronized void start() throws ResourceAdapterInternalException {
    if (getStatus() == Status.STARTED)
      throw new ResourceAdapterInternalException("Adapter already started.");
    if (getStatus() == Status.STOPPED)
      throw new ResourceAdapterInternalException("Adapter has been stopped.");

    if (workManager == null) {
      throw new ResourceAdapterInternalException("WorkManager has not been set.");
    }

    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "JORAM Resource adapter starting...");

    status.value = Status.STARTING;
    
    if (jndiName != null && jndiName.length() > 0) {
    	// bind RessourceAdapter
    	bind(jndiName, this);
    }
    
    status.value = Status.STARTED;

    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "JORAM Resource adapter " + ConnectionMetaData.providerVersion + " successfully deployed.");
  }

  /**
   * Notifies the adapter to terminate the connections it manages, and if
   * needed.
   */
  public synchronized void stop() {
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "JORAM Resource adapter stopping...");

    if (getStatus() != Status.STARTED || getStatus() == Status.STOPPED)
      return;
    
    status.value = Status.STOPPING;

    // Unbinds the bound objects...
    while (! boundNames.isEmpty())
      unbind((String) boundNames.remove(0));

    // Closing the outbound connections, if any.
    while (! producers.isEmpty()) {
      try {
        ((ManagedConnectionImpl) producers.remove(0)).destroy();
      } catch (Exception exc) {}
    }

    // Closing the inbound connections, if any.
    for (Enumeration<ActivationSpecImpl> keys = consumers.keys(); keys.hasMoreElements();)
      ((InboundConsumer) consumers.get(keys.nextElement())).close();

    // Browsing the recovery connections, if any.
    if (connections != null) {
      for (Enumeration<String> keys = connections.keys(); keys.hasMoreElements();) {
        try {
          ((XAConnection) connections.get(keys.nextElement())).close();
        } catch (Exception exc) {}
      }
    }

    status.value = Status.STOPPED;

    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "JORAM Resource adapter successfully stopped.");
  }

  // override in JoramAdapter needed for admin stuff
  public String createUser(String userName, String password, String identityClass) throws Exception {throw new Exception("createUser: not yet available.");} 
  public String createQueue(String name) throws Exception {throw new Exception("createQueue: not yet available.");}
  public String createTopic(String name) throws Exception {throw new Exception("createTopic: not yet available.");}
  
  private ConnectionFactory getConnectionFactory(ActivationSpecImpl specImpl) {
    ConnectionFactory cf = null;
    if (specImpl.getCollocated())
      cf = LocalConnectionFactory.create();
    else
      cf = TcpConnectionFactory.create(specImpl.getHostName(), specImpl.getServerPort());

    cf.getParameters().connectingTimer = specImpl.getConnectingTimer();
    cf.getParameters().cnxPendingTimer = specImpl.getCnxPendingTimer();
    cf.getParameters().txPendingTimer = specImpl.getTxPendingTimer();

    if (specImpl.getQueueMessageReadMax() > 0) {
      cf.getParameters().queueMessageReadMax = specImpl.getQueueMessageReadMax();
    }

    if (specImpl.getTopicAckBufferMax() > 0) {
      cf.getParameters().topicAckBufferMax = specImpl.getTopicAckBufferMax();
    }

    if (specImpl.getTopicPassivationThreshold() > 0) {
      cf.getParameters().topicPassivationThreshold = specImpl.getTopicPassivationThreshold();
    }

    if (specImpl.getTopicActivationThreshold() > 0) {
      cf.getParameters().topicActivationThreshold = specImpl.getTopicActivationThreshold();
    }

    // set identity class for this connectionFactory.
    cf.setIdentityClassName(specImpl.getIdentityClass());
    return cf;
  }
  
  /**
   * Notifies the adapter to setup asynchronous message delivery for an
   * application server endpoint.
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

    if (getStatus() != Status.STARTED)
      throw new IllegalStateException("Non started resource adapter.");
    if (getStatus() == Status.STOPPED)
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
    	Class<?> listenerClass = Class.forName("javax.jms.MessageListener");
    	Class<?>[] parameters = { Class.forName("javax.jms.Message") };
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
      Destination dest = null;
      
      try {
        Context ctx = new InitialContext();
        dest = (Destination) ctx.lookup(destName);
      } catch (javax.naming.NamingException exc) {
      	// create the destination if needed
        if ("javax.jms.Queue".equals(destType))
        	createQueue(destName);
        else if ("javax.jms.Topic".equals(destType))
        	createTopic(destName);
        else
          throw new NotSupportedException("Invalid destination type provided as activation parameter: " + destType);

        try {
        	Context ctx = new InitialContext();
        	dest =(Destination) ctx.lookup(destName);
        } catch (javax.naming.NamingException e) {
        	throw new ResourceException("The destination \"" + destName + "\" not found. " + e.getMessage());
        }
        
        if (logger.isLoggable(BasicLevel.INFO))
          logger.log(BasicLevel.INFO,
                     "  - Destination [" + dest.getName() + "] has been created.");
      }

      if ("javax.jms.Queue".equals(destType)) {
        if (! (dest instanceof javax.jms.Queue))
          throw new NotSupportedException("Existing destination " + destName  + " does not provide correct type.");
      } else if ("javax.jms.Topic".equals(destType)) {
        if (! (dest instanceof javax.jms.Topic))
          throw new NotSupportedException("Existing destination " + destName  + " does not provide correct type.");
      } else
        throw new NotSupportedException("Invalid destination type provided as activation parameter: " + destType);

      // create the user if needed
      String userName = specImpl.getUserName();
      String password = specImpl.getPassword();
      String identityClass = specImpl.getIdentityClass();
      createUser(userName, password, identityClass);

      ConnectionFactory cf = getConnectionFactory(specImpl);
      cf.setCnxJMXBeanBaseName(jmxRootName+"#"+getName());
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
                            specImpl.getDeleteDurableSubscription());

      consumers.put(specImpl, consumer);
    } catch (javax.jms.JMSSecurityException exc) {
      throw new SecurityException("Invalid user identification: " + exc);
    } catch (javax.jms.JMSException exc) {
      throw new CommException("Could not connect to the JORAM server: " + exc);
    } catch (Exception exc) {
    	throw new ResourceException("Problem when handling the JORAM destinations: " + exc);
    }
  }

  public void onException(JMSException exception) {
  	if (logger.isLoggable(BasicLevel.DEBUG))
  		logger.log(BasicLevel.DEBUG, "JoramResourceAdapter: onException " + exception);
  	while (true) {
  		try {
  			if (logger.isLoggable(BasicLevel.WARN))
  				logger.log(BasicLevel.WARN, "JoramResourceAdapter: try to reconnect...");
  			reconnect();
  			if (logger.isLoggable(BasicLevel.WARN))
  				logger.log(BasicLevel.WARN, "JoramResourceAdapter: reconnected.");
  			break;
  		} catch (Exception e) {
  			continue;
  		}
  	}
  }
  
  public synchronized void reconnect() throws Exception {
  	if (logger.isLoggable(BasicLevel.DEBUG))
  		logger.log(BasicLevel.DEBUG, "JoramResourceAdapter: reconnect()");
  	if (getStatus() != Status.STARTED || getStatus() == Status.STOPPED)
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
	    		logger.log(BasicLevel.INFO, "JoramResourceAdapter: reconnect spec = " + spec, e);
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
    if (getStatus() != Status.STARTED || getStatus() == Status.STOPPED)
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

    if (getStatus() != Status.STARTED)
      throw new IllegalStateException("Non started resource adapter.");
    if (getStatus() == Status.STOPPED)
      throw new IllegalStateException("Stopped resource adapter.");

    ActivationSpecImpl specImpl;
    String userName;
    ConnectionFactory cf = null;
    XAConnection connection;
    Vector<XAResource> resources = new Vector<XAResource>();

    if (connections == null)
      connections = new Hashtable<String, XAConnection>();

    try {
      for (int i = 0; i < specs.length; i++) {
        if (! (specs[i] instanceof ActivationSpecImpl))
          throw new ResourceException("Provided ActivationSpec instance is not a JORAM activation spec.");

        specImpl = (ActivationSpecImpl) specs[i];
        
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, this + " getXAResources ActivationSpecImpl = " + specImpl);

        if (! specImpl.getResourceAdapter().equals(this))
          throw new ResourceException("Supplied ActivationSpec instance associated to an other ResourceAdapter.");

        userName = specImpl.getUserName();

        // The connection does not already exist: creating it.
        if (! connections.containsKey(userName)) {
          String password = specImpl.getPassword();

          cf = getConnectionFactory(specImpl);
          cf.setCnxJMXBeanBaseName(jmxRootName+"#"+getName());
          connection = cf.createXAConnection(userName, password);

          connections.put(userName, connection);

          resources.add(connection.createXASession().getXAResource());
        }
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG,
                     this + " getXAResources resources = " + resources);
      }
    } catch (javax.jms.JMSSecurityException exc) {
    	if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "EXCEPTION:: getXAResources ", exc);
      throw new SecurityException("Invalid user identification: " + exc);
    } catch (javax.jms.JMSException exc) {
    	if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "EXCEPTION:: getXAResources ", exc);
      throw new CommException("Could not connect to the JORAM server: " + exc);
    }

    return (XAResource[]) resources.toArray(new XAResource[resources.size()]);
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

  /** Binds an object to the JNDI context. */
  protected void bind(String name, Object obj) {
    try {
      jndiHelper.rebind(name, obj);
      if (! boundNames.contains(name))
        boundNames.add(name);
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN,
                   "Binding failed:  bind(" + name +"," + obj +")", e);
    }
  }

  /** Unbinds an object from the JNDI context. */
  protected void unbind(String name) {
    try {
      jndiHelper.unbind(name);
      boundNames.remove(name);
    } catch (Exception exc) {}
  }


  /**
   * Creates a non managed connection factory and binds it to JNDI.
   * 
   * @param name Name of created connection factory.
   */
  public void createCF(String name, ManagedConnectionFactoryImpl mcf) {
    try {
    	mcf.setName(name);
    	mcf.setResourceAdapter(this);
//      mcf.setCollocated(new Boolean(false));

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
  public void createQueueCF(String name, ManagedConnectionFactoryImpl mcf) {
    try {
    	mcf.setName(name);
    	mcf.setResourceAdapter(this);
//      mcf.setCollocated(new Boolean(false));

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
  public void createTopicCF(String name, ManagedConnectionFactoryImpl mcf) {
    try {
    	mcf.setName(name);
    	mcf.setResourceAdapter(this);
//      mcf.setCollocated(new Boolean(false));

      Object factory = mcf.createConnectionFactory();
      bind(name, factory);
      if (logger.isLoggable(BasicLevel.INFO))
        logger.log(BasicLevel.INFO,
                   "  - TopicConnectionFactory [" + name
                   + "] has been created and bound.");
    } catch (Exception exc) {}
  }

}
