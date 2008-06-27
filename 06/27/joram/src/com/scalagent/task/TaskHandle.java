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
package com.scalagent.task;

import java.io.*;

import fr.dyade.aaa.agent.*;

/**
 * Structure to keep management data associated with a sub-task.
 *
 * @see	Composed
 */
public class TaskHandle implements Serializable {
  /** sub-task identifier */
  public AgentId id;
  /** sub-task status, as known by parent */
  public int status;
  /** sub-task result, when status is DONE */
  public Object result;

  /**
   * Initializes object to be decoded.
   */
  public TaskHandle() {
    this(null);
  }

  /**
   * Initializes status to <code>NONE</code> and result to null.
   *
   * @param id		sub-task identifier
   */
  public TaskHandle(AgentId id) {
    this.id = id;
    status = Task.Status.NONE;
    result = null;
  }

  /**
   * Provides a string image for this object.
   *
   * @return	string image for this object
   */
  public String toString() {
    return "(" +
      "id=" + id +
      ",status=" + Task.Status.toString(status) +
      ",result=" + result + ")";
  }
}
