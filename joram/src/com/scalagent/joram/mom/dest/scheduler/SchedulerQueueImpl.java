package com.scalagent.joram.mom.dest.scheduler;

import java.util.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import fr.dyade.aaa.agent.*;

import org.objectweb.joram.mom.dest.*;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.joram.mom.notifications.*;

import com.scalagent.scheduler.*;

import org.objectweb.util.monolog.api.*;

public class SchedulerQueueImpl extends QueueImpl {
    public static Logger logger = Debug.getLogger(
      "com.scalagent.joram.scheduler.SchedulerQueueImpl");

    public static final String SCHEDULE_DATE = "scheduleDate";

    public static final String SCHEDULED = "scheduled";
    
    public SchedulerQueueImpl(AgentId destId, AgentId adminId) {
	super(destId, adminId);
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
	    try {
		long scheduleDate = msg.getLongProperty("scheduleDate");
		//DF: to improve
		// two notifs are necessary to subscribe
		Channel.sendTo(Scheduler.getDefault(),
			       new AddConditionListener(msg.getIdentifier()));
		Channel.sendTo(Scheduler.getDefault(),
			       new ScheduleEvent(msg.getIdentifier(),
						 new Date(scheduleDate)));
	    } catch (Exception exc) {
		logger.log(BasicLevel.ERROR, "", exc);
	    }
	}
    }

    public void react(AgentId from, Notification not)
	throws UnknownNotificationException {
	if (logger.isLoggable(BasicLevel.DEBUG))
	    logger.log(BasicLevel.DEBUG, 
		       "SchedulerQueueImpl.react(" + 
		       from + ',' + not + ')');
	if (not instanceof Condition) {
	    doReact((Condition) not);
	} else super.react(from, not);
    }

    private void doReact(Condition not) {
	String msgId = not.name;
	for (int i = 0; i < messages.size(); i++) {
	    Message msg = (Message)messages.elementAt(i);
	    if (msg.getIdentifier().equals(msgId)) {
		msg.resetPropertiesRO();
		try {
		    msg.setStringProperty(SCHEDULED, "" + System.currentTimeMillis());
		} catch (Exception exc) {}
		msg.setReadOnly();
		// The message is scheduled.
		// Save it again in order to make the scheduling persistent.
		// Only the header is saved.
		// The body should not be saved again.
		msg.save(getDestinationId());
		break;
	    }
	}
	deliverMessages(0);
    }

    protected boolean checkDelivery(Message msg) {
	if (logger.isLoggable(BasicLevel.DEBUG))
	    logger.log(BasicLevel.DEBUG, 
		       "SchedulerQueueImpl.checkDelivery(" + msg + ')');
	Object scheduleDate = msg.getObjectProperty(SCHEDULE_DATE);
	if (scheduleDate == null) {
	    if (logger.isLoggable(BasicLevel.DEBUG))
		logger.log(BasicLevel.DEBUG, "no schedule date");
	    return true;
	} else {
	    Object scheduledDate = msg.getObjectProperty(SCHEDULED);
	    if (logger.isLoggable(BasicLevel.DEBUG))
		logger.log(BasicLevel.DEBUG, "scheduledDate = " + scheduledDate);
	    return (scheduledDate != null);
	}
    }
}
