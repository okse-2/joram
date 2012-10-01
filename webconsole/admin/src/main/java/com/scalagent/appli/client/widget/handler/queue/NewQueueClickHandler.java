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

import com.scalagent.appli.client.presenter.QueueListPresenter;
import com.scalagent.appli.shared.QueueWTO;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;

/**
 * @author Yohann CINTRE
 */
public class NewQueueClickHandler implements ClickHandler {

  private QueueListPresenter queuePresenter;
  private DynamicForm form;

  public NewQueueClickHandler(QueueListPresenter queuePresenter, DynamicForm form) {
    super();
    this.queuePresenter = queuePresenter;
    this.form = form;
  }

  @Override
  public void onClick(ClickEvent event) {

    try {
      if (form.validate()) {

        String nameValue = form.getValueAsString("nameItem");

        QueueWTO newQueue = new QueueWTO();
        newQueue.setId(nameValue);

        queuePresenter.createNewQueue(newQueue);
      }
    } catch (Exception e) {
      SC.warn("An error occured while parsing datas");
    }
  }
}
