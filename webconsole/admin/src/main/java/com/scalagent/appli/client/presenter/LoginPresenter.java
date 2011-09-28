/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 - 2011 ScalAgent Distributed Technologies
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
package com.scalagent.appli.client.presenter;

import com.google.gwt.event.shared.SimpleEventBus;
import com.scalagent.appli.client.RPCServiceCacheClient;
import com.scalagent.appli.client.command.session.LoginAction;
import com.scalagent.appli.client.command.session.LoginResponse;
import com.scalagent.appli.client.event.session.LoginValidEvent;
import com.scalagent.appli.client.widget.LoginWidget;
import com.scalagent.engine.client.BaseRPCServiceAsync;
import com.scalagent.engine.client.command.Handler;
import com.scalagent.engine.client.presenter.BasePresenter;
import com.smartgwt.client.util.SC;

/**
 * This class is the presenter associated to the login screen.
 * Its widget is LoginWidget.
 * 
 * @author Yohann CINTRE
 */
public class LoginPresenter extends BasePresenter<LoginWidget, BaseRPCServiceAsync, RPCServiceCacheClient> {

  public LoginPresenter(BaseRPCServiceAsync testService, SimpleEventBus eventBus, RPCServiceCacheClient cache) {
    super(testService, cache, eventBus);
    this.widget = new LoginWidget(this);
  }

  /**
   * This method is called by the LoginClickHandler when a new the user click on
   * the
   * login button on the LoginWidget
   * It send the login information to the server and wait for the response
   */
  public void sendLogin(String login, String password) {
    service.execute(new LoginAction(login, password), new Handler<LoginResponse>(eventBus) {
      @Override
      public void onSuccess(LoginResponse response) {
        if (response.isSuccess()) {
          eventBus.fireEvent(new LoginValidEvent());
        } else {
          SC.warn(response.getMessage());
        }
      }
    });
  }
}
