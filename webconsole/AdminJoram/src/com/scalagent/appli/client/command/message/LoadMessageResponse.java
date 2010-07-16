/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.command.message;

import java.util.List;

import com.scalagent.appli.shared.MessageWTO;
import com.scalagent.engine.client.command.Response;


/**
 * Response to the action GetDevicesAction.
 * @author Florian Gimbert
 */
public class LoadMessageResponse implements Response {

	private List<MessageWTO> messages;
	private String queueName;
	private boolean success;
	
	public LoadMessageResponse() {}
	
	public LoadMessageResponse(List<MessageWTO> messages, String queueName, Boolean success) {
		this.messages = messages;
		this.queueName = queueName;
		this.success = success;
	}
	 
	public List<MessageWTO> getMessages() {
		return messages;
	}
	
	public String getQueueName() {
		return queueName;
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	
}
