/*
 * Copyright (C) 2002 - 2012 ScalAgent Distributed Technologies 
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

import fr.dyade.aaa.agent.AgentServer;

/**
 * Defines XML syntactic element for A3CML configuration file.
 */
public class A3CML {
  /** Syntaxic name for config element */
  static final String ELT_CONFIG = "config";
  /** Syntaxic name for domain element */
  static final String ELT_DOMAIN = "domain";
  /** Syntaxic name for server element */
  static final String ELT_SERVER = "server";
  /** Syntaxic name for network element */
  static final String ELT_NETWORK = "network";
  /** Syntaxic name for service element */
  static final String ELT_SERVICE = "service";
  /** Syntaxic name for property element */
  static final String ELT_PROPERTY = "property";
  /** Syntaxic name for nat element */
  static final String ELT_NAT = "nat";
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
  /** Syntaxic name for sid attribute */
  static final String ATT_SID = "sid";

  static final String TAB = "  ";
  static final String TAB2 = TAB + TAB;
  /**
   * write a configuration in an A3CML file.
   */
  public static final void toXML(A3CMLConfig config,
                                 String cfgDir,
                                 String xmlFileName) throws Exception {
    File xmlFile = new File(cfgDir, xmlFileName);
    PrintWriter out = new PrintWriter(new FileWriter(xmlFile));
    toXML(config, out);
  }
    
  public static final void toXML(A3CMLConfig config,
                                 PrintWriter out) throws Exception {
    // out.write("<?xml version=\"1.0\"?>\n");
//     out.write("<!DOCTYPE config SYSTEM \"a3config.dtd\">\n");
    out.write("\n<" + ELT_CONFIG + ">\n\n");

    // write all property
    for (Enumeration e = config.properties.elements();
         e.hasMoreElements();) {
      A3CMLProperty p = (A3CMLProperty) e.nextElement();
      out.write(TAB +
                "<" + ELT_PROPERTY + " " +
                ATT_NAME + "=\"");
      out.write(p.name);
      out.write("\" " +
                ATT_VALUE + "=\"");
      out.write(p.value);
      out.write("\"/>\n");
    }
    out.write("\n");

    // write all know domain
    for (Enumeration e = config.domains.elements();
         e.hasMoreElements();) {
      A3CMLDomain d = (A3CMLDomain) e.nextElement();
      out.write(TAB + "<" + ELT_DOMAIN + " " + ATT_NAME + "=\"");
      out.write(d.name);
      out.write("\" " + ATT_NETWORK + "=\"");
      out.write(d.network);
      out.write("\"/>\n");
    }
    out.write("\n");

    // write all know servers.
    for (Enumeration e = config.servers.elements();
         e.hasMoreElements();) {
      Object obj = e.nextElement();

      if (obj instanceof A3CMLServer)
        writeToXMLServer(obj,out);
      out.write("\n");
    }

    out.write("</" + ELT_CONFIG + ">\n");
    out.flush();
  }

  private static final void writeToXMLServer(Object obj,
                                             PrintWriter out) {
    if (obj instanceof A3CMLServer) {
      A3CMLServer server = (A3CMLServer) obj;
      out.write(TAB + "<" + ELT_SERVER + " " + ATT_HOSTNAME + "=\"");
      out.write(server.hostname);
      out.write("\" " + ATT_ID + "=\"");
      out.write(Short.toString(server.sid));
      out.write("\" " + ATT_NAME + "=\"");
      out.write(server.name);
      out.write("\">\n");
          
      // jvm args
      if (server.jvmArgs != null && server.jvmArgs.length() > 0) {
        out.write(TAB2 + "<" + ELT_JVM_ARGS + " " + ATT_VALUE + "=\"");
        out.write(server.jvmArgs);
        out.write("\"/>\n");
      }

      // write all property
      if (server.properties != null) {
        for (Enumeration e = server.properties.elements();
             e.hasMoreElements();) {
          A3CMLProperty p = (A3CMLProperty) e.nextElement();
          out.write(TAB2 + "<" + ELT_PROPERTY + " " + ATT_NAME + "=\"");
          out.write(p.name);
          out.write("\" " + ATT_VALUE + "=\"");
          out.write(p.value);
          out.write("\"/>\n");
        }
      }

      // write all Nat
      if (server.nat != null) {
        for (Enumeration e = server.nat.elements();
             e.hasMoreElements();) {
          A3CMLNat n = (A3CMLNat) e.nextElement();
          out.write(TAB2 + "<" + ELT_NAT + " " + ATT_SID + "=\"");
          out.write(Short.toString(n.sid));
          out.write("\" " + ATT_HOSTNAME + "=\"");
          out.write(n.host);
          out.write("\" " + ATT_PORT + "=\"");
          out.write(Integer.toString(n.port));
          out.write("\"/>\n");
        }
      }

      // network
      if (server.networks != null) {
        for (Enumeration e = server.networks.elements();
             e.hasMoreElements();) {
          A3CMLNetwork n = (A3CMLNetwork) e.nextElement();
          out.write(TAB2 + "<" + ELT_NETWORK + " " + ATT_DOMAIN + "=\"");
          out.write(n.domain);
          out.write("\" " + ATT_PORT + "=\"");
          out.write(Integer.toString(n.port));
          out.write("\"/>\n");
        }
      }

      //service
      if (server.services != null) {
        for (Enumeration e = server.services.elements();
             e.hasMoreElements();) {
          A3CMLService service = (A3CMLService) e.nextElement();
          out.write(TAB2 + "<" + ELT_SERVICE + " " + ATT_CLASS + "=\"");
          out.write(service.classname);
          out.write("\" " + ATT_ARGS + "=\"");
          if ((service.args != null) && (service.args.length() > 0)) {
            out.write(service.args);
          }
          out.write("\"/>\n");
        }
      }
      out.write(TAB + "</" + ELT_SERVER + ">\n");
    }
  }

