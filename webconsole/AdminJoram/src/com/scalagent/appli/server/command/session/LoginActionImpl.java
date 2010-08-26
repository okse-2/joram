/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.server.command.session;


import com.scalagent.appli.client.command.session.LoginAction;
import com.scalagent.appli.client.command.session.LoginResponse;
import com.scalagent.appli.server.RPCServiceCache;
import com.scalagent.engine.server.BaseRPCService;
import com.scalagent.engine.server.command.ActionImpl;


public class LoginActionImpl extends ActionImpl<LoginResponse, LoginAction, RPCServiceCache>{


	@Override
	public LoginResponse execute(RPCServiceCache cache, LoginAction loginAction) {
		boolean result = cache.connectJORAM(loginAction.getLogin(), loginAction.getPassword());

		String info = "";

		if (!result) {
			info = BaseRPCService.getString("Error while loging-in");
		}

		return new LoginResponse(result, info);
	}


}
