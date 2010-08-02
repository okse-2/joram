/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.server.command.info;

import java.util.Vector;

import com.scalagent.appli.client.command.info.LoadServerInfoAction;
import com.scalagent.appli.client.command.info.LoadServerInfoResponse;
import com.scalagent.appli.server.RPCServiceCache;
import com.scalagent.engine.server.command.ActionImpl;

/**
 * @author Yohann CINTRE
 */
public class LoadServerInfoActionImpl 
extends ActionImpl<LoadServerInfoResponse, LoadServerInfoAction, RPCServiceCache> {

	@Override
	public LoadServerInfoResponse execute(RPCServiceCache cache, LoadServerInfoAction action) {

		Vector<Float> infos = cache.getInfos(action.isforceUpdate());
		return new LoadServerInfoResponse(infos);
	}
}