/*
 * Copyright (C) 2001 - 2003 ScalAgent Distributed Technologies 
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
 */
package fr.dyade.aaa.agent.conf;

import java.io.*;
import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Transaction;
import fr.dyade.aaa.agent.Debug;
import fr.dyade.aaa.agent.AgentServer;

public class A3CMLConfig implements Serializable {
  /** use serialVersionUID for interoperability */
  private static final long serialVersionUID = -2497621374376654935L;

  static Logger logmon = null;

  /** Hashtable of all domains */
  public Hashtable domains = null;
  /** Hashtable of all servers (persitent and transient) */
  public Hashtable servers = null;
  /** Hashtable of all global properties */
  public Hashtable properties = null;

  public A3CMLConfig() {
    domains = new Hashtable();
    servers = new Hashtable();
    properties = new Hashtable();
  }

  /**
   * Adds a domain.
   *
   * @param domain 	The description of added domain.
   * @exception DuplicateDomainException
   *			If the domain already exist.
   */
  public final void addDomain(A3CMLDomain domain) throws DuplicateDomainException {
    if (domains.containsKey(domain.name))
      throw new DuplicateDomainException("Duplicate domain " + domain.name);
    domains.put(domain.name, domain);
  }

  /**
   * Removes a domain.
   *
   * @param name 	The domain name.
   * @return		The domain description if exist.
   * @exception UnknownDomainException
   *			If the domain don't exist.
   */
  public final A3CMLDomain removeDomain(String name) throws UnknownDomainException {
    A3CMLDomain domain = null;
    if (domains.containsKey(name))
      domain = (A3CMLDomain) domains.remove(name);
    else
      throw new UnknownDomainException("Unknown domain " + name);
    return domain;
  }

  /**
   * Returns true if it exists a domain with this name, false otherwise.
   *
   * @param name 	The domain name.
   * @return	 	True if the domain is declared, false otherwise.
   */
  public final boolean containsDomain(String name) {
    return domains.containsKey(name);
  }

  /**
   * Returns the description of a domain.
   *
   * @param name 	The domain name.
   * @return	 	The domain description if exist.
   * @exception UnknownDomainException
   *			If the domain don't exist.
   */
  public final A3CMLDomain getDomain(String name) throws UnknownDomainException {
    A3CMLDomain domain = (A3CMLDomain) domains.get(name);
    if (domain == null)
      throw new UnknownDomainException("Unknown domain " + name);
    return domain;
  }

  /**
   * Adds a server.
   *
   * @param server	The description of added server.
   * @exception DuplicateServerException
   *			If the server already exist.
   */
  public final void addServer(A3CMLServer server) throws DuplicateServerException {
    Short id = new Short(server.sid);
    if (servers.containsKey(id))
      throw new DuplicateServerException("Duplicate server id. #" + server.sid);
    servers.put(id, server);
  }
  
  /**
   * Removes a server.
   *
   * @param sid  	The unique server identifier.
   * @return	 	The server description if exists.
   * @exception UnknownServerException
   * 		 	If the server does not exist.
   */
  public final A3CMLServer removeServer(short sid) throws UnknownServerException {
    A3CMLServer server = null;
    Short id = new Short(sid);
    if (servers.containsKey(id))
      server = (A3CMLServer) servers.remove(id);
    else
      throw new UnknownServerException("Unknown server id. #" + sid);
    return server;
  }
  
  /**
   * Remove a server.
   *
   * @param name 	The server name.
   * @return	 	The server description if exists.
   * @exception UnknownServerException
   *			If the server does not exist.
   */
  public final A3CMLServer removeServer(String name) throws UnknownServerException {
    return removeServer(getServerIdByName(name));
  }
  
  /**
   * Returns true if the configuration contains a server with specified id.
   *
   * @param sid  server id
   * @return	 true if contain sid; false otherwise.
   */
  public final boolean containsServer(short sid) {
    return servers.containsKey(new Short(sid));
  }
  
