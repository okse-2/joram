/*
 * Copyright (C) 2001 - 2006 ScalAgent Distributed Technologies
 * Copyright (C) 2004 - France Telecom R&D
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 * Initial developer(s): Dyade
 * Contributor(s): ScalAgent Distributed Technologies
 */
package fr.dyade.aaa.agent;

import java.io.*;
import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.objectweb.util.monolog.api.LoggerFactory;

import fr.dyade.aaa.util.*;
import fr.dyade.aaa.agent.conf.*;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 * The <code>AgentServer</code> class manages the global configuration
 * of an agent server. It reads the configuration file, then it creates
 * and configure {@link Engine <code>Engine</code>},
 * {@link Channel <code>Channel</code>},
 * {@link Network <code>Network</code>s}.
 * This class contains the main method for AgentServer, for example to
 * acivate a server you have to run this class with two parameters: the
 * server id. and the path of the root of persistancy. You can also use
 * a specialized main calling methods initi and start.
 * <p><hr>
 * To start the agents server an XML configuration file describing the
 * architecture of the agent platform is needed. By default, this file is 
 * the a3servers.xml file and it should be located inside the running
 * directory where the server is launched. Each server must use the same
 * configuration file.<p>
 * The configuration file contains a <code>config</code> element, that
 * is essentially made up of <code>domain</code>s elements, and servers
 * (<code>server</code>s elements):
 * <ul>
 * <li>Each domain of the configuration is described by an XML element with
 * attributes giving the name and the classname for <code>Network</code>
 * implementation (class {@link SimpleNetwork <code>SimpleNetwork</code>}
 * by default).
 * <li>Each server is described by an XML element with attributes giving
 * the id, the name (optional) and the node (the <code>hostname</code>
 * attribute describes the name or the IP address of this node)
 * <ul>
 * <li>Each persistent server must be part of a domain (at least one), to
 * do this you need to define a <code>network</code> element with attributes
 * giving the domain's name (<code>domain</code> attribute) and the
 * communication port (<code>port</code> attribute).
 * <li>A service can be declared on any of these servers by inserting a
 * <code>service</code> element describing it.
 * </ul>
 * <li>Additonnaly, you can define property for the global configuration
 * or for a particular server, it depends where you define it: in the
 * <code>config</code> element or in a server one.
 * </ul>
 * Each server that is part of two domains is named a "router", be careful,
 * it should have only one route between two domains. If it is not true the
 * configuration failed.
 * <p><hr>
 * A simple example of a3servers.xml follows:
 * <p><blockquote><pre>
 * &lt;?xml version="1.0"?&gt;
 * &lt;!DOCTYPE config SYSTEM "a3config.dtd"&gt;
 * 
 * &lt;config&gt;
 *   &lt;domain name="D1"/&gt;
 *   &lt;domain name="D2" class="fr.dyade.aaa.agent.PoolCnxNetwork"/&gt;
 *
 *   &lt;property name="D2.nbMaxCnx" value="1"\&gt;
 *
 *   &lt;server id="0" name="S0" hostname="acores"&gt;
 *     &lt;network domain="D1" port="16300"/&gt;
 *     &lt;service class=\"fr.dyade.aaa.ns.NameService\" args=\"\"/&gt;
 *     &lt;property name="A3DEBUG_PROXY" value="true"\&gt;
 *   &lt;/server&gt;
 *
 *   &lt;server id="2" name="S2" hostname="bermudes"&gt;
 *     &lt;network domain="D1" port="16310"/&gt;
 *     &lt;network domain="D2" port="16312"/&gt;
 *   &lt;/server&gt;
 * 
 *   &lt;server id="3" name="S3" hostname="baleares"&gt;
 *     &lt;network domain="D2" port="16320"/&gt;
 *   &lt;/server&gt;
 * &lt;/config&gt;
 * </pre></blockquote>
 * <p>
 * This file described a 2 domains configuration D1 and D2, D1 with default
 * network protocol and D2 with the <code>PoolCnxNetwork</code> one, and 4
 * servers:
 * <ul>
 * <li>The first server (id 0 and name "S0") is hosted by acores, it is
 * in domain D1, and listen on port 16300. It defines a service and a
 * property.
 * <li>The second server (id 2) is hosted by bermudes and it is the router
 * between D1 and D2.
 * <li>The last server is a persistent one, it is hosted on baleares and it
 * runs in domain D2.
 * </ul>
 * At the beginning of the file, there is a global property that defines
 * the maximum number of connection handled by each server of domain D2.
 * <hr>
 * @see Engine
 * @see Channel
 * @see Network
 * @see MessageQueue
 * @see fr.dyade.aaa.util.Transaction
 */
public final class AgentServer {
  public final static short NULL_ID = -1;

  public final static String ADMIN_DOMAIN = "D0";
  public final static String ADMIN_SERVER = "s0";

  private static short serverId = NULL_ID;

  private static Logger logmon = null;

  public final static String CFG_DIR_PROPERTY = "fr.dyade.aaa.agent.A3CONF_DIR";
  public final static String DEFAULT_CFG_DIR = null;
  
  public final static String CFG_FILE_PROPERTY = "fr.dyade.aaa.agent.A3CONF_FILE";
  public final static String DEFAULT_CFG_FILE = "a3servers.xml";
  public final static String DEFAULT_SER_CFG_FILE = "a3cmlconfig";
  
  public final static String CFG_NAME_PROPERTY = "fr.dyade.aaa.agent.A3CONF_NAME";
  public final static String DEFAULT_CFG_NAME = "default";
  
  public final static String A3CMLWRP_PROPERTY = "fr.dyade.aaa.agent.A3CMLWrapper";
  public final static String DEFAULT_A3CMLWRP = "fr.dyade.aaa.agent.conf.A3CMLSaxWrapper";

