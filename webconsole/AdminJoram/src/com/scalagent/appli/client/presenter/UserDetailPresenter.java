/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.presenter;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;

import com.google.gwt.event.shared.HandlerManager;
import com.scalagent.appli.client.RPCServiceAsync;
import com.scalagent.appli.client.RPCServiceCacheClient;
import com.scalagent.appli.client.command.subscription.DeleteSubscriptionAction;
import com.scalagent.appli.client.command.subscription.DeleteSubscriptionHandler;
import com.scalagent.appli.client.command.subscription.DeleteSubscriptionResponse;
import com.scalagent.appli.client.command.subscription.SendEditedSubscriptionAction;
import com.scalagent.appli.client.command.subscription.SendEditedSubscriptionHandler;
import com.scalagent.appli.client.command.subscription.SendEditedSubscriptionResponse;
import com.scalagent.appli.client.command.subscription.SendNewSubscriptionAction;
import com.scalagent.appli.client.command.subscription.SendNewSubscriptionHandler;
import com.scalagent.appli.client.command.subscription.SendNewSubscriptionResponse;
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
import com.scalagent.engine.client.presenter.BasePresenter;
import com.smartgwt.client.util.SC;

/**
 * This class is the presenter associated to the details about a user.
 * Its widget is UserDetailWidget.
 * 
 * @author Yohann CINTRE
 */
public class UserDetailPresenter extends BasePresenter<UserDetailWidget, RPCServiceAsync, RPCServiceCacheClient> 
implements 
NewSubscriptionHandler,
DeletedSubscriptionHandler,
UpdatedSubscriptionHandler,
UpdateCompleteHandler,
DeletedUserHandler,
UpdatedUserHandler
{
	private UserWTO user;

	public UserDetailPresenter(RPCServiceAsync serviceRPC, HandlerManager eventBus,RPCServiceCacheClient cache, UserWTO user) {

		super(serviceRPC, cache, eventBus);

		this.eventBus = eventBus;
		this.user = user;

		widget = new UserDetailWidget(this);
		retrieveSubscription();
	}

	/**
	 * @return The user displayed
	 */
	public UserWTO getUser() { return user; }

	/**
	 * This method refresh the subscription list for the displayed user
	 */	
	public void retrieveSubscription() {
		cache.retrieveSubscription(true);
	}

	/**
	 * This method is called by the the UserDetailWidget when the user click 
	 * on the "Refresh" button.
	 * The "Refresh" button is disabled, the user list and subscriptions are updated.
	 */	
	public void fireRefreshAll() {
		widget.getRefreshButton().disable();

		cache.retrieveUser(true);
		cache.retrieveSubscription(true);

		List<String> lstSubSUser = Arrays.asList(user.getSubscriptionNames());
		HashMap<String, SubscriptionWTO> lstSubsCache = cache.getSubscriptions();

		for (String subName : lstSubsCache.keySet()) {
			if(lstSubSUser.contains(subName)) {
				getWidget().updateSubscription(lstSubsCache.get(subName));
			}
		}
	}

	/**
	 * This method is called by the EventBus when a new subscription has been created on the server.
	 * The widget is called to add it to the list if the subscription belong to the displayed user
	 */
	public void onNewSubscription(SubscriptionWTO sub) {
		List<String> lstSub = Arrays.asList(user.getSubscriptionNames());
		if(lstSub.contains(sub.getName())) {
			getWidget().addSubscription(new SubscriptionListRecord(sub));
		}
	}

	/**
	 * This method is called by the EventBus when a subscription has been updated on the server.
	 * The widget is called to update it if the subscription belong to the displayed user.
	 */
	public void onSubscriptionUpdated(SubscriptionWTO sub) {
		List<String> lstSub = Arrays.asList(user.getSubscriptionNames());
		if(lstSub.contains(sub.getName())) {
			getWidget().updateSubscription(sub);
		}
	}

	/**
	 * This method is called by the EventBus when a subscription has been deleted on the server.
	 * The widget is called to remove it from the list if the subscription belong to the displayed user.
	 */
	public void onSubscriptionDeleted(SubscriptionWTO sub) {
		List<String> lstSub = Arrays.asList(user.getSubscriptionNames());
		if(lstSub.contains(sub.getName())) {
			widget.removeSubscription(new SubscriptionListRecord(sub));
		}

	}

	/**
	 * This method is called by the EventBus when the update is done.
	 * The refresh button is re-enabled and the chart redrawn
	 */
	public void onUpdateComplete(String info) {
		if(info.equals("user") || info.equals("sub")) {
			widget.enableRefreshButton();
			widget.redrawChart(true);
		}
	}

	/**
	 * This method is called by the EventBus when a user has been deleted on the server.
	 * The refresh button is disabled.
	 */
	public void onUserDeleted(UserWTO user) {

		if(this.user.getName().equals(user.getName())) {
			widget.setActive(false);
			widget.getRefreshButton().disable();
			widget.getRefreshButton().setIcon("remove.png");
			widget.getRefreshButton().setTooltip("This user no longer exists on JORAM");
		}
	}

	/**
	 * This method is called by the EventBus when a user has been updated on the server.
	 * The widget is called to update it if the user is currently displayed.
	 */
	public void onUserUpdated(UserWTO user) {
		if(this.user.getName().equals(user.getName())) {
			this.user = user;
			widget.updateUser();
		}
	}
	
	/**
	 * This method is called by the MainPresenter when the user close a tab.
	 * The widget is called to stop updating the non-displayed chart to avoid an exception.
	 */
	public void stopChart() {
		widget.stopChart();
	}

	/**
	 * This method is called by the UserDetailWidget when the updating the chart.
	 * @result A map containing the history of the current user
	 */
	public SortedMap<Date, int[]> getUserHistory() {
		return cache.getSpecificHistory(user.getName());
	}

	/**
	 * This method is called by the UserDetailWidget when the updating the chart.
	 * @result A map containing the history of the specified subscription
	 */
	public SortedMap<Date, int[]> getSubHistory(String sub) {
		return cache.getSpecificHistory(sub);
	}

	/**
	 * This method is called by the UserDetailWidget when the user submit the new subscription form.
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
	 * This method is called by the UserDetailWidget when the user submit the edited subscription form.
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
	 * This method is called by the UserDetailWidget when the user click the "delete" button of a subscription.
	 * The subscription name is sent to the server which delete the subscription.
	 */
	public void deleteSubscription(SubscriptionWTO sub) {
		service.execute(new DeleteSubscriptionAction(sub.getName()), new DeleteSubscriptionHandler(eventBus) {
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