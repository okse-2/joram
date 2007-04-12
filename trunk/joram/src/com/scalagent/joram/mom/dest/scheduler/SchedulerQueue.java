/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2007 ScalAgent Distributed Technologies
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
package com.scalagent.joram.mom.dest.scheduler;

import java.util.Properties;

import org.objectweb.joram.mom.dest.DestinationImpl;
import org.objectweb.joram.mom.dest.Queue;
import org.objectweb.util.monolog.api.BasicLevel;

import com.scalagent.scheduler.Condition;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;

public class SchedulerQueue extends Queue {
  public static final String QUEUE_SCHEDULER_TYPE = "queue_scheduler";

  public static String getDestinationType() {
    return QUEUE_SCHEDULER_TYPE;
  }
    
  public static void init(String args, boolean firstTime) throws Exception {
    if (! firstTime) return;
  }
    
  /**
   * Empty constructor for newInstance(). 
   */ 
  public SchedulerQueue() {}
    
  /**
   * Creates the <tt>QueueImpl</tt>.
   *
   * @param adminId  Identifier of the queue administrator.
   * @param prop     The initial set of properties.
   */
  public DestinationImpl createsImpl(AgentId adminId, Properties prop) {
    return new SchedulerQueueImpl(getId(), adminId, prop);
  }
  
  public void react(AgentId from, Notification not)
  throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
          "SchedulerQueue.react(" + from + ',' + not + ')');
    if (not instanceof Condition) {
      ((SchedulerQueueImpl) destImpl).condition((Condition) not);
    } else
      super.react(from, not);
  }
}

