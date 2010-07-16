/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.event.user;

import com.google.gwt.event.shared.EventHandler;
import com.scalagent.appli.shared.UserWTO;

public interface UserDetailClickHandler extends EventHandler {

	public void onUserDetailsClick(UserWTO user);
	
}
