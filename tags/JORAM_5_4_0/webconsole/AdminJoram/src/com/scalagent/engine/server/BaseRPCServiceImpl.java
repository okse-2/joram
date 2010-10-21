package com.scalagent.engine.server;

import java.util.HashMap;

import javax.servlet.http.HttpSession;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.scalagent.engine.client.BaseRPCService;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;
import com.scalagent.engine.client.command.Response;
import com.scalagent.engine.server.command.ActionImpl;

public abstract class BaseRPCServiceImpl extends RemoteServiceServlet implements BaseRPCService {

	private static final long serialVersionUID = 8980983186019611263L;
	
	   public BaseRPCServiceImpl() {
	        BaseRPCServiceTimer timer = new BaseRPCServiceTimer(this, 30000, 100000);   
	        timer.start();
	    }

	/**
	 * This hashmap stores information on opened sessions.
	 * The key is the session id, the value is the timestamp of the last
	 * command received via this session.
	 * A timer (BaseRPCServiceTimer) checks periodically if the client
	 * still sends requests:
	 *    - if yes, nothing is done
	 *    - if no, the method onClientDisconnected(String sessionId, Long lastTimepstamp) is called. 
	 */
	private HashMap<String, Long> sessions = new HashMap<String, Long>();
	
	/**
	 * This method stores session information in the sessions hashmap.
	 *
	 * @param sessionId session id to store
	 * @param timestamp creation timestamp
	 */
	public synchronized void setSession(String sessionId, long timestamp) {
		
		sessions.put(sessionId, new Long(timestamp));
		
	}
	
	/**
	 * This method updates session information stored in the sessions hashmap.
	 * It's called for each new request received from a client which has already sent an SetSessionAction,
	 * and thus, created an entry in sessions hashmap.
	 * If not, nothing is done. 
	 * 
	 * @param sessionId session to update
	 * @param timestamp new timestamp
	 */
	public synchronized void updateSessionInformation(String sessionId, long timestamp) {
		
		if (sessions.containsKey(sessionId)) {
			sessions.put(sessionId, new Long(timestamp));
		}
	}
	
	/**
	 * This method removes session from the sessions hashmap.
	 *
	 * @param sessionId session id to remove
	 */
	public synchronized void removeSession(String sessionId) {
		
		sessions.remove(sessionId);
	}
	
	protected void onInvalidSession(String sessionId, Long timestamp) {
	}
	
	protected HashMap<String, Long> getSessionsInformation() {
		return (HashMap<String, Long>) sessions.clone();

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

            this.getThreadLocalResponse().setCharacterEncoding("utf-8");
            HttpSession session = this.getThreadLocalRequest().getSession();

            
            CalledMethod annotation = action.getClass().getAnnotation(CalledMethod.class);
            try {

                Class toCall = annotation.value();
                ActionImpl procedure = (ActionImpl)toCall.newInstance();
                try {
                    updateSessionInformation(session.getId(), System.currentTimeMillis());
                    procedure.setHttpSession(session);
                    procedure.setRPCService(this);
                    R response = (R)procedure.execute(this, action);
                    return response;
                    
                } catch(Throwable t) { 
                  t.printStackTrace();
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
	
}
