/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.event.subscription;

import com.google.gwt.event.shared.GwtEvent;
import com.scalagent.appli.shared.SubscriptionWTO;

/**
 * @author Yohann CINTRE
 */
public class NewSubscriptionEvent extends GwtEvent<NewSubscriptionHandler> {

	public static Type<NewSubscriptionHandler> TYPE = new Type<NewSubscriptionHandler>();
	private SubscriptionWTO sub;
	
	public NewSubscriptionEvent(SubscriptionWTO sub) {
		this.sub = sub;
	}
	@Override
	public final Type<NewSubscriptionHandler> getAssociatedType() {
		return TYPE;
	}
	@Override
	public void dispatch(NewSubscriptionHandler handler) {
		handler.onNewSubscription(sub);
	}

}
