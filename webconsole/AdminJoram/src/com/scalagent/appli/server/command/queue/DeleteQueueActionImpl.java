/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.server.command.queue;


import com.scalagent.appli.client.command.queue.DeleteQueueAction;
import com.scalagent.appli.client.command.queue.DeleteQueueResponse;
import com.scalagent.appli.server.RPCServiceCache;
import com.scalagent.engine.server.command.ActionImpl;


public class DeleteQueueActionImpl extends ActionImpl<DeleteQueueResponse, DeleteQueueAction, RPCServiceCache>{


	@Override
	public DeleteQueueResponse execute(RPCServiceCache cache, DeleteQueueAction deleteQueueAction) {
		boolean result = cache.deleteQueue(deleteQueueAction.getQueueName());

		String info = "";

		if (result) {
			info = "The Queue \""+deleteQueueAction.getQueueName()+"\" has been deleted.";
		}
		else {
			info = "Error while deleting Queue: \""+deleteQueueAction.getQueueName()+"\" not found.";
		}

		return new DeleteQueueResponse(result, info);
	}


}
