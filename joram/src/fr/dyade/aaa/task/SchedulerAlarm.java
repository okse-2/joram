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

import fr.dyade.aaa.agent.*;
import java.io.*;


/**
 * This object is the input stream of a <code>Scheduler</code> agent.
 * It produces a <code>ScheduleNotification</code> notification when its timer
 * falls.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		Scheduler
 * @see		ScheduleNotification
 */
public class SchedulerAlarm implements NotificationInputStream {

public static final String RCS_VERSION="@(#)$Id: SchedulerAlarm.java,v 1.2 2002-03-26 16:09:59 joram Exp $"; 


  /** time to sleep in milliseconds */
  long time;

  /**
   * Constructor.
   */
  public SchedulerAlarm() {
    time = 0;
  }

  /**
   * Gets a <code>Notification</code> from the stream.
   * <p>
   * This function is executed by the driver thread.
   *
   * @return	a <code>ScheduleNotification</code> notification when the
   *		timer falls
   */
  public Notification readNotification() throws ClassNotFoundException, IOException {
    synchronized(this) {
      waitLoop:
      while (true) {
	while (time == 0) {
	  try {
	    wait();
	  } catch (InterruptedException exc) {
	    // ignores interrupts
	  }
	}

	if (time < 0) {
	  // stops this thread, see close function
	  throw new EOFException();
	}

	// waits for time
	long localTime = time;
	time = 0;
	try {
	  wait(localTime);
	} catch (InterruptedException exc) {
	  // ignores interrupts
	}
	if (time == 0) {
	  // timer has fallen
	  break waitLoop;
	}
      }
    }

    // sends notification
    return new ScheduleNotification();
  }

  /**
   * Sets the timer.
   * <p>
   * This function is executed by the main scheduler thread.
   *
   * @param time	time to sleep in milliseconds
   */
  synchronized void setTime(long time) {
    this.time = time;
    notify();
  }

  /**
   * Closes the stream.
   * <p>
   * This function is called from main scheduler thread.
   */
  public void close() throws IOException {
    synchronized(this) {
      this.time = -1;
      notify();
    }
  }
}
