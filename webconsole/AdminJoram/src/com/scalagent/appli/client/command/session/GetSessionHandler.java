/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.command.session;

import com.google.gwt.event.shared.HandlerManager;
import com.scalagent.engine.client.command.Handler;


public abstract class GetSessionHandler extends Handler<GetSessionResponse> {
	
	public GetSessionHandler(HandlerManager eventBus) {
		super(eventBus);
	}
	
}
