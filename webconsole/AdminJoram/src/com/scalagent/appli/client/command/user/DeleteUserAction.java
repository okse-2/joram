/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.command.user;

import com.scalagent.appli.server.command.user.DeleteUserActionImpl;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;

/**
 * This action delete a user on the server.
 * 
 * @author Yohann CINTRE
 */
@CalledMethod(value=DeleteUserActionImpl.class)
public class DeleteUserAction implements Action<DeleteUserResponse> {
	
	private String userName;

	public DeleteUserAction() {}
	
	public DeleteUserAction(String userName) {
		this.userName = userName;
	}
	
	public String getUserName() {
		return userName;
	}
	
}
