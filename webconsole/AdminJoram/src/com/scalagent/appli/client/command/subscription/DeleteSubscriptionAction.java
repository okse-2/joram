/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.command.subscription;

import com.scalagent.appli.server.command.subscription.DeleteSubscriptionActionImpl;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;

/**
 * This action delete a subscription on the server.
 * 
 * @author Yohann CINTRE
 */
@CalledMethod(value=DeleteSubscriptionActionImpl.class)
public class DeleteSubscriptionAction implements Action<DeleteSubscriptionResponse> {
	
	private String subName;

	public DeleteSubscriptionAction() {}
	
	public DeleteSubscriptionAction(String subName) {
		this.subName = subName;
	}
	
	public String getSubscriptionName() {
		return subName;
	}
	
}
