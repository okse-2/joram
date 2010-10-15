package com.scalagent.engine.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * This class implements a runnable.
 * It checks periodically sessions stored in BaseRPCServiceImpl.sessions hashmap.
 * If a session is invalidate, calls the method BaseRPCServiceImpl.onInvalidSession(String sessionId, Long lastTimepstamp).
 * Otherwise, do nothing.
 * 
 * @author Florian Gimbert
 *
 */
public class BaseRPCServiceTimer extends Thread {

//	private static final Logger LOGGER = Log.getLogger(BaseRPCServiceTimer.class.getName());
	
	private BaseRPCServiceImpl service;
	private int updatePeriod = 30000;
	private int invalidateSessionDelay = 80000;
	private boolean canStop = false;
	
	public BaseRPCServiceTimer(BaseRPCServiceImpl service, int updatePeriod, int invalidateSessionDelay) {
		this.updatePeriod = updatePeriod;
		this.invalidateSessionDelay = invalidateSessionDelay;
		this.service = service;
	}
	
	@Override
	public void run() {


		while (!canStop) {
			
			// checks sessions

			HashMap<String, Long> sessions = service.getSessionsInformation();
			Set<String> keys = sessions.keySet();
			Iterator<String> iterator = keys.iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
                Long sessionTimestamp = sessions.get(key);
                long currentTimestamp = System.currentTimeMillis();

				// check timestamp
				if ((currentTimestamp - sessionTimestamp.longValue()) > invalidateSessionDelay) {
					
					// and remove it from the sessions hashmap
					service.removeSession(key);
					// inform RPCService that the session is invalid
					service.onInvalidSession(key, sessionTimestamp);
				}
				
			}
			
			// sleep for updatePeriod
			try {
				Thread.sleep(updatePeriod);
			} catch (InterruptedException e) {
				canStop = true;
			}
		}
		
	}
		
}
