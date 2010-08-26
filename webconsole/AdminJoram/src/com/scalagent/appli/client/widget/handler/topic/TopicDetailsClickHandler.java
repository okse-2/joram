/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.widget.handler.topic;

import com.scalagent.appli.client.presenter.TopicListPresenter;
import com.scalagent.appli.client.widget.record.TopicListRecord;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;

public class TopicDetailsClickHandler implements ClickHandler {


	private TopicListPresenter topicPresenter;
	private TopicListRecord record;
	
	
	public TopicDetailsClickHandler(TopicListPresenter topicPresenter, TopicListRecord record) {
		super();
		this.topicPresenter = topicPresenter;
		this.record = record;
	}
	
	@Override
	public void onClick(ClickEvent event) {

		topicPresenter.fireQueueDetailsClick(record.getTopic());
	}
	
	


}
