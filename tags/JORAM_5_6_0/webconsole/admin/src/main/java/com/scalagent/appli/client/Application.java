/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 - 2011 ScalAgent Distributed Technologies
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 *
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package com.scalagent.appli.client;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Command;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine;
import com.scalagent.appli.client.event.common.UpdateCompleteEvent;
import com.scalagent.appli.client.event.queue.DeletedQueueEvent;
import com.scalagent.appli.client.event.queue.NewQueueEvent;
import com.scalagent.appli.client.event.queue.QueueDetailClickEvent;
import com.scalagent.appli.client.event.queue.UpdatedQueueEvent;
import com.scalagent.appli.client.event.session.LoginValidEvent;
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
import com.scalagent.engine.client.BaseRPCService;
import com.scalagent.engine.client.BaseRPCServiceAsync;
import com.scalagent.engine.client.event.SystemErrorEvent;
import com.smartgwt.client.widgets.Canvas;

/**
 * @author Yohann CINTRE
 */
public class Application implements EntryPoint {

  public static final ApplicationMessages messages = (ApplicationMessages) GWT
      .create(ApplicationMessages.class);

  private BaseRPCServiceAsync serviceAsync;
  private RPCServiceCacheClient serviceCache;
  private SimpleEventBus eventBus;

  public void onModuleLoad() {
    Log.setUncaughtExceptionHandler();

    Scheduler.get().scheduleDeferred(new Command() {
      public void execute() {
        onModuleLoad2();
      }
    });
  }

  /**
   * This is the entry point method.
   */
  public void onModuleLoad2() {

    Runnable onLoadCallback = new Runnable() {
      public void run() {

        serviceAsync = GWT.create(BaseRPCService.class);

        eventBus = new SimpleEventBus();
        serviceCache = new RPCServiceCacheClient(serviceAsync, eventBus, -1);

        ServerPresenter serverPresenter = new ServerPresenter(serviceAsync, eventBus, serviceCache);
        LoginPresenter loginPresenter = new LoginPresenter(serviceAsync, eventBus, serviceCache);
        TopicListPresenter topicPresenter = new TopicListPresenter(serviceAsync, eventBus, serviceCache);
        QueueListPresenter queuePresenter = new QueueListPresenter(serviceAsync, eventBus, serviceCache);
        UserListPresenter userPresenter = new UserListPresenter(serviceAsync, eventBus, serviceCache);
        SubscriptionListPresenter subscriptionPresenter = new SubscriptionListPresenter(serviceAsync,
            eventBus, serviceCache);

        MainPresenter mainPresenter = new MainPresenter(serviceAsync, serviceCache, eventBus, loginPresenter,
            serverPresenter, topicPresenter, queuePresenter, userPresenter, subscriptionPresenter);

        eventBus.addHandler(SystemErrorEvent.TYPE, mainPresenter);
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
        Canvas mainCanvas = (Canvas) mWidget.asWidget();

        mainCanvas.draw();
      }
    };
    VisualizationUtils.loadVisualizationApi(onLoadCallback, AnnotatedTimeLine.PACKAGE);
  }
}
