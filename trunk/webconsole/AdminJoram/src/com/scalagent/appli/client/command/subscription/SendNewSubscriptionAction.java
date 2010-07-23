/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.command.subscription;

import com.scalagent.appli.server.command.subscription.SendNewSubscriptionActionImpl;
import com.scalagent.appli.shared.SubscriptionWTO;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;


/**
 * This action asks for devices list from the server.
 */
@CalledMethod(value=SendNewSubscriptionActionImpl.class)
public class SendNewSubscriptionAction implements Action<SendNewSubscriptionResponse> {
	
	private SubscriptionWTO sub;

	public SendNewSubscriptionAction() {}
	
	public SendNewSubscriptionAction(SubscriptionWTO sub) {
		this.sub = sub;
	}
	
	public SubscriptionWTO getSubscription() {
		return sub;
	}
	
}
