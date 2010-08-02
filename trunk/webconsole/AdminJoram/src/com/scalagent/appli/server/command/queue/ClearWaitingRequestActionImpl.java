/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.server.command.queue;

import com.scalagent.appli.client.command.queue.ClearWaitingRequestAction;
import com.scalagent.appli.client.command.queue.ClearWaitingRequestResponse;
import com.scalagent.appli.server.RPCServiceCache;
import com.scalagent.engine.server.BaseRPCService;
import com.scalagent.engine.server.command.ActionImpl;

/**
 * @author Yohann CINTRE
 */
public class ClearWaitingRequestActionImpl extends ActionImpl<ClearWaitingRequestResponse, ClearWaitingRequestAction, RPCServiceCache>{

	@Override
	public ClearWaitingRequestResponse execute(RPCServiceCache cache, ClearWaitingRequestAction clearWaitingRequestAction) {

		boolean result = cache.cleanWaitingRequest(clearWaitingRequestAction.getQueueName());

		String info = "";
		
		if (!result) {
			info = BaseRPCService.getString("Error while clearing waiting request : Queue \""+clearWaitingRequestAction.getQueueName()+"\" not found.");
		}
		
		return new ClearWaitingRequestResponse(result, info);
	}
}
