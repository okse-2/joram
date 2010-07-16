/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.event.message;

import com.google.gwt.event.shared.EventHandler;


public interface QueueNotFoundHandler extends EventHandler {

	public void onQueueNotFound(String queueName);
	
}
