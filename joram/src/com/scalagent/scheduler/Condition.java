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
package com.scalagent.scheduler;

import fr.dyade.aaa.agent.*;


/**
 * Notification changing the status of a starting condition of a
 * <code>Task</code> agent.
 *
 * @see		Task
 */
public class Condition extends Notification {
  /** condition name, may be null */
  public String name;
  /** condition status */
  public boolean status;

  /**
   * Constructor.
   *
   * @param name		condition name, may be null
   * @param status		condition status
   */
  public Condition(String name, boolean status) {
    this.name = name;
    this.status = status;
  }

  /**
   * Constructor with default true status.
   *
   * @param name		condition name, may be null
   */
  public Condition(String name) {
    this(name, true);
  }

  /**
   * Constructor with default null name.
   *
   * @param status		condition status
   */
  public Condition(boolean status) {
    this(null, status);
  }

  /**
   * Constructor with default null name and true status.
   */
  public Condition() {
    this(null, true);
  }

  
  /**
   * Provides a string image for this object.
   *
   * @result		string image for this object
   */
  public StringBuffer toString(StringBuffer output) {
    output.append('(');
    output.append(super.toString(output));
    output.append(",name=");
    output.append(name);
    output.append(",status=");
    output.append(status);
    output.append(')');
    return output;
  }
}