  static ThreadGroup tgroup = null;

  public static ThreadGroup getThreadGroup() {
    return tgroup;
  }

 /**
   * Static reference to the engine. Used in <code>Channel.sendTo</code> to
   * know if the method is called from a react or no.
   * <p><hr>
   * AF: I think we must supress this dependency in order to be able to run
   * multiples engine.
   */
  static Engine engine = null;

  /**
   * Returns the agent server engine.
   */
  public static Engine getEngine() {
    return engine;
  }

  /** Static reference to the transactional monitor. */
  private static Transaction transaction = null;

  /**
   * Returns the agent server transaction context.
   */
  public static Transaction getTransaction() {
    return transaction;
  }

  private static JGroups jgroups = null;
  private static short clusterId = NULL_ID;

  private static ConfigController configController;

  public static ConfigController getConfigController() {
    return configController;
  }

  /**
   * Static references to all messages consumers initialized in this
   * agent server (including <code>Engine</code>).
   */
  private static Hashtable consumers = null;

  static void addConsumer(String domain,
                          MessageConsumer cons) throws Exception {
    if (consumers.containsKey(domain))
      throw new Exception("Consumer for domain " + domain + " already exist");

    consumers.put(domain, cons);

    try {
      MXWrapper.registerMBean(cons,
                              "AgentServer",
                              "server=" + getName() + ",cons=" + cons.getName());
    } catch (Exception exc) {
      logmon.log(BasicLevel.ERROR, getName() + " jmx failed", exc);
    }
  }

  static Enumeration getConsumers() {
    if (consumers == null)
      return null;
    else
      return consumers.elements();
  }

  static MessageConsumer getConsumer(String domain) throws Exception {
    if (! consumers.containsKey(domain))
      throw new Exception("Unknown consumer for domain " + domain);
    return (MessageConsumer) consumers.get(domain);
  }

  static void removeConsumer(String domain) {
    MessageConsumer cons = (MessageConsumer) consumers.remove(domain);
    if (cons != null) cons.stop();

    try {
      MXWrapper.unregisterMBean("AgentServer",
                                "server=" + getName() + ",cons=" + cons.getName());
    } catch (Exception exc) {
      logmon.log(BasicLevel.ERROR, getName() + " jmx failed", exc);
    }
  }

  /** Static reference to the configuration. */
  private static A3CMLConfig a3config = null;

  /**
   *  Set the agent server configuration. Be careful, this method cannot
   * be called after initialization.
   *
   * @param a3config  A3CMLConfig
   * @exception Exception	Server is already initialized.
   */
  public final static void setConfig(A3CMLConfig a3config) throws Exception {
    setConfig(a3config, false);
  }

  final static void setConfig(A3CMLConfig a3config,
                              boolean force) throws Exception {
    if (! force) {
      synchronized(status) {
        if (status.value != Status.INSTALLED)
          throw new Exception("cannot set config, bad status: " + status.value);
      }
    }
    AgentServer.a3config = a3config;
  }
  
  /**
   * Returns the agent server configuration.
   *
   * @return  agent server configuration (A3CMLConfig)
   */
  public final static A3CMLConfig getConfig() throws Exception {
    if (a3config == null) throw new Exception("Server not configured");
    return a3config;
  }

  /**
   *  Gets configuration of agent servers for a domain from the current
   * A3CMLConfig object. This method fills the object graph configuration
   * in a <code>A3CMLConfig</code> object.
   *
   * @param domains  	list of domain's names
   * @return	        a <code>A3CMLConfig</code> object.
   */
  public static A3CMLConfig getAppConfig(String[] domains) throws Exception {
    return getConfig().getDomainConfig(domains);
  }

  public final static short getServerId() {
    return serverId;
  }

  public final static short getClusterId() {
    return clusterId;
  }

  private static String name = null;

  public final static String getName() {
    return name;
  }

  /**
   * Returns the identifier of the agent server which name is specified.
   *
   * @param name the name of the agent server
   * @return the identifier of the agent server
   * @exception Exception if the server name is unknown.
   */
  public static short getServerIdByName(String name) throws Exception {
    return getConfig().getServerIdByName(name);
  }
  
  /**
   * Searches for the property with the specified key in the server property
   * list.
   *
   * @param key	   the hashtable key.
   * @return	   the value with the specified key value.
   */
  public static String getProperty(String key) {
    return System.getProperty(key);
  }

  /**
   * Searches for the property with the specified key in the server property
   * list.
   *
   * @param key	   the hashtable key.
   * @param value  a default value.
   * @return	   the value with the specified key value.
   */
  public static String getProperty(String key, String value) {
    return System.getProperty(key, value);
  }

  /**
   * Determines the integer value of the server property with the
   * specified name.
   *
   * @param key property name.
   * @return 	the Integer value of the property.
   */
  public static Integer getInteger(String key) {
    try {
      return Integer.getInteger(key);
    } catch (Exception exc) {
      return null;
    }
  }

  /**
   * Determines the integer value of the server property with the
   * specified name.
   *
   * @param key property name.
   * @param value  a default value.
   * @return 	the Integer value of the property.
   */
  public static Integer getInteger(String key, int value) {
    try {
      return Integer.getInteger(key, value);
    } catch (Exception exc) {
      return null;
    }
  }

  /** Static description of all known agent servers in ascending order. */
  private static ServersHT servers = null;

  static void addServerDesc(ServerDesc desc) throws Exception {
    if (desc == null) return;
    servers.put(desc);
  }

  static ServerDesc removeServerDesc(short sid) throws Exception {
    return servers.remove(sid);
  }

  public static Enumeration elementsServerDesc() {
    return servers.elements();
  }

