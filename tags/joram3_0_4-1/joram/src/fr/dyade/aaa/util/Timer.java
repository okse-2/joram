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

import java.util.Vector;

/**
 * This class is a facility for scheduling tasks future execution.
 *
 * <p>It is a simplified version of the timer provided by the jdk1.3.
 */
public class Timer
{
  /** <code>true</code> if the timer has been cancelled. */
  private boolean cancelled = false;
  /** The timer's daemon. */
  TimerDaemon daemon;
  /** The timer's tasks. */
  Vector tasks;

  /** Constructs a <code>Timer</code> instance. */ 
  public Timer()
  {
    tasks = new Vector();
    daemon = new TimerDaemon(this);
  }

  /** 
   * Schedules a given task for execution after a given delay.
   *
   * @param task  The task to be executed.
   * @param delay  Delay in ms before executing the task.
   * @exception IllegalStateException  If the timer or the task have already
   *              been cancelled, or if the task is already scheduled.
   * @exception IllegalArgumentException  If the delay is negative.
   */
  public synchronized void schedule(TimerTask task, long delay)
                         throws Exception
  {
    if (cancelled)
      throw new IllegalStateException("Timer has been cancelled.");
    if (tasks.contains(task))
      throw new IllegalStateException("Task is already scheduled.");
    if (task.cancelled)
      throw new IllegalStateException("Task has been cancelled.");

    if (delay < 0)
      throw new IllegalArgumentException("Invalid negative delay: " + delay);
   
    long wakeupTime = System.currentTimeMillis() + delay;
    insertTask(task, wakeupTime);

    if (wakeupTime < daemon.nextWakeup)
      daemon.interrupt();
   
    if (! daemon.started)
      daemon.start();
  }

  /** Cancels the timer and all its non executed tasks. */
  public synchronized void cancel()
  {
    cancelled = true;

    if (! daemon.started)
      return;

    tasks.removeAllElements();

    daemon.interrupt();
  }

  /** Inserts a task in the timer's tasks list. */
  private void insertTask(TimerTask task, long wakeupTime)
  {
    task.timer = this;
    task.wakeupTime = wakeupTime;

    int i = 0;
    TimerTask currentTask;
    while (i < tasks.size()) {
      currentTask = (TimerTask) tasks.get(i);

      if (currentTask.wakeupTime > wakeupTime) {
        tasks.insertElementAt(task, i);
        break;
      }
      else
        i++;
    }
    if (i == tasks.size())
      tasks.addElement(task);
  }
}


/** The timer's daemon, actually scheduling the tasks. */
class TimerDaemon extends Daemon 
{
  /** The timer the daemon belongs to. */
  private Timer timer;
  /** <code>true</code> if the daemon is currently waiting. */
  private boolean waiting = false;
  /** The next wake up time of the daemon. */
  long nextWakeup = -1; 
  /** <code>true</code> if the daemon is actually started. */
  boolean started = false;


  /** Constructs a <code>TimerDaemon</code> instance for a given timer. */
  TimerDaemon(Timer timer)
  {
    super("timer");
    setDaemon(true);
    this.timer = timer;
  }

  /** Starts the daemon. */
  public void start()
  {
    super.start();
    started = true;
  }

  /** Daemon's loop. */
  public void run()
  { 
    try {
      TimerTask task = null;
      while (running) {
        canStop = true;
  
        synchronized (timer) {
          if (timer.tasks.isEmpty()) {
            started = false;
            break;
          }

          task = (TimerTask) timer.tasks.elementAt(0);
          nextWakeup = task.wakeupTime;
        }

        try {
          synchronized (this) {
            waiting = true;
            task.waiting = true;
            try {
              this.wait(nextWakeup - System.currentTimeMillis());
            }
            catch (IllegalArgumentException illAE) {}
          }
 
          synchronized (timer) {
            canStop = false;
            waiting = false;
            nextWakeup = -1;
            task.run();
            timer.tasks.remove(task);
          }
        }
        catch (InterruptedException iE0) {}
      }
    }
    catch (Exception e) {}
    finally {
      finish();
    }
  }

  /** Interrupts the thread. */
  public synchronized void interrupt()
  {
    if (waiting)
      thread.interrupt();
  }

  /** Shuts the daemon down. */
  public void shutdown()
  {}

  /** Releases the daemon's resources. */
  public void close()
  {}
}
