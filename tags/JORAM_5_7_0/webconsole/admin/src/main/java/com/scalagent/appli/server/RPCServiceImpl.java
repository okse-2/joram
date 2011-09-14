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
package com.scalagent.appli.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.objectweb.joram.mom.dest.QueueMBean;
import org.objectweb.joram.mom.dest.TopicMBean;
import org.objectweb.joram.mom.messages.MessageView;
import org.objectweb.joram.mom.proxies.ClientSubscriptionMBean;
import org.objectweb.joram.mom.proxies.UserAgentMBean;
import org.osgi.framework.BundleContext;
import org.ow2.easybeans.osgi.annotation.OSGiResource;
import org.ow2.joram.admin.Activator;
import org.ow2.joram.admin.AdminListener;
import org.ow2.joram.admin.JoramAdmin;
import org.ow2.joram.admin.JoramAdminOSGi;

import com.scalagent.appli.server.converter.MessageWTOConverter;
import com.scalagent.appli.server.converter.QueueWTOConverter;
import com.scalagent.appli.server.converter.SubscriptionWTOConverter;
import com.scalagent.appli.server.converter.TopicWTOConverter;
import com.scalagent.appli.server.converter.UserWTOConverter;
import com.scalagent.appli.shared.MessageWTO;
import com.scalagent.appli.shared.QueueWTO;
import com.scalagent.appli.shared.SubscriptionWTO;
import com.scalagent.appli.shared.TopicWTO;
import com.scalagent.appli.shared.UserWTO;
import com.scalagent.engine.server.BaseRPCServiceImpl;
import com.scalagent.engine.server.BaseRPCServiceUtils;

/**
 * This class is used as a cache. Periodically, it retrieves data from the
 * server, compares it with stored data (in session) and send diff to the
 * client.
 * It handles:
 * - queues
 * - topics
 * - users
 * - subscriptions
 * - messages
 * 
 * @author Yohann CINTRE
 */
public class RPCServiceImpl extends BaseRPCServiceImpl {

  private static final long serialVersionUID = -5632426706957344608L;

  private static final String SESSION_TOPICS = "topicsList";
  private static final String SESSION_QUEUES = "queuesList";
  private static final String SESSION_MESSAGES = "messagesList";
  private static final String SUBSCRIPTION_MESSAGES = "subMessagesList";
  private static final String SESSION_USERS = "usersList";
  private static final String SESSION_SUBSCRIPTION = "subscriptionList";

  @OSGiResource
  private BundleContext bundleContext = null;

  private boolean isConnected = false;
  private JoramAdmin joramAdmin;
  private LiveListener listener = new LiveListener();

  private Map<String, QueueMBean> mapQueues;
  private Map<String, TopicMBean> mapTopics;
  private Map<String, UserAgentMBean> mapUsers;
  private Map<String, ClientSubscriptionMBean> listSubscriptions;

  private long lastupdate = 0;

  @SuppressWarnings("unchecked")
  public List<TopicWTO> getTopics(HttpSession session, boolean retrieveAll, boolean forceUpdate) {

    synchWithJORAM(forceUpdate);

    TopicWTO[] newTopics = TopicWTOConverter.getTopicWTOArray(mapTopics.values());

    // retrieve previous devices list from session
    HashMap<String, TopicWTO> sessionTopics = (HashMap<String, TopicWTO>) session
        .getAttribute(RPCServiceImpl.SESSION_TOPICS);

    if (sessionTopics == null) {
      sessionTopics = new HashMap<String, TopicWTO>();
    }

    List<TopicWTO> toReturn = null;
    toReturn = BaseRPCServiceUtils.compareEntities(newTopics, sessionTopics);

    if (retrieveAll) {
      toReturn = BaseRPCServiceUtils.retrieveAll(sessionTopics);
    }

    // save devices in session
    session.setAttribute(RPCServiceImpl.SESSION_TOPICS, sessionTopics);

    return toReturn;

  }

