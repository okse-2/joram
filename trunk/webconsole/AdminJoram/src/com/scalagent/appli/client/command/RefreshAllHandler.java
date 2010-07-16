/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.command;

import com.google.gwt.event.shared.HandlerManager;
import com.scalagent.engine.client.command.Handler;

public abstract class RefreshAllHandler extends Handler<RefreshAllResponse> {
	
	public RefreshAllHandler(HandlerManager eventBus) {
		super(eventBus);
	}
	
}
