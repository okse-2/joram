/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.server.command.message;

import com.scalagent.appli.client.command.message.DeleteMessageAction;
import com.scalagent.appli.client.command.message.DeleteMessageResponse;
import com.scalagent.appli.server.RPCServiceCache;
import com.scalagent.engine.server.BaseRPCService;
import com.scalagent.engine.server.command.ActionImpl;

/**
 * @author Yohann CINTRE
 */
public class DeleteMessageActionImpl extends ActionImpl<DeleteMessageResponse, DeleteMessageAction, RPCServiceCache>{


	@Override
	public DeleteMessageResponse execute(RPCServiceCache cache, DeleteMessageAction deleteMessageAction) {
		boolean result = cache.deleteMessage(deleteMessageAction.getMessageName(), deleteMessageAction.getQueueName());

		String info = "";

		if (!result) {
			info = BaseRPCService.getString("Error while deleting Message: \""+deleteMessageAction.getMessageName()+"\" not found.");
		}

		return new DeleteMessageResponse(result, info);
	}


}
