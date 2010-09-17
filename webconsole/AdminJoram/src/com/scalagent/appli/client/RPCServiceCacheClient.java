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
/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Timer;
import com.scalagent.appli.client.command.info.LoadServerInfoAction;
import com.scalagent.appli.client.command.info.LoadServerInfoHandler;
import com.scalagent.appli.client.command.info.LoadServerInfoResponse;
import com.scalagent.appli.client.command.message.LoadMessageAction;
import com.scalagent.appli.client.command.message.LoadMessageHandler;
import com.scalagent.appli.client.command.message.LoadMessageResponse;
import com.scalagent.appli.client.command.queue.LoadQueueAction;
import com.scalagent.appli.client.command.queue.LoadQueueHandler;
import com.scalagent.appli.client.command.queue.LoadQueueResponse;
import com.scalagent.appli.client.command.subscription.LoadSubscriptionAction;
import com.scalagent.appli.client.command.subscription.LoadSubscriptionHandler;
import com.scalagent.appli.client.command.subscription.LoadSubscriptionResponse;
import com.scalagent.appli.client.command.topic.LoadTopicAction;
import com.scalagent.appli.client.command.topic.LoadTopicHandler;
import com.scalagent.appli.client.command.topic.LoadTopicResponse;
import com.scalagent.appli.client.command.user.LoadUserAction;
import com.scalagent.appli.client.command.user.LoadUserHandler;
import com.scalagent.appli.client.command.user.LoadUserResponse;
import com.scalagent.appli.client.event.common.UpdateCompleteEvent;
import com.scalagent.appli.client.event.message.DeletedMessageEvent;
import com.scalagent.appli.client.event.message.NewMessageEvent;
import com.scalagent.appli.client.event.message.QueueNotFoundEvent;
import com.scalagent.appli.client.event.message.UpdatedMessageEvent;
import com.scalagent.appli.client.event.queue.DeletedQueueEvent;
import com.scalagent.appli.client.event.queue.NewQueueEvent;
import com.scalagent.appli.client.event.queue.UpdatedQueueEvent;
import com.scalagent.appli.client.event.subscription.DeletedSubscriptionEvent;
import com.scalagent.appli.client.event.subscription.NewSubscriptionEvent;
import com.scalagent.appli.client.event.subscription.UpdatedSubscriptionEvent;
import com.scalagent.appli.client.event.topic.DeletedTopicEvent;
import com.scalagent.appli.client.event.topic.NewTopicEvent;
import com.scalagent.appli.client.event.topic.UpdatedTopicEvent;
import com.scalagent.appli.client.event.user.DeletedUserEvent;
import com.scalagent.appli.client.event.user.NewUserEvent;
import com.scalagent.appli.client.event.user.UpdatedUserEvent;
import com.scalagent.appli.shared.MessageWTO;
import com.scalagent.appli.shared.QueueWTO;
import com.scalagent.appli.shared.SubscriptionWTO;
import com.scalagent.appli.shared.TopicWTO;
import com.scalagent.appli.shared.UserWTO;
import com.scalagent.engine.client.BaseRPCServiceCacheClient;
import com.scalagent.engine.shared.BaseWTO;

/**
 * This class is used as a cache.
 * Periodically, it retrieves data from the server, compares it
 * with its stored data and fires corresponding events on the EventBus.
 * It handles:
 * - queues
 * - topics
 * - users
 * - subscriptions
 * - messages
 * 
 * @author Yohann CINTRE
 */
public class RPCServiceCacheClient implements BaseRPCServiceCacheClient {

  public static final int QUEUE = 0;
  public static final int TOPIC = 1;
  public static final int MESSAGE = 2;
  public static final int USER = 3;
  public static final int SUB = 4;
  public static final int GLOBAL = 5;
  public static final int SERVER = 6;

  /** Devices available in the cache */
  private HashMap<String, TopicWTO> topics = new HashMap<String, TopicWTO>();
  private HashMap<String, QueueWTO> queues = new HashMap<String, QueueWTO>();
  private HashMap<String, MessageWTO> messages = new HashMap<String, MessageWTO>();
  private HashMap<String, UserWTO> users = new HashMap<String, UserWTO>();
  private HashMap<String, SubscriptionWTO> subs = new HashMap<String, SubscriptionWTO>();

  private SortedMap<Date, Integer> topicsHistory = new TreeMap<Date, Integer>();
  private SortedMap<Date, Integer> queuesHistory = new TreeMap<Date, Integer>();
  private SortedMap<Date, Integer> messagesHistory = new TreeMap<Date, Integer>();
  private SortedMap<Date, Integer> usersHistory = new TreeMap<Date, Integer>();
  private SortedMap<Date, Integer> subsHistory = new TreeMap<Date, Integer>();

