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

import java.util.List;

import com.scalagent.appli.shared.MessageWTO;
import com.scalagent.engine.client.command.Response;

/**
 * Response to the action LoadMessageAction.
 * 
 * @author Yohann CINTRE
 */
public class LoadMessageResponse implements Response {

  private List<MessageWTO> messages;
  private String queueName;
  private boolean success;

  public LoadMessageResponse() {
  }

  public LoadMessageResponse(List<MessageWTO> messages, String queueName, boolean success) {
    this.messages = messages;
    this.queueName = queueName;
    this.success = success;
  }

  public List<MessageWTO> getMessages() {
    return messages;
  }

  public String getQueueName() {
    return queueName;
  }

  public boolean isSuccess() {
    return success;
  }
}
