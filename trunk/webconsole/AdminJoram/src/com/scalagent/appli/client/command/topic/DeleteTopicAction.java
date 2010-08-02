/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.command.topic;

import com.scalagent.appli.server.command.topic.DeleteTopicActionImpl;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;

/**
 * This action delete a topic on the server.
 * 
 * @author Yohann CINTRE
 */
@CalledMethod(value=DeleteTopicActionImpl.class)
public class DeleteTopicAction implements Action<DeleteTopicResponse> {
	
	private String topicName;

	public DeleteTopicAction() {}
	
	public DeleteTopicAction(String topicName) {
		this.topicName = topicName;
	}
	
	public String getTopicName() {
		return topicName;
	}
	
}