  private SortedMap<String, SortedMap<Date, int[]>> globalHistory = new TreeMap<String, SortedMap<Date, int[]>>();

  private SortedMap<Date, float[]> serverHistory = new TreeMap<Date, float[]>();

  private RPCServiceAsync RPCService;
  private HandlerManager eventBus;

  private boolean topicRequest = true;
  private boolean queueRequest = true;
  private boolean userRequest = true;
  private boolean subRequest = true;
  private boolean servRequest = true;

  public RPCServiceCacheClient(RPCServiceAsync RPCService, HandlerManager eventBus, int updatePeriod) {

    this.RPCService = RPCService;
    this.eventBus = eventBus;

    // start the timer, to update periodically the cache
    if (updatePeriod != -1) {
      Timer timer = new RPCServiceCacheTimer(this);
      timer.scheduleRepeating(updatePeriod);
    }
  }

  public HashMap<String, TopicWTO> getTopics() {
    return topics;
  }

  public HashMap<String, QueueWTO> getQueues() {
    return queues;
  }

  public HashMap<String, MessageWTO> getMessages() {
    return messages;
  }

  public HashMap<String, UserWTO> getUsers() {
    return users;
  }

  public HashMap<String, SubscriptionWTO> getSubscriptions() {
    return subs;
  }

  public SortedMap<Date, Integer> getTopicsHistory() {
    return topicsHistory;
  }

  public SortedMap<Date, Integer> getQueuesHistory() {
    return queuesHistory;
  }

  public SortedMap<Date, Integer> getMessagesHistory() {
    return messagesHistory;
  }

  public SortedMap<Date, Integer> getUsersHistory() {
    return usersHistory;
  }

  public SortedMap<Date, Integer> getSubsHistory() {
    return subsHistory;
  }

  public SortedMap<Date, int[]> getSpecificHistory(String name) {
    return globalHistory.get(name);
  }

  public SortedMap<Date, float[]> getServerHistory() {
    return serverHistory;
  }

  @SuppressWarnings("deprecation")
  public void addToHistory(int type, float... value) {
    Date nowMilli = new Date();
    Date nowSec = new Date(nowMilli.getYear(), nowMilli.getMonth(), nowMilli.getDay(), nowMilli.getHours(),
        nowMilli.getMinutes(), nowMilli.getSeconds());

    switch (type) {
    case QUEUE:
      if (!queuesHistory.containsKey(nowSec)) {
        queuesHistory.put(nowSec, new Integer((int) value[0]));
      }
      break;

    case TOPIC:
      if (!topicsHistory.containsKey(nowSec)) {
        topicsHistory.put(nowSec, new Integer((int) value[0]));
      }
      break;

    case USER:
      if (!usersHistory.containsKey(nowSec)) {
        usersHistory.put(nowSec, new Integer((int) value[0]));
      }
      break;

    case SUB:
      if (!subsHistory.containsKey(nowSec)) {
        subsHistory.put(nowSec, new Integer((int) value[0]));
      }
      break;

    case SERVER:
      if (!serverHistory.containsKey(nowSec)) {
        serverHistory.put(nowSec, value);
      }
      break;

    default:
      break;
    }
  }

  @SuppressWarnings("deprecation")
  public void addToSpecificHistory(String name, int... value) {

    Date nowMilli = new Date();
    Date nowSec = new Date(nowMilli.getYear(), nowMilli.getMonth(), nowMilli.getDay(), nowMilli.getHours(),
        nowMilli.getMinutes(), nowMilli.getSeconds());

    if (!globalHistory.containsKey(name)) {
      globalHistory.put(name, new TreeMap<Date, int[]>());
    }

    SortedMap<Date, int[]> mapDate = globalHistory.get(name);

    if (!mapDate.containsKey(nowSec)) {
      mapDate.put(nowSec, value);
    }
  }

  public void setPeriod(int updatePeriod) {
    if (updatePeriod != -1) {
      Timer timer = new RPCServiceCacheTimer(this);
      timer.run();
      timer.scheduleRepeating(updatePeriod);
    }
  }

  /**
   * Called periodically by the RPCServiceCacheTimer
   * in order to update data.
   * Depending on the type of objects:
   * - events are fired on the event bus,
   * - lists devices and ecwSpecifications are updated.
   */

