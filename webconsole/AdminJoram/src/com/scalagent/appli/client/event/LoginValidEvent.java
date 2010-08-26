/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class LoginValidEvent extends GwtEvent<LoginValidHandler> {

	public static Type<LoginValidHandler> TYPE = new Type<LoginValidHandler>();
	
	public LoginValidEvent() {}
	
	@Override
	public final Type<LoginValidHandler> getAssociatedType() {
		return TYPE;
	}
	@Override
	public void dispatch(LoginValidHandler handler) {
		handler.onLoginValid();
	}

}
