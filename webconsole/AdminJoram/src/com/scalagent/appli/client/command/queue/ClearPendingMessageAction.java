/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.command.queue;

import com.scalagent.appli.server.command.queue.ClearPendingMessageActionImpl;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;


/**
 * This action asks for devices list from the server.
 * @author Florian Gimbert
 */
@CalledMethod(value=ClearPendingMessageActionImpl.class)
public class ClearPendingMessageAction implements Action<ClearPendingMessageResponse> {
	
	private String queueName;

	public ClearPendingMessageAction() {}
	
	public ClearPendingMessageAction(String queueName) {
		this.queueName = queueName;
	}
	
	public String getQueueName() {
		return queueName;
	}
	
}
