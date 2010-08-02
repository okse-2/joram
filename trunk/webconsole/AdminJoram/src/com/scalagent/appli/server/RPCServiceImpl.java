/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.server;

import com.scalagent.appli.client.RPCService;
import com.scalagent.engine.server.BaseRPCServiceImpl;

/**
 * The server side implementation of the RPC service.
 * 
 * @author Yohann CINTRE
 */
public class RPCServiceImpl extends BaseRPCServiceImpl implements RPCService {
	private static final long serialVersionUID = 3858814321088436484L;

	public RPCServiceImpl() {
		cache = new RPCServiceCache();
	}
}
