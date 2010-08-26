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
import com.scalagent.appli.client.event.subscription.DeletedSubscriptionHandler;
import com.scalagent.appli.client.event.subscription.NewSubscriptionHandler;
import com.scalagent.appli.client.event.subscription.SubscriptionDetailClickEvent;
import com.scalagent.appli.client.event.subscription.UpdatedSubscriptionHandler;
import com.scalagent.appli.client.widget.SubscriptionListWidget;
import com.scalagent.appli.client.widget.record.SubscriptionListRecord;
import com.scalagent.appli.shared.SubscriptionWTO;
import com.scalagent.engine.client.presenter.BasePresenter;


/**
 * This class is the presenter associated to the list of devices.
 * Its widget is DevicesWidget.
 * 
 */
public class SubscriptionListPresenter extends BasePresenter<SubscriptionListWidget, RPCServiceAsync, RPCServiceCacheClient> 
implements 
NewSubscriptionHandler,
DeletedSubscriptionHandler,
UpdatedSubscriptionHandler,
UpdateCompleteHandler
{

	public SubscriptionListPresenter(RPCServiceAsync testService, HandlerManager eventBus, RPCServiceCacheClient cache) {

		super(testService, cache, eventBus);

		System.out.println("### appli.client.presenter.SubscriptionPresenter loaded ");

		this.eventBus = eventBus;
		widget = new SubscriptionListWidget(this);
	}

	public void fireRefreshAll() {
		widget.getRefreshButton().disable();
		cache.retrieveSubscription(true);
	}

	@Override
	public void onUpdateComplete(String info) {
		if(info.equals("sub")) {
			widget.getRefreshButton().enable();
			widget.redrawChart(true);
		}
	}

	public void onNewSubscription(SubscriptionWTO sub) {
		widget.addSubscription(new SubscriptionListRecord(sub));
	}

	public void onSubscriptionDeleted(SubscriptionWTO sub) {
		widget.removeSubscription(new SubscriptionListRecord(sub));
	}

	public void onSubscriptionUpdated(SubscriptionWTO sub) {
		widget.updateUser(sub);	
	}
	
	public SortedMap<Date, int[]> getSubHistory(String name) {
		return cache.getSpecificHistory(name);
	}

	public void fireQueueDetailsClick(SubscriptionWTO subscription) {
		eventBus.fireEvent(new SubscriptionDetailClickEvent(subscription));
	}
}
