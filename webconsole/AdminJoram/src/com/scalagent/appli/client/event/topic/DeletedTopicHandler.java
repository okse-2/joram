/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.event.topic;

import com.google.gwt.event.shared.EventHandler;
import com.scalagent.appli.shared.TopicWTO;


public interface DeletedTopicHandler extends EventHandler {

	public void onTopicDeleted(TopicWTO device);
	
}
