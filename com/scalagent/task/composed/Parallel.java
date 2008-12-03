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
package com.scalagent.task.composed;

import java.io.*;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.*;
import com.scalagent.scheduler.Condition;
import com.scalagent.task.*;

/**
 * <code>Composed</code> task with a parallel order of execution of sub-tasks.
 */
public class Parallel extends Composed {
  /**
   * Default constructor
   */
  public Parallel() {
    super();
  }

  /**
   * Creates an agent to be deployed remotely.
   *
   * @param to		agent server id where agent is to be deployed
   * @param parent	agent to report status to
   * @param child	children tasks ids
   */
  public Parallel(short to, AgentId parent, AgentId child[]) {
    super(to, parent, child);
  }

  /**
   * Starts sub-tasks that can be started (all their dependent tasks
   * have completed).
   */
  protected void startSubtasks() throws Exception {
    int i;
    for (i = 0; i < child.length; i ++) {
      if (child[i].status != Status.WAIT)
	break;
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   getName() + ", " + " start sub-task #" + i);
      if (parameter != null)
	sendTo(child[i].id, new ParamNotification(parameter));
      sendTo(child[i].id, new Condition());
      child[i].status = Status.RUN;
    }
  }

  /**
   * Builds the result object when this task completes.
   * <p>
   * Returns the array of sub-tasks results, or null if all results are null.
   *
   * @return	the result object of the task
   */
  protected Object buildResult() throws Exception {
    boolean nullReturn = true;
    for (int i = child.length; i-- > 0;) {
      if (child[i].result != null) {
	nullReturn = false;
	break;
      }
    }
    if (nullReturn)
      return null;

    Object result[] = new Object[child.length];
    for (int i = child.length; i-- > 0;)
      result[i] = child[i].result;
    return result;
  }
}
