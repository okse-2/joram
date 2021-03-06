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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.objectweb.joram.mom.dest.Queue;
import org.objectweb.joram.mom.dest.QueueMBean;
import org.objectweb.joram.mom.dest.Topic;
import org.objectweb.joram.mom.dest.TopicMBean;
import org.objectweb.joram.mom.proxies.ClientSubscriptionMBean;
import org.objectweb.joram.mom.proxies.UserAgentMBean;
import org.objectweb.joram.mom.util.JoramHelper;
import org.objectweb.joram.shared.DestinationConstants;

import fr.dyade.aaa.agent.EngineMBean;
import fr.dyade.aaa.agent.NetworkMBean;

public abstract class JoramAdmin {

  protected Map<String, QueueMBean> queues = new HashMap<String, QueueMBean>();
  protected Map<String, TopicMBean> topics = new HashMap<String, TopicMBean>();
  protected Map<String, UserAgentMBean> users = new HashMap<String, UserAgentMBean>();
  protected Map<String, ClientSubscriptionMBean> subscriptions = new HashMap<String, ClientSubscriptionMBean>();

  protected List<NetworkMBean> networks = new ArrayList<NetworkMBean>();

  protected EngineMBean engine;

  public abstract boolean connect(String login, String password) throws Exception;

  public abstract void start();

  public abstract void stop();

  public abstract void disconnect();

  /**
   * Create a message on a queue in JORAM
   * 
   * @param queueName
   *          Name of the queue containing the message
   * @param id
   *          Id of the message
   * @param expiration
   *          Expiration date of the message
   * @param timestamp
   *          Timestamp of the message
   * @param priority
   *          Priority of the message
   * @param text
   *          Text of the message
   * @param type
   *          Type of the message
   * @return Create successful
   */
  public boolean createNewMessage(String queueName, String id, long expiration, long timestamp, int priority,
      String text, int type) {
    // TODO Auto-generated method stub
    return true;
  }

  /**
   * Edit a message on a queue in JORAM
   * 
   * @param queueName
   *          Name of the queue containing the message
   * @param id
   *          Id of the message
   * @param expiration
   *          Expiration date of the message
   * @param timestamp
   *          Timestamp of the message
   * @param priority
   *          Priority of the message
   * @param text
   *          Text of the message
   * @param type
   *          Type of the message
   * @return Edit successful
   */
  public boolean editMessage(String queueName, String id, long expiration, long timestamp, int priority,
      String text, int type) {
    // TODO Auto-generated method stub
    return true;
  }

  /**
   * Delete a message in a given subscription.
   * 
   * @param sub
   *          The subscription where the message will be deleted.
   * @param msgId
   *          ID of the message to delete
   * @return suppression Delete Successful
   */
  public boolean deleteSubscriptionMessage(ClientSubscriptionMBean sub, String msgId) {
    sub.deleteMessage(msgId);
    return true;
  }

