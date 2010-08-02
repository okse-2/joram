/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.event.message;

import com.google.gwt.event.shared.EventHandler;
import com.scalagent.appli.shared.MessageWTO;

/**
 * @author Yohann CINTRE
 */
public interface UpdatedMessageHandler extends EventHandler {

	public void onMessageUpdated(MessageWTO message, String queueName);
	
}
