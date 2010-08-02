/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.event.user;

import com.google.gwt.event.shared.EventHandler;
import com.scalagent.appli.shared.UserWTO;

/**
 * @author Yohann CINTRE
 */
public interface NewUserHandler extends EventHandler {

	public void onNewUser(UserWTO user);
	
}
