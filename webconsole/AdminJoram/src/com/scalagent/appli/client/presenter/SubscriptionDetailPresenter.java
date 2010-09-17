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
import java.util.Map;
import java.util.SortedMap;

import com.google.gwt.event.shared.HandlerManager;
import com.scalagent.appli.client.RPCServiceAsync;
import com.scalagent.appli.client.RPCServiceCacheClient;
import com.scalagent.appli.client.command.message.DeleteMessageAction;
import com.scalagent.appli.client.command.message.DeleteMessageHandler;
import com.scalagent.appli.client.command.message.DeleteMessageResponse;
import com.scalagent.appli.client.command.message.SendEditedMessageAction;
import com.scalagent.appli.client.command.message.SendEditedMessageHandler;
import com.scalagent.appli.client.command.message.SendEditedMessageResponse;
import com.scalagent.appli.client.command.message.SendNewMessageAction;
import com.scalagent.appli.client.command.message.SendNewMessageHandler;
import com.scalagent.appli.client.command.message.SendNewMessageResponse;
import com.scalagent.appli.client.event.common.UpdateCompleteHandler;
import com.scalagent.appli.client.event.message.DeletedMessageHandler;
import com.scalagent.appli.client.event.message.NewMessageHandler;
import com.scalagent.appli.client.event.message.UpdatedMessageHandler;
import com.scalagent.appli.client.widget.SubscriptionDetailWidget;
import com.scalagent.appli.client.widget.record.MessageListRecord;
import com.scalagent.appli.shared.MessageWTO;
import com.scalagent.appli.shared.QueueWTO;
import com.scalagent.appli.shared.SubscriptionWTO;
import com.scalagent.engine.client.presenter.BasePresenter;
import com.smartgwt.client.util.SC;

/**
 * This class is the presenter associated to the details about a subscription.
 * Its widget is SubscriptionDetailWidget.
 * 
 * @author Yohann CINTRE
 */
