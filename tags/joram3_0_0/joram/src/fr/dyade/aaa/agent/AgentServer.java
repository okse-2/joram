/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */
package fr.dyade.aaa.agent;

import java.io.*;
import java.util.*;

import org.objectweb.monolog.api.BasicLevel;
import org.objectweb.monolog.api.Monitor;

import fr.dyade.aaa.util.*;

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
 * (<code>server</code>s and <code>transient</code>s elements):
 * <ul>
 * <li>Each domain of the configuration is described by an XML element with
 * attributes giving the name and the classname for <code>Network</code>
 * implementation (class {@link SingleCnxNetwork <code>SingleCnxNetwork</code>}
 * by default).
 * <li>Each server is described by an XML element with attributes giving
 * the id, the name (optional) and the node (the <code>hostname</code>
 * attribute describes the name or the IP address of this node). A server
 * may be either persitent (<code>server</code> element) or transient
 * (<code>transient</code> element) as described below:
 * <ul>
 * <li>Each persistent server must be part of a domain (at least one), to
 * do this you need to define a <code>network</code> element with attributes
 * giving the domain's name (<code>domain</code> attribute) and the
 * communication port (<code>port</code> attribute).
 * <li>A transient server must be handled by a persitent one, the
 * <code>server</code> attribute is used to determine it.
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
 *   &lt;transient id="1" name="T1" hostname="acores" server="0"/&gt;
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
 * <li>The second server (id 1 and name "T1") is a transient one, it is
 * also hosted by acores and its proxy is the server 0 (this declaration
 * implicitly defines a "transient" domain on server 0).
 * <li>The third server is a persitent one, it is hosted by bermudes and it
 * is the router between D1 and D2.
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
 *
 * @author  Andre Freyssinet
 */
public final class AgentServer {
  /** RCS version number of this file: $Revision: 1.8 $ */
  public static final String RCS_VERSION="@(#)$Id: AgentServer.java,v 1.8 2002-03-06 16:50:00 joram Exp $"; 

  public final static short NULL_ID = -1;

  private static short serverId;

  private static Monitor logmon = null;