  /**
   * Gets a server identifier from its name.
   *
   * @param name 	The server name.
   * @return	 	The server identifier.
   * @exception UnknownServerException
   * 		 	If the server does not exist.
   */
  public short getServerIdByName(String name) throws UnknownServerException {
    for (Enumeration s = servers.elements(); s.hasMoreElements(); ) {
      A3CMLServer server = (A3CMLServer) s.nextElement();
      if (server.name.equals(name)) return server.sid;
    }
    throw new UnknownServerException("Unknown server " + name);
  }

  /**
   * Returns true if the configuration contains a server with specified name.
   *
   * @param name server name
   * @return	 true if contain name; false otherwise.
   */
  public final boolean containsServer(String name) {
    try {
      getServerIdByName(name);
    } catch (UnknownServerException exc) {
      return false;
    }
    return true;
  }

  /**
   * Returns the description of a server.
   *
   * @param name 	The server identifier.
   * @return	 	The server description if exist.
   * @exception UnknownServerException
   * 		 	If the server does not exist.
   */
  public final A3CMLServer getServer(short sid) throws UnknownServerException {
    A3CMLServer server = (A3CMLServer) servers.get(new Short(sid));
    if (server == null)
      throw new UnknownServerException("Unknown server id. #" + sid);
    return server;
  }

  /**
   * Returns the description of a server.
   *
   * @param name 	The server name.
   * @return	 	The server description if exist.
   * @exception UnknownServerException
   * 		 	If the server does not exist.
   */
  public final A3CMLServer getServer(String name) throws UnknownServerException {
    for (Enumeration s = servers.elements(); s.hasMoreElements(); ) {
      A3CMLServer server = (A3CMLServer) s.nextElement();
      if (server.name.equals(name)) return server;
    }
    throw new UnknownServerException("Unknown server id for server " + name);
  }

  /**
   * Tests if the specified agent server is transient.
   *
   * @param id		agent server id
   * @return		true if the server is transient; false otherwise; 
   * @exception UnknownServerException
   *			The specified server does not exist.
   */
  public final boolean isTransient(short sid) throws UnknownServerException {
    A3CMLServer server = getServer(sid);

    if (server instanceof A3CMLPServer) {
      return false;
    } else if (server instanceof A3CMLTServer) {
      return true;
    } else {
      throw new UnknownServerException("Unknown type for server id. # " + sid);
    }
  }

  /**
   * Tests if the specified agent server is transient.
   *
   * @param name 	agent server name
   * @return	 	true if the server is transient; false otherwise; 
   * @exception  UnknownServerException
   *			The specified server does not exist.
   */
  public final boolean isTransient(String name) throws UnknownServerException {
    A3CMLServer server = getServer(name);

    if (server instanceof A3CMLPServer) {
      return false;
    } else if (server instanceof A3CMLTServer) {
      return true;
    } else {
      throw new UnknownServerException("Unknown type for server " + name);
    }
  }

  /**
   * add property
   *
   * @param prop A3CMLProperty
   * @return	 the previous value of the specified prop.name in
   *             this hashtable, or null if it did not have one.
   * @exception	Exception
   */
  public final A3CMLProperty addProperty(A3CMLProperty prop) throws Exception {
    return (A3CMLProperty) properties.put(prop.name, prop);
  }

  /**
   * remove property
   *
   * @param name property name
   * @return	 the value to which the name had been mapped in 
   *             this hashtable, or null if the name did not have a mapping.
   */
  public final A3CMLProperty removeProperty(String name) {
    return (A3CMLProperty) properties.remove(name);
  }

  /**
   * contains property
   *
   * @param name property name
   * @return	 true if contain name; false otherwise.
   */
  public final boolean containsProperty(String name) {
    return properties.containsKey(name);
  }

