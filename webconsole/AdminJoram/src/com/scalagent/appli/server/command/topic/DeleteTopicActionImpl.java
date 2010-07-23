/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.server.command.topic;


import com.scalagent.appli.client.command.topic.DeleteTopicAction;
import com.scalagent.appli.client.command.topic.DeleteTopicResponse;
import com.scalagent.appli.server.RPCServiceCache;
import com.scalagent.engine.server.command.ActionImpl;


public class DeleteTopicActionImpl extends ActionImpl<DeleteTopicResponse, DeleteTopicAction, RPCServiceCache>{


	@Override
	public DeleteTopicResponse execute(RPCServiceCache cache, DeleteTopicAction action) {
		boolean result = cache.deleteTopic(action.getTopicName());

		String info = "";

		if (result) {
			info = "The topic \""+action.getTopicName()+"\" has been deleted.";
		}
		else {
			info = "Error while deleting topic \""+action.getTopicName()+"\"";
		}

		return new DeleteTopicResponse(result, info);
	}
}
