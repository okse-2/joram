/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.command.topic;

import com.scalagent.appli.server.command.topic.SendNewTopicActionImpl;
import com.scalagent.appli.shared.TopicWTO;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;


/**
 * This action asks for devices list from the server.
 */
@CalledMethod(value=SendNewTopicActionImpl.class)
public class SendNewTopicAction implements Action<SendNewTopicResponse> {
	
	private TopicWTO topic;

	public SendNewTopicAction() {}
	
	public SendNewTopicAction(TopicWTO topic) {
		this.topic = topic;
	}
	
	public TopicWTO getTopic() {
		return topic;
	}
	
}
