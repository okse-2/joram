/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.event.subscription;

import com.google.gwt.event.shared.GwtEvent;
import com.scalagent.appli.shared.SubscriptionWTO;


/**
 * @author Yohann CINTRE
 */
public class DeletedSubscriptionEvent extends GwtEvent<DeletedSubscriptionHandler> {

	public static Type<DeletedSubscriptionHandler> TYPE = new Type<DeletedSubscriptionHandler>();
	private SubscriptionWTO sub;
	
	public DeletedSubscriptionEvent(SubscriptionWTO sub) {
		this.sub = sub;
	}
	@Override
	public final Type<DeletedSubscriptionHandler> getAssociatedType() {
		return TYPE;
	}
	@Override
	public void dispatch(DeletedSubscriptionHandler handler) {
		handler.onSubscriptionDeleted(sub);
	}

}
