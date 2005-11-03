/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - France Telecom R&D
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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package org.objectweb.joram.mom.dest;

import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.joram.mom.MomTracing;
import org.objectweb.joram.mom.dest.Queue;
import org.objectweb.joram.mom.notifications.WakeUpNot;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.util.Timer;
import fr.dyade.aaa.util.TimerTask;

/**
 * A <code>ClusterQueue</code> agent is an agent hosting a MOM queue, and which
 * behaviour is provided by a <code>ClusterQueueImpl</code> instance.
 *
 * @see ClusterQueueImpl
 */
public class ClusterQueue extends Queue {

  /** period to eval the loading factor */
  private long period = -1;
  /** producer threshold */
  private int producThreshold = -1;
  /** consumer threshold */
  private int consumThreshold = -1;
  /** automatic eval threshold */
  private boolean autoEvalThreshold;
  /** waiting after a cluster request */
  private long waitAfterClusterReq = -1;

  /** use to schedule event */
  transient private Timer timer = null;

  /**
   * Empty constructor for newInstance(). 
   */ 
  protected ClusterQueue() {
    if (period != -1) {
      if (timer == null)
        timer = new fr.dyade.aaa.util.Timer();
      try {
        timer.schedule(new Task(getId()), period);
      } catch (Exception exc) {
        if (MomTracing.dbgDestination.isLoggable(BasicLevel.ERROR))
          MomTracing.dbgDestination.log(BasicLevel.ERROR,
                                        "--- " + this +
                                        " ClusterQueue(...)",
                                        exc);
      }
    }
  }

//   /**
//    * Constructs a <code>ClusterQueue</code> agent. 
//    *
//    * @param adminId  Identifier of the agent which will be the administrator
//    *          of the queue.
//    */ 
//   public ClusterQueue(AgentId adminId,
//                       long period,
//                       int producThreshold,
//                       int consumThreshold,
//                       boolean autoEvalThreshold,
//                       long waitAfterClusterReq) {
//     queueImpl = new ClusterQueueImpl(getId(),
//                                      adminId,
//                                      period,
//                                      producThreshold,
//                                      consumThreshold,
//                                      autoEvalThreshold,
//                                      waitAfterClusterReq);
//     if (period != -1) {
//       if (timer == null)
//         timer = new fr.dyade.aaa.util.Timer();
//       try {
//         timer.schedule(new Task(getId()), period);
//       } catch (Exception exc) {
//         if (MomTracing.dbgDestination.isLoggable(BasicLevel.ERROR))
//           MomTracing.dbgDestination.log(BasicLevel.ERROR,
//                                         "--- " + this +
//                                         " ClusterQueue(...)",
//                                         exc);
//       }
//     }
//   }

  /**
   * Creates the <tt>QueueImpl</tt>.
   *
   * @param adminId  Identifier of the queue administrator.
   */
  public DestinationImpl createsImpl(AgentId adminId) {
    ClusterQueueImpl queueImpl = new ClusterQueueImpl(getId(),
                                                      adminId,
                                                      period,
                                                      producThreshold,
                                                      consumThreshold,
                                                      autoEvalThreshold,
                                                      waitAfterClusterReq);
    if (period != -1) {
      if (timer == null)
        timer = new fr.dyade.aaa.util.Timer();
      try {
        timer.schedule(new Task(getId()), period);
      } catch (Exception exc) {
        if (MomTracing.dbgDestination.isLoggable(BasicLevel.ERROR))
          MomTracing.dbgDestination.log(BasicLevel.ERROR,
                                        "--- " + this +
                                        " ClusterQueue(...)",
                                        exc);
      }
    }

    return queueImpl;
  }

  public void setProperties(Properties prop) {
    try {
      period = Long.valueOf(prop.getProperty("period")).longValue();
    } catch (NumberFormatException exc) {
      period = 600000;
    }
    try {
      waitAfterClusterReq = 
        Long.valueOf(prop.getProperty("waitAfterClusterReq")).longValue();
    } catch (NumberFormatException exc) {
      waitAfterClusterReq = 60000;
    }
    try {
      producThreshold = 
        Integer.valueOf(prop.getProperty("producThreshold")).intValue();
    } catch (NumberFormatException exc) {
      producThreshold = 10000;
    }
    try {
      consumThreshold = 
        Integer.valueOf(prop.getProperty("consumThreshold")).intValue();
    } catch (NumberFormatException exc) {
      consumThreshold = 10000;
    }

    autoEvalThreshold =
      Boolean.valueOf(prop.getProperty("autoEvalThreshold")).booleanValue();
  }

  public void react(AgentId from, Notification not) throws Exception {
    if (not instanceof WakeUpNot) {
      if (timer == null)
        timer = new fr.dyade.aaa.util.Timer();
      timer.schedule(new Task(getId()), period);
      destImpl.react(from,not);
    } else 
      super.react(from, not);
  }

  private class Task extends TimerTask {
    private AgentId to;

    private Task(AgentId to) {
      this.to = to;
    }
    
    /** Method called when the timer expires. */
    public void run() {
      try {
        Channel.sendTo(to, new WakeUpNot());
      } catch (Exception e) {}
    }
  }
}
