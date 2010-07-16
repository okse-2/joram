/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.event.queue;

import com.google.gwt.event.shared.EventHandler;
import com.scalagent.appli.shared.QueueWTO;


public interface UpdatedQueueHandler extends EventHandler {

	public void onQueueUpdated(QueueWTO queue);
	
}
