/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.event.queue;

import com.google.gwt.event.shared.GwtEvent;
import com.scalagent.appli.shared.QueueWTO;

public class NewQueueEvent extends GwtEvent<NewQueueHandler> {

	public static Type<NewQueueHandler> TYPE = new Type<NewQueueHandler>();
	private QueueWTO queue;
	
	public NewQueueEvent(QueueWTO queue) {
		this.queue = queue;
	}
	@Override
	public final Type<NewQueueHandler> getAssociatedType() {
		return TYPE;
	}
	@Override
	public void dispatch(NewQueueHandler handler) {
		handler.onNewQueue(queue);
	}

}