  @SuppressWarnings("unchecked")
  public List<QueueWTO> getQueues(HttpSession session, boolean retrieveAll, boolean forceUpdate) {

    synchWithJORAM(forceUpdate);

    QueueWTO[] newQueues = QueueWTOConverter.getQueueWTOArray(mapQueues.values());

    // retrieve previous devices list from session
    HashMap<String, QueueWTO> sessionQueues = (HashMap<String, QueueWTO>) session
        .getAttribute(RPCServiceImpl.SESSION_QUEUES);

    if (sessionQueues == null) {
      sessionQueues = new HashMap<String, QueueWTO>();
    }

    List<QueueWTO> toReturn = null;
    toReturn = BaseRPCServiceUtils.compareEntities(newQueues, sessionQueues);

    if (retrieveAll) {
      toReturn = BaseRPCServiceUtils.retrieveAll(sessionQueues);
    }

    // save devices in session
    session.setAttribute(RPCServiceImpl.SESSION_QUEUES, sessionQueues);

    return toReturn;
  }

  @SuppressWarnings("unchecked")
  public List<MessageWTO> getMessages(HttpSession session, boolean retrieveAll, String queueName)
      throws Exception {

    synchWithJORAM(true);

    QueueMBean queue = mapQueues.get(queueName);

    if (queue == null) {
      throw new Exception("Queue not found");
    }
    
    List<MessageView> listMessage = queue.getMessagesView();

    MessageWTO[] newMessages = MessageWTOConverter.getMessageWTOArray(listMessage);

    // retrieve previous devices list from session
    HashMap<String, HashMap<String, MessageWTO>> sessionMessagesAll = (HashMap<String, HashMap<String, MessageWTO>>) session
        .getAttribute(RPCServiceImpl.SESSION_MESSAGES);
    if (sessionMessagesAll == null) {
      sessionMessagesAll = new HashMap<String, HashMap<String, MessageWTO>>();
    }

    HashMap<String, MessageWTO> sessionMessagesQueue = sessionMessagesAll.get(queueName);
    if (sessionMessagesQueue == null) {
      sessionMessagesQueue = new HashMap<String, MessageWTO>();
    }

    List<MessageWTO> toReturn = null;
    toReturn = BaseRPCServiceUtils.compareEntities(newMessages, sessionMessagesQueue);

    if (retrieveAll) {
      toReturn = BaseRPCServiceUtils.retrieveAll(sessionMessagesQueue);
    }

    // save devices in session
    sessionMessagesAll.put(queueName, sessionMessagesQueue);
    session.setAttribute(RPCServiceImpl.SESSION_MESSAGES, sessionMessagesAll);

    return toReturn;
  }

  @SuppressWarnings("unchecked")
  public List<MessageWTO> getSubMessages(HttpSession session, boolean retrieveAll, String subName)
      throws Exception {

    synchWithJORAM(true);

    ClientSubscriptionMBean sub = listSubscriptions.get(subName);
    if (sub == null) {
      throw new Exception("Subscription not found");
    }

    List<MessageView> listMessage = sub.getMessagesView();

    MessageWTO[] newMessages = MessageWTOConverter.getMessageWTOArray(listMessage);

    // retrieve previous devices list from session
    HashMap<String, HashMap<String, MessageWTO>> sessionMessagesAll = (HashMap<String, HashMap<String, MessageWTO>>) session
        .getAttribute(RPCServiceImpl.SUBSCRIPTION_MESSAGES);
    if (sessionMessagesAll == null) {
      sessionMessagesAll = new HashMap<String, HashMap<String, MessageWTO>>();
    }

    HashMap<String, MessageWTO> sessionMessagesSub = sessionMessagesAll.get(subName);
    if (sessionMessagesSub == null) {
      sessionMessagesSub = new HashMap<String, MessageWTO>();
    }

    List<MessageWTO> toReturn = null;
    toReturn = BaseRPCServiceUtils.compareEntities(newMessages, sessionMessagesSub);

    if (retrieveAll) {
      toReturn = BaseRPCServiceUtils.retrieveAll(sessionMessagesSub);
    }

    // save devices in session
    sessionMessagesAll.put(subName, sessionMessagesSub);
    session.setAttribute(RPCServiceImpl.SUBSCRIPTION_MESSAGES, sessionMessagesAll);

    return toReturn;
  }

  @SuppressWarnings("unchecked")
  public List<UserWTO> getUsers(HttpSession session, boolean retrieveAll, boolean forceUpdate) {

    synchWithJORAM(forceUpdate);

    UserWTO[] newUsers = UserWTOConverter.getUserWTOArray(mapUsers.values());

    // retrieve previous devices list from session
    HashMap<String, UserWTO> sessionUsers = (HashMap<String, UserWTO>) session
        .getAttribute(RPCServiceImpl.SESSION_USERS);

    if (sessionUsers == null) {
      sessionUsers = new HashMap<String, UserWTO>();
    }

    List<UserWTO> toReturn = null;
    toReturn = BaseRPCServiceUtils.compareEntities(newUsers, sessionUsers);

    if (retrieveAll) {
      toReturn = BaseRPCServiceUtils.retrieveAll(sessionUsers);
    }

    // save devices in session
    session.setAttribute(RPCServiceImpl.SESSION_USERS, sessionUsers);

    return toReturn;
  }

