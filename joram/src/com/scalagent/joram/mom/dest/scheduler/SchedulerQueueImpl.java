/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2008 ScalAgent Distributed Technologies
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
import java.util.Enumeration;
import java.util.Properties;

import org.objectweb.joram.mom.dest.QueueImpl;
import org.objectweb.joram.mom.messages.Message;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import com.scalagent.scheduler.AddConditionListener;
import com.scalagent.scheduler.Condition;
import com.scalagent.scheduler.RemoveConditionListener;
import com.scalagent.scheduler.ScheduleEvent;
import com.scalagent.scheduler.Scheduler;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Debug;

public class SchedulerQueueImpl extends QueueImpl {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  public static Logger logger =
      Debug.getLogger("com.scalagent.joram.scheduler.SchedulerQueueImpl");

  public static final String SCHEDULE_DATE = "scheduleDate";

  public static final String SCHEDULED = "scheduled";

  /**
   * Constructs a <code>SchedulerQueueImpl</code> instance.
   *
   * @param adminId  Identifier of the administrator of the queue.
   * @param prop     The initial set of properties.
   */
  public SchedulerQueueImpl(AgentId adminId, Properties prop) {
    super(adminId, prop);
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "SchedulerQueueImpl.<init>(" + getId() + ',' + adminId + ')');
  }

  public void postProcess(ClientMessages not) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "SchedulerQueueImpl.postProcess(" + not + ')');
    
    org.objectweb.joram.shared.messages.Message msg;
    for (Enumeration msgs = not.getMessages().elements(); msgs.hasMoreElements();) {
      msg = (org.objectweb.joram.shared.messages.Message) msgs.nextElement();
      long scheduleDate = getScheduleDate(msg);
      if (scheduleDate < 0) return;
      //DF: to improve
      // two notifs are necessary to subscribe
      forward(Scheduler.getDefault(), new AddConditionListener(msg.id));
      forward(Scheduler.getDefault(), new ScheduleEvent(msg.id, new Date(scheduleDate)));
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

  public void condition(Condition not) {
    String msgId = not.name;
    for (int i = 0; i < messages.size(); i++) {
      Message msg = (Message) messages.elementAt(i);
      if (msg.getIdentifier().equals(msgId)) {
        try {
          msg.setObjectProperty(SCHEDULED, "" + System.currentTimeMillis());
        } catch (Exception exc) {}
        // Must remove the condition
        forward(Scheduler.getDefault(), 
                new RemoveConditionListener(msg.getIdentifier()));
        break;
      }
    }
    deliverMessages(0);
  }

  protected boolean checkDelivery(org.objectweb.joram.shared.messages.Message msg) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "SchedulerQueueImpl.checkDelivery(" + msg + ')');
    long scheduleDate = getScheduleDate(msg);
    if (scheduleDate < 0) {
      return true;
    } else {
      long currentTime = System.currentTimeMillis();
      return !(scheduleDate > currentTime);
    }
  }
}
