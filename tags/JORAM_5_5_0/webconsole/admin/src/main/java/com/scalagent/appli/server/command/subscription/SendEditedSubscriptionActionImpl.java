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
package com.scalagent.appli.server.command.subscription;

import com.scalagent.appli.client.command.subscription.SendEditedSubscriptionAction;
import com.scalagent.appli.client.command.subscription.SendEditedSubscriptionResponse;
import com.scalagent.appli.server.RPCServiceImpl;
import com.scalagent.engine.server.command.ActionImpl;

/**
 * @author Yohann CINTRE
 */
public class SendEditedSubscriptionActionImpl extends
    ActionImpl<SendEditedSubscriptionResponse, SendEditedSubscriptionAction, RPCServiceImpl> {

  @Override
  public SendEditedSubscriptionResponse execute(RPCServiceImpl cache, SendEditedSubscriptionAction action) {

    boolean result = cache.editSubscription(action.getSubscription());

    String info = "";

    if (result) {
      info = "The subscription \"" + action.getSubscription().getId() + "\" has been updated.";
    } else {
      info = "Error while updating queue \"" + action.getSubscription().getId() + "\"";
    }

    return new SendEditedSubscriptionResponse(result, info);
  }
}
