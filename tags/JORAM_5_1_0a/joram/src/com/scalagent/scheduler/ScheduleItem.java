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
package com.scalagent.scheduler;

import java.io.*;
import java.util.*;

/**
 * Implements a double linked list of <code>ScheduleEvent</code> objects.
 * Both ends are marked with a <code>null</code> value. Events are ordered in
 * increasing order of dates.
 * <p>
 * The <code>status</code> field associated with the <code>ScheduleEvent</code>
 * is used by the <code>Scheduler</code> agent, and is set to <code>true</code>
 * when the <code>true</code> <code>Condition</code> notification has been sent
 * and the <code>Scheduler</code> agent waits for the duration to expire before
 * sending the <code>false</code> condition and resetting the field.
 *
 * @see		Scheduler
 * @see		ScheduleEvent
 */
public class ScheduleItem implements Serializable {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  /** may be of a derived class */
  ScheduleEvent event;
  /** next schedule date */
  Date date;
  /** last sent condition status */
  boolean status;
  /** previous item, null terminated */
  ScheduleItem prev;
  /** next item, null terminated */
  ScheduleItem next;

  /**
   * Creates an item.
   *
   * @param event	event to schedule
   */
  public ScheduleItem(ScheduleEvent event) {
    this.event = event;
    date = null;
    status = false;
    prev = null;
    next = null;
  }

  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    StringBuffer output = new StringBuffer();
    output.append("(");
    for (ScheduleItem item = this; item != null; item = item.next) {
      output.append("(event=");
      output.append(item.event.toString());
      output.append(",date=");
      output.append(item.date);
      output.append(",status=");
      output.append(item.status);
      output.append("),");
    }
    output.append("null)");
    return output.toString();
  }
}
