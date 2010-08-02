/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client;

import com.google.gwt.user.client.Timer;

/**
 * @author Yohann CINTRE
 */
public class RPCServiceCacheTimer extends Timer {

	private RPCServiceCacheClient cache;
	
	public RPCServiceCacheTimer(RPCServiceCacheClient cache) {
		this.cache = cache;
	}
	
	@Override
	public void run() {
		cache.retrieveQueue(false);
		cache.retrieveTopic(false);
		cache.retrieveUser(false);
		cache.retrieveSubscription(false);
		cache.retrieveServerInfo(false);
	}
}
