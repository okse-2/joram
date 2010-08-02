/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.event.subscription;

import com.google.gwt.event.shared.GwtEvent;
import com.scalagent.appli.shared.SubscriptionWTO;

/**
 * @author Yohann CINTRE
 */
public class UpdatedSubscriptionEvent extends GwtEvent<UpdatedSubscriptionHandler> {

	public static Type<UpdatedSubscriptionHandler> TYPE = new Type<UpdatedSubscriptionHandler>();
	private SubscriptionWTO sub;
	
	public UpdatedSubscriptionEvent(SubscriptionWTO sub) {
		this.sub = sub;
	}
	@Override
	public final Type<UpdatedSubscriptionHandler> getAssociatedType() {
		return TYPE;
	}
	@Override
	public void dispatch(UpdatedSubscriptionHandler handler) {
		handler.onSubscriptionUpdated(sub);
	}

}
