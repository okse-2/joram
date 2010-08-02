/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.widget;

import com.scalagent.appli.client.Application;
import com.google.gwt.user.client.ui.Widget;
import com.scalagent.appli.client.presenter.LoginPresenter;
import com.scalagent.appli.client.widget.handler.LoginClickHandler;
import com.scalagent.engine.client.widget.BaseWidget;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.ButtonItem;
import com.smartgwt.client.widgets.form.fields.PasswordItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.layout.HLayout;

/**
 * @author Yohann CINTRE
 */
public class LoginWidget extends BaseWidget<LoginPresenter> {

	public LoginWidget(LoginPresenter loginPresenter) {
		super(loginPresenter);
	}

	@Override
	public Widget asWidget() {

		HLayout mainLayout = new HLayout();  
		mainLayout.setWidth100();
		mainLayout.setHeight100();
		mainLayout.setAlign(Alignment.CENTER);
		mainLayout.setAlign(VerticalAlignment.CENTER);

		DynamicForm form = new DynamicForm();  
		form.setWidth(150);  
		form.setHeight(500);  
		form.setShowEdges(false);
		form.setAlign(Alignment.CENTER);
		form.setPadding(30);

		TextItem username = new TextItem();  
		username.setName("username");  
		username.setTitle(Application.messages.loginWidget_usernameField_title());  
		username.setRequired(true);  
		username.setWidth(150);
		username.setAlign(Alignment.CENTER);

		PasswordItem password = new PasswordItem();  
		password.setName("password");  
		password.setTitle(Application.messages.loginWidget_passwordField_title()); 
		password.setRequired(true);
		password.setWidth(150);
		password.setAlign(Alignment.CENTER);

		ButtonItem loginButton = new ButtonItem();  
		loginButton.setName("Login");  
		loginButton.setTitle(Application.messages.loginWidget_loginButton_title());
		loginButton.setIcon("login.png");
		loginButton.setWidth(100);
		loginButton.setAlign(Alignment.CENTER);
		loginButton.addClickHandler(new LoginClickHandler(presenter));
		loginButton.setColSpan(2);

		form.setFields(username, password, loginButton);

		mainLayout.addMember(form);

		return mainLayout;
	}
}
