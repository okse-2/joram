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
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import fr.dyade.aaa.agent.AgentServer;

/**
 * Utility class used to test the web admin module, without running over OSGi,
 * for example in GWT hosted mode.
 */
public class JoramAdminJMX extends JoramAdmin {

  static final String REGISTERED = "JMX.mbean.registered";
  static final String UNREGISTERED = "JMX.mbean.unregistered";

  private MBeanServer mbeanServer;

  ObjectName UserON, DestinationON;

  public JoramAdminJMX() {
    mbeanServer = ManagementFactory.getPlatformMBeanServer();
  }

  public boolean connect(String login, String password) {
    return login.equals(password);
  }

  public void disconnect() {
  }

  public void start(AdminListener adminListener) {

    super.start(adminListener);

    NotificationListener jmxListener = new MyNotificationListener();
    NotificationFilter filter = new MyNotificationFilter();

    try {
      UserON = new ObjectName("Joram#0:type=User,*");
      DestinationON = new ObjectName("Joram#0:type=Destination,*");

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

  public void stop() {
    super.stop();
  }

  class MyNotificationListener implements NotificationListener {

    public void handleNotification(Notification n, Object handback) {
      try {
        MBeanServerNotification not = (MBeanServerNotification) n;
        ObjectName mbeanName = not.getMBeanName();

        Object mbean = mbeanServer.getObjectInstance(mbeanName);

        if (not.getType().equals(REGISTERED)) {
          handleAdminObjectAdded(mbean);
        } else if (not.getType().equals(UNREGISTERED)) {
          handleAdminObjectRemoved(mbean);
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