  /**
   * Create a topic on JORAM
   * 
   * @param name
   *          Name of the topic
   * @param DMQ
   *          DMQ Id of the topic
   * @param destination
   *          Destination of the topic
   * @param period
   *          Period of the topic
   * @param freeReading
   *          FreeReading of the topic
   * @param freeWriting
   *          FreeWriting of the topic
   * @return Create successful
   */
  public boolean createNewTopic(String name, String DMQ, String destination, long period,
      boolean freeReading, boolean freeWriting) {
    try {
      Properties props = null;
      if (period != 0) {
        props = new Properties();
        props.setProperty("period", Long.toString(period));
      }
      JoramHelper.createDestination(name, null, Topic.class.getName(), DestinationConstants.TOPIC_TYPE,
          props, freeReading, freeWriting);
    } catch (Exception exc) {
      exc.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * Edit a topic on JORAM
   * 
   * @param name
   *          Name of the topic
   * @param DMQ
   *          DMQ Id of the topic
   * @param destination
   *          Destination of the topic
   * @param period
   *          Period of the topic
   * @param freeReading
   *          FreeReading of the topic
   * @param freeWriting
   *          FreeWriting of the topic
   * @return Create successful
   */
  public boolean editTopic(TopicMBean topic, String DMQ, String destination, long period,
      boolean freeReading, boolean freeWriting) {
    topic.setFreeReading(freeReading);
    topic.setFreeWriting(freeWriting);
    topic.setPeriod(period);
    return true;
  }

  /**
   * Delete a topic in JORAM
   * 
   * @param topic
   *          The topic to delete
   * @return Delete successful
   */
  public boolean deleteTopic(TopicMBean topic) {
    topic.delete();
    return true;
  }

  /**
   * Create a user on JORAM
   * 
   * @param name
   *          Name of the user
   * @param password
   *          User's password
   * @param period
   *          Period of the user
   * @return Create successful
   */
  public boolean createNewUser(String name, String password, long period) {
    try {
      JoramHelper.createUser(name, password);
    } catch (Exception exc) {
      return false;
    }
    return true;
  }

  /**
   * Edit a user on JORAM
   * 
   * @param name
   *          Name of the user
   * @param period
   *          Period of the user
   * @return Edit successful
   */
  public boolean editUser(UserAgentMBean user, String password, long period) {
    user.setPeriod(period);
    return true;
  }

  /**
   * Delete a user on JORAM
   * 
   * @param user
   *          User to delete
   * @return Delete successful
   */
  public boolean deleteUser(UserAgentMBean user) {
    user.delete();
    return true;
  }

  /**
   * Create a queue on JORAM
   * 
   * @param name
   *          Name of the queue
   * @param DMQ
   *          DMQ Id of the queue
   * @param destination
   *          Destination Id of the queue
   * @param period
   *          Period of the queue
   * @param threshold
   *          Threshold of the queue
   * @param nbMaxMsg
   *          Maximum messages of the queue
   * @param freeReading
   *          Is the queue FreeReading
   * @param freeWriting
   *          Is the queue FreeWriting
   * @return Create successful
   */
  public boolean createNewQueue(String name, String DMQ, String destination, long period, int threshold,
      int nbMaxMsg, boolean freeReading, boolean freeWriting) {
    try {
      Properties props = null;
      if (period != 0) {
        props = new Properties();
        props.setProperty("period", Long.toString(period));
      }
      JoramHelper.createDestination(name, null, Queue.class.getName(), DestinationConstants.QUEUE_TYPE,
          props, freeReading, freeWriting);
    } catch (Exception exc) {
      exc.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * Edit a queue on JORAM
   * 
   * @param name
   *          Name of the queue
   * @param DMQ
   *          DMQ Id of the queue
   * @param destination
   *          Destination Id of the queue
   * @param period
   *          Period of the queue
   * @param threshold
   *          Threshold of the queue
   * @param nbMaxMsg
   *          Maximum messages of the queue
   * @param freeReading
   *          Is the queue FreeReading
   * @param freeWriting
   *          Is the queue FreeWriting
   * @return Edit successful
   */
  public boolean editQueue(QueueMBean queue, String DMQ, String destination, long period, int threshold,
      int nbMaxMsg, boolean freeReading, boolean freeWriting) {
    queue.setFreeReading(freeReading);
    queue.setFreeWriting(freeWriting);
    queue.setPeriod(period);
    queue.setNbMaxMsg(nbMaxMsg);
    queue.setThreshold(threshold);
    return true;
  }

  /**
   * Delete a Queue on JORAM
   * 
   * @param queue
   *          The queue to delete
   * @return Delete successful
   */
  public boolean deleteQueue(QueueMBean queue) {
    queue.delete();
    return true;
  }

  /**
   * Clear the waiting requests for a queue on JORAM
   * 
   * @param queueName
   *          Name of the queue
   * @return Clear successful
   */
  public boolean cleanWaitingRequest(QueueMBean queue) {
    queue.cleanWaitingRequest();
    return true;
  }

  /**
   * Clear the pending messages for a queue on JORAM
   * 
   * @param queueName
   *          Name of the queue
   * @return Clear successful
   */
  public boolean cleanPendingMessage(QueueMBean queue) {
    queue.cleanPendingMessage();
    return true;
  }

  /**
   * Create a subscription on JORAM
   * 
   * @param name
   *          Name of the subscription
   * @param nbMaxMsg
   *          Maximum messages on the subscription
   * @param context
   *          Context Id of the subscription
   * @param selector
   *          Selector of the subscription
   * @param subRequest
   *          SubRequest of the subscription
   * @param active
   *          Is the subscription active
   * @param durable
   *          Is the subscription durable
   * @return Create successful
   */
  public boolean createNewSubscription(String name, int nbMaxMsg, int context, String selector,
      int subRequest, boolean active, boolean durable) {
    // TODO Auto-generated method stub
    return true;
  }

  /**
   * Edit a subscription on JORAM
   * 
   * @param sub
   *          The subscription to edit
   * @param nbMaxMsg
   *          Maximum messages on the subscription
   * @param context
   *          Context Id of the subscription
   * @param selector
   *          Selector of the subscription
   * @param subRequest
   *          SubRequest of the subscription
   * @param active
   *          Is the subscription active
   * @param durable
   *          Is the subscription durable
   * @return Edit successful
   */
  public boolean editSubscription(ClientSubscriptionMBean sub, int nbMaxMsg, int context, String selector,
      int subRequest, boolean active, boolean durable) {
    sub.setNbMaxMsg(nbMaxMsg);
    return true;
  }

  /**
   * Delete a subscription in JORAM
   * 
   * @param subscriptionName
   *          Name of the subscription to delete
   * @return Delete successful
   */
  public boolean deleteSubscription(String subscriptionName) {
    // TODO Auto-generated method stub
    return true;
  }

  public float[] getInfos() {
    float[] infos = new float[networks.size() + 1];
    if (engine != null) {
      infos[0] = engine.getAverageLoad1();
    }
    for (int i = 0; i < networks.size(); i++) {
      infos[i + 1] = networks.get(i).getAverageLoad1();
    }
    return infos;
  }

  public Map<String, QueueMBean> getQueues() {
    return queues;
  }

  public Map<String, TopicMBean> getTopics() {
    return topics;
  }

  public Map<String, UserAgentMBean> getUsers() {
    return users;
  }

  public Map<String, ClientSubscriptionMBean> getSubscription() {
    return subscriptions;
  }

  public void handleAdminObjectAdded(Object obj) {
    if (obj instanceof QueueMBean) {
      QueueMBean queue = (QueueMBean) obj;
      queues.put(queue.getName(), queue);
    } else if (obj instanceof TopicMBean) {
      TopicMBean topic = (TopicMBean) obj;
      topics.put(topic.getName(), topic);
    } else if (obj instanceof UserAgentMBean) {
      UserAgentMBean user = (UserAgentMBean) obj;
      users.put(user.getName(), user);
    } else if (obj instanceof ClientSubscriptionMBean) {
      ClientSubscriptionMBean subscription = (ClientSubscriptionMBean) obj;
      subscriptions.put(subscription.getName(), subscription);
    } else if (obj instanceof NetworkMBean) {
      networks.add((NetworkMBean) obj);
    } else if (obj instanceof EngineMBean) {
      engine = (EngineMBean) obj;
    }
  }

  public void handleAdminObjectRemoved(Object obj) {
    if (obj instanceof QueueMBean) {
      queues.remove(((QueueMBean) obj).getName());
    } else if (obj instanceof TopicMBean) {
      topics.remove(((TopicMBean) obj).getName());
    } else if (obj instanceof UserAgentMBean) {
      users.remove(((UserAgentMBean) obj).getName());
    } else if (obj instanceof ClientSubscriptionMBean) {
      subscriptions.remove(((ClientSubscriptionMBean) obj).getName());
    } else if (obj instanceof NetworkMBean) {
      networks.remove(obj);
    } else if (obj instanceof EngineMBean) {
      engine = null;
    }
  }

}