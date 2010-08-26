/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client;

import com.google.gwt.user.client.Timer;

public class RPCServiceCacheTimer extends Timer {

	private RPCServiceCacheClient cache;
	
	public RPCServiceCacheTimer(RPCServiceCacheClient cache) {
		System.out.println("### appli.client.RPCServiceCacheTimer loaded : création du timer pour le cache client");
		this.cache = cache;
	}
	
	@Override
	public void run() {
		System.out.println("############################################################");
		System.out.println("### appli.client.RPCServiceCacheTimer run : maj du cache client");
		cache.retrieveQueue(false);
		cache.retrieveTopic(false);
		cache.retrieveUser(false);
		cache.retrieveSubscription(false);
		
	}
	
}
