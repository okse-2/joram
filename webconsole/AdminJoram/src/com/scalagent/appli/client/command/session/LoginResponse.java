/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.command.session;

import com.scalagent.engine.client.command.Response;


/**
 * Response to the action LoginAction.
 * 
 * @author Yohann CINTRE
 */
public class LoginResponse implements Response {

	private boolean success;
	private String message;
	
	public LoginResponse() {}
	
	public LoginResponse(boolean success, String message) {
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
