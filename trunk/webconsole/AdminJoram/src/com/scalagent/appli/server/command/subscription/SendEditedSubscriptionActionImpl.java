/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.server.command.subscription;

import com.scalagent.appli.client.command.subscription.SendEditedSubscriptionAction;
import com.scalagent.appli.client.command.subscription.SendEditedSubscriptionResponse;
import com.scalagent.appli.server.RPCServiceCache;
import com.scalagent.engine.server.command.ActionImpl;

public class SendEditedSubscriptionActionImpl extends ActionImpl<SendEditedSubscriptionResponse, SendEditedSubscriptionAction, RPCServiceCache>{

	@Override
	public SendEditedSubscriptionResponse execute(RPCServiceCache cache, SendEditedSubscriptionAction action) {
		
		boolean result = cache.editSubscription(action.getSubscription());

		String info = "";
		
		if (result) {
			info = "The subscription \""+action.getSubscription().getName()+"\" has been updated.";
		}
		else {
			info = "Error while updating queue \""+action.getSubscription().getName()+"\"";
		}
		
		return new SendEditedSubscriptionResponse(result, info);
	}
}
