/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.command.subscription;

import com.scalagent.appli.server.command.subscription.SendEditedSubscriptionActionImpl;
import com.scalagent.appli.shared.SubscriptionWTO;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;

/**
 * This action send a updated subscription to the server.
 * 
 * @author Yohann CINTRE
 */
@CalledMethod(value=SendEditedSubscriptionActionImpl.class)
public class SendEditedSubscriptionAction implements Action<SendEditedSubscriptionResponse> {
	
	private SubscriptionWTO sub;

	public SendEditedSubscriptionAction() {}
	
	public SendEditedSubscriptionAction(SubscriptionWTO sub) {
		this.sub = sub;
	}
	
	public SubscriptionWTO getSubscription() {
		return sub;
	}
	
}
