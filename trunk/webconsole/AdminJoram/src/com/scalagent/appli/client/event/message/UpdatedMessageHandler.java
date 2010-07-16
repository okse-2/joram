/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.event.message;

import com.google.gwt.event.shared.EventHandler;
import com.scalagent.appli.shared.MessageWTO;


public interface UpdatedMessageHandler extends EventHandler {

	public void onMessageUpdated(MessageWTO message, String queueName);
	
}
