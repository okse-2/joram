/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.command.queue;

import com.scalagent.appli.server.command.queue.ClearWaitingRequestActionImpl;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;


/**
 * This action asks server to clean the waiting requests for the specified queue.
 * 
 * @author Yohann CINTRE
 */
@CalledMethod(value=ClearWaitingRequestActionImpl.class)
public class ClearWaitingRequestAction implements Action<ClearWaitingRequestResponse> {
	
	private String queueName;

	public ClearWaitingRequestAction() {}
	
	public ClearWaitingRequestAction(String queueName) {
		this.queueName = queueName;
	}
	
	public String getQueueName() {
		return queueName;
	}
	
}
