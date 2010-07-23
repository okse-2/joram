/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.command.message;

import com.scalagent.appli.server.command.message.SendNewMessageActionImpl;
import com.scalagent.appli.shared.MessageWTO;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;


/**
 * This action asks for devices list from the server.
 */
@CalledMethod(value=SendNewMessageActionImpl.class)
public class SendNewMessageAction implements Action<SendNewMessageResponse> {
	
	private MessageWTO message;
	private String queueName;

	public SendNewMessageAction() {}
	
	public SendNewMessageAction(MessageWTO message, String queueName) {
		this.message = message;
		this.queueName = queueName;
	}
	
	public MessageWTO getMessage() {
		return message;
	}
	
	public String getQueueName() {
		return queueName;
	}
	
}
