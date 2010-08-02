/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.presenter;

import java.util.Date;
import java.util.SortedMap;

import com.google.gwt.event.shared.HandlerManager;
import com.scalagent.appli.client.RPCServiceAsync;
import com.scalagent.appli.client.RPCServiceCacheClient;
import com.scalagent.appli.client.command.user.DeleteUserAction;
import com.scalagent.appli.client.command.user.DeleteUserHandler;
import com.scalagent.appli.client.command.user.DeleteUserResponse;
import com.scalagent.appli.client.command.user.SendEditedUserAction;
import com.scalagent.appli.client.command.user.SendEditedUserHandler;
import com.scalagent.appli.client.command.user.SendEditedUserResponse;
import com.scalagent.appli.client.command.user.SendNewUserAction;
import com.scalagent.appli.client.command.user.SendNewUserHandler;
import com.scalagent.appli.client.command.user.SendNewUserResponse;
import com.scalagent.appli.client.event.common.UpdateCompleteHandler;
import com.scalagent.appli.client.event.user.DeletedUserHandler;
import com.scalagent.appli.client.event.user.NewUserHandler;
import com.scalagent.appli.client.event.user.UpdatedUserHandler;
import com.scalagent.appli.client.event.user.UserDetailClickEvent;
import com.scalagent.appli.client.widget.UserListWidget;
import com.scalagent.appli.client.widget.record.UserListRecord;
import com.scalagent.appli.shared.UserWTO;
import com.scalagent.engine.client.presenter.BasePresenter;
import com.smartgwt.client.util.SC;

/**
 * This class is the presenter associated to the list of users.
 * Its widget is UserListWidget.
 * 
 * @author Yohann CINTRE
 */
public class UserListPresenter extends BasePresenter<UserListWidget, RPCServiceAsync, RPCServiceCacheClient> 
implements 
NewUserHandler,
DeletedUserHandler,
UpdatedUserHandler,
UpdateCompleteHandler
{
	public UserListPresenter(RPCServiceAsync testService, HandlerManager eventBus, RPCServiceCacheClient cache) {

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
	public void onUpdateComplete(String info) {
		if(info.equals("user")) {
			widget.getRefreshButton().enable();
			widget.redrawChart(true);
		}
	}

	/**
	 * This method is called by the EventBus when a new user has been created on the server.
	 * The widget is called to add it to the list.
	 */
	public void onNewUser(UserWTO user) {
		widget.addUser(new UserListRecord(user));
	}

	/**
	 * This method is called by the EventBus when a user has been deleted on the server.
	 * The widget is called to remove it from the list.
	 */
	public void onUserDeleted(UserWTO user) {
		widget.removeUser(new UserListRecord(user));
	}

	/**
	 * This method is called by the EventBus when a uner has been updated on the server.
	 * The widget is called to update the user list.
	 */
	public void onUserUpdated(UserWTO user) {
		widget.updateUser(user);	
	}

	/**
	 * This method is called by the UserListWidget when the user click on "browse" button of a user.
	 * An event is fired to the EventBus.
	 */
	public void fireUserDetailsClick(UserWTO user) {
		eventBus.fireEvent(new UserDetailClickEvent(user));
	}

	/**
	 * This method is called by the UserListWidget when the updating the chart.
	 * @result A map containing the history of the current user.
	 */
	public SortedMap<Date, int[]> getUserHistory(String name) {
		return cache.getSpecificHistory(name);
	}

	/**
	 * This method is called by the UserListWidget when the user submit the new user form.
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
	 * This method is called by the UserListWidget when the user submit the edited user form.
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
	 * This method is called by the UserListWidget when the user click the "delete" button of a user.
	 * The user name is sent to the server which delete the user.
	 */
	public void deleteUser(UserWTO user) {
		service.execute(new DeleteUserAction(user.getName()), new DeleteUserHandler(eventBus) {
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
