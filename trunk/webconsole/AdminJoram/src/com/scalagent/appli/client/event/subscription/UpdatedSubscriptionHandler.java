/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.event.subscription;

import com.google.gwt.event.shared.EventHandler;
import com.scalagent.appli.shared.SubscriptionWTO;

/**
 * @author Yohann CINTRE
 */
public interface UpdatedSubscriptionHandler extends EventHandler {

	public void onSubscriptionUpdated(SubscriptionWTO sub);
	
}