  /**
   * Returns the specified property.
   */
  public final A3CMLProperty getProperty(String name) {
    return (A3CMLProperty) properties.get(name);
  }

  /**
   * Get the JVM argument for a particular agent server identified by its id.
   *
   * @param id		agent server identifier.
   * @return		the arguments as declared in configuration file
   * @exception	UnknownServerException
   *			The specified server does not exist.
   */
  public final String getJvmArgs(short sid) throws UnknownServerException {
    A3CMLServer server = getServer(sid);
    return server.getJvmArgs();
  }
  
  /**
   * Get the JVM argument for a particular agent server identified by its name.
   *
   * @param name	agent server name.
   * @return		the arguments as declared in configuration file
   * @exception	UnknownServerException
   *			The specified server does not exist.
   */
  public final String getJvmArgs(String name) throws UnknownServerException {
    A3CMLServer server = getServer(name);
    return server.getJvmArgs();
  }

  /**
   * Get the argument strings for a particular service on a particular
   * agent server identified by its id.
   *
   * @param sid		agent server id.
   * @param classname	the service class name.
   * @return		the arguments as declared.
   * @exception	UnknownServerException
   *			The specified server does not exist.
   * @exception UnknownServiceException
   *			The specified service is not declared on this server. 
   */
  public final String
      getServiceArgs(short sid, String classname)
    throws UnknownServerException, UnknownServiceException {
    A3CMLServer server = getServer(sid);
    return server.getServiceArgs(classname);
  }

  /**
   * Get the argument strings for a particular service on a particular
   * agent server identified by its name.
   *
   * @param sid		agent server name.
   * @param classname	the service class name.
   * @return		the arguments as declared.
   * @exception	UnknownServerException
   *			The specified server does not exist.
   * @exception UnknownServiceException
   *			The specified service is not declared on this server. 
   */
  public final String
      getServiceArgs(String name, String classname)
    throws UnknownServerException, UnknownServiceException {
    A3CMLServer server = getServer(name);
    return server.getServiceArgs(classname);
  }

  /* -+-+-+-                                                -+-+-+- */
  /* -+-+-+- Code needed for configuration.                 -+-+-+- */
  /* -+-+-+-                                                -+-+-+- */

  /**
   *  Adapts the current configuration to the specified transient server.
   */
   public void configure(A3CMLTServer root) throws Exception {
    short rootid = root.sid;

    // Temporary fix, reset visited and gateway fields
    reset();

    // Gets the descriptor of gateway server.
    A3CMLPServer gateway = (A3CMLPServer) getServer(root.gateway);

    logmon.log(BasicLevel.DEBUG,
               "configure - gateway=" + gateway);

    // Search the listen port of proxy
    for (Enumeration n = gateway.networks.elements(); n.hasMoreElements();) {
      A3CMLNetwork network = (A3CMLNetwork) n.nextElement();

      logmon.log(BasicLevel.DEBUG,
                 "configure - explore(" + network + ")");
      if (network.domain.equals("transient"))
        gateway.port = network.port;
    }

    logmon.log(BasicLevel.DEBUG,
               "configure - gateway.port=" + gateway.port);

    if (gateway.port == -1)
      throw new Exception("There is no transient network on server #" +
			  root.gateway + ", bad configuration.");
  }

