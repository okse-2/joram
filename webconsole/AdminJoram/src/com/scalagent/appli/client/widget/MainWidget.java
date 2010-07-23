/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.widget;


import com.google.gwt.user.client.ui.Widget;
import com.scalagent.appli.client.Application;
import com.scalagent.appli.client.presenter.MainPresenter;
import com.scalagent.engine.client.widget.BaseWidget;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Side;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import com.smartgwt.client.widgets.tab.events.CloseClickHandler;
import com.smartgwt.client.widgets.tab.events.TabCloseClickEvent;


public class MainWidget extends BaseWidget<MainPresenter> {

	private LoginWidget loginWidget;
	private ServerWidget serverWidget;
	private TopicListWidget topicWidget;
	private QueueListWidget queueWidget;
	private UserListWidget userWidget;
	private SubscriptionListWidget subscriptionWidget;

	Canvas cLogin;
	Canvas cAdminPanel;

	VLayout applicationLayout;
	HLayout headerLayout;
	Label headerLabel;
	Img headerImg;

	private TabSet topTabSet;

	public MainWidget(MainPresenter mainpresenter, 
			LoginWidget loginWidget,
			ServerWidget serverWidget,
			TopicListWidget topicWidget, 
			QueueListWidget queueWidget, 
			UserListWidget userWidget,
			SubscriptionListWidget subscriptionWidget) {
		
		super(mainpresenter);
		this.loginWidget = loginWidget;
		this.serverWidget = serverWidget;
		this.topicWidget = topicWidget;
		this.queueWidget = queueWidget;
		this.userWidget = userWidget;
		this.subscriptionWidget = subscriptionWidget;
	}


	@Override
	public Widget asWidget() {

		headerLabel = new Label();
		headerLabel.setWidth("*");
		headerLabel.setPadding(10);  
		headerLabel.setAlign(Alignment.CENTER);  
		headerLabel.setValign(VerticalAlignment.CENTER);   
		headerLabel.setContents("<h1>"+Application.messages.mainWidget_headerLabel_title()+"</h1>");  


		headerImg = new Img("joram_logo.png");
		headerImg.setSize("160px", "75px");

		headerLayout = new HLayout();
		headerLayout.setWidth100();
		headerLayout.setHeight("75px");
		headerLayout.addMember(headerLabel);
		headerLayout.addMember(headerImg);


		applicationLayout = new VLayout();  
		applicationLayout.setShowEdges(false);
		applicationLayout.setWidth100();
		applicationLayout.setHeight100();
		applicationLayout.setShowResizeBar(true);
		applicationLayout.setMembersMargin(2);  
		applicationLayout.setLayoutMargin(2);  

		applicationLayout.addMember(headerLayout);

		createLogin();
		showLogin();
		
		return applicationLayout;
	}

	public void addTab(Tab tab) {
		topTabSet.addTab(tab);
	}

	public void showTab(Tab tab) {
		topTabSet.selectTab(tab);
	}

	public void createLogin() {
		cLogin = (Canvas) loginWidget.asWidget();
	}
	
	public void showLogin() {
		applicationLayout.addMember(cLogin);
	}
	
	public void hideLogin() {
		applicationLayout.hideMember(cLogin);
	}

	public void showAdminPanel() {
		applicationLayout.addMember(cAdminPanel);
	}

	public void hideAdminPanel() {
		applicationLayout.removeMember(cAdminPanel);
	}

	public void createAdminPanel() {
		topTabSet = new TabSet();  
		topTabSet.setTabBarPosition(Side.TOP);  


		Tab tabInfo = new Tab(Application.messages.mainWidget_tabInfo_title()); 
		tabInfo.setPane((Canvas) serverWidget.asWidget());  	
		tabInfo.setIcon("server.png");

		Tab tabTopics = new Tab(Application.messages.mainWidget_tabTopic_title());
		tabTopics.setPane((Canvas) topicWidget.asWidget());  
		tabTopics.setIcon("topics.png");

		Tab tabQueues = new Tab(Application.messages.mainWidget_tabQueue_title());
		tabQueues.setPane((Canvas) queueWidget.asWidget());   
		tabQueues.setIcon("queues.png");

		Tab tabSubscriptions = new Tab(Application.messages.mainWidget_tabSubscription_title());
		tabSubscriptions.setPane((Canvas) subscriptionWidget.asWidget());  
		tabSubscriptions.setIcon("subs.png");

		Tab tabUsers = new Tab(Application.messages.mainWidget_tabUsers_title());
		tabUsers.setPane((Canvas) userWidget.asWidget());  
		tabUsers.setIcon("users.png");

		Tab tabConnections = new Tab(Application.messages.mainWidget_tabConnections_title());
		tabConnections.setPane(new Label("Connexions...")); 
		tabConnections.setIcon("connect.png");

		topTabSet.addTab(tabInfo);  
		topTabSet.addTab(tabTopics);  
		topTabSet.addTab(tabQueues);  
		topTabSet.addTab(tabSubscriptions);  
		topTabSet.addTab(tabUsers);  
		topTabSet.addTab(tabConnections);  

		topTabSet.addCloseClickHandler(new CloseClickHandler() {

			@Override
			public void onCloseClick(TabCloseClickEvent event) {
				presenter.onTabCloseClick(event.getTab());
			}
		});  
		

		cAdminPanel=topTabSet;
	}


}

