/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */


package com.scalagent.appli.server.command.user;

import java.util.List;

import com.scalagent.appli.client.command.user.LoadUserAction;
import com.scalagent.appli.client.command.user.LoadUserResponse;
import com.scalagent.appli.server.RPCServiceCache;
import com.scalagent.appli.shared.UserWTO;
import com.scalagent.engine.server.command.ActionImpl;


/**
 *
 * @author sgonzalez
 */
public class LoadUserActionImpl 
extends ActionImpl<LoadUserResponse, LoadUserAction, RPCServiceCache> {

	@Override
	public LoadUserResponse execute(RPCServiceCache cache, LoadUserAction action) {

		List<UserWTO> users = cache.getUsers(this.getHttpSession(), action.isRetrieveAll(), action.isforceUpdate());
		System.out.println("### engine.server.command.queue.LoadUserActionImpl.execute : "+users.size()+" users recupérés sur le serveur");
		return new LoadUserResponse(users);
	}

}