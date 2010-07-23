/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.server.command.user;

import com.scalagent.appli.client.command.user.SendNewUserAction;
import com.scalagent.appli.client.command.user.SendNewUserResponse;
import com.scalagent.appli.server.RPCServiceCache;
import com.scalagent.engine.server.command.ActionImpl;

public class SendNewUserActionImpl extends ActionImpl<SendNewUserResponse, SendNewUserAction, RPCServiceCache>{

	@Override
	public SendNewUserResponse execute(RPCServiceCache cache, SendNewUserAction action) {
		
		boolean result = cache.createNewUser(action.getUser());

		String info = new String();
		
		if (result) {
			info = "The User \""+action.getUser().getName()+"\" has been created.";
		}
		else {
			info = "Error while creating new User \""+action.getUser().getName()+"\"";
		}
		
		return new SendNewUserResponse(result, info);
	}
}
