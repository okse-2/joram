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
package com.scalagent.appli.client.widget.handler.subscription;

import com.scalagent.appli.client.presenter.SubscriptionListPresenter;
import com.scalagent.appli.client.presenter.UserDetailPresenter;
import com.scalagent.appli.shared.SubscriptionWTO;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;

/**
 * @author Yohann CINTRE
 */
public class SubscriptionEditClickHandler implements ClickHandler {

  private SubscriptionListPresenter sPresenter;
  private UserDetailPresenter uPresenter;
  private DynamicForm form;

  public SubscriptionEditClickHandler(SubscriptionListPresenter sPresenter, DynamicForm form) {
    super();
    this.sPresenter = sPresenter;
    this.form = form;
  }

  public SubscriptionEditClickHandler(UserDetailPresenter uPresenter, DynamicForm form) {
    super();
    this.uPresenter = uPresenter;
    this.form = form;
  }

  @Override
  public void onClick(ClickEvent event) {

    /**
		 * 
		 */
    try {
      if (form.validate()) {

        String nameValue = form.getValueAsString("nameItem");
        boolean activeValue = ((CheckboxItem) form.getField("activeItem")).getValueAsBoolean();
        boolean durableValue = ((CheckboxItem) form.getField("durableItem")).getValueAsBoolean();
        int nbMaxMsgValue = Integer.parseInt(form.getValueAsString("nbMaxMsgItem"));
        int contextIdValue = Integer.parseInt(form.getValueAsString("contextIdItem"));
        String selectorValue = form.getValueAsString("selectorItem");
        int subRequestIdValue = Integer.parseInt(form.getValueAsString("subRequestIdItem"));

        SubscriptionWTO newSub = new SubscriptionWTO(nameValue, activeValue, durableValue, nbMaxMsgValue,
            contextIdValue, 0, 0, 0, selectorValue, subRequestIdValue);

        if (sPresenter != null)
          sPresenter.editSubscription(newSub);
        if (uPresenter != null)
          uPresenter.editSubscription(newSub);
      }
    } catch (Exception e) {
      SC.warn("An error occured while parsing datas");
    }
  }
}
