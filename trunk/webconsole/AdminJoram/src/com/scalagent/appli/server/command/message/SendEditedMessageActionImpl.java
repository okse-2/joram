/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.server.command.message;

import com.scalagent.appli.client.command.message.SendEditedMessageAction;
import com.scalagent.appli.client.command.message.SendEditedMessageResponse;
import com.scalagent.appli.server.RPCServiceCache;
import com.scalagent.engine.server.command.ActionImpl;

public class SendEditedMessageActionImpl extends ActionImpl<SendEditedMessageResponse, SendEditedMessageAction, RPCServiceCache>{

	@Override
	public SendEditedMessageResponse execute(RPCServiceCache cache, SendEditedMessageAction action) {

		boolean result = cache.editMessage(action.getMessage(), action.getQueueName());

		String info = new String();

		if (result) {
			info = "The message \""+action.getMessage().getIdS()+"\" has been updated on "+action.getQueueName();
		}
		else {
			info = "Error while updating message \""+action.getMessage().getIdS()+"\" on "+action.getQueueName()+"";
		}	

		return new SendEditedMessageResponse(result, info);
	}
}