public class SubscriptionDetailPresenter extends
    BasePresenter<SubscriptionDetailWidget, RPCServiceAsync, RPCServiceCacheClient> implements
    NewMessageHandler, DeletedMessageHandler, UpdatedMessageHandler, UpdateCompleteHandler {
  private SubscriptionWTO sub;

  public SubscriptionDetailPresenter(RPCServiceAsync serviceRPC, HandlerManager eventBus,
      RPCServiceCacheClient cache, SubscriptionWTO sub) {

    super(serviceRPC, cache, eventBus);

    this.eventBus = eventBus;
    this.sub = sub;

    widget = new SubscriptionDetailWidget(this);
    retrieveMessage(sub);

  }

  /**
   * @return The subscription displayed
   */
  public SubscriptionWTO getSubscription() {
    return sub;
  }

  /**
   * This method refresh the message list for the displayed subscription
   */
  public void retrieveMessage(SubscriptionWTO subscription) {
    cache.retrieveMessageSub(subscription);
  }

  /**
   * This method is called by the the SubscriptionDetailWidget when the user
   * click
   * on the "Refresh" button.
   * The "Refresh" button is disabled, the subscription and messages for the
   * displayed subscription is updated.
   */
  public void fireRefreshAll() {
    widget.getRefreshButton().disable();
    cache.retrieveSubscription(true);
    cache.retrieveMessageSub(sub);
  }

  /**
   * This method is called by the EventBus when a new message has been created
   * on the server.
   * The widget is called to add it to the list if the message belong to the
   * displayed subscription
   */
  public void onNewMessage(MessageWTO message, String subName) {
    if (sub.getName().equals(subName))
      getWidget().addMessage(new MessageListRecord(message));
  }

  /**
   * This method is called by the EventBus when a message has been deleted on
   * the server.
   * The widget is called to remove it from the list if the message belong to
   * the displayed subscription
   */
  public void onMessageDeleted(MessageWTO message, String subName) {
    if (sub.getName().equals(subName))

      widget.removeMessage(new MessageListRecord(message));
  }

  /**
   * This method is called by the EventBus when a message has been updated on
   * the server.
   * The widget is called to update it if the message belong to the displayed
   * subscription.
   */
  public void onMessageUpdated(MessageWTO message, String subName) {
    if (sub.getName().equals(subName))
      widget.updateMessage(message);
  }

  /**
   * This method is called by the EventBus when the update is done.
   * The refresh button is re-enabled and the chart redrawn
   */
  public void onUpdateComplete(String info) {
    if (sub.getName().equals(info)) {
      widget.getRefreshButton().enable();
      widget.redrawChart(true);
    }
  }

  /**
   * This method disable the refresh button on the widget
   */
  public void disableButtonRefresh(String queueName) {
    if (sub.getName().equals(queueName)) {
      widget.getRefreshButton().disable();
      widget.getRefreshButton().setIcon("remove.png");
      widget.getRefreshButton().setTooltip("This queue no longer exists on JORAM");
    }
  }

  /**
   * This method is called by the SubscriptionDetailWidget when the user click
   * the "delete" button of a message.
   * The message ID and the queue name are sent to the server which delete the
   * message.
   */
  public void deleteMessage(MessageWTO message, SubscriptionWTO sub) {
    service.execute(new DeleteMessageAction(message.getIdS(), sub.getName()), new DeleteMessageHandler(
        eventBus) {
      @Override
      public void onSuccess(DeleteMessageResponse response) {
        if (response.isSuccess()) {
          fireRefreshAll();
        } else {
          SC.warn(response.getMessage());
          fireRefreshAll();
        }
      }
    });
  }

  /**
   * This method is called by the ServerDetailWidget when the updating the
   * chart.
   * 
   * @result A map containing the history of the number of subscriptions.
   */
  public SortedMap<Date, int[]> getSubHistory() {
    return cache.getSpecificHistory(sub.getName());
  }

  /**
   * This method is called by the MainPresenter when the user close a tab.
   * he widget is called stop updating the non-displayed chart to avoid an
   * exception.
   */
  public void stopChart() {
    widget.stopChart();
  }

  /**
   * This method is called by the SubscriptionDetailWidget when updating the
   * chart.
   * 
   * @return A map of the subscriptions in the client side cache.
   */
  public Map<String, SubscriptionWTO> getSubscriptions() {
    return cache.getSubscriptions();
  }

  /**
   * This method is called by the SubscriptionDetailWidget when the user submit
   * the new message form.
   * The form information are sent to the server.
   */
  public void createNewMessage(MessageWTO message, String queueName) {
    service.execute(new SendNewMessageAction(message, queueName), new SendNewMessageHandler(eventBus) {
      @Override
      public void onSuccess(SendNewMessageResponse response) {
        if (response.isSuccess()) {
          SC.say(response.getMessage());
          widget.destroyForm();
          fireRefreshAll();
        } else {
          SC.warn(response.getMessage());
          fireRefreshAll();
        }
      }
    });
  }

  /**
   * This method is called by the SubscriptionDetailWidget when the user submit
   * the edited message form.
   * The form information are sent to the server.
   */
  public void editMessage(MessageWTO message, String queueName) {
    service.execute(new SendEditedMessageAction(message, queueName), new SendEditedMessageHandler(eventBus) {
      @Override
      public void onSuccess(SendEditedMessageResponse response) {
        if (response.isSuccess()) {
          SC.say(response.getMessage());
          widget.destroyForm();
          fireRefreshAll();
        } else {
          SC.warn(response.getMessage());
          fireRefreshAll();
        }
      }
    });
  }

  /**
   * This method is called by the SubscriptionDetailWidget when the user click
   * the "delete" button of a message.
   * The message ID and the queue name are sent to the server which delete the
   * message.
   */
  public void deleteMessage(MessageWTO message, QueueWTO queue) {
    service.execute(new DeleteMessageAction(message.getIdS(), queue.getName()), new DeleteMessageHandler(
        eventBus) {
      @Override
      public void onSuccess(DeleteMessageResponse response) {
        if (response.isSuccess()) {
          fireRefreshAll();
        } else {
          SC.warn(response.getMessage());
          fireRefreshAll();
        }
      }
    });
  }
}