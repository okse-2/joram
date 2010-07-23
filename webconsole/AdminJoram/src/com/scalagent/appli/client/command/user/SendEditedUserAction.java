/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.command.user;

import com.scalagent.appli.server.command.user.SendEditedUserActionImpl;
import com.scalagent.appli.shared.UserWTO;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;


/**
 * This action asks for devices list from the server.
 */
@CalledMethod(value=SendEditedUserActionImpl.class)
public class SendEditedUserAction implements Action<SendEditedUserResponse> {
	
	private UserWTO user;

	public SendEditedUserAction() {}
	
	public SendEditedUserAction(UserWTO user) {
		this.user = user;
	}
	
	public UserWTO getUser() {
		return user;
	}
	
}
