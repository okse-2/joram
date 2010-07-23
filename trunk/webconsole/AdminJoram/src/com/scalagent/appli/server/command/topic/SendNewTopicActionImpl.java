/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.server.command.topic;

import com.scalagent.appli.client.command.topic.SendNewTopicAction;
import com.scalagent.appli.client.command.topic.SendNewTopicResponse;
import com.scalagent.appli.server.RPCServiceCache;
import com.scalagent.engine.server.command.ActionImpl;

public class SendNewTopicActionImpl extends ActionImpl<SendNewTopicResponse, SendNewTopicAction, RPCServiceCache>{

	@Override
	public SendNewTopicResponse execute(RPCServiceCache cache, SendNewTopicAction action) {
		
		boolean result = cache.createNewTopic(action.getTopic());

		String info = "";
		
		if (result) {
			info = "The topic \""+action.getTopic().getName()+"\" has been created.";
		}
		else {
			info = "Error while creating new topic \""+action.getTopic().getName()+"\"";
		}
		
		return new SendNewTopicResponse(result, info);
	}
}
