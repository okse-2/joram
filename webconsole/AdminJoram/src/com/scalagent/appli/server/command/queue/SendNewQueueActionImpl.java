/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.server.command.queue;

import com.scalagent.appli.client.command.queue.SendNewQueueAction;
import com.scalagent.appli.client.command.queue.SendNewQueueResponse;
import com.scalagent.appli.server.RPCServiceCache;
import com.scalagent.engine.server.command.ActionImpl;

/**
 * @author Yohann CINTRE
 */
public class SendNewQueueActionImpl extends ActionImpl<SendNewQueueResponse, SendNewQueueAction, RPCServiceCache>{

	@Override
	public SendNewQueueResponse execute(RPCServiceCache cache, SendNewQueueAction sendNewQueueAction) {
		
		boolean result = cache.createNewQueue(sendNewQueueAction.getQueue());

		String info = "";
		
		if (result) {
			info = "The Queue \""+sendNewQueueAction.getQueue().getName()+"\" has been created.";
		}
		else {
			info = "Error while creating new Queue \""+sendNewQueueAction.getQueue().getName()+"\"";
		}
		
		return new SendNewQueueResponse(result, info);
	}
}
