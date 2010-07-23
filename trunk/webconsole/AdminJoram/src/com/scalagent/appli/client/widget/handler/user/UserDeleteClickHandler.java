/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.widget.handler.user;

import com.scalagent.appli.client.Application;
import com.scalagent.appli.client.presenter.UserListPresenter;
import com.scalagent.appli.client.widget.record.UserListRecord;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;

public class UserDeleteClickHandler implements ClickHandler {


	private UserListPresenter presenter;
	private UserListRecord record;
	
	
	public UserDeleteClickHandler(UserListPresenter presenter, UserListRecord record) {
		super();
		this.presenter = presenter;
		this.record = record;
	}
	

	
	public void onClick(ClickEvent event) {
		SC.confirm(Application.messages.userWidget_confirmDelete(), new BooleanCallback() {

			@Override
			public void execute(Boolean value) {
				if(value) presenter.deleteUser(record.getUser());
			}
		});
	}	

	
	


}