  public static Enumeration getServersIds() {
    return servers.keys();
  }

  /**
   * Gets the number of server known on the current server.
   *
   * @return	the number of server.
   */
  final static int getServerNb() {
    return servers.size();
  }

  /**
   * Gets the characteristics of the corresponding server.
   *
   * @param sid	agent server id.
   * @return	the server's descriptor.
   */
  final static ServerDesc getServerDesc(short sid) throws UnknownServerException {
    ServerDesc serverDesc = servers.get(sid);
    if (serverDesc == null)
      throw new UnknownServerException("Unknow server id. #" + sid);
    return serverDesc;
  }

  /**
   * Gets the message consumer for the corresponding server.
   *
   * @param sid	agent server id.
   * @return	the corresponding message consumer.
   */
  final static MessageConsumer getConsumer(short sid) throws UnknownServerException {
    return getServerDesc(sid).domain;
  }

  /**
   * Get the host name of an agent server.
   *
   * @param sid		agent server id
   * @return		server host name as declared in configuration file
   */
  public final static String getHostname(short sid) throws UnknownServerException {
    return getServerDesc(sid).getHostname();
  }

  /**
   * Get the description of all services of the current agent server.
   *
   * @return		server host name as declared in configuration file
   */
  final static ServiceDesc[] getServices() throws UnknownServerException {
    return getServerDesc(getServerId()).services;
  }

  /**
   * Get the argument strings for a particular service.
   * The information provides from the A3 configuration file, so it's
   * only available if this file contains service's informations for all
   * nodes.
   *
   * @see A3CMLConfig#getServiceArgs(short,String)
   *
   * @param sid		agent server id
   * @param classname	the service class name
   * @return		the arguments as declared in configuration file
   * @exception	UnknownServerException
   *	The specified server does not exist.
   * @exception UnknownServiceException
   *	The specified service is not declared on this server. 
   * @exception Exception
   *	Probably there is no configuration defined.
   */
  public final static
  String getServiceArgs(short sid,
			String classname) throws Exception {
    return getConfig().getServiceArgs(sid, classname);
  }

  /**
   * Get the argument strings for a particular service running on a server
   * identified by its host.
   * The information provides from the A3 configuration file, so it's
   * only available if this file contains service's informations for all
   * nodes.
   *
   * @see A3CMLConfig#getServiceArgs(String, String)
   *
   * @param hostname	hostname
   * @param classname	the service class name
   * @return		the arguments as declared in configuration file
   * @exception UnknownServiceException
   *	The specified service is not declared on this server. 
   * @exception Exception
   *	Probably there is no configuration defined.
   */
  public final static
  String getServiceArgs(String hostname,
			String classname) throws Exception {
    return getConfig().getServiceArgsHost(hostname, classname);
  }

  /**
   *  The second step of initialization. It needs the Transaction component
   * be up, then it initializes all <code>AgentServer</code> structures from
   * the <code>A3CMLConfig</code> ones. In particular the servers array is
   * initialized.
   */
  private static void configure() throws Exception {
    A3CMLServer root = getConfig().getServer(serverId, clusterId);
    //Allocates the temporary descriptors hashtable for each server.
    servers = new ServersHT();
    // Initialized the descriptor of current server in order to permit
    // Channel and Engine initialization.
    ServerDesc local = new ServerDesc(root.sid, root.name, root.hostname, -1);
    servers.put(local);

    // Parse configuration in order to fix route related to the
    // current server
    getConfig().configure(root);

    // Creates all the local MessageConsumer.
    createConsumers(root);
    
    for (Enumeration s = getConfig().servers.elements();
         s.hasMoreElements();) {
      A3CMLServer server = (A3CMLServer) s.nextElement();
      if (server.sid == root.sid) continue;

      ServerDesc desc = createServerDesc(server);
      addServerDesc(desc);
    }

    // for clusters
    for (Enumeration c = getConfig().clusters.elements();
         c.hasMoreElements();) {
      A3CMLCluster cluster = (A3CMLCluster) c.nextElement();

      for (Enumeration s = cluster.servers.elements();
           s.hasMoreElements();) {
        A3CMLServer server = (A3CMLServer) s.nextElement();
        if (server.sid == root.sid) continue;

        ServerDesc desc = servers.get(server.sid);
        if (desc == null) {
          desc = createServerDesc(server);
          addServerDesc(desc);
        } else {
          desc.addSockAddr(server.hostname, server.port);
        }
      }
    }

    initServices(root, local);
    local.domain = engine;

//     if (logmon.isLoggable(BasicLevel.DEBUG)) {
//       for (int i=0; i<servers.length; i++) {
//         logmon.log(BasicLevel.DEBUG,
//                    getName() + ", servers[" + i + "]=(" + 
//                    "sid=" + servers[i].sid + 
//                    ", name=" + servers[i].name + 
//                    ", gateway=" + servers[i].gateway + 
//                    ", domain=" + servers[i].domain + ")");
//       }
//     }
    
    return;
  }

