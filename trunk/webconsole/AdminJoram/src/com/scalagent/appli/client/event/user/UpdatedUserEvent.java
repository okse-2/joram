/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.event.user;

import com.google.gwt.event.shared.GwtEvent;
import com.scalagent.appli.shared.UserWTO;

/**
 * @author Yohann CINTRE
 */
public class UpdatedUserEvent extends GwtEvent<UpdatedUserHandler> {

	public static Type<UpdatedUserHandler> TYPE = new Type<UpdatedUserHandler>();
	private UserWTO user;
	
	public UpdatedUserEvent(UserWTO user) {
		this.user = user;
	}
	@Override
	public final Type<UpdatedUserHandler> getAssociatedType() {
		return TYPE;
	}
	@Override
	public void dispatch(UpdatedUserHandler handler) {
		handler.onUserUpdated(user);
	}

}
