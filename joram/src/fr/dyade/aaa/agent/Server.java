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
import java.text.ParseException;

import fr.dyade.aaa.util.*;

/**
 * The <code>Server</code> class ...
 *
 * @author  Andr* Freyssinet
 * @version 1.0, 22 oct 97
 */
public class Server {

public static final String RCS_VERSION="@(#)$Id: Server.java,v 1.3 2000-10-05 15:15:23 tachkeni Exp $"; 

  static short serverId;
  public final static short NULL_ID = -1;

  final static short MIN_TRANSIENT_ID = 16384;

  static volatile boolean isRunning = false;

  static Engine engine = null;
  static Channel channel = null;
  static Network network = null;

  static MatrixClock mclock = null;

  static Queue mq = null;

  static MessageQueue qin = null;
  static MessageQueue qout = null;

  static Transaction transaction = null;

  /* AF: networkServers, transientServers are now obsolete, they are
   * reference to internal objects in A3Config. I let them here in order
   * to temporary allow direct access from others class in the package:
   * Agent, Network, TransientManager and A3ServersList.
   */
  static ServerDesc[] networkServers = null;
  static ServerDesc[] transientServers = null;
  static ServiceDesc[] services = null;

  static A3Config a3config = null;

  /** server properties, publicly accessible (see get/set operation) */
  static Properties properties = null;

  /** Set the property pair (key, value). */
  public static Object setProperty(String key,
				   String value) {
    // AF: since jdk1.2 we can use Properties.setProperty.
    return properties.put(key, value);
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
    return properties.getProperty(key);
  }

