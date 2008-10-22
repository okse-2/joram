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

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.Debug;
import fr.dyade.aaa.util.Timer;
import fr.dyade.aaa.util.TimerTask;

/**
 *
 */
public class Scheduler implements Serializable {
 
  private static final long serialVersionUID = 1L;
  public static Logger logger = Debug.getLogger(Scheduler.class.getName());

  /** events list */
  private ScheduleItem items;
  /** the timer */
  private transient Timer timer;
  /** Current task schedule in Timer */
  private WakeUp wakeUp;

  /**
   * Creates the default scheduler.
   *
   * @param timer a ScalAgent timer.
   * @throws IOException
   */
  public Scheduler(Timer timer) throws IOException {
    items = null;
    this.timer = timer;
  }
  
  /**
   * restart scheduler.
   * 
   * @param timer a ScalAgent timer.
   * @throws Exception
   */
  public void restart(Timer timer) throws Exception {
    if (this.timer != null) {
      // cancel task
      cancel();
    }
    // set timer and check items.
    this.timer = timer;
    checkItems();
  }

  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "Scheduler (items=" + items + ")";
  }

  /**
   * schedule an event.
   * Calls <code>insertItem</code>.
   * Calls <code>checkItems</code>.
   * @param event		event to schedule.
   * @param task    task to execute.
   */
  public void scheduleEvent(ScheduleEvent event, ScheduleTask task) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Scheduler.scheduleEvent(" + event + ", " + task + ')');
    
    // inserts event in list
    insertItem(event, task);

    // checks for ripe items
    checkItems();
  }

  /**
   * insertItem <code>ScheduleEvent</code> event.
   *
   * @param event
   * @param task task to execute.
   */
  private void insertItem(ScheduleEvent event, ScheduleTask task) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Scheduler.insertItem(" + event + ", " + task + ')');
    
    Date now = new Date();
    ScheduleItem newItem = new ScheduleItem(event, task);

    // finds next scheduling date of event
    newItem.date = event.nextDate(now);

    // checks for an outdated scheduling
    if (newItem.date == null) {
      if (! event.outdatedRestart) return;
      newItem.date = now;
    }

    insertItem(newItem);
  }

  /**
   * Inserts an item in the list ordered by date.
   * Inserts at list head a null dated item.
   *
   * @param newItem		item to insert
   */
  private void insertItem(ScheduleItem newItem) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Scheduler.insertItem(" + newItem + ')');
    
    if (newItem.date == null) return;

    if (items == null) {
      items = newItem;
    } else {
      ScheduleItem prev = null;

      for (ScheduleItem item = items; item != null; item = item.next) {
        if (!newItem.date.after(item.date))
          break;
        prev = item;
      }
      if (prev == null) {
        if (items != null) {
          // cancel Wakeup Timer.
          cancel();
          
          newItem.next = items;
          items.prev = newItem;
        }
        items = newItem;
      } else {
        newItem.next = prev.next;
        newItem.prev = prev;
        prev.next = newItem;
        if (newItem.next != null)
          newItem.next.prev = newItem;
      }
    }
  }

  /**
   * Checks for ripe events.
   * reschedule event.
   */
  private void checkItems() throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Scheduler.checkItems()");
    Date now = new Date();

    checkLoop:
      for (ScheduleItem item = items; item != null;) {
        if (item.date != null &&
            item.date.after(now))
          break checkLoop;

        ScheduleItem nextItem = item.next;
        
        // reschedules event
        item.date = item.event.nextDate(now);
        // this call must return a date later than now
        // for fear of an infinite loop
        if ((item.date != null) && ! item.date.after(now))
          item.date = null;

        if (item.date == null) {
          // removes event from list
          removeItem(item);
          item = nextItem;
          continue checkLoop;
        }

        // checks if event needs to be changed in the list
        if (nextItem == null ||
            ! item.date.after(nextItem.date)) {
          continue checkLoop;
        }

        // removes event from list
        removeItem(item);
        // and inserts it at its new place
        insertItem(item);

        item = nextItem;
      }

    // sets next wake-up time, schedule in Timer.
    if (items != null) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Scheduler.checkItems nextDate = " + items.date);
      
      schedule(items.event, items.date.getTime() - now.getTime());
    }
  }

  /**
   * Removes an item from the list.
   * Cancel WakeUP if the item is the current.
   *
   * @param item	item to remove
   */
  private void removeItem(ScheduleItem item) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Scheduler.removeItem(" + item + ')');
    
    if (item.next != null)
      item.next.prev = item.prev;
    if (item.prev == null)
      items = item.next;
    else
      item.prev.next = item.next;
    item.prev = item.next = null;
    
    //cancel(item.event);
  }
  
  
  /**
   * schedule wake up task in timer.
   * 
   * @param event  schedule event.
   * @param period period in ms.
   */
  private void schedule(ScheduleEvent event, long period) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Scheduler.schedule(" + event + ", " + period + ')');
    
    try {
      wakeUp = new WakeUp();
      timer.schedule(wakeUp, period);
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, "Exception in schedule", exc);
    }
  }
  
  /**
   * cancel a wake up task in timer.
   * 
   * @param event schedule event.
   */
  private void cancel() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Scheduler.cancel() wakeUp = " + wakeUp);
    
    try {
      wakeUp.cancel();
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Exception Scheduler.cancel wakeUp = " + wakeUp, exc);
    }
  }
  
  public class WakeUp extends TimerTask implements Serializable {
 
    private static final long serialVersionUID = 1L;

    public void run() {
      try {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "WakeUp.run");

        // the task is ready to execute, called run method.
        items.task.run();
        
        checkItems();
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "Exception :: WakeUp.run ", e);
      }
      
    }

  }
}
