/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.command.topic;

import com.scalagent.appli.server.command.topic.SendEditedTopicActionImpl;
import com.scalagent.appli.shared.TopicWTO;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;


/**
 * This action asks for devices list from the server.
 */
@CalledMethod(value=SendEditedTopicActionImpl.class)
public class SendEditedTopicAction implements Action<SendEditedTopicResponse> {
	
	private TopicWTO topic;

	public SendEditedTopicAction() {}
	
	public SendEditedTopicAction(TopicWTO topic) {
		this.topic = topic;
	}
	
	public TopicWTO getTopic() {
		return topic;
	}
	
}
