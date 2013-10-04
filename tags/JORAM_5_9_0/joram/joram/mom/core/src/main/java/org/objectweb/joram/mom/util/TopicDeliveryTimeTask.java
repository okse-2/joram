/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2013 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.util;

import java.util.List;

import org.objectweb.joram.mom.notifications.TopicDeliveryTimeNot;
import org.objectweb.joram.shared.messages.Message;

import com.scalagent.scheduler.ScheduleTask;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;

/**
 * Task sending a TopicDeliveryTimeNot to a UserAgent.
 */
public class TopicDeliveryTimeTask implements ScheduleTask {
  private static final long serialVersionUID = 1L;
  
  private AgentId destId = null;
  private Message msg = null;
  private List<String> subNames = null;
  private AgentId topic = null;
  
  public TopicDeliveryTimeTask(AgentId destId, AgentId topic, Message msg, List<String> subNames) {
    this.destId = destId;
    this.msg = msg;
    this.subNames = subNames;
    this.topic= topic;
  }
  
  /**
   * Task to execute: send a TopicDeliveryTimeNot to the related UserAgent.
   * 
   * @see com.scalagent.scheduler.ScheduleTask#run()
   */
  public void run() {
    Channel.sendTo(destId, new TopicDeliveryTimeNot(msg, subNames, topic));
  }

}
