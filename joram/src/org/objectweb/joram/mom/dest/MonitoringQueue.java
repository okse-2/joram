/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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
import org.objectweb.joram.shared.DestinationConstants;
import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;

/**
 * Agent of the monitoring queue. 
 */
public class MonitoringQueue extends Queue {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /**
   * Empty constructor for newInstance().
   */
  public MonitoringQueue() {}
  
  protected void agentInitialize(boolean firstTime) throws Exception {
    super.agentInitialize(firstTime);
  }

  /**
   * Creates an instance of MonitoringQueue.
   * 
   * @see org.objectweb.joram.mom.dest.Queue#createsImpl(fr.dyade.aaa.agent.AgentId, java.util.Properties)
   */
  public DestinationImpl createsImpl(AgentId adminId, Properties prop) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MonitoringQueue.createImpl()");
    DestinationImpl dest = new MonitoringQueueImpl(adminId, prop);
    return dest;
  }
  
  private final static String JoramMonitoringQueue = "JoramMonitoringQueue";
  
  /**
   *  Static method allowing the creation of a default MonitoringQueue through a
   * service. This topic is registered with the name "JoramMonitoringQueue".
   * 
   * @param args        useless.
   * @param firstTime   The queue is created only at the first initialization.
   * @throws Exception  
   */
  public static void init(String args, boolean firstTime) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MonitoringQueue.init(" + args + ',' + firstTime + ')');
    
    if (!firstTime) return;
    
    MonitoringQueue queue = new MonitoringQueue();
    queue.setName(JoramMonitoringQueue);
    queue.init(null, null);
    queue.deploy();
    
    RegisterDestNot regDestNot = new RegisterDestNot(queue.getId(),
                                                     JoramMonitoringQueue,
                                                     MonitoringQueue.class.getName(),
                                                     DestinationConstants.QUEUE_TYPE);
    Channel.sendTo(AdminTopic.getDefault(), regDestNot);
  }
}
