/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.command.user;

import com.scalagent.appli.server.command.user.SendNewUserActionImpl;
import com.scalagent.appli.shared.UserWTO;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;


/**
 * This action asks for devices list from the server.
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
