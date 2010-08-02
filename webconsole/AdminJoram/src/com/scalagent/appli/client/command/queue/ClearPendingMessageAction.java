/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.command.queue;

import com.scalagent.appli.server.command.queue.ClearPendingMessageActionImpl;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;


/**
 * This action asks server to clean the pending messages for the specified queue.
 * 
 * @author Yohann CINTRE
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
