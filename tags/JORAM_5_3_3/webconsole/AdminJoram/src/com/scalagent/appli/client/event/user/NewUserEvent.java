/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.event.user;

import com.google.gwt.event.shared.GwtEvent;
import com.scalagent.appli.shared.UserWTO;

public class NewUserEvent extends GwtEvent<NewUserHandler> {

	public static Type<NewUserHandler> TYPE = new Type<NewUserHandler>();
	private UserWTO user;
	
	public NewUserEvent(UserWTO user) {
		this.user = user;
	}
	@Override
	public final Type<NewUserHandler> getAssociatedType() {
		return TYPE;
	}
	@Override
	public void dispatch(NewUserHandler handler) {
		handler.onNewUser(user);
	}

}
