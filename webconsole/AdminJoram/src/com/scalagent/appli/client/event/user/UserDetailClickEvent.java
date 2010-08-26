/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.event.user;

import com.google.gwt.event.shared.GwtEvent;
import com.scalagent.appli.shared.UserWTO;

public class UserDetailClickEvent extends GwtEvent<UserDetailClickHandler> {

	public static Type<UserDetailClickHandler> TYPE = new Type<UserDetailClickHandler>();
	private UserWTO user;
	
	public UserDetailClickEvent(UserWTO user) {
		this.user = user;
	}
	@Override
	public final Type<UserDetailClickHandler> getAssociatedType() {
		return TYPE;
	}
	@Override
	public void dispatch(UserDetailClickHandler handler) {
		handler.onUserDetailsClick(user);
	}

}
