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
package com.scalagent.appli.server.command.queue;

import com.scalagent.appli.client.command.queue.SendEditedQueueAction;
import com.scalagent.appli.client.command.queue.SendEditedQueueResponse;
import com.scalagent.appli.server.RPCServiceCache;
import com.scalagent.engine.server.command.ActionImpl;

/**
 * @author Yohann CINTRE
 */
public class SendEditedQueueActionImpl extends
    ActionImpl<SendEditedQueueResponse, SendEditedQueueAction, RPCServiceCache> {

  @Override
  public SendEditedQueueResponse execute(RPCServiceCache cache, SendEditedQueueAction sendEditedQueueAction) {

    boolean result = cache.editQueue(sendEditedQueueAction.getQueue());

    String info = "";

    if (result) {
      info = "The Queue \"" + sendEditedQueueAction.getQueue().getName() + "\" has been updated.";
    } else {
      info = "Error while updating queue \"" + sendEditedQueueAction.getQueue().getName() + "\"";
    }

    return new SendEditedQueueResponse(result, info);
  }
}
