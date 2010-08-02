/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.server.command.message;

import com.scalagent.appli.client.command.message.SendNewMessageAction;
import com.scalagent.appli.client.command.message.SendNewMessageResponse;
import com.scalagent.appli.server.RPCServiceCache;
import com.scalagent.engine.server.command.ActionImpl;

/**
 * @author Yohann CINTRE
 */
public class SendNewMessageActionImpl extends ActionImpl<SendNewMessageResponse, SendNewMessageAction, RPCServiceCache>{

	@Override
	public SendNewMessageResponse execute(RPCServiceCache cache, SendNewMessageAction action) {
		
		boolean result = cache.createNewMessage(action.getMessage(), action.getQueueName());

		String info = new String();
		
		if (result) {
			info = "The message \""+action.getMessage().getIdS()+"\" has been created on "+action.getQueueName();
		}
		else {
			info = "Error while creating new message \""+action.getMessage().getIdS()+"\" on "+action.getQueueName()+"";
		}
		
		return new SendNewMessageResponse(result, info);
	}
}
