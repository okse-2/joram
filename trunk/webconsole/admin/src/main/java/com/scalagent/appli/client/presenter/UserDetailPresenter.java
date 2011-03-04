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
import com.scalagent.appli.client.event.subscription.UpdatedSubscriptionHandler;
import com.scalagent.appli.client.event.user.DeletedUserHandler;
import com.scalagent.appli.client.event.user.UpdatedUserHandler;
import com.scalagent.appli.client.widget.UserDetailWidget;
import com.scalagent.appli.client.widget.record.SubscriptionListRecord;
import com.scalagent.appli.shared.SubscriptionWTO;
import com.scalagent.appli.shared.UserWTO;
import com.scalagent.engine.client.BaseRPCServiceAsync;
import com.scalagent.engine.client.presenter.BasePresenter;
import com.smartgwt.client.util.SC;

/**
 * This class is the presenter associated to the details about a user.
 * Its widget is UserDetailWidget.
 * 
 * @author Yohann CINTRE
 */
public class UserDetailPresenter extends
    BasePresenter<UserDetailWidget, BaseRPCServiceAsync, RPCServiceCacheClient> implements
    NewSubscriptionHandler, DeletedSubscriptionHandler, UpdatedSubscriptionHandler, UpdateCompleteHandler,
    DeletedUserHandler, UpdatedUserHandler {

  private UserWTO user;

  public UserDetailPresenter(BaseRPCServiceAsync serviceRPC, SimpleEventBus eventBus,
      RPCServiceCacheClient cache, UserWTO user) {

    super(serviceRPC, cache, eventBus);

    this.eventBus = eventBus;
    this.user = user;

    widget = new UserDetailWidget(this);
    retrieveSubscription();
  }

  /**
   * @return The user displayed
   */
  public UserWTO getUser() {
    return user;
  }

  /**
   * This method refresh the subscription list for the displayed user
   */
  public void retrieveSubscription() {
    cache.retrieveSubscription(true);
  }

  /**
   * This method is called by the the UserDetailWidget when the user click
   * on the "Refresh" button.
   * The "Refresh" button is disabled, the user list and subscriptions are
   * updated.
   */
  public void fireRefreshAll() {
    widget.getRefreshButton().disable();

    cache.retrieveUser(true);
    cache.retrieveSubscription(true);

    String[] subSUser = user.getSubscriptionNames();
    Map<String, SubscriptionWTO> lstSubsCache = cache.getSubscriptions();

    for (int i = 0; i < subSUser.length; i++) {
      getWidget().updateSubscription(lstSubsCache.get(subSUser[i]));
    }
  }

  private boolean isUserSubscription(String subName) {
    boolean found = false;
    String[] subscriptions = user.getSubscriptionNames();
    for (int i = 0; i < subscriptions.length; i++) {
      if (subscriptions[i].equals(subName)) {
        found = true;
        break;
      }
    }
    return found;
  }

  /**
   * This method is called by the EventBus when a new subscription has been
   * created on the server.
   * The widget is called to add it to the list if the subscription belong to
   * the displayed user
   */
  public void onNewSubscription(SubscriptionWTO sub) {
    if (isUserSubscription(sub.getId())) {
      getWidget().addSubscription(new SubscriptionListRecord(sub));
    }
  }

  /**
   * This method is called by the EventBus when a subscription has been updated
   * on the server.
   * The widget is called to update it if the subscription belong to the
   * displayed user.
   */
  public void onSubscriptionUpdated(SubscriptionWTO sub) {
    if (isUserSubscription(sub.getId())) {
      getWidget().updateSubscription(sub);
    }
  }

  /**
   * This method is called by the EventBus when a subscription has been deleted
   * on the server.
   * The widget is called to remove it from the list if the subscription belong
   * to the displayed user.
   */
  public void onSubscriptionDeleted(SubscriptionWTO sub) {
    getWidget().removeSubscription(new SubscriptionListRecord(sub));
  }

  /**
   * This method is called by the EventBus when the update is done.
   * The refresh button is re-enabled and the chart redrawn
   */
  public void onUpdateComplete(int updateType, String info) {
    if (updateType == UpdateCompleteEvent.USER_UPDATE) {
      widget.enableRefreshButton();
      widget.redrawChart();
    }
  }

  /**
   * This method is called by the EventBus when a user has been deleted on the
   * server.
   * The refresh button is disabled.
   */
  public void onUserDeleted(UserWTO user) {

    if (this.user.getId().equals(user.getId())) {
      widget.setActive(false);
      widget.getRefreshButton().disable();
      widget.getRefreshButton().setIcon("remove.png");
      widget.getRefreshButton().setTooltip("This user no longer exists on JORAM");
    }
  }

  /**
   * This method is called by the EventBus when a user has been updated on the
   * server.
   * The widget is called to update it if the user is currently displayed.
   */
  public void onUserUpdated(UserWTO user) {
    if (this.user.getId().equals(user.getId())) {
      this.user = user;
      widget.updateUser();
    }
  }

  /**
   * This method is called by the MainPresenter when the user close a tab.
   * The widget is called to stop updating the non-displayed chart to avoid an
   * exception.
   */
  public void stopChart() {
    widget.stopChart();
  }

  /**
   * This method is called by the UserDetailWidget when the widget is
   * initialized.
   * It retrieve the subscriptions for the current user to add them to the
   * subscription list of the widget.
   */
  public void initList() {
    String[] vSubC = user.getSubscriptionNames();
    ArrayList<SubscriptionWTO> listSubs = new ArrayList<SubscriptionWTO>();
    for (int i = 0; i < vSubC.length; i++) {
      listSubs.add(cache.getSubscriptions().get(vSubC[i]));
    }
    widget.setData(listSubs);
  }

  /**
   * This method is called by the UserDetailWidget when the updating the chart.
   * 
   * @result A map containing the history of the current user
   */
  public List<HistoryData> getUserHistory() {
    return cache.getSpecificHistory(user.getId());
  }

  /**
   * This method is called by the UserDetailWidget when the updating the chart.
   * 
   * @result A map containing the history of the specified subscription
   */
  public List<HistoryData> getSubHistory(String sub) {
    return cache.getSpecificHistory(sub);
  }

  /**
   * This method is called by the UserDetailWidget when the user submit the new
   * subscription form.
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
   * This method is called by the UserDetailWidget when the user submit the
   * edited subscription form.
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
   * This method is called by the UserDetailWidget when the user click the
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