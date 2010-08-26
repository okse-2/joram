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
import com.scalagent.appli.client.event.UpdateCompleteHandler;
import com.scalagent.appli.client.event.user.DeletedUserHandler;
import com.scalagent.appli.client.event.user.NewUserHandler;
import com.scalagent.appli.client.event.user.UpdatedUserHandler;
import com.scalagent.appli.client.event.user.UserDetailClickEvent;
import com.scalagent.appli.client.widget.UserListWidget;
import com.scalagent.appli.client.widget.record.UserListRecord;
import com.scalagent.appli.shared.UserWTO;
import com.scalagent.engine.client.presenter.BasePresenter;


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
}
