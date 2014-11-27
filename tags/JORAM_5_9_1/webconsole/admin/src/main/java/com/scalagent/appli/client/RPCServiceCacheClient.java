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
package com.scalagent.appli.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Timer;
import com.scalagent.appli.client.command.info.LoadServerInfoAction;
import com.scalagent.appli.client.command.info.LoadServerInfoResponse;
import com.scalagent.appli.client.command.message.LoadMessageAction;
import com.scalagent.appli.client.command.message.LoadMessageResponse;
import com.scalagent.appli.client.command.queue.LoadQueueAction;
import com.scalagent.appli.client.command.queue.LoadQueueResponse;
import com.scalagent.appli.client.command.subscription.LoadSubscriptionAction;
import com.scalagent.appli.client.command.subscription.LoadSubscriptionResponse;
import com.scalagent.appli.client.command.topic.LoadTopicAction;
import com.scalagent.appli.client.command.topic.LoadTopicResponse;
import com.scalagent.appli.client.command.user.LoadUserAction;
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
import com.scalagent.appli.client.presenter.SubscriptionDetailPresenter;
import com.scalagent.appli.shared.MessageWTO;
import com.scalagent.appli.shared.QueueWTO;
import com.scalagent.appli.shared.SubscriptionWTO;
import com.scalagent.appli.shared.TopicWTO;
import com.scalagent.appli.shared.UserWTO;
import com.scalagent.engine.client.BaseRPCServiceAsync;
import com.scalagent.engine.client.BaseRPCServiceCacheClient;
import com.scalagent.engine.client.command.Handler;
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

  public static class HistoryData {
    public Date time;
    public int[] data;

    public HistoryData(Date time, int[] data) {
      super();
      this.time = time;
      this.data = data;
    }
  }

  public static class FloatHistoryData {
    public Date time;
    public float[] data;

    public FloatHistoryData(Date time, float[] data) {
      super();
      this.time = time;
      this.data = data;
    }
  }

  private static final String logCategory = SubscriptionDetailPresenter.class.getName();

  /** Devices available in the cache */
  private Map<String, TopicWTO> topics = new HashMap<String, TopicWTO>();
  private Map<String, QueueWTO> queues = new HashMap<String, QueueWTO>();
  private Map<String, MessageWTO> messages = new HashMap<String, MessageWTO>();
  private Map<String, UserWTO> users = new HashMap<String, UserWTO>();
  private Map<String, SubscriptionWTO> subs = new HashMap<String, SubscriptionWTO>();

  private List<HistoryData> countHistory = new ArrayList<HistoryData>();
  private Map<String, List<HistoryData>> globalHistory = new HashMap<String, List<HistoryData>>();
  private List<FloatHistoryData> serverHistory = new ArrayList<FloatHistoryData>();

  private BaseRPCServiceAsync RPCService;
  private SimpleEventBus eventBus;

  private boolean topicRequest = true;
  private boolean queueRequest = true;
  private boolean userRequest = true;
  private boolean subRequest = true;
  private boolean servRequest = true;

  public RPCServiceCacheClient(BaseRPCServiceAsync RPCService, SimpleEventBus eventBus, int updatePeriod) {

    Log.debug("RPCServiceCacheClient start.");

    this.RPCService = RPCService;
    this.eventBus = eventBus;

    // start the timer, to update periodically the cache
    if (updatePeriod != -1) {
      Timer timer = new RPCServiceCacheTimer(this);
      timer.scheduleRepeating(updatePeriod);
    }
  }

  public Map<String, TopicWTO> getTopics() {
    return topics;
  }

  public Map<String, QueueWTO> getQueues() {
    return queues;
  }

  public Map<String, MessageWTO> getMessages() {
    return messages;
  }

  public Map<String, UserWTO> getUsers() {
    return users;
  }

  public Map<String, SubscriptionWTO> getSubscriptions() {
    return subs;
  }

  public List<HistoryData> getCountHistory() {
    return countHistory;
  }

  public List<HistoryData> getSpecificHistory(String name) {
    return globalHistory.get(name);
  }

  public List<FloatHistoryData> getServerHistory() {
    return serverHistory;
  }

  public void addToCountHistory(int... value) {
    Date nowSec = new Date(System.currentTimeMillis() / 1000 * 1000);
    if (countHistory.size() == 0 || !countHistory.get(countHistory.size() - 1).time.equals(nowSec)) {
      countHistory.add(new HistoryData(nowSec, value));
    }
  }

  public void addToHistory(float... value) {
    Date nowSec = new Date(System.currentTimeMillis() / 1000 * 1000);
    if (serverHistory.size() == 0 || !serverHistory.get(serverHistory.size() - 1).time.equals(nowSec)) {
      serverHistory.add(new FloatHistoryData(nowSec, value));
    }
  }

  public void addToSpecificHistory(String name, int... value) {
    Date nowSec = new Date(System.currentTimeMillis() / 1000 * 1000);

    if (!globalHistory.containsKey(name)) {
      globalHistory.put(name, new ArrayList<HistoryData>());
    }

    List<HistoryData> history = globalHistory.get(name);
    if (history.size() == 0 || !history.get(history.size() - 1).time.equals(nowSec)) {
      history.add(new HistoryData(nowSec, value));
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
      RPCService.execute(action, new Handler<LoadTopicResponse>(eventBus) {
        @Override
        public void onSuccess(LoadTopicResponse response) {
          if (response != null) {
            processTopics(response.getTopics());
            topicRequest = true;
          }
          fireBusEvent(new UpdateCompleteEvent(UpdateCompleteEvent.TOPIC_UPDATE));
        }
      });
    }
  }

  public void retrieveQueue(boolean forceUpdate) {
    if (queueRequest) {
      queueRequest = false;
      LoadQueueAction action = new LoadQueueAction(queues.isEmpty(), forceUpdate);
      RPCService.execute(action, new Handler<LoadQueueResponse>(eventBus) {
        @Override
        public void onSuccess(LoadQueueResponse response) {
          if (response != null) {
            processQueues(response.getQueues());
            queueRequest = true;
          }
          fireBusEvent(new UpdateCompleteEvent(UpdateCompleteEvent.QUEUE_UPDATE));
        }
      });
    }
  }

  public void retrieveMessageQueue(QueueWTO queue, boolean retrieveAll) {
    RPCService.execute(new LoadMessageAction(queue.getId(), retrieveAll, true),
        new Handler<LoadMessageResponse>(eventBus) {
      @Override
      public void onSuccess(LoadMessageResponse response) {
        if (response.isSuccess()) {
          processMessages(response.getMessages(), response.getQueueName());
          fireBusEvent(new UpdateCompleteEvent(UpdateCompleteEvent.GENERIC_UPDATE, response.getQueueName()));
        } else {
          fireBusEvent(new QueueNotFoundEvent(response.getQueueName()));
        }
      }
    });
  }

  public void retrieveMessageSub(SubscriptionWTO sub, boolean retrieveAll) {
    RPCService.execute(new LoadMessageAction(sub.getId(), retrieveAll, false),
        new Handler<LoadMessageResponse>(eventBus) {
      @Override
      public void onSuccess(LoadMessageResponse response) {
        if (response.isSuccess()) {
          processSubMessages(response.getMessages(), response.getQueueName());
          fireBusEvent(new UpdateCompleteEvent(UpdateCompleteEvent.GENERIC_UPDATE, response.getQueueName()));
        } else {
          fireBusEvent(new QueueNotFoundEvent(response.getQueueName()));
        }
      }
    });
  }

  public void retrieveUser(boolean forceUpdate) {
    if (userRequest) {
      userRequest = false;
      LoadUserAction action = new LoadUserAction((users.isEmpty()), forceUpdate);
      RPCService.execute(action, new Handler<LoadUserResponse>(eventBus) {
        @Override
        public void onSuccess(LoadUserResponse response) {
          if (response != null) {
            processUsers(response.getUsers());
            userRequest = true;
          }
          fireBusEvent(new UpdateCompleteEvent(UpdateCompleteEvent.USER_UPDATE));
        }
      });
    }
  }

  public void retrieveSubscription(boolean forceUpdate) {
    if (subRequest) {
      subRequest = false;
      LoadSubscriptionAction action = new LoadSubscriptionAction((subs.isEmpty()), forceUpdate);
      RPCService.execute(action, new Handler<LoadSubscriptionResponse>(eventBus) {
        @Override
        public void onSuccess(LoadSubscriptionResponse response) {
          if (response != null) {
            processSubscriptions(response.getSubscriptions());
            subRequest = true;
          }
          fireBusEvent(new UpdateCompleteEvent(UpdateCompleteEvent.SUBSCRIPTION_UPDATE));
        }
      });
    }
  }

  public void retrieveServerInfo(boolean forceUpdate) {
    if (servRequest) {
      servRequest = false;
      LoadServerInfoAction action = new LoadServerInfoAction(forceUpdate);
      RPCService.execute(action, new Handler<LoadServerInfoResponse>(eventBus) {
        @Override
        public void onSuccess(LoadServerInfoResponse response) {
          if (response != null) {
            processInfos(response.getInfos());
            servRequest = true;
          }
          fireBusEvent(new UpdateCompleteEvent(UpdateCompleteEvent.SERVER_INFO_UPDATE));
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

        // new topic
        if (topic.getDbChangeStatus() == BaseWTO.NEW) {
          topics.put(topic.getId(), topic);
          fireBusEvent(new NewTopicEvent(topic));
          continue;
        }

        // updated topic
        if (topic.getDbChangeStatus() == BaseWTO.UPDATED) {
          topics.put(topic.getId(), topic);
          fireBusEvent(new UpdatedTopicEvent(topic));
          continue;
        }

        // deleted topic
        if (topic.getDbChangeStatus() == BaseWTO.DELETED) {
          topics.remove(topic.getId());
          fireBusEvent(new DeletedTopicEvent(topic));
          continue;
        }
      }
    }
  }

  private void processQueues(List<QueueWTO> newQueues) {

    if (newQueues != null) {

      for (int i = 0; i < newQueues.size(); i++) {

        QueueWTO queue = newQueues.get(i);

        // new queue
        if (queue.getDbChangeStatus() == BaseWTO.NEW) {
          queues.put(queue.getId(), queue);
          fireBusEvent(new NewQueueEvent(queue));
          continue;
        }

        // updated queue
        if (queue.getDbChangeStatus() == BaseWTO.UPDATED) {
          queues.put(queue.getId(), queue);
          fireBusEvent(new UpdatedQueueEvent(queue));
          continue;
        }

        // deleted queue
        if (queue.getDbChangeStatus() == BaseWTO.DELETED) {
          queues.remove(queue.getId());
          fireBusEvent(new DeletedQueueEvent(queue));
          continue;
        }
      }
    }
  }

  private void processMessages(List<MessageWTO> newMessages, String queueName) {

    if (newMessages != null) {

      for (int i = 0; i < newMessages.size(); i++) {

        MessageWTO message = newMessages.get(i);

        queues.get(queueName).addMessageToList(message.getId());

        // new queue message
        if (message.getDbChangeStatus() == BaseWTO.NEW) {
          messages.put(message.getId(), message);
          fireBusEvent(new NewMessageEvent(message, queueName));
          continue;
        }

        // updated queue message
        if (message.getDbChangeStatus() == BaseWTO.UPDATED) {
          messages.put(message.getId(), message);
          fireBusEvent(new UpdatedMessageEvent(message, queueName));
          continue;
        }

        // deleted queue message
        if (message.getDbChangeStatus() == BaseWTO.DELETED) {
          messages.remove(message.getId());
          fireBusEvent(new DeletedMessageEvent(message, queueName));
          continue;
        }
      }
    }
  }

  private void processSubMessages(List<MessageWTO> newMessages, String subName) {

    if (newMessages != null) {

      for (int i = 0; i < newMessages.size(); i++) {

        MessageWTO message = newMessages.get(i);

        // new sub message
        if (message.getDbChangeStatus() == BaseWTO.NEW) {
          subs.get(subName).addMessageToList(message.getId());
          messages.put(message.getId(), message);
          fireBusEvent(new NewMessageEvent(message, subName));
          continue;
        }

        // updated sub message
        if (message.getDbChangeStatus() == BaseWTO.UPDATED) {
          messages.put(message.getId(), message);
          fireBusEvent(new UpdatedMessageEvent(message, subName));
          continue;
        }

        // deleted sub message
        if (message.getDbChangeStatus() == BaseWTO.DELETED) {
          subs.get(subName).removeMessageFromList(message.getId());
          messages.remove(message.getId());
          fireBusEvent(new DeletedMessageEvent(message, subName));
          continue;
        }
      }
    }
  }

  private void processUsers(List<UserWTO> newUsers) {

    if (newUsers != null) {

      for (int i = 0; i < newUsers.size(); i++) {

        UserWTO user = newUsers.get(i);

        // new user
        if (user.getDbChangeStatus() == BaseWTO.NEW) {
          users.put(user.getId(), user);
          fireBusEvent(new NewUserEvent(user));
          continue;
        }

        // updated user
        if (user.getDbChangeStatus() == BaseWTO.UPDATED) {
          users.put(user.getId(), user);
          fireBusEvent(new UpdatedUserEvent(user));
          continue;
        }

        // deleted user
        if (user.getDbChangeStatus() == BaseWTO.DELETED) {
          users.remove(user.getId());
          fireBusEvent(new DeletedUserEvent(user));
        }
      }
    }
  }

  private void processSubscriptions(List<SubscriptionWTO> newSubs) {

    if (newSubs != null) {

      for (int i = 0; i < newSubs.size(); i++) {

        SubscriptionWTO sub = newSubs.get(i);

        // new subscription
        if (sub.getDbChangeStatus() == BaseWTO.NEW) {
          subs.put(sub.getId(), sub);
          fireBusEvent(new NewSubscriptionEvent(sub));
          continue;
        }

        // updated subscription
        if (sub.getDbChangeStatus() == BaseWTO.UPDATED) {
          subs.put(sub.getId(), sub);
          fireBusEvent(new UpdatedSubscriptionEvent(sub));
          continue;
        }

        // deleted subscription
        if (sub.getDbChangeStatus() == BaseWTO.DELETED) {
          subs.remove(sub.getId());
          fireBusEvent(new DeletedSubscriptionEvent(sub));
          continue;
        }
      }
    }
  }

  private void fireBusEvent(GwtEvent<?> event) {
    Log.debug(logCategory, " fire BUS event : " + event.getClass().getName());
    eventBus.fireEvent(event);
  }

  private void processInfos(float[] infos) {
    if (infos != null) {
      addToHistory(infos);
    }
  }
}
