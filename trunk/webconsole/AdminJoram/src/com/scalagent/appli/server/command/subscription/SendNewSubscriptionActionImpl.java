/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.server.command.subscription;

import com.scalagent.appli.client.command.subscription.SendNewSubscriptionAction;
import com.scalagent.appli.client.command.subscription.SendNewSubscriptionResponse;
import com.scalagent.appli.server.RPCServiceCache;
import com.scalagent.engine.server.command.ActionImpl;

/**
 * @author Yohann CINTRE
 */
public class SendNewSubscriptionActionImpl extends ActionImpl<SendNewSubscriptionResponse, SendNewSubscriptionAction, RPCServiceCache>{

	@Override
	public SendNewSubscriptionResponse execute(RPCServiceCache cache, SendNewSubscriptionAction action) {
		
		boolean result = cache.createNewSubscription(action.getSubscription());

		String info = "";
		
		if (result) {
			info = "The subscription \""+action.getSubscription().getName()+"\" has been created.";
		}
		else {
			info = "Error while creating new subscription \""+action.getSubscription().getName()+"\"";
		}
		
		return new SendNewSubscriptionResponse(result, info);
	}
}
