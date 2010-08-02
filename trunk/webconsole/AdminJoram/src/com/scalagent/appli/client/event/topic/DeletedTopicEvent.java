/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.event.topic;

import com.google.gwt.event.shared.GwtEvent;
import com.scalagent.appli.shared.TopicWTO;

/**
 * @author Yohann CINTRE
 */
public class DeletedTopicEvent extends GwtEvent<DeletedTopicHandler> {

	public static Type<DeletedTopicHandler> TYPE = new Type<DeletedTopicHandler>();
	private TopicWTO topic;
	
	public DeletedTopicEvent(TopicWTO device) {
		this.topic = device;
	}
	@Override
	public final Type<DeletedTopicHandler> getAssociatedType() {
		return TYPE;
	}
	@Override
	public void dispatch(DeletedTopicHandler handler) {
		handler. onTopicDeleted(topic);
	}

}
