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
package com.scalagent.appli.client.presenter;

import java.util.List;
import java.util.Map;

import com.google.gwt.event.shared.SimpleEventBus;
import com.scalagent.appli.client.RPCServiceCacheClient;
import com.scalagent.appli.client.RPCServiceCacheClient.FloatHistoryData;
import com.scalagent.appli.client.RPCServiceCacheClient.HistoryData;
import com.scalagent.appli.client.event.common.UpdateCompleteEvent;
import com.scalagent.appli.client.event.common.UpdateCompleteHandler;
import com.scalagent.appli.client.widget.ServerWidget;
import com.scalagent.appli.shared.QueueWTO;
import com.scalagent.appli.shared.SubscriptionWTO;
import com.scalagent.appli.shared.TopicWTO;
import com.scalagent.appli.shared.UserWTO;
import com.scalagent.engine.client.BaseRPCServiceAsync;
import com.scalagent.engine.client.presenter.BasePresenter;

/**
 * This class is the presenter associated to the server information.
 * Its widget is ServerWidget.
 * 
 * @author Yohann CINTRE
 */
public class ServerPresenter extends BasePresenter<ServerWidget, BaseRPCServiceAsync, RPCServiceCacheClient>
    implements UpdateCompleteHandler {

  public ServerPresenter(BaseRPCServiceAsync serviceRPC, SimpleEventBus eventBus, RPCServiceCacheClient cache) {
    super(serviceRPC, cache, eventBus);
    this.eventBus = eventBus;
    widget = new ServerWidget(this);
  }

  /**
   * This method is called by the ServerWidget when the updating the chart.
   * 
   * @result A map containing the history of the number of queues.
   */
  public List<HistoryData> getCountHistory() {
    return cache.getCountHistory();
  }

  /**
   * This method is called by the ServerWidget when the updating the chart.
   * 
   * @result A map containing the history of the average load of the engine and
   *         the networks.
   */
  public List<FloatHistoryData> getServerHistory() {
    return cache.getServerHistory();
  }

  /**
   * This method is called by the EventBus when the update is done.
   * The histories are updated
   */
  public void onUpdateComplete(int updateType, String info) {

    if (updateType == UpdateCompleteEvent.SERVER_INFO_UPDATE) {

      cache.addToCountHistory(cache.getQueues().size(), cache.getTopics().size(), cache.getUsers().size(),
          cache.getSubscriptions().size());

      List<FloatHistoryData> history = cache.getServerHistory();
      if (history.size() != 0) {
        widget.initCharts(history.get(0).data.length);
        widget.redrawChart();
      }

    } else if (updateType == UpdateCompleteEvent.QUEUE_UPDATE) {

      Map<String, QueueWTO> queues = cache.getQueues();
      for (String key : queues.keySet()) {
        QueueWTO queue = queues.get(key);
        cache.addToSpecificHistory(queue.getId(), (int) queue.getNbMsgsReceiveSinceCreation(),
            (int) queue.getNbMsgsDeliverSinceCreation(), (int) queue.getNbMsgsSentToDMQSinceCreation(),
            (int) queue.getPendingMessageCount());
      }

    } else if (updateType == UpdateCompleteEvent.TOPIC_UPDATE) {

      Map<String, TopicWTO> topics = cache.getTopics();
      for (String key : topics.keySet()) {
        TopicWTO topic = topics.get(key);
        cache.addToSpecificHistory(topic.getId(), (int) topic.getNbMsgsReceiveSinceCreation(),
            (int) topic.getNbMsgsDeliverSinceCreation(), (int) topic.getNbMsgsSentToDMQSinceCreation());
      }

    } else if (updateType == UpdateCompleteEvent.USER_UPDATE) {

      Map<String, UserWTO> users = cache.getUsers();
      for (String key : users.keySet()) {
        UserWTO user = users.get(key);
        cache.addToSpecificHistory(user.getId(), (int) user.getNbMsgsSentToDMQSinceCreation(),
            (int) user.getSubscriptionNames().length);
      }

    } else if (updateType == UpdateCompleteEvent.SUBSCRIPTION_UPDATE) {

      Map<String, SubscriptionWTO> subs = cache.getSubscriptions();
      for (String key : subs.keySet()) {
        SubscriptionWTO sub = subs.get(key);
        cache.addToSpecificHistory(sub.getId(), (int) sub.getPendingMessageCount(),
            (int) sub.getNbMsgsDeliveredSinceCreation(), (int) sub.getNbMsgsSentToDMQSinceCreation());
      }

    }
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