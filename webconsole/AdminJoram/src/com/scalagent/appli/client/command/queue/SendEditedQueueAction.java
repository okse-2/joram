/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.command.queue;

import com.scalagent.appli.server.command.queue.SendEditedQueueActionImpl;
import com.scalagent.appli.shared.QueueWTO;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;


/**
 * This action send a updated queue to the server.
 * 
 * @author Yohann CINTRE
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
