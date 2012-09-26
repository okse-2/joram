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
package com.scalagent.appli.client.command.topic;

import com.scalagent.appli.server.command.topic.DeleteTopicActionImpl;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.CalledMethod;

/**
 * This action delete a topic on the server.
 * 
 * @author Yohann CINTRE
 */
@CalledMethod(value = DeleteTopicActionImpl.class)
public class DeleteTopicAction implements Action<DeleteTopicResponse> {

  private String topicName;

  public DeleteTopicAction() {
  }

  public DeleteTopicAction(String topicName) {
    this.topicName = topicName;
  }

  public String getTopicName() {
    return topicName;
  }

}
