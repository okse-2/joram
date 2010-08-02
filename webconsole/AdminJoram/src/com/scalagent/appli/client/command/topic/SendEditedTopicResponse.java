/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.command.topic;

import com.scalagent.engine.client.command.Response;

/**
 * Response to the action SendEditedTopicAction.
 * 
 * @author Yohann CINTRE
 */
public class SendEditedTopicResponse implements Response {

	private boolean success;
	private String message;
	
	public SendEditedTopicResponse() {}
	
	public SendEditedTopicResponse(boolean success, String message) {
		this.success = success;
		this.message = message;
	}
	 
	public boolean isSuccess() {
		return success;
	}
	
	public String getMessage() {
		return message;
	}
	
}
