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
package com.scalagent.appli.server.command.message;

import java.util.List;

import com.scalagent.appli.client.command.message.LoadMessageAction;
import com.scalagent.appli.client.command.message.LoadMessageResponse;
import com.scalagent.appli.server.RPCServiceImpl;
import com.scalagent.appli.shared.MessageWTO;
import com.scalagent.engine.server.command.ActionImpl;

/**
 * @author Yohann CINTRE
 */
public class LoadMessageActionImpl extends
    ActionImpl<LoadMessageResponse, LoadMessageAction, RPCServiceImpl> {

  @Override
  public LoadMessageResponse execute(RPCServiceImpl cache, LoadMessageAction action) throws Exception {

    List<MessageWTO> messages;
    try {
      if (action.isQueue())
        messages = cache.getMessages(this.getHttpSession(), action.isRetrieveAll(), action.getName());
      else
        messages = cache.getSubMessages(this.getHttpSession(), action.isRetrieveAll(), action.getName());
    } catch (Exception e) {
      return new LoadMessageResponse(null, action.getName(), false);
    }
    return new LoadMessageResponse(messages, action.getName(), true);
  }

}
