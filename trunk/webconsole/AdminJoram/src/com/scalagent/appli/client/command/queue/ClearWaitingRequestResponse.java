/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.command.queue;

import com.scalagent.engine.client.command.Response;

/**
 * Response to the action ClearWaitingRequestAction.
 * 
 * @author Yohann CINTRE
 */
public class ClearWaitingRequestResponse implements Response {

	private boolean success;
	private String message;
	
	public ClearWaitingRequestResponse() {}
	
	public ClearWaitingRequestResponse(boolean success, String message) {
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
