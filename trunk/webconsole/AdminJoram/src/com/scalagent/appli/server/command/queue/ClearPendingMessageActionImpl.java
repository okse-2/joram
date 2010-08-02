/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.server.command.queue;


import com.scalagent.appli.client.command.queue.ClearPendingMessageAction;
import com.scalagent.appli.client.command.queue.ClearPendingMessageResponse;
import com.scalagent.appli.server.RPCServiceCache;
import com.scalagent.engine.server.BaseRPCService;
import com.scalagent.engine.server.command.ActionImpl;

/**
 * @author Yohann CINTRE
 */
public class ClearPendingMessageActionImpl extends ActionImpl<ClearPendingMessageResponse, ClearPendingMessageAction, RPCServiceCache>{


	@Override
	public ClearPendingMessageResponse execute(RPCServiceCache cache, ClearPendingMessageAction clearPendingMessageAction) {
		boolean result = cache.cleanPendingMessage(clearPendingMessageAction.getQueueName());

		String info = "";

		if (!result) {
			info = BaseRPCService.getString("Error while clearing pending messages : Queue \""+clearPendingMessageAction.getQueueName()+"\" not found.");
		}

		return new ClearPendingMessageResponse(result, info);
	}
}
