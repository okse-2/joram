/**
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

package jmx.remote.jms.agent;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import net.java.balloontip.BalloonTip;
import net.java.balloontip.utils.TimingUtils;

import jmx.remote.jms.ShowMessageInformations;
import jmx.remote.jms.tests.A;
import jmx.remote.jms.tests.BroadcastingUser;
import jmx.remote.jms.tests.JmsJmxConnectorServerLaunch;

/**
 * the <b>JmsJmxConnectorServerAgent </b> class launches the server connector in
 * an agent which is launched with JVM.
 * <p>
 * <ul>
 * <li>
 * <ul>
 * <li>An agent comes in the form of a JAR that will contain:
 * </ul>
 * </li>
 * <ul>
 * </ul>
 * <ul>
 * - specific entries in Manifest
 * </ul>
 * <ul>
 * - method 'premain' serving as an entry point for the agent, here are the
 * possible signatures:
 * </ul>
 * <ul>
 * </ul>
 * <ul>
 * <li>When starting the JVM, the methods 'premain' of each of the agents
 * present on the command line will be called before starting the application
 * itself. Although their names may make us think about running a third-party
 * application, the agents are loaded into the system classloader of the JVM
 * with the same safety rules as the application. No particular restriction is
 * given on the code executed by the agents: they can create threads, using
 * introspection, ... as they see fit.
 * </ul>
 * </li>
 * 
 * <ul>
 * <li>The Manifest that describes the agent is composed of a number of
 * attributes. The only mandatory attribute is Premain-Class. It is the class
 * containing the method to be called by premain agent
 * </ul>
 * </li>
 * 
 * </li> </ul>
 * </p>
 * 
 * @author Djamel-Eddine Boumchedda
 * @version 1.3
 * 
 */

public class JmsJmxConnectorServerAgent {

  public static void premain(String agentArgs, Instrumentation inst) throws MalformedURLException {
    // System.out.println("*** Premain method is called : loading " +
    // JmsJmxConnectorServerLaunch.class.getSimpleName());
    // inst.addTransformer(new JmsJmxConnectorServerLaunch());
    JMXServiceURL serverURL = new JMXServiceURL("service:jmx:jms:///tcp://localhost:6000");
    // MBeanServer mbs = MBeanServerFactory.createMBeanServer();
    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    System.out.println(mbs.toString());
    Map serverEnv = new HashMap();
    serverEnv.put("jmx.remote.protocol.provider.pkgs", "joram.jmx.remote.provider");
    JMXConnectorServer connectorServer = null;
    try {
      connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(serverURL, serverEnv, mbs);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    try {
      connectorServer.start();
      System.out.println("!!--> Le JmsJmxConnecteur Serveur est lancé voici son addresse :" + serverURL);
      // me.getComponent();
      // BalloonTip balloonTip = new BalloonTip((JComponent) me.getComponent(),
      // "I will dissapear in 3 seconds.");
      // Now make the balloon tip disappear in 3000 milliseconds
      // TimingUtils.showTimedBalloon(balloonTip, 3000);
      ShowMessageInformations showMessageInformations = new ShowMessageInformations(null,
          "The Connector Server is launch with these URL adress :" + serverURL, "Lauch of Connector Server ",
          JOptionPane.INFORMATION_MESSAGE);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
}