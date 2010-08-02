/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.command.queue;

import com.scalagent.appli.server.command.queue.SendNewQueueActionImpl;
import com.scalagent.appli.shared.QueueWTO;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;


/**
 * This action send a new message to the server.
 * 
 * @author Yohann CINTRE
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
