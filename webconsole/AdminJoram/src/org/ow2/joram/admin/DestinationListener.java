package org.ow2.joram.admin;

import org.objectweb.joram.mom.dest.QueueImplMBean;
import org.objectweb.joram.mom.dest.TopicImplMBean;
import org.objectweb.joram.mom.proxies.ClientSubscriptionMBean;
import org.objectweb.joram.mom.proxies.ProxyImplMBean;

public interface DestinationListener {

  public void onQueueAdded(String queueName, QueueImplMBean queue);

  public void onQueueRemoved(String queueName, QueueImplMBean queue);

  public void onTopicAdded(String topicName, TopicImplMBean topic);

  public void onTopicRemoved(String topicName, TopicImplMBean topic);

  public void onSubscriptionAdded(String userName, ClientSubscriptionMBean subscription);

  public void onSubscriptionRemoved(String userName, ClientSubscriptionMBean subscription);

  public void onUserAdded(String userName, ProxyImplMBean user);

  public void onUserRemoved(String userName, ProxyImplMBean user);

}
