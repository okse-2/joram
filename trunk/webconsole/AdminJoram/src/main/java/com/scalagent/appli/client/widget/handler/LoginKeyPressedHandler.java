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
package com.scalagent.appli.client.widget.handler;

import com.scalagent.appli.client.presenter.LoginPresenter;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.events.ItemKeyPressEvent;
import com.smartgwt.client.widgets.form.events.ItemKeyPressHandler;

public class LoginKeyPressedHandler implements ItemKeyPressHandler {

  private LoginPresenter loginPresenter;
  private DynamicForm form;

  public LoginKeyPressedHandler(LoginPresenter loginPresenter, DynamicForm form) {
    super();
    this.loginPresenter = loginPresenter;
    this.form = form;
  }

  public void onItemKeyPress(ItemKeyPressEvent event) {
    if (event.getKeyName().equals("Enter")) {
      if (form.validate()) {
        String login = (String) form.getField("username").getValue();
        String password = (String) form.getField("password").getValue();
        loginPresenter.sendLogin(login, password);
      } else
        SC.warn("You must enter a valid login and password to login");
    }
  }
}
