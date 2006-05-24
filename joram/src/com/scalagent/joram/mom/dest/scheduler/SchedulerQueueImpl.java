package com.scalagent.joram.mom.dest.scheduler;

import java.util.Date;
import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.MessageFormatException;

import org.objectweb.joram.mom.dest.QueueImpl;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.shared.excepts.MessageValueException;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

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

public class SchedulerQueueImpl extends QueueImpl {
  public static Logger logger = Debug
      .getLogger("com.scalagent.joram.scheduler.SchedulerQueueImpl");

  public static final String SCHEDULE_DATE = "scheduleDate";

  public SchedulerQueueImpl(AgentId destId, AgentId adminId) {
    super(destId, adminId);
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "SchedulerQueueImpl.<init>(" + destId + ','
          + adminId + ')');
  }

  protected void doProcess(ClientMessages not) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "SchedulerQueueImpl.doProcess(" + not + ')');
    super.doProcess(not);
    Message msg;
    for (Enumeration msgs = not.getMessages().elements(); msgs
        .hasMoreElements();) {
      msg = (Message) msgs.nextElement();
      long scheduleDate = getScheduleDate(msg);
      if (scheduleDate < 0) return;
      //DF: to improve
      // two notifs are necessary to subscribe
      Channel.sendTo(Scheduler.getDefault(), new AddConditionListener(msg
          .getIdentifier()));
      Channel.sendTo(Scheduler.getDefault(), new ScheduleEvent(msg
          .getIdentifier(), new Date(scheduleDate)));
    }
  }
  
  private static long getScheduleDate(Message msg) {
    Object scheduleDateValue = msg.getObjectProperty(SCHEDULE_DATE);
    if (scheduleDateValue == null) return -1;
    try {
      return ((Long)scheduleDateValue).longValue();
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, 
          "Scheduled message error", exc);
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
      logger.log(BasicLevel.DEBUG, "SchedulerQueueImpl.checkDelivery(" + msg
          + ')');
    long scheduleDate = getScheduleDate(msg);
    if (scheduleDate < 0) {
      return true;
    } else {
      long currentTime = System.currentTimeMillis();
      return !(scheduleDate > currentTime);
    }
  }
}

