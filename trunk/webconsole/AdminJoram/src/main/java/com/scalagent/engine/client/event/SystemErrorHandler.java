package com.scalagent.engine.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface SystemErrorHandler extends EventHandler {

	public void onSystemError(Throwable throwable);
	
}
