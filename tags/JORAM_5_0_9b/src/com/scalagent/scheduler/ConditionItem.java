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
import java.io.*;

/**
 * Structure to keep registered listeners for a scheduler condition.
 *
 * @see	Scheduler
 */
public class ConditionItem implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** condition name */
  String name;
  /** list of registered agents for this event */
  RoleMultiple listeners;
  /** next item, <code>null</code> terminated, in lexicographic order of name */
  ConditionItem next;

  /**
   * Initializes object to be decoded.
   */
  public ConditionItem() {
    this(null);
  }

  /**
   * Constructor.
   *
   * @param name	condition name
   * @param listeners	list of registered agents for this event
   */
  public ConditionItem(String name, RoleMultiple listeners) {
    this.name = name;
    if (listeners != null)
      this.listeners = listeners;
    else
      this.listeners = new RoleMultiple();
    next = null;
  }

  /**
   * Constructor with default <code>null</code> value for
   * <code>listeners<code>.
   *
   * @param name	condition name
   */
  public ConditionItem(String name) {
    this(name, null);
  }

  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    StringBuffer output = new StringBuffer();
    output.append("(");
    for (ConditionItem item = this; item != null; item = item.next) {
      output.append("(name=");
      output.append(item.name);
      output.append(",listeners=");
      output.append(item.listeners);
      output.append("),");
    }
    output.append("null)");
    return output.toString();
  }
}
