/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.scalagent.engine.client.BaseRPCService;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.Response;


/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("RPCService")
public interface RPCService extends BaseRPCService {
	
	public <R extends Response> R execute(Action<R> action);
	
}
