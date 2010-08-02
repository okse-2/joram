/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.command.info;

import com.scalagent.appli.server.command.info.LoadServerInfoActionImpl;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;

/**
 * This action load the server info (engine and network utilization) from the server
 * @author Yohann CINTRE
 */
@CalledMethod(value=LoadServerInfoActionImpl.class)
public class LoadServerInfoAction implements Action<LoadServerInfoResponse>{

	private boolean forceUpdate;

	public LoadServerInfoAction(){}

	public LoadServerInfoAction(boolean forceUpdate){
		this.forceUpdate = forceUpdate;
	}

	public boolean isforceUpdate() {
		return forceUpdate;
	}
}