  /**
   *  Adapts the current configuration to the specified persistent server.
   */
  public void configure(A3CMLPServer root) throws Exception {
    short rootid = root.sid;
    Vector toExplore = new Vector();

    // Temporary fix, reset visited and gateway fields
    reset();
    
    // TODO: Adds the local domain
//  Domain domain = new Domain("local"
    // Search alls directly accessible domains (except transient).
    for (Enumeration n = root.networks.elements(); n.hasMoreElements();) {
      A3CMLNetwork network = (A3CMLNetwork)  n.nextElement();
      if (network.domain.equals("transient")) {
        // Adds the transient domain.
        // AF: Normally, the domain should be added during creation phase (from
        // XML file or from AgentAdmin), but as all transient domain are named
        // "transient" and at this moment we don't known the root server we 
        // can't do this, so we have a "patch" here.
        // TODO: creates server.transient domain then handle them...
        if (! containsDomain("transient")) {
          A3CMLDomain domain = new A3CMLDomain(network.domain,
                                               "fr.dyade.aaa.agent.TransientNetworkProxy");
          addDomain(domain);
        }
        // Transient network is handled later
        continue;
      }

      A3CMLDomain domain = (A3CMLDomain) domains.get(network.domain);
      domain.gateway = rootid;
      toExplore.addElement(domain);

      logmon.log(BasicLevel.DEBUG,
                 "configure - toExplore.add(" + domain + ")");
    }
    
    root.visited = true;
    root.gateway = -1;
    root.domain = "local";

    while (toExplore.size() > 0) {
      A3CMLDomain domain = (A3CMLDomain) toExplore.elementAt(0);
      toExplore.removeElementAt(0);
      A3CMLPServer gateway = (A3CMLPServer) servers.get(new Short(domain.gateway));

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "configure - explore(" + domain + ")");

      // Parse all nodes of this domain
      for (Enumeration s = domain.servers.elements();
	   s.hasMoreElements();) {
	A3CMLPServer server = (A3CMLPServer) s.nextElement();

        if (server.visited) continue;

        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, "configure - explore(" + server + ")");

        server.visited = true;
        if (domain.gateway == rootid) {
          // The server is directly accessible from root
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

          if (network.domain.equals("transient")) {
            // A transient server is never a router so it is not
            // necessary to explore these servers

            continue;
          }

          A3CMLDomain d2 = (A3CMLDomain) domains.get(network.domain);

          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG, "configure - parse(" + d2 + ")");

          if (d2 == domain) {
            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG, "configure - setPort(" + network.port + ")");
            // The server is directly accessible from root server by
            // this network interface; fixes the communication port
            // for this server.
            server.port = network.port;
            continue;
          }

          // If the domain is already explored then there is more
          // than one route to this domain.
          if (d2.gateway != -1)
            throw new Exception("more than one route to: " + domain);

