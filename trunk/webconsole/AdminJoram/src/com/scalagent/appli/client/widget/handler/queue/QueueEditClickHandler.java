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

import java.util.Date;

import com.scalagent.appli.client.presenter.QueueListPresenter;
import com.scalagent.appli.shared.QueueWTO;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;

/**
 * @author Yohann CINTRE
 */
public class QueueEditClickHandler implements ClickHandler {

  private QueueListPresenter queuePresenter;
  private DynamicForm form;

  public QueueEditClickHandler(QueueListPresenter queuePresenter, DynamicForm form) {
    super();
    this.queuePresenter = queuePresenter;
    this.form = form;
  }

  @Override
  public void onClick(ClickEvent event) {

    try {
      if (form.validate()) {

        String nameValue = form.getValueAsString("nameItem");
        String DMQValue = form.getValueAsString("DMQItem");
        String destinationValue = form.getValueAsString("destinationItem");
        int periodValue = Integer.parseInt(form.getValueAsString("periodItem"));
        int thresholdValue = Integer.parseInt(form.getValueAsString("thresholdItem"));
        int nbMaxMsgValue = Integer.parseInt(form.getValueAsString("nbMaxMsgItem"));
        boolean freeReadingValue = ((CheckboxItem) form.getField("freeReadingItem")).getValueAsBoolean();
        boolean freeWritingValue = ((CheckboxItem) form.getField("freeWritingItem")).getValueAsBoolean();

        QueueWTO newQueue = new QueueWTO(nameValue, new Date(), DMQValue, destinationValue, 0, 0, 0,
            periodValue, null, freeReadingValue, freeWritingValue, thresholdValue, 0, 0, 0, nbMaxMsgValue);

        queuePresenter.editQueue(newQueue);
      }
    } catch (Exception e) {
      SC.warn("An error occured while parsing datas");
    }
  }
}
