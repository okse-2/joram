/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.command.message;

import com.google.gwt.event.shared.HandlerManager;
import com.scalagent.engine.client.command.Handler;

public abstract class LoadMessageHandler extends Handler<LoadMessageResponse> {
	
	public LoadMessageHandler(HandlerManager eventBus) {
		super(eventBus);
	}
	
}