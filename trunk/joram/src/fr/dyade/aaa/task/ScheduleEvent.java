/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */
package fr.dyade.aaa.task;

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
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		Scheduler
 * @see		Condition
 */
public class ScheduleEvent extends Notification {

public static final String RCS_VERSION="@(#)$Id: ScheduleEvent.java,v 1.2 2002-03-26 16:09:59 joram Exp $"; 


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
  public String toString() {
    return "(" + super.toString() +
      ",name=" + name +
      ",date=" + date +
      ",duration=" + duration +
      ",outdatedRestart=" + outdatedRestart + ")";
  }

  /**
   * Returns the next scheduling date after current date given as parameter.
   * The new date must be strictly greater than the current date.
   * A <code>null</code> date leads to the scheduler deleting the event.
   * <p>
   * This function should be overloaded in derived classes to actually implement
   * recurrent scheduling.
   *
   * @param now		current date
   * @return		next scheduling date after now
   */
  Date nextDate(Date now) {
    if (date == null)
      return null;
    if (date.after(now))
      return date;
    return null;
  }
}
