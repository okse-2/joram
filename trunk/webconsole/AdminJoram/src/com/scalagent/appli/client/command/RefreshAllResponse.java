/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.command;

import com.scalagent.engine.client.command.Response;


/**
 * Response to the action GetDevicesAction.
 */
public class RefreshAllResponse implements Response {

	private boolean success;
	private String message;
	
	public RefreshAllResponse() {}
	
	public RefreshAllResponse(boolean success, String message) {
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