  @SuppressWarnings("unchecked")
  public List<SubscriptionWTO> getSubscriptions(HttpSession session, boolean retrieveAll, boolean forceUpdate) {

    synchWithJORAM(forceUpdate);

    SubscriptionWTO[] newSubscriptions = SubscriptionWTOConverter.getSubscriptionWTOArray(listSubscriptions.values());

    // retrieve previous devices list from session
    HashMap<String, SubscriptionWTO> sessionSubscriptions = (HashMap<String, SubscriptionWTO>) session
        .getAttribute(RPCServiceImpl.SESSION_SUBSCRIPTION);

    if (sessionSubscriptions == null) {
      sessionSubscriptions = new HashMap<String, SubscriptionWTO>();
    }

    List<SubscriptionWTO> toReturn = null;
    toReturn = BaseRPCServiceUtils.compareEntities(newSubscriptions, sessionSubscriptions);

    if (retrieveAll) {
      toReturn = BaseRPCServiceUtils.retrieveAll(sessionSubscriptions);
    }

    // save devices in session
    session.setAttribute(RPCServiceImpl.SESSION_SUBSCRIPTION, sessionSubscriptions);

    return toReturn;
  }

  public float[] getInfos(boolean isforceUpdate) {
    if (!isConnected) {
      return new float[1];
    }

    synchWithJORAM(isforceUpdate);
    return joramAdmin.getInfos();

  }

  public boolean connectJORAM(String login, String password) throws Exception {
    // If context has not been injected, we should be in pax web OSGi case.
    if (bundleContext == null) {
      bundleContext = Activator.getContext();
    }
    if (bundleContext == null) {
      throw new Exception("OSGi context has not been found, server is not configured properly.");
    }
    joramAdmin = new JoramAdminOSGi(bundleContext);
    isConnected = joramAdmin.connect(login, password);
    if (isConnected) {
      joramAdmin.start(listener);
    }
    return isConnected;
  }

  public void synchWithJORAM(boolean forceUpdate) {

    long now = System.currentTimeMillis();

    if (now > lastupdate + 5000 || mapQueues == null || forceUpdate) {

      mapQueues = listener.getQueues();
      mapTopics = listener.getTopics();
      mapUsers = listener.getUsers();
      listSubscriptions = listener.getSubscription();
      lastupdate = now;
    }

  }

  /** QUEUES **/

  public boolean createNewQueue(QueueWTO queue) {
    if (!isConnected) {
      return false;
    }
    return joramAdmin.createNewQueue(queue.getId(), queue.getDMQId(), queue.getDestinationId(),
        queue.getPeriod(), queue.getThreshold(), queue.getNbMaxMsg(), queue.isFreeReading(),
        queue.isFreeWriting());
  }

  public boolean editQueue(QueueWTO queue) {
    if (!isConnected) {
      return false;
    }
    return joramAdmin.editQueue(mapQueues.get(queue.getId()), queue.getDMQId(), queue.getDestinationId(),
        queue.getPeriod(), queue.getThreshold(), queue.getNbMaxMsg(), queue.isFreeReading(),
        queue.isFreeWriting());
  }

  public boolean deleteQueue(String queueName) {
    if (!isConnected) {
      return false;
    }
    return joramAdmin.deleteQueue(mapQueues.get(queueName));
  }

  public boolean cleanWaitingRequest(String queueName) {
    if (!isConnected) {
      return false;
    }
    return joramAdmin.cleanWaitingRequest(mapQueues.get(queueName));
  }

  public boolean cleanPendingMessage(String queueName) {
    if (!isConnected) {
      return false;
    }
    return joramAdmin.cleanPendingMessage(mapQueues.get(queueName));
  }

  /** USERS **/

  public boolean createNewUser(UserWTO user) {
    if (!isConnected) {
      return false;
    }
    return joramAdmin.createNewUser(user.getId(), user.getPassword(), user.getPeriod());
  }

