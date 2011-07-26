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

package jmx.remote.jms.tests;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.NamingException;

import org.objectweb.joram.client.jms.admin.AdminException;

/**
 * <b><i>JmsJmxConnectorClientLaunch</i></b> launches the client connector,and
 * made a series of test.
 * 
 * @author Djamel-Eddine Boumchedda
 * 
 */
public class JmsJmxConnectorClientLaunch {
  public static void main(String[] args) throws IOException, MalformedObjectNameException,
      NullPointerException, AttributeNotFoundException, InstanceNotFoundException, MBeanException,
      ReflectionException {
    JMXServiceURL clientURL = new JMXServiceURL("service:jmx:jms:///tcp://localhost:6000");
    Map clientEnv = new HashMap();
    clientEnv.put("jmx.remote.protocol.provider.pkgs", "joram.jmx.remote.provider");
    JMXConnector clientConnector = JMXConnectorFactory.connect(clientURL, clientEnv);
    // Connect a JSR 160 JMXConnector to the server side
    // connector=JMXConnectorFactory.connect(clientURL,clientEnv);
    // now test the Connection
    System.out.println("--> le Connecteur du Client s'est connect� au Connecteur du Serveur!");
    MBeanServerConnection mbeanServerConnection = clientConnector.getMBeanServerConnection();
    System.out.println(mbeanServerConnection);
    ObjectName name = new ObjectName("SimpleAgent:name=A");
    System.out.println("***********************************************************************");
    System.out.println("                         LANCEMENT DU TEST");
    System.out.println("***********************************************************************");

    System.out.println("--------------------------------------------------");
    System.out.println("test de la methode : isRegistered(ObjectName name)");
    System.out.println("--------------------------------------------------");
    mbeanServerConnection.isRegistered(name);
    System.out.println("--------------------------------------------------");
    System.out.println("fin du test de la methode : isRegistered(ObjectName name)");
    System.out.println("--------------------------------------------------");
    System.out.println();
    // System.out.println("--------------------------------------------------");
    // System.out.println("test de la methode : createMBean(String className,ObjectName name)");
    // System.out.println("--------------------------------------------------");
    // try {
    // ClientJMS clientJms = new ClientJMS();
    //
    // System.out.println(clientJms.getClass());
    // mbeanServerConnection.createMBean(clientJms.getClass().toString(), new
    // ObjectName("SimpleAgent:name=ClientJMS"));
    // } catch (InstanceAlreadyExistsException e1) {
    // // TODO Auto-generated catch block
    // e1.printStackTrace();
    // } catch (NotCompliantMBeanException e1) {
    // // TODO Auto-generated catch block
    // e1.printStackTrace();
    // } catch (NamingException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch (JMSException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch (AdminException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // System.out.println("--------------------------------------------------");
    // System.out.println("fin du test de la methode : createMBean(String className,ObjectName name)");
    // System.out.println("--------------------------------------------------");
    // System.out.println();

    System.out.println("--------------------------------------------------");
    System.out.println("test de la methode : isInstanceOf(name, className)");
    System.out.println("--------------------------------------------------");
    String className = "A";
    String className2 = "B";
    mbeanServerConnection.isInstanceOf(name, className);
    System.out.println("--------------------------------------------------");
    System.out.println("test de la methode : isInstanceOf(name, className)");
    System.out.println("--------------------------------------------------");
    System.out.println();
    System.out.println("--------------------------------------------------");
    System.out.println("RE : test de la methode : isInstanceOf(name, className)");
    System.out.println("--------------------------------------------------");
    mbeanServerConnection.isInstanceOf(name, className2);
    System.out.println("--------------------------------------------------");
    System.out.println("RE : fin du test de la methode : isInstanceOf(name, className)");
    System.out.println("--------------------------------------------------");
    System.out.println();
    System.out.println("--------------------------------------------------");
    System.out.println("test de la methode : getDefaultDomain()");
    System.out.println("--------------------------------------------------");
    mbeanServerConnection.getDefaultDomain();
    System.out.println("--------------------------------------------------");
    System.out.println("fin du test de la methode : getDefaultDomain()");
    System.out.println("--------------------------------------------------");
    System.out.println();
    System.out.println("--------------------------------------------------");
    System.out.println("test de la methode : getDomains()");
    System.out.println("--------------------------------------------------");
    mbeanServerConnection.getDomains();
    System.out.println("--------------------------------------------------");
    System.out.println("fin du test de la methode : getDomains()");
    System.out.println("--------------------------------------------------");
    System.out.println();
    System.out.println("--------------------------------------------------");
    System.out.println("test de la methode : queryNames(ObjectName name,QueryExp query) ");
    System.out.println("--------------------------------------------------");
    mbeanServerConnection.queryNames(name, null);
    System.out.println("--------------------------------------------------");
    System.out.println("fin du test de la methode : queryNames(ObjectName name,QueryExp query) ");
    System.out.println("--------------------------------------------------");
    System.out.println();
    System.out.println("--------------------------------------------------");
    System.out.println("test de la methode : getMBeanInfo(name) ");
    System.out.println("--------------------------------------------------");
    try {
      mbeanServerConnection.getMBeanInfo(name);
    } catch (IntrospectionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println("--------------------------------------------------");
    System.out.println("fin du test de la methode : getMBeanInfo(name) ");
    System.out.println("--------------------------------------------------");
    System.out.println();
    System.out.println("--------------------------------------------------");
    System.out.println("test de la methode : getMBeanCount() ");
    System.out.println("--------------------------------------------------");
    mbeanServerConnection.getMBeanCount();
    System.out.println("--------------------------------------------------");
    System.out.println("fin du test de la methode : getMBeanCount() ");
    System.out.println("--------------------------------------------------");
    System.out.println();
    System.out.println("--------------------------------------------------");
    System.out.println("test de la methode : getAttribute(ObjectName name,String attribute) ");
    System.out.println("--------------------------------------------------");
    mbeanServerConnection.getAttribute(name, "a");
    System.out.println("--------------------------------------------------");
    System.out.println("fin du test de la methode : getAttribute(ObjectName name,String attribute) ");
    System.out.println("--------------------------------------------------");
    System.out.println();
    System.out.println("--------------------------------------------------");
    System.out.println("test de la methode : setAttribute(ObjectName name,Attribute attribute) ");
    System.out.println("--------------------------------------------------");
    Attribute attribute = new Attribute("a", new Integer(3));
    try {
      mbeanServerConnection.setAttribute(name, attribute);
    } catch (InvalidAttributeValueException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println("--------------------------------------------------");
    System.out.println("fin du test de la methode : setAttribute(ObjectName name,Attribute attribute) ");
    System.out.println("--------------------------------------------------");
    System.out.println();
    System.out.println("--------------------------------------------------");
    System.out.println("RE : test de la methode :  getAttribute(ObjectName name,String attribute) ");
    System.out.println("--------------------------------------------------");
    mbeanServerConnection.getAttribute(name, "a");
    System.out.println("--------------------------------------------------");
    System.out.println("RE : fin du test de la methode :  getAttribute(ObjectName name,String attribute) ");
    System.out.println("--------------------------------------------------");
    System.out.println();
    System.out.println("--------------------------------------------------");
    System.out.println("test de la methode : setAttributes(ObjectName name,AttributeList attributes) ");
    System.out.println("--------------------------------------------------");

    Attribute attribute2 = new Attribute("b", new Integer(5));
    AttributeList attributes = new AttributeList();
    int index = 0;
    attributes.add(index, attribute);
    index++;
    attributes.add(index, attribute2);
    mbeanServerConnection.setAttributes(name, attributes);
    System.out.println("--------------------------------------------------");
    System.out
        .println("fin du test de la methode : setAttributes(ObjectName name,AttributeList attributes) ");
    System.out.println("--------------------------------------------------");
    System.out.println();
    System.out.println("--------------------------------------------------");
    System.out.println("test de la methode : getAttributes(ObjectName name,String[] attributes) ");
    System.out.println("--------------------------------------------------");
    String[] stringAttributes = new String[2];
    stringAttributes[0] = "a";
    stringAttributes[1] = "b";
    mbeanServerConnection.getAttributes(name, stringAttributes);
    System.out.println("--------------------------------------------------");
    System.out.println("fin du test de la methode : getAttributes(ObjectName name,String[] attributes) ");
    System.out.println("--------------------------------------------------");
    System.out.println();
    System.out.println("--------------------------------------------------");
    System.out
        .println("test de la methode : invoke(ObjectName name,String operationName,Object[] params,String[] signature) ");
    System.out.println("--------------------------------------------------");
    String operationName;
    Object[] params = new Object[0];
    String[] sig = new String[0];
    operationName = "affiche";
    mbeanServerConnection.invoke(name, operationName, params, sig);
    System.out.println("--------------------------------------------------");
    System.out
        .println("fin du test de la methode : invoke(ObjectName name,String operationName,Object[] params,String[] signature) ");
    System.out.println("--------------------------------------------------");
    System.out.println();
    System.out.println("--------------------------------------------------");
    System.out
        .println("RE : test de la methode : invoke(ObjectName name,String operationName,Object[] params,String[] signature) ");
    System.out.println("--------------------------------------------------");

    String operationName2 = "addValeurs";
    String[] sig2 = new String[2];
    sig2[0] = "int";
    sig2[1] = "int";
    Object[] params2 = new Object[2];
    params2[0] = new Integer(3);
    params2[1] = new Integer(4);
    mbeanServerConnection.invoke(name, operationName2, params2, sig2);
    System.out.println("--------------------------------------------------");
    System.out
        .println("RE : fin du test de la methode : invoke(ObjectName name,String operationName,Object[] params,String[] signature) ");
    System.out.println("--------------------------------------------------");
    System.out.println();
    System.out.println("--------------------------------------------------");
    System.out
        .println("test de la methode : addNotificationListener(ObjectName name,NotificationListener listener,NotificationFilter filter,Object handback) ");
    System.out.println("--------------------------------------------------");
    ObjectName name2 = new ObjectName("SimpleAgent:name=BroadcastingUser");
    NotificationListener notificationListener = new NotificationListener() {

      public void handleNotification(Notification notification, Object handback) {
        // TODO Auto-generated method stub
        System.out
            .println("--> methode handleNotification(Notification notification, Object handback) est appel�e");

      }
    };
    Object handback = new String("handback");
    Object handback2 = new String("handback2");
    mbeanServerConnection.addNotificationListener(name2, notificationListener, null, handback);

    System.out.println("--------------------------------------------------");
    System.out
        .println("fin du test de la methode : addNotificationListener(ObjectName name,NotificationListener listener,NotificationFilter filter,Object handback) ");
    System.out.println("--------------------------------------------------");
    System.out.println();
    System.out.println("--------------------------------------------------");
    System.out
        .println("RE : test de la methode : addNotificationListener(ObjectName name,NotificationListener listener,NotificationFilter filter,Object handback) ");
    System.out.println("--------------------------------------------------");
    mbeanServerConnection.addNotificationListener(name2, notificationListener, null, handback2);
    System.out.println("--------------------------------------------------");
    System.out
        .println("RE : fin du test de la methode : addNotificationListener(ObjectName name,NotificationListener listener,NotificationFilter filter,Object handback) ");
    System.out.println("--------------------------------------------------");
    String operationName3;
    Object[] params3 = new Object[0];
    String[] sig3 = new String[0];
    operationName3 = "remove";
    mbeanServerConnection.invoke(name2, operationName3, params3, sig3);

    System.out.println();
    System.out.println("--------------------------------------------------");
    System.out
        .println("test de la methode : removeNotificationListener(ObjectName name,NotificationListener listener)");
    System.out.println("--------------------------------------------------");
    try {
      mbeanServerConnection.removeNotificationListener(name2, notificationListener, null, handback);
    } catch (ListenerNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println("--------------------------------------------------");
    System.out
        .println("fin du test de la methode : removeNotificationListener(ObjectName name,NotificationListener listener)");
    System.out.println("--------------------------------------------------");
    mbeanServerConnection.invoke(name2, operationName3, params3, sig3);
    System.out.println();

    /*
     * System.out.println("--------------------------------------------------");
     * System.out.println(
     * "test de la methode : removeNotificationListener(ObjectName name,NotificationListener listener,NotificationFilter filter,Object handback)"
     * );
     * System.out.println("--------------------------------------------------");
     * try { mbeanServerConnection.removeNotificationListener(name2,
     * notificationListener, null, null); } catch (ListenerNotFoundException e)
     * { // TODO Auto-generated catch block e.printStackTrace(); }
     * System.out.println("--------------------------------------------------");
     * System.out.println(
     * "fin du test de la methode : removeNotificationListener(ObjectName name,NotificationListener listener,NotificationFilter filter,Object handback)"
     * );
     * System.out.println("--------------------------------------------------");
     * System.out.println();
     * System.out.println("--------------------------------------------------");
     * System.out.println(
     * "RE : test de la methode : removeNotificationListener(ObjectName name,NotificationListener listener,NotificationFilter filter,Object handback)"
     * );
     * System.out.println("--------------------------------------------------");
     * try { mbeanServerConnection.removeNotificationListener(name2,
     * notificationListener, null, null); } catch (ListenerNotFoundException e)
     * { // TODO Auto-generated catch block e.printStackTrace(); }
     * System.out.println("--------------------------------------------------");
     * System.out.println(
     * "RE : fin du test de la methode : removeNotificationListener(ObjectName name,NotificationListener listener,NotificationFilter filter,Object handback)"
     * );
     * System.out.println("--------------------------------------------------");
     * System.out.println();
     */
    System.out.println("***********************************************************************");
    System.out.println("                        FIN  DU TEST");
    System.out.println("***********************************************************************");

  }

}
