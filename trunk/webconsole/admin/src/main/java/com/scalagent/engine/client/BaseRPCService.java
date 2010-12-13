package com.scalagent.engine.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.Response;


/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("rpc")
public interface BaseRPCService extends RemoteService {
	
	public <R extends Response> R execute(Action<R> action);
}
