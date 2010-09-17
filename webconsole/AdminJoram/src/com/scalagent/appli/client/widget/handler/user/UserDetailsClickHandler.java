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
import com.scalagent.appli.client.widget.record.UserListRecord;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;

/**
 * @author Yohann CINTRE
 */
public class UserDetailsClickHandler implements ClickHandler {

  private UserListPresenter userListPresenter;
  private UserListRecord record;

  public UserDetailsClickHandler(UserListPresenter userListPresenter, UserListRecord record) {
    super();
    this.userListPresenter = userListPresenter;
    this.record = record;
  }

  @Override
  public void onClick(ClickEvent event) {

    userListPresenter.fireUserDetailsClick(record.getUser());
  }

}