          // The domain is not already explored.
          if (server.gateway == -1)
            d2.gateway = server.sid;	 // the server is directly accessible
          else
            d2.gateway = server.gateway; // the server itself is routed
          toExplore.addElement(d2);

          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG, "configure - toExplore.add(" + d2 + ")");
        }
      }
    }

    // verify that all declared servers are accessible
    for (Enumeration s = servers.elements(); s.hasMoreElements();) {
      A3CMLServer server = (A3CMLServer) s.nextElement();
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "configure - verify " + server);
      if (server instanceof A3CMLPServer) {
        if (! server.visited)
          throw new Exception(server + " inaccessible");
      } else if (server instanceof A3CMLTServer) {
        A3CMLServer router = getServer(server.gateway);
        if (! router.visited)
          throw new Exception(server + "/" + router + " inaccessible");
        if (! (router instanceof A3CMLPServer))
          throw new Exception("transient " + router +
                              " can't route transient " + server);
        A3CMLNetwork network = null;
        for (Enumeration n = ((A3CMLPServer) router).networks.elements();
             n.hasMoreElements();) {
          network = (A3CMLNetwork)  n.nextElement();
          if (network.domain.equals("transient")) break;
        }
        if (network == null)
          throw new Exception("server " + router +
                              " can't route transient " + server);
        // Adds transient servers to the domain list
        if (router.sid == rootid) {
          getDomain(network.domain).addServer(server);
        }
        // set visited value for transient servers 
        server.visited = true;
      }
    }
  }

  /**
   * Gets configuration of agent servers by a domain from a Config object. 
   * This method fills the object graph configuration in the <code>Config</code>
   * object.
   *
   * @param domainName  domain name
   * @return	        the <code>Config</code> object if file exists and is
   * 		        correct, null otherwise.
   *
   * @exception Exception
   *	unspecialized exception when reading and parsing the configuration file
   */
  public A3CMLConfig getDomainConfig(String domainName) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "Config.getDomainConfig(" + domainName + ")");

    A3CMLConfig domainConf = new A3CMLConfig();

    // add domain "domainName" in domainConf.
    A3CMLDomain dom = getDomain(domainName).duplicate();
    domainConf.addDomain(dom);
    
    // add persistent server in domainConf.
    for (int i = 0; i < dom.servers.size(); i++) {
      A3CMLPServer server = (A3CMLPServer) dom.servers.elementAt(i);
      domainConf.servers.put(new Short(server.sid),server);
    }

    // add transient server in domainConf.
    for (Enumeration s1 = domainConf.servers.elements(); s1.hasMoreElements(); ) {
      A3CMLServer server = (A3CMLServer) s1.nextElement();
      for (Enumeration s2 = servers.elements(); s2.hasMoreElements(); ) {
        A3CMLServer serv = (A3CMLServer) s2.nextElement();
        if (serv instanceof A3CMLTServer) {
          if (server.sid == ((A3CMLTServer) serv).gateway) {
            domainConf.servers.put(new Short(serv.sid),
                                   ((A3CMLTServer) serv).duplicate());
          }
        }
      }
    }

    // add global properties in domainConf.
    for (Enumeration p = properties.elements(); p.hasMoreElements(); ) {
      A3CMLProperty property = (A3CMLProperty) p.nextElement();
      domainConf.addProperty(((A3CMLProperty) property).duplicate());
    }

    try {
      // for Admin Domain
      // add domain "ADMIN_DOMAIN" in domainConf.
      A3CMLDomain d0 = getDomain(AgentServer.ADMIN_DOMAIN);
      domainConf.addDomain(new A3CMLDomain(d0.name,d0.network));
      A3CMLPServer s0 = (A3CMLPServer) domainConf.getServer(AgentServer.getServerId());
      d0 = domainConf.getDomain(AgentServer.ADMIN_DOMAIN);
      d0.addServer(s0);
      for (int i = 0; i < s0.networks.size(); ) {
        A3CMLNetwork network = (A3CMLNetwork) s0.networks.elementAt(i);
        if (!(network.domain.equals(AgentServer.ADMIN_DOMAIN) || 
              network.domain.equals(domainName))) {
          s0.networks.removeElement(network);
        } else
          i++;
      }
    } catch (UnknownServerException exc) {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "", exc);
    }

    return domainConf;
  }

  /**
   * Gets configuration of agent servers by a list of domain from a Config object. 
   * This method fills the object graph configuration in the <code>Config</code>
   * object.
   *
   * @param domainName  list of domain name
   * @return	        the <code>Config</code> object if file exists and is
   * 		        correct, null otherwise.
   *
   * @exception Exception
   *	unspecialized exception when reading and parsing the configuration file
   */
  public A3CMLConfig getDomainConfig(String[] listDomainName) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "Config.getDomainConfig(" + listDomainName + ")");

    Hashtable context = new Hashtable();

    A3CMLConfig domainConf = new A3CMLConfig();
    Vector domainList = new Vector();
    for (int i = 0; i < listDomainName.length; i++)
      domainList.addElement(listDomainName[i]);
    
    for (int n = 0; n < listDomainName.length; n++) {
      String domainName = listDomainName[n];

      // add domain "domainName" in domainConf.
      A3CMLDomain dom = getDomain(domainName).duplicate(context);
      domainConf.addDomain(dom);
      
      // add persistent server in domainConf.
      for (int i = 0; i < dom.servers.size(); i++) {
        A3CMLPServer server = (A3CMLPServer) dom.servers.elementAt(i);
        for (int j = 0; j < server.networks.size(); ) {
          A3CMLNetwork network = (A3CMLNetwork) server.networks.elementAt(j);
          if (!(network.domain.equals(AgentServer.ADMIN_DOMAIN) ||
                network.domain.equals("transient") || 
                domainList.contains(network.domain))) {
            server.networks.removeElement(network);
          } else
            j++;
        }
        domainConf.servers.put(new Short(server.sid),server);
      }
      
      // add transient server in domainConf.
      for (Enumeration s1 = domainConf.servers.elements(); s1.hasMoreElements(); ) {
        A3CMLServer server = (A3CMLServer) s1.nextElement();
        for (Enumeration s2 = servers.elements(); s2.hasMoreElements(); ) {
          A3CMLServer serv = (A3CMLServer) s2.nextElement();
          if (serv instanceof A3CMLTServer) {
            if (server.sid == serv.gateway) {
              domainConf.servers.put(new Short(serv.sid),
                                     ((A3CMLTServer) serv).duplicate());
            }
          }
        }
      }
    }
      
    // add global properties in domainConf.
    for (Enumeration p = properties.elements(); p.hasMoreElements(); ) {
      A3CMLProperty property = (A3CMLProperty) p.nextElement();
      domainConf.addProperty(((A3CMLProperty) property).duplicate());
    }
    
    try {
      // for Admin Domain
      // add domain "ADMIN_DOMAIN" in domainConf.
      A3CMLDomain d0 = getDomain(AgentServer.ADMIN_DOMAIN);
      domainConf.addDomain(new A3CMLDomain(d0.name,d0.network));
      A3CMLPServer s0 = (A3CMLPServer) domainConf.getServer(AgentServer.getServerId());
      d0 = domainConf.getDomain(AgentServer.ADMIN_DOMAIN);
      d0.addServer(s0);
      for (int i = 0; i < s0.networks.size(); ) {
        A3CMLNetwork network = (A3CMLNetwork) s0.networks.elementAt(i);
        if (!(network.domain.equals(AgentServer.ADMIN_DOMAIN) || 
              domainList.contains(network.domain))) {
          s0.networks.removeElement(network);
        } else
          i++;
      }
    } catch (UnknownServerException exc) {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "", exc);
    }

    return domainConf;
  }
  
  /**
   * save configuration of agent servers (Config)
   * in a serialized file.
   *
   * @exception IOException
   * @see AgentServer.DEFAULT_SER_CFG_FILE
   */
  public void save() throws IOException {
    // Get the logging monitor from current server MonologMonitorFactory
    if (logmon == null)
      logmon =  Debug.getLogger("fr.dyade.aaa.agent.Admin");

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "Config.save(" + this + ")");

    AgentServer.getTransaction().save(this, AgentServer.DEFAULT_SER_CFG_FILE);
  }
    
  /**
   * read object from a serialized file,
   * in cfgDir if null, search object in 
   * path used to load classes
   *
   * @param cfgDir        read obj in this directory
   * @param cfgFileName   serialized file name
   * @exception           Exception
   */
  public static A3CMLConfig load() throws Exception {
    // Get the logging monitor from current server MonologMonitorFactory
    if (logmon == null)
      logmon =  Debug.getLogger("fr.dyade.aaa.agent.Admin");
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "Config.load()");
    
    A3CMLConfig a3config = (A3CMLConfig) AgentServer.getTransaction().load(AgentServer.DEFAULT_SER_CFG_FILE);

    if (a3config == null) {
      logmon.log(BasicLevel.WARN,
                 "Unable to find configuration file.");
      throw new IOException("Unable to find configuration file .");
    }
    
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "Config.load : a3cmlconfig = " + a3config);
    return a3config;
  }

  /**
   * Gets a <code>A3CMLConfig</code> serialialized object from file.
   *
   * @param path   path of serialized configuration file
   * @return	   the <code>A3CMLConfig</code> object if file exists and is
   * 		   correct, null otherwise.
   *
   * @exception Exception
   *	unspecialized exception when reading and parsing the configuration file
   */
  public static A3CMLConfig getConfig(String path) throws Exception {
    // Get the logging monitor from current server MonologMonitorFactory
    if (logmon == null)
      logmon =  Debug.getLogger("fr.dyade.aaa.agent.Admin");

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "Config.load(" + path + ")");
    
    A3CMLConfig a3config = null;
    
    File cfgFile = new File(path);
    if (cfgFile.exists() && cfgFile.isFile()) {
      if ((cfgFile.length() == 0)) {
        logmon.log(BasicLevel.ERROR,
                   " \"" + cfgFile.getPath() + "\", is empty.");
        throw new IOException(" \"" + cfgFile.getPath() + "\", is empty.");
      }
      
      FileInputStream fis = null;
      try {
        fis = new FileInputStream(cfgFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        a3config = (A3CMLConfig) ois.readObject();
      } catch (Exception exc) {
        logmon.log(BasicLevel.WARN, "Can't load configuration: " + path, exc);
      } finally {
        if (fis != null) fis.close();
      }

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   "Config.load : a3cmlconfig = " + a3config);
      return a3config;
    }

    //search a3config in path used to load classes.
    ClassLoader classLoader = null;
    InputStream is = null;
    try {
      classLoader = A3CMLConfig.class.getClassLoader();
      if (classLoader != null) {
        logmon.log(BasicLevel.WARN,
                   "Trying to find [" + path + "] using " +
                   classLoader + " class loader.");
        is = classLoader.getResourceAsStream(path);
      }
    } catch(Throwable t) {
      logmon.log(BasicLevel.WARN,
                 "Can't find [" + path + "] using " +
                 classLoader + " class loader.", t);
      is = null;
    }
    if (is == null) {
      // Last ditch attempt: get the resource from the system class path.
      logmon.log(BasicLevel.WARN,
                 "Trying to find serialized config using ClassLoader.getSystemResource().");
      is = ClassLoader.getSystemResourceAsStream(path);
    }
    if (is != null) {
      ObjectInputStream ois = new ObjectInputStream(is);
      a3config = (A3CMLConfig) ois.readObject();
    }

    if (a3config == null) {
      logmon.log(BasicLevel.WARN,
                 "Unable to find configuration file: " + path);
      throw new IOException("Unable to find configuration file: " + path);
    }
    
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "Config.load : a3cmlconfig = " + a3config);
    return a3config;
  }

