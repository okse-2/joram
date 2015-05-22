/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2010 ScalAgent Distributed Technologies
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

import java.util.Date;
import java.util.Iterator;

import org.objectweb.joram.mom.dest.Queue;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import com.scalagent.scheduler.ScheduleEvent;
import com.scalagent.scheduler.Scheduler;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.Debug;
import fr.dyade.aaa.agent.Notification;

/**
 * This class overrides the default Queue behavior in order to allow timed
 * deliveries.
 * When such a queue receives a message with a property called 'scheduleDate'
 * then the message is not available for delivery before the specified date.
 */
public class SchedulerQueue extends Queue {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  public static Logger logger = Debug.getLogger(SchedulerQueue.class.getName());

  public static final String SCHEDULE_DATE = "scheduleDate";

  public static final String SCHEDULED = "scheduled";
  
  // TODO (AF): The scheduler could be transient and initialized from the message list
  // at each start.
  private Scheduler scheduler = null;

  public SchedulerQueue() {
    fixed = true;
  }

  /**
   * Initializes the destination.
   * 
   * @param firstTime   true when first called by the factory
   */
  public void initialize(boolean firstTime) {
    super.initialize(firstTime);

    try {
      if (scheduler == null)
        scheduler = new Scheduler(AgentServer.getTimer());
      else
        scheduler.restart(AgentServer.getTimer());
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "SchedulerQueue.initialize(" + firstTime + ')', exc);
    }
  }

  public void react(AgentId from, Notification not) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "SchedulerQueue.react(" + from + ',' + not + ')');

    if (not instanceof SchedulerQueueNot) {
      condition((SchedulerQueueNot) not);
    } else
      super.react(from, not);
  }

  public void postProcess(ClientMessages not) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "SchedulerQueue.postProcess(" + not + ')');

    org.objectweb.joram.shared.messages.Message msg;
    long currentTimeMillis = System.currentTimeMillis();
    for (Iterator msgs = not.getMessages().iterator(); msgs.hasNext();) {
      msg = (org.objectweb.joram.shared.messages.Message) msgs.next();
      long scheduleDate = getScheduleDate(msg);
      // If there is no schedule date or if it is outdated  do nothing.
      if (scheduleDate < currentTimeMillis) return;
      
      // schedule a task
      try {
        scheduler.scheduleEvent(new ScheduleEvent(msg.id, new Date(scheduleDate)), 
                                new SchedulerQueueTask(getId()));
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR, "SchedulerQueue.postProcess(" + not + ')', e);
      }
    }
  }

  private static long getScheduleDate(org.objectweb.joram.shared.messages.Message msg) {
    Object scheduleDateValue = msg.getProperty(SCHEDULE_DATE);
    if (scheduleDateValue == null) return -1;
    try {
      return ((Long)scheduleDateValue).longValue();
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, "Scheduled message error", exc);
      return -1;
    }
  }

  private void condition(SchedulerQueueNot not) {
    deliverMessages(0);
  }

  protected boolean checkDelivery(org.objectweb.joram.shared.messages.Message msg) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "SchedulerQueue.checkDelivery(" + msg + ')');
    
    if (getScheduleDate(msg) <= System.currentTimeMillis()) {
      return true;
    }
    return false;
  }
  
  public int getEncodableClassId() {
    // Not defined: still not encodable
    return -1;
  }
  
}
