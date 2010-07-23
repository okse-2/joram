/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.widget.handler.user;

import com.scalagent.appli.client.presenter.UserListPresenter;
import com.scalagent.appli.shared.UserWTO;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;

public class UserEditClickHandler implements ClickHandler {


	private UserListPresenter presenter;
	private DynamicForm form;


	public UserEditClickHandler(UserListPresenter presenter, DynamicForm form) {
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
				int periodValue = Integer.parseInt(form.getValueAsString("periodItem"));

				UserWTO newUser = new UserWTO(nameValue, periodValue, 0, null);
			
				presenter.editUser(newUser);
			}  
		} catch (Exception e) {
			SC.warn("An error occured while parsing datas");
		}
	}
}
