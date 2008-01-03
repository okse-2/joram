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
 * Notification requesting an agent to be unregistered for receiving <code>Condition</code> notifications
 * for a scheduled event.
 * An agent may only unregister itself.
 *
 * @see		Scheduler
 */
public class RemoveConditionListener extends Notification {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** condition name */
  String name;

  /**
   * Creates an item.
   *
   * @param name	condition name
   */
  public RemoveConditionListener(String name) {
    this.name = name;
  }


  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public StringBuffer toString(StringBuffer output) {
    output.append('(');
    output.append(super.toString(output));
    output.append(",name=");
    output.append(name);
    output.append(')');
    return output;
  }
}
