/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
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
import com.scalagent.appli.client.event.UpdateCompleteHandler;
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
 * This class is the presenter associated to the list of devices.
 * Its widget is DevicesWidget.
 * 
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

		System.out.println("### appli.client.presenter.UserPresenter loaded ");

		this.eventBus = eventBus;
		widget = new UserListWidget(this);
	}

	public void fireRefreshAll() {
		widget.getRefreshButton().disable();
		cache.retrieveUser(true);
		widget.getUserList().markForRedraw();
	}

	@Override
	public void onUpdateComplete(String info) {
		if(info.equals("user")) {
			widget.getRefreshButton().enable();
			widget.redrawChart(true);
		}
	}

	@Override
	public void onNewUser(UserWTO user) {
		widget.addUser(new UserListRecord(user));
	}

	@Override
	public void onUserDeleted(UserWTO user) {
		widget.removeUser(new UserListRecord(user));
	}

	@Override
	public void onUserUpdated(UserWTO user) {
		widget.updateUser(user);	
	}

	public void fireUserDetailsClick(UserWTO user) {
		eventBus.fireEvent(new UserDetailClickEvent(user));
	}
	
	public SortedMap<Date, int[]> getUserHistory(String name) {
		return cache.getSpecificHistory(name);
	}

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
