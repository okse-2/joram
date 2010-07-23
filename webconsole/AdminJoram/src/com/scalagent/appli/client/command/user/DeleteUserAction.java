/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.command.user;

import com.scalagent.appli.server.command.user.DeleteUserActionImpl;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;


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
