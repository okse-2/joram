/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.command.queue;

import com.scalagent.appli.server.command.queue.SendEditedQueueActionImpl;
import com.scalagent.appli.shared.QueueWTO;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;


/**
 * This action asks for devices list from the server.
 */
@CalledMethod(value=SendEditedQueueActionImpl.class)
public class SendEditedQueueAction implements Action<SendEditedQueueResponse> {
	
	private QueueWTO queue;

	public SendEditedQueueAction() {}
	
	public SendEditedQueueAction(QueueWTO queue) {
		this.queue = queue;
	}
	
	public QueueWTO getQueue() {
		return queue;
	}
	
}
