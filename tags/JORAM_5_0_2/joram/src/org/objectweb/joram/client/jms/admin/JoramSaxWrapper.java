/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2006 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): Benoit Pelletier (Bull SA)
 */
package org.objectweb.joram.client.jms.admin;

import java.io.*;
import java.util.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.xml.sax.InputSource;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.lang.reflect.Method;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.admin.DeadMQueue;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.Destination;

import org.objectweb.joram.shared.JoramTracing;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

/**
 * XML SAX Wrapper for Joram Admin configuration file.
 */
public class JoramSaxWrapper extends DefaultHandler {

  public JoramSaxWrapper() {}

  /** Syntaxic name for JoramAdmin element */
  static final String ELT_JORAMADMIN = "JoramAdmin";
  /** Syntaxic name for AdminModule element */
  static final String ELT_ADMINMODULE = "AdminModule";
  /** Syntaxic name for connect element */
  static final String ELT_CONNECT = "connect";
  /** Syntaxic name for collocatedConnect element */
  static final String ELT_COLLOCATEDCONNECT = "collocatedConnect";
  /** Syntaxic name for ConnectionFactory element */
  static final String ELT_CONNECTIONFACTORY = "ConnectionFactory";
  /** Syntaxic name for tcp element */
  static final String ELT_TCP = "tcp";
  /** Syntaxic name for local element */
  static final String ELT_LOCAL = "local";
  /** Syntaxic name for hatcp element */
  static final String ELT_HATCP = "hatcp";
  /** Syntaxic name for halocal element */
  static final String ELT_HALOCAL = "halocal";
  /** Syntaxic name for soap element */
  static final String ELT_SOAP = "soap";
  /** Syntaxic name for jndi element */
  static final String ELT_JNDI = "jndi";
  /** Syntaxic name for User element */
  static final String ELT_USER = "User";
  /** Syntaxic name for Destination element */
  static final String ELT_DESTINATION = "Destination";
  /** Syntaxic name for Queue element */
  static final String ELT_QUEUE = "Queue";
  /** Syntaxic name for Topic element */
  static final String ELT_TOPIC = "Topic";
  /** Syntaxic name for Dead message Queue element */
  static final String ELT_DMQUEUE = "DMQueue";
  /** Syntaxic name for property element */
  static final String ELT_PROPERTY = "property";
  /** Syntaxic name for reader element */
  static final String ELT_READER = "reader";
  /** Syntaxic name for writer element */
  static final String ELT_WRITER = "writer";
  /** Syntaxic name for freeReader element */
  static final String ELT_FREEREADER = "freeReader";
  /** Syntaxic name for freeWriter element */
  static final String ELT_FREEWRITER = "freeWriter";
  /** Syntaxic name for InitialContext element */
  static final String ELT_INITIALCONTEXT = "InitialContext";
  /** Syntaxic name for Cluster CF */
  static final String ELT_CLUSTER_CF = "ClusterCF";
  /** Syntaxic name for Cluster Queue */
  static final String ELT_CLUSTER_QUEUE = "ClusterQueue";
  /** Syntaxic name for Cluster Topic */
  static final String ELT_CLUSTER_TOPIC = "ClusterTopic";
  /** Syntaxic name for Cluster element */
  static final String ELT_CLUSTER_ELEMENT = "ClusterElement";


