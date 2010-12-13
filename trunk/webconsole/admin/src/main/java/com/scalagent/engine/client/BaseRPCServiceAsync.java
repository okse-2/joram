package com.scalagent.engine.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.Response;


public interface BaseRPCServiceAsync {
	
	public <R extends Response> void execute(Action<R> action, AsyncCallback<R> callback);
	
}
