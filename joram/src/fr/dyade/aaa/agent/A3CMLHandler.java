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

import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * XML SAX Handler for A3 configuration file.
 */
public class A3CMLHandler extends DefaultHandler {
  static final boolean PARSEDBG = false;

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

  String conf = null;
  /**
   * Working attribute used during domain's definition between start and
   * end element.
   */
  A3CMLDomain domain = null;
  /**
   * Working attribute used during server's definition (persitent or transient)
   * between start and end element.
   */
  A3CMLServer server = null;
  /**
   * Working attribute used during network's definition between start and
   * end element.
   */
  A3CMLNetwork network = null;
  /**
   * Working attribute used during service's definition between start and
   * end element.
   */
  A3CMLService service = null;
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
  /**
   * Properties defined in A3CML file, either global properties or properties
   * defined for server serverId.
   */
  public Properties properties = null;

  /**
   * A3CML file is always parsed in regard to a particular server: serverId.
   */
  short serverId = -1;

  /**
   * Name of configuration to get from the file.
   */
  String configName = "default";

  public final static String CFG_DIR_PROPERTY = "fr.dyade.aaa.agent.A3CONF_DIR";
  public final static String DEFAULT_CFG_DIR = ".";
  public final static String CFG_FILE_PROPERTY = "fr.dyade.aaa.agent.A3CONF_FILE";
  public final static String DEFAULT_CFG_FILE = "a3servers.xml";
  
  final static String CFG_NAME_PROPERTY = "fr.dyade.aaa.agent.A3CONF_NAME";
  final static String DEFAULT_CFG_NAME = "default";

  static String DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";

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
    String cfgDir = System.getProperty(CFG_DIR_PROPERTY, DEFAULT_CFG_DIR);
    String cfgFileName = System.getProperty(CFG_FILE_PROPERTY,
					    DEFAULT_CFG_FILE);

    File cfgFile = new File(cfgDir, cfgFileName);
    if ((cfgFile == null) || (!cfgFile.exists()) || (!cfgFile.isFile()))
      return null;

    String cfgName = System.getProperty(CFG_NAME_PROPERTY, DEFAULT_CFG_NAME);

    XMLReader reader =
      XMLReaderFactory.createXMLReader(
	System.getProperty("org.xml.sax.driver",
			   "org.apache.xerces.parsers.SAXParser"));
    A3CMLHandler a3configHdl = new A3CMLHandler(cfgName, serverId);
    reader.setContentHandler(a3configHdl);
    reader.setErrorHandler(a3configHdl);
    reader.parse(cfgFile.getCanonicalPath());

    if (a3configHdl.servers == null)
      throw new Exception("Empty configuration \"" + cfgName + "\" in " +
			  cfgFile.getCanonicalPath() + " configuration file.");

