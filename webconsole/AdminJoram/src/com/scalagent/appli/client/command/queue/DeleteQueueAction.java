/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.command.queue;

import com.scalagent.appli.server.command.queue.DeleteQueueActionImpl;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;

/**
 * This action delete a queue from the server.
 * 
 * @author Yohann CINTRE
 */
@CalledMethod(value=DeleteQueueActionImpl.class)
public class DeleteQueueAction implements Action<DeleteQueueResponse> {
	
	private String queueName;

	public DeleteQueueAction() {}
	
	public DeleteQueueAction(String queueName) {
		this.queueName = queueName;
	}
	
	public String getQueueName() {
		return queueName;
	}
	
}
