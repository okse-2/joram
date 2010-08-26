/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.event.queue;

import com.google.gwt.event.shared.GwtEvent;
import com.scalagent.appli.shared.QueueWTO;



public class DeletedQueueEvent extends GwtEvent<DeletedQueueHandler> {

	public static Type<DeletedQueueHandler> TYPE = new Type<DeletedQueueHandler>();
	private QueueWTO queue;
	
	public DeletedQueueEvent(QueueWTO queue) {
		this.queue = queue;
	}
	@Override
	public final Type<DeletedQueueHandler> getAssociatedType() {
		return TYPE;
	}
	@Override
	public void dispatch(DeletedQueueHandler handler) {
		handler.onQueueDeleted(queue);
	}

}
