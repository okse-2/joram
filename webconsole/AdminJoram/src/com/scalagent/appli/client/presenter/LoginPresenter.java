/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.presenter;

import com.google.gwt.event.shared.HandlerManager;
import com.scalagent.appli.client.RPCServiceAsync;
import com.scalagent.appli.client.RPCServiceCacheClient;
import com.scalagent.appli.client.command.session.LoginAction;
import com.scalagent.appli.client.command.session.LoginHandler;
import com.scalagent.appli.client.command.session.LoginResponse;
import com.scalagent.appli.client.event.session.LoginValidEvent;
import com.scalagent.appli.client.widget.LoginWidget;
import com.scalagent.engine.client.presenter.BasePresenter;
import com.smartgwt.client.util.SC;

/**
 * This class is the presenter associated to the login screen.
 * Its widget is LoginWidget.
 * 
 * @author Yohann CINTRE
 */
public class LoginPresenter extends BasePresenter<LoginWidget, RPCServiceAsync, RPCServiceCacheClient>
{
	public LoginPresenter(RPCServiceAsync testService, 
			HandlerManager eventBus,
			RPCServiceCacheClient cache) {
		super(testService, cache, eventBus);
		this.widget = new LoginWidget(this);
	}

	/**
	 * This method is called by the LoginClickHandler when a new the user click on the
	 * login button on the LoginWidget
	 * It send the login information to the server and wait for the response
	 * 
	 */	
	public void sendLogin(String login, String password) {
		service.execute(new LoginAction(login, password), new LoginHandler(eventBus) {
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
