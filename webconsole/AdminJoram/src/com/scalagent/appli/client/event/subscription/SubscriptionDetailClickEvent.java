/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.event.subscription;

import com.google.gwt.event.shared.GwtEvent;
import com.scalagent.appli.shared.SubscriptionWTO;

/**
 * @author Yohann CINTRE
 */
public class SubscriptionDetailClickEvent extends GwtEvent<SubscriptionDetailClickHandler> {

	public static Type<SubscriptionDetailClickHandler> TYPE = new Type<SubscriptionDetailClickHandler>();
	private SubscriptionWTO sub;
	
	public SubscriptionDetailClickEvent(SubscriptionWTO sub) {
		this.sub = sub;
	}
	@Override
	public final Type<SubscriptionDetailClickHandler> getAssociatedType() {
		return TYPE;
	}
	@Override
	public void dispatch(SubscriptionDetailClickHandler handler) {
		handler.onSubDetailsClick(sub);
	}

}
