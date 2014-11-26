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
package com.scalagent.appli.client.command.queue;

import com.scalagent.appli.server.command.queue.SendNewQueueActionImpl;
import com.scalagent.appli.shared.QueueWTO;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;

/**
 * This action send a new message to the server.
 * 
 * @author Yohann CINTRE
 */
@CalledMethod(value = SendNewQueueActionImpl.class)
public class SendNewQueueAction implements Action<SendNewQueueResponse> {

  private QueueWTO queue;

  public SendNewQueueAction() {
  }

  public SendNewQueueAction(QueueWTO queue) {
    this.queue = queue;
  }

  public QueueWTO getQueue() {
    return queue;
  }

}
