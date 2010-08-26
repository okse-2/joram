package com.scalagent.appli.client.event.queue;

import com.google.gwt.event.shared.GwtEvent;
import com.scalagent.appli.shared.QueueWTO;

public class UpdatedQueueEvent extends GwtEvent<UpdatedQueueHandler> {

	public static Type<UpdatedQueueHandler> TYPE = new Type<UpdatedQueueHandler>();
	private QueueWTO queue;
	
	public UpdatedQueueEvent(QueueWTO queue) {
		this.queue = queue;
	}
	@Override
	public final Type<UpdatedQueueHandler> getAssociatedType() {
		return TYPE;
	}
	@Override
	public void dispatch(UpdatedQueueHandler handler) {
		handler.onQueueUpdated(queue);
	}

}