// ### save
//     Transaction transaction = AgentServer.getTransaction();
//     if (transaction != null) {
//       // use transaction to save this obj
//       if (logmon.isLoggable(BasicLevel.DEBUG))
//         logmon.log(BasicLevel.DEBUG,
//                    "Config.save with AgentServer.transaction");
// //     AgentServer.getTransaction().save(obj,cfgDir,cfgFileName);
//     } else {
//       if (logmon.isLoggable(BasicLevel.DEBUG))
//         logmon.log(BasicLevel.DEBUG,
//                    "Config.save without transaction");
//       File file = new File(cfgDir, cfgFileName);
//       File temp = new File(cfgDir, cfgFileName+"_temp");
      
//       if (cfgDir != null) {
//         File dir = new File(cfgDir);
//         if (!dir.exists()) dir.mkdir();
//       }
      
//       if (file.exists())
//         file.renameTo(temp);
      
//       // Save the current state of the object.
//       FileOutputStream fos = null;
//       try {
//         fos = new FileOutputStream(file);
//         ObjectOutputStream oos = new ObjectOutputStream(fos);
//         oos.writeObject(obj);
//         oos.flush();
//         fos.getFD().sync();
//         fos.close();
//         fos = null;
//         temp.delete();
//         temp = null;
//       } finally {
//         if (fos != null) fos.close();
//       }
//     }

