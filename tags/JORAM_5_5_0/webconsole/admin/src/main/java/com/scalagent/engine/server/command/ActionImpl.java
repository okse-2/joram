package com.scalagent.engine.server.command;

import javax.servlet.http.HttpSession;

import com.scalagent.engine.client.BaseRPCService;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.Response;

/**
 * This class must be implemented in order to perform an action and return a response 
 * via the RPC service. It is called automatically by TestRPCServiceImpl depending on the value of 
 * the annotation CalledAnnotation.
 * It sole method must return an instance of the Response class R and it will 
 * be send by the TestRPCServiceImpl to the requester.
 * 
 * If this application uses a cache, the session is available in this class
 * via the method getHttpSession().
 * 
 * @author Florian Gimbert
 * @param <R> class used to send the response
 * @param <A> class used to request an action
 */
public abstract class ActionImpl<R extends Response, A extends Action, C extends BaseRPCService> {

	private HttpSession session;
	private BaseRPCService service;
	
	public abstract R execute(C cache, A action) throws Exception;

	public void setHttpSession(HttpSession session) {
		this.session = session;
	}
	
	public HttpSession getHttpSession() {
		return session;
	}
	
	public BaseRPCService getRPCService() {
		return service;
	}
	
	public void setRPCService(BaseRPCService service) {
		this.service = service;
	}
	
}
