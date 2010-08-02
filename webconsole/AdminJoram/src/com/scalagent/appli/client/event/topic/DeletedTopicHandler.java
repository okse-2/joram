/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.event.topic;

import com.google.gwt.event.shared.EventHandler;
import com.scalagent.appli.shared.TopicWTO;

/**
 * @author Yohann CINTRE
 */
public interface DeletedTopicHandler extends EventHandler {

	public void onTopicDeleted(TopicWTO device);
	
}
