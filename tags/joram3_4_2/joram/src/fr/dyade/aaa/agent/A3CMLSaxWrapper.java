/*
 * Copyright (C) 2002 SCALAGENT
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

import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * XML SAX Wrapper for A3 configuration file.
 */
public class A3CMLSaxWrapper
    extends DefaultHandler
    implements A3CMLWrapper {

  final static String PARSER_NAME_PROPERTY = "org.xml.sax.driver";
  final static String DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";

  protected Logger logmon = null;
  protected A3CMLHandler a3configHdl = null;

  public A3CMLSaxWrapper() {
    // Get the logging monitor from current server MonologMonitorFactory
    logmon = Debug.getLogger("fr.dyade.aaa.agent.A3CMLWrapper");
  }

  /**
   * A3CML file is always parsed in regard to a particular server: serverId.
   */
  short serverId = -1;
  /**
   * Name of configuration to get from the file.
   */
  String configName = "default";
  /** Working attribute used during configuration's */
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
   * Working attribute used during jvmArgs' definition between  start and
   * end element.
   */
  String jvmArgs = null;

  /**
   * Parses the xml file named <code>cfgFileName</code> and calls handler 
   * methods. Calls only methode <code>startDocument()</code>,
   * <code>startElement</code>, <code>endElement</code> and 
   * <code>endDocument</code>.
   *
   * @param cfgFileName    the name of the xml file
   * @param configName     the name of the configuration
   * @param serverId       the id of the local server
   *
   * @exception Exception  unspecialized error
   */
  public A3CMLHandler parse(Reader cfgReader,
                            String cfgName,
                            short serverId) throws Exception {
    this.configName = cfgName;
    this.serverId = serverId;

    a3configHdl = new A3CMLHandler();

    XMLReader reader =
      XMLReaderFactory.createXMLReader(
	System.getProperty(PARSER_NAME_PROPERTY, DEFAULT_PARSER_NAME));
    reader.setContentHandler(this);
    reader.setErrorHandler(this);
    reader.parse(new InputSource(cfgReader));

    return a3configHdl;
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
    logmon.log(BasicLevel.ERROR,
               "fatal error parsing " + e.getPublicId() +
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
    logmon.log(BasicLevel.ERROR,
               "error parsing " + e.getPublicId() +
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
    logmon.log(BasicLevel.ERROR,
               "warning parsing " + e.getPublicId() +
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
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "startDocument");
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

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "startElement: " + name);

    if (name.equals(A3CMLHandler.ELT_CONFIG)) {
      conf = atts.getValue(A3CMLHandler.ATT_NAME);
      if (conf == null) conf = configName;
    } else if (configName.equals(conf)) {
      if (name.equals(A3CMLHandler.ELT_DOMAIN)) {
        try {
          domain = new A3CMLDomain(atts.getValue(A3CMLHandler.ATT_NAME),
                                   atts.getValue(A3CMLHandler.ATT_NETWORK));
        } catch (Exception exc) {
          throw new SAXException(exc.getMessage());
        }
      } else if (name.equals(A3CMLHandler.ELT_SERVER)) {
        try {
          server = new A3CMLPServer(atts.getValue(A3CMLHandler.ATT_ID),
                                    atts.getValue(A3CMLHandler.ATT_NAME),
                                    atts.getValue(A3CMLHandler.ATT_HOSTNAME));
        } catch (Exception exc) {
          throw new SAXException(exc.getMessage());
        }
      } else if (name.equals(A3CMLHandler.ELT_TRANSIENT)) {
        try {
          server = new A3CMLTServer(atts.getValue(A3CMLHandler.ATT_ID),
                                    atts.getValue(A3CMLHandler.ATT_NAME),
                                    atts.getValue(A3CMLHandler.ATT_HOSTNAME),
                                    atts.getValue(A3CMLHandler.ATT_SERVER));
        } catch (Exception exc) {
          throw new SAXException(exc.getMessage());
        }
      } else if (name.equals(A3CMLHandler.ELT_NETWORK)) {
        try {
          network = new A3CMLNetwork(atts.getValue(A3CMLHandler.ATT_DOMAIN),
                                     atts.getValue(A3CMLHandler.ATT_PORT));
        } catch (Exception exc) {
          throw new SAXException(exc.getMessage());
        }
      } else if (name.equals(A3CMLHandler.ELT_SERVICE)) {
        service = new A3CMLService(atts.getValue(A3CMLHandler.ATT_CLASS),
                                   atts.getValue(A3CMLHandler.ATT_ARGS));
      } else if (name.equals(A3CMLHandler.ELT_PROPERTY)) {
	if ((server ==  null) ||	                // Global property
	    (server.sid == serverId)) {    // Server property
          System.getProperties().put(atts.getValue(A3CMLHandler.ATT_NAME),
                                     atts.getValue(A3CMLHandler.ATT_VALUE));
	}
      } else if (name.equals(A3CMLHandler.ELT_JVM_ARGS)) {
        jvmArgs = atts.getValue(A3CMLHandler.ATT_VALUE);
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

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "endElement: " + name);

    if (name.equals(A3CMLHandler.ELT_CONFIG)) {
      conf = null;
    } else if (configName.equals(conf)) {
      if (name.equals(A3CMLHandler.ELT_DOMAIN)) {
	if (a3configHdl.domains == null)
	  a3configHdl.domains = new Hashtable();
	a3configHdl.domains.put(domain.name, domain);
	domain = null;
      } else if (name.equals(A3CMLHandler.ELT_SERVER) ||
                 name.equals(A3CMLHandler.ELT_TRANSIENT)) {
	if (server.sid > a3configHdl.maxid)
	  a3configHdl.maxid = server.sid;
	a3configHdl.servers.put(new Short(server.sid), server);
	server = null;
      } else if (name.equals(A3CMLHandler.ELT_NETWORK)) {
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
	    if (a3configHdl.domains != null)
	      d = (A3CMLDomain) a3configHdl.domains.get(network.domain);
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
      } else if (name.equals(A3CMLHandler.ELT_SERVICE)) {
	if (server != null) {
	  if (server.services == null)
	    server.services = new Vector();
	  server.services.addElement(service);
	} else {
	  // Can never happen (see DTD).
	}
      } else if (name.equals(A3CMLHandler.ELT_PROPERTY)) {
      } else if (name.equals(A3CMLHandler.ELT_JVM_ARGS)) {
        if (server != null && jvmArgs != null) {
          server.jvmArgs = jvmArgs;
        }
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
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "endDocument");
  }
}
