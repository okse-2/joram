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

import fr.dyade.aaa.agent.*;

import java.io.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

/**
 * This object is the input stream of a <code>Scheduler</code> agent.
 * It produces a <code>ScheduleNotification</code> notification when its timer
 * falls.
 *
 * @see		Scheduler
 * @see		ScheduleNotification
 */
public class SchedulerAlarm implements NotificationInputStream {
  public static Logger logger = Debug.getLogger(SchedulerAlarm.class.getName());
  
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
    synchronized (this) {
      waitLoop: while (true) {
        while (time == 0) {
          try {
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, "SchedulerAlarm indefinitely waits");
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
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "SchedulerAlarm waits " + localTime);
          wait(localTime);
        } catch (InterruptedException exc) {
          // ignores interrupts
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "SchedulerAlarm interrupted", exc);
        }
        if (time == 0) {
          // timer has fallen
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "SchedulerAlarm break loop");
          break waitLoop;
        } else {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "SchedulerAlarm loops");
        }
      }
    }

    // sends notification
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "SchedulerAlarm send notif");
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
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "SchedulerAlarm.setTime(" +
          	time + ')');
    this.time = time;
    notify();
  }

  /**
   * Closes the stream.
   * <p>
   * This function is called from main scheduler thread.
   */
  public void close() throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "SchedulerAlarm.close()");
    synchronized(this) {
      this.time = -1;
      notify();
    }
  }
  
}
