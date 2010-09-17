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
package com.scalagent.appli.client.presenter;

import java.util.Date;
import java.util.HashMap;
import java.util.SortedMap;

import com.google.gwt.event.shared.HandlerManager;
import com.scalagent.appli.client.RPCServiceAsync;
import com.scalagent.appli.client.RPCServiceCacheClient;
import com.scalagent.appli.client.event.common.UpdateCompleteHandler;
import com.scalagent.appli.client.widget.ServerWidget;
import com.scalagent.appli.shared.QueueWTO;
import com.scalagent.appli.shared.SubscriptionWTO;
import com.scalagent.appli.shared.TopicWTO;
import com.scalagent.appli.shared.UserWTO;
import com.scalagent.engine.client.presenter.BasePresenter;

/**
 * This class is the presenter associated to the server information.
 * Its widget is ServerWidget.
 * 
 * @author Yohann CINTRE
 */
public class ServerPresenter extends BasePresenter<ServerWidget, RPCServiceAsync, RPCServiceCacheClient>
    implements UpdateCompleteHandler {
  public ServerPresenter(RPCServiceAsync serviceRPC, HandlerManager eventBus, RPCServiceCacheClient cache) {
    super(serviceRPC, cache, eventBus);
    this.eventBus = eventBus;
    widget = new ServerWidget(this);
  }

  /**
   * This method is called by the ServerWidget when the updating the chart.
   * 
   * @result A map containing the history of the number of queues.
   */
  public SortedMap<Date, Integer> getQueuesHistory() {
    return cache.getQueuesHistory();
  }

  /**
   * This method is called by the ServerWidget when the updating the chart.
   * 
   * @result A map containing the history of the number of topics.
   */
  public SortedMap<Date, Integer> getTopicsHistory() {
    return cache.getTopicsHistory();
  }

  /**
   * This method is called by the ServerWidget when the updating the chart.
   * 
   * @result A map containing the history of the number of users.
   */
  public SortedMap<Date, Integer> getUsersHistory() {
    return cache.getUsersHistory();
  }

  /**
   * This method is called by the ServerWidget when the updating the chart.
   * 
   * @result A map containing the history of the number of subscriptions.
   */
  public SortedMap<Date, Integer> getSubsHistory() {
    return cache.getSubsHistory();
  }

  /**
   * This method is called by the ServerWidget when the updating the chart.
   * 
   * @result A map containing the history of the average load of the engine and
   *         the networks.
   */
  public SortedMap<Date, float[]> getServerHistory() {
    return cache.getServerHistory();
  }

  /**
   * This method is called by the EventBus when the update is done.
   * The histories are updated
   */
  public void onUpdateComplete(String info) {
    cache.addToHistory(RPCServiceCacheClient.QUEUE, cache.getQueues().size());
    cache.addToHistory(RPCServiceCacheClient.SUB, cache.getSubscriptions().size());
    cache.addToHistory(RPCServiceCacheClient.TOPIC, cache.getTopics().size());
    cache.addToHistory(RPCServiceCacheClient.USER, cache.getUsers().size());

    HashMap<String, QueueWTO> queues = cache.getQueues();
    for (String key : queues.keySet()) {
      QueueWTO queue = queues.get(key);
      cache.addToSpecificHistory(queue.getName(), (int) queue.getNbMsgsReceiveSinceCreation(),
          (int) queue.getNbMsgsDeliverSinceCreation(), (int) queue.getNbMsgsSentToDMQSinceCreation(),
          (int) queue.getPendingMessageCount());
    }

    HashMap<String, TopicWTO> topics = cache.getTopics();
    for (String key : topics.keySet()) {
      TopicWTO topic = topics.get(key);
      cache.addToSpecificHistory(topic.getName(), (int) topic.getNbMsgsReceiveSinceCreation(),
          (int) topic.getNbMsgsDeliverSinceCreation(), (int) topic.getNbMsgsSentToDMQSinceCreation());
    }

    HashMap<String, UserWTO> users = cache.getUsers();
    for (String key : users.keySet()) {
      UserWTO user = users.get(key);
      cache.addToSpecificHistory(user.getName(), (int) user.getNbMsgsSentToDMQSinceCreation(),
          (int) user.getSubscriptionNames().length);
    }

    HashMap<String, SubscriptionWTO> subs = cache.getSubscriptions();
    for (String key : subs.keySet()) {
      SubscriptionWTO sub = subs.get(key);
      cache.addToSpecificHistory(sub.getName(), (int) sub.getPendingMessageCount(),
          (int) sub.getNbMsgsDeliveredSinceCreation(), (int) sub.getNbMsgsSentToDMQSinceCreation());
    }

    SortedMap<Date, float[]> h = cache.getServerHistory();
    if (h.size() != 0)
      widget.initCharts(h.get(h.firstKey()).length);
  }

  /**
   * This method is called by the the ServerWidget when the user click
   * on the "Refresh" button.
   * The queues list, topic list, user list, subscription list and server
   * information are updated.
   */
  public void refreshAll() {
    cache.retrieveQueue(true);
    cache.retrieveTopic(true);
    cache.retrieveUser(true);
    cache.retrieveSubscription(true);
    cache.retrieveServerInfo(true);
  }
}