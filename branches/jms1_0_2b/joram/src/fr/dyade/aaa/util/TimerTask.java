/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
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
 *
 * The present code contributor is ScalAgent Distributed Technologies.
 */
package fr.dyade.aaa.util;

/**
 * The <code>TimerTask</code> class is the base class for implementing
 * tasks run by a <code>Timer</code> instance.
 *
 * @see Timer
 */
public abstract class TimerTask
{
  /** The task's timer reference. */
  Timer timer;
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
  public boolean cancel()
  {
    synchronized (timer) {
      cancelled = true;

      if (timer.tasks.contains(this)) {
        timer.tasks.remove(this);

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
