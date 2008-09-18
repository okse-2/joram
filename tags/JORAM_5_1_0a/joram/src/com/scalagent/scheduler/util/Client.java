/*
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
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
package com.scalagent.scheduler.util;

import fr.dyade.aaa.agent.* ;
import com.scalagent.scheduler.* ;
import com.scalagent.scheduler.event.* ;

/**
 * This class enables a <code>Client</code> agent to program the Scheduler
 * with a scheduling event date described by a cron like string 
 * The configurator proceeds to the programming of the scheduler
 * when the agent is deployed
 * assuming that there is only one default scheduler in the agent server.
 * <br>
 * Should be in <code>SchedulingCondition</code>.
 *
 * @see		Scheduler
 * @see		CronEvent
 * @see		Condition
 */
public class Client extends Agent {
  /** CronEvent for scheduling */
  private CronEvent crone ;

  /** the condition the scheduler is supposed to send */
  private String schedulerCondition ;

  /** cron like string representing the event scheduling date */
  private String frequency ;

  /**
    * Creates an agent to be configured.
    *
    * @param to		target agent server
    * @param name	symbolic name of this agent
    */
  public Client(short to, String name, String condition, String frequency) {
    super(to, name) ;
    setSchedulerCondition(condition) ;
    setFrequency(frequency) ;
  }    

  /**
   * Constructor invoked by the configurator at deployment stage.
   */
  public Client(short to, String name) {
    super(to, name) ;
  }

  /**
   * Initializes the Client.
   * Creates a <code>CronEvent</cod> and send it to the default scheduler
   *
   * @param firstTime	<code>true</code> when agent server starts a new Client
   */
  protected void agentInitialize(boolean firstTime) throws Exception {
    super.agentInitialize(firstTime);
    if (firstTime) {     
      crone = new CronEvent(schedulerCondition, this.frequency);   
      sendTo(Scheduler.getDefault(), crone);
    }
  }


  /**
   * Property accessor.
   * 
   * @param condition	condition the scheduler must send
   */
  public void setSchedulerCondition(String schedulerCondition) {
    this.schedulerCondition = schedulerCondition ;
  }


  /**
   * Property accessor.
   * 
   * @param frequency	cron like string to determine the next scheduled date
   */
  public void setFrequency(String frequency) {
    this.frequency = frequency ;
  }


}
