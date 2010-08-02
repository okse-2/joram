/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.event.message;

import com.google.gwt.event.shared.GwtEvent;
import com.scalagent.appli.shared.MessageWTO;

/**
 * @author Yohann CINTRE
 */
public class NewMessageEvent extends GwtEvent<NewMessageHandler> {

	public static Type<NewMessageHandler> TYPE = new Type<NewMessageHandler>();
	private MessageWTO message;
	private String queueName;
	
	public NewMessageEvent(MessageWTO message, String queueName) {
		this.message = message;
		this.queueName = queueName;
	}
	@Override
	public final Type<NewMessageHandler> getAssociatedType() {
		return TYPE;
	}
	@Override
	public void dispatch(NewMessageHandler handler) {
		handler.onNewMessage(message, queueName);
		
		
	}

}
