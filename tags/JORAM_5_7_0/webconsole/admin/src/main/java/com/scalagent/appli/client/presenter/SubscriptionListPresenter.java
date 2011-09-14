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

import com.google.gwt.event.shared.SimpleEventBus;
import com.scalagent.appli.client.RPCServiceCacheClient;
import com.scalagent.appli.client.RPCServiceCacheClient.HistoryData;
import com.scalagent.appli.client.command.subscription.DeleteSubscriptionAction;
import com.scalagent.appli.client.command.subscription.DeleteSubscriptionHandler;
import com.scalagent.appli.client.command.subscription.DeleteSubscriptionResponse;
import com.scalagent.appli.client.command.subscription.SendEditedSubscriptionAction;
import com.scalagent.appli.client.command.subscription.SendEditedSubscriptionHandler;
import com.scalagent.appli.client.command.subscription.SendEditedSubscriptionResponse;
import com.scalagent.appli.client.command.subscription.SendNewSubscriptionAction;
import com.scalagent.appli.client.command.subscription.SendNewSubscriptionHandler;
import com.scalagent.appli.client.command.subscription.SendNewSubscriptionResponse;
import com.scalagent.appli.client.event.common.UpdateCompleteEvent;
import com.scalagent.appli.client.event.common.UpdateCompleteHandler;
import com.scalagent.appli.client.event.subscription.DeletedSubscriptionHandler;
import com.scalagent.appli.client.event.subscription.NewSubscriptionHandler;
import com.scalagent.appli.client.event.subscription.SubscriptionDetailClickEvent;
import com.scalagent.appli.client.event.subscription.UpdatedSubscriptionHandler;
import com.scalagent.appli.client.widget.SubscriptionListWidget;
import com.scalagent.appli.client.widget.record.SubscriptionListRecord;
import com.scalagent.appli.shared.SubscriptionWTO;
import com.scalagent.engine.client.BaseRPCServiceAsync;
import com.scalagent.engine.client.presenter.BasePresenter;
import com.smartgwt.client.util.SC;

/**
 * This class is the presenter associated to the list of subscriptions.
 * Its widget is SubscriptionListWidget.
 * 
 * @author Yohann CINTRE
 */
public class SubscriptionListPresenter extends
    BasePresenter<SubscriptionListWidget, BaseRPCServiceAsync, RPCServiceCacheClient> implements
    NewSubscriptionHandler, DeletedSubscriptionHandler, UpdatedSubscriptionHandler, UpdateCompleteHandler {

  public SubscriptionListPresenter(BaseRPCServiceAsync testService, SimpleEventBus eventBus,
      RPCServiceCacheClient cache) {
    super(testService, cache, eventBus);
    this.eventBus = eventBus;
    widget = new SubscriptionListWidget(this);
  }

  /**
   * This method is called by the the SubscrtiptionListWidget when the user
   * click
   * on the "Refresh" button.
   * The "Refresh" button is disabled, the subscription list is updated.
   */
  public void fireRefreshAll() {
    widget.getRefreshButton().disable();
    cache.retrieveSubscription(true);
  }

  /**
   * This method is called by the EventBus when the update is done.
   * The refresh button is re-enabled and the chart redrawn
   */
  @Override
  public void onUpdateComplete(int updateType, String info) {
    if (updateType == UpdateCompleteEvent.SUBSCRIPTION_UPDATE) {
      widget.getRefreshButton().enable();
      widget.redrawChart(true);
    }
  }

  /**
   * This method is called by the EventBus when a new subscription has been
   * created on the server.
   * The widget is called to add it to the list.
   */
  public void onNewSubscription(SubscriptionWTO sub) {
    widget.addSubscription(new SubscriptionListRecord(sub));
  }

  /**
   * This method is called by the EventBus when a subscription has been deleted
   * on the server.
   * The widget is called to remove it from the list.
   */
  public void onSubscriptionDeleted(SubscriptionWTO sub) {
    widget.removeSubscription(new SubscriptionListRecord(sub));
  }

  /**
   * This method is called by the EventBus when a subscription has been updated
   * on the server.
   * The widget is called to update the subscription list.
   */
  public void onSubscriptionUpdated(SubscriptionWTO sub) {
    widget.updateSubscription(sub);
  }

  /**
   * This method is called by the SubscriptionListWidget when the updating the
   * chart.
   * 
   * @result A map containing the history of the current subscription
   */
  public List<HistoryData> getSubHistory(String name) {
    return cache.getSpecificHistory(name);
  }

  /**
   * This method is called by the SubscriptionListWidget when the user click on
   * "browse" button of a subscription.
   * An event is fired to the EventBus.
   */
  public void fireSubscriptionDetailsClick(SubscriptionWTO subscription) {
    eventBus.fireEvent(new SubscriptionDetailClickEvent(subscription));
  }

  /**
   * This method is called by the SubscriptionListWidget when the user submit
   * the new subscription form.
   * The form information are sent to the server.
   */
  public void createNewSubscription(SubscriptionWTO newSub) {
    service.execute(new SendNewSubscriptionAction(newSub), new SendNewSubscriptionHandler(eventBus) {
      @Override
      public void onSuccess(SendNewSubscriptionResponse response) {
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
   * This method is called by the SubscriptionListWidget when the user submit
   * the edited subscription form.
   * The form information are sent to the server.
   */
  public void editSubscription(SubscriptionWTO sub) {
    service.execute(new SendEditedSubscriptionAction(sub), new SendEditedSubscriptionHandler(eventBus) {
      @Override
      public void onSuccess(SendEditedSubscriptionResponse response) {
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
   * This method is called by the SubscriptionListWidget when the user click the
   * "delete" button of a subscription.
   * The subscription name is sent to the server which delete the subscription.
   */
  public void deleteSubscription(SubscriptionWTO sub) {
    service.execute(new DeleteSubscriptionAction(sub.getId()), new DeleteSubscriptionHandler(eventBus) {
      @Override
      public void onSuccess(DeleteSubscriptionResponse response) {
        if (response.isSuccess()) {
          SC.say(response.getMessage());
          fireRefreshAll();
        } else {
          SC.warn(response.getMessage());
          fireRefreshAll();
        }
      }
    });
  }
}
