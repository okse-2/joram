/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.command.user;

import com.scalagent.appli.server.command.user.SendEditedUserActionImpl;
import com.scalagent.appli.shared.UserWTO;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;


/**
 * This action send a updated user to the server.
 * 
 * @author Yohann CINTRE
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
