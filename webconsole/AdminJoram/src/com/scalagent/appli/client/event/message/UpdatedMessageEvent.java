/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.event.message;

import com.google.gwt.event.shared.GwtEvent;
import com.scalagent.appli.shared.MessageWTO;

/**
 * @author Yohann CINTRE
 */
public class UpdatedMessageEvent extends GwtEvent<UpdatedMessageHandler> {

	public static Type<UpdatedMessageHandler> TYPE = new Type<UpdatedMessageHandler>();
	private MessageWTO message;
	private String queueName;
	
	public UpdatedMessageEvent(MessageWTO message, String queueName) {
		this.message = message;
		this.queueName = queueName;
	}
	@Override
	public final Type<UpdatedMessageHandler> getAssociatedType() {
		return TYPE;
	}
	@Override
	public void dispatch(UpdatedMessageHandler handler) {
		handler.onMessageUpdated(message, queueName);
	}

}
