/*
 * Copyright (C) 2002 - 2008 ScalAgent Distributed Technologies
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
 * The present code contributor is ScalAgent Distributed Technologies.
 */
package fr.dyade.aaa.util;

/**
 * The <code>TimerTask</code> class is the base class for implementing tasks run
 * by a <code>Timer</code> instance.
 * 
 * @see Timer
 */
public abstract class TimerTask {
  /** The task's timer reference. */
  protected Timer timer;
  /** The task's wake up time. */
  long wakeupTime;
  /** <code>true</code> if the task is the next scheduled. */
  boolean waiting = false;
  /** <code>true</code> if the task has been cancelled. */
  boolean cancelled = false;

  /**
   * Cancels this task by removing it from the timer's tasks list, and
   * interrupting the timer thread if necessary.
   */
  public boolean cancel() {
    synchronized (timer) {
      cancelled = true;

      if (timer.tasks.contains(this)) {
        timer.tasks.removeElement(this);

        if (waiting)
          timer.daemon.interrupt();

        return true;
      }
      return false;
    }
  }

  /** Method called by the timer when the task is ready to execute. */
  public abstract void run();
}
