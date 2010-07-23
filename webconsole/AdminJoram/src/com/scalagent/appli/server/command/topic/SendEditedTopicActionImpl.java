/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.server.command.topic;

import com.scalagent.appli.client.command.topic.SendEditedTopicAction;
import com.scalagent.appli.client.command.topic.SendEditedTopicResponse;
import com.scalagent.appli.server.RPCServiceCache;
import com.scalagent.engine.server.command.ActionImpl;

public class SendEditedTopicActionImpl extends ActionImpl<SendEditedTopicResponse, SendEditedTopicAction, RPCServiceCache>{

	@Override
	public SendEditedTopicResponse execute(RPCServiceCache cache, SendEditedTopicAction action) {
		
		boolean result = cache.editTopic(action.getTopic());

		String info = "";
		
		if (result) {
			info = "The topic \""+action.getTopic().getName()+"\" has been updated.";
		}
		else {
			info = "Error while updating topic \""+action.getTopic().getName()+"\".";
		}
		
		return new SendEditedTopicResponse(result, info);
	}
}
