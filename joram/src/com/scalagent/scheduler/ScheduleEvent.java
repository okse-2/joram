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

import java.io.Serializable;
import java.util.Date;

/**
 * Base class for event requesting a scheduling to a
 * <code>Scheduler</code>.
 * <p>
 * The base class implements a one shot scheduling. 
 * This is true as long as the <code>Scheduler</code> is active at (about) 
 * the scheduling date and time. If it was down at that time, the outdated 
 * event is triggered only if its <code>outdatedRestart</code> field is true.
 * <p>
 * This class is also used by the <code>Scheduler</code> to keep the
 * request until it is complete.
 *
 * @see		Scheduler
 */
public class ScheduleEvent implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** event name */
  protected String name;
  /** event scheduling date */
  protected Date date;
  /** execute outdated event on restart */
  protected boolean outdatedRestart;


  /**
   * Creates an item.
   *
   * @param name		    event name
   * @param date		    event scheduling date
   * @param outdatedRestart	execute outdated event on restart
   */
  public ScheduleEvent(String name, Date date, boolean outdatedRestart) {
    this.name = name;
    this.date = date;
    this.outdatedRestart = outdatedRestart;
  }

  /**
   * Creates an item with a default value for <code>outdatedRestart</code>.
   * <p>
   * <code>outdatedRestart</code> is given a <code>true</code> value when
   *
   * @param name		    event name
   * @param date		    event scheduling date
   */
  public ScheduleEvent(String name, Date date) {
    this(name, date, true);
  }

  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public StringBuffer toString(StringBuffer output) {
    output.append("ScheduleEvent (name=");
    output.append(name);
    output.append(",date=");
    output.append(date);
    output.append(",outdatedRestart=");
    output.append(outdatedRestart);
    output.append(')');
    return output;
  }

  /**
   * Provides a string image for this object.
   * 
   * @return event string representation
   */
  public String toString() {
    StringBuffer buff = new StringBuffer();
    return toString(buff).toString();
  }
  
  /**
   * Returns the next scheduling date after current date given as parameter. The
   * new date must be strictly greater than the current date.
   * A <code>null</code> date leads to the scheduler deleting the event.
   * <p>
   * This function should be overloaded in derived classes to actually implement
   * recurrent scheduling.
   * 
   * @param now current date
   * @return next scheduling date after now
   */
  protected Date nextDate(Date now) {
    if (date == null)
      return null;
    if (date.after(now))
      return date;
    return null;
  }
}
