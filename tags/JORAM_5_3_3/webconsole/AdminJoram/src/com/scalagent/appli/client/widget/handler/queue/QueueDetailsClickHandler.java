/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.widget.handler.queue;

import com.scalagent.appli.client.presenter.QueueListPresenter;
import com.scalagent.appli.client.widget.record.QueueListRecord;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;

public class QueueDetailsClickHandler implements ClickHandler {


	private QueueListPresenter queuePresenter;
	private QueueListRecord record;
	
	
	public QueueDetailsClickHandler(QueueListPresenter queuePresenter, QueueListRecord record) {
		super();
		this.queuePresenter = queuePresenter;
		this.record = record;
	}
	
	@Override
	public void onClick(ClickEvent event) {

		queuePresenter.fireQueueDetailsClick(record.getQueue());
	}
	
	


}
