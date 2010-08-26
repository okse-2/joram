/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.widget.handler.subscription;

import com.scalagent.appli.client.presenter.SubscriptionListPresenter;
import com.scalagent.appli.client.widget.record.SubscriptionListRecord;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;

public class SubscriptionDetailsClickHandler implements ClickHandler {


	private SubscriptionListPresenter subscriptionPresenter;
	private SubscriptionListRecord record;
	
	
	public SubscriptionDetailsClickHandler(SubscriptionListPresenter subscriptionPresenter, SubscriptionListRecord record) {
		super();
		this.subscriptionPresenter = subscriptionPresenter;
		this.record = record;
	}
	
	@Override
	public void onClick(ClickEvent event) {
		subscriptionPresenter.fireQueueDetailsClick(record.getSubscription());
	}
}
