/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.presenter;

import java.util.HashMap;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.Widget;
import com.scalagent.appli.client.RPCServiceAsync;
import com.scalagent.appli.client.RPCServiceCacheClient;
import com.scalagent.appli.client.command.session.GetSessionAction;
import com.scalagent.appli.client.command.session.GetSessionHandler;
import com.scalagent.appli.client.command.session.GetSessionResponse;
import com.scalagent.appli.client.event.LoginValidHandler;
import com.scalagent.appli.client.event.UpdateCompleteEvent;
import com.scalagent.appli.client.event.message.DeletedMessageEvent;
import com.scalagent.appli.client.event.message.NewMessageEvent;
import com.scalagent.appli.client.event.message.QueueNotFoundEvent;
import com.scalagent.appli.client.event.message.UpdatedMessageEvent;
import com.scalagent.appli.client.event.queue.DeletedQueueEvent;
import com.scalagent.appli.client.event.queue.QueueDetailClickHandler;
import com.scalagent.appli.client.event.queue.UpdatedQueueEvent;
import com.scalagent.appli.client.event.subscription.DeletedSubscriptionEvent;
import com.scalagent.appli.client.event.subscription.NewSubscriptionEvent;
import com.scalagent.appli.client.event.subscription.SubscriptionDetailClickHandler;
import com.scalagent.appli.client.event.subscription.UpdatedSubscriptionEvent;
import com.scalagent.appli.client.event.user.DeletedUserEvent;
import com.scalagent.appli.client.event.user.UpdatedUserEvent;
import com.scalagent.appli.client.event.user.UserDetailClickHandler;
import com.scalagent.appli.client.widget.MainWidget;
import com.scalagent.appli.shared.QueueWTO;
import com.scalagent.appli.shared.SubscriptionWTO;
import com.scalagent.appli.shared.UserWTO;
import com.scalagent.engine.client.event.SetUserHeaderEvent;
import com.scalagent.engine.client.presenter.BasePresenter;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.tab.Tab;