  public boolean editUser(UserWTO user) {
    if (!isConnected) {
      return false;
    }
    return joramAdmin.editUser(mapUsers.get(user.getId()), user.getPassword(), user.getPeriod());
  }

  public boolean deleteUser(String userName) {
    if (!isConnected) {
      return false;
    }
    return joramAdmin.deleteUser(mapUsers.get(userName));
  }

  /** MESSAGES **/

  public boolean createNewMessage(MessageWTO message, String queueName) {
    if (!isConnected) {
      return false;
    }
    return joramAdmin.createNewMessage(queueName, message.getId(), message.getExpiration(),
        message.getTimestamp(), message.getPriority(), message.getText(), message.getType());
  }

  public boolean editMessage(MessageWTO message, String queueName) {
    if (!isConnected) {
      return false;
    }
    return joramAdmin.editMessage(queueName, message.getId(), message.getExpiration(),
        message.getTimestamp(), message.getPriority(), message.getText(), message.getType());
  }

  public boolean deleteQueueMessage(String messageID, String queueName) {
    if (!isConnected) {
      return false;
    }
    return false;
  }

  public boolean deleteSubscriptionMessage(String messageID, String subName) {
    if (!isConnected) {
      return false;
    }
    return joramAdmin.deleteSubscriptionMessage(listSubscriptions.get(subName), messageID);
  }

  /** TOPICS **/

  public boolean createNewTopic(TopicWTO topic) {
    if (!isConnected) {
      return false;
    }
    return joramAdmin.createNewTopic(topic.getId(), topic.getDMQId(), topic.getDestinationId(),
        topic.getPeriod(), topic.isFreeReading(), topic.isFreeWriting());
  }

  public boolean editTopic(TopicWTO topic) {
    if (!isConnected) {
      return false;
    }
    return joramAdmin.editTopic(mapTopics.get(topic.getId()), topic.getDMQId(), topic.getDestinationId(),
        topic.getPeriod(), topic.isFreeReading(), topic.isFreeWriting());
  }

  public boolean deleteTopic(String topicName) {
    if (!isConnected) {
      return false;
    }
    return joramAdmin.deleteTopic(mapTopics.get(topicName));
  }

  /** SUBSCRIPTIONS **/

  public boolean createNewSubscription(SubscriptionWTO sub) {
    if (!isConnected) {
      return false;
    }
    return joramAdmin.createNewSubscription(sub.getId(), sub.getNbMaxMsg(), sub.getContextId(),
        sub.getSelector(), sub.getSubRequestId(), sub.isActive(), sub.isDurable());
  }

  public boolean editSubscription(SubscriptionWTO sub) {
    if (!isConnected) {
      return false;
    }
    return joramAdmin.editSubscription(listSubscriptions.get(sub.getId()), sub.getNbMaxMsg(),
        sub.getContextId(), sub.getSelector(), sub.getSubRequestId(), sub.isActive(), sub.isDurable());
  }

  public boolean deleteSubscription(String subName) {
    if (!isConnected) {
      return false;
    }
    return joramAdmin.deleteSubscription(subName);
  }

  static class LiveListener implements AdminListener {

    private Map<String, QueueMBean> queues = new HashMap<String, QueueMBean>();
    private Map<String, TopicMBean> topics = new HashMap<String, TopicMBean>();
    private Map<String, UserAgentMBean> users = new HashMap<String, UserAgentMBean>();
    private Map<String, ClientSubscriptionMBean> subscriptions = new HashMap<String, ClientSubscriptionMBean>();

    public void onQueueAdded(QueueMBean queue) {
      queues.put(queue.getName(), queue);
    }

    public void onQueueRemoved(QueueMBean queue) {
      queues.remove(queue.getName());
    }

    public void onTopicAdded(TopicMBean topic) {
      topics.put(topic.getName(), topic);
    }

    public void onTopicRemoved(TopicMBean topic) {
      topics.remove(topic.getName());
    }

    public void onSubscriptionAdded(ClientSubscriptionMBean subscription) {
      subscriptions.put(subscription.getName(), subscription);
    }

    public void onSubscriptionRemoved(ClientSubscriptionMBean subscription) {
      subscriptions.remove(subscription.getName());
    }

    public void onUserAdded(UserAgentMBean user) {
      users.put(user.getName(), user);
    }

    public void onUserRemoved(UserAgentMBean user) {
      users.remove(user.getName());
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

  }

}
