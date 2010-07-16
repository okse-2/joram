/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.command.queue;

import com.scalagent.engine.client.command.Response;


/**
 * Response to the action GetDevicesAction.
 * @author Florian Gimbert
 */
public class ClearPendingMessageResponse implements Response {

	private boolean success;
	private String message;
	
	public ClearPendingMessageResponse() {}
	
	public ClearPendingMessageResponse(boolean success, String message) {
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
