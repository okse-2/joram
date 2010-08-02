/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.event.queue;

import com.google.gwt.event.shared.EventHandler;
import com.scalagent.appli.shared.QueueWTO;

/**
 * @author Yohann CINTRE
 */
public interface DeletedQueueHandler extends EventHandler {

	public void onQueueDeleted(QueueWTO queue);
	
}
