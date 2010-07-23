/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
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
import com.scalagent.appli.client.event.UpdateCompleteHandler;
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
 * This class is the presenter associated to the list of devices.
 * Its widget is DevicesWidget.
 * 
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

		System.out.println("### appli.client.presenter.UserDetailsPresenter loaded ");
		this.eventBus = eventBus;
		this.user = user;

		widget = new UserDetailWidget(this);
		retrieveSubscription();
	}

	public UserWTO getUser() { return user; }


	public void retrieveSubscription() {
		cache.retrieveSubscription(true);
	}

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

	@Override
	public void onNewSubscription(SubscriptionWTO sub) {
		List<String> lstSub = Arrays.asList(user.getSubscriptionNames());
		if(lstSub.contains(sub.getName())) {
			getWidget().addSubscription(new SubscriptionListRecord(sub));
		}
	}

	@Override
	public void onSubscriptionUpdated(SubscriptionWTO sub) {
		List<String> lstSub = Arrays.asList(user.getSubscriptionNames());
		if(lstSub.contains(sub.getName())) {
			getWidget().updateSubscription(sub);
		}
	}

	@Override
	public void onSubscriptionDeleted(SubscriptionWTO sub) {
		List<String> lstSub = Arrays.asList(user.getSubscriptionNames());
		if(lstSub.contains(sub.getName())) {
			widget.removeSubscription(new SubscriptionListRecord(sub));
		}

	}

	@Override
	public void onUpdateComplete(String info) {
		if(info.equals("user") || info.equals("sub")) {
			widget.enableRefreshButton();
			widget.redrawChart(true);
		}
	}

	@Override
	public void onUserDeleted(UserWTO user) {
		
		if(this.user.getName().equals(user.getName())) {
			widget.setActive(false);
			widget.getRefreshButton().disable();
			widget.getRefreshButton().setIcon("remove.png");
			widget.getRefreshButton().setTooltip("This user no longer exists on JORAM");
		}
	}

	@Override
	public void onUserUpdated(UserWTO user) {
		if(this.user.getName().equals(user.getName())) {
			this.user = user;
			widget.updateUser();
		}
	}

	public void stopChart() {
		widget.stopChart();
	}

	public SortedMap<Date, int[]> getUserHistory() {
		return cache.getSpecificHistory(user.getName());
	}

	public SortedMap<Date, int[]> getSubHistory(String sub) {
		return cache.getSpecificHistory(sub);
	}

	
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