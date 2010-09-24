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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.objectweb.joram.mom.dest.AdminTopicImplMBean;
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

import fr.dyade.aaa.agent.EngineMBean;
import fr.dyade.aaa.agent.NetworkMBean;

public class JoramAdminOSGi implements JoramAdmin, ServiceTrackerCustomizer {

  private static Filter filter;

  private AdminListener listener;

  private ServiceTracker serviceTracker;

  private BundleContext context;

  private AdminTopicImplMBean adminTopic;

  private List<NetworkMBean> networks = new ArrayList<NetworkMBean>();

  private EngineMBean engine;

  static {
    try {
      filter = FrameworkUtil.createFilter("(|(" + Constants.OBJECTCLASS + "="
          + QueueImplMBean.class.getName() + ")(" + Constants.OBJECTCLASS + "="
          + TopicImplMBean.class.getName() + ")(" + Constants.OBJECTCLASS + "="
          + ClientSubscriptionMBean.class.getName() + ")(" + Constants.OBJECTCLASS + "="
          + ProxyImplMBean.class.getName() + ")(" + Constants.OBJECTCLASS + "="
          + NetworkMBean.class.getName() + ")(" + Constants.OBJECTCLASS + "=" + EngineMBean.class.getName()
          + "))");
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
    adminTopic = null;
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
      if (service instanceof AdminTopicImplMBean) {
        adminTopic = (AdminTopicImplMBean) service;
      }
      listener.onTopicAdded(topicName, (TopicImplMBean) service);
    } else if (service instanceof ProxyImplMBean) {
      String userName = (String) reference.getProperty("name");
      listener.onUserAdded(userName, (ProxyImplMBean) service);
    } else if (service instanceof ClientSubscriptionMBean) {
      String subName = (String) reference.getProperty("name");
      listener.onSubscriptionAdded(subName, (ClientSubscriptionMBean) service);
    } else if (service instanceof NetworkMBean) {
      networks.add((NetworkMBean) service);
    } else if (service instanceof EngineMBean) {
      engine = (EngineMBean) service;
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
    } else if (service instanceof NetworkMBean) {
      networks.remove(service);
    } else if (service instanceof EngineMBean) {
      engine = null;
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
    if (adminTopic != null) {
      adminTopic.createTopic(name);
      return true;
    }
    return false;
  }

  public boolean editTopic(TopicImplMBean topic, String DMQ, String destination, long period,
      boolean freeReading, boolean freeWriting) {
    topic.setFreeReading(freeReading);
    topic.setFreeWriting(freeWriting);
    topic.setPeriod(period);
    return true;
  }

  public boolean deleteTopic(TopicImplMBean topic) {
    topic.delete();
    return true;
  }

  public boolean createNewUser(String name, String password, long period) {
    if (adminTopic != null) {
      try {
        adminTopic.createUser(name, password);
      } catch (Exception exc) {
        return false;
      }
      return true;
    }
    return false;
  }

  public boolean editUser(ProxyImplMBean user, String password, long period) {
    user.setPeriod(period);
    return true;
  }

  public boolean deleteUser(ProxyImplMBean user) {
    user.delete();
    return true;
  }

  public boolean createNewQueue(String name, String DMQ, String destination, long period, int threshold,
      int nbMaxMsg, boolean freeReading, boolean freeWriting) {
    if (adminTopic != null) {
      adminTopic.createQueue(name);
      return true;
    }
    return false;
  }

  public boolean editQueue(QueueImplMBean queue, String DMQ, String destination, long period, int threshold,
      int nbMaxMsg, boolean freeReading, boolean freeWriting) {
    queue.setFreeReading(freeReading);
    queue.setFreeWriting(freeWriting);
    queue.setPeriod(period);
    queue.setNbMaxMsg(nbMaxMsg);
    queue.setThreshold(threshold);
    return true;
  }

  public boolean deleteQueue(QueueImplMBean queue) {
    queue.delete();
    return true;
  }

  public boolean cleanWaitingRequest(QueueImplMBean queue) {
    queue.cleanWaitingRequest();
    return true;
  }

  public boolean cleanPendingMessage(QueueImplMBean queue) {
    queue.cleanPendingMessage();
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

  public float[] getInfos() {
    float[] infos = new float[networks.size() + 1];
    if (engine != null) {
      infos[0] = engine.getAverageLoad1();
    }
    for (int i = 1; i < infos.length; i++) {
      infos[i] = networks.get(i).getAverageLoad1();
    }
    return infos;
  }

}
