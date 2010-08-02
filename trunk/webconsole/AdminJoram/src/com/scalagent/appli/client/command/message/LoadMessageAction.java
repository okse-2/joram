/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.command.message;

import com.scalagent.appli.server.command.message.LoadMessageActionImpl;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;


/**
 * This action asks for Message list from the server.
 * 
 * @author Yohann CINTRE
 */
@CalledMethod(value=LoadMessageActionImpl.class)
public class LoadMessageAction implements Action<LoadMessageResponse> {
	
	private String queueName;

	public LoadMessageAction() {}
	
	public LoadMessageAction(String queueName) {
		this.queueName = queueName;
	}
	
	public String getQueueName() {
		return queueName;
	}
	
}
