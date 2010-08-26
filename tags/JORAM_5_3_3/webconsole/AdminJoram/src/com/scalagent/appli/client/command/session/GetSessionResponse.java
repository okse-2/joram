/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.command.session;

import com.scalagent.engine.client.command.Response;


/**
 * Response to the action GetDevicesAction.
 */
public class GetSessionResponse implements Response {

	private String userName;
	private String userFirstname;
	private String userLogin;
	
	public String getLogin() {
		return userLogin;
	}
	
	public void setLogin(String userLogin) {
		this.userLogin = userLogin;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getUserFirstname() {
		return userFirstname;
	}
	
	public void setUserFirstname(String userFirstname) {
		this.userFirstname = userFirstname;
	}
	
}
