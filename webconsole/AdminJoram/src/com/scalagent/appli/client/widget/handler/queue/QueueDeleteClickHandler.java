/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.widget.handler.queue;

import com.scalagent.appli.client.Application;
import com.scalagent.appli.client.presenter.QueueListPresenter;
import com.scalagent.appli.client.widget.record.QueueListRecord;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;

public class QueueDeleteClickHandler implements ClickHandler {


	private QueueListPresenter queuePresenter;
	private QueueListRecord record;
	
	
	public QueueDeleteClickHandler(QueueListPresenter queuePresenter, QueueListRecord record) {
		super();
		this.queuePresenter = queuePresenter;
		this.record = record;
	}
	

	
	public void onClick(ClickEvent event) {
		SC.confirm(Application.messages.queueWidget_confirmDelete(), new BooleanCallback() {

			@Override
			public void execute(Boolean value) {
				if(value) queuePresenter.deleteQueue(record.getQueue());
			}
		});
	}	

	
	


}