//     if (logmon.isLoggable(BasicLevel.DEBUG)) {
//       try {
//         A3CML.toXML(this, null, "debugServers.xml");
//       } catch (Exception exc) {}
//     }

  public String toString() {
    StringBuffer strBuf = new StringBuffer();
    strBuf.append("(").append(super.toString());
    strBuf.append(",properties=").append(properties);
    strBuf.append(",domains=").append(domains);
    strBuf.append(",servers=").append(servers);
    strBuf.append(")");

    return strBuf.toString();
  }
  
  /* -+-+-+-                                                -+-+-+- */
  /* -+-+-+- This code below is needed for historic reason. -+-+-+- */
  /* -+-+-+- It is used in mediation chain in order to find -+-+-+- */
  /* -+-+-+- the corresponding TcpServer services.          -+-+-+- */
  /* -+-+-+- This code below is needed for historic reason. -+-+-+- */
  /* -+-+-+-                                                -+-+-+- */

  /**
   * Get the argument strings for a particular service running on a server
   * (identified by its id.) and on associated transient servers.
   *
   * @param id		agent server id
   * @param classname	the service class name
   * @return		the arguments as declared in configuration file
   * @exception	UnknownServerException
   *	The specified server does not exist.
   * @exception UnknownServiceException
   *	The specified service is not declared on this server. 
   */
  public final String getServiceArgsFamily(short sid,
                                           String classname) 
    throws UnknownServerException, UnknownServiceException {
    try {
      String args = getServiceArgs(sid, classname);
      return args;
    } catch (UnknownServiceException exc) {}
    for (Enumeration s = servers.elements(); s.hasMoreElements(); ) {
      A3CMLServer server = (A3CMLServer) s.nextElement();

      if ((server instanceof A3CMLTServer) &&
	  (((A3CMLTServer) server).gateway == sid)) {
	try {
	  String args = getServiceArgs(server.sid, classname);
	  return args;
	} catch (Exception exc) {}
      }
    }
    throw new UnknownServiceException("Unknown service \"" + classname +
                                      "\" on family server#" + sid);
  }

  /**
   * Gets the argument strings for a particular service running on a server
   * identified by its host (searchs on all servers and associated transient).
   *
   * @param hostname	hostname
   * @param className	the service class name
   * @return		the arguments as declared in configuration file
   * @exception UnknownServiceException
   *	The specified service is not declared on this server. 
   */
  public final String getServiceArgsHost(String hostname,
                                         String classname) throws Exception {
    for (Enumeration s = servers.elements(); s.hasMoreElements(); ) {
      A3CMLServer server = (A3CMLServer) s.nextElement();
      if (server.hostname.equals(hostname)) {
	try {
	  String args = getServiceArgs(server.sid, classname);
	  return args;
	} catch (Exception exc) {}
      }
    }
    throw new UnknownServiceException("Unknown service \"" + classname +
                                      "\" on host " + hostname);
  }

  public boolean equals(Object obj) {
    if (obj == null) return false;

    if (obj instanceof A3CMLConfig) {
      A3CMLConfig config = (A3CMLConfig) obj;

      if (domains.equals(config.domains) &&
          servers.equals(config.servers) &&
          properties.equals(config.properties))
        return true;
    }
    return false;
  }

  /**
   * reset visited and gateway fields.
   */
  public void reset() {
    for (Enumeration s = servers.elements(); s.hasMoreElements(); ) {
      A3CMLServer server = (A3CMLServer) s.nextElement();
      server.visited = false;
      if (server instanceof A3CMLPServer)
        // don't reset gateway attribute for transient server (set
        // in initialization).
        server.gateway = (short) -1;
    }
    for (Enumeration d = domains.elements(); d.hasMoreElements(); ) {
      A3CMLDomain domain = (A3CMLDomain) d.nextElement();
      domain.gateway = (short) -1;
    }
  }
}
