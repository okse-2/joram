/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 ScalAgent Distributed Technologies
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
 * Initial developer(s): Djamel-Eddine Boumchedda
 * 
 */
package org.ow2.joram.jmxconnector.server.agent;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;


import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.ow2.joram.jmxconnector.JMXConnectorMetadata;

import fr.dyade.aaa.common.Debug;

/**
 * This class initiates a JMS server connector using an agent which is
 * launched in a new JVM.
 * <p>
 * An agent comes in the form of a JAR that will contain:<ul>
 * <li>specific entries in Manifest,</li>
 * <li>method 'premain' serving as an entry point for the agent.</li>
 * </ul></p><p>
 * When starting the JVM, the methods 'premain' of each agent present on
 * the command line will be called before starting the application itself.
 * Although their names may make us think about running a third-party
 * application, the agents are loaded into the system classloader of the JVM
 * with the same safety rules as the application. No particular restriction is
 * given on the code executed by the agents: they can create threads, using
 * introspection, ... as they see fit.
 * </p><p>
 * The Manifest that describes the agent is composed of a number of
 * attributes. The only mandatory attribute is Premain-Class. It is the class
 * containing the method to be called by premain agent.</p>
 * 
 * @author Djamel-Eddine Boumchedda
 */

public class JMSConnectorServerAgent {
  private static final Logger logger = Debug.getLogger(JMSConnectorServerAgent.class.getName());

  /**
   * Name of the property allowing to define the URL of Joram server used
   * by the instantiated connector.
   */
  private static final String URL_NAME = "org.ow2.joram.jmxconnector.server.url";
  
  public static void premain(String args, Instrumentation inst) throws MalformedURLException {
    System.err.println("JMSConnectorServerAgent.premain -> " + JMXConnectorMetadata.JMS_SERVER_PACKAGE);
    JMXServiceURL url = new JMXServiceURL(System.getProperty(URL_NAME, JMXConnectorMetadata.DefaultURL));
    Map env = new HashMap();
    env.put(JMXConnectorServerFactory.PROTOCOL_PROVIDER_PACKAGES, JMXConnectorMetadata.JMS_SERVER_PACKAGE);
    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    JMXConnectorServer connectorServer = null;
    try {
      connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
      System.err.println("JMSConnectorServerAgent: connector created");
    } catch (IOException exc) {
      exc.printStackTrace();
      logger.log(BasicLevel.ERROR,
                 "JMSConnectorServerAgent: Cannot instantiate JMXConnectorServer", exc);
      return;
    }
    try {
      connectorServer.start();
      System.out.println("!!--> The Server JmsJmxConnecteur  is launched with this address :" + url);
    } catch (IOException exc) {
      logger.log(BasicLevel.ERROR,
                 "JMSConnectorServerAgent: Cannot start JMXConnectorServer", exc);
    }
  }
}