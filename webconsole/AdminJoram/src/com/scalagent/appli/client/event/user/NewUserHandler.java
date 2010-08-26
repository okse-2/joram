/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.event.user;

import com.google.gwt.event.shared.EventHandler;
import com.scalagent.appli.shared.UserWTO;

public interface NewUserHandler extends EventHandler {

	public void onNewUser(UserWTO user);
	
}
