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

import org.xml.sax.Parser;
import org.xml.sax.DocumentHandler;
import org.xml.sax.HandlerBase;
import org.xml.sax.AttributeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.ParserFactory;
import org.w3c.dom.Document;

import fr.dyade.aaa.util.Strings;

/**
 * The <code>A3Config</code> class allow to parse the A3 configuration
 * file.
 *
 * @author  Andr* Freyssinet
 * @version 1.0, 01/09/99
 */
public class A3Config {
public static final String RCS_VERSION="@(#)$Id: A3Config.java,v 1.2 2000-08-01 09:13:25 tachkeni Exp $";
  final static boolean DEBUG = true;
  static public boolean trace = false;

  public ServerDesc networkServers[] = null;
  public ServerDesc transientServers[] = null;

  final static short MIN_TRANSIENT_ID = 16384;

  public final static String CFG_PROPERTY = "A3SERVERS_CFG";
  public final static String DEFAULT_CFG_FILE = "a3servers.xml";

  final static String DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";

  public static void main(String args[]) throws Exception {
    Parser parser = ParserFactory.makeParser(DEFAULT_PARSER_NAME);
    HandlerBase a3configHdl = new A3CMLHandler();
    parser.setDocumentHandler(a3configHdl);
    parser.setErrorHandler(a3configHdl);
    for (int i = 0; i < args.length; i++) {
      if ((DEBUG) && (trace))
	System.out.println("Parse " + args[i]);
      parser.parse(args[i]);
      System.out.println(a3configHdl);
    }
  }

  public final boolean isTransient(short sid) {
    if (sid >= MIN_TRANSIENT_ID)
      return true;
    return false;
  }

  final AgentId transientProxyId(short sid) {
    return transientServers[sid - MIN_TRANSIENT_ID].proxyId;
  }

  public final ServerDesc getServerDesc(short sid) {
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
  final public String getHostname(short sid) {
    return getServerDesc(sid).hostname;
  }

  public final
  String getServiceArgs(short sid,
			String className) throws Exception {
    ServerDesc server = getServerDesc(sid);
    if (server.services != null) {
      for (int i = server.services.length -1; i >=0; i--) {
	if (server.services[i].className.equals(className))
	  return server.services[i].parameters;
      }
    }
    throw new Exception("Service \"" +
			className + "\" not found on server#" + sid);
  }

  public final
  String getServiceArgsFamily(short sid,
			      String className) throws Exception {
    try {
      String args = getServiceArgs(sid, className);
      return args;
    } catch (Exception exc) {}
    for (int i=transientServers.length -1; i>=0; i--) {
      if (transientServers[i].proxyId.to == sid) {
	try {
	  String args = getServiceArgs(transientServers[i].sid, className);
	  return args;
	} catch (Exception exc) {}
      }
    }
    throw new Exception("Service \"" +
			className + "\" not found on family server#" + sid);
  }

  public final
  String getServiceArgs(String hostname,
			String className) throws Exception {
    for (int i=networkServers.length -1; i>=0; i--) {
      if (networkServers[i].hostname.equals(hostname)) {
	try {
	  String args = getServiceArgs(networkServers[i].sid, className);
	  return args;
	} catch (Exception exc) {}
      }
    }
    for (int i=transientServers.length -1; i>=0; i--) {
      if (transientServers[i].hostname.equals(hostname)) {
	try {
	  String args = getServiceArgs(transientServers[i].sid, className);
	  return args;
	} catch (Exception exc) {}
      }
    }
    throw new Exception("Service \"" +
			className + "\" not found on host " + hostname);
  }

  /**
   * Gets static configuration of agent servers from default file.
   *
   * @return	static configuration of agent servers
   *
   * @exception Exception
   *	unspecialized exception when reading and parsing the configuration file
   */
  public static A3Config getConfig() throws Exception {
    return getConfig(null);
  }

  /**
   * Gets static configuration of agent servers from a file.
   *
   * @return	static configuration of agent servers
   *
   * @exception Exception
   *	unspecialized exception when reading and parsing the configuration file
   */
  public static A3Config getConfig(String cfgFileName) throws Exception {
    if (cfgFileName == null)
      cfgFileName = System.getProperty(CFG_PROPERTY,
				       DEFAULT_CFG_FILE);

    File cfgFile = new File(cfgFileName);
    if ((cfgFile == null) || (!cfgFile.exists()) || (!cfgFile.isFile())) {
      // There is no config file, set a default configuration.
      A3Config a3config = new A3Config();

      a3config.networkServers = new ServerDesc[1];
      a3config.transientServers = null;

      a3config.networkServers[0] = new ServerDesc((short) 0,
						  "default",
						  "localhost",
						  0);
      return a3config;
    }

    Parser parser = ParserFactory.makeParser(DEFAULT_PARSER_NAME);
    A3CMLHandler a3configHdl = new A3CMLHandler();
    parser.setDocumentHandler(a3configHdl);
    parser.setErrorHandler(a3configHdl);
    parser.parse(cfgFileName);
    return a3configHdl.getConfig();
  }
}

class A3CMLHandler extends HandlerBase {
  static final String ELT_CONFIG = "config";
  static final String ELT_HOST = "host";
  static final String ELT_SERVER = "server";
  static final String ELT_TRANSIENT = "transient";
  static final String ELT_SERVICE = "service";
  static final String ATT_HOSTNAME = "hostname";
  static final String ATT_ID = "id";
  static final String ATT_NAME = "name";
  static final String ATT_PORT = "port";
  static final String ATT_TPORT = "tport";
  static final String ATT_SERVER = "server";
  static final String ATT_CLASS = "class";
  static final String ATT_ARGS = "args";

