/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.server.command.user;

import com.scalagent.appli.client.command.user.SendEditedUserAction;
import com.scalagent.appli.client.command.user.SendEditedUserResponse;
import com.scalagent.appli.server.RPCServiceCache;
import com.scalagent.engine.server.command.ActionImpl;

public class SendEditedUserActionImpl extends ActionImpl<SendEditedUserResponse, SendEditedUserAction, RPCServiceCache>{

	@Override
	public SendEditedUserResponse execute(RPCServiceCache cache, SendEditedUserAction action) {
		
		boolean result = cache.editUser(action.getUser());

		String info = new String();
		
		if (result) {
			info = "The User \""+action.getUser().getName()+"\" has been updated.";
		}
		else {
			info = "Error while updating user \""+action.getUser().getName()+"\"";
		}
		
		return new SendEditedUserResponse(result, info);
	}
}
