/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.event.subscription;

import com.google.gwt.event.shared.EventHandler;
import com.scalagent.appli.shared.SubscriptionWTO;


public interface UpdatedSubscriptionHandler extends EventHandler {

	public void onSubscriptionUpdated(SubscriptionWTO sub);
	
}
