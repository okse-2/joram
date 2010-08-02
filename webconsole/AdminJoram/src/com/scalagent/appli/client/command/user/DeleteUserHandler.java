/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.command.user;

import com.google.gwt.event.shared.HandlerManager;
import com.scalagent.engine.client.command.Handler;

/**
 * @author Yohann CINTRE
 */
public abstract class DeleteUserHandler extends Handler<DeleteUserResponse> {
	
	public DeleteUserHandler(HandlerManager eventBus) {
		super(eventBus);
	}
	
}