  public static String getProperty(String key,
			    String value) {
    return properties.getProperty(key, value);
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
      return Integer.decode(properties.getProperty(key));
    } catch (Exception exc) {
      return null;
    }
  }

     

  /**
   * Compile server with or without Administration features.
   */
  public static final boolean ADMINISTRED = false;
  
  /**
   * Compile with or without agent monitoring code.
   */
  public static final boolean MONITOR_AGENT = false;

  /**
   * if true the server is administred.
   * by default a Server is administred.
   */
  public static boolean admin = true;

  /**
   * Gets static configuration of agent servers from a file.
   * Temporary solution to allow parallel tests by developers.
   * Also gets the service configuration for this agent server.
   * <p>
   * File name is found in A3SERVERS_CFG environment variable,
   * ./a3servers.cfg is the default. The environment variable may be set
   * by option -DA3SERVERS_CFG=xxx when launching execution.
   * <p>
   * The file format is one line for each agent server, and one line for
   * each service for this agent server. Service lines for other agent servers
   * are ignored. Comment lines begin with #.
   * Line format for a persistent agent server is:
   * <br> [server] "id" ["name"] "host name" "port" ["transient port"]<br>
   * Line format for a transient agent server is:
   * <br> [server] "id" ["name"] "host name" @"persistent server id"<br>
   * with an id greater than or equal to <code>MIN_TRANSIENT_ID</code>.
   * Line format for a service is:
   * <br> service @"hosting server id" "service class" ["parameters"]<br>
   *
   * @exception Exception
   *	unspecialized exception
   */
  static A3Config readConfig(String cfgFileName) throws Exception {
    A3Config a3config = null;
    File cfgFile = null;

    if (cfgFileName != null) cfgFile = new File(cfgFileName);

    if ((cfgFile == null) || (!cfgFile.exists()) || (!cfgFile.isFile())) {
      // There is no config file, set a default configuration.
      a3config = new A3Config();

      a3config.networkServers = new ServerDesc[1];
      a3config.transientServers = null;
      a3config.networkServers[0] = new ServerDesc((short) 0,
						  "default",
						  "localhost",
						  0);
      return a3config;
    }
    
    try {
      FileReader fileRd = new FileReader(cfgFile);
      BufferedReader bufRd = new BufferedReader(fileRd);

      Vector cfg = new Vector();
      int nbNetwork = 0;
      int nbTransient = 0;
      int nbServices = 0;
      int lineno = 0;

      lineLoop:
      while (true) {
	String line = bufRd.readLine();
	lineno += 1;

	if (line == null) break lineLoop;
	if (line.startsWith("#")) continue lineLoop;
	StringTokenizer parser = new StringTokenizer(line);
	if (! parser.hasMoreTokens()) continue lineLoop;
	String token = parser.nextToken();

	// finds line kind
	if (token.equals("service")) {
	  // looks up for hosting server
	  if (! parser.hasMoreTokens())
	    throw new ParseException("bad syntax (missing field), line#" +
				     lineno, 0);
	  token = parser.nextToken();
	  if (token.charAt(0) != '@')
	    throw new ParseException("bad syntax (server id), line#" +
				     lineno, 0);
	  short sid = Short.parseShort(token.substring(1));
	  if (sid != getServerId()) {
	    // ignores line
	    continue lineLoop;
	  }

	  // finds service class name
	  if (! parser.hasMoreTokens())
	    throw new ParseException("bad syntax (missing field), line#" +
				     lineno, 0);
	  String srvClassName = parser.nextToken();

	  // finds optional parameters
	  String srvParameters = null;
	  if (parser.hasMoreTokens()) {
	    srvParameters = parser.nextToken();
	    while (parser.hasMoreTokens()) {
	      srvParameters = srvParameters + " " + parser.nextToken();
	    }
	  }

	  ServiceDesc desc = new ServiceDesc(srvClassName, srvParameters);
	  cfg.addElement(desc);
	  nbServices += 1;
	} else {
	  if (token.equals("server")) {
	    if (! parser.hasMoreTokens())
	      throw new ParseException("bad syntax (missing field), line#" +
				       lineno, 0);
	    token = parser.nextToken();
	  } else {
	    // default is a server line
	  }

	  // finds server id
	  short sid = Short.parseShort(token);
	  if (sid < 0)
	    throw new ParseException("bad syntax (server id), line#"  +
				     lineno, 0);

	  if (! parser.hasMoreTokens())
	    throw new ParseException("bad syntax (missing field), line#" +
				     lineno, 0);
	  String name = parser.nextToken();
	  String hostname = null;
	  int port = -1;
	  if (! parser.hasMoreTokens())
	    throw new ParseException("bad syntax (missing field), line#" +
				     lineno, 0);
	  token = parser.nextToken();

	  if (isTransient(sid)) {
	    nbTransient += 1;
	    // hostname may be a server id prefixed with @
	    if (token.charAt(0) == '@') {
	      hostname = name;
	      name = "server" + sid;
	    } else {
	      hostname = token;
	      if (! parser.hasMoreTokens())
		throw new ParseException("bad syntax (missing field), line#" +
					 lineno, 0);
	      token = parser.nextToken();
	      if (token.charAt(0) != '@')
		throw new ParseException("bad syntax (transient), line#"  +
					 lineno, 0);
	    }
	    short persistentId = Short.parseShort(token.substring(1));
	      cfg.addElement(new ServerDesc(sid, name, hostname, persistentId));
	  } else {
	    nbNetwork += 1;
	    // try to read a port number
	    try {
	      port = Integer.parseInt(token);
	      hostname = name;
	      name = name + ":" + port;
	    } catch (NumberFormatException exc) {
	      hostname = token;
	      if (! parser.hasMoreTokens())
		throw new ParseException("bad syntax (missing field), line#" +
					 lineno, 0);
	      token = parser.nextToken();
	      port = Integer.parseInt(token);
	    }
	    ServerDesc desc = new ServerDesc(sid, name, hostname, port);
	    
	    // a transient port number may follow
	    if (parser.hasMoreTokens()) {
	      token = parser.nextToken();
	      desc.transientPort = Integer.parseInt(token);
	    }
	    cfg.addElement(desc);
	  }
	}
      }

      a3config = new A3Config();
      a3config.networkServers = new ServerDesc[nbNetwork];
      a3config.transientServers = new ServerDesc[nbTransient];

      ServiceDesc[] services = new ServiceDesc[nbServices];
      int isrv = 0;
      
      for (int i=0; i<cfg.size(); i++) {
	if (cfg.elementAt(i) instanceof ServiceDesc) {
	  services[isrv] = (ServiceDesc) cfg.elementAt(i);
	  isrv ++;
	} else {
	  ServerDesc desc = (ServerDesc) cfg.elementAt(i);
	  if (desc.sid == getServerId())
	    desc.services = services;
	  if (isTransient(desc.sid)) {
	    if (((desc.sid - MIN_TRANSIENT_ID) >= nbTransient) ||
		(a3config.transientServers[desc.sid - MIN_TRANSIENT_ID] != null))
	      throw new ParseException("bad definition server#" + desc.sid, 0);
	    a3config.transientServers[desc.sid - MIN_TRANSIENT_ID] = desc;
	  } else {
	    if ((desc.sid >= nbNetwork) ||
		(a3config.networkServers[desc.sid] != null))
	      throw new ParseException("bad definition server#" + desc.sid, 0);
	    a3config.networkServers[desc.sid] = desc;
	  }
	}
      }
    } catch (Exception exc) {
      throw exc;
    }
    return a3config;
  }

  final public static boolean isTransient(short sid) {
    if (sid >= MIN_TRANSIENT_ID)
      return true;
    return false;
  }

  final static AgentId transientProxyId(short sid) {
    return transientServers[sid - MIN_TRANSIENT_ID].proxyId;
  }

  final public static short getServerId() {
    return serverId;
  }

  final static ServerDesc getServerDesc(short sid) {
    if (isTransient(sid))
      return transientServers[sid - MIN_TRANSIENT_ID];
    else
      return networkServers[sid];
  }

  /**
    * Get the host name of an agent server.
    *
    * @param id		agent server id
    * @return		server host name as declared in configuration file
    */
  final public static String getHostname(short sid) {
    return a3config.getHostname(sid);
  }

  final public static
  String getServiceArgs(short sid,
			String className) throws Exception {
    return a3config.getServiceArgs(sid, className);
  }

  final public static
  String getServiceArgsFamily(short sid,
			      String className) throws Exception {
    return a3config.getServiceArgsFamily(sid, className);
  }

  final public static
  String getServiceArgs(String hostname,
			String className) throws Exception {
    return a3config.getServiceArgs(hostname, className);
  }

  /**
   * Initializes this agent server. The <code>start</code> function is then
   * called to start this agent server execution. Between the <code>init</code>
   * and </code>start</code> calls, agents may be created and deployed, and
   * notifications may be sent using the <code>Channel</code>
   * <code>sendTo</code> function.
   *
   * @param sid		this agent server id
   * @param path	persistency directory
   *
   * @exception Exception
   *	unspecialized exception
   */
  public static void
  init(short sid, String path) throws Exception {
    serverId = sid;

    Debug.init(serverId);
    properties = new Properties(System.getProperties());

    // Get configuration and verify server id. validity
    // AF: For backward compability, try to read ".cfg" if needed.
    String cfgFileName = System.getProperty(A3Config.CFG_PROPERTY,
					    A3Config.DEFAULT_CFG_FILE);
    if (!cfgFileName.endsWith(".xml"))
      a3config = readConfig(cfgFileName);
    else
      a3config = A3Config.getConfig();

    networkServers = a3config.networkServers;
    transientServers = a3config.transientServers;
    try {
      services = getServerDesc(serverId).services;
    } catch (ArrayIndexOutOfBoundsException exc) {
      throw new ParseException("bad server id. #" + serverId, 0);
    }

    if (isTransient(serverId)) {
      transaction = new SimpleTransaction(path);
    } else {
      String tname = getProperty("Transaction", "JTransaction");
      if (tname.equals("NullTransaction"))
	transaction = new NullTransaction(path);
      else if (tname.equals("FSTransaction"))
	transaction = new FSTransaction(path);
      else if (tname.equals("ATransaction"))
	transaction = new ATransaction(path);
      else
	transaction = new JTransaction(path);

      // Restore matrix clock.
      mclock = MatrixClock.load();
      if (mclock == null) mclock = new MatrixClock(sid);
    }
    
    // Initialize AgentId class's variables.
    AgentId.init();

    // Creates message queues.
    mq = new Queue();
    if (! isTransient(serverId)) {
      qin = new MessageQueue();
      qout = new MessageQueue();
    }

    String nname = getProperty("Network", "SingleCnxNetwork");
    if (nname.equals("PoolCnxNetwork")) {
      network = new PoolCnxNetwork(mclock);
    } else {
      network = new SingleCnxNetwork(mclock);
    }

    // then restores all messages.
    String[] list = transaction.getList("@");
    for (int i=0; i<list.length; i++) {
      // There is never saved message in non TRANSACTION mode.
      Message msg = Message.load(list[i]);
      if (msg.to.to == serverId) {
	// The destination server is "local".
	if (msg.update.l == serverId) {
	  // It's an already delivered message.
	  qin.insert(msg);
	} else {
	  network.addRecvMessage(msg);
	}
      } else {
	// The destination server is "remote".
	qout.insert(msg);
      }
    }

    // initialize channel before initializing fixed agents
    channel = Channel.newInstance(mq, qin, qout, mclock);
    // Creates and initialize Engine (It must be done before any call to
    // Channel.sendTo method).
    engine = Engine.newInstance(mq, qin, qout);

    //  Initialize Agent component: load AgentFactory and all
    // fixed agents.
    Agent.init();

    isRunning = true;

    network.start(qin, qout);

    ProcessManager.init();

    Debug.trace("start agent server #" + serverId + ": " +
		getServerDesc(serverId) +
		" at " + new Date(), false);
  }

  /**
   * Parses agent server arguments and initializes agent server.
   *
   * @param args	lauching arguments
   * @return		number of arguments consumed in args
   *
   * @exception Exception
   *	unspecialized exception
   */
  public static int
  init(String args[]) throws Exception {
    short sid;
    if (args.length < 2)
      throw new ParseException("usage: java <main> sid storage", 0);
    try {
      sid = (short) Integer.parseInt(args[0]);
    } catch (NumberFormatException exc) {
      throw new ParseException("usage: java <main> sid storage", 0);
    }
    
    if (ADMINISTRED){
      String administred = System.getProperty("ADMINISTRED");
      if (administred == null)
	admin = true; // by default a server is administred
      else {
	administred = administred.trim();
	if ( administred.equalsIgnoreCase("yes") || administred.equalsIgnoreCase("true") || administred.equalsIgnoreCase("1")) admin = true;
	else if ( administred.equalsIgnoreCase("no") || administred.equalsIgnoreCase("false") || administred.equalsIgnoreCase("0")) admin = false;
	else throw new ParseException("ADMINISTRED parameters: yes|YES|no|NO|true|TRUE|false|FALSE|1|0", 0);
      }
    }
    
    String path = args[1];
    init(sid, path);
    return 2;
  }

  public static void
  start() throws IOException {
    transaction.begin();
    channel.dispatch();
    transaction.commit();
    // The transaction has commited, then validate the sending messages.
    if (qin != null)
      qin.validate();
    if (qout != null)
      qout.validate();
    transaction.release();

    Runtime.getRuntime().traceMethodCalls(Debug.traceMethodCalls);
    Runtime.getRuntime().traceInstructions(Debug.traceInstructions);

    // Now we can start the engine.
    engine.start();
  }

  public static void stop() {
    isRunning = false;
    engine.stop();
    network.stop();
  }
}
