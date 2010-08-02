/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.server.command.queue;

import com.scalagent.appli.client.command.queue.SendEditedQueueAction;
import com.scalagent.appli.client.command.queue.SendEditedQueueResponse;
import com.scalagent.appli.server.RPCServiceCache;
import com.scalagent.engine.server.command.ActionImpl;

/**
 * @author Yohann CINTRE
 */
public class SendEditedQueueActionImpl extends ActionImpl<SendEditedQueueResponse, SendEditedQueueAction, RPCServiceCache>{

	@Override
	public SendEditedQueueResponse execute(RPCServiceCache cache, SendEditedQueueAction sendEditedQueueAction) {
		
		boolean result = cache.editQueue(sendEditedQueueAction.getQueue());

		String info = "";
		
		if (result) {
			info = "The Queue \""+sendEditedQueueAction.getQueue().getName()+"\" has been updated.";
		}
		else {
			info = "Error while updating queue \""+sendEditedQueueAction.getQueue().getName()+"\"";
		}
		
		return new SendEditedQueueResponse(result, info);
	}
}
