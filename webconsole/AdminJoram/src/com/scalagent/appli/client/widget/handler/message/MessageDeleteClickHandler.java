/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.widget.handler.message;

import com.scalagent.appli.client.Application;
import com.scalagent.appli.client.presenter.QueueDetailPresenter;
import com.scalagent.appli.client.widget.record.MessageListRecord;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;

public class MessageDeleteClickHandler implements ClickHandler {


	private QueueDetailPresenter queueDetailPresenter;
	private MessageListRecord record;


	public MessageDeleteClickHandler(QueueDetailPresenter queueDetailPresenter, MessageListRecord record) {
		super();
		this.queueDetailPresenter = queueDetailPresenter;
		this.record = record;
	}


	@Override
	public void onClick(ClickEvent event) {
		SC.confirm(Application.messages.queueDetailWidget_confirmDelete(), new BooleanCallback() {

			@Override
			public void execute(Boolean value) {
				if(value) queueDetailPresenter.deleteMessage(record.getMessage(), queueDetailPresenter.getQueue());
			}
		});
	}	
}
