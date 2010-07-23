/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.command.user;

import com.scalagent.engine.client.command.Response;

public class SendNewUserResponse implements Response {

	private boolean success;
	private String message;
	
	public SendNewUserResponse() {}
	
	public SendNewUserResponse(boolean success, String message) {
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
