/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.widget.handler;

import com.scalagent.appli.client.presenter.LoginPresenter;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
import com.smartgwt.client.widgets.form.fields.events.ClickHandler;

/**
 * @author Yohann CINTRE
 */
public class LoginClickHandler implements ClickHandler {


	private LoginPresenter loginPresenter;
	
	
	public LoginClickHandler(LoginPresenter loginPresenter) {
		super();
		this.loginPresenter = loginPresenter;
	}
	
	@Override
	public void onClick(ClickEvent event) {
		if(event.getForm().validate()) {
		
			String login = (String) event.getForm().getField("username").getValue();
			String password = (String) event.getForm().getField("password").getValue();
	
			loginPresenter.sendLogin(login, password);
		}
		else SC.warn("You must enter a valid login and password to login");
	}
}
