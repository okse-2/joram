/*
 * Copyright (C) 2002-2008 ScalAgent Distributed Technologies 
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

import org.objectweb.util.monolog.api.BasicLevel;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * XML SAX Wrapper for A3 configuration file.
 */
public class A3CMLSaxWrapper extends DefaultHandler implements A3CMLWrapper {
  protected A3CMLConfig a3cmlConfig = null;

  public A3CMLSaxWrapper() {}

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
   * Working attribute used during server's definition between start and
   * end element.
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
   * Working attribute used during service's definition between start and
   * end element.
   */
  A3CMLProperty property = null;
  /**
   * Working attribute used during jvmArgs' definition between  start and
   * end element.
   */
  String jvmArgs = null;
  /**
   * Working attribute used during nat' definition between  start and
   * end element.
   */
  A3CMLNat nat = null;
  /**
   * Working attribute used during cluster' definition between  start and
   * end element.
   */
  A3CMLCluster cluster = null;

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
  public A3CMLConfig parse(Reader cfgReader,
                           String cfgName) throws Exception {
    this.configName = cfgName;

    a3cmlConfig = new A3CMLConfig();

    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser parser = factory.newSAXParser();
    parser.parse(new InputSource(cfgReader), this);

    return a3cmlConfig;
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
    Log.logger.log(BasicLevel.ERROR,
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
    Log.logger.log(BasicLevel.ERROR,
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
    Log.logger.log(BasicLevel.ERROR,
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
    if (Log.logger.isLoggable(BasicLevel.DEBUG))
      Log.logger.log(BasicLevel.DEBUG, "startDocument");
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

    if (Log.logger.isLoggable(BasicLevel.DEBUG))
      Log.logger.log(BasicLevel.DEBUG, "startElement: " + name);

    if (name.equals(A3CML.ELT_CONFIG)) {
      conf = atts.getValue(A3CML.ATT_NAME);
      if (conf == null) conf = configName;
    } else if (configName.equals(conf)) {
      if (name.equals(A3CML.ELT_DOMAIN)) {
        try {
          domain = new A3CMLDomain(atts.getValue(A3CML.ATT_NAME),
                                   atts.getValue(A3CML.ATT_NETWORK));
        } catch (Exception exc) {
          throw new SAXException(exc.getMessage());
        }
      } else if (name.equals(A3CML.ELT_SERVER)) {
        try {
          short sid;
          try {
            sid = Short.parseShort(atts.getValue(A3CML.ATT_ID));
          } catch (NumberFormatException exc) {
            throw new Exception("bad value for server id: " +
                                atts.getValue(A3CML.ATT_ID));
          }
          server = new A3CMLServer(sid,
                                   atts.getValue(A3CML.ATT_NAME),
                                   atts.getValue(A3CML.ATT_HOSTNAME));
        } catch (Exception exc) {
          throw new SAXException(exc.getMessage());
        }
      } else if (name.equals(A3CML.ELT_CLUSTER)) {
        try {
          short sid;
          try {
            sid = Short.parseShort(atts.getValue(A3CML.ATT_ID));
          } catch (NumberFormatException exc) {
            throw new Exception("bad value for cluster id: " +
                                atts.getValue(A3CML.ATT_ID));
          }
          cluster = new A3CMLCluster(sid,
                                     atts.getValue(A3CML.ATT_NAME));
        } catch (Exception exc) {
          throw new SAXException(exc.getMessage());
        }
      } else if (name.equals(A3CML.ELT_NETWORK)) {
        try {
          int port;
          try {
            port = Integer.parseInt(atts.getValue(A3CML.ATT_PORT));
          } catch (NumberFormatException exc) {
            throw new Exception("bad value for network port: " +
                                atts.getValue(A3CML.ATT_PORT));
          }
          network = new A3CMLNetwork(atts.getValue(A3CML.ATT_DOMAIN),
                                     port);
        } catch (Exception exc) {
          throw new SAXException(exc.getMessage());
        }
      } else if (name.equals(A3CML.ELT_SERVICE)) {
        service = new A3CMLService(atts.getValue(A3CML.ATT_CLASS),
                                   atts.getValue(A3CML.ATT_ARGS));
      } else if (name.equals(A3CML.ELT_PROPERTY)) {
        property = new A3CMLProperty(atts.getValue(A3CML.ATT_NAME),
                                     atts.getValue(A3CML.ATT_VALUE));
      } else if (name.equals(A3CML.ELT_NAT)) {
        nat = new A3CMLNat(Short.parseShort(atts.getValue(A3CML.ATT_SID)),
                           atts.getValue(A3CML.ATT_HOSTNAME),
                           Integer.parseInt(atts.getValue(A3CML.ATT_PORT)));
      } else if (name.equals(A3CML.ELT_JVM_ARGS)) {
        jvmArgs = atts.getValue(A3CML.ATT_VALUE);
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

    if (Log.logger.isLoggable(BasicLevel.DEBUG))
      Log.logger.log(BasicLevel.DEBUG, "endElement: " + name);

    if (name.equals(A3CML.ELT_CONFIG)) {
      conf = null;
    } else if (configName.equals(conf)) {
      try {
        if (name.equals(A3CML.ELT_DOMAIN)) {
          a3cmlConfig.addDomain(domain);
          domain = null;
        } else if (name.equals(A3CML.ELT_SERVER)) {
          if (cluster == null)
            a3cmlConfig.addServer(server);
          else
            cluster.addServer(server);
          server = null;
        } else if (name.equals(A3CML.ELT_CLUSTER)) {
          a3cmlConfig.addCluster(cluster);
          cluster = null;
        } else if (name.equals(A3CML.ELT_NETWORK)) {
          if (server != null) {
            server.addNetwork(network);
            // Add the server to the corresponding domains
            a3cmlConfig.getDomain(network.domain).addServer(server);
          } else {
            // Can never happen (see DTD).
          }
          network = null;
        } else if (name.equals(A3CML.ELT_SERVICE)) {
          if (server != null) {
            server.addService(service);
          } else {
            // Can never happen (see DTD).
          }
          service = null;
        } else if (name.equals(A3CML.ELT_PROPERTY)) {
          if (server ==  null && cluster == null)
            a3cmlConfig.addProperty(property);	// Global property
          else if (server !=  null)
            server.addProperty(property); 	// Server property
          else if (server ==  null && cluster != null)
            cluster.addProperty(property); 	// Cluster property
          property = null;
        } else if (name.equals(A3CML.ELT_NAT)) {
          if (server !=  null)
            server.addNat(nat);
          nat = null;
        } else if (name.equals(A3CML.ELT_JVM_ARGS)) {
          if (server != null && jvmArgs != null)
            server.jvmArgs = jvmArgs;
          else if (server ==  null && cluster != null)
            cluster.jvmArgs = jvmArgs;
          jvmArgs = null;
        } else {
          throw new SAXException("unknown element \"" + name + "\"");
        }
      } catch (SAXException exc) {
        throw exc;
      } catch (Exception exc) {
        throw new SAXException(exc.getMessage());
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
    if (Log.logger.isLoggable(BasicLevel.DEBUG))
      Log.logger.log(BasicLevel.DEBUG, "endDocument");
  }
}
