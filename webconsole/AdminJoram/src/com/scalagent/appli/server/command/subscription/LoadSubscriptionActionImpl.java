/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */


package com.scalagent.appli.server.command.subscription;

import java.util.List;

import com.scalagent.appli.client.command.subscription.LoadSubscriptionAction;
import com.scalagent.appli.client.command.subscription.LoadSubscriptionResponse;
import com.scalagent.appli.server.RPCServiceCache;
import com.scalagent.appli.shared.SubscriptionWTO;
import com.scalagent.engine.server.command.ActionImpl;


/**
 *
 * @author sgonzalez
 */
public class LoadSubscriptionActionImpl 
extends ActionImpl<LoadSubscriptionResponse, LoadSubscriptionAction, RPCServiceCache> {

	@Override
	public LoadSubscriptionResponse execute(RPCServiceCache cache, LoadSubscriptionAction action) {

		List<SubscriptionWTO> subs = cache.getSubscriptions(this.getHttpSession(), action.isRetrieveAll(), action.isforceUpdate());
		System.out.println("### engine.server.command.queue.LoadSubscriptionActionImpl.execute : "+subs.size()+" subscriptions recupérés sur le serveur");
		return new LoadSubscriptionResponse(subs);
	}

}