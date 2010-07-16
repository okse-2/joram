package com.scalagent.engine.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface SetUserHeaderHandler extends EventHandler {

	public void onSetUserHeader(String userFirstname);
	
}
