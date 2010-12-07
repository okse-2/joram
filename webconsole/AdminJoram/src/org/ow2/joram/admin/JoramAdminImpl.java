/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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

import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.objectweb.joram.mom.dest.QueueMBean;
import org.objectweb.joram.mom.dest.TopicMBean;
import org.objectweb.joram.mom.proxies.ClientSubscriptionMBean;
import org.objectweb.joram.mom.proxies.UserAgentMBean;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.util.management.MXWrapper;

public class JoramAdminImpl implements JoramAdmin {

  static final String REGISTERED = "JMX.mbean.registered";
  static final String UNREGISTERED = "JMX.mbean.unregistered";

  ObjectName ConnectionON, UserON, DestinationON;

  AdminListener adminListener;

  public boolean connect(String login, String password) {
    return login.equals(password);
  }

  public void disconnect() {

  }

  public void start(AdminListener adminListener) {

    this.adminListener = adminListener;

    NotificationListener jmxListener = new MyNotificationListener();
    NotificationFilter filter = new MyNotificationFilter();

    try {
      ConnectionON = new ObjectName("Joram#0:type=Connection,*");
      UserON = new ObjectName("Joram#0:type=User,*");
      DestinationON = new ObjectName("Joram#0:type=Destination,*");

      System.setProperty("com.sun.management.jmxremote", "true");
      System.setProperty("MXServer", "com.scalagent.jmx.JMXServer");

      MXWrapper.init();
      try {
        MXWrapper.addNotificationListener(new ObjectName("JMImplementation:type=MBeanServerDelegate"),
            jmxListener, filter, null);
      } catch (NullPointerException exc) {
        System.err.println("JMX must be enabled, use -Dcom.sun.management.jmxremote "
            + "-DMXServer=com.scalagent.jmx.JMXServer options in command line.");
        System.exit(-1);
      }

      AgentServer.init((short) 0, "./s0", null);
      AgentServer.start();

    } catch (Exception exc) {
      System.out.println("FATAL: Error launching JORAM server.");
      exc.printStackTrace();
    }

  }

  public void stop() {
  }

  class MyNotificationListener implements NotificationListener {

    public void handleNotification(Notification n, Object handback) {
      try {
        MBeanServerNotification not = (MBeanServerNotification) n;
        ObjectName mbeanName = not.getMBeanName();

        Object mbean = MXWrapper.getMBeanInstance(mbeanName);

        if (UserON.apply(mbeanName)) {
          if (mbean instanceof UserAgentMBean) {
            UserAgentMBean user = (UserAgentMBean) mbean;
            if (not.getType().equals(REGISTERED)) {
              adminListener.onUserAdded(user);
            } else if (not.getType().equals(UNREGISTERED)) {
              adminListener.onUserRemoved(user);
            }
          } else if (mbean instanceof ClientSubscriptionMBean) {
            ClientSubscriptionMBean subscription = (ClientSubscriptionMBean) mbean;
            if (not.getType().equals(REGISTERED)) {
              adminListener.onSubscriptionAdded(subscription.getName(), subscription);
            } else if (not.getType().equals(UNREGISTERED)) {
              adminListener.onSubscriptionRemoved(subscription.getName(), subscription);
            }
          } else {
            System.out.println("Unknown User: " + mbean.getClass().getName());
          }
        } else if (DestinationON.apply(mbeanName)) {
          if (mbean instanceof QueueMBean) {
            QueueMBean queue = (QueueMBean) mbean;
            if (not.getType().equals(REGISTERED)) {
              adminListener.onQueueAdded(queue);
            } else if (not.getType().equals(UNREGISTERED)) {
              adminListener.onQueueRemoved(queue);
            }
          } else if (mbean instanceof TopicMBean) {
            TopicMBean topic = (TopicMBean) mbean;
            if (not.getType().equals(REGISTERED)) {
              adminListener.onTopicAdded(topic);
            } else if (not.getType().equals(UNREGISTERED)) {
              adminListener.onTopicRemoved(topic);
            }
          } else {
            System.out.println("Unknown Destination: " + mbean.getClass().getName());
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

  public boolean createNewMessage(String queueName, String id, long expiration, long timestamp, int priority,
      String text, int type) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean editMessage(String queueName, String id, long expiration, long timestamp, int priority,
      String text, int type) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean deleteSubscriptionMessage(ClientSubscriptionMBean sub, String msgId) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean createNewTopic(String name, String DMQ, String destination, long period,
      boolean freeReading, boolean freeWriting) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean editTopic(TopicMBean topic, String DMQ, String destination, long period,
      boolean freeReading, boolean freeWriting) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean deleteTopic(TopicMBean topic) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean createNewUser(String name, String password, long period) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean editUser(UserAgentMBean user, String password, long period) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean deleteUser(UserAgentMBean user) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean createNewQueue(String name, String DMQ, String destination, long period, int threshold,
      int nbMaxMsg, boolean freeReading, boolean freeWriting) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean editQueue(QueueMBean queue, String DMQ, String destination, long period, int threshold,
      int nbMaxMsg, boolean freeReading, boolean freeWriting) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean deleteQueue(QueueMBean queue) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean cleanWaitingRequest(QueueMBean queue) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean cleanPendingMessage(QueueMBean queue) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean createNewSubscription(String name, int nbMaxMsg, int context, String selector,
      int subRequest, boolean active, boolean durable) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean editSubscription(ClientSubscriptionMBean sub, int nbMaxMsg, int context, String selector,
      int subRequest, boolean active, boolean durable) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean deleteSubscription(String subscriptionName) {
    // TODO Auto-generated method stub
    return true;
  }

  public float[] getInfos() {
    // TODO Auto-generated method stub
    return null;
  }

}
