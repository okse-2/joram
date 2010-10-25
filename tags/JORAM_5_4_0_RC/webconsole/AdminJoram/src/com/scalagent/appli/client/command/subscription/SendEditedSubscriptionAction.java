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
package com.scalagent.appli.client.command.subscription;

import com.scalagent.appli.server.command.subscription.SendEditedSubscriptionActionImpl;
import com.scalagent.appli.shared.SubscriptionWTO;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;

/**
 * This action send a updated subscription to the server.
 * 
 * @author Yohann CINTRE
 */
@CalledMethod(value = SendEditedSubscriptionActionImpl.class)
public class SendEditedSubscriptionAction implements Action<SendEditedSubscriptionResponse> {

  private SubscriptionWTO sub;

  public SendEditedSubscriptionAction() {
  }

  public SendEditedSubscriptionAction(SubscriptionWTO sub) {
    this.sub = sub;
  }

  public SubscriptionWTO getSubscription() {
    return sub;
  }

}
