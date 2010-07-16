/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.event.topic;

import com.google.gwt.event.shared.GwtEvent;
import com.scalagent.appli.shared.TopicWTO;


public class NewTopicEvent extends GwtEvent<NewTopicHandler> {

	public static Type<NewTopicHandler> TYPE = new Type<NewTopicHandler>();
	private TopicWTO topic;
	
	public NewTopicEvent(TopicWTO topic) {
		this.topic = topic;
	}
	@Override
	public final Type<NewTopicHandler> getAssociatedType() {
		return TYPE;
	}
	@Override
	public void dispatch(NewTopicHandler handler) {
		handler.onNewTopic(topic);
	}

}
