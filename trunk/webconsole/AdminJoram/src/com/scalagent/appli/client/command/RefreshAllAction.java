/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.command;

import com.scalagent.appli.server.command.RefreshAllActionImpl;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;


/**
 * This action asks for devices list from the server.
 */
@CalledMethod(value=RefreshAllActionImpl.class)
public class RefreshAllAction implements Action<RefreshAllResponse> {
	

	public RefreshAllAction() {}
	
	
}
