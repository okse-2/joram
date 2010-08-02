/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.command.session;

import com.scalagent.appli.server.command.session.LoginActionImpl;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;


/**
 * This action send login information the server.
 * 
 * @author Yohann CINTRE
 */
@CalledMethod(value=LoginActionImpl.class)
public class LoginAction implements Action<LoginResponse> {

	private String login;
	private String password;

	public LoginAction() {}

	public LoginAction(String login, String password) {
		this.login = login;
		this.password = password;
	}

	public String getLogin() {
		return login;
	}
	public String getPassword() {
		return password;
	}

}