  /**
   * Gets an agent server configuration from a XML file. This method
   * fills the object graph configuration in the <code>A3CMLConfig</code>
   * object.
   *
   * @return   the <code>A3CMLConfig</code> object if file exists and is
   * 	       correct, null otherwise.
   *
   * @exception Exception
   *	unspecialized exception when reading and parsing the configuration file
   */
  public static A3CMLConfig getXMLConfig() throws Exception {
    String cfgDir = System.getProperty(AgentServer.CFG_DIR_PROPERTY, 
                                       AgentServer.DEFAULT_CFG_DIR);
    String cfgFile = System.getProperty(AgentServer.CFG_FILE_PROPERTY,
                                        AgentServer.DEFAULT_CFG_FILE);
    return getXMLConfig(cfgDir,cfgFile);
  }

  /**
   * Gets an agent server configuration from a XML file. This method
   * fills the object graph configuration in the <code>A3CMLConfig</code>
   * object.
   *
   * @param cfgDir        directory of XML file
   * @param cfgFile       XML configuration file
   * @return   the <code>A3CMLConfig</code> object if file exists and is
   * 	       correct, null otherwise.
   *
   * @exception Exception
   *	unspecialized exception when reading and parsing the configuration file
   */
  public static A3CMLConfig getXMLConfig(String cfgDir,
                                         String cfgFileName) throws Exception {
    return getXMLConfig(new File(cfgDir, cfgFileName).getPath());
  }

  public static A3CMLConfig getXMLConfig(String path) throws Exception {
    if (Log.logger.isLoggable(BasicLevel.DEBUG))
      Log.logger.log(BasicLevel.DEBUG,"Config.getXMLConfig(" + path + ")");
    
    A3CMLConfig a3cmlconfig = null;    
    Reader reader = null;

    // 1st, search XML configuration file in directory.    
    File cfgFile = new File(path);
    try {
      if (!cfgFile.exists() || !cfgFile.isFile() || (cfgFile.length() == 0)) {
        throw new IOException();
      }
      reader = new FileReader(cfgFile);
    } catch (IOException exc) {
      // configuration file seems not exist, search it from the
      // search path used to load classes.
      Log.logger.log(BasicLevel.WARN,
                     "Unable to find configuration file \"" +
                     cfgFile.getPath() + "\".");
      reader = null;
    }
      
    // 2nd, search XML configuration file in path used to load classes.
    if (reader == null) {
      ClassLoader classLoader = null;
      InputStream is = null;
      try {
        classLoader = A3CMLConfig.class.getClassLoader();
        if (classLoader != null) {
          Log.logger.log(BasicLevel.WARN,
                         "Trying to find [" + path + "] using " +
                         classLoader + " class loader.");
          is = classLoader.getResourceAsStream(path);
        }
      } catch (Throwable t) {
        Log.logger.log(BasicLevel.WARN,
                       "Can't find [" + path + "] using " +
                       classLoader + " class loader.",
                       t);
        is = null;
      }
      
      if (is == null) {
        // Last ditch attempt: get the resource from the class path.
        Log.logger.log(BasicLevel.WARN,
                       "Trying to find [" + path +
                       "] using ClassLoader.getSystemResource().");
        is = ClassLoader.getSystemResourceAsStream(path);
      }
      if (is != null) {
        a3cmlconfig = getConfig(new InputStreamReader(is));
      }
    } else {
      a3cmlconfig = getConfig(reader);
    }
    
    if (Log.logger.isLoggable(BasicLevel.DEBUG))
      Log.logger.log(BasicLevel.DEBUG, "a3cmlconfig = " + a3cmlconfig);
    
    if (a3cmlconfig == null)
      throw new FileNotFoundException("xml configuration file not found.");

    return a3cmlconfig;
  }

  /**
   * Gets configuration of agent servers from a XML file. This method
   * fills the object graph configuration in the <code>Config</code>
   * object.
   *
   * @param reader      Reader
   * @param cfgFileName configuration file name (XML file)
   * @return	        the <code>Config</code> object if file exists and is
   * 		        correct, null otherwise.
   *
   * @exception Exception
   *	unspecialized exception when reading and parsing the configuration file
   */
  public static A3CMLConfig getConfig(Reader reader) 
    throws Exception {
    if (Log.logger.isLoggable(BasicLevel.DEBUG))
      Log.logger.log(BasicLevel.DEBUG, "Config.getConfig(" + reader + ")");
    
    String cfgName = System.getProperty(AgentServer.CFG_NAME_PROPERTY, 
                                        AgentServer.DEFAULT_CFG_NAME);
    String wrpCName = System.getProperty(AgentServer.A3CMLWRP_PROPERTY, 
                                         AgentServer.DEFAULT_A3CMLWRP);
    Class<?> wrpClass = Class.forName(wrpCName);

    A3CMLWrapper wrapper = (A3CMLWrapper) wrpClass.newInstance();
    A3CMLConfig a3config = null;
    try {
      a3config = wrapper.parse(reader,cfgName);
    } catch (Exception exc) {
        Log.logger.log(BasicLevel.ERROR,
                   "Config.getConfig : " + exc.getMessage(), 
                   exc);
    }

    if ((a3config == null) || (a3config.servers == null))
      throw new Exception("Empty configuration \"" + cfgName +
                          "\" in configuration file.");

    return a3config;
  }
}
