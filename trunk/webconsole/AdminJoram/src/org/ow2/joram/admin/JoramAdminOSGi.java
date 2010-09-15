package org.ow2.joram.admin;

import org.objectweb.joram.mom.dest.QueueImplMBean;
import org.objectweb.joram.mom.dest.TopicImplMBean;
import org.objectweb.joram.mom.proxies.ClientSubscriptionMBean;
import org.objectweb.joram.mom.proxies.ProxyImplMBean;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class JoramAdminOSGi implements JoramAdmin, ServiceTrackerCustomizer {

  private static Filter filter;

  private AdminListener listener;

  private ServiceTracker serviceTracker;

  private BundleContext context;

  static {
    try {
      filter = FrameworkUtil.createFilter("(|(" + Constants.OBJECTCLASS + "="
          + QueueImplMBean.class.getName() + ")(" + Constants.OBJECTCLASS + "="
          + TopicImplMBean.class.getName() + ")(" + Constants.OBJECTCLASS + "="
          + ClientSubscriptionMBean.class.getName() + ")(" + Constants.OBJECTCLASS + "="
          + ProxyImplMBean.class.getName() + "))");
    } catch (InvalidSyntaxException exc) {
      exc.printStackTrace();
    }
  }

  public JoramAdminOSGi(BundleContext context) {
    this.context = context;
  }

  public boolean connect(String login, String password) {
    return login.equals(password);
  }

  public void start(AdminListener listener) {
    this.listener = listener;
    serviceTracker = new ServiceTracker(context, filter, this);
    serviceTracker.open();
  }

  public void stop() {
    serviceTracker.close();
  }

  public void disconnect() {
  }

  public Object addingService(ServiceReference reference) {
    Object service = context.getService(reference);
    if (service instanceof QueueImplMBean) {
      String queueName = (String) reference.getProperty("name");
      listener.onQueueAdded(queueName, (QueueImplMBean) service);
    } else if (service instanceof TopicImplMBean) {
      String topicName = (String) reference.getProperty("name");
      listener.onTopicAdded(topicName, (TopicImplMBean) service);
    } else if (service instanceof ProxyImplMBean) {
      String userName = (String) reference.getProperty("name");
      listener.onUserAdded(userName, (ProxyImplMBean) service);
    } else if (service instanceof ClientSubscriptionMBean) {
      String subName = (String) reference.getProperty("name");
      listener.onSubscriptionAdded(subName, (ClientSubscriptionMBean) service);
    }
    return service;
  }

  public void removedService(ServiceReference reference, Object service) {
    if (service instanceof QueueImplMBean) {
      String queueName = (String) reference.getProperty("name");
      listener.onQueueRemoved(queueName, (QueueImplMBean) service);
    } else if (service instanceof TopicImplMBean) {
      String topicName = (String) reference.getProperty("name");
      listener.onTopicRemoved(topicName, (TopicImplMBean) service);
    } else if (service instanceof ProxyImplMBean) {
      String userName = (String) reference.getProperty("name");
      listener.onUserRemoved(userName, (ProxyImplMBean) service);
    } else if (service instanceof ClientSubscriptionMBean) {
      String subName = (String) reference.getProperty("name");
      listener.onSubscriptionRemoved(subName, (ClientSubscriptionMBean) service);
    }
  }

  public void modifiedService(ServiceReference arg0, Object arg1) {
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
