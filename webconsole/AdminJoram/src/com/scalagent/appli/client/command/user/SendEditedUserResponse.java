/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.command.user;

import com.scalagent.engine.client.command.Response;

/**
 * Response to the action SendEditedUserAction.
 * 
 * @author Yohann CINTRE
 */
public class SendEditedUserResponse implements Response {

	private boolean success;
	private String message;
	
	public SendEditedUserResponse() {}
	
	public SendEditedUserResponse(boolean success, String message) {
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
