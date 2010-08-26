package com.scalagent.engine.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class SetUserHeaderEvent extends GwtEvent<SetUserHeaderHandler> {

	public static Type<SetUserHeaderHandler> TYPE = new Type<SetUserHeaderHandler>();
	private String userFirstname;
	
	public SetUserHeaderEvent(String userFirstname) {
		this.userFirstname = userFirstname;
	}
	
	@Override
	public void dispatch(SetUserHeaderHandler handler) {
			handler.onSetUserHeader(userFirstname);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<SetUserHeaderHandler> getAssociatedType() {
		return TYPE;
	}

	
}
