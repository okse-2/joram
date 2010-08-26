/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.scalagent.engine.client.BaseRPCServiceAsync;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.Response;


public interface RPCServiceAsync extends BaseRPCServiceAsync {
	
	public <R extends Response> void execute(Action<R> action, AsyncCallback<R> callback);
	
}
