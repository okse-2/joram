/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.widget.handler.queue;

import java.util.Date;

import com.scalagent.appli.client.presenter.QueueListPresenter;
import com.scalagent.appli.shared.QueueWTO;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;

public class QueueEditClickHandler implements ClickHandler {


	private QueueListPresenter queuePresenter;
	private DynamicForm form;


	public QueueEditClickHandler(QueueListPresenter queuePresenter, DynamicForm form) {
		super();
		this.queuePresenter = queuePresenter;
		this.form = form;
	}
	
	@Override
	public void onClick(ClickEvent event) {

		try {
			if(form.validate())
			{
				
				String nameValue = form.getValueAsString("nameItem");
				String DMQValue = form.getValueAsString("DMQItem");
				String destinationValue = form.getValueAsString("destinationItem");
				int periodValue = Integer.parseInt(form.getValueAsString("periodItem"));
				int thresholdValue = Integer.parseInt(form.getValueAsString("thresholdItem"));
				int nbMaxMsgValue = Integer.parseInt(form.getValueAsString("nbMaxMsgItem"));
				boolean freeReadingValue = ((CheckboxItem)form.getField("freeReadingItem")).getValueAsBoolean();
				boolean freeWritingValue = ((CheckboxItem)form.getField("freeWritingItem")).getValueAsBoolean();

				QueueWTO newQueue = new QueueWTO(nameValue, new Date(), DMQValue, destinationValue, 0, 0, 0, periodValue, null, freeReadingValue, freeWritingValue, thresholdValue, 0, 0, 0, nbMaxMsgValue);
			
				queuePresenter.editQueue(newQueue);
			}  
		} catch (Exception e) {
			SC.warn("An error occured while parsing datas");
		}
	}
}
