/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.event.user;

import com.google.gwt.event.shared.GwtEvent;
import com.scalagent.appli.shared.UserWTO;

/**
 * @author Yohann CINTRE
 */
public class DeletedUserEvent extends GwtEvent<DeletedUserHandler> {

	public static Type<DeletedUserHandler> TYPE = new Type<DeletedUserHandler>();
	private UserWTO user;
	
	public DeletedUserEvent(UserWTO user) {
		this.user = user;
	}
	@Override
	public final Type<DeletedUserHandler> getAssociatedType() {
		return TYPE;
	}
	@Override
	public void dispatch(DeletedUserHandler handler) {
		handler.onUserDeleted(user);
	}

}