  public final static short getServerId() {
    return serverId;
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
   * Static references to all messages consumumers initialized in this
   * agent server (including <code>Engine</code>).
   */
  static MessageConsumer[] consumers = null;
  /** Static description of all known agent servers. */
  static ServerDesc[] servers = null;
  /** Static reference to the transactional monitor. */
  static Transaction transaction = null;

  /**
   * Returns the agent server transaction context.
   */
  public static Transaction getTransaction() {
    return transaction;
  }
  
  /**
   * Searches for the property with the specified key in the server property
   * list.
   *
   * @param key	   the hashtable key.
   * @param value  a default value.
   * @return	   the value with the specified key value.
   */
  public static String getProperty(String key) {
    return System.getProperty(key);
  }

  public static String getProperty(String key,
                                   String value) {
    return System.getProperty(key, value);
  }

  /**
   * Determines the integer value of the server property with the
   * specified name.
   *
   * @param key property name.
   * @return 	the Integer value of the property.
   *
   * @see java.lang.Integer.getInteger
   */
  public static Integer getInteger(String key) {
    try {
      return Integer.decode(System.getProperty(key));
    } catch (Exception exc) {
      return null;
    }
  }
  
  /**
   * Compile with or without agent monitoring code.
   */
  public static final boolean MONITOR_AGENT = false;

  /**
   * Tests if this agent server is transient.
   *
   * @return	true if the server is transient; false otherwise; 
   */
  public final static boolean isTransient() {
    try {
      return isTransient(getServerId());
    } catch (UnknownServerException exc) {
      // can't never happened.
      return false;
    }
  }

  /**
   * Tests if the specified agent server is transient.
   *
   * @return	true if the server is transient; false otherwise; 
   * @exception UnknownServerException
   *	The specified server does not exist.
   */
  public final static boolean isTransient(short sid) throws UnknownServerException {
    try {
      return getServerDesc(sid).isTransient;
    } catch (NullPointerException exc) {
      throw new UnknownServerException("Unknown server id. #" + sid);
    }
  }

  /**
   * Gets the number of server known on the current server.
   *
   * @return	the number of server.
   */
  final static int getServerNb() {
    return servers.length;
  }

  /**
   * Gets the characteristics of the corresponding server.
   *
   * @param id	agent server id.
   * @return	the server's descriptor.
   */
  final static ServerDesc getServerDesc(short sid) throws UnknownServerException {
    try {
      if (servers[sid] == null)
        throw new UnknownServerException("Unknow server id. #" + sid);
      return servers[sid];
    } catch (ArrayIndexOutOfBoundsException exc) {
      throw new UnknownServerException("Unknow server id. #" + sid);
    } catch (NullPointerException exc) {
      throw new UnknownServerException("Bad server configuration");
    }
  }

  /**
   * Gets the message consumer for the corresponding server.
   *
   * @param id	agent server id.
   * @return	the corresponding message consumer.
   */
  final static MessageConsumer getConsumer(short sid) throws UnknownServerException {
    return getServerDesc(sid).domain;
  }

  /**
   * Get the host name of an agent server.
   *
   * @param id		agent server id
   * @return		server host name as declared in configuration file
   */
  public final static String getHostname(short sid) throws UnknownServerException {
    return getServerDesc(sid).hostname;
  }

  /**
   * Get the description of all services of the current agent server.
   *
   * @param id		agent server id
   * @return		server host name as declared in configuration file
   */
  final static ServiceDesc[] getServices() {
    return servers[getServerId()].services;
  }

//   /**
//    * Get the description of all services of an agent server.
//    *
//    * @param id		agent server id
//    * @return		server host name as declared in configuration file
//    */
//   public final static ServiceDesc[] getServices(short sid) {
//     // We have not to clone this array to avoid user modification since
//     // ServiceDesc is constant outside of this package.
//     return getServerDesc(sid).services;
//   }

  /**
   * Get the argument strings for a particular service.
   * The information provides from the A3 configuration file, so it's
   * only available if this file contains service's informations for all
   * nodes.
   *
   * @see A3CMLHandler#getServiceArgs(short,String)
   *
   * @param id		agent server id
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
    if (a3configHdl != null)
      return a3configHdl.getServiceArgs(sid, classname);
    else
      throw new Exception("Service \"" +
			  classname + "\" not found on server#" + sid);
  }

  /**
   * Get the argument strings for a particular service running on a server
   * (identified by its id.) and on associated transient servers.
   * The information provides from the A3 configuration file, so it's
   * only available if this file contains service's informations for all
   * nodes.
   *
   * @see A3CMLHandler#getServiceArgsFamily(short,String)
   *
   * @param id		agent server id
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
  String getServiceArgsFamily(short sid,
			      String classname) throws Exception {
    if (a3configHdl != null)
      return a3configHdl.getServiceArgsFamily(sid, classname);
    else
      throw new Exception("Service \"" +
			  classname + "\" not found on family server#" + sid);
  }

  /**
   * Get the argument strings for a particular service running on a server
   * identified by its host.
   * The information provides from the A3 configuration file, so it's
   * only available if this file contains service's informations for all
   * nodes.
   *
   * @see A3CMLHandler#getServiceArgs(String, String)
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
    if (a3configHdl != null)
      return a3configHdl.getServiceArgs(hostname, classname);
    else
      throw new Exception("Service \"" +
			  classname + "\" not found on host " + hostname);
  }

  private static A3CMLHandler a3configHdl = null;

  /**
   * The second step of initialization. It needs the Transaction component be
   * up, then it initializes all <code>Server</code> structures from the
   * <code>A3CMLHandler</code> ones.
   */
  private static void configure() throws Exception {
    if (a3configHdl == null) {
      // It's an isolated server.

      // Creates the local MessageConsumer: the Engine.
      engine = Engine.newInstance(false);
      consumers = new MessageConsumer[1];
      consumers[0] = engine;

      servers = new ServerDesc[1];
      servers[0] = new ServerDesc(serverId, "default", "localhost");
      servers[0].isTransient = false;
      servers[0].domain = engine;
    } else {
      A3CMLServer root;
      root = (A3CMLServer) a3configHdl.servers.get(new Short(serverId));
      if (root instanceof A3CMLPServer) {
	configure((A3CMLPServer) root);
      } else if (root instanceof A3CMLTServer) {
	configure((A3CMLTServer) root);
      } else {
	throw new Exception("Unknown agent server type: " + serverId);
      }
    }
    return;
  }

  private static void configure(A3CMLTServer root) throws Exception {
    short rootid = root.sid;

    // AF: Be careful, a transient server can not be configured as well.
    // In this case, there is 2 domains: a local one, and a network one that
    // route all remote messages to the proxy server.
    consumers = new MessageConsumer[2];
    // Creates the local MessageConsumer: the Engine.
    engine = Engine.newInstance(true);
    consumers[0] = engine;
    // Creates the network MessageConsumer and initialize it.
    consumers[1] = new TransientNetworkServer();

    // Gets the descriptor of gateway server.
    A3CMLPServer gateway = (A3CMLPServer) a3configHdl.servers.get(new Short(root.gateway));
    // Initializes the descriptors of each server.
    servers = new ServerDesc[a3configHdl.maxid +1];
    for (Enumeration s = a3configHdl.servers.elements(); s.hasMoreElements();) {
      A3CMLServer server = (A3CMLServer) s.nextElement();
      servers[server.sid] = new ServerDesc(server.sid,
					   server.name,
					   server.hostname);
      if (server instanceof A3CMLPServer) {
	servers[server.sid].isTransient = false;
      } else if (server instanceof A3CMLTServer) {
	servers[server.sid].isTransient = true;
      }
      servers[server.sid].gateway = root.gateway;
      servers[server.sid].domain = consumers[1];
      if (server.services != null) {
	ServiceDesc services[]  = new ServiceDesc[server.services.size()];
	int idx = 0;
	for (Enumeration x = server.services.elements(); x.hasMoreElements();) {
	  A3CMLService service = (A3CMLService) x.nextElement();
	  services[idx++] = new ServiceDesc(service.classname, service.args);
	}
	servers[server.sid].services = services;
      }
    }

    // Fixes the current transient server properties.
    // Be careful, the gateway attribute is used by NetworkTransientServer
    // to determine the proxy id server.
    servers[rootid].gateway = root.gateway;
    servers[rootid].domain = consumers[0];

    // Search the listen port of proxy
    for (Enumeration n = gateway.networks.elements(); n.hasMoreElements();) {
      A3CMLNetwork network = (A3CMLNetwork) n.nextElement();
      if (network.domain.equals("transient"))
	servers[root.gateway].port = network.port;
    }
    if (servers[root.gateway].port == -1)
      throw new Exception("There is no transient network on server #" +
			  root.gateway + ", bad configuration.");
  }

  private static void configure(A3CMLPServer root) throws Exception {
    short rootid = root.sid;
    Vector toExplore = new Vector();

    Hashtable consumersTempHT = new Hashtable();
    // Creates the local MessageConsumer: the Engine.
    engine = Engine.newInstance(false);
    consumersTempHT.put("local", engine);
    
    // Search alls directly accessible domains.
    for (Enumeration n = root.networks.elements();
	 n.hasMoreElements();) {
      A3CMLNetwork network = (A3CMLNetwork)  n.nextElement();
      if (! network.domain.equals("transient")) {
	A3CMLDomain domain = (A3CMLDomain) a3configHdl.domains.get(network.domain);
	domain.gateway = rootid;
	toExplore.add(domain);

	// Creates the corresponding MessageConsumer.
	try {
	  Network consumer = (Network) Class.forName(domain.network).newInstance();
	  // Initializes it with domain description. Be careful, this array
	  // is kept in consumer, don't reuse it!!
	  short[] domainSids = new short[domain.servers.size()];
	  for (int i=0; i<domainSids.length; i++) {
	    domainSids[i] = ((A3CMLServer) domain.servers.get(i)).sid;
	  }
	  consumer.init(domain.name, network.port, domainSids);
	  consumersTempHT.put(network.domain, consumer);
	} catch (ClassNotFoundException exc) {
	  throw exc;
	} catch (InstantiationException exc) {
	  throw exc;
	} catch (IllegalAccessException exc) {
	  throw exc;
	}
      } else {
	Vector tdomain = new Vector();
	// Initializes it with domain description. Be careful, this array
	// is kept in consumer, don't reuse it!!
	for (Enumeration s = a3configHdl.servers.elements(); s.hasMoreElements();) {
	  A3CMLServer server = (A3CMLServer) s.nextElement();
	  if ((server instanceof A3CMLTServer)  &&
	      (((A3CMLTServer) server).gateway == rootid)) {
	    tdomain.add(server);
	  }
	}
	if (tdomain.size() > 0) {
	  short[] domainSids = new short[tdomain.size()];
	  for (int i=0; i<domainSids.length; i++) {
	    domainSids[i] = ((A3CMLServer) tdomain.get(i)).sid;
	  }
	  // Creates a transient server's proxy and initializes it.
	  consumersTempHT.put(network.domain,
			new TransientNetworkProxy(network.port, domainSids));
	}
      }
    }

    consumers = new MessageConsumer[consumersTempHT.size()];
    int i = 0;
    for (Enumeration e = consumersTempHT.elements(); e.hasMoreElements() ;) {
      consumers[i++] = (MessageConsumer) e.nextElement();
    }

    root.visited = true;
    root.gateway = -1;
    root.domain = "local";

    servers = new ServerDesc[a3configHdl.maxid +1];
    while (toExplore.size() > 0) {
      A3CMLDomain domain = (A3CMLDomain) toExplore.remove(0);
      A3CMLPServer gateway = (A3CMLPServer) a3configHdl.servers.get(new Short(domain.gateway));
      // Parse all nodes of this domain
      for (Enumeration s = domain.servers.elements();
	   s.hasMoreElements();) {
	A3CMLPServer server = (A3CMLPServer) s.nextElement();
	if (! server.visited) {
	  server.visited = true;
	  if (domain.gateway == rootid) {
	    server.gateway = -1;
	    server.domain = domain.name;
	  } else {
	    server.gateway = domain.gateway;
	    server.domain = gateway.domain;
	  }

	  // If the server is a router then add the accessible domains
	  // to the list.
	  for (Enumeration n = server.networks.elements();
	       n.hasMoreElements();) {
	    A3CMLNetwork network = (A3CMLNetwork)  n.nextElement();
	    if (! network.domain.equals("transient")) {
	      A3CMLDomain d2 = (A3CMLDomain) a3configHdl.domains.get(network.domain);
	      if (d2.gateway == -1) {
		// The domain is not already explored.
		if (server.gateway == -1)
		  d2.gateway = server.sid;
		else
		  d2.gateway = server.gateway;
		toExplore.add(d2);
	      } else if (d2 != domain) {
		// More than one route to this domain.
		throw new Exception("more than one route to: " + domain);
	      } else {
		if (server.gateway == -1) {
		  // The server is directly accessible from root server by
		  // this network interface; fixes the communication port
		  // for this server.
		  server.port = network.port;
		}
	      }
	    }
	  }
	}
      }
    }

    servers = new ServerDesc[a3configHdl.maxid +1];    
    for (Enumeration s = a3configHdl.servers.elements(); s.hasMoreElements();) {
      A3CMLServer server = (A3CMLServer) s.nextElement();
      servers[server.sid] = new ServerDesc(server.sid,
					   server.name,
					   server.hostname);

      if (server instanceof A3CMLPServer) {
	A3CMLPServer serverP = (A3CMLPServer) server;
	if (serverP.visited) {
	  servers[server.sid].isTransient = false;
	  servers[server.sid].gateway = serverP.gateway;
	  // For each server set the gateway to the real next destination of
	  // messages; if the server is directly accessible: itself.
	  if (servers[server.sid].gateway == -1) {
	    servers[server.sid].gateway = serverP.sid;
	    servers[server.sid].port = serverP.port;
	  }
	  servers[server.sid].domain = (MessageConsumer) consumersTempHT.get(serverP.domain);
	  if (server.services != null) {
	    ServiceDesc services[]  = new ServiceDesc[server.services.size()];
	    int idx = 0;
	    for (Enumeration x = server.services.elements(); x.hasMoreElements();) {
	      A3CMLService service = (A3CMLService) x.nextElement();
	      services[idx++] = new ServiceDesc(service.classname, service.args);
	    }
	    servers[server.sid].services = services;
	  }
	} else {
	  throw new Exception(serverP + " inaccessible");
	}
      } else if (server instanceof A3CMLTServer) {
	A3CMLTServer serverT = (A3CMLTServer) server;
	if (serverT.gateway == rootid) {
	  servers[server.sid].isTransient = true;
	  servers[server.sid].gateway = -1;
	  servers[server.sid].domain = (MessageConsumer) consumersTempHT.get("transient");
	} else {
	  servers[server.sid].isTransient = true;
	  A3CMLServer router = (A3CMLServer) a3configHdl.servers.get(new Short(serverT.gateway));
	  if (! (router instanceof A3CMLPServer)) {
	    throw new Exception("transient " + router +
			" can't route transient " + serverT);
	  }
	  if (((A3CMLPServer) router).visited) {
	    if (((A3CMLPServer) router).gateway == -1) {
	      servers[server.sid].gateway = router.sid;
	      servers[server.sid].domain = (MessageConsumer) consumersTempHT.get(((A3CMLPServer) router).domain);
	    } else {
	      servers[server.sid].gateway = ((A3CMLPServer) router).gateway;
	      servers[server.sid].domain = (MessageConsumer) consumersTempHT.get(((A3CMLPServer) router).domain);
	    }
	  } else {
	    throw new Exception(serverT + "/" + router + " inaccessible");
	  }
	}
	if (server.services != null) {
	  ServiceDesc services[]  = new ServiceDesc[server.services.size()];
	  int idx = 0;
	  for (Enumeration x = server.services.elements(); x.hasMoreElements();) {
	    A3CMLService service = (A3CMLService) x.nextElement();
	    services[idx++] = new ServiceDesc(service.classname, service.args);
	  }
	  servers[server.sid].services = services;
	}
      }
    }
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
  public static int
  init(String args[]) throws Exception {
    if (args.length < 2)
      throw new Exception("usage: java <main> sid storage");
    try {
      serverId = (short) Integer.parseInt(args[0]);
    } catch (NumberFormatException exc) {
      throw new Exception("usage: java <main> sid storage");
    }
   
    String path = args[1];

    Debug.init(serverId);
    logmon = Debug.getMonitor(Debug.A3Debug + ".AgentServer" +
                              ".#" + AgentServer.getServerId());

    // Read and parse the configuration file.
    boolean isTransient;
    
    // Gets static configuration of agent servers from a file. This method
    // fills the object graph configuration in the <code>A3CMLHandler</code>
    // object, then the configure method really initializes the server.
    // There are two steps because the configuration step needs the transaction
    // components to be initialized.
    try {
      a3configHdl = A3CMLHandler.getConfig(serverId);
    } catch (Exception exc) {
      logmon.log(BasicLevel.FATAL,
                 "AgentServer#" + serverId +
                 ", problem during configuration parsing", exc);
      throw new Exception("Problem during configuration parsing");
    }

    if (a3configHdl == null) {
      logmon.log(BasicLevel.FATAL,
                 "AgentServer#" + serverId +
                 ", can't initialize (no configuration)");
      throw new Exception("Can't initialize server (no configuration)");
    } else {
      try {
	isTransient = a3configHdl.isTransient(serverId);
      } catch (Exception exc) {
	logmon.log(BasicLevel.FATAL,
                 "AgentServer#" + serverId + ", can't initialize", exc);
	throw new Exception("Can't initialize server");
      }
    }

    try {
      if (isTransient) {
	transaction = new SimpleTransaction(path);
      } else {
	String tname = getProperty("Transaction", "JTransaction");
	if (tname.equals("NullTransaction")) {
	  transaction = new NullTransaction(path);
	} else if (tname.equals("FSTransaction")) {
	  transaction = new FSTransaction(path);
	} else if (tname.equals("ATransaction")) {
	  transaction = new ATransaction(path);
	} else {
	  transaction = new JTransaction(path);
	}
      }
    } catch (IOException exc) {
      logmon.log(BasicLevel.FATAL,
                 "AgentServer#" + serverId +
                 ", can't start transaction manager", exc);
      throw new Exception("Can't start transaction manager");
    }

    try {
      // Configure the agent server.
      configure();
    } catch (Exception exc) {
      logmon.log(BasicLevel.FATAL,
                 "AgentServer#" + serverId + ", can't configure", exc);
      throw new Exception("Can't configure server");
    }

    try {
      // Initialize AgentId class's variables.
      AgentId.init();
    } catch (ClassNotFoundException exc) {
      logmon.log(BasicLevel.FATAL,
                 "AgentServer#" + serverId + ", can't initialize AgentId",
                 exc);
      throw new Exception("Can't initialize AgentId, bad classpath");
    } catch (IOException exc) {
      logmon.log(BasicLevel.FATAL,
                 "AgentServer#" + serverId + ", can't initialize AgentId",
                 exc);
      throw new Exception("Can't initialize AgentId, storage problems");
    }

    try {
      // then restores all messages.
      String[] list = transaction.getList("@");
      for (int i=0; i<list.length; i++) {
	Message msg = Message.load(list[i]);
	if (msg.update.getFromId() == serverId) {
	  // The update has been locally generated, the message is ready to
	  // deliver to its consumer (Engine or Network component). So we have
	  // to insert it in the queue of this consumer.
	  servers[msg.update.getToId()].domain.insert(msg);
	} else {
	  // The update has been generated on a remote server. If the message
	  // don't have a local update, It is waiting to be delivered. So we
	  // have to insert it in waiting list of the network component that
	  // received it.
	  servers[msg.update.getFromId()].domain.insert(msg);
	}
      }
    } catch (ClassNotFoundException exc) {
      logmon.log(BasicLevel.FATAL,
                 "AgentServer#" + serverId + ", can't restore messages", exc);
      throw new Exception("Can't restore messages, bad classpath");
    } catch (IOException exc) {
      logmon.log(BasicLevel.FATAL,
                 "AgentServer#" + serverId + ", can't restore messages", exc);
      throw new Exception("Can't restore messages, storage problems");
    }

    // initialize channel before initializing fixed agents
    Channel.newInstance();

    try {
      //  Initialize Agent component: load AgentFactory and all
      // fixed agents.
      Agent.init();
    } catch (Exception exc) {
      logmon.log(BasicLevel.FATAL,
                 "AgentServer#" + serverId + ", can't initialize agents", exc);
      throw new Exception("Can't initialize agents");
    }

    try {
      //  Initialize and start services.
      ServiceManager.init();
      /* Actually get Services from A3CML configuration file. */
      ServiceDesc services[] = AgentServer.getServices();
      if (services != null) {
	for (int i = 0; i < services.length; i ++) {
	  ServiceManager.register(services[i].getClassName(),
				  services[i].getArguments());
	}
      }
      /* For future use */
      // Adds (vs removes) services in accordance with launching script.
//    for (;;) {
// 	if (add)
// 	  ServiceManager.register();
// 	else if (remove)
// 	  ServiceManager.unregister();
// 	else
// 	  Debug.trace("Unknown request", false);
//    }
      /* End */
      ServiceManager.save();
      // May be we can launch services in AgentServer.start()
      ServiceManager.start();
    } catch (Exception exc) {
      logmon.log(BasicLevel.FATAL,
                 "AgentServer#" + serverId + ", can't initialize services",
                 exc);
      throw new Exception("Can't initialize services");
    }

    // May be ProcessManager should become a service.
    try {
      ProcessManager.init();
    } catch (Exception exc) {
      logmon.log(BasicLevel.FATAL,
                 "AgentServer#" + serverId +
                 ", can't initialize ProcessManager", exc);
      throw new Exception("Can't initialize ProcessManager");
    }

    logmon.log(BasicLevel.WARN,
               "AgentServer#" + serverId + ", initialized at " + new Date());

    // Commit all changes.
    transaction.begin();
    transaction.commit();
    transaction.release();

    return 2;
  }

  /**
   *  Causes this AgentServer to begin its execution. This method starts all
   * <code>MessageConsumer</code> (i.e. the engine and the network components).
   */
  public static void
  start() throws Exception {
    boolean ok = true;

    // Now we can start all message consumers.
    for (int i=0; i<consumers.length; i++) {
      try {
	consumers[i].start();
      } catch (IOException exc) {
	ok = false;
	logmon.log(BasicLevel.FATAL,
                   "AgentServer#" + serverId + 
                   ", problem during " + consumers[i].getName() + " starting",
                   exc);
      }
    }

    // The server is running.
    if (ok) {
      logmon.log(BasicLevel.WARN,
                 "AgentServer#" + serverId + ", started at " + new Date());
    } else {
      throw new Exception("Problem during MessageConsumer starting");
    }

    // Commit all changes.
    transaction.begin();
    transaction.commit();
    transaction.release();
  }

  /**
   *  Forces this AgentServer to stop executing. This method stops all
   * consumers and services. Be careful, the stop process is asynchronous
   * and may be in progress when the method ends.
   */
  public static void stop() {
    if (logmon.isLoggable(BasicLevel.DEBUG)) {
      // Trace all threads that keep alive !!
      Thread t = new Thread() {
        public void run() {
          int i = 0;
          ThreadGroup tg = Thread.currentThread().getThreadGroup();
          while (tg.getParent() != null)
            tg = tg.getParent();

          try {
            sleep(10000L);
          } catch (InterruptedException e) {}

          while (true) {
            int nbt = tg.activeCount();
            Thread[] tab = new Thread[nbt];
            tg.enumerate(tab);
            
            for (int j=0; j<nbt; j++) {
              if (tab[j] == this) continue;
              logmon.log(BasicLevel.DEBUG,
                         "[" + i + "-" +
                         tab[j].getThreadGroup().getName() + "." +
                         tab[j].getName() + ":" +
                         (tab[j].isAlive()?"alive":"-") + "/" +
                         (tab[j].isDaemon()?"daemon":"-") + "," +
                         tab[j]);
            }
            try {
              sleep(60000L);
              i += 1;
            } catch (InterruptedException e) {}
          }
        }
      };
      t.setName("TFD");
      t.setDaemon(true);
      t.start();
    }

    // Stop all message consumers.
    for (int i=0; i<consumers.length; i++) {
      if (consumers[i] != null) {
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG,
                     "AgentServer#" + serverId + ", stop " + consumers[i]);
        consumers[i].stop();
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG,
                     "AgentServer#" + serverId + ", " +
                     consumers[i] + "stopped");
      }
    }
    // Stop all services.
    ServiceManager.stop();

    // Wait for all message consumers and services before stop the TM !!

    // Stop the transaction manager.
    if (transaction != null) transaction.stop();
    logmon.log(BasicLevel.WARN,
               "AgentServer#" + serverId + ", stopped at " + new Date());
  }

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
  public static void main (String args[]) throws Exception {
    try {
      init(args);
    } catch (Throwable exc) {
      System.out.println("AgentServer#" + getServerId() +
                         "initialisation failed: " + exc.toString());
      System.exit(1);
    }
    try {
      start();
      // Be careful, the output below is needed by some tools (AdminProxy for
      // example.
      System.out.println("AgentServer#" + getServerId() + " started.");
    } catch (Exception exc) {
      System.out.println("AgentServer#" + getServerId() + " failed: " +
                         exc.toString());
      // AF: TODO ?
//    AgentServer.stop();
      System.exit(1);
    }
  }
}

