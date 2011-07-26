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

package jmx.remote.jms.tests;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.util.logging.*;

import jmx.remote.jms.tests.A;
import joram.jmx.remote.provider.jms.*;

/**
 * This <b><i>JmsJmxConnectorServerLaunch</i></b> Class launches the server
 * connector.
 * 
 * @author Djamel-Eddine Boumchedda
 * 
 */
public class JmsJmxConnectorServerLaunch implements ClassFileTransformer {
  protected static Logger logger;

  public static void main(String[] args) throws SecurityException, IOException {
    JMXServiceURL serverURL = new JMXServiceURL("service:jmx:jms:///tcp://localhost:6000");
    // MBeanServer mbs = MBeanServerFactory.createMBeanServer();
    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    System.out.println(mbs.toString());
    Map serverEnv = new HashMap();
    serverEnv.put("jmx.remote.protocol.provider.pkgs", "joram.jmx.remote.provider");
    JMXConnectorServer connectorServer = null;
    A objetA = new A();
    BroadcastingUser broadcastingUser = new BroadcastingUser("john");
    System.out.println(mbs.toString());
    ObjectName name = null;
    ObjectName name2 = null;
    logger = Logger.getLogger("jmx.remote.jms.JmsJmxConnectorServerLaunch");
    Handler fh = new FileHandler("myLog.log");
    logger.addHandler(fh);
    logger.setLevel(Level.ALL);
    try {
      name = new ObjectName("SimpleAgent:name=A");
      name2 = new ObjectName("SimpleAgent:name=BroadcastingUser");

    } catch (MalformedObjectNameException e1) {

      // TODO Auto-generated catch block
      e1.printStackTrace();
    } catch (NullPointerException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    try {
      mbs.registerMBean(objetA, name);
      mbs.registerMBean(broadcastingUser, name2);
      System.out.println("-->Enregistrement de la classe A dans le MBS");
      System.out.println("-->Enregistrement de la classe BroadcastingUser dans le MBS");

      /*
       * System.out.println("--------------------------------------------------")
       * ; System.out.println(
       * "test de la methode : addNotificationListener(ObjectName name,NotificationListener listener,NotificationFilter filter,Object handback) "
       * );
       * System.out.println("--------------------------------------------------"
       * ); NotificationListener notificationListener = new
       * NotificationListener() {
       * 
       * public void handleNotification(Notification notification, Object
       * handback) { // TODO Auto-generated method stub System.out.println(
       * "--> methode handleNotification(Notification notification, Object handback) est appelée"
       * );
       * 
       * } }; Object handback = new String("handback");
       * mbs.addNotificationListener(name2, notificationListener, null, null);
       * 
       * System.out.println("--------------------------------------------------")
       * ; System.out.println(
       * "fin du test de la methode : addNotificationListener(ObjectName name,NotificationListener listener,NotificationFilter filter,Object handback) "
       * );
       * System.out.println("--------------------------------------------------"
       * ); System.out.println();
       * System.out.println("--------------------------------------------------"
       * ); System.out.println(
       * "RE : test de la methode : addNotificationListener(ObjectName name,NotificationListener listener,NotificationFilter filter,Object handback) "
       * );
       * System.out.println("--------------------------------------------------"
       * ); mbs.addNotificationListener(name2, notificationListener, null,
       * null);
       * System.out.println("--------------------------------------------------"
       * ); System.out.println(
       * "RE : fin du test de la methode : addNotificationListener(ObjectName name,NotificationListener listener,NotificationFilter filter,Object handback) "
       * );
       * System.out.println("--------------------------------------------------"
       * ); String operationName3; Object[] params3 = new Object[0]; String[]
       * sig3 = new String[0]; operationName3 = "remove"; mbs.invoke(name2,
       * operationName3, params3, sig3); System.out.println();
       * System.out.println
       * ("--------------------------------------------------");
       * System.out.println(
       * "test de la methode : removeNotificationListener(ObjectName name,NotificationListener listener)"
       * );
       * System.out.println("--------------------------------------------------"
       * ); try { mbs.removeNotificationListener(name2, notificationListener);
       * System.out.println("je suis la!!!!!!!!"); } catch
       * (ListenerNotFoundException e) { // TODO Auto-generated catch block
       * e.printStackTrace(); }
       * System.out.println("--------------------------------------------------"
       * ); System.out.println(
       * "fin du test de la methode : removeNotificationListener(ObjectName name,NotificationListener listener)"
       * );
       * System.out.println("--------------------------------------------------"
       * ); System.out.println();
       * 
       * mbs.invoke(name2, operationName3, params3, sig3);
       */

    } catch (InstanceAlreadyExistsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (MBeanRegistrationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (NotCompliantMBeanException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } /*
       * catch (InstanceNotFoundException e) { // TODO Auto-generated catch
       * block e.printStackTrace(); } catch (ReflectionException e) { // TODO
       * Auto-generated catch block e.printStackTrace(); }
       */catch (MBeanException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    try {
      connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(serverURL, serverEnv, mbs);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    try {
      connectorServer.start();
      System.out.println("!!--> Le JmsJmxConnecteur Serveur est lancé voici son addresse :" + serverURL);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
      ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
    // TODO Auto-generated method stub
    return null;
  }

}
