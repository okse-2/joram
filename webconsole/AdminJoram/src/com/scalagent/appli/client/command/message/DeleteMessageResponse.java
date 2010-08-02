/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.command.message;

import com.scalagent.engine.client.command.Response;


/**
 * Response to the action DeleteMessageAction.
 * 
 * @author Yohann CINTRE
 */
public class DeleteMessageResponse implements Response {

	private boolean success;
	private String message;
	
	public DeleteMessageResponse() {}
	
	public DeleteMessageResponse(boolean success, String message) {
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
