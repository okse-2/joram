package com.scalagent.joram.mom.dest.scheduler;

import org.objectweb.joram.mom.dest.*;
import org.objectweb.joram.shared.admin.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.BagSerializer;
import fr.dyade.aaa.agent.DeleteNot;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.UnknownNotificationException;

public class SchedulerQueue extends Destination {
  
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
     * Constructs a <code>Queue</code> agent. 
     *
     * @param adminId  Identifier of the agent which will be the administrator
     *          of the queue.
     */ 
    public SchedulerQueue(AgentId adminId) {
	super(adminId);
    }
    
    /**
     * Constructor with parameter for fixing the queue or not.
     */ 
    public SchedulerQueue(boolean fixed) {
	super(fixed);
    }
    
    /**
     * Creates the <tt>QueueImpl</tt>.
     *
     * @param adminId  Identifier of the queue administrator.
     */
    public DestinationImpl createsImpl(AgentId adminId) {
	return new SchedulerQueueImpl(getId(), adminId);
    }
}

