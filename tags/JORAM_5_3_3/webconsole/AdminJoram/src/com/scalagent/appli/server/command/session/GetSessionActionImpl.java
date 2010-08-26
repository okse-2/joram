/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.server.command.session;



import com.scalagent.appli.client.command.session.GetSessionAction;
import com.scalagent.appli.client.command.session.GetSessionResponse;
import com.scalagent.engine.server.BaseRPCServiceCache;
import com.scalagent.engine.server.command.ActionImpl;


public class GetSessionActionImpl extends ActionImpl<GetSessionResponse,GetSessionAction, BaseRPCServiceCache>{



	@Override
	public GetSessionResponse execute(BaseRPCServiceCache cache, GetSessionAction setSessionAction) {
		
		System.out.println("### engine.server.command.GetSessionActionImpl.execute : Session id :"+getHttpSession().getId());
	
		// save session id in service
		getRPCService().setSession(getHttpSession().getId(), System.currentTimeMillis());
	
		
		GetSessionResponse response = new GetSessionResponse();
		response.setLogin("root");
		response.setUserFirstname("Denver");
		response.setUserName("Le dernier dinosaure");
		
		
		// retrieve user information
//		UserDTO user = FacadeManager.getUserFacade().getUserByLogin(getHttpSession().getAttribute(BaseRPCServiceCache.SESSION_USER_LOGIN).toString());
//		GetSessionResponse response = new GetSessionResponse();
//		response.setLogin(user.getLogin());
//		response.setUserFirstname(user.getFirstname());
//		response.setUserName(user.getSecondname());
		
		return response;
		
	}

}
