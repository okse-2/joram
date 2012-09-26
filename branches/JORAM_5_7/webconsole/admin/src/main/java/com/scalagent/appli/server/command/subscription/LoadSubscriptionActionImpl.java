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

import java.util.List;

import com.scalagent.appli.client.command.subscription.LoadSubscriptionAction;
import com.scalagent.appli.client.command.subscription.LoadSubscriptionResponse;
import com.scalagent.appli.server.RPCServiceImpl;
import com.scalagent.appli.shared.SubscriptionWTO;
import com.scalagent.engine.server.command.ActionImpl;

/**
 * @author Yohann CINTRE
 */
public class LoadSubscriptionActionImpl extends
    ActionImpl<LoadSubscriptionResponse, LoadSubscriptionAction, RPCServiceImpl> {

  @Override
  public LoadSubscriptionResponse execute(RPCServiceImpl cache, LoadSubscriptionAction action) {

    List<SubscriptionWTO> subs = cache.getSubscriptions(this.getHttpSession(), action.isRetrieveAll(),
        action.isforceUpdate());
    return new LoadSubscriptionResponse(subs);
  }

}