/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.event.topic;

import com.google.gwt.event.shared.GwtEvent;
import com.scalagent.appli.shared.TopicWTO;

/**
 * @author Yohann CINTRE
 */
public class UpdatedTopicEvent extends GwtEvent<UpdatedTopicHandler> {

	public static Type<UpdatedTopicHandler> TYPE = new Type<UpdatedTopicHandler>();
	private TopicWTO topic;
	
	public UpdatedTopicEvent(TopicWTO device) {
		this.topic = device;
	}
	@Override
	public final Type<UpdatedTopicHandler> getAssociatedType() {
		return TYPE;
	}
	@Override
	public void dispatch(UpdatedTopicHandler handler) {
		handler.onTopicUpdated(topic);
	}

}
