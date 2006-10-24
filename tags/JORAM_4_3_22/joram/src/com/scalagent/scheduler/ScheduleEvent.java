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

import java.util.*;
import fr.dyade.aaa.agent.*;

/**
 * Base class for notifications requesting a scheduling to a
 * <code>Scheduler</code> agent. The event holds a name which is the name of
 * the condition the scheduler is requested to send.
 * <p>
 * The base class implements a one shot scheduling. A positive
 * <code>Condition</code> notification is sent at the event start date, and a
 * negative one is sent when the not null duration expires.
 * This is true as long as the <code>Scheduler</code> agent is active at
 * (about) the scheduling date and time. If the agent server was down at that
 * time, the outdated event is triggered only if its
 * <code>outdatedRestart</code> field is true.
 * <p>
 * This class is also used by the <code>Scheduler</code> agent to keep the
 * request until it is complete.
 *
 * @see		Scheduler
 * @see		Condition
 */
public class ScheduleEvent extends Notification {
  /** event and condition name */
  protected String name;
  /** event scheduling date */
  protected Date date;
  /** event duration in seconds */
  protected long duration;
  /** execute outdated event on restart */
  protected boolean outdatedRestart;


  /**
   * Creates an item.
   *
   * @param name		event and condition name
   * @param date		event scheduling date
   * @param duration		event duration in seconds
   * @param outdatedRestart	execute outdated event on restart
   */
  public ScheduleEvent(String name, Date date, long duration, boolean outdatedRestart) {
    this.name = name;
    this.date = date;
    this.duration = duration;
    this.outdatedRestart = outdatedRestart;
  }

  /**
   * Creates an item with a default value for <code>outdatedRestart</code>.
   * <p>
   * <code>outdatedRestart</code> is given a <code>true</code> value when
   * <code>duration</code> value is <code>0</code>.
   *
   * @param name		event and condition name
   * @param date		event scheduling date
   * @param duration		event duration in seconds
   */
  public ScheduleEvent(String name, Date date, long duration) {
    this(name, date, duration, duration == 0);
  }

  /**
   * Creates an item with null duration.
   *
   * @param name		event and condition name
   * @param date		event scheduling date
   */
  public ScheduleEvent(String name, Date date) {
    this(name, date, 0);
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
    output.append(",date=");
    output.append(date);
    output.append(",duration=");
    output.append(duration);
    output.append(",outdatedRestart=");
    output.append(outdatedRestart);
    output.append(')');
    return output;
  }

  /**
   * Returns the next scheduling date after current date given as parameter. The
   * new date must be strictly greater than the current date. A
   * <code>null</code> date leads to the scheduler deleting the event.
   * <p>
   * This function should be overloaded in derived classes to actually implement
   * recurrent scheduling.
   * 
   * @param now
   *          current date
   * @return next scheduling date after now
   */
  Date nextDate(Date now) {
    if (date == null)
      return null;
    if (date.after(now))
      return date;
    return null;
  }
}
