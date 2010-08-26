/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.command.queue;

import com.scalagent.appli.server.command.queue.DeleteQueueActionImpl;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;


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
