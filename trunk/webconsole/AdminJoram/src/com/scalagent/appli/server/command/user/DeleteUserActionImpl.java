/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.server.command.user;


import com.scalagent.appli.client.command.user.DeleteUserAction;
import com.scalagent.appli.client.command.user.DeleteUserResponse;
import com.scalagent.appli.server.RPCServiceCache;
import com.scalagent.engine.server.BaseRPCService;
import com.scalagent.engine.server.command.ActionImpl;

/**
 * @author Yohann CINTRE
 */
public class DeleteUserActionImpl extends ActionImpl<DeleteUserResponse, DeleteUserAction, RPCServiceCache>{


	@Override
	public DeleteUserResponse execute(RPCServiceCache cache, DeleteUserAction action) {
		boolean result = cache.deleteUser(action.getUserName());

		String info = new String();
		
		if (result) {
			info = "The User \""+action.getUserName()+"\" has been deleted.";
		}
		else {
			info = BaseRPCService.getString("Error while deleting User: \""+action.getUserName()+"\" not found.");
		}
		
		return new DeleteUserResponse(result, info);
	}


}
