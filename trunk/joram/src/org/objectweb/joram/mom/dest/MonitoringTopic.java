/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.dest;

import java.util.Properties;

import org.objectweb.joram.mom.notifications.RegisterDestNot;
import org.objectweb.joram.mom.notifications.WakeUpNot;
import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.WakeUpTask;

/**
 * Agent of the monitoring topic. Schedules the monitoring.
 */
public class MonitoringTopic extends Topic {

  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  private transient WakeUpTask task;

  /**
   * Empty constructor for newInstance().
   */
  public MonitoringTopic() {
  }
  
  public static void init(String args, boolean firstTime) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MonitoringTopic.init(" + args + ',' + firstTime + ')');
    if (!firstTime)
      return;
    MonitoringTopic monitTopic = new MonitoringTopic();
    monitTopic.setName("JoramMonitoringTopic");
    monitTopic.init(null, null);
    monitTopic.deploy();
    
    RegisterDestNot regDestNot = new RegisterDestNot(monitTopic.getId(), monitTopic.getName(),
        MonitoringTopic.class.getName(), TOPIC_TYPE);
    Channel.sendTo(AdminTopic.getDefault(), regDestNot);
  }
  
  protected void agentInitialize(boolean firstTime) throws Exception {
    super.agentInitialize(firstTime);
    task = new WakeUpTask(getId(), WakeUpNot.class);
    task.schedule(((MonitoringTopicImpl) destImpl).getPeriod());
  }
  
  public void react(AgentId from, Notification not) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MonitoringTopic.react(" + from + ',' + not + ')');
    if (not instanceof WakeUpNot) {
      if (task == null)
        task = new WakeUpTask(getId(), WakeUpNot.class);
      task.schedule(((MonitoringTopicImpl) destImpl).getPeriod());
      ((MonitoringTopicImpl) destImpl).wakeUpNot((WakeUpNot) not);
    } else
      super.react(from, not);
  }

  public DestinationImpl createsImpl(AgentId adminId, Properties prop) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MonitoringTopic.createImpl()");
    DestinationImpl dest = new MonitoringTopicImpl(adminId, prop);
    return dest;
  }
  
}
