/*
 * Copyright (C) 2002 - 2008 ScalAgent Distributed Technologies 
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

import org.kxml.parser.*;
import org.kxml.*;

/**
 * A3CML XML parser using kxml.
 */
public class A3CMLKXmlWrapper implements A3CMLWrapper {
  protected A3CMLConfig a3cmlconfig = null;

  public A3CMLKXmlWrapper() { }

  public static final String getValue(Vector atts, String qName) {
    if(atts != null){
      for (int i=0; i<atts.size(); i++) {
        if (((Attribute) atts.elementAt(i)).getName().equals(qName))
          return ((Attribute) atts.elementAt(i)).getValue();
      }
    }
    return null;
  }

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
   * Parses the xml file named <code>cfgFileName</code>.
   *
   * @param cfgFileName    the name of the xml file
   * @param configName     the name of the configuration
   *
   * @exception Exception  unspecialized error
   */
  public A3CMLConfig parse(Reader reader,
                           String configName) throws Exception {
    this.configName = configName;

    a3cmlconfig = new A3CMLConfig();

    // instantiation du parser
    XmlParser parser = new XmlParser(reader);

    ParseEvent event = parser.read();
    while (event.getType() != Xml.END_DOCUMENT) {
      switch (event.getType()) {
      case Xml.START_DOCUMENT:
        break;
      case Xml.START_TAG: {
        String name = event.getName();

        if (name.equals(A3CML.ELT_CONFIG)) {
          conf = getValue(event.getAttributes(),
                          A3CML.ATT_NAME);
          if (conf == null)
            conf = configName;
        } else if (configName.equals(conf)) {
          Vector atts = event.getAttributes();

          if (name.equals(A3CML.ELT_DOMAIN)) {
            domain = new A3CMLDomain(
              getValue(atts, A3CML.ATT_NAME),
              getValue(atts, A3CML.ATT_NETWORK));
          } else if (name.equals(A3CML.ELT_SERVER)) {
            short sid;
            try {
              sid = Short.parseShort(getValue(atts, A3CML.ATT_ID));
            } catch (NumberFormatException exc) {
              throw new Exception("bad value for server id: " +
                                  getValue(atts, A3CML.ATT_ID));
            }
            server = new A3CMLServer(
              sid,
              getValue(atts, A3CML.ATT_NAME),
              getValue(atts, A3CML.ATT_HOSTNAME));
          } else if (name.equals(A3CML.ELT_NETWORK)) {
            int port;
            try {
              port = Integer.parseInt(getValue(atts, A3CML.ATT_PORT));
            } catch (NumberFormatException exc) {
              throw new Exception("bad value for network port: " +
                                  getValue(atts, A3CML.ATT_PORT));
            }
            network = new A3CMLNetwork(
              getValue(atts, A3CML.ATT_DOMAIN),
              port);
          } else if (name.equals(A3CML.ELT_SERVICE)) {
            service = new A3CMLService(
              getValue(atts, A3CML.ATT_CLASS),
              getValue(atts, A3CML.ATT_ARGS));
          } else if (name.equals(A3CML.ELT_PROPERTY)) {
            property = new A3CMLProperty(
              getValue(atts, A3CML.ATT_NAME),
              getValue(atts, A3CML.ATT_VALUE));
          } else if (name.equals(A3CML.ELT_NAT)) {
            nat = new A3CMLNat(Short.parseShort(getValue(atts, A3CML.ATT_SID)),
                               getValue(atts, A3CML.ATT_HOSTNAME),
                               Integer.parseInt(getValue(atts, A3CML.ATT_PORT)));
          } else if (name.equals(A3CML.ELT_JVM_ARGS)) {
            jvmArgs = getValue(atts, A3CML.ATT_VALUE);
          } else {
            throw new Exception("unknown element \"" + name + "\"");
          }
        }
        break;
      }
      case Xml.END_TAG: {
        String name = event.getName();

        if (name.equals(A3CML.ELT_CONFIG)) {
          conf = null;
        } else if (configName.equals(conf)) {
          if (name.equals(A3CML.ELT_DOMAIN)) {
            a3cmlconfig.addDomain(domain);
            domain = null;
          } else if (name.equals(A3CML.ELT_SERVER)) {
            a3cmlconfig.addServer(server);
            server = null;
          } else if (name.equals(A3CML.ELT_NETWORK)) {
            if (server != null) {
              server.addNetwork(network);
              // Add the server to the corresponding domains
              // AF: This step should be done at the end of parsing, in order
              // to avoid to declare domains first.
              a3cmlconfig.getDomain(network.domain).addServer(server);
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
            if (server ==  null) 
              a3cmlconfig.addProperty(property);	// Global property
            else 
              server.addProperty(property);		// Server property
            property = null;
          } else if (name.equals(A3CML.ELT_NAT)) {
            if (server !=  null) 
              server.addNat(nat);
            nat = null;
          } else if (name.equals(A3CML.ELT_JVM_ARGS)) {
            if (server != null && jvmArgs != null)
              server.jvmArgs = jvmArgs;
            jvmArgs = null;
          } else {
            throw new Exception("unknown element \"" + name + "\"");
          }
        }
        break;
      }
      }
      event = parser.read();
    }

    return a3cmlconfig;
  }
}
