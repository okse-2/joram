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
import com.scalagent.appli.client.command.user.DeleteUserAction;
import com.scalagent.appli.client.command.user.DeleteUserHandler;
import com.scalagent.appli.client.command.user.DeleteUserResponse;
import com.scalagent.appli.client.command.user.SendEditedUserAction;
import com.scalagent.appli.client.command.user.SendEditedUserHandler;
import com.scalagent.appli.client.command.user.SendEditedUserResponse;
import com.scalagent.appli.client.command.user.SendNewUserAction;
import com.scalagent.appli.client.command.user.SendNewUserHandler;
import com.scalagent.appli.client.command.user.SendNewUserResponse;
import com.scalagent.appli.client.event.common.UpdateCompleteEvent;
import com.scalagent.appli.client.event.common.UpdateCompleteHandler;
import com.scalagent.appli.client.event.user.DeletedUserHandler;
import com.scalagent.appli.client.event.user.NewUserHandler;
import com.scalagent.appli.client.event.user.UpdatedUserHandler;
import com.scalagent.appli.client.event.user.UserDetailClickEvent;
import com.scalagent.appli.client.widget.UserListWidget;
import com.scalagent.appli.client.widget.record.UserListRecord;
import com.scalagent.appli.shared.UserWTO;
import com.scalagent.engine.client.BaseRPCServiceAsync;
import com.scalagent.engine.client.presenter.BasePresenter;
import com.smartgwt.client.util.SC;

/**
 * This class is the presenter associated to the list of users.
 * Its widget is UserListWidget.
 * 
 * @author Yohann CINTRE
 */
public class UserListPresenter extends
    BasePresenter<UserListWidget, BaseRPCServiceAsync, RPCServiceCacheClient> implements NewUserHandler,
    DeletedUserHandler, UpdatedUserHandler, UpdateCompleteHandler {

  public UserListPresenter(BaseRPCServiceAsync testService, SimpleEventBus eventBus,
      RPCServiceCacheClient cache) {

    super(testService, cache, eventBus);

    this.eventBus = eventBus;
    widget = new UserListWidget(this);
  }

  /**
   * This method is called by the the UserListWidget when the user click
   * on the "Refresh" button.
   * The "Refresh" button is disabled, the user list is updated.
   */
  public void fireRefreshAll() {
    widget.getRefreshButton().disable();
    cache.retrieveUser(true);
    widget.getUserList().markForRedraw();
  }

  /**
   * This method is called by the EventBus when the update is done.
   * The refresh button is re-enabled and the chart redrawn
   */
  public void onUpdateComplete(int updateType, String info) {
    if (updateType == UpdateCompleteEvent.USER_UPDATE) {
      widget.getRefreshButton().enable();
      widget.redrawChart(true);
    }
  }

  /**
   * This method is called by the EventBus when a new user has been created on
   * the server.
   * The widget is called to add it to the list.
   */
  public void onNewUser(UserWTO user) {
    widget.addUser(new UserListRecord(user));
  }

  /**
   * This method is called by the EventBus when a user has been deleted on the
   * server.
   * The widget is called to remove it from the list.
   */
  public void onUserDeleted(UserWTO user) {
    widget.removeUser(new UserListRecord(user));
  }

  /**
   * This method is called by the EventBus when a uner has been updated on the
   * server.
   * The widget is called to update the user list.
   */
  public void onUserUpdated(UserWTO user) {
    widget.updateUser(user);
  }

  /**
   * This method is called by the UserListWidget when the user click on "browse"
   * button of a user.
   * An event is fired to the EventBus.
   */
  public void fireUserDetailsClick(UserWTO user) {
    eventBus.fireEvent(new UserDetailClickEvent(user));
  }

  /**
   * This method is called by the UserListWidget when the updating the chart.
   * 
   * @result A map containing the history of the current user.
   */
  public List<HistoryData> getUserHistory(String name) {
    return cache.getSpecificHistory(name);
  }

  /**
   * This method is called by the UserListWidget when the user submit the new
   * user form.
   * The form information are sent to the server.
   */
  public void createNewUser(UserWTO user) {
    service.execute(new SendNewUserAction(user), new SendNewUserHandler(eventBus) {
      @Override
      public void onSuccess(SendNewUserResponse response) {
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
   * This method is called by the UserListWidget when the user submit the edited
   * user form.
   * The form information are sent to the server.
   */
  public void editUser(UserWTO user) {
    service.execute(new SendEditedUserAction(user), new SendEditedUserHandler(eventBus) {
      @Override
      public void onSuccess(SendEditedUserResponse response) {
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
   * This method is called by the UserListWidget when the user click the
   * "delete" button of a user.
   * The user name is sent to the server which delete the user.
   */
  public void deleteUser(UserWTO user) {
    service.execute(new DeleteUserAction(user.getId()), new DeleteUserHandler(eventBus) {
      @Override
      public void onSuccess(DeleteUserResponse response) {
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
