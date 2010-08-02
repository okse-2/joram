/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.event.message;

import com.google.gwt.event.shared.GwtEvent;
import com.scalagent.appli.shared.MessageWTO;

/**
 * @author Yohann CINTRE
 */
public class DeletedMessageEvent extends GwtEvent<DeletedMessageHandler> {

	public static Type<DeletedMessageHandler> TYPE = new Type<DeletedMessageHandler>();
	private MessageWTO message;
	private String queueName;

	public DeletedMessageEvent(MessageWTO message, String queueName) {
		this.message = message;
		this.queueName = queueName;
	}
	@Override
	public final Type<DeletedMessageHandler> getAssociatedType() {
		return TYPE;
	}
	@Override
	public void dispatch(DeletedMessageHandler handler) {
		handler.onMessageDeleted(message, queueName);
	}
}
