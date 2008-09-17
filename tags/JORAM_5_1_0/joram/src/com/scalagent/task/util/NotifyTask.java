/*
 * Copyright (C) 2001 - 2005 ScalAgent Distributed Technologies
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
package com.scalagent.task.util;

import fr.dyade.aaa.agent.*;
import com.scalagent.task.Task;
import com.scalagent.task.Task.Status;

/**
 * <code>Task</code> whose goal is to send a notification.
 */
public class NotifyTask extends Task {
  /** agent to send notification to */
  protected AgentId target;
  /** notification to send to target */
  protected Notification notification;

  /**
   * Creates an agent to be deployed remotely.
   *
   * @param to		 agent server id where agent is to be deployed
   * @param parent	 agent to report status to
   * @param target	 agent to send notification to
   * @param notification notification to send to target
   */
  public NotifyTask(short to, AgentId parent, AgentId target, Notification notification) throws Exception {
    super(to, parent);
    this.target = target;
    this.notification = notification;
  }

  
  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",target=" + target +
      ",notification=" + notification + ")";
  }

  /**
   * Starts program execution, overloads <code>start</code> from base class.
   */
  protected void start() throws Exception {
    setStatus(Status.RUN);
    sendTo(target, notification);
    setStatus(Status.DONE);
  }

  /**
   * Stops task execution.
   * This function must ensure that <code>setStatus(Status.DONE/FAIL/STOP)</code>
   * is eventually called.
   */
  protected void taskStop() throws Exception {
    // should never be called
  }
}
