/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.server.command.subscription;


import com.scalagent.appli.client.command.subscription.DeleteSubscriptionAction;
import com.scalagent.appli.client.command.subscription.DeleteSubscriptionResponse;
import com.scalagent.appli.server.RPCServiceCache;
import com.scalagent.engine.server.command.ActionImpl;


public class DeleteSubscriptionActionImpl extends ActionImpl<DeleteSubscriptionResponse, DeleteSubscriptionAction, RPCServiceCache>{


	@Override
	public DeleteSubscriptionResponse execute(RPCServiceCache cache, DeleteSubscriptionAction action) {
		boolean result = cache.deleteSubscription(action.getSubscriptionName());

		String info = "";

		if (result) {
			info = "The subscription \""+action.getSubscriptionName()+"\" has been deleted.";
		}
		else {
			info = "Error while deleting subscription: \""+action.getSubscriptionName()+"\".";
		}

		return new DeleteSubscriptionResponse(result, info);
	}


}
