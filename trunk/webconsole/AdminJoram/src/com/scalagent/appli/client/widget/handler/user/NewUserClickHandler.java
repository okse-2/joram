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
package com.scalagent.appli.client.widget.handler.user;

import com.scalagent.appli.client.presenter.UserListPresenter;
import com.scalagent.appli.shared.UserWTO;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;

/**
 * @author Yohann CINTRE
 */
public class NewUserClickHandler implements ClickHandler {

  private UserListPresenter presenter;
  private DynamicForm form;

  public NewUserClickHandler(UserListPresenter presenter, DynamicForm form) {
    super();
    this.presenter = presenter;
    this.form = form;
  }

  @Override
  public void onClick(ClickEvent event) {

    try {
      if (form.validate()) {
        String nameValue = form.getValueAsString("nameItem");
        String passwordValue = form.getValueAsString("passwordItem");
        int periodValue = Integer.parseInt(form.getValueAsString("periodItem"));

        UserWTO newUser = new UserWTO(nameValue, passwordValue, periodValue, 0, null);

        presenter.createNewUser(newUser);
      }
    } catch (Exception e) {
      SC.warn("An error occured while parsing datas");
    }
  }
}
