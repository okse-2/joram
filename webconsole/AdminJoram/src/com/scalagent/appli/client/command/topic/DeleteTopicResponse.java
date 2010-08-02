/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.command.topic;

import com.scalagent.engine.client.command.Response;

/**
 * Response to the action DeleteTopicAction.
 * 
 * @author Yohann CINTRE
 */
public class DeleteTopicResponse implements Response {

	private boolean success;
	private String message;
	
	public DeleteTopicResponse() {}
	
	public DeleteTopicResponse(boolean success, String message) {
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
