/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.command.message;

import com.scalagent.appli.server.command.message.DeleteMessageActionImpl;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;


/**
 * This action asks for devices list from the server.
 * @author Florian Gimbert
 */
@CalledMethod(value=DeleteMessageActionImpl.class)
public class DeleteMessageAction implements Action<DeleteMessageResponse> {
	
	private String messageName;
	private String queueName;

	public DeleteMessageAction() {}
	
	public DeleteMessageAction(String messageName, String queueName) {
		this.messageName = messageName;
		this.queueName = queueName;
	}
	
	public String getMessageName() {
		return messageName;
	}
	
	public String getQueueName() {
		return queueName;
	}
	
}
