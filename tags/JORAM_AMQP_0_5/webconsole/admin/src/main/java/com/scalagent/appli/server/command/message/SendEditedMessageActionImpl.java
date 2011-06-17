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

import com.scalagent.appli.client.command.message.SendEditedMessageAction;
import com.scalagent.appli.client.command.message.SendEditedMessageResponse;
import com.scalagent.appli.server.RPCServiceImpl;
import com.scalagent.engine.server.command.ActionImpl;

/**
 * @author Yohann CINTRE
 */
public class SendEditedMessageActionImpl extends
    ActionImpl<SendEditedMessageResponse, SendEditedMessageAction, RPCServiceImpl> {

  @Override
  public SendEditedMessageResponse execute(RPCServiceImpl cache, SendEditedMessageAction action) {

    boolean result = cache.editMessage(action.getMessage(), action.getQueueName());

    String info = new String();

    if (result) {
      info = "The message \"" + action.getMessage().getId() + "\" has been updated on "
          + action.getQueueName();
    } else {
      info = "Error while updating message \"" + action.getMessage().getId() + "\" on "
          + action.getQueueName() + "";
    }

    return new SendEditedMessageResponse(result, info);
  }
}
