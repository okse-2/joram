/*
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies 
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentServer;

public class A3CMLConfig implements Serializable {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** Hashtable of all domains */
  public Hashtable<String, A3CMLDomain> domains = null;
  /** Hashtable of all servers (persistent and transient) */
  public Hashtable<Short, A3CMLServer> servers = null;
  /** Hashtable of all global properties */
  public Hashtable<String, A3CMLProperty> properties = null;

  public A3CMLConfig() {
    domains = new Hashtable<String, A3CMLDomain>();
    servers = new Hashtable<Short, A3CMLServer>();
    properties = new Hashtable<String, A3CMLProperty>();
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
    
    for (Enumeration<A3CMLServer> s = servers.elements(); s.hasMoreElements(); ) {
      A3CMLServer serv = s.nextElement();
      if (serv.name.equals(server.name))
        throw new DuplicateServerException("Duplicate server name. " + server.name);
    }
    
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
    if (servers.containsKey(id)) {
      server = (A3CMLServer) servers.remove(id);
    } else {
      throw new UnknownServerException("Unknown server id. #" + sid);
    }

    for (int i = 0; i < server.networks.size(); i++) {
      A3CMLNetwork network = (A3CMLNetwork)server.networks.elementAt(i);
      A3CMLDomain domain = (A3CMLDomain)domains.get(network.domain);
      domain.removeServer(sid);
    }

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
    for (Enumeration<A3CMLServer> s = servers.elements(); s.hasMoreElements(); ) {
      A3CMLServer server = s.nextElement();
      if (server.name.equals(name)) return server.sid;
    }
    throw new UnknownServerException("Unknown server " + name);
  }

  /**
   * Gets a server name from its identifier.
   *
   * @param name  The server identifier.
   * @return    The server name.
   * @exception UnknownServerException
   *      If the server does not exist.
   */
  public String getServerNameById(short sid) throws UnknownServerException {
    return getServer(sid).name;
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
   * @param sid 	The server identifier.
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
    for (Enumeration<A3CMLServer> s = servers.elements(); s.hasMoreElements(); ) {
      A3CMLServer server = s.nextElement();
      if (server.name.equals(name)) return server;
    }
    throw new UnknownServerException("Unknown server id for server " + name);
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
   * Returns the specified property.
   */
  public final A3CMLProperty getProperty(String name, short sid) 
      throws Exception {
    A3CMLServer server = getServer(sid);
    return server.getProperty(name);
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
   *  Adapts the current configuration to the specified persistent server.
   */
  public void configure(A3CMLServer root) throws Exception {
    short rootid = root.sid;
    Vector<A3CMLDomain> toExplore = new Vector<A3CMLDomain>();

    // Temporary fix, reset visited and gateway fields
    reset();

    // Search alls directly accessible domains.
    for (Enumeration<A3CMLNetwork> n = root.networks.elements(); n.hasMoreElements();) {
      A3CMLNetwork network = n.nextElement();

      A3CMLDomain domain = domains.get(network.domain);
      domain.gateway = rootid;
      domain.hops = 1;
      toExplore.addElement(domain);

      Log.logger.log(BasicLevel.DEBUG, "configure - toExplore.add(" + domain + ")");
    }

    root.visited = true;
    root.gateway = -1;
    root.hops = 0;
    root.domain = "local";

    while (toExplore.size() > 0) {
      A3CMLDomain domain = (A3CMLDomain) toExplore.elementAt(0);
      toExplore.removeElementAt(0);
      A3CMLServer gateway = (A3CMLServer) servers.get(new Short(domain.gateway));

      if (Log.logger.isLoggable(BasicLevel.DEBUG))
        Log.logger.log(BasicLevel.DEBUG, "configure - explore(" + domain + ")");

      // Parse all nodes of this domain
      for (Enumeration<A3CMLServer> s = domain.servers.elements(); s.hasMoreElements();) {
        A3CMLServer server = s.nextElement();

        if (server.visited) continue;

        if (Log.logger.isLoggable(BasicLevel.DEBUG))
          Log.logger.log(BasicLevel.DEBUG, "configure - explore(" + server + ")");

        server.visited = true;
        if (domain.gateway == rootid) {
          // The server is directly accessible from root
          server.gateway = -1;
          server.domain = domain.name;
        } else {
          server.gateway = domain.gateway;
          server.domain = gateway.domain;
        }
        server.hops = domain.hops;

        // If the server is a router then add the accessible domains
        // to the list.
        for (Enumeration<A3CMLNetwork> n = server.networks.elements(); n.hasMoreElements();) {
          A3CMLNetwork network = n.nextElement();
          A3CMLDomain d2 = domains.get(network.domain);

          if (Log.logger.isLoggable(BasicLevel.DEBUG))
            Log.logger.log(BasicLevel.DEBUG, "configure - parse(" + d2 + ")");

          if (d2 == domain) {
            if (Log.logger.isLoggable(BasicLevel.DEBUG))
              Log.logger.log(BasicLevel.DEBUG, "configure - setPort(" + network.port + ")");
            // The server is directly accessible from root server by
            // this network interface; fixes the communication port
            // for this server.

            // AF 03/11/2004 - It seems in fact the domain is the one we are
            // exploring, so if the server is directly accessible its listen
            // port is the one of this network...
            server.port = network.port;
            continue;
          }

          // If the domain is already explored then there is more
          // than one route to this domain.

          //if (d2.gateway != -1)
          // throw new Exception("more than one route to: " + domain);

          // if (d2.hops != -1)
          //             throw new Exception("more than one route to: " + domain);
          d2.hops = domain.hops +1;

          // The domain is not already explored.
          if (server.gateway == -1)
            d2.gateway = server.sid;	 // the server is directly accessible
          else
            d2.gateway = server.gateway; // the server itself is routed
          toExplore.addElement(d2);

          if (Log.logger.isLoggable(BasicLevel.DEBUG))
            Log.logger.log(BasicLevel.DEBUG, "configure - toExplore.add(" + d2 + ")");
        }
      }
    }

    // verify that all declared servers are accessible
    for (Enumeration<A3CMLServer> s = servers.elements(); s.hasMoreElements();) {
      A3CMLServer server = s.nextElement();
      if (Log.logger.isLoggable(BasicLevel.DEBUG))
        Log.logger.log(BasicLevel.DEBUG, "configure - verify " + server);
      if (! server.visited)
        throw new Exception(server + " inaccessible");
    }

    // Search alls directly accessible domains, then set special routes
    // for HttpNetworks.
    for (Enumeration<A3CMLNetwork> n = root.networks.elements(); n.hasMoreElements();) {
      A3CMLNetwork network = n.nextElement();
      A3CMLDomain domain = domains.get(network.domain);

      if (("fr.dyade.aaa.agent.HttpNetwork".equals(domain.network)) ||
          ("fr.dyade.aaa.agent.HttpsNetwork".equals(domain.network))) {

        // First search for the listen server..
        short router = -1;
        for (int i=0; i<domain.servers.size(); i++) {
          A3CMLServer server = (A3CMLServer) domain.servers.elementAt(i);
          if ((server.port > 0) && (server.sid != rootid)) {
            router = server.sid;
            break;
          }
        }
        // .. then set the gateway for all clients.
        if (router != -1) {
          for (int i=0; i<domain.servers.size(); i++) {
            A3CMLServer server = (A3CMLServer) domain.servers.elementAt(i);
            if ((server.sid != router) && (server.sid != rootid))
              server.gateway = router;
          }
        }
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
    if (Log.logger.isLoggable(BasicLevel.DEBUG))
      Log.logger.log(BasicLevel.DEBUG,
                     "Config.getDomainConfig(" + domainName + ")");

    A3CMLConfig domainConf = new A3CMLConfig();

    // add domain "domainName" in domainConf.
    A3CMLDomain dom = getDomain(domainName).duplicate();
    domainConf.addDomain(dom);
    
    // add persistent server in domainConf.
    for (int i = 0; i < dom.servers.size(); i++) {
      A3CMLServer server = (A3CMLServer) dom.servers.elementAt(i);
      domainConf.servers.put(new Short(server.sid),server);
    }

    // add global properties in domainConf.
    for (Enumeration<A3CMLProperty> p = properties.elements(); p.hasMoreElements(); ) {
      A3CMLProperty property = p.nextElement();
      domainConf.addProperty(property.duplicate());
    }

    try {
      // for Admin Domain
      // add domain "ADMIN_DOMAIN" in domainConf.
      A3CMLDomain d0 = getDomain(AgentServer.ADMIN_DOMAIN);
      domainConf.addDomain(new A3CMLDomain(d0.name,d0.network));
      A3CMLServer s0 = domainConf.getServer(AgentServer.getServerId());
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
      if (Log.logger.isLoggable(BasicLevel.DEBUG))
        Log.logger.log(BasicLevel.DEBUG, "", exc);
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
    if (Log.logger.isLoggable(BasicLevel.DEBUG))
      Log.logger.log(BasicLevel.DEBUG,
                     "Config.getDomainConfig(" + listDomainName + ")");

    Hashtable<Short, A3CMLServer> context = new Hashtable<Short, A3CMLServer>();

    A3CMLConfig domainConf = new A3CMLConfig();
    Vector<String> domainList = new Vector<String>();
    for (int i = 0; i < listDomainName.length; i++)
      domainList.addElement(listDomainName[i]);
    
    for (int n = 0; n < listDomainName.length; n++) {
      String domainName = listDomainName[n];

      // add domain "domainName" in domainConf.
      A3CMLDomain dom = getDomain(domainName).duplicate(context);
      domainConf.addDomain(dom);
      
      // add persistent server in domainConf.
      for (int i = 0; i < dom.servers.size(); i++) {
        A3CMLServer server = (A3CMLServer) dom.servers.elementAt(i);
        for (int j = 0; j < server.networks.size(); ) {
          A3CMLNetwork network = (A3CMLNetwork) server.networks.elementAt(j);
          if (!(network.domain.equals(AgentServer.ADMIN_DOMAIN) ||
                network.domain.equals("transient") || 
                domainList.contains(network.domain))) {
            server.networks.removeElement(network);
          } else
            j++;
        }
        domainConf.servers.put(new Short(server.sid), server);
      }
    }
      
    // add global properties in domainConf.
    for (Enumeration<A3CMLProperty> p = properties.elements(); p.hasMoreElements(); ) {
      A3CMLProperty property = p.nextElement();
      domainConf.addProperty(property.duplicate());
    }
    
    try {
      // for Admin Domain
      // add domain "ADMIN_DOMAIN" in domainConf.
      A3CMLDomain d0 = getDomain(AgentServer.ADMIN_DOMAIN);
      domainConf.addDomain(new A3CMLDomain(d0.name,d0.network));
      A3CMLServer s0 = domainConf.getServer(AgentServer.getServerId());
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
      if (Log.logger.isLoggable(BasicLevel.DEBUG))
        Log.logger.log(BasicLevel.DEBUG, "", exc);
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
    if (Log.logger.isLoggable(BasicLevel.DEBUG))
      Log.logger.log(BasicLevel.DEBUG, "Config.save(" + this + ")");

    AgentServer.getTransaction().save(this, AgentServer.DEFAULT_SER_CFG_FILE);
  }
    
  /**
   * read object from a serialized file.
   *
   * @exception           Exception
   */
//  public static A3CMLConfig load(File dir) throws Exception {
  public static A3CMLConfig load() throws Exception {
    if (Log.logger.isLoggable(BasicLevel.DEBUG))
      Log.logger.log(BasicLevel.DEBUG, "Config.load()");
    
    A3CMLConfig a3config = (A3CMLConfig) AgentServer.getTransaction().load(AgentServer.DEFAULT_SER_CFG_FILE);
        
//    File file;
//    file = new File(dir, AgentServer.DEFAULT_SER_CFG_FILE);
//    FileInputStream fis = new FileInputStream(file);
//    byte[] buf = new byte[(int) file.length()];
//    for (int nb = 0; nb < buf.length;) {
//      int ret = fis.read(buf, nb, buf.length - nb);
//      if (ret == -1)
//        throw new EOFException();
//      nb += ret;
//    }
//    fis.close();
//    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buf));
//    A3CMLConfig a3config = (A3CMLConfig) ois.readObject();

    if (a3config == null) {
      Log.logger.log(BasicLevel.WARN,
                     "Unable to find configuration file.");
      throw new IOException("Unable to find configuration file .");
    }
    
    if (Log.logger.isLoggable(BasicLevel.DEBUG))
      Log.logger.log(BasicLevel.DEBUG, "Config.load : a3cmlconfig = " + a3config);
    return a3config;
  }

  /**
   * Gets a <code>A3CMLConfig</code> serialized object from file.
   *
   * @param path   path of serialized configuration file
   * @return	   the <code>A3CMLConfig</code> object if file exists and is
   * 		   correct, null otherwise.
   *
   * @exception Exception
   *	unspecialized exception when reading and parsing the configuration file
   */
  public static A3CMLConfig getConfig(String path) throws Exception {
    if (Log.logger.isLoggable(BasicLevel.DEBUG))
      Log.logger.log(BasicLevel.DEBUG, "Config.load(" + path + ")");
    
    A3CMLConfig a3config = null;
    
    File cfgFile = new File(path);
    if (cfgFile.exists() && cfgFile.isFile()) {
      if ((cfgFile.length() == 0)) {
        Log.logger.log(BasicLevel.ERROR,
                       " \"" + cfgFile.getPath() + "\", is empty.");
        throw new IOException(" \"" + cfgFile.getPath() + "\", is empty.");
      }
      
      FileInputStream fis = null;
      try {
        fis = new FileInputStream(cfgFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        a3config = (A3CMLConfig) ois.readObject();
      } catch (Exception exc) {
        Log.logger.log(BasicLevel.WARN, "Can't load configuration: " + path, exc);
      } finally {
        if (fis != null) fis.close();
      }

      if (Log.logger.isLoggable(BasicLevel.DEBUG))
        Log.logger.log(BasicLevel.DEBUG,
                       "Config.load : a3cmlconfig = " + a3config);
      return a3config;
    }

    //search a3config in path used to load classes.
    ClassLoader classLoader = null;
    InputStream is = null;
    try {
      classLoader = A3CMLConfig.class.getClassLoader();
      if (classLoader != null) {
        Log.logger.log(BasicLevel.INFO,
                       "Trying to find [" + path + "] using " +
                       classLoader + " class loader.");
        is = classLoader.getResourceAsStream(path);
      }
    } catch(Throwable t) {
      Log.logger.log(BasicLevel.WARN,
                     "Can't find [" + path + "] using " +
                     classLoader + " class loader.", t);
      is = null;
    }
    if (is == null) {
      // Last ditch attempt: get the resource from the system class path.
      Log.logger.log(BasicLevel.INFO,
                     "Trying to find serialized config using ClassLoader.getSystemResource().");
      is = ClassLoader.getSystemResourceAsStream(path);
    }
    if (is != null) {
      ObjectInputStream ois = new ObjectInputStream(is);
      a3config = (A3CMLConfig) ois.readObject();
    }

    if (a3config == null) {
      Log.logger.log(BasicLevel.INFO,
                     "Unable to find configuration file: " + path);
      throw new IOException("Unable to find configuration file: " + path);
    }
    
    if (Log.logger.isLoggable(BasicLevel.DEBUG))
      Log.logger.log(BasicLevel.DEBUG, "Config.load : a3cmlconfig = " + a3config);
    return a3config;
  }

// ### save
//     Transaction transaction = AgentServer.getTransaction();
//     if (transaction != null) {
//       // use transaction to save this obj
//       if (Log.logger.isLoggable(BasicLevel.DEBUG))
//         Log.logger.log(BasicLevel.DEBUG,
//                    "Config.save with AgentServer.transaction");
// //     AgentServer.getTransaction().save(obj,cfgDir,cfgFileName);
//     } else {
//       if (Log.logger.isLoggable(BasicLevel.DEBUG))
//         Log.logger.log(BasicLevel.DEBUG,
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

//     if (Log.logger.isLoggable(BasicLevel.DEBUG)) {
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
    for (Enumeration<A3CMLServer> s = servers.elements(); s.hasMoreElements(); ) {
      A3CMLServer server = s.nextElement();
      if (server.hostname.equals(hostname)) {
        try {
          String args = getServiceArgs(server.sid, classname);
          return args;
        } catch (Exception exc) {}
      }
    }
    throw new UnknownServiceException("Unknown service \"" + classname + "\" on host " + hostname);
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
  
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((domains == null) ? 0 : domains.hashCode());
    result = prime * result + ((properties == null) ? 0 : properties.hashCode());
    result = prime * result + ((servers == null) ? 0 : servers.hashCode());
    return result;
  }

  /**
   * reset visited and gateway fields.
   */
  public void reset() {
    for (Enumeration<A3CMLServer> s = servers.elements(); s.hasMoreElements(); ) {
      A3CMLServer server = s.nextElement();
      server.visited = false;
      server.gateway = (short) -1;
    }
    for (Enumeration<A3CMLDomain> d = domains.elements(); d.hasMoreElements(); ) {
      A3CMLDomain domain = d.nextElement();
      domain.gateway = (short) -1;
    }
  }
}