  /** Syntaxic name for name attribute */
  static final String ATT_NAME = "name";
  /** Syntaxic name for login attribute */
  static final String ATT_LOGIN = "login";
  /** Syntaxic name for password attribute */
  static final String ATT_PASSWORD = "password";
  /** Syntaxic name for value attribute */
  static final String ATT_VALUE = "value";
  /** Syntaxic name for host attribute */
  static final String ATT_HOST = "host";
  /** Syntaxic name for port attribute */
  static final String ATT_PORT = "port";
  /** Syntaxic name for cnxTimer attribute */
  static final String ATT_CNXTIMER = "cnxTimer";
  /** Syntaxic name for reliableClass attribute */
  static final String ATT_RELIABLECLASS = "reliableClass";
  /** Syntaxic name for url attribute */
  static final String ATT_URL = "url";
  /** Syntaxic name for timeout attribute */
  static final String ATT_TIMEOUT = "timeout";
  /** Syntaxic name for serverId attribute */
  static final String ATT_SERVERID = "serverId";
  /** Syntaxic name for type attribute */
  static final String ATT_TYPE = "type";
  /** Syntaxic name for className attribute */
  static final String ATT_CLASSNAME = "className";
  /** Syntaxic name for user attribute */
  static final String ATT_USER = "user";
  /** Syntaxic name for dead message queue attribute */
  static final String ATT_DMQ = "dmq";
  /** Syntaxic name for nbMaxMsg attribute */
  static final String ATT_NBMAXMSG = "nbMaxMsg";
  /** Syntaxic name for parent attribute */
  static final String ATT_PARENT = "parent";
  /** Syntaxic name for threshold attribute */
  static final String ATT_THRESHOLD = "threshold";
  /** Syntaxic name for location attribute */
  static final String ATT_LOCATION = "location";

  static final String DFLT_LISTEN_HOST = "localhost";
  static final int DFLT_LISTEN_PORT = 16010;

  static final String DFLT_CF =
    "org.objectweb.joram.client.jms.tcp.TcpConnectionFactory";

  boolean result = true;
  Object obj = null;
  String name = null;
  String login = null;
  String password = null;
  String value = null;
  String host = null;
  int port = -1;
  int cnxTimer = -1;
  String reliableClass = null;
  String url = null;
  int timeout = -1;
  int serverId = -1;
  String className = null;
  String user = null;
  String type = null;
  Properties properties = null;

  String jndiName = null;
  Hashtable toBind = new Hashtable();

  Vector readers = new Vector();
  Vector writers = new Vector();
  boolean freeReading = false;
  boolean freeWriting = false;

  InitialContext jndiCtx = null;

  /** Contains ConnectionFactory defined in the current script */
  Hashtable cfs = new Hashtable();
  /** Contains all users defined in the current script */
  Hashtable users = new Hashtable();
  /** Contains all queues defined in the current script */
  Hashtable queues = new Hashtable();
  /** Contains all topics defined in the current script */
  Hashtable topics = new Hashtable();
  /** Contains all DMQ defined in the current script */
  Hashtable dmqs = new Hashtable();

  /** Temporary set of cluster's elements */
  Hashtable cluster = new Hashtable();

  String dmq = null;
  int threshold = -1;
  int nbMaxMsg = -1;
  String parent = null;

  /**
   * Name of joram admin to get from the file.
   */
  String joramAdmName = "default";

  /** Working attribute used during configuration's */
  String conf = null;

  Logger logger = null;

  /**
   * Launches the XML parser.
   */
  public boolean parse(Reader cfgReader, String cfgName) throws Exception {
    this.joramAdmName = cfgName;

    logger = JoramTracing.dbgAdmin;

    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser parser = factory.newSAXParser();
    parser.parse(new InputSource(cfgReader), this);

    return result;
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
    logger.log(BasicLevel.FATAL,
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
    logger.log(BasicLevel.ERROR,
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
    if (logger.isLoggable(BasicLevel.WARN))
      logger.log(BasicLevel.WARN,
                 "warning parsing " + e.getPublicId() +
                 " at " + e.getLineNumber() + "." + e.getColumnNumber());
    throw e;
  }

  private final boolean isSet(String value) {
    return value != null && value.length() > 0;
  }

  /**
   * Initializes parsing of a document.
   *
   * @exception SAXException
   *	unspecialized error
   */
  public void startDocument() throws SAXException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "startDocument");
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

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "startElement: " + rawName);