  String hostname = null;
  ServerDesc server = null;
  ServiceDesc service = null;

  Vector servers = null;
  Vector transients = null;
  Vector services = null;

  public void startDocument() throws SAXException {
    if ((A3Config.DEBUG) && (A3Config.trace))
      System.out.println("startDocument");
    hostname = null;
    server = null;
    service = null;
    servers = new Vector();
    transients = new Vector();
    services = new Vector();
  }

  ServerDesc newServer(AttributeList atts) throws SAXException {
    short sid = -1;
    String name = null;
    int port = -1;
    ServerDesc server = null;

    String attribute = atts.getValue(ATT_ID);
    try {
      sid = Short.parseShort(attribute);
    } catch (NumberFormatException exc) {
      throw new SAXException("bad value \"" +
			     attribute +
			     "\" for attribute \"" + 
			     ATT_ID);
    }
    name = atts.getValue(ATT_NAME);
    attribute = atts.getValue(ATT_PORT);
    try {
      port = Integer.parseInt(attribute);
    } catch (NumberFormatException exc) {
      throw new SAXException("bad value \"" +
			     attribute +
			     "\" for attribute \"" + 
			     ATT_PORT);
    }
    server = new ServerDesc(sid, name, hostname, port);

    attribute = atts.getValue(ATT_TPORT);
    if ((attribute != null) && !attribute.equals("")) {
      try {
	server.transientPort = Short.parseShort(attribute);
      } catch (NumberFormatException exc) {
	throw new SAXException("bad value \"" +
			       attribute +
			       "\" for attribute \"" + 
			       ATT_TPORT);
      }
    }
    
    return server;
  }
  
  ServerDesc newTransient(AttributeList atts) throws SAXException {
    short sid = -1;
    String name = null;
    short server = -1;
    ServerDesc tserver = null;

    String attribute = atts.getValue(ATT_ID);
    try {
      sid = Short.parseShort(attribute);
    } catch (NumberFormatException exc) {
      throw new SAXException("bad value \"" +
			     attribute +
			     "\" for attribute \"" + 
			     ATT_ID);
    }
    name = atts.getValue(ATT_NAME);
    attribute = atts.getValue(ATT_SERVER);
    try {
      server = Short.parseShort(attribute);
    } catch (NumberFormatException exc) {
      throw new SAXException("bad value \"" +
			     attribute +
			     "\" for attribute \"" + 
			     ATT_SERVER);
    }
    tserver = new ServerDesc(sid, name, hostname, server);

    return tserver;
  }

