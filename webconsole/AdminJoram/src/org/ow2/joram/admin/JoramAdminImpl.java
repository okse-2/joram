package org.ow2.joram.admin;

import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.objectweb.joram.mom.dest.QueueImpl;
import org.objectweb.joram.mom.dest.TopicImpl;
import org.objectweb.joram.mom.proxies.ClientSubscriptionMBean;
import org.objectweb.joram.mom.proxies.ProxyImplMBean;

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
          if (mbean instanceof ProxyImplMBean) {
            ProxyImplMBean user = (ProxyImplMBean) mbean;
            if (not.getType().equals(REGISTERED)) {
              adminListener.onUserAdded(user.getName(), user);
            } else if (not.getType().equals(UNREGISTERED)) {
              adminListener.onUserRemoved(user.getName(), user);
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
          if (mbean instanceof QueueImpl) {
            QueueImpl queue = (QueueImpl) mbean;
            if (not.getType().equals(REGISTERED)) {
              adminListener.onQueueAdded(queue.getName(), queue);
            } else if (not.getType().equals(UNREGISTERED)) {
              adminListener.onQueueRemoved(queue.getName(), queue);
            }
          } else if (mbean instanceof TopicImpl) {
            TopicImpl topic = (TopicImpl) mbean;
            if (not.getType().equals(REGISTERED)) {
              adminListener.onTopicAdded(topic.getName(), topic);
            } else if (not.getType().equals(UNREGISTERED)) {
              adminListener.onTopicRemoved(topic.getName(), topic);
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

  public boolean deleteMessage(String messageName, String queueName) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean createNewTopic(String name, String DMQ, String destination, long period,
      boolean freeReading, boolean freeWriting) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean editTopic(String name, String DMQ, String destination, long period, boolean freeReading,
      boolean freeWriting) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean deleteTopic(String topicName) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean createNewUser(String name, long period) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean editUser(String name, long period) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean deleteUser(String userName) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean createNewQueue(String name, String DMQ, String destination, long period, int threshold,
      int nbMaxMsg, boolean freeReading, boolean freeWriting) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean editQueue(String name, String DMQ, String destination, long period, int threshold,
      int nbMaxMsg, boolean freeReading, boolean freeWriting) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean deleteQueue(String queueName) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean cleanWaitingRequest(String queueName) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean cleanPendingMessage(String queueName) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean createNewSubscription(String name, int nbMaxMsg, int context, String selector,
      int subRequest, boolean active, boolean durable) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean editSubscription(String name, int nbMaxMsg, int context, String selector, int subRequest,
      boolean active, boolean durable) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean deleteSubscription(String subscriptionName) {
    // TODO Auto-generated method stub
    return true;
  }

}
