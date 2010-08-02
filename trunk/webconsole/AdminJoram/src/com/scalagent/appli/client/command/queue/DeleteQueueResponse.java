/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.command.queue;

import com.scalagent.engine.client.command.Response;

/**
 * Response to the action DeleteQueueAction.
 * 
 * @author Yohann CINTRE
 */
public class DeleteQueueResponse implements Response {

	private boolean success;
	private String message;
	
	public DeleteQueueResponse() {}
	
	public DeleteQueueResponse(boolean success, String message) {
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
