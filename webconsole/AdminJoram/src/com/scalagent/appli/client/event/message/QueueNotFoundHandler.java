/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.event.message;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Yohann CINTRE
 */
public interface QueueNotFoundHandler extends EventHandler {

	public void onQueueNotFound(String queueName);
	
}
