package com.scalagent.engine.server;


import javax.servlet.http.HttpSession;



import com.scalagent.appli.server.RPCServiceCache;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;
import com.scalagent.engine.client.command.Response;
import com.scalagent.engine.server.command.ActionImpl;


public class BaseRPCServiceImpl extends BaseRPCService {

	private static final long serialVersionUID = 1L;
	protected BaseRPCServiceCache cache;
	
	public BaseRPCServiceImpl() {
		
		System.out.println("### engine.server.BaseRPCServiceImpl loaded : création du service RPC");
		
		BaseRPCServiceTimer timer = new BaseRPCServiceTimer(this, 30000, 100000);	
		timer.start();
		
	}
	
	/**
	 * Unique method of the service.
	 * It takes an action Action as parameter and returns a response Response.
	 * See Action, ActionImpl for more information.
	 * 
	 * It uses the annotation CalledAnnotation to determine which class must be called
	 * in order to provide a response to the requester.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public synchronized <R extends Response> R execute(Action<R> action) {

		try {
			System.out.println("### engine.server.BaseRPCServiceImpl.execute : "+action.toString());

			this.getThreadLocalResponse().setCharacterEncoding("utf-8");
			HttpSession session = this.getThreadLocalRequest().getSession();
			session.setAttribute(RPCServiceCache.SESSION_USER_LOGIN, this.getThreadLocalRequest().getRemoteUser());

			
			CalledMethod annotation = action.getClass().getAnnotation(CalledMethod.class);
			try {

				Class toCall = annotation.value();
				ActionImpl procedure = (ActionImpl)toCall.newInstance();
				try {
					
					updateSessionInformation(session.getId(), System.currentTimeMillis());
					procedure.setHttpSession(session);
					procedure.setRPCService(this);
					R response = (R)procedure.execute(cache, action);
					return response;
					
				} catch(Throwable t) { 
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
