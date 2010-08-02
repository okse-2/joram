/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.command.topic;

import com.google.gwt.event.shared.HandlerManager;
import com.scalagent.engine.client.command.Handler;

/**
 * @author Yohann CINTRE
 */
public abstract class DeleteTopicHandler extends Handler<DeleteTopicResponse> {
	
	public DeleteTopicHandler(HandlerManager eventBus) {
		super(eventBus);
	}
	
}
