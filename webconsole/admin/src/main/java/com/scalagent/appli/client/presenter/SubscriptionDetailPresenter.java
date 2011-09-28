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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.shared.SimpleEventBus;
import com.scalagent.appli.client.RPCServiceCacheClient;
import com.scalagent.appli.client.RPCServiceCacheClient.HistoryData;
import com.scalagent.appli.client.command.message.DeleteMessageAction;
import com.scalagent.appli.client.command.message.DeleteMessageResponse;
import com.scalagent.appli.client.command.message.SendEditedMessageAction;
import com.scalagent.appli.client.command.message.SendEditedMessageResponse;
import com.scalagent.appli.client.command.message.SendNewMessageAction;
import com.scalagent.appli.client.command.message.SendNewMessageResponse;
import com.scalagent.appli.client.event.common.UpdateCompleteEvent;
import com.scalagent.appli.client.event.common.UpdateCompleteHandler;
import com.scalagent.appli.client.event.message.DeletedMessageHandler;
import com.scalagent.appli.client.event.message.NewMessageHandler;
import com.scalagent.appli.client.event.message.UpdatedMessageHandler;
import com.scalagent.appli.client.event.subscription.DeletedSubscriptionHandler;
import com.scalagent.appli.client.event.subscription.UpdatedSubscriptionHandler;
import com.scalagent.appli.client.widget.SubscriptionDetailWidget;
import com.scalagent.appli.client.widget.record.MessageListRecord;
import com.scalagent.appli.shared.MessageWTO;
import com.scalagent.appli.shared.SubscriptionWTO;
import com.scalagent.engine.client.BaseRPCServiceAsync;
import com.scalagent.engine.client.command.Handler;
import com.scalagent.engine.client.presenter.BasePresenter;
import com.smartgwt.client.util.SC;

/**
 * This class is the presenter associated to the details about a subscription.
 * Its widget is SubscriptionDetailWidget.
 * 
 * @author Yohann CINTRE
 */
public class SubscriptionDetailPresenter extends
    BasePresenter<SubscriptionDetailWidget, BaseRPCServiceAsync, RPCServiceCacheClient> implements
    NewMessageHandler, DeletedMessageHandler, UpdatedMessageHandler, UpdateCompleteHandler,
    DeletedSubscriptionHandler, UpdatedSubscriptionHandler {

  private static final String logCategory = SubscriptionDetailPresenter.class.getName();

  private SubscriptionWTO sub;

  public SubscriptionDetailPresenter(BaseRPCServiceAsync serviceRPC, SimpleEventBus eventBus,
      RPCServiceCacheClient cache, SubscriptionWTO sub) {

    super(serviceRPC, cache, eventBus);

    this.eventBus = eventBus;
    this.sub = sub;

    widget = new SubscriptionDetailWidget(this);

    sub.clearMessagesList();
    cache.retrieveMessageSub(sub, true);

  }

  /**
   * @return The subscription displayed
   */
  public SubscriptionWTO getSubscription() {
    return sub;
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
    cache.retrieveMessageSub(sub, false);
  }

  /**
   * This method is called by the EventBus when a new message has been created
   * on the server.
   * The widget is called to add it to the list if the message belong to the
   * displayed subscription
   */
  public void onNewMessage(MessageWTO message, String subName) {
    if (sub.getId().equals(subName))
      getWidget().addMessage(new MessageListRecord(message));
  }

  /**
   * This method is called by the EventBus when a message has been deleted on
   * the server.
   * The widget is called to remove it from the list if the message belong to
   * the displayed subscription
   */
  public void onMessageDeleted(MessageWTO message, String subName) {
    if (sub.getId().equals(subName))
      widget.removeMessage(new MessageListRecord(message));
  }

  /**
   * This method is called by the EventBus when a message has been updated on
   * the server.
   * The widget is called to update it if the message belong to the displayed
   * subscription.
   */
  public void onMessageUpdated(MessageWTO message, String subName) {
    if (sub.getId().equals(subName))
      widget.updateMessage(message);
  }

  /**
   * This method is called by the EventBus when the update is done.
   * The refresh button is re-enabled and the chart redrawn
   */
  public void onUpdateComplete(int updateType, String info) {
    if (updateType == UpdateCompleteEvent.SUBSCRIPTION_UPDATE) {
      widget.getRefreshButton().enable();
      widget.redrawChart();
    }
  }

  /**
   * This method is called by the EventBus when a queue has been deleted on the
   * server.
   * The refresh button is disabled.
   */
  public void onSubscriptionDeleted(SubscriptionWTO sub) {
    if (this.sub.getId().equals(sub.getId())) {
      Log.debug(logCategory, "SubscriptionDetailPresenter: onSubscriptionDeleted " + sub.getId());
      disableButtonRefresh(sub.getId());
    }
  }

  /**
   * This method disable the refresh button on the widget
   */
  public void disableButtonRefresh(String subName) {
    if (sub.getId().equals(subName)) {
      widget.getRefreshButton().disable();
      widget.getRefreshButton().setIcon("remove.png");
      widget.getRefreshButton().setTooltip("This subscription no longer exists on JORAM");
    }
  }
  
  /**
   * This method is called by the EventBus when a queue has been updated on the
   * server.
   * The widget is called to update it if the queue is currently displayed.
   */
  public void onSubscriptionUpdated(SubscriptionWTO sub) {
    if (this.sub.getId().equals(sub.getId())) {
      Log.debug(logCategory,
          "SubscriptionDetailPresenter: onSubscriptionUpdated " + sub.getId() + " " + sub.getMessagesList());
      this.sub = sub;
      widget.updateSubscription();
    }
  }

  /**
   * This method is called by the SubscriptionDetailWidget when the user click
   * the "delete" button of a message.
   * The message ID and the queue name are sent to the server which delete the
   * message.
   */
  public void deleteMessage(MessageWTO message, SubscriptionWTO sub) {
    Log.debug(logCategory, "SubscriptionDetailPresenter: deleteMessage " + message.getId());
    service.execute(new DeleteMessageAction(message.getId(), sub.getId(), false),
        new Handler<DeleteMessageResponse>(eventBus) {
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
  public List<HistoryData> getSubHistory() {
    return cache.getSpecificHistory(sub.getId());
  }

  /**
   * This method is called by the MainPresenter when the user close a tab.
   * he widget is called stop updating the non-displayed chart to avoid an
   * exception.
   */
  public void stopChart() {
    widget.stopChart();
  }

  public void initList() {
    Log.debug(logCategory, "SubscriptionDetailPresenter: initList()");

    List<String> vMessagesC = sub.getMessagesList();
    ArrayList<MessageWTO> listMessages = new ArrayList<MessageWTO>();

    for (String idMessage : vMessagesC) {
      listMessages.add(cache.getMessages().get(idMessage));
    }
    widget.setData(listMessages);
  }

  /**
   * This method is called by the SubscriptionDetailWidget when the user submit
   * the new message form.
   * The form information are sent to the server.
   */
  public void createNewMessage(MessageWTO message, String queueName) {
    service.execute(new SendNewMessageAction(message, queueName), new Handler<SendNewMessageResponse>(eventBus) {
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
    service.execute(new SendEditedMessageAction(message, queueName), new Handler<SendEditedMessageResponse>(eventBus) {
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
   * This method is called by the SubscriptionDetailWidget when updating the
   * chart.
   * 
   * @return A map of the subscriptions in the client side cache.
   */
  public Map<String, SubscriptionWTO> getSubscriptions() {
    return cache.getSubscriptions();
  }
}