    return a3configHdl;
  }

  private A3CMLHandler(String name, short serverId) {
    super();
    if (name != null)
      configName = name;
    this.serverId = serverId;
    this.servers = new Hashtable();
  }

  /**
   * Handles notification of a non-recoverable parser error.
   *
   * @param e	The warning information encoded as an exception.
   *
   * @exception SAXException
   *	Any SAX exception, possibly wrapping another exception.
   */
  public void fatalError(SAXParseException e) throws SAXException {
    if (PARSEDBG)
      System.err.println("fatal error parsing " + e.getPublicId() +
	" at " + e.getLineNumber() + "." + e.getColumnNumber());
    throw e;
  }

  /**
   * Handles notification of a recoverable parser error.
   *
   * @param e	The warning information encoded as an exception.
   *
   * @exception SAXException
   *	Any SAX exception, possibly wrapping another exception.
   */
  public void error(SAXParseException e) throws SAXException {
    if (PARSEDBG)
      System.err.println("error parsing " + e.getPublicId() +
	" at " + e.getLineNumber() + "." + e.getColumnNumber());
    throw e;
  }


  /**
   * Handles notification of a parser warning.
   *
   * @param e	The warning information encoded as an exception.
   *
   * @exception SAXException
   *	Any SAX exception, possibly wrapping another exception.
   */
  public void warning(SAXParseException e) throws SAXException {
    if (PARSEDBG)
      System.err.println("warning parsing " + e.getPublicId() +
	" at " + e.getLineNumber() + "." + e.getColumnNumber());
    throw e;
  }

  /**
   * Initializes parsing of a document.
   *
   * @exception SAXException
   *	unspecialized error
   */
  public void startDocument() throws SAXException {
    if (PARSEDBG)
      System.out.println("startDocument");
  }

  /**
   * Receive notification of the start of an element.
   *
   * @param uri		The Namespace URI
   * @param localName	The local name
   * @param rawName	The qualified name
   * @param atts	The attributes attached to the element.
   *
   * @exception SAXException
   *	unspecialized error
   */
  public void startElement(String uri,
			   String localName,
			   String rawName,
			   Attributes atts) throws SAXException {
    String name = rawName;

    if (PARSEDBG)
      System.out.println("startElement: " + name);

    if (name.equals(ELT_CONFIG)) {
      conf = atts.getValue(A3CMLHandler.ATT_NAME);
      if (conf == null) conf = configName;
    } else if (configName.equals(conf)) {
      if (name.equals(ELT_DOMAIN)) {
	domain = new A3CMLDomain(atts);
      } else if (name.equals(ELT_SERVER)) {
	server = new A3CMLPServer(atts);
      } else if (name.equals(ELT_TRANSIENT)) {
	server = new A3CMLTServer(atts);
      } else if (name.equals(ELT_NETWORK)) {
	network = new A3CMLNetwork(atts);
      } else if (name.equals(ELT_SERVICE)) {
	service = new A3CMLService(atts);
      } else if (name.equals(ELT_PROPERTY)) {
	if ((server ==  null) ||	   // Global property
	    (server.sid == serverId)) {    // Server property
	  if (properties == null) properties = new Properties();
	  properties.setProperty(atts.getValue(ATT_NAME),
				 atts.getValue(ATT_VALUE));
	}
      } else {
	throw new SAXException("unknown element \"" + name + "\"");
      }
    }
  }

  /**
   * Receive notification of the end of an element.
   *
   * @param uri		The Namespace URI
   * @param localName	The local name
   * @param rawName	The qualified name
   * @param atts	The attributes attached to the element.
   *
   * @exception SAXException
   *	unspecialized error
   */
  public void endElement(String uri,
			 String localName,
			 String rawName) throws SAXException {
    String name = rawName;

    if (PARSEDBG)
      System.out.println("endElement: " + name);

    if (name.equals(ELT_CONFIG)) {
      conf = null;
    } else if (configName.equals(conf)) {
      if (name.equals(ELT_DOMAIN)) {
	if (domains == null)
	  domains = new Hashtable();
	domains.put(domain.name, domain);
	domain = null;
      } else if (name.equals(ELT_SERVER) || name.equals(ELT_TRANSIENT)) {
	if (server.sid > maxid)
	  maxid = server.sid;
	servers.put(new Short(server.sid), server);
	server = null;
      } else if (name.equals(ELT_NETWORK)) {
	if ((server != null) &&
	    (server instanceof A3CMLPServer)) {
	  A3CMLPServer pserver = (A3CMLPServer) server;
	  if (pserver.networks == null)
	    pserver.networks = new Vector();
	  pserver.networks.add(network);
	  // Add the server to the corresponding domains
	  // AF: This step should be done at the end of parsing, in order
	  // to avoid to declare domains first.
	  if (! network.domain.equals("transient")) {
	    A3CMLDomain d = null;
	    if (domains != null)
	      d = (A3CMLDomain) domains.get(network.domain);
	    if (d != null) {
	      d.addServer((A3CMLPServer) server);
	    } else {
	      throw new SAXException("Unknown domain \"" + network.domain +
				     "\" for server \"" + server.name + "\".");
	    }
	  }
	} else {
	  // Can never happen (see DTD).
	}
	network = null;
      } else if (name.equals(ELT_SERVICE)) {
	if (server != null) {
	  if (server.services == null)
	    server.services = new Vector();
	  server.services.addElement(service);
	} else {
	  // Can never happen (see DTD).
	}
      } else if (name.equals(ELT_PROPERTY)) {
      } else {
	throw new SAXException("unknown element \"" + name + "\"");
      }
    }
  }

  /**
   * Finalizes parsing of a document.
   *
   * @exception SAXException
   *	unspecialized error
   */
  public void endDocument() throws SAXException {
    if (PARSEDBG)
      System.out.println("endDocument");
  }

  /**
   * Tests if the specified agent server is transient.
   *
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
