package com.scalagent.engine.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class SystemErrorEvent extends GwtEvent<SystemErrorHandler> {

	private Throwable throwable;
	public static Type<SystemErrorHandler> TYPE = new Type<SystemErrorHandler>();
	
	public SystemErrorEvent(Throwable throwable) {
		this.throwable = throwable;
	}
	
	@Override
	public void dispatch(SystemErrorHandler handler) {
			handler.onSystemError(throwable);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<SystemErrorHandler> getAssociatedType() {
		return TYPE;
	}

	
}
