/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011- 2013 ScalAgent Distributed Technologies
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
 * Contributor(s): 
 */
package jmxconnector;

import java.util.HashMap;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

/**
 *
 */
public class JMXConnectorTest {

  static class MyFilter implements NotificationFilter {
    public boolean isNotificationEnabled(Notification n) {
      return (n.getType().equals("Hello.notifyHello")) ? true : false;
    }
  }

  static class HelloListener implements NotificationListener {
    public void handleNotification(Notification notif, Object handback) {
      try {
        System.out.println("handleNotification -> " + notif.getMessage() + ", " + handback);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public static void main(String[] args) throws Exception {
    JMXConnector clientConnector = null;
    
    try {
      JMXServiceURL url = new JMXServiceURL("service:jmx:jms://localhost:16010/Hello");
      Map clientEnv = new HashMap();
      clientEnv.put(JMXConnectorServerFactory.PROTOCOL_PROVIDER_PACKAGES, "org.ow2.joram.jmxconnector");
      clientConnector = JMXConnectorFactory.connect(url, clientEnv);

      System.out.println("--> JMXConnector connected to server.");
      MBeanServerConnection mbeanServerConnection = clientConnector.getMBeanServerConnection();
      System.out.println(mbeanServerConnection);

      System.out.println("getMBeanCount() -> " + mbeanServerConnection.getMBeanCount());
      System.out.println("getDefaultDomain() -> " + mbeanServerConnection.getDefaultDomain());
      System.out.println("getDomains() -> " + mbeanServerConnection.getDomains());

      ObjectName hello = new ObjectName("SimpleAgent:name=hello");
      System.out.println("isRegistered(hello) -> " + mbeanServerConnection.isRegistered(hello));

      System.out.println("isInstanceOf(hello, jmxconnector.Hello) -> " +
                         mbeanServerConnection.isInstanceOf(hello, "jmxconnector.Hello"));

      System.out.println("getMBeanInfo(hello) -> " + mbeanServerConnection.getMBeanInfo(hello));

      System.out.println("queryNames(null, null) -> " + mbeanServerConnection.queryNames(null, null).size());
      System.out.println("queryNames(hello, null) -> " + mbeanServerConnection.queryNames(hello, null).size());

      // Test get/set attribute
      System.out.println("getAttribute(hello, msg) -> " +
                         mbeanServerConnection.getAttribute(hello, "Msg"));

      mbeanServerConnection.setAttribute(hello, new Attribute("Msg", "coucou"));
      System.out.println("setAttribute(hello, msg, coucou) -> " +
                         mbeanServerConnection.getAttribute(hello, "Msg"));

      System.out.println("getAttribute(hello, value) -> " +
                         mbeanServerConnection.getAttribute(hello, "Value"));

      mbeanServerConnection.setAttribute(hello, new Attribute("Value", new Integer(10)));
      System.out.println("setAttribute(hello, value, 10) -> " +
                         mbeanServerConnection.getAttribute(hello, "Value"));

      AttributeList atts = new AttributeList();
      atts.add(new Attribute("Msg", "Hello world"));
      atts.add(new Attribute("Value", new Integer(5)));
      mbeanServerConnection.setAttributes(hello, atts);
      AttributeList res = mbeanServerConnection.getAttributes(hello, new String[] {"Msg", "Value"});
      System.out.println("setAttributes(hello, {msg, value}) -> " + res.get(0) + ", " + res.get(1));

      System.out.println("invoke(hello.saidHello) -> " +
                         mbeanServerConnection.invoke(hello, "saidHello", new Object[0], new String[0]));

      // create the listener and filter instances
      HelloListener listener = new HelloListener();
      MyFilter filter = new MyFilter();
      mbeanServerConnection.addNotificationListener(hello, listener, filter, "hello");
      System.out.println("addNotificationListener(hello)");

      System.out.println("invoke(hello.notifyHello)");
      mbeanServerConnection.invoke(hello, "notifyHello", new Object[0], new String[0]);

      Thread.sleep(1000L);

//      ObjectName hello2 = new ObjectName("SimpleAgent:name=Hello2");
//      mbeanServerConnection.createMBean(Hello.class.toString(), hello2);
//
//      System.out.println("isRegistered(hello2) -> " + mbeanServerConnection.isRegistered(hello2));
//      System.out.println("isInstanceOf(hello2, jmxconnector.Hello) -> " +
//                         mbeanServerConnection.isInstanceOf(hello2, "jmxconnector.Hello"));
//      System.out.println("getMBeanInfo(hello2) -> " + mbeanServerConnection.getMBeanInfo(hello2));

      mbeanServerConnection.removeNotificationListener(new ObjectName("SimpleAgent:name=hello"), listener);
    } catch (Throwable exc) {
      exc.printStackTrace();
    } finally {
      clientConnector.close();
    }
  }
}