  public void retrieveTopic(boolean forceUpdate) {
    if (topicRequest) {
      topicRequest = false;
      LoadTopicAction action = new LoadTopicAction((topics.isEmpty()), forceUpdate);
      RPCService.execute(action, new LoadTopicHandler(eventBus) {
        @Override
        public void onSuccess(LoadTopicResponse response) {
          if (response != null) {
            processTopics(response.getTopics());
            topicRequest = true;
          }
          eventBus.fireEvent(new UpdateCompleteEvent("topic"));
        }
      });
    }
  }

  public void retrieveQueue(boolean forceUpdate) {
    if (queueRequest) {
      queueRequest = false;
      LoadQueueAction action = new LoadQueueAction(queues.isEmpty(), forceUpdate);
      RPCService.execute(action, new LoadQueueHandler(eventBus) {
        @Override
        public void onSuccess(LoadQueueResponse response) {
          if (response != null) {
            processQueues(response.getQueues());
            queueRequest = true;
          }
          eventBus.fireEvent(new UpdateCompleteEvent("queue"));
        }
      });
    }
  }

  public void retrieveMessageQueue(QueueWTO queue) {
    RPCService.execute(new LoadMessageAction(queue.getName()), new LoadMessageHandler(eventBus) {
      @Override
      public void onSuccess(LoadMessageResponse response) {
        if (response.isSuccess()) {
          processMessages(response.getMessages(), response.getQueueName());
          eventBus.fireEvent(new UpdateCompleteEvent(response.getQueueName()));
        } else {
          eventBus.fireEvent(new QueueNotFoundEvent(response.getQueueName()));
        }
      }
    });
  }

  public void retrieveMessageSub(SubscriptionWTO sub) {
    RPCService.execute(new LoadMessageAction(sub.getName()), new LoadMessageHandler(eventBus) {
      @Override
      public void onSuccess(LoadMessageResponse response) {
        if (response.isSuccess()) {
          processMessages(response.getMessages(), response.getQueueName());
          eventBus.fireEvent(new UpdateCompleteEvent(response.getQueueName()));
        } else {
          eventBus.fireEvent(new QueueNotFoundEvent(response.getQueueName()));
        }
      }
    });
  }

  public void retrieveUser(boolean forceUpdate) {
    if (userRequest) {
      userRequest = false;
      LoadUserAction action = new LoadUserAction((users.isEmpty()), forceUpdate);
      RPCService.execute(action, new LoadUserHandler(eventBus) {
        @Override
        public void onSuccess(LoadUserResponse response) {
          if (response != null) {
            processUsers(response.getUsers());
            userRequest = true;
          }
          eventBus.fireEvent(new UpdateCompleteEvent("user"));
        }
      });
    }
  }

  public void retrieveSubscription(boolean forceUpdate) {
    if (subRequest) {
      subRequest = false;
      LoadSubscriptionAction action = new LoadSubscriptionAction((subs.isEmpty()), forceUpdate);
      RPCService.execute(action, new LoadSubscriptionHandler(eventBus) {
        @Override
        public void onSuccess(LoadSubscriptionResponse response) {
          if (response != null) {
            processSubscriptions(response.getSubscriptions());
            subRequest = true;
          }
          eventBus.fireEvent(new UpdateCompleteEvent("sub"));
        }
      });
    }
  }

  public void retrieveServerInfo(boolean forceUpdate) {
    if (servRequest) {
      servRequest = false;
      LoadServerInfoAction action = new LoadServerInfoAction(forceUpdate);
      RPCService.execute(action, new LoadServerInfoHandler(eventBus) {
        @Override
        public void onSuccess(LoadServerInfoResponse response) {
          if (response != null) {
            processInfos(response.getInfos());
            servRequest = true;
          }
          eventBus.fireEvent(new UpdateCompleteEvent("sub"));
        }
      });
    }
  }

  /**
   * Using dbChangeStatus attribute, update the list of cached devices.
   * It also fires event regarding action performed on devices.
   * 
   * @param newDevices list of devices retrieved from the server
   */
  private void processTopics(List<TopicWTO> newTopics) {

    if (newTopics != null) {

      for (int i = 0; i < newTopics.size(); i++) {

        TopicWTO topic = newTopics.get(i);
        boolean processed = false;

        // new device
        if (topic.getDbChangeStatus() == BaseWTO.NEW) {
          topics.put(topic.getId(), topic);
          eventBus.fireEvent(new NewTopicEvent(topic));
          processed = true;
        }

        // updated device
        if ((!processed) && (topic.getDbChangeStatus() == BaseWTO.UPDATED)) {
          topics.put(topic.getId(), topic);
          eventBus.fireEvent(new UpdatedTopicEvent(topic));
          processed = true;
        }

        // deleted device
        if ((!processed) && (topic.getDbChangeStatus() == BaseWTO.DELETED)) {
          topics.remove(topic.getId());
          eventBus.fireEvent(new DeletedTopicEvent(topic));
          processed = true;
        }
      }
    }
  }

