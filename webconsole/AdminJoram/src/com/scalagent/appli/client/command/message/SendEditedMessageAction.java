/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.command.message;

import com.scalagent.appli.server.command.message.SendEditedMessageActionImpl;
import com.scalagent.appli.shared.MessageWTO;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;


/**
 * This action send a updated message to the server.
 * 
 * @author Yohann CINTRE
 */
@CalledMethod(value=SendEditedMessageActionImpl.class)
public class SendEditedMessageAction implements Action<SendEditedMessageResponse> {

	private MessageWTO message;
	private String queueName;
	public SendEditedMessageAction() {}

	public SendEditedMessageAction(MessageWTO message, String queueName) {
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
