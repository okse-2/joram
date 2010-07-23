/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.widget.handler.subscription;

import com.scalagent.appli.client.presenter.SubscriptionListPresenter;
import com.scalagent.appli.client.presenter.UserDetailPresenter;
import com.scalagent.appli.shared.SubscriptionWTO;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;

public class NewSubscriptionClickHandler implements ClickHandler {


	private SubscriptionListPresenter sPresenter;
	private UserDetailPresenter uPresenter;
	private DynamicForm form;

	public NewSubscriptionClickHandler(SubscriptionListPresenter sPresenter, DynamicForm form) {
		super();
		this.sPresenter = sPresenter;
		this.form = form;
	}
	
	public NewSubscriptionClickHandler(UserDetailPresenter uPresenter, DynamicForm form) {
		super();
		this.uPresenter = uPresenter;
		this.form = form;
	}
	

	@Override
	public void onClick(ClickEvent event) {

		try {
			if(form.validate())
			{
				String nameValue = form.getValueAsString("nameItem");
				boolean activeValue = ((CheckboxItem)form.getField("activeItem")).getValueAsBoolean();
				boolean durableValue = ((CheckboxItem)form.getField("durableItem")).getValueAsBoolean();
				int nbMaxMsgValue = Integer.parseInt(form.getValueAsString("nbMaxMsgItem"));
				int contextIdValue = Integer.parseInt(form.getValueAsString("contextIdItem"));
				String selectorValue = form.getValueAsString("selectorItem");
				int subRequestIdValue = Integer.parseInt(form.getValueAsString("subRequestIdItem"));

				SubscriptionWTO newSub = new SubscriptionWTO(nameValue, activeValue, durableValue, 
						nbMaxMsgValue, contextIdValue,
						0, 0, 0, selectorValue, subRequestIdValue);

				if(sPresenter != null) sPresenter.createNewSubscription(newSub);
				if(uPresenter != null) uPresenter.createNewSubscription(newSub);
			}  
		} catch (Exception e) {
			SC.warn("An error occured while parsing datas");
		}
	}
}
