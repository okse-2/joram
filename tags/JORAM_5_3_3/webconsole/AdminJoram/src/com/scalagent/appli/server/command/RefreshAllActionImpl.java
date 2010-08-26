/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.server.command;


import com.scalagent.appli.client.command.RefreshAllResponse;
import com.scalagent.appli.client.command.queue.DeleteQueueAction;
import com.scalagent.appli.server.RPCServiceCache;
import com.scalagent.engine.server.BaseRPCService;
import com.scalagent.engine.server.command.ActionImpl;


public class RefreshAllActionImpl extends ActionImpl<RefreshAllResponse, DeleteQueueAction, RPCServiceCache>{


	@Override
	public RefreshAllResponse execute(RPCServiceCache cache, DeleteQueueAction deleteQueueAction) {

		boolean result=true;
		String info = "Queue Deleted";
		if (!result) {
			info = BaseRPCService.getString("Queue Deleted");
		}
		return new RefreshAllResponse(result, info);

	}


}
