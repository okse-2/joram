/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.widget.handler.user;

import com.scalagent.appli.client.presenter.UserListPresenter;
import com.scalagent.appli.client.widget.record.UserListRecord;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;

public class UserDetailsClickHandler implements ClickHandler {


	private UserListPresenter userListPresenter;
	private UserListRecord record;
	
	
	public UserDetailsClickHandler(UserListPresenter userListPresenter, UserListRecord record) {
		super();
		this.userListPresenter = userListPresenter;
		this.record = record;
	}
	
	@Override
	public void onClick(ClickEvent event) {

		userListPresenter.fireUserDetailsClick(record.getUser());
	}
	
	


}
