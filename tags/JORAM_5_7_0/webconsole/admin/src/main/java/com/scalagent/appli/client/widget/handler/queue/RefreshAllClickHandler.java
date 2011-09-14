/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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
package com.scalagent.appli.client.widget.handler.queue;

import com.scalagent.appli.client.presenter.QueueDetailPresenter;
import com.scalagent.appli.client.presenter.QueueListPresenter;
import com.scalagent.appli.client.presenter.SubscriptionDetailPresenter;
import com.scalagent.appli.client.presenter.SubscriptionListPresenter;
import com.scalagent.appli.client.presenter.TopicListPresenter;
import com.scalagent.appli.client.presenter.UserDetailPresenter;
import com.scalagent.appli.client.presenter.UserListPresenter;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;

/**
 * @author Yohann CINTRE
 */
public class RefreshAllClickHandler implements ClickHandler {

  private QueueListPresenter queuePresenter;
  private QueueDetailPresenter queueDetailPresenter;
  private TopicListPresenter topicPresenter;
  private UserListPresenter userPresenter;
  private UserDetailPresenter userDetailPresenter;
  private SubscriptionListPresenter subscriptionPresenter;
  private SubscriptionDetailPresenter subscriptionDetailPresenter;

  public RefreshAllClickHandler(QueueListPresenter queuePresenter) {
    super();
    this.queuePresenter = queuePresenter;
  }

  public RefreshAllClickHandler(TopicListPresenter topicPresenter) {
    super();
    this.topicPresenter = topicPresenter;
  }

  public RefreshAllClickHandler(QueueDetailPresenter queueDetailPresenter) {
    super();
    this.queueDetailPresenter = queueDetailPresenter;
  }

  public RefreshAllClickHandler(UserListPresenter userPresenter) {
    super();
    this.userPresenter = userPresenter;
  }

  public RefreshAllClickHandler(SubscriptionListPresenter subscriptionPresenter) {
    super();
    this.subscriptionPresenter = subscriptionPresenter;
  }

  public RefreshAllClickHandler(UserDetailPresenter userDetailPresenter) {
    super();
    this.userDetailPresenter = userDetailPresenter;
  }

  public RefreshAllClickHandler(SubscriptionDetailPresenter subscriptionDetailPresenter) {
    super();
    this.subscriptionDetailPresenter = subscriptionDetailPresenter;
  }

  @Override
  public void onClick(ClickEvent event) {

    if (queuePresenter != null)
      queuePresenter.fireRefreshAll();
    if (queueDetailPresenter != null)
      queueDetailPresenter.fireRefreshAll();
    if (topicPresenter != null)
      topicPresenter.fireRefreshAll();
    if (userPresenter != null)
      userPresenter.fireRefreshAll();
    if (userDetailPresenter != null)
      userDetailPresenter.fireRefreshAll();
    if (subscriptionPresenter != null)
      subscriptionPresenter.fireRefreshAll();
    if (subscriptionDetailPresenter != null)
      subscriptionDetailPresenter.fireRefreshAll();
  }
}
