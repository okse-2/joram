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
package com.scalagent.appli.client.presenter;

import java.util.HashMap;
import java.util.Map;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.Widget;
import com.scalagent.appli.client.RPCServiceCacheClient;
import com.scalagent.appli.client.event.common.UpdateCompleteEvent;
import com.scalagent.appli.client.event.message.DeletedMessageEvent;
import com.scalagent.appli.client.event.message.NewMessageEvent;
import com.scalagent.appli.client.event.message.QueueNotFoundEvent;
import com.scalagent.appli.client.event.message.UpdatedMessageEvent;
import com.scalagent.appli.client.event.queue.DeletedQueueEvent;
import com.scalagent.appli.client.event.queue.QueueDetailClickHandler;
import com.scalagent.appli.client.event.queue.UpdatedQueueEvent;
import com.scalagent.appli.client.event.session.LoginValidHandler;
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
import com.scalagent.engine.client.BaseRPCServiceAsync;
import com.scalagent.engine.client.event.SystemErrorHandler;
import com.scalagent.engine.client.presenter.BasePresenter;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.tab.Tab;

/**
 * This class is the presenter associated to the main screen.
 * Its widget is MainWidget.
 * 
 * @author Yohann CINTRE
 */
public class MainPresenter extends BasePresenter<MainWidget, BaseRPCServiceAsync, RPCServiceCacheClient>
    implements SystemErrorHandler, QueueDetailClickHandler, UserDetailClickHandler,
    SubscriptionDetailClickHandler, LoginValidHandler {

  private Map<String, Tab> openedTabList = new HashMap<String, Tab>();
  private Map<String, QueueDetailPresenter> openedQueueList = new HashMap<String, QueueDetailPresenter>();
  private Map<String, UserDetailPresenter> openedUserList = new HashMap<String, UserDetailPresenter>();
  private Map<String, SubscriptionDetailPresenter> openedSubList = new HashMap<String, SubscriptionDetailPresenter>();

  public MainPresenter(BaseRPCServiceAsync testService, RPCServiceCacheClient cache, SimpleEventBus eventBus,
      LoginPresenter loginPresenter, ServerPresenter serverPresenter, TopicListPresenter topicPresenter,
      QueueListPresenter queuePresenter, UserListPresenter userPresenter,
      SubscriptionListPresenter subscriptionPresenter) {

    super(testService, cache, eventBus);
    this.widget = new MainWidget(this, loginPresenter.getWidget(), serverPresenter.getWidget(),
        topicPresenter.getWidget(), queuePresenter.getWidget(), userPresenter.getWidget(),
        subscriptionPresenter.getWidget());
  }

  /**
   * This method is called by the the QueueDetailClickHandler when the user
   * click
   * on the "Browse" button on the QueueListWidget.
   * If a tab for this queue is already open the tab get the focus,
   * otherwise a new tab for the queue is created, added to list and tabset ant
   * hen get the focus.
   */
  public void onQueueDetailsClick(QueueWTO queue) {
    String newTabTitle = "Queue: " + queue.getId();

    if (!openedTabList.containsKey(newTabTitle)) {

      Tab tabQueue = new Tab(newTabTitle);

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
      openedTabList.put(newTabTitle, tabQueue);
      openedQueueList.put(newTabTitle, queueDetailsPresenter);
    }
    widget.showTab(openedTabList.get(newTabTitle));
  }

  /**
   * This method is called by the the UserDetailClickHandler when the user click
   * on the "Browse" button on the UserListWidget.
   * If a tab for this subscription is already open the tab get the focus,
   * otherwise a new tab for the user is created, added to list and tabset ant
   * hen get the focus.
   */
  public void onUserDetailsClick(UserWTO user) {
    String newTabTitle = "User: " + user.getId();

    if (!openedTabList.containsKey(newTabTitle)) {

      Tab tabUser = new Tab(newTabTitle);

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
      openedTabList.put(newTabTitle, tabUser);
      openedUserList.put(newTabTitle, userDetailsPresenter);
    }
    widget.showTab(openedTabList.get(newTabTitle));
  }

  /**
   * This method is called by the the SubscriptionDetailClickHandler when the
   * user click
   * on the "Browse" button on the SubscriptionListWidget.
   * If a tab for this subscription is already open the tab get the focus,
   * otherwise a new tab for the subscription is created, added to list and
   * tabset ant hen get the focus.
   */
  @Override
  public void onSubDetailsClick(SubscriptionWTO sub) {
    String newTabTitle = "Sub: " + sub.getId();

    if (!openedTabList.containsKey(newTabTitle)) {

      Tab tabSub = new Tab(newTabTitle);

      SubscriptionDetailPresenter subDetailsPresenter = new SubscriptionDetailPresenter(service, eventBus,
          cache, sub);

      eventBus.addHandler(NewMessageEvent.TYPE, subDetailsPresenter);
      eventBus.addHandler(DeletedMessageEvent.TYPE, subDetailsPresenter);
      eventBus.addHandler(UpdatedMessageEvent.TYPE, subDetailsPresenter);
      eventBus.addHandler(UpdateCompleteEvent.TYPE, subDetailsPresenter);
      eventBus.addHandler(DeletedSubscriptionEvent.TYPE, subDetailsPresenter);
      eventBus.addHandler(UpdatedSubscriptionEvent.TYPE, subDetailsPresenter);

      Canvas canvas = new Canvas();
      Widget wpie = subDetailsPresenter.getWidget().asWidget();
      canvas.addChild(wpie);

      tabSub.setPane(canvas);
      tabSub.setCanClose(true);

      widget.addTab(tabSub);
      openedTabList.put(newTabTitle, tabSub);
      openedSubList.put(newTabTitle, subDetailsPresenter);

    }
    widget.showTab(openedTabList.get(newTabTitle));

  }

  /**
   * This method is called by the the MainWidget when the user close a tab.
   * The tab is removed from list and tabset.
   */
  public void onTabCloseClick(Tab tab) {
    if (openedQueueList.containsKey(tab.getTitle())) {
      openedQueueList.remove(tab.getTitle()).stopChart();
      widget.showQueueTab();
    } else if (openedUserList.containsKey(tab.getTitle())) {
      openedUserList.remove(tab.getTitle()).stopChart();
      widget.showUserTab();
    } else if (openedSubList.containsKey(tab.getTitle())) {
      openedSubList.remove(tab.getTitle()).stopChart();
      widget.showSubscriptionTab();
    }
    openedTabList.remove(tab.getTitle());
  }

  /**
   * This method is called by the the LoginValidHandler when the user
   * successfully log in.
   * The cache is started, the login widget is hidden and the admin panel
   * displayed
   */
  public void onLoginValid() {
    // the session has been correctly set up.
    // then, the cache can be started.
    cache.setPeriod(20000);

    widget.hideLogin();
    widget.createAdminPanel();
    widget.showAdminPanel();
  }

  /**
   * This method is called when an error occurred with the user session.
   * The login widget is displayed and the admin panel hidden
   */
  public void onSessionError() {
    widget.showLogin();
    widget.hideAdminPanel();
  }

  public void onSystemError(Throwable throwable) {
    Log.error("Contacting server failed.", throwable);
    SC.say("The browser encountered an issue while accessing the server. Maybe the server is down or has been restarted.");
  }
}
