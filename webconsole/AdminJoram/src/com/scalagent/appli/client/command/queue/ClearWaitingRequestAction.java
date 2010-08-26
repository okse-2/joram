/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.command.queue;

import com.scalagent.appli.server.command.queue.ClearWaitingRequestActionImpl;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;


/**
 * This action asks for devices list from the server.
 * @author Florian Gimbert
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
