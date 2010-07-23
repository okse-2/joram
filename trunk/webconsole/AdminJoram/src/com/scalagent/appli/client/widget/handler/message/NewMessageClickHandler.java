/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.widget.handler.message;

import com.scalagent.appli.client.presenter.QueueDetailPresenter;
import com.scalagent.appli.client.presenter.SubscriptionDetailPresenter;
import com.scalagent.appli.shared.MessageWTO;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;

public class NewMessageClickHandler implements ClickHandler {


	private QueueDetailPresenter qPresenter;
	private SubscriptionDetailPresenter sPresenter;
	private DynamicForm form;

	public NewMessageClickHandler(QueueDetailPresenter qPresenter, DynamicForm form) {
		super();
		this.qPresenter = qPresenter;
		this.form = form;
	}
	
	public NewMessageClickHandler(SubscriptionDetailPresenter sPresenter, DynamicForm form) {
		super();
		this.sPresenter = sPresenter;
		this.form = form;
	}

	@Override
	public void onClick(ClickEvent event) {
		try {
			if(form.validate())
			{
				String queueNameValue = form.getValueAsString("queueNameItem");
				String idValue = form.getValueAsString("idItem");
				int expirationValue = Integer.parseInt(form.getValueAsString("expirationItem"));
				long timestampValue = Long.parseLong(form.getValueAsString("timestampItem"));
				int priorityValue = Integer.parseInt(form.getValueAsString("priorityItem"));
				String textValue = form.getValueAsString("textItem");
				int typeValue = Integer.parseInt(form.getValueAsString("typeItem"));

				MessageWTO newMessage = new MessageWTO(idValue, expirationValue, timestampValue, 0, priorityValue, textValue, typeValue, null);
				
				if(qPresenter != null) qPresenter.createNewMessage(newMessage, queueNameValue);
				else sPresenter.createNewMessage(newMessage, queueNameValue);
			}  
		} catch (Exception e) {
			SC.warn("An error occured while parsing datas");
		}
	}
}
