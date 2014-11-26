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
package com.scalagent.appli.client.command.message;

import com.scalagent.appli.server.command.message.DeleteMessageActionImpl;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;

/**
 * This action delete the message from a queue on the server.
 * 
 * @author Yohann CINTRE
 */
@CalledMethod(value = DeleteMessageActionImpl.class)
public class DeleteMessageAction implements Action<DeleteMessageResponse> {

  private String messageId;
  private String queueName;
  private boolean isQueue;

  public DeleteMessageAction() {
  }

  public DeleteMessageAction(String messageName, String queueName, boolean isQueue) {
    this.messageId = messageName;
    this.queueName = queueName;
    this.isQueue = isQueue;
  }

  public String getMessageId() {
    return messageId;
  }

  public String getQueueName() {
    return queueName;
  }

  public boolean isQueue() {
    return isQueue;
  }

}
