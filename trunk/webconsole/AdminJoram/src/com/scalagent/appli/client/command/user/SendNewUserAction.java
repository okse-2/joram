/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.command.user;

import com.scalagent.appli.server.command.user.SendNewUserActionImpl;
import com.scalagent.appli.shared.UserWTO;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;


/**
 * This action send a new user to the server.
 * 
 * @author Yohann CINTRE
 */
@CalledMethod(value=SendNewUserActionImpl.class)
public class SendNewUserAction implements Action<SendNewUserResponse> {
	
	private UserWTO user;

	public SendNewUserAction() {}
	
	public SendNewUserAction(UserWTO user) {
		this.user = user;
	}
	
	public UserWTO getUser() {
		return user;
	}
	
}
