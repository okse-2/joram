/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.widget.handler.subscription;

import com.scalagent.appli.client.Application;
import com.scalagent.appli.client.presenter.SubscriptionListPresenter;
import com.scalagent.appli.client.presenter.UserDetailPresenter;
import com.scalagent.appli.client.widget.record.SubscriptionListRecord;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;

/**
 * @author Yohann CINTRE
 */
public class SubscriptionDeleteClickHandler implements ClickHandler {

	private SubscriptionListPresenter sPresenter;
	private UserDetailPresenter uPresenter;
	private SubscriptionListRecord record;
	
	public SubscriptionDeleteClickHandler(SubscriptionListPresenter sPresenter, SubscriptionListRecord record) {
		super();
		this.sPresenter = sPresenter;
		this.record = record;
	}
	
	public SubscriptionDeleteClickHandler(UserDetailPresenter uPresenter, SubscriptionListRecord record) {
		super();
		this.uPresenter = uPresenter;
		this.record = record;
	}
	
	public void onClick(ClickEvent event) {
		SC.confirm(Application.messages.subscriptionWidget_confirmDelete(), new BooleanCallback() {
			@Override
			public void execute(Boolean value) {
				if(value) {
					if(sPresenter != null) sPresenter.deleteSubscription(record.getSubscription());
					if(uPresenter != null) uPresenter.deleteSubscription(record.getSubscription());
				}
			}
		});
	}	
}
