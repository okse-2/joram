/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2006 ScalAgent Distributed Technologies
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

import javax.jms.JMSException;
import javax.jms.MessageFormatException;

import org.objectweb.joram.mom.dest.QueueImpl;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.shared.excepts.MessageValueException;
import org.objectweb.joram.shared.messages.Message;

import com.scalagent.scheduler.AddConditionListener;
import com.scalagent.scheduler.Condition;
import com.scalagent.scheduler.RemoveConditionListener;
import com.scalagent.scheduler.ScheduleEvent;
import com.scalagent.scheduler.Scheduler;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.Debug;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.UnknownNotificationException;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

public class SchedulerQueueImpl extends QueueImpl {
  public static Logger logger = Debug.getLogger("com.scalagent.joram.scheduler.SchedulerQueueImpl");

  public static final String SCHEDULE_DATE = "scheduleDate";
    
  /**
   * Constructs a <code>SchedulerQueueImpl</code> instance.
   *
   * @param destId   Identifier of the agent hosting the queue.
   * @param adminId  Identifier of the administrator of the queue.
   * @param prop     The initial set of properties.
   */
  public SchedulerQueueImpl(AgentId destId, AgentId adminId, Properties prop) {
    super(destId, adminId, prop);
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "SchedulerQueueImpl.<init>(" + destId + ',' + adminId + ')');
  }

  protected void doProcess(ClientMessages not) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "SchedulerQueueImpl.doProcess(" + not + ')');
    super.doProcess(not);
    Message msg;
    for (Enumeration msgs = not.getMessages().elements();
         msgs.hasMoreElements();) {
      msg = (Message) msgs.nextElement();
        long scheduleDate = getScheduleDate(msg);
        if (scheduleDate < 0) return;
        //DF: to improve
        // two notifs are necessary to subscribe
        Channel.sendTo(Scheduler.getDefault(),
                       new AddConditionListener(msg.getIdentifier()));
        Channel.sendTo(Scheduler.getDefault(),
                       new ScheduleEvent(msg.getIdentifier(),
                                         new Date(scheduleDate)));
    }
  }

  private static long getScheduleDate(Message msg) {
    Object scheduleDateValue = msg.getObjectProperty(SCHEDULE_DATE);
    if (scheduleDateValue == null) return -1;
    try {
      return ((Long)scheduleDateValue).longValue();
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, "Scheduled message error", exc);
      return -1;
    }
  }

  public void react(AgentId from, Notification not)
      throws UnknownNotificationException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "SchedulerQueueImpl.react(" + from + ','
          + not + ')');
    if (not instanceof Condition) {
      doReact((Condition) not);
    } else
      super.react(from, not);
  }

  private void doReact(Condition not) {
    String msgId = not.name;
    for (int i = 0; i < messages.size(); i++) {
      Message msg = (Message) messages.elementAt(i);
      if (msg.getIdentifier().equals(msgId)) {
        // Must remove the condition
        Channel.sendTo(
            Scheduler.getDefault(), 
            new RemoveConditionListener(msg.getIdentifier()));
        break;
      }
    }
    deliverMessages(0);
  }

  protected boolean checkDelivery(Message msg) {
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
