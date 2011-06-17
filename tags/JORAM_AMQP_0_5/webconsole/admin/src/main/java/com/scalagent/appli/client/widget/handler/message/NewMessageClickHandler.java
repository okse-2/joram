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
package com.scalagent.appli.client.widget.handler.message;

import com.scalagent.appli.client.presenter.QueueDetailPresenter;
import com.scalagent.appli.client.presenter.SubscriptionDetailPresenter;
import com.scalagent.appli.shared.MessageWTO;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;

/**
 * @author Yohann CINTRE
 */
public class NewMessageClickHandler implements ClickHandler {

  private QueueDetailPresenter qPresenter;
  private SubscriptionDetailPresenter sPresenter;
  private DynamicForm form;

  public NewMessageClickHandler(QueueDetailPresenter qPresenter, DynamicForm form) {
    super();
    this.qPresenter = qPresenter;
    this.form = form;
  }

  public NewMessageClickHandler(SubscriptionDetailPresenter sPresenter, DynamicForm form) {
    super();
    this.sPresenter = sPresenter;
    this.form = form;
  }

  @Override
  public void onClick(ClickEvent event) {
    try {
      if (form.validate()) {
        String queueNameValue = form.getValueAsString("queueNameItem");
        String idValue = form.getValueAsString("idItem");
        int expirationValue = Integer.parseInt(form.getValueAsString("expirationItem"));
        long timestampValue = Long.parseLong(form.getValueAsString("timestampItem"));
        int priorityValue = Integer.parseInt(form.getValueAsString("priorityItem"));
        String textValue = form.getValueAsString("textItem");
        int typeValue = Integer.parseInt(form.getValueAsString("typeItem"));

        MessageWTO newMessage = new MessageWTO(idValue, expirationValue, timestampValue, 0, priorityValue,
            textValue, typeValue, null);

        if (qPresenter != null)
          qPresenter.createNewMessage(newMessage, queueNameValue);
        else
          sPresenter.createNewMessage(newMessage, queueNameValue);
      }
    } catch (Exception e) {
      SC.warn("An error occured while parsing datas");
    }
  }
}