  ServiceDesc newService(AttributeList atts) throws SAXException {
    return new ServiceDesc(atts.getValue(ATT_CLASS),
			   atts.getValue(ATT_ARGS));
  }

  public void startElement(java.lang.String name,
			   AttributeList atts) throws SAXException {
    if ((A3Config.DEBUG) && (A3Config.trace))
      System.out.println("startElement:" + name);
    if (name.equals(ELT_CONFIG)) {
    } else if (name.equals(ELT_HOST)) {
      hostname = atts.getValue(ATT_HOSTNAME);
    } else if (name.equals(ELT_SERVER)) {
      server = newServer(atts);
    } else if (name.equals(ELT_TRANSIENT)) {
      server = newTransient(atts);
    } else if (name.equals(ELT_SERVICE)) {
      service = newService(atts);
    } else {
      throw new SAXException("unknow element \"" + name + "\"");
    }
  }

  public void endElement(java.lang.String name) throws SAXException {
    if ((A3Config.DEBUG) && (A3Config.trace))
      System.out.println("endElement:" + name);
    if (name.equals(ELT_CONFIG)) {
    } else if (name.equals(ELT_HOST)) {
      hostname = null;
    } else if (name.equals(ELT_SERVER)) {
      // Creates it
      server.services = new ServiceDesc[services.size()];
      services.copyInto(server.services);
      servers.addElement(server);
      services.removeAllElements();
      server = null;
    } else if (name.equals(ELT_TRANSIENT)) {
      // Creates it
      server.services = new ServiceDesc[services.size()];
      services.copyInto(server.services);
      transients.addElement(server);
      services.removeAllElements();
      server = null;
    }  else if (name.equals(ELT_SERVICE)) {
      services.addElement(service);
    } else {
      throw new SAXException("unknow element \"" + name + "\"");
    }
  }

  public void endDocument() throws SAXException {
    if ((A3Config.DEBUG) && (A3Config.trace))
      System.out.println("endDocument");
  }

  A3Config getConfig() {
    A3Config a3config = new A3Config();

    int max = 0;
    for (Enumeration e = servers.elements(); e.hasMoreElements();) {
      ServerDesc server = (ServerDesc) e.nextElement();
      if (server.sid > max)
	max = server.sid;
    }
    a3config.networkServers = new ServerDesc[max + 1];
    for (Enumeration e = servers.elements(); e.hasMoreElements();) {
      ServerDesc server = (ServerDesc) e.nextElement();
      a3config.networkServers[server.sid] = server;
    }

    max = 0;
    for (Enumeration e = transients.elements(); e.hasMoreElements();) {
      ServerDesc server = (ServerDesc) e.nextElement();
      if (server.sid > max)
	max = server.sid;
    }
    if (max != 0) {
      max -= A3Config.MIN_TRANSIENT_ID;
      a3config.transientServers = new ServerDesc[max + 1];
      for (Enumeration e = transients.elements(); e.hasMoreElements();) {
	ServerDesc server = (ServerDesc) e.nextElement();
	a3config.transientServers[server.sid - A3Config.MIN_TRANSIENT_ID] = server;
      }
    } else {
      a3config.transientServers = new ServerDesc[0];
    }

    return a3config;
  }

  public String toString() {
    StringBuffer strBuf = new StringBuffer();
    strBuf.append("Config=[servers=");
    Strings.toString(strBuf, servers);
    strBuf.append(",transients=");
    Strings.toString(strBuf, transients);
    return strBuf.toString();
  }
}
