/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.command.subscription;

import com.google.gwt.event.shared.HandlerManager;
import com.scalagent.engine.client.command.Handler;

public abstract class SendEditedSubscriptionHandler extends Handler<SendEditedSubscriptionResponse> {
	
	public SendEditedSubscriptionHandler(HandlerManager eventBus) {
		super(eventBus);
	}
	
}
