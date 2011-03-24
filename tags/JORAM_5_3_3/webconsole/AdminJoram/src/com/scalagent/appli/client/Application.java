/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine;
import com.scalagent.appli.client.event.LoginValidEvent;
import com.scalagent.appli.client.event.UpdateCompleteEvent;
import com.scalagent.appli.client.event.queue.DeletedQueueEvent;
import com.scalagent.appli.client.event.queue.NewQueueEvent;
import com.scalagent.appli.client.event.queue.QueueDetailClickEvent;
import com.scalagent.appli.client.event.queue.UpdatedQueueEvent;
import com.scalagent.appli.client.event.subscription.DeletedSubscriptionEvent;
import com.scalagent.appli.client.event.subscription.NewSubscriptionEvent;
import com.scalagent.appli.client.event.subscription.SubscriptionDetailClickEvent;
import com.scalagent.appli.client.event.subscription.UpdatedSubscriptionEvent;
import com.scalagent.appli.client.event.topic.DeletedTopicEvent;
import com.scalagent.appli.client.event.topic.NewTopicEvent;
import com.scalagent.appli.client.event.topic.UpdatedTopicEvent;
import com.scalagent.appli.client.event.user.DeletedUserEvent;
import com.scalagent.appli.client.event.user.NewUserEvent;
import com.scalagent.appli.client.event.user.UpdatedUserEvent;
import com.scalagent.appli.client.event.user.UserDetailClickEvent;
import com.scalagent.appli.client.presenter.LoginPresenter;
import com.scalagent.appli.client.presenter.MainPresenter;
import com.scalagent.appli.client.presenter.QueueListPresenter;
import com.scalagent.appli.client.presenter.ServerPresenter;
import com.scalagent.appli.client.presenter.SubscriptionListPresenter;
import com.scalagent.appli.client.presenter.TopicListPresenter;
import com.scalagent.appli.client.presenter.UserListPresenter;
import com.scalagent.appli.client.widget.MainWidget;
import com.scalagent.engine.client.BaseEntryPoint;
import com.smartgwt.client.widgets.Canvas;


public class Application implements BaseEntryPoint {

	public static final ApplicationMessages messages = (ApplicationMessages) GWT
	.create(ApplicationMessages.class);

	private RPCServiceAsync serviceAsync;
	private RPCServiceCacheClient serviceCache;
	private HandlerManager eventBus;
//	private final int UPDATEDELAY = 3000;
	// TODO : updatedelay en dur quelque part

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {

		Runnable onLoadCallback = new Runnable() {
			public void run() {

				System.out.println("############################################################");
				System.out.println("### appli.client.Application loaded : démarrage application");

				serviceAsync = GWT.create(RPCService.class);

				eventBus = new HandlerManager(null);
				serviceCache = new RPCServiceCacheClient(serviceAsync, eventBus, -1);

				ServerPresenter serverPresenter = new ServerPresenter(serviceAsync, eventBus, serviceCache);
				LoginPresenter loginPresenter = new LoginPresenter(serviceAsync, eventBus, serviceCache);
				TopicListPresenter topicPresenter = new TopicListPresenter(serviceAsync, eventBus, serviceCache);
				QueueListPresenter queuePresenter = new QueueListPresenter(serviceAsync, eventBus, serviceCache);
				UserListPresenter userPresenter = new UserListPresenter(serviceAsync, eventBus, serviceCache);
				SubscriptionListPresenter subscriptionPresenter = new SubscriptionListPresenter(serviceAsync, eventBus, serviceCache);


				MainPresenter mainPresenter = new MainPresenter(serviceAsync, serviceCache, eventBus, loginPresenter, serverPresenter, topicPresenter, queuePresenter, userPresenter, subscriptionPresenter);

				eventBus.addHandler(QueueDetailClickEvent.TYPE, mainPresenter);
				eventBus.addHandler(UserDetailClickEvent.TYPE, mainPresenter);
				eventBus.addHandler(SubscriptionDetailClickEvent.TYPE, mainPresenter);
				eventBus.addHandler(LoginValidEvent.TYPE, mainPresenter);

				eventBus.addHandler(UpdateCompleteEvent.TYPE, serverPresenter);

				eventBus.addHandler(NewTopicEvent.TYPE, topicPresenter);
				eventBus.addHandler(DeletedTopicEvent.TYPE, topicPresenter);
				eventBus.addHandler(UpdatedTopicEvent.TYPE, topicPresenter);
				eventBus.addHandler(UpdateCompleteEvent.TYPE, topicPresenter);

				eventBus.addHandler(NewQueueEvent.TYPE, queuePresenter);
				eventBus.addHandler(DeletedQueueEvent.TYPE, queuePresenter);
				eventBus.addHandler(UpdatedQueueEvent.TYPE, queuePresenter);
				eventBus.addHandler(UpdateCompleteEvent.TYPE, queuePresenter);

				eventBus.addHandler(NewUserEvent.TYPE, userPresenter);
				eventBus.addHandler(DeletedUserEvent.TYPE, userPresenter);
				eventBus.addHandler(UpdatedUserEvent.TYPE, userPresenter);
				eventBus.addHandler(UpdateCompleteEvent.TYPE, userPresenter);

				eventBus.addHandler(NewSubscriptionEvent.TYPE, subscriptionPresenter);
				eventBus.addHandler(DeletedSubscriptionEvent.TYPE, subscriptionPresenter);
				eventBus.addHandler(UpdatedSubscriptionEvent.TYPE, subscriptionPresenter);
				eventBus.addHandler(UpdateCompleteEvent.TYPE, subscriptionPresenter);


				MainWidget mWidget = mainPresenter.getWidget();			
				Canvas mainCanvas = (Canvas)mWidget.asWidget();

				mainCanvas.draw();



			}
		};

//		VisualizationUtils.loadVisualizationApi(onLoadCallback, LineChart.PACKAGE);
		VisualizationUtils.loadVisualizationApi(onLoadCallback, AnnotatedTimeLine.PACKAGE);
	}
}