/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 *
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package com.scalagent.appli.client.widget;



import com.google.gwt.user.client.ui.Widget;
import com.scalagent.appli.client.Application;
import com.scalagent.appli.client.presenter.LoginPresenter;
import com.scalagent.appli.client.widget.handler.LoginClickHandler;
import com.scalagent.appli.client.widget.handler.LoginKeyPressedHandler;
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
    form.setShowEdges(Boolean.FALSE);
    form.setAlign(Alignment.CENTER);
    form.setPadding(30);
    form.setAutoFocus(Boolean.TRUE);
    form.addItemKeyPressHandler(new LoginKeyPressedHandler(presenter, form));

    TextItem username = new TextItem();
    username.setName("username");
    username.setTitle(Application.messages.loginWidget_usernameField_title());
    username.setRequired(Boolean.TRUE);
    username.setWidth(150);
    username.setAlign(Alignment.CENTER);
    username.setSelectOnFocus(Boolean.TRUE);

    PasswordItem password = new PasswordItem();
    password.setName("password");
    password.setTitle(Application.messages.loginWidget_passwordField_title());
    password.setRequired(Boolean.TRUE);
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
