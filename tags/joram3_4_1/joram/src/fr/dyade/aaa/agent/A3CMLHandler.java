/*
 * Copyright (C) 2001 - 2002 SCALAGENT
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

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

/**
 * Generic XML Handler for A3 configuration file.
 */
public class A3CMLHandler {
  /** Syntaxic name for config element */
  static final String ELT_CONFIG = "config";
  /** Syntaxic name for domain element */
  static final String ELT_DOMAIN = "domain";
  /** Syntaxic name for server element */
  static final String ELT_SERVER = "server";
  /** Syntaxic name for transient element */
  static final String ELT_TRANSIENT = "transient";
  /** Syntaxic name for network element */
  static final String ELT_NETWORK = "network";
  /** Syntaxic name for service element */
  static final String ELT_SERVICE = "service";
  /** Syntaxic name for property element */
  static final String ELT_PROPERTY = "property";
  /** Syntaxic name for id attribute */
  static final String ATT_ID = "id";
  /** Syntaxic name for name attribute */
  static final String ATT_NAME = "name";
  /** Syntaxic name for domain attribute */
  static final String ATT_DOMAIN = "domain";
  /** Syntaxic name for network attribute */
  static final String ATT_NETWORK = "network";
  /** Syntaxic name for hostname attribute */
  static final String ATT_HOSTNAME = "hostname";
  /** Syntaxic name for port attribute */
  static final String ATT_PORT = "port";
  /** Syntaxic name for server attribute */
  static final String ATT_SERVER = "server";
  /** Syntaxic name for class attribute */
  static final String ATT_CLASS = "class";
  /** Syntaxic name for args attribute */
  static final String ATT_ARGS = "args";
  /** Syntaxic name for value attribute */
  static final String ATT_VALUE = "value";
  /** Syntaxic for server jvm arguments */
  static final String ELT_JVM_ARGS = "jvmArgs";

  /**
   * Working attribute used to aggregate services during server's definition
   * between start and end element.
   */
  Vector services = null;

  /** Biggest server's id found during configuration parsing. */
  public short maxid = -1;
  /** Hashtable of all domains defined during configuration parsing. */
  public Hashtable domains = null;
  /**
   * Hashtable of all servers (persitent or transient) defined during
   * configuration parsing.
   */
  public Hashtable servers = null;

  public final static String CFG_DIR_PROPERTY = "fr.dyade.aaa.agent.A3CONF_DIR";
  public final static String DEFAULT_CFG_DIR = null;

  public final static String CFG_FILE_PROPERTY = "fr.dyade.aaa.agent.A3CONF_FILE";
  public final static String DEFAULT_CFG_FILE = "a3servers.xml";
  
  final static String CFG_NAME_PROPERTY = "fr.dyade.aaa.agent.A3CONF_NAME";
  final static String DEFAULT_CFG_NAME = "default";

  final static String A3CMLWRP_PROPERTY = "fr.dyade.aaa.agent.A3CMLWrapper";
  final static String DEFAULT_A3CMLWRP = "fr.dyade.aaa.agent.A3CMLSaxWrapper";

  /**
   * Gets static configuration of agent servers from a file. This method
   * fills the object graph configuration in the <code>A3CMLHandler</code>
   * object.
   *
   * @return	the <code>A3CMLHandler</code> object if file exists and is
   * 		correct, null otherwise.
   *
   * @exception Exception
   *	unspecialized exception when reading and parsing the configuration file
   */
  public static A3CMLHandler getConfig(short serverId) throws Exception {
    // Get the logging monitor from current server MonologMonitorFactory
    Logger logmon =  Debug.getLogger("fr.dyade.aaa.agent.A3CMLWrapper");

    String cfgDir = System.getProperty(CFG_DIR_PROPERTY, DEFAULT_CFG_DIR);
    String cfgFileName = System.getProperty(CFG_FILE_PROPERTY,
					    DEFAULT_CFG_FILE);

    Reader reader = null;
    if (cfgDir != null) {
      File cfgFile = new File(cfgDir, cfgFileName);
      
      try {
        if ((cfgFile != null) &&
            (cfgFile.length() != 0) &&
            cfgFile.exists() &&
            cfgFile.isFile()) {
          cfgFileName = cfgFile.getPath();
        } else {
          throw new IOException();
        }
        reader = new FileReader(cfgFile);
      } catch (IOException exc) {
        // configuration file seems not exist, search it from the
        // search path used to load classes.
        logmon.log(BasicLevel.ERROR,
                   "Unable to find configuration file \"" +
                   cfgFile.getPath() + "\".");
        reader = null;
      }
    }

    if (reader == null) {
      ClassLoader classLoader = null;
      InputStream is = null;
      try {
        classLoader = A3CMLHandler.class.getClassLoader();
        if (classLoader != null) {
          logmon.log(BasicLevel.WARN,
                     "Trying to find [" + cfgFileName + "] using " +
                     classLoader + " class loader.");
          is = classLoader.getResourceAsStream(cfgFileName);
        }
      } catch(Throwable t) {
        logmon.log(BasicLevel.WARN,
                   "Can't find [" + cfgFileName + "] using " +
                   classLoader + " class loader.",
                   t);
        is = null;
      }
      
      if (is == null) {
        // Last ditch attempt: get the resource from the class path.
        logmon.log(BasicLevel.WARN,
                   "Trying to find [" + cfgFileName +
                   "] using ClassLoader.getSystemResource().");
        is = ClassLoader.getSystemResourceAsStream(cfgFileName);
      }
      if (is == null) {
        throw new FileNotFoundException("configuration file " + cfgFileName +
                                        " not found in classpath");
      }
      reader = new InputStreamReader(is);
    }
    return getConfig(serverId, reader, cfgFileName);
  }


  
  public static A3CMLHandler getConfig(short serverId,
                                       Reader reader,
                                       String cfgFileName) 
    throws Exception {
    String cfgName = System.getProperty(CFG_NAME_PROPERTY, DEFAULT_CFG_NAME);

    String wrpCName = System.getProperty(A3CMLWRP_PROPERTY, DEFAULT_A3CMLWRP);
    Class wrpClass = Class.forName(wrpCName);

    A3CMLWrapper wrapper = (A3CMLWrapper) wrpClass.newInstance();
    A3CMLHandler a3configHdl = wrapper.parse(reader, cfgName, serverId);

    if (a3configHdl.servers == null)
      throw new Exception("Empty configuration \"" + cfgName + "\" in " +
			  cfgFileName + " configuration file.");

    return a3configHdl;
  }

