/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 - 2011 ScalAgent Distributed Technologies
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
package org.ow2.joram.admin;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import fr.dyade.aaa.agent.AgentServer;

/**
 * Utility class used to test the web admin module, without running over OSGi,
 * for example in GWT hosted mode.
 */
public class JoramAdminJMX extends JoramAdmin {

  private MBeanServer mbeanServer;

  public JoramAdminJMX() {
    mbeanServer = ManagementFactory.getPlatformMBeanServer();
  }

  public boolean connect(String login, String password) {
    return login.equals(password);
  }

  public void disconnect() {
  }

  public void start() {

    NotificationListener jmxListener = new MyNotificationListener();
    NotificationFilter filter = new MyNotificationFilter();

    if (AgentServer.getStatus() != AgentServer.Status.STARTED) {
      try {

        System.setProperty("com.sun.management.jmxremote", "true");

        mbeanServer.addNotificationListener(new ObjectName("JMImplementation:type=MBeanServerDelegate"),
            jmxListener, filter, null);

        AgentServer.init((short) 0, "./s0", null);
        AgentServer.start();

      } catch (Exception exc) {
        System.out.println("FATAL: Error launching JORAM server.");
        exc.printStackTrace();
      }
    }

    System.out.println("AgentServer#0 started.");

  }

  public void stop() {
  }

  class MyNotificationListener implements NotificationListener {

    public void handleNotification(Notification n, Object handback) {
      MBeanServerNotification not = (MBeanServerNotification) n;

      try {
        if (not.getType().equals(MBeanServerNotification.REGISTRATION_NOTIFICATION)) {

          ObjectName mbeanName = not.getMBeanName();
          if ("User".equals(mbeanName.getKeyProperty("type"))
              || "Destination".equals(mbeanName.getKeyProperty("type"))) {
            ObjectInstance objectInstance = mbeanServer.getObjectInstance(mbeanName);
            Class clazz = Class.forName(objectInstance.getClassName() + "MBean");
            Object bean = MBeanServerInvocationHandler.newProxyInstance(mbeanServer, mbeanName, clazz, false);
            handleAdminObjectAdded(bean);
          }

        } else if (not.getType().equals(MBeanServerNotification.UNREGISTRATION_NOTIFICATION)) {
          ObjectName mbeanName = not.getMBeanName();
          if ("User".equals(mbeanName.getKeyProperty("type"))) {
            if (mbeanName.getKeyProperty("sub") == null) {
              users.remove(mbeanName.getKeyProperty("name"));
            } else {
              subscriptions.remove(mbeanName.getKeyProperty("sub"));
            }
          } else if ("Destination".equals(mbeanName.getKeyProperty("type"))) {
            queues.remove(mbeanName.getKeyProperty("name"));
            topics.remove(mbeanName.getKeyProperty("name"));
          }
        }

      } catch (Throwable exc) {
        exc.printStackTrace();
      }
    }
  }

  static class MyNotificationFilter implements NotificationFilter {

    private static final long serialVersionUID = 1L;

    public boolean isNotificationEnabled(Notification notification) {
      return true;
    }
  }

}
