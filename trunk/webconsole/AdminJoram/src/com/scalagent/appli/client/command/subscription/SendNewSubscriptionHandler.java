/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.command.subscription;

import com.google.gwt.event.shared.HandlerManager;
import com.scalagent.engine.client.command.Handler;

/**
 * @author Yohann CINTRE
 */
public abstract class SendNewSubscriptionHandler extends Handler<SendNewSubscriptionResponse> {
	
	public SendNewSubscriptionHandler(HandlerManager eventBus) {
		super(eventBus);
	}
	
}
