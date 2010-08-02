/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.command.user;

import com.google.gwt.event.shared.HandlerManager;
import com.scalagent.engine.client.command.Handler;

/**
 * @author Yohann CINTRE
 */
public abstract class SendEditedUserHandler extends Handler<SendEditedUserResponse> {
	
	public SendEditedUserHandler(HandlerManager eventBus) {
		super(eventBus);
	}
	
}