  private void processQueues(List<QueueWTO> newQueues) {

    if (newQueues != null) {

      for (int i = 0; i < newQueues.size(); i++) {

        QueueWTO queue = newQueues.get(i);
        boolean processed = false;

        // new queue
        if (queue.getDbChangeStatus() == BaseWTO.NEW) {
          queues.put(queue.getId(), queue);
          eventBus.fireEvent(new NewQueueEvent(queue));
          processed = true;
        }

        // updated queue
        if ((!processed) && (queue.getDbChangeStatus() == BaseWTO.UPDATED)) {
          queues.put(queue.getId(), queue);
          eventBus.fireEvent(new UpdatedQueueEvent(queue));
          processed = true;
        }

        // deleted device
        if ((!processed) && (queue.getDbChangeStatus() == BaseWTO.DELETED)) {
          queues.remove(queue.getId());
          eventBus.fireEvent(new DeletedQueueEvent(queue));
          processed = true;
        }
      }
    }
  }

  private void processMessages(List<MessageWTO> newMessages, String queueName) {

    if (newMessages != null) {

      for (int i = 0; i < newMessages.size(); i++) {

        MessageWTO message = newMessages.get(i);
        boolean processed = false;

        queues.get(queueName).addMessageToList(message.getIdS());

        // new queue
        if (message.getDbChangeStatus() == BaseWTO.NEW) {
          messages.put(message.getId(), message);
          eventBus.fireEvent(new NewMessageEvent(message, queueName));
          processed = true;
        }

        // updated queue
        if ((!processed) && (message.getDbChangeStatus() == BaseWTO.UPDATED)) {
          messages.put(message.getId(), message);
          eventBus.fireEvent(new UpdatedMessageEvent(message, queueName));
          processed = true;
        }

        // deleted device
        if ((!processed) && (message.getDbChangeStatus() == BaseWTO.DELETED)) {
          messages.remove(message.getId());
          eventBus.fireEvent(new DeletedMessageEvent(message, queueName));
          processed = true;
        }
      }
    }
  }

  private void processUsers(List<UserWTO> newUsers) {

    if (newUsers != null) {

      for (int i = 0; i < newUsers.size(); i++) {

        UserWTO user = newUsers.get(i);
        boolean processed = false;

        // new user
        if (user.getDbChangeStatus() == BaseWTO.NEW) {
          users.put(user.getId(), user);
          eventBus.fireEvent(new NewUserEvent(user));
          processed = true;
        }

        // updated user
        if ((!processed) && (user.getDbChangeStatus() == BaseWTO.UPDATED)) {
          users.put(user.getId(), user);
          eventBus.fireEvent(new UpdatedUserEvent(user));
          processed = true;
        }

        // deleted user
        if ((!processed) && (user.getDbChangeStatus() == BaseWTO.DELETED)) {
          users.remove(user.getId());
          eventBus.fireEvent(new DeletedUserEvent(user));
        }
      }
    }
  }

  private void processSubscriptions(List<SubscriptionWTO> newSubs) {

    if (newSubs != null) {

      for (int i = 0; i < newSubs.size(); i++) {

        SubscriptionWTO sub = newSubs.get(i);
        boolean processed = false;

        // new user
        if (sub.getDbChangeStatus() == BaseWTO.NEW) {
          subs.put(sub.getId(), sub);
          eventBus.fireEvent(new NewSubscriptionEvent(sub));
          processed = true;
        }

        // updated user
        if ((!processed) && (sub.getDbChangeStatus() == BaseWTO.UPDATED)) {
          subs.put(sub.getId(), sub);
          eventBus.fireEvent(new UpdatedSubscriptionEvent(sub));
          processed = true;
        }

        // deleted user
        if ((!processed) && (sub.getDbChangeStatus() == BaseWTO.DELETED)) {
          subs.remove(sub.getId());
          eventBus.fireEvent(new DeletedSubscriptionEvent(sub));
          processed = true;
        }
      }
    }
  }

  private void processInfos(float[] infos) {
    if (infos != null) {
      addToHistory(SERVER, infos);
    }
  }
}