    if (rawName.equals(ELT_JORAMADMIN)) {
      conf = atts.getValue(ATT_NAME);
      if (conf == null) conf = joramAdmName;
    } else if (joramAdmName.equals(conf)) {
      if (rawName.equals(ELT_ADMINMODULE)) {
      } else if (rawName.equals(ELT_CONNECT)) {
        try {
          try {
            // Get the hostname of server for administrator connection.
            host = atts.getValue(ATT_HOST);
            if (!isSet(host)) host = DFLT_LISTEN_HOST;
            // Get the listen port of server for administrator connection.
            String value = atts.getValue(ATT_PORT);
            if (value == null)
              port = DFLT_LISTEN_PORT;
            else
              port = Integer.parseInt(value);
            // Get the username for administrator connection.
            name = atts.getValue(ATT_NAME);
            if (!isSet(name))
              name = AbstractConnectionFactory.getDefaultRootLogin();
            // Get the password for administrator connection.
            password = atts.getValue(ATT_PASSWORD);
            if (!isSet(password))
              password = AbstractConnectionFactory.getDefaultRootPassword();
            // Get the CnxTimer attribute for administrator connection.
            value = atts.getValue(ATT_CNXTIMER);
            if (value == null)
              cnxTimer = 60;
            else
              cnxTimer = Integer.parseInt(value);
            // Get the protocol implementation.
            reliableClass = atts.getValue(ATT_RELIABLECLASS);
          } catch (NumberFormatException exc) {
            throw new Exception("bad value for port: " +
                                atts.getValue(ATT_PORT) +
                                " or cnxTimer: " +
                                atts.getValue(ATT_CNXTIMER));
          }
        } catch (Exception exc) {
          throw new SAXException(exc.getMessage(), exc);
        }
      } else if (rawName.equals(ELT_COLLOCATEDCONNECT)) {
        try {
          name = atts.getValue(ATT_NAME);
          if (!isSet(name))
            name = AbstractConnectionFactory.getDefaultRootLogin();
          password = atts.getValue(ATT_PASSWORD);
          if (!isSet(password))
            password = AbstractConnectionFactory.getDefaultRootPassword();
        } catch (Exception exc) {
          throw new SAXException(exc.getMessage(), exc);
        }
      } else if (rawName.equals(ELT_CONNECTIONFACTORY)) {
        try {
          name = atts.getValue(ATT_NAME);
          className = atts.getValue(ATT_CLASSNAME);
          if (!isSet(className)) className = DFLT_CF;
        } catch (Exception exc) {
          throw new SAXException(exc.getMessage(), exc);
        }
      } else if (rawName.equals(ELT_TCP)) {
        try {
          try {
            host = atts.getValue(ATT_HOST);
            if (!isSet(host)) host = DFLT_LISTEN_HOST;
            String value = atts.getValue(ATT_PORT);
            if (value == null)
              port = DFLT_LISTEN_PORT;
            else
              port = Integer.parseInt(value);
            reliableClass = atts.getValue(ATT_RELIABLECLASS);
          } catch (NumberFormatException exc) {
            throw new Exception("bad value for port: " +
                                atts.getValue(ATT_PORT));
          }
        } catch (Exception exc) {
          throw new SAXException(exc.getMessage(), exc);
        }
      } else if (rawName.equals(ELT_LOCAL)) {
      } else if (rawName.equals(ELT_HATCP)) {
        try {
          url = atts.getValue(ATT_URL);
          reliableClass = atts.getValue(ATT_RELIABLECLASS);
        } catch (Exception exc) {
          throw new SAXException(exc.getMessage(), exc);
        }
      } else if (rawName.equals(ELT_HALOCAL)) {
      } else if (rawName.equals(ELT_SOAP)) {
        try {
          try {
            host = atts.getValue(ATT_HOST);
            if (!isSet(host))
              host = "localhost";
            String value = atts.getValue(ATT_PORT);
            if (value == null)
              port = 16010;
            else
              port = Integer.parseInt(value);
            value = atts.getValue(ATT_TIMEOUT);
            if (value == null)
              timeout = 60;
            else
              timeout = Integer.parseInt(value);
          } catch (NumberFormatException exc) {
            throw new Exception("bad value for port: " +
                                atts.getValue(ATT_PORT));
          }
        } catch (Exception exc) {
          throw new SAXException(exc.getMessage(), exc);
        }
      } else if (rawName.equals(ELT_JNDI)) {
        try {
          jndiName = atts.getValue(ATT_NAME);
        } catch (Exception exc) {
          throw new SAXException(exc.getMessage(), exc);
        }
      } else if (rawName.equals(ELT_USER)) {
        try {
          try {
            name = atts.getValue(ATT_NAME);
            login = atts.getValue(ATT_LOGIN);
            password = atts.getValue(ATT_PASSWORD);
            String value = atts.getValue(ATT_SERVERID);
            if (value == null)
              serverId =  AdminModule.getLocalServerId();
            else
              serverId = Integer.parseInt(value);
            dmq = atts.getValue(ATT_DMQ);
            value = atts.getValue(ATT_THRESHOLD);
            if (value == null)
              threshold = -1;
            else
              threshold = Integer.parseInt(value);
          } catch (NumberFormatException exc) {
            throw new Exception("bad value for serverId: " +
                                atts.getValue(ATT_SERVERID));
          }
        } catch (Exception exc) {
          throw new SAXException(exc.getMessage(), exc);
        }
      } else if (rawName.equals(ELT_DESTINATION)) {
        try {
          try {
            type = atts.getValue(ATT_TYPE);
            name = atts.getValue(ATT_NAME);
            String value = atts.getValue(ATT_SERVERID);
            if (value == null)
              serverId = AdminModule.getLocalServerId();
            else
              serverId = Integer.parseInt(value);
            className = atts.getValue(ATT_CLASSNAME);
            dmq = atts.getValue(ATT_DMQ);
          } catch (NumberFormatException exc) {
            throw new Exception("bad value for serverId: " +
                                atts.getValue(ATT_SERVERID));
          }
        } catch (Exception exc) {
          throw new SAXException(exc.getMessage(), exc);
        }
      } else if (rawName.equals(ELT_QUEUE)) {
        try {
          try {
            name = atts.getValue(ATT_NAME);
            String value = atts.getValue(ATT_SERVERID);
            if (value == null)
              serverId = AdminModule.getLocalServerId();
            else
              serverId = Integer.parseInt(value);
            className = atts.getValue(ATT_CLASSNAME);
            dmq = atts.getValue(ATT_DMQ);
            value = atts.getValue(ATT_THRESHOLD);
            if (value == null)
              threshold = -1;
            else
              threshold = Integer.parseInt(value);
            value = atts.getValue(ATT_NBMAXMSG);
            if (value == null)
              nbMaxMsg = -1;
            else
              nbMaxMsg = Integer.parseInt(value);
          } catch (NumberFormatException exc) {
            throw new Exception("bad value for serverId: " +
                                atts.getValue(ATT_SERVERID));
          }
        } catch (Exception exc) {
          throw new SAXException(exc.getMessage(), exc);
        }
      } else if (rawName.equals(ELT_TOPIC)) {
        try {
          try {
            name = atts.getValue(ATT_NAME);
            String value = atts.getValue(ATT_SERVERID);
            if (value == null)
              serverId = AdminModule.getLocalServerId();
            else
              serverId = Integer.parseInt(value);
            className = atts.getValue(ATT_CLASSNAME);
            dmq = atts.getValue(ATT_DMQ);
            parent = atts.getValue(ATT_PARENT);
          } catch (NumberFormatException exc) {
            throw new Exception("bad value for serverId: " +
                                atts.getValue(ATT_SERVERID));
          }
        } catch (Exception exc) {
          throw new SAXException(exc.getMessage(), exc);
        }
      } else if (rawName.equals(ELT_DMQUEUE)) {
        try {
          try {
            name = atts.getValue(ATT_NAME);
            String value = atts.getValue(ATT_SERVERID);
            if (value == null)
              serverId = AdminModule.getLocalServerId();
            else
              serverId = Integer.parseInt(value);
          } catch (NumberFormatException exc) {
            throw new Exception("bad value for serverId: " +
                                atts.getValue(ATT_SERVERID));
          }
        } catch (Exception exc) {
          throw new SAXException(exc.getMessage(), exc);
        }
      } else if (rawName.equals(ELT_PROPERTY)) {
        try {
          if (properties == null)
            properties = new Properties();
          properties.put(atts.getValue(ATT_NAME),
                         atts.getValue(ATT_VALUE));
        } catch (Exception exc) {
          throw new SAXException(exc.getMessage(), exc);
        }
      } else if (rawName.equals(ELT_READER)) {
        try {
          user = atts.getValue(ATT_USER);
        } catch (Exception exc) {
          throw new SAXException(exc.getMessage(), exc);
        }
      } else if (rawName.equals(ELT_WRITER)) {
        try {
          user = atts.getValue(ATT_USER);
        } catch (Exception exc) {
          throw new SAXException(exc.getMessage(), exc);
        }
      } else if (rawName.equals(ELT_FREEREADER)) {
        freeReading = true;
      } else if (rawName.equals(ELT_FREEWRITER)) {
        freeWriting = true;
      } else if (rawName.equals(ELT_INITIALCONTEXT)) {
      } else if (rawName.equals(ELT_CLUSTER_ELEMENT)) {
        try {
          logger.log(BasicLevel.FATAL, "+++" + atts.getValue(ATT_NAME) + ", " +atts.getValue(ATT_LOCATION));
          cluster.put(atts.getValue(ATT_NAME),
                      atts.getValue(ATT_LOCATION));
        } catch (Exception exc) {
          throw new SAXException(exc.getMessage(), exc);
        }
      } else if (rawName.equals(ELT_CLUSTER_QUEUE)) {
        cluster.clear();
      } else if (rawName.equals(ELT_CLUSTER_TOPIC)) {
        cluster.clear();
      } else if (rawName.equals(ELT_CLUSTER_CF)) {
        cluster.clear();
      } else {
	throw new SAXException("unknown element \"" + rawName + "\"");
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

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "endElement: " + rawName);

    if (rawName.equals(ELT_JORAMADMIN)) {
      conf = null;
    } else if (joramAdmName.equals(conf)) {
      try {
        if (rawName.equals(ELT_ADMINMODULE)) {
        } else if (rawName.equals(ELT_CONNECT)) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "AdminModule.connect(" +
                       host + "," +
                       port + "," +
                       name + "," +
                       password + "," +
                       cnxTimer + "," +
                       reliableClass + ")");
          AdminModule.connect(host,port,name,password,cnxTimer,reliableClass);
        } else if (rawName.equals(ELT_COLLOCATEDCONNECT)) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "AdminModule.collocatedConnect(" +
                       name + "," +
                       password + ")");
          AdminModule.collocatedConnect(name,password);
        } else if (rawName.equals(ELT_CONNECTIONFACTORY)) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "cf \""+ name + "\"= " + obj);
          // Bind the ConnectionFactory in JNDI.
          // Be Careful, currently only one binding is handled.
          if (isSet(jndiName))
            toBind.put(jndiName, obj);
          jndiName = null;
          // Register the CF in order to handle it later (cluster, etc.)
          if (isSet(name)) cfs.put(name, obj);

          className = null;
          obj = null;
        } else if (rawName.equals(ELT_TCP)) {
          Class clazz = Class.forName(className);
          Class [] classParams = {new String().getClass(),
                                  Integer.TYPE,
                                  new String().getClass()};
          Method methode = clazz.getMethod("create", classParams);
          Object[] objParams = {host, new Integer(port), reliableClass};
          obj = methode.invoke(null, objParams);
        } else if (rawName.equals(ELT_LOCAL)) {
          Class clazz = Class.forName(className);
          Method methode = clazz.getMethod("create", new Class[0]);
          obj = methode.invoke(null, new Object[0]);
        } else if (rawName.equals(ELT_HATCP)) {
          Class clazz = Class.forName(className);
          Class [] classParams = {new String().getClass(),
                                  new String().getClass()};
          Method methode = clazz.getMethod("create",classParams);
          Object[] objParams = {url,reliableClass};
          obj = methode.invoke(null,objParams);
        } else if (rawName.equals(ELT_HALOCAL)) {
          Class clazz = Class.forName(className);
          Method methode = clazz.getMethod("create", new Class[0]);
          obj = methode.invoke(null, new Object[0]);
        } else if (rawName.equals(ELT_SOAP)) {
          Class clazz = Class.forName(className);
          Class [] classParams = {new String().getClass(),
                                  Integer.TYPE,
                                  Integer.TYPE};
          Method methode = clazz.getMethod("create", classParams);
          Object[] objParams = {host, new Integer(port), new Integer(timeout)};
          obj = methode.invoke(null, objParams);
        } else if (rawName.equals(ELT_JNDI)) {
        } else if (rawName.equals(ELT_USER)) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "User.create(" +
                       name + "," +
                       login + "," +
                       password + "," +
                       serverId + ")");
          if (! isSet(login)) login = name;
          User user = User.create(login, password, serverId);
          users.put(name, user);

          if (threshold > 0)
            user.setThreshold(threshold);

          if (isSet(dmq)) {
            if (dmqs.containsKey(dmq)) {
              user.setDMQ((DeadMQueue) dmqs.get(dmq));
            } else {
              logger.log(BasicLevel.ERROR,
                         "User.create(): Unknown DMQ: " + dmq);
            }
          }
        } else if (rawName.equals(ELT_DESTINATION)) {
          Destination dest = null;
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "dest type =" + type);

          if (type.equals("queue")) {
            if (className == null)
              className = "org.objectweb.joram.mom.dest.Queue";
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, "Queue.create(" +
                         serverId + "," +
                         name + "," +
                         className + "," +
                         properties + ")");
            dest = Queue.create(serverId,
                                name,
                                className,
                                properties);
          } else if (type.equals("topic")) {
            if (className == null)
              className = "org.objectweb.joram.mom.dest.Topic";
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, "Topic.create(" +
                         serverId + "," +
                         name + "," +
                         className + "," +
                         properties + ")");
            dest = Topic.create(serverId,
                                name,
                                className,
                                properties);
          } else
            throw new Exception("type " + type + " bad value. (queue or topic)");

          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "destination = " + dest);

          properties = null;

          configureDestination(dest);

          // Bind the destination in JNDI.
          // Be Careful, currently only one binding is handled.
          if (isSet(jndiName))
            toBind.put(jndiName, dest);
          jndiName = null;
          // Register the destination in order to handle it later.
          String name = dest.getAdminName();
          if (! isSet(name))
            name = dest.getName();
          if (dest instanceof Queue) {
            queues.put(name, dest);
          } else {
            // It's a Topic
            topics.put(name, dest);
          }
          // Fix DMQ if any
          setDestinationDMQ(dest, dmq);
        } else if (rawName.equals(ELT_QUEUE)) {
          if (className == null)
            className = "org.objectweb.joram.mom.dest.Queue";
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "Queue.create(" +
                       serverId + "," +
                       name + "," +
                       className + "," +
                       properties + ")");
          Queue queue = Queue.create(serverId,
                                     name,
                                     className,
                                     properties);
          properties = null;

          configureDestination(queue);

          if (threshold > 0)
            queue.setThreshold(threshold);

          if (nbMaxMsg > 0)
            queue.setNbMaxMsg(nbMaxMsg);

          // Bind the queue in JNDI.
          // Be Careful, currently only one binding is handled.
          if (isSet(jndiName))
            toBind.put(jndiName, queue);
          jndiName = null;
          // Register the queue in order to handle it later (cluster, etc.)
          String name = queue.getAdminName();
          if (! isSet(name)) name = queue.getName();
          queues.put(name, queue);
          // Fix DMQ if any
          setDestinationDMQ(queue, dmq);
        } else if (rawName.equals(ELT_TOPIC)) {
          if (className == null)
            className = "org.objectweb.joram.mom.dest.Topic";
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "Topic.create(" +
                       serverId + "," +
                       name + "," +
                       className + "," +
                       properties + ")");
          Topic topic = Topic.create(serverId,
                                     name,
                                     className,
                                     properties);
          properties = null;

          configureDestination(topic);

          if (isSet(parent)) {
            if (topics.containsKey(parent)) {
              topic.setParent((Topic) topics.get(parent));
            } else {
              logger.log(BasicLevel.ERROR,
                   "Topic.create(): Unknown parent: " + parent);
            }
          }
          // Bind the topic in JNDI.
          // Be Careful, currently only one binding is handled.
          if (isSet(jndiName))
            toBind.put(jndiName, topic);
          jndiName = null;
          // Register the topic in order to handle it later (cluster, etc.)
          String name = topic.getAdminName();
          if (! isSet(name)) name = topic.getName();
          topics.put(name, topic);
          // Fix DMQ if any
          setDestinationDMQ(topic, dmq);
        } else if (rawName.equals(ELT_DMQUEUE)) {
          className = "org.objectweb.joram.mom.dest.DeadMQueue";
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "DeadMQueue.create(" +
                       serverId + "," +
                       name + ")");

          DeadMQueue dmq = (DeadMQueue) DeadMQueue.create(serverId, name);

          configureDestination(dmq);

          // Bind the destination in JNDI.
          // Be Careful, currently only one binding is handled.
          if (isSet(jndiName))
            toBind.put(jndiName, dmq);
          jndiName = null;
          // Register the DMQ in order to handle it later.
          if (isSet(name))
            dmqs.put(name, dmq);
        } else if (rawName.equals(ELT_PROPERTY)) {
        } else if (rawName.equals(ELT_READER)) {
          readers.add(user);
        } else if (rawName.equals(ELT_WRITER)) {
          writers.add(user);
        } else if (rawName.equals(ELT_FREEREADER)) {
        } else if (rawName.equals(ELT_FREEWRITER)) {
        } else if (rawName.equals(ELT_INITIALCONTEXT)) {
          try {
            jndiCtx = new javax.naming.InitialContext(properties);
          } catch (NamingException exc) {
            logger.log(BasicLevel.ERROR,"",exc);
          }
        } else if (rawName.equals(ELT_CLUSTER_ELEMENT)) {
        } else if (rawName.equals(ELT_CLUSTER_CF)) {
          Map.Entry entries[] = new Map.Entry [cluster.size()];
          cluster.entrySet().toArray(entries);
          ClusterConnectionFactory clusterCF = new ClusterConnectionFactory();

          for (int i=0; i<entries.length; i++) {
            ConnectionFactory cf = (ConnectionFactory) cfs.get(entries[i].getKey());
            clusterCF.addConnectionFactory((String) entries[i].getValue(), cf);
          }
          cluster.clear();

          // Bind the destination in JNDI.
          // Be Careful, currently only one binding is handled.
          if (isSet(jndiName))
            toBind.put(jndiName, clusterCF);
          jndiName = null;
        } else if (rawName.equals(ELT_CLUSTER_QUEUE)) {
          Map.Entry entries[] = new Map.Entry [cluster.size()];
          cluster.entrySet().toArray(entries);
          ClusterQueue clusterQueue = new ClusterQueue();

          Queue root = null;
          for (int i=0; i<entries.length; i++) {
            Queue queue = (Queue) queues.get(entries[i].getKey());
            clusterQueue.addDestination((String) entries[i].getValue(), queue);
            if (i == 0)
              root = queue;
            else
              root.addClusteredQueue(queue);
          }
          cluster.clear();

          // Bind the destination in JNDI.
          // Be Careful, currently only one binding is handled.
          if (isSet(jndiName))
            toBind.put(jndiName, clusterQueue);
          jndiName = null;
        } else if (rawName.equals(ELT_CLUSTER_TOPIC)) {
          Map.Entry entries[] = new Map.Entry [cluster.size()];
          cluster.entrySet().toArray(entries);
          ClusterTopic clusterTopic = new ClusterTopic();

          Topic root = null;
          for (int i=0; i<entries.length; i++) {
            Topic topic = (Topic) topics.get(entries[i].getKey());
            clusterTopic.addDestination((String) entries[i].getValue(), topic);
            if (i == 0)
              root = topic;
            else
              root.addClusteredTopic(topic);
          }
          cluster.clear();

          // Bind the destination in JNDI.
          // Be Careful, currently only one binding is handled.
          if (isSet(jndiName))
            toBind.put(jndiName, clusterTopic);
          jndiName = null;
        } else {
          throw new SAXException("unknown element \"" + rawName + "\"");
        }
      } catch (SAXException exc) {
        throw exc;
      } catch (Exception exc) {
        Exception cause = (Exception) exc.getCause();
        if (cause != null) {
          logger.log(BasicLevel.ERROR,"",cause);
          throw new SAXException(cause.getMessage(), cause);
        } else {
          logger.log(BasicLevel.ERROR,"",exc);
          throw new SAXException(exc.getMessage(), exc);
        }
      }
    }
  }

  void configureDestination(Destination dest) throws Exception {
    if (freeReading)
      dest.setFreeReading();
    freeReading = false;

    if (freeWriting)
      dest.setFreeWriting();
    freeWriting = false;

    for (int i = 0; i < readers.size(); i++) {
      User u = (User) users.get(readers.get(i));
      if (u != null)
        dest.setReader(u);
    }
    readers.clear();

    for (int i = 0; i < writers.size(); i++) {
      User u = (User) users.get(writers.get(i));
      if (u != null)
        dest.setWriter(u);
    }
    writers.clear();
  }

  void setDestinationDMQ(Destination dest, String dmq) throws Exception {
    if (isSet(dmq)) {
      if (dmqs.containsKey(dmq)) {
        dest.setDMQ((DeadMQueue) dmqs.get(dmq));
      } else  {
        logger.log(BasicLevel.ERROR,
                   "Destination.create(): Unknown DMQ: " + dmq);
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
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "endDocument");
    AdminModule.disconnect();

    try {
      if (jndiCtx == null)
        jndiCtx = new javax.naming.InitialContext();

      for (Enumeration e = toBind.keys(); e.hasMoreElements();) {
        String name = (String) e.nextElement();
        StringTokenizer st = new StringTokenizer(name, "/");
        StringBuffer buff = new StringBuffer((String) st.nextToken());
        while (st.hasMoreTokens()) {
          try {
            jndiCtx.createSubcontext(buff.toString());
          } catch (NamingException exc) {
            if (logger.isLoggable(BasicLevel.WARN))
              logger.log(BasicLevel.WARN, "createSubcontext", exc);
          }
          buff.append("/");
          buff.append((String) st.nextToken());
        }
        jndiCtx.rebind(name, toBind.get(name));
      }
      jndiCtx.close();
      toBind.clear();

    } catch (NamingException exc) {
      logger.log(BasicLevel.ERROR,"",exc);
    }
  }
}
