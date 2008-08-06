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

import fr.dyade.aaa.agent.*;
import java.io.*;

/**
 * Structure to keep management data associated with a task execution
 * condition.
 *
 * @see	Task
 */
public class ConditionHandle implements Serializable {
  /** condition name, may be <code>null</code> */
  public String name;
  /** agent sending condition */
  public AgentId id;
  /** condition status */
  public boolean status;

  /**
   * Initializes object to be decoded.
   */
  public ConditionHandle() {
    this(null, null);
  }

  /**
   * Constructor.
   * Initializes <code>status</code> to <code>false</code>.
   *
   * @param name	condition name, may be <code>null</code>
   * @param id		agent sending condition
   */
  public ConditionHandle(String name, AgentId id) {
    this.name = name;
    this.id = id;
    status = false;
  }

  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "(" +
      "name=" + name + "," +
      "id=" + id + "," +
      "status=" + status + ")";
  }
}