public class MainPresenter extends BasePresenter<MainWidget, RPCServiceAsync, RPCServiceCacheClient>
implements 
QueueDetailClickHandler,
UserDetailClickHandler,
SubscriptionDetailClickHandler,
LoginValidHandler
{

	private HashMap<String, Tab> openedTabList = new HashMap<String, Tab>();
	private HashMap<String, QueueDetailPresenter> openedQueueList = new HashMap<String, QueueDetailPresenter>();
	private HashMap<String, UserDetailPresenter> openedUserList = new HashMap<String, UserDetailPresenter>();
	private HashMap<String, SubscriptionDetailPresenter> openedSubList = new HashMap<String, SubscriptionDetailPresenter>();

	public MainPresenter(RPCServiceAsync testService, 
			RPCServiceCacheClient cache, 
			HandlerManager eventBus, 
			LoginPresenter loginPresenter,
			ServerPresenter serverPresenter,
			TopicListPresenter topicPresenter, 
			QueueListPresenter queuePresenter, 
			UserListPresenter userPresenter,
			SubscriptionListPresenter subscriptionPresenter) {
		
		super(testService, cache, eventBus);
		this.widget = new MainWidget(this, loginPresenter.getWidget(), 
				serverPresenter.getWidget(),
				topicPresenter.getWidget(), 
				queuePresenter.getWidget(), 
				userPresenter.getWidget(), 
				subscriptionPresenter.getWidget());
	}

	public void onQueueDetailsClick(QueueWTO queue) {

		if (!openedTabList.containsKey(queue.getName())) {

			Tab tabQueue = new Tab(queue.getName()); 
			
			QueueDetailPresenter queueDetailsPresenter = new QueueDetailPresenter(service, eventBus, cache, queue);

			eventBus.addHandler(NewMessageEvent.TYPE, queueDetailsPresenter);
			eventBus.addHandler(DeletedMessageEvent.TYPE, queueDetailsPresenter);
			eventBus.addHandler(UpdatedMessageEvent.TYPE, queueDetailsPresenter);
			eventBus.addHandler(UpdateCompleteEvent.TYPE, queueDetailsPresenter);
			eventBus.addHandler(QueueNotFoundEvent.TYPE, queueDetailsPresenter);
			eventBus.addHandler(DeletedQueueEvent.TYPE, queueDetailsPresenter);
			eventBus.addHandler(UpdatedQueueEvent.TYPE, queueDetailsPresenter);

			Canvas canvas = new Canvas();
			Widget wpie = queueDetailsPresenter.getWidget().asWidget();

			canvas.addChild(wpie);

			tabQueue.setPane(canvas);
			tabQueue.setCanClose(true);
			
			widget.addTab(tabQueue);
			openedTabList.put(queue.getName(), tabQueue);
			openedQueueList.put(queue.getName(), queueDetailsPresenter);
		}
		widget.showTab(openedTabList.get(queue.getName()));
	}

	public void onUserDetailsClick(UserWTO user) {

		if (!openedTabList.containsKey(user.getName())) {

			Tab tabUser = new Tab(user.getName()); 
			

			UserDetailPresenter userDetailsPresenter = new UserDetailPresenter(service, eventBus, cache, user);

			
			eventBus.addHandler(NewSubscriptionEvent.TYPE, userDetailsPresenter);
			eventBus.addHandler(DeletedSubscriptionEvent.TYPE, userDetailsPresenter);
			eventBus.addHandler(UpdatedSubscriptionEvent.TYPE, userDetailsPresenter);
			eventBus.addHandler(UpdateCompleteEvent.TYPE, userDetailsPresenter);
			eventBus.addHandler(DeletedUserEvent.TYPE, userDetailsPresenter);
			eventBus.addHandler(UpdatedUserEvent.TYPE, userDetailsPresenter);

			Canvas canvas = new Canvas();
			Widget wpie = userDetailsPresenter.getWidget().asWidget();
			canvas.addChild(wpie);
			
			tabUser.setPane(canvas);
			tabUser.setCanClose(true);

			widget.addTab(tabUser);
			openedTabList.put(user.getName(), tabUser);
			openedUserList.put(user.getName(), userDetailsPresenter);
		}
		widget.showTab(openedTabList.get(user.getName()));
	}
	
	@Override
	public void onSubDetailsClick(SubscriptionWTO sub) {
		
		if (!openedTabList.containsKey(sub.getName())) {

			Tab tabSub = new Tab(sub.getName()); 

			SubscriptionDetailPresenter subDetailsPresenter = new SubscriptionDetailPresenter(service, eventBus, cache, sub);
			
			eventBus.addHandler(NewMessageEvent.TYPE, subDetailsPresenter);
			eventBus.addHandler(DeletedMessageEvent.TYPE, subDetailsPresenter);
			eventBus.addHandler(UpdatedMessageEvent.TYPE, subDetailsPresenter);
			eventBus.addHandler(UpdateCompleteEvent.TYPE, subDetailsPresenter);
//			eventBus.addHandler(QueueNotFoundEvent.TYPE, subDetailsPresenter);
//			eventBus.addHandler(DeletedQueueEvent.TYPE, subDetailsPresenter);
//			eventBus.addHandler(UpdatedQueueEvent.TYPE, subDetailsPresenter);
			
			Canvas canvas = new Canvas();
			Widget wpie = subDetailsPresenter.getWidget().asWidget();
			canvas.addChild(wpie);
			
			tabSub.setPane(canvas);
			tabSub.setCanClose(true);

			widget.addTab(tabSub);
			openedTabList.put(sub.getName(), tabSub);
			openedSubList.put(sub.getName(), subDetailsPresenter);
		}
		widget.showTab(openedTabList.get(sub.getName()));
		
	}
	
	
	public void onTabCloseClick(Tab tab) {
		
		if(openedQueueList.keySet().contains(tab.getTitle())) openedQueueList.get(tab.getTitle()).stopChart();
		if(openedUserList.keySet().contains(tab.getTitle())) openedUserList.get(tab.getTitle()).stopChart();
		openedTabList.remove(tab.getTitle());
	}

	
	
	public void onLoginValid() {
		
		
		service.execute(new GetSessionAction(), new GetSessionHandler(eventBus) {

			@Override
			public void onSuccess(GetSessionResponse response) {
				// on SetSessionAction response, the session has been correctly set up.
				// then, the cache can be started.
				// XXX setPeriod
				cache.setPeriod(1000000);

				eventBus.fireEvent(new SetUserHeaderEvent(response.getUserFirstname()));
			}	
		});		
		
		widget.hideLogin();
		widget.createAdminPanel();
		widget.showAdminPanel();
	}
	
	public void onSessionError() {
		widget.showLogin();
		widget.hideAdminPanel();
	}

	
}
