/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.widget.handler.topic;

import com.scalagent.appli.client.Application;
import com.scalagent.appli.client.presenter.TopicListPresenter;
import com.scalagent.appli.client.widget.record.TopicListRecord;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;

public class TopicDeleteClickHandler implements ClickHandler {


	private TopicListPresenter presenter;
	private TopicListRecord record;
	
	
	public TopicDeleteClickHandler(TopicListPresenter presenter, TopicListRecord record) {
		super();
		this.presenter = presenter;
		this.record = record;
	}
	

	
	public void onClick(ClickEvent event) {
		SC.confirm(Application.messages.topicWidget_confirmDelete(), new BooleanCallback() {

			@Override
			public void execute(Boolean value) {
				if(value) presenter.deleteTopic(record.getTopic());
			}
		});
	}	

	
	


}
