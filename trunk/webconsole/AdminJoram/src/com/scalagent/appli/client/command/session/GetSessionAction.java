/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.command.session;

import com.scalagent.appli.server.command.session.GetSessionActionImpl;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;

/**
 * This action is used to get session information from the server.
 * It must be called before any other requests (ideally in the EntryPoint class).
 * It will return account information, such as username, firstname...
 * 
 * Calling this method also causes the server to store the session id and timestamps on
 * which remote commands are received in order to monitor client disconnection.
 * Note that if this method is not called, idle sessions won't be detected.
 * See BaseRPCServiceImpl.
 * 
 */
@CalledMethod(value=GetSessionActionImpl.class)
public class GetSessionAction implements Action<GetSessionResponse> {
	
}