  private static void createConsumers(A3CMLServer root) throws Exception {
    consumers = new Hashtable();

    // Creates the local MessageConsumer: the Engine.
    engine = Engine.newInstance();
    addConsumer("local", engine);

    // if JGroups
    if (clusterId > NULL_ID) {
      jgroups = new JGroups();
      if (engine instanceof HAEngine) {
        jgroups.setEngine((HAEngine) engine);
        ((HAEngine) engine).setJGroups(jgroups);
      } else
        logmon.log(BasicLevel.ERROR, getName() + ", createConsumers(" + root + ")\n" +
                   "engine [" + engine + "] is not a HAEngine");
    }

    // Search alls directly accessible domains.
    for (Enumeration n = root.networks.elements();
	 n.hasMoreElements();) {
      A3CMLNetwork network = (A3CMLNetwork) n.nextElement();

      A3CMLDomain domain = getConfig().getDomain(network.domain);
      // Creates the corresponding MessageConsumer.
      try {
        Network consumer = (Network) Class.forName(domain.network).newInstance();
        // Initializes it with domain description. Be careful, this array
        // is kept in consumer, don't reuse it!!
        consumer.init(domain.name, network.port, domain.getServersId());
        if (consumer instanceof SimpleNetwork &&
            jgroups != null) {//NTA modify to SimpleHANetwork
          ((SimpleNetwork) consumer).setJGroups(jgroups);
          jgroups.setNetWork((SimpleNetwork) consumer);
        }
//         domain.consumer = consumer;
        addConsumer(network.domain, consumer);
      } catch (ClassNotFoundException exc) {
        throw exc;
      } catch (InstantiationException exc) {
        throw exc;
      } catch (IllegalAccessException exc) {
        throw exc;
      }
    }
  }

  static void initServerDesc(ServerDesc desc,
                             A3CMLServer server) throws Exception {
    desc.gateway = server.gateway;
    // For each server set the gateway to the real next destination of
    // messages; if the server is directly accessible: itself.
    if ((desc.gateway == -1) || (desc.gateway == server.sid)) {
      desc.gateway = server.sid;
      desc.updateSockAddr(desc.getHostname(), server.port);   
      A3CMLServer current = getConfig().getServer(getServerId(),getClusterId());
      if (current.containsNat(server.sid)) {
        A3CMLNat nat = current.getNat(server.sid);
        desc.updateSockAddr(nat.host, nat.port);
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, getName() + " : NAT sDesc = " + desc);
      }
    }
    desc.domain = getConsumer(server.domain);
  }   

  private static ServerDesc
      createServerDesc(A3CMLServer server) throws Exception {
    if (! server.visited)
      throw new Exception(server + " inaccessible");
    
    ServerDesc desc = new ServerDesc(server.sid, 
                                     server.name, 
                                     server.hostname,
                                     -1);

    initServerDesc(desc, server);
    initServices(server, desc);

    return desc;
  }

  private static void initServices(A3CMLServer server, ServerDesc desc) {
    if (server.services != null) {
      ServiceDesc services[]  = new ServiceDesc[server.services.size()];
      int idx = 0;
      for (Enumeration x = server.services.elements();
           x.hasMoreElements();) {
        A3CMLService service = (A3CMLService) x.nextElement();
        services[idx++] = new ServiceDesc(service.classname, service.args);
      }
      desc.services = services;
    }
  }
  
  private static void setProperties(short sid, short cid) throws Exception {
    if (a3config == null) return;

    // add global properties
    if (a3config.properties != null) {
      for (Enumeration e = a3config.properties.elements(); e.hasMoreElements();) {
        A3CMLProperty p = (A3CMLProperty) e.nextElement();
        System.getProperties().put(p.name,p.value);
      }
    }

    A3CMLServer server = null;
    if (cid != NULL_ID) {
      A3CMLCluster cluster = null;
      cluster = a3config.getCluster(sid);

      // add cluster properties
      if (cluster != null 
          && cluster.properties != null 
          && cluster.properties.size() > 0) {
        Enumeration e = cluster.properties.elements();
        do {
          A3CMLProperty p = (A3CMLProperty) e.nextElement();
          System.getProperties().put(p.name,p.value);
        } while (e.hasMoreElements());
      }
      server = cluster.getServer(cid);
    } else {
      server = a3config.getServer(sid);
    }

    // add server properties
    if (server != null && server.properties != null) {
      Enumeration e = server.properties.elements();
      do {
        A3CMLProperty p = (A3CMLProperty) e.nextElement();
        System.getProperties().put(p.name,p.value);
      } while (e.hasMoreElements());
    }
  }
  
  static class Status {
    public static final int INSTALLED = 0;
    public static final int INITIALIZING = 0x1;
    public static final int INITIALIZED = 0x2;
    public static final int STARTING = 0x3;
    public static final int STARTED = 0x4;
    public static final int STOPPING = 0x5;
    public static final int STOPPED = 0x6;
    public static final int RESETING = 0x7;

    private int value = INSTALLED;

    public static String[] info = {"installed",
                                   "initializing", "initialized",
                                   "starting", "started",
                                   "stopping", "stopped",
                                   "reseting"};
  }

  static Status status = new Status();

  public static int getStatus() {
    return status.value;
  }

  public static String getStatusInfo() {
    return Status.info[status.value];
  }

  /**
   * Parses agent server arguments, then initializes this agent server. The
   * <code>start</code> function is then called to start this agent server
   * execution. Between the <code>init</code> and </code>start</code> calls,
   * agents may be created and deployed, and notifications may be sent using
   * the <code>Channel</code> <code>sendTo</code> function.
   *
   * @param args	lauching arguments, the first one is the server id
   *			and the second one the persistency directory.
   * @return		number of arguments consumed in args
   *
   * @exception Exception
   *	unspecialized exception
   */
  public static int init(String args[]) throws Exception {
    if (args.length < 2)
      throw new Exception("usage: java <main> sid storage");
    short sid = NULL_ID;
    try {
      sid = (short) Integer.parseInt(args[0]);
    } catch (NumberFormatException exc) {
      throw new Exception("usage: java <main> sid storage");
    }
    String path = args[1];
    short cid = NULL_ID;
    try {
      if (args.length == 3)
      cid = (short) Integer.parseInt(args[2]);
    } catch (NumberFormatException exc) {}

    init(sid, path, null, cid);

    configController = new ConfigController();

    return 2;
  }

  public static void reset(boolean force) {
    if (force) {
      synchronized(status) {
        if (status.value != Status.STOPPED) {
          logmon.log(BasicLevel.WARN,
                     getName() + ", force status: " + status.value);
        }
        status.value = Status.STOPPED;
      }
    }
    reset();
  }

  /**
   *  Cleans an AgentServer configuration in order to restart it from
   * persistent storage.
   */
  public static void reset() {
    synchronized(status) {
      if (status.value != Status.STOPPED) {
        logmon.log(BasicLevel.WARN,
                   getName() + ", cannot reset, bad status: " + status.value);
        return;
      }
      status.value = Status.RESETING;
    }

    // Remove all consumers Mbean
    Enumeration e = getConsumers();
    if (e != null) {
      for (; e.hasMoreElements();) {
        MessageConsumer cons = (MessageConsumer) e.nextElement();
        try {
          MXWrapper.unregisterMBean(
            "AgentServer",
            "server=" + getName() + ",cons=" + cons.getName());
        } catch (Exception exc) {
          logmon.log(BasicLevel.DEBUG,
                     getName() + ", jmx failed: " +
                     "server=" + getName() + ",cons=" + cons.getName(), exc);
        }
      }
      consumers = null;
    }

    try {
      MXWrapper.unregisterMBean("AgentServer",
                                "server=" + getName() + ",cons=Transaction");
    } catch (Exception exc) {
      logmon.log(BasicLevel.DEBUG,
                 getName() + ", jmx failed: " +
                 "server=" + getName() + ",cons=Transaction", exc);
    }

    if (transaction != null) transaction.close();
    transaction = null;

    try {
      MXWrapper.unregisterMBean("AgentServer", "server=" + getName());
    } catch (Exception exc) {
      logmon.log(BasicLevel.DEBUG,
                 getName() + " jmx failed: "+ "server=" + getName(), exc);
    }
    
    a3config = null;

    synchronized(status) {
      status.value = Status.INSTALLED;
    }
  }

 /**
   * Initializes this agent server.
   * <code>start</code> function is then called to start this agent server
   * execution. Between the <code>init</code> and </code>start</code> calls,
   * agents may be created and deployed, and notifications may be sent using
   * the <code>Channel</code> <code>sendTo</code> function.
   *
   * @param sid		the server id
   * @param path        the persistency directory.
   * @param loggerFactory the monolog LoggerFactory;
   * 	
   *
   * @exception Exception
   *	unspecialized exception
   */
  public static void init(short sid,
                          String path,
                          LoggerFactory loggerFactory) throws Exception {
    init(sid, path, loggerFactory, NULL_ID);
  }

 /**
   * Initializes this agent server.
   * <code>start</code> function is then called to start this agent server
   * execution. Between the <code>init</code> and </code>start</code> calls,
   * agents may be created and deployed, and notifications may be sent using
   * the <code>Channel</code> <code>sendTo</code> function.
   *
   * @param sid		the server id
   * @param path        the persistency directory.
   * @param loggerFactory the monolog LoggerFactory;
   * @param cid		the cluster id
   * 	
   *
   * @exception Exception
   *	unspecialized exception
   */
  public static void init(short sid,
                          String path,
                          LoggerFactory loggerFactory,
                          short cid) throws Exception {
    name = new StringBuffer("AgentServer#").append(sid).toString();

    if (loggerFactory != null) Debug.setLoggerFactory(loggerFactory);
    logmon = Debug.getLogger(Debug.A3Debug + ".AgentServer.#" + sid);

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, getName() + ", init()", new Exception());
    else
      logmon.log(BasicLevel.WARN, getName() + ", init()");

    synchronized(status) {
      if (status.value == Status.STOPPED) {
        logmon.log(BasicLevel.DEBUG, getName() + ", reset configuration");
        reset();
      }
      if (status.value != Status.INSTALLED)
        throw new Exception("cannot initialize, bad status: " + status.value);
      status.value = Status.INITIALIZING;
    }

    try {
      serverId = sid; 

      tgroup = new ThreadGroup(getName()) {
          public void uncaughtException(Thread t, Throwable e) {
            if (logmon.isLoggable(BasicLevel.WARN)) {
              logmon.log(BasicLevel.WARN,
                         "Abnormal termination for " +
                         t.getThreadGroup().getName() + "." + t.getName(),
                         e);
            }
          }
        };
   
      //  Try to get transaction type from disk, then initialize the rigth
      // transaction manager and get the configuration.
      File dir = new File(path);
      if (dir.exists() && dir.isDirectory()) {
        File tfc = new File(dir, "TFC");
        if (tfc.exists()) {
          DataInputStream dis = null;
          try {
            dis = new DataInputStream(new FileInputStream(tfc));
            String tname = dis.readUTF();
            Class tclass = Class.forName(tname);
            transaction = (Transaction) tclass.newInstance();
          } catch (Exception exc) {
            logmon.log(BasicLevel.FATAL,
                       getName() + ", can't instanciate transaction manager",
                       exc);
            throw new Exception("Can't instanciate transaction manager");
          } finally {
            if (dis != null) dis.close();
          }
          try {
            transaction.init(path);
          } catch (IOException exc) {
            logmon.log(BasicLevel.FATAL,
                       getName() + ", can't start transaction manager", exc);
            throw new Exception("Can't start transaction manager");
          }
        }
      }

      // Gets static configuration of agent servers from a file. This method
      // fills the object graph configuration in the <code>A3CMLConfig</code>
      // object, then the configure method really initializes the server.
      // There are two steps because the configuration step needs the
      // transaction components to be initialized.
      if (transaction != null) {
        // Try to read the serialiazed configuration (trough transaction)
        try {
          a3config = A3CMLConfig.load();
        } catch (Exception exc) {
          logmon.log(BasicLevel.WARN, getName() + ", config not found");
        }
      }

      if (a3config == null) {
        //  Try to load an initial configuration (serialized or XML), or
        // generates a default one in case of failure.
        try {
          a3config = A3CMLConfig.getConfig(DEFAULT_SER_CFG_FILE);
        } catch (Exception exc) {
          logmon.log(BasicLevel.WARN,
                     getName() + ", serialized a3cmlconfig not found");
        }

        if (a3config == null) {
          // Try to found XML configuration file, then parse it.
          try {
            a3config = A3CML.getXMLConfig();
          } catch (Exception exc) {
            logmon.log(BasicLevel.WARN,
                       getName() + ", XML configuration file not found");
          }
        }

        if (a3config == null) {
          // 3rd, Generate A3CMLConfig base.
          logmon.log(BasicLevel.WARN,
                     "Generate default configuration");
          A3CMLDomain d = new A3CMLDomain(ADMIN_DOMAIN,
                                          "fr.dyade.aaa.agent.SimpleNetwork");
          A3CMLServer s = new A3CMLServer((short) 0, ADMIN_SERVER, "localhost");
          s.networks.addElement(new A3CMLNetwork(ADMIN_DOMAIN, 27300));
          s.services.addElement(new A3CMLService("fr.dyade.aaa.agent.AgentAdmin",null));
          s.services.addElement(new A3CMLService("fr.dyade.aaa.agent.HttpDebug","20080"));
          d.addServer(s);
          a3config = new A3CMLConfig();
          a3config.addDomain(d);
          a3config.addServer(s);
        }
      }

      // if JGroups
      if (cid > NULL_ID) clusterId = cid;

      // set properties
      setProperties(serverId, clusterId);

      // Initializes the JMX Wrapper
      MXWrapper.init();

      if (transaction == null) {
        try {
          String tname = getProperty("Transaction",
                                     "fr.dyade.aaa.util.NTransaction");
          Class tclass = Class.forName(tname);
          transaction = (Transaction) Class.forName(tname).newInstance();
        } catch (Exception exc) {
          logmon.log(BasicLevel.FATAL,
                     getName() + ", can't instanciate transaction manager", exc);
          throw new Exception("Can't instanciate transaction manager");
        }

        try {
          transaction.init(path);
        } catch (IOException exc) {
          logmon.log(BasicLevel.FATAL,
                     getName() + ", can't start transaction manager", exc);
          throw new Exception("Can't start transaction manager");
        }
      }

      try {
        MXWrapper.registerMBean(transaction,
                                "AgentServer",
                                "server=" + getName() + ",cons=Transaction");
      } catch (Exception exc) {
        if (logmon == null)
          logmon = Debug.getLogger(Debug.A3Debug + ".AgentServer");
        logmon.log(BasicLevel.ERROR, getName() + " jmx failed", exc);
      }

      // save A3CMLConfig (May be we can omit it in some case).
      a3config.save();

      try {
        // Initialize AgentId class's variables.
        AgentId.init();
      } catch (ClassNotFoundException exc) {
        logmon.log(BasicLevel.FATAL,
                   getName() + ", can't initialize AgentId", exc);
        throw new Exception("Can't initialize AgentId, bad classpath");
      } catch (IOException exc) {
        logmon.log(BasicLevel.FATAL,
                   getName() + ", can't initialize AgentId", exc);
        throw new Exception("Can't initialize AgentId, storage problems");
      }

      try {
        // Configure the agent server.
        configure();
      } catch (Exception exc) {
        logmon.log(BasicLevel.FATAL, getName() + ", can't configure", exc);
        throw new Exception("Can't configure server");
      }

      try {
        // then restores all messages.
        String[] list = transaction.getList("@");
        for (int i=0; i<list.length; i++) {
          Message msg = Message.load(list[i]);

          if (msg.getSource() == serverId) {
            // The update has been locally generated, the message is ready to
            // deliver to its consumer (Engine or Network component). So we have
            // to insert it in the queue of this consumer.
            try {
              getServerDesc(msg.getDest()).domain.insert(msg);
            } catch (UnknownServerException exc) {
              logmon.log(BasicLevel.ERROR,
                         getName() + ", discard message to unknown server id#" +
                         msg.getDest());
              msg.delete();
              msg.free();
              continue;
            } catch (NullPointerException exc) {
              logmon.log(BasicLevel.ERROR,
                         getName() + ", discard message to unknown server id#" +
                         msg.getDest());
              msg.delete();
              msg.free();
              continue;
            } catch (ArrayIndexOutOfBoundsException exc) {
              logmon.log(BasicLevel.ERROR,
                         getName() + ", discard message to unknown server id#" +
                         msg.getDest());
              msg.delete();
              msg.free();
              continue;
            }
          } else {
            logmon.log(BasicLevel.ERROR,
                       getName() + ", discard undelivered message from server id#" +
                       msg.getDest());
            msg.delete();
            continue;
          }
        }
      } catch (ClassNotFoundException exc) {
        logmon.log(BasicLevel.FATAL,
                   getName() + ", can't restore messages", exc);
        throw new Exception("Can't restore messages, bad classpath");
      } catch (IOException exc) {
        logmon.log(BasicLevel.FATAL,
                   getName() + ", can't restore messages", exc);
        throw new Exception("Can't restore messages, storage problems");
      }

      // initializes channel before initializing fixed agents
      Channel.newInstance();    

      try {
        //  Initialize services.
        ServiceManager.init();

        logmon.log(BasicLevel.DEBUG,
                   getName() + ", ServiceManager initialized");

        /* Actually get Services from A3CML configuration file. */
        ServiceDesc services[] = AgentServer.getServices();
        if (services != null) {
          for (int i = 0; i < services.length; i ++) {
            ServiceManager.register(services[i].getClassName(),
                                    services[i].getArguments());
          }
        }
        ServiceManager.save();
      } catch (Exception exc) {
        logmon.log(BasicLevel.FATAL,
                   getName() + ", can't initialize services", exc);
        throw new Exception("Can't initialize services");
      }

      // initializes fixed agents
      engine.init();

      // If the server is part of an HA group starts the JGroup component
      if (jgroups != null) jgroups.init(getName());

      logmon.log(BasicLevel.WARN,
                 getName() + ", initialized at " + new Date());

      // Commit all changes.
      transaction.begin();
      transaction.commit();
      transaction.release();

      try {
        SCServerMBean bean = new SCServer();
        MXWrapper.registerMBean(bean, "AgentServer", "server=" + getName());
      } catch (Exception exc) {
        if (logmon == null)
          logmon = Debug.getLogger(Debug.A3Debug + ".AgentServer");
        logmon.log(BasicLevel.ERROR, getName() + " jmx failed", exc);
      }
    } catch (Exception exc) {
      logmon.log(BasicLevel.ERROR, getName() + "Cannot initialize", exc);
      synchronized(status) {
        // AF: Will be replaced by a BAD_INITIALIZED status allowing the
        // re-initialization..
        status.value = Status.INSTALLED;
      }
      throw exc;
    } catch (Throwable t) {
      logmon.log(BasicLevel.ERROR, getName() + "Cannot initialize", t);
      synchronized(status) {
        // AF: Will be replaced by a BAD_INITIALIZED status allowing the
        // re-initialization..
        status.value = Status.INSTALLED;
      }
      throw new Exception(t);
    }

    synchronized(status) {
      status.value = Status.INITIALIZED;
    }
  }

  static String startConsumers() throws Exception {
    StringBuffer errBuf = null;

    // Now we can start all networks.
    if (consumers != null) {
      for (Enumeration c=AgentServer.getConsumers(); c.hasMoreElements(); ) {
        MessageConsumer cons = (MessageConsumer) c.nextElement();
        if (cons != null) {
          try {
            if (! (cons instanceof Engine)) {
              cons.start();
            }
          } catch (IOException exc) {
            if (errBuf == null) errBuf = new StringBuffer();
            errBuf.append(cons.getName()).append(": ");
            errBuf.append(exc.getMessage()).append('\n');
            logmon.log(BasicLevel.FATAL,
                       getName() +
                       ", problem during " + cons.getName() + " starting", exc);
          }
        }
      }
    }

    if (errBuf == null) return null;
    return errBuf.toString();
  }

  /**
   *  Causes this AgentServer to begin its execution. This method starts all
   * <code>MessageConsumer</code> (i.e. the engine and the network components).
   */
  public static String start() throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, getName() + ", start()", new Exception());
    else
      logmon.log(BasicLevel.WARN, getName() + ", start()");

    synchronized(status) {
      if ((status.value != Status.INITIALIZED) &&
          (status.value != Status.STOPPED))
        throw new Exception("cannot start, bad status: " + status.value);
      status.value = Status.STARTING;
    }

    StringBuffer errBuf = null;
    try {
      try {
        if (jgroups == null) {
          ServiceManager.start();
          // Be careful, we have to save ServiceManager after start (initialized
          // attribute).
          ServiceManager.save();
          logmon.log(BasicLevel.DEBUG,
                     getName() + ", ServiceManager started");
        }
      } catch (Exception exc) {
        logmon.log(BasicLevel.FATAL,
                   getName() + ", can't initialize services", exc);
        throw new Exception("Can't initialize services");
      }

      // Now we can start all message consumers.
      if (consumers != null) {
        for (Enumeration c=AgentServer.getConsumers(); c.hasMoreElements(); ) {
          MessageConsumer cons = (MessageConsumer) c.nextElement();
          if (cons != null) {
            try {
              if ((jgroups == null) || (cons instanceof Engine)) {
                cons.start();
              }
            } catch (IOException exc) {
              if (errBuf == null) errBuf = new StringBuffer();
              errBuf.append(cons.getName()).append(": ");
              errBuf.append(exc.getMessage()).append('\n');
              logmon.log(BasicLevel.FATAL,
                         getName() +
                         ", problem during " + cons.getName() + " starting", exc);
            }
          }
        }
      }
      // The server is running.
      logmon.log(BasicLevel.WARN, getName() + ", started at " + new Date());

      // Commit all changes.
      transaction.begin();
      transaction.commit();
      transaction.release();
    } catch (Exception exc) {
      logmon.log(BasicLevel.ERROR, getName() + "Cannot start", exc);
      synchronized(status) {
        // AF: Will be replaced by a BAD_STARTED status allowing the
        // stop and reset..
        status.value = Status.STOPPED;
      }
      throw exc;
    } catch (Throwable t) {
      logmon.log(BasicLevel.ERROR, getName() + "Cannot start", t);
      synchronized(status) {
        // AF: Will be replaced by a BAD_STARTED status allowing the
        // stop and reset..
        status.value = Status.STOPPED;
      }
      throw new Exception(t);
    }

    synchronized(status) {
      status.value = Status.STARTED;
    }

    if (errBuf == null) return null;
    return errBuf.toString();
  }

  /**
   *  Forces this AgentServer to stop executing. This method stops all
   * consumers and services. Be careful, if you specify a synchronous
   * process, this method wait for all server's thread to terminate; so
   * if this method is called from a server's thread it should result a
   * dead-lock.
   *
   * @param sync	If true the stop is precessed synchronous, otherwise
   *			a thread is created and the method returns.
   */
  public static void stop(boolean sync) {
    stop(sync, 0, false);
  }

  /**
   *  Forces this AgentServer to stop executing. This method stops all
   * consumers and services. Be careful, if you specify a synchronous
   * process, this method wait for all server's thread to terminate; so
   * if this method is called from a server's thread it should result a
   * dead-lock.
   *
   * @param sync	If true the stop is precessed synchronous, otherwise
   *			a thread is created and the method returns.
   * @param delay       if sync is false then the thread in charge of
   *                    stopping the server waits this delay before
   *                    initiating the stop.
   * @param reset	If true the server is stopped then reseted.
   */
  public static void stop(boolean sync, long delay, boolean reset) {
    ServerStopper stopper = new ServerStopper(delay, reset);
    if (sync == true) {
      stopper.run();
    } else {
      // Creates a thread to execute AgentServer.stop in order to
      // avoid deadlock.
      Thread t = new Thread(stopper);
      t.setDaemon(true);
      t.start();
    }
  }

  static class ServerStopper implements Runnable {
    private long delay;
    private boolean reset;

    ServerStopper(long delay, boolean reset) {
      this.delay = delay;
      this.reset = reset;
    }

    public void run() {
      if (delay > 0) {
        try {
          Thread.sleep(delay);
        } catch (InterruptedException exc) {}
      }
      AgentServer.stop();
      if (reset) {
        AgentServer.reset();
      }
    }
  }

  /**
   *  Forces this AgentServer to stop executing. This method stops all
   * consumers and services. Be careful, the stop process is now synchronous
   * and wait for all server's thread to terminate ; If this method is called
   * from a server's thread it should result a dead-lock.
   */
  public static void stop() {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, getName() + ", stop()", new Exception());
    else
      logmon.log(BasicLevel.WARN, getName() + ", stop()");

    synchronized(status) {
      if ((status.value != Status.STARTED) &&
          (status.value != Status.STOPPED)) {
        logmon.log(BasicLevel.WARN,
                   getName() + "cannot stop, bad status: " + status.value);
        return;
      }
      status.value = Status.STOPPING;
    }

    try {
      // If the server is part of an HA group stops the JGroup component
      if (jgroups != null) jgroups.disconnect();

      // Stop all message consumers.
      if (consumers != null) {
        for (Enumeration c=AgentServer.getConsumers(); c.hasMoreElements(); ) {
          MessageConsumer cons = (MessageConsumer) c.nextElement();
          if (cons != null) {
            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG,
                         getName() + ", stop " + cons.getName());

            cons.stop();

            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG,
                         getName() + ", " + cons.getName() + " stopped");
          }
        }
      }
      // Stop all services.
      ServiceManager.stop();
      // Stop all drivers
      Driver.stopAll();

      // Wait for all threads before stop the TM !!
      while (true) {
        int nbt = getThreadGroup().activeCount();
        if (nbt == 0) break;

        Thread[] tab = new Thread[nbt];
        getThreadGroup().enumerate(tab);
        if ((nbt == 1) && (tab[0] == Thread.currentThread())) break;

        for (int j=0; j<tab.length; j++) {
          logmon.log(BasicLevel.DEBUG,
                     "[" +  tab[j].getName() + ":" +
                     (tab[j].isAlive()?"alive":"-") + "/" +
                     (tab[j].isDaemon()?"daemon":"-") + "," +
                     tab[j]);
        }
        try {
          Thread.sleep(2500);
        } catch (InterruptedException e) {}
      }

      // Stop the transaction manager.
      if (transaction != null) transaction.stop();

      // Wait for the transaction manager stop

      Runtime.getRuntime().gc();
      System.runFinalization();

      logmon.log(BasicLevel.WARN, getName() + ", stopped at " + new Date());
    } catch (Throwable t) {
      logmon.log(BasicLevel.ERROR, getName() + "Cannot stop", t);
      synchronized(status) {
        // AF: Will be replaced by a BAD_STOPPED status allowing the
        // stop and reset..
        status.value = Status.STOPPED;
      }
    }

    synchronized(status) {
      status.value = Status.STOPPED;
    }
  }

  public static final String OKSTRING = "OK";
  public static final String ERRORSTRING = "ERROR";
  public static final String ENDSTRING = "END";
  /**
   * Main for a standard agent server.
   * The start arguments include in first position the identifier of the
   * agent server to start, and in second position the directory name where
   * the agent server stores its persistent data.
   *
   * @param args	start arguments
   *
   * @exception Exception
   *	unspecialized exception
   */
  public static void main(String args[]) throws Exception {
    try {
      init(args);
    } catch (Throwable exc) {
      System.out.println(getName() + "initialisation failed: " + ERRORSTRING);
      System.out.println(exc.toString());
      System.out.println(ENDSTRING);
      if (logmon == null)
        logmon = Debug.getLogger(Debug.A3Debug + ".AgentServer");
      logmon.log(BasicLevel.ERROR,
                 getName() + " initialisation failed", exc);
      System.exit(1);
    }

    try {
      String errStr = start();
      // Be careful, the output below is needed by some tools (AdminProxy for
      // example.
      if (errStr == null) {
        System.out.println(getName() + " started: " + OKSTRING);
      } else {
        System.out.println(getName() + " started: " + ERRORSTRING);
        System.out.print(errStr);
        System.out.println(ENDSTRING);
      }
    } catch (Throwable exc) {
      System.out.println(getName() + " start failed: " + ERRORSTRING);
      System.out.print(exc.toString());
      System.out.println(ENDSTRING);
      if (logmon == null)
        logmon = Debug.getLogger(Debug.A3Debug + ".AgentServer");
      logmon.log(BasicLevel.ERROR, getName() + " failed", exc);
      System.exit(1);
    }
  }
}
