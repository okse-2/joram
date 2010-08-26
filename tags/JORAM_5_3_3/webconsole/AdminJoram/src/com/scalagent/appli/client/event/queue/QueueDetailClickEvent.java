/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.event.queue;

import com.google.gwt.event.shared.GwtEvent;
import com.scalagent.appli.shared.QueueWTO;

public class QueueDetailClickEvent extends GwtEvent<QueueDetailClickHandler> {

	public static Type<QueueDetailClickHandler> TYPE = new Type<QueueDetailClickHandler>();
	private QueueWTO queue;
	
	public QueueDetailClickEvent(QueueWTO queue) {
		this.queue = queue;
	}
	@Override
	public final Type<QueueDetailClickHandler> getAssociatedType() {
		return TYPE;
	}
	@Override
	public void dispatch(QueueDetailClickHandler handler) {
		handler.onQueueDetailsClick(queue);
	}

}
