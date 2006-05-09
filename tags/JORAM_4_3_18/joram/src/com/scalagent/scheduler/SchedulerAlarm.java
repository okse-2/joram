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
 * This object is the input stream of a <code>Scheduler</code> agent.
 * It produces a <code>ScheduleNotification</code> notification when its timer
 * falls.
 *
 * @see		Scheduler
 * @see		ScheduleNotification
 */
public class SchedulerAlarm implements NotificationInputStream {
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
