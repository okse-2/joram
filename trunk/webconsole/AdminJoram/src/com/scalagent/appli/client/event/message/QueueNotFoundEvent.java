/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.event.message;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Yohann CINTRE
 */
public class QueueNotFoundEvent extends GwtEvent<QueueNotFoundHandler> {

	private String queueName;
	
	public static Type<QueueNotFoundHandler> TYPE = new Type<QueueNotFoundHandler>();
	
	public QueueNotFoundEvent(String queueName) {
		this.queueName = queueName;
	}
	@Override
	public final Type<QueueNotFoundHandler> getAssociatedType() {
		return TYPE;
	}
	@Override
	public void dispatch(QueueNotFoundHandler handler) {
		handler.onQueueNotFound(queueName);
	}

}
