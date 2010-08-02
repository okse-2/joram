/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.event.session;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Yohann CINTRE
 */
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
