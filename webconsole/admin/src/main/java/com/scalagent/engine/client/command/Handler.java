package com.scalagent.engine.client.command;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.scalagent.engine.client.event.SystemErrorEvent;

public class Handler<R extends Response> implements AsyncCallback<R> {

	private SimpleEventBus eventBus;
	
//	public Handler() {}
	
	public Handler(SimpleEventBus eventBus) {
		this.eventBus = eventBus;
	}
	
	public void onFailure(Throwable caught) {
		eventBus.fireEvent(new SystemErrorEvent(caught));
	}

	public void onSuccess(R result) {};

}
