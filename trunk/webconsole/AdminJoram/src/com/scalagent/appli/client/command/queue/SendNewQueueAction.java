/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.command.queue;

import com.scalagent.appli.server.command.queue.SendNewQueueActionImpl;
import com.scalagent.appli.shared.QueueWTO;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;


/**
 * This action asks for devices list from the server.
 */
@CalledMethod(value=SendNewQueueActionImpl.class)
public class SendNewQueueAction implements Action<SendNewQueueResponse> {
	
	private QueueWTO queue;

	public SendNewQueueAction() {}
	
	public SendNewQueueAction(QueueWTO queue) {
		this.queue = queue;
	}
	
	public QueueWTO getQueue() {
		return queue;
	}
	
}
