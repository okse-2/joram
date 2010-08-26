/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.command.message;

import com.google.gwt.event.shared.HandlerManager;
import com.scalagent.engine.client.command.Handler;

public abstract class DeleteMessageHandler extends Handler<DeleteMessageResponse> {
	
	public DeleteMessageHandler(HandlerManager eventBus) {
		super(eventBus);
	}
	
}
