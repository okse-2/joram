/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.widget.handler.topic;

import java.util.Date;

import com.scalagent.appli.client.presenter.TopicListPresenter;
import com.scalagent.appli.shared.TopicWTO;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;

/**
 * @author Yohann CINTRE
 */
public class NewTopicClickHandler implements ClickHandler {


	private TopicListPresenter presenter;
	private DynamicForm form;

	public NewTopicClickHandler(TopicListPresenter presenter, DynamicForm form) {
		super();
		this.presenter = presenter;
		this.form = form;
	}
	
	@Override
	public void onClick(ClickEvent event) {

		try {
			if(form.validate())
			{
				String nameValue = form.getValueAsString("nameItem");
				String DMQIdValue = form.getValueAsString("DMQIdItem");
				String destinationIdValue = form.getValueAsString("destinationIdItem");
				long periodValue = Long.parseLong(form.getValueAsString("periodItem"));
				boolean freeReadingValue = ((CheckboxItem)form.getField("freeReadingItem")).getValueAsBoolean();
				boolean freeWritingValue = ((CheckboxItem)form.getField("freeWritingItem")).getValueAsBoolean();
				
				
				TopicWTO newTopic = new  TopicWTO(nameValue, new Date(), null, DMQIdValue, 
			    		destinationIdValue, 0, 0, 0, periodValue, null, 
			    		freeReadingValue, freeWritingValue);
				presenter.createNewTopic(newTopic);
			}  
		} catch (Exception e) {
			SC.warn("An error occured while parsing datas");
		}
	}
}
