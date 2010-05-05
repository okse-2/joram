/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 - ScalAgent Distributed Technologies
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
package fr.dyade.aaa.agent;

import java.util.Timer;
import java.util.TimerTask;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

/**
 * Class used to schedule a wake up on a specific agent. A notification is sent
 * to activate the agent.
 */
public class WakeUpTask extends TimerTask {

  private AgentId destId;
  private Logger logger;
  private Class wakeUpNot;
  private boolean schedule;

  /**
   * Creates a new WakeUpTask.
   * 
   * @param id
   *          the id of the agent to wake up.
   * @param wakeUpNotClass
   *          the notification which will be sent to the agent
   * @param period  period to wakeup.
   */
  public WakeUpTask(AgentId id, Class wakeUpNotClass, long period) {
    schedule = false;
    destId = id;
    wakeUpNot = wakeUpNotClass;
    logger = Debug.getLogger(getClass().getName());
    schedule(period);
  }

  public void run() {
    try {
      Channel.sendTo(destId, (Notification) wakeUpNot.newInstance());
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, "--- " + this, exc);
    }
  }

  /**
   * Schedules the wake up task for execution after the given period.
   * 
   * @param period Delay in ms before waking up.
   */
  public void schedule(long period) {
    // Don't schedule on HA slaves.
    if (AgentServer.isHAServer() && !AgentServer.isMasterHAServer())
      return;

    if (period > 0) {
      try {
        Timer timer = AgentServer.getTimer();
        if (!schedule) {
          timer.schedule(this, period, period);
          schedule = true;
        }
      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "--- " + this, exc);
      }
    }
  }
}