  A3CMLHandler() {
    super();
    this.servers = new Hashtable();
  }

  /**
   * Returns the specified AgentServer desc.
   *
   * @param id		agent server id
   * @return		the arguments as declared in configuration file
   */
  public final
  A3CMLServer getServer(short sid) throws UnknownServerException {
    A3CMLServer server = (A3CMLServer) servers.get(new Short(sid));
    if (server == null)
      throw new UnknownServerException("Unknown server id. #" + sid);
    return server;
  }

  /**
   * Tests if the specified agent server is transient.
   *
   * @param id		agent server id
   * @return	true if the server is transient; false otherwise; 
   * @exception UnknownServerException
   *	The specified server does not exist.
   */
  public final
  boolean isTransient(short sid) throws UnknownServerException {
    A3CMLServer server = (A3CMLServer) servers.get(new Short(sid));
    if (server == null)
      throw new UnknownServerException("Unknown server id. #" + sid);

    if (server instanceof A3CMLPServer) {
      return false;
    } else if (server instanceof A3CMLTServer) {
      return true;
    } else {
      throw new UnknownServerException("Unknown type for server id. # " + sid);
    }
  }

  /**
   * Get the argument strings for a particular service on a particular
   * agent server identified by its id.
   *
   * @param id		agent server id
   * @param classname	the service class name
   * @return		the arguments as declared in configuration file
   * @exception	UnknownServerException
   *	The specified server does not exist.
   * @exception UnknownServiceException
   *	The specified service is not declared on this server. 
   */
  public final
  String getServiceArgs(short sid,
			String classname) throws UnknownServerException, UnknownServiceException {
    A3CMLServer server = (A3CMLServer) servers.get(new Short(sid));
    if (server == null)
      throw new UnknownServerException("Unknown server id. #" + sid);

    if (server.services != null) {
      for (int i = server.services.size() -1; i >=0; i--) {
	A3CMLService service = (A3CMLService) server.services.elementAt(i);
	if (service.classname.equals(classname))
	  return service.args;
      }
    }
    throw new UnknownServiceException("Service \"" +
			classname + "\" not found on server#" + sid);
  }


  /**
   * Get the argument to pass to server's jvm on a particular
   * agent server identified by its id.
   *
   * @param id		agent server id
   * @return		the arguments as declared in configuration file
   * @exception	UnknownServerException
   *	The specified server does not exist.
   */
  public final String getJvmArgs(short sid) throws UnknownServerException{
    A3CMLServer server = (A3CMLServer) servers.get(new Short(sid));
    if (server == null)
      throw new UnknownServerException("Unknown server id. #" + sid);
    if (server.jvmArgs != null)
      return server.jvmArgs;
    return "";
  }
  

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
  public final
  String getServiceArgsFamily(short sid,
			      String classname) throws UnknownServerException, UnknownServiceException {
    try {
      String args = getServiceArgs(sid, classname);
      return args;
    } catch (UnknownServerException exc) {
      throw exc;
    } catch (Exception exc) {}
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
    throw new UnknownServiceException("Service \"" +
				      classname + "\" not found on family server#" + sid);
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
  public final
  String getServiceArgs(String hostname,
			String className) throws Exception {
    for (Enumeration s = servers.elements(); s.hasMoreElements(); ) {
      A3CMLServer server = (A3CMLServer) s.nextElement();
      if (server.hostname.equals(hostname)) {
	try {
	  String args = getServiceArgs(server.sid, className);
	  return args;
	} catch (Exception exc) {}
      }
    }
    throw new UnknownServiceException("Service \"" +
				      className + "\" not found on host " + hostname);
  }

}
