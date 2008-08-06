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

import java.io.*;
import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.*;

/**
 * A <code>Scheduler</code> agent sends <code>Condition</code> notifications
 * to agents registered for specific conditions, at dates and times specified
 * by <code>ScheduleEvent</code> objects.
 * <p>
 * Events in the scheduler are described by <code>ScheduleEvent</code> objects,
 * which hold a start date, a possibly not null duration, a condition name, and
 * a class specific function to get the next scheduling date of the event. The
 * <code>Scheduler</code> agent sends a <code>true</code> <code>Condition</code>
 * notification at the event start date, a <code>false</code> one at the event
 * end date if duration is not null, and then reschedules the event if the next
 * date, as provided by the event class specific function, is not
 * <code>null</code>. Events are stored in a double linked list of
 * <code>ScheduleItem</code> objects. Events are deleted by the scheduler as
 * soon as their next scheduling date is <code>null</code>.
 * <p>
 * The <code>Condition</code> notifications are sent to agents previously
 * registered. An agent registers in the scheduler for all the events with a
 * given condition name, by sending the scheduler a
 * <code>AddConditionListener</code> notification. The agent must register
 * itself.
 * <p>
 * Agents registering and events definition are two separate processes. There
 * may exist many events signaling the same condition. Or an agent may register
 * for a condition which no event yet signals at that time. An registered agent
 * must eventually unregister.
 * <p>
 * The <code>Scheduler</code> agent may be programmed by
 * <code>ScheduleEvent</code> notifications, or by a command line interface
 * through an associated <code>SchedulerProxy</code> agent.
 * The <code>SchedulerProxy</code> agent must be created and deployed
 * explicitely.
 * <p>
 * The scheduler blocks waiting for the next event to be started. This is
 * implemented using a <code>SchedulerAlarm</code> object as input stream of
 * the <code>ProxyAgent</code> base class. The stream is responsible for waking
 * up its scheduler by sending it a <code>ScheduleNotification</code>.
 * The scheduler controls its stream by synchronized function calls.
 * <p>
 * Note: there misses functions for administering the scheduled events.
 *
 * @see		Condition
 * @see		ScheduleEvent
 * @see		AddConditionListener
 * @see		RemoveConditionListener
 * @see		SchedulerAlarm
 * @see		SchedulerProxy
 */
public class Scheduler extends ProxyAgent {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public static Logger logger = Debug.getLogger(Scheduler.class.getName());
  
  /** initializes service only once */
  private static boolean initialized = false;

  /** Name of default scheduler, if any. */
  public static String defaultName = "DefaultScheduler";

  /**
   * Initializes the package as a well known service.
   * <p>
   * Creates a <code>Scheduler</code> agent with the well known stamp
   * <code>AgentId.SchedulerServiceStamp</code>.
   *
   * @param args	parameters from the configuration file
   * @param firstTime	<code>true</code> when agent server starts anew
   */
  public static void init(String args, boolean firstTime) throws Exception {
    if (initialized) return;
    initialized = true;

    if (! firstTime) return;

    Scheduler scheduler = new Scheduler();
    scheduler.deploy();
  }

  public static void stopService() {
    // Do nothing
  }

  /**
   * Gets the identifier of the default scheduler in an agent server.
   *
   * @param serverId	id of agent server
   * @return		id of default scheduler agent
   */
  public static AgentId getDefault(short serverId) {
    return new AgentId(serverId, serverId, AgentId.SchedulerServiceStamp);
  }

  /**
   * Gets the identifier of the default scheduler in this agent server.
   *
   * @param serverId	id of agent server
   * @return		id of default scheduler agent
   */
  public static AgentId getDefault() {
    return getDefault(AgentServer.getServerId());
  }

  /** events list */
  ScheduleItem items;
  /** registered listeners for each condition */
  ConditionItem conditions;

  /** object in charge of waking up this agent */
  transient SchedulerAlarm alarm;

  /**
   * Creates a local agent with unknown port.
   *
   * @param schedulerName	symbolic name of this agent
   */
  public Scheduler(String schedulerName) {
    super(schedulerName);
    blockingCnx = false;
    items = null;
    conditions = null;
    alarm = null;
  }

  /**
   * Creates the default scheduler agent with well known stamp
   * <code>AgentId.SchedulerServiceStamp</code>.
   */
  private Scheduler() throws IOException {
    super(defaultName, AgentId.SchedulerServiceStamp);
    blockingCnx = false;
    items = null;
    conditions = null;
    alarm = null;
  }

  /**
    * Creates an agent to be configured.
    *
    * @param to		target agent server
    * @param name	symbolic name of this agent
    */
  public Scheduler(short to, String name) {
    super(to, name);
    blockingCnx = false;
    items = null;
    conditions = null;
    alarm = null;
  }


  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",items=" + items +
      ",conditions=" + conditions + ")";
  }


  /**
   * Initializes the connection with the outside, up to creating
   * the input and output streams <code>ois</code> and <code>oos</code>.
   */
  public void connect() throws Exception {
    alarm = new SchedulerAlarm();
    ois = alarm;
  }

  /**
   * Closes the connection with the outside.
   */
  public void disconnect() throws IOException {
    if (alarm != null) {
      // stops alarm thread
      alarm.close();
      alarm = null;
      ois = null;
    }
  }


  /**
   * Inserts an item in the list, if it does not exist.
   *
   * @param name	name of condition item to insert.
   * @return		inserted or existing item
   */
  ConditionItem addCondition(String name) {
    if (conditions == null) {
      conditions = new ConditionItem(name);
      return conditions;
    }
    ConditionItem prev = null;
    for (ConditionItem item = conditions; item != null; item = item.next) {
      int cmp = name.compareTo(item.name);
      if (cmp == 0)
	return item;
      if (cmp < 0)
	break;
      prev = item;
    }
    
    ConditionItem newItem = new ConditionItem(name);
    if (prev == null) {
      newItem.next = conditions;
      conditions = newItem;
    } else {
      newItem.next = prev.next;
      prev.next = newItem;
    }

    return newItem;
  }

  /**
   * Finds an item in the list.
   *
   * @param name	name of condition item to find.
   * @return		existing item, or null
   */
  ConditionItem findCondition(String name) {
    if (conditions == null)
      return null;
    for (ConditionItem item = conditions; item != null; item = item.next) {
      int cmp = name.compareTo(item.name);
      if (cmp == 0)
	return item;
      if (cmp < 0)
	break;
    }
    
    return null;
  }

  /**
   * Removes an item from the list.
   *
   * @param name	name of condition item to remove.
   */
  void removeCondition(String name) {
    if (conditions == null)
      return;
    ConditionItem prev = null;
    for (ConditionItem item = conditions; item != null; item = item.next) {
      int cmp = name.compareTo(item.name);
      if (cmp == 0)
	break;
      if (cmp < 0)
	return;
      prev = item;
    }
    
    if (prev == null) {
      conditions = conditions.next;
    } else {
      prev.next = prev.next.next;
    }
  }

  /**
   * Initializes the transient members of this agent.
   * This function is first called by the factory agent,
   * then by the system each time the agent server is restarted.
   *
   * @param firstTime		true when first called by the factory
   */
  protected void agentInitialize(boolean firstTime) throws Exception {
    // initializes the alarm stream
    super.agentInitialize(firstTime);

    // reschedules all events
    checkItems(true);
  }

  /**
   * Reacts to <code>Scheduler</code> specific notifications.
   * Analyzes the notification type, then calls the appropriate
   * <code>doReact</code> function. By default calls <code>react</code>
   * from base class.
   * Handled notification types are :
   *	<code>ScheduleEvent</code> and derived types,
   *	<code>ScheduleNotification</code>,
   *	<code>AddConditionListener</code>,
   *	<code>RemoveConditionListener</code>,
   *	<code>AgentDeleteRequest</code>.
   * <p>
   * Note: management of <code>AgentDeleteRequest</code> notifications has been
   * removed to conform to the behaviour expected by the configurator agent.
   * This should be fixed.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   */
  public void react(AgentId from, Notification not) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Scheduler.react(" + from + ',' + not + ')');
    if (not instanceof ScheduleEvent) {
      doReact(from, (ScheduleEvent) not);
    } else if (not instanceof ScheduleNotification) {
      doReact(from, (ScheduleNotification) not);
    } else if (not instanceof AddConditionListener) {
      doReact(from, (AddConditionListener) not);
    } else if (not instanceof RemoveConditionListener) {
      doReact(from, (RemoveConditionListener) not);
      //    } else if (not instanceof AgentDeleteRequest) {
      //      doReact(from, (AgentDeleteRequest) not);
    } else {
      super.react(from, not);
    }
  }

  /**
   * Reacts to <code>ScheduleEvent</code> notifications.
   * Calls <code>insertItem</code>.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   */
  protected void doReact(AgentId from, ScheduleEvent not) throws Exception {
    // inserts event in list
    insertItem(not);

    // checks for ripe items
    checkItems(false);
  }

  /**
   * Reacts to <code>ScheduleNotification</code> notifications.
   * Calls <code>checkItems</code>.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   */
  protected void doReact(AgentId from, ScheduleNotification not) throws Exception {
    // checks for ripe items
    checkItems(false);
  }

  /**
   * Reacts to <code>AddConditionListener</code> notifications.
   * Calls <code>addConditionListener</code>.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   */
  protected void doReact(AgentId from, AddConditionListener not) throws Exception {
    addConditionListener(not.name, from);
  }

  /**
   * Reacts to <code>RemoveConditionListener</code> notifications.
   * Calls <code>removeConditionListener</code>.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   */
  protected void doReact(AgentId from, RemoveConditionListener not) throws Exception {
    removeConditionListener(not.name, from);
  }

  /**
   * Reacts to <code>AgentDeleteRequest</code> notifications.
   * Calls <code>delete</code>.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   */
  //  protected void doReact(AgentId from, AgentDeleteRequest not) throws Exception {
  //    delete();
  //  }

  /**
   * Reacts to <code>ScheduleEvent</code> notifications.
   *
   * @param not		notification to react to
   */
  protected void insertItem(ScheduleEvent not) throws Exception {
    Date now = new Date();
    ScheduleItem newItem = new ScheduleItem(not);

    // finds next scheduling date of event
    newItem.date = not.nextDate(now);

    // checks for an outdated scheduling
    if (newItem.date == null) {
      if (! not.outdatedRestart) return;
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
  protected void insertItem(ScheduleItem newItem) throws Exception {
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
   * Sends <code>Condition</code> notification and reschedule event.
   *
   * @param restart	<code>true</code> if called on restart
   */
  protected void checkItems(boolean restart) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Scheduler.checkItems(" + restart + ')');
    Date now = new Date();

    checkLoop:
    for (ScheduleItem item = items; item != null;) {
      if (item.date != null &&
	  item.date.after(now))
	break checkLoop;

      ScheduleItem nextItem = item.next;

      if (! restart ||
	  item.event.outdatedRestart ||
	  item.status ||	// closes event
	  (item.event.duration > 0 &&
	   new Date(item.date.getTime() +
		    (item.event.duration * 1000)).after(now))
	) {
	// signals event
	item.status = ! item.status;
	signalEvent(item);
      }

      // reschedules event
      if (item.status == true) {
	if (item.event.duration > 0) {
	  // finds event end date
	  item.date.setTime(item.date.getTime() + (item.event.duration * 1000));
	} else {
	  // sends no false condition when duration is null
	  item.status = ! item.status;
	}
      }
      if (item.status == false) {
	item.date = item.event.nextDate(now);
        // this call must return a date later than now
        // for fear of an infinite loop
        if ((item.date != null) && ! item.date.after(now))
          item.date = null;
      }

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

    // sets next wake-up time
    if (items != null) {
      alarm.setTime(items.date.getTime() - now.getTime());
    }
  }

  /**
   * Removes an item from the list
   *
   * @param item	item to remove
   */
  void removeItem(ScheduleItem item) {
    if (item.next != null)
      item.next.prev = item.prev;
    if (item.prev == null)
      items = item.next;
    else
      item.prev.next = item.next;
    item.prev = item.next = null;

    // checks if condition may be deleted
    ConditionItem citem = findCondition(item.event.name);
    if (citem == null)
      return;
    Enumeration listeners = citem.listeners.getListeners();
    if (listeners != null &&
	listeners.hasMoreElements())
      return;
    for (ScheduleItem sitem = items; sitem != null; sitem = sitem.next) {
      if (sitem.event.name.equals(item.event.name))
	return;
    }
    removeCondition(item.event.name);
  }

  /**
   * Adds an agent to the list of listeners for an event.
   *
   * @param condition	condition name of events to register to
   * @param listener	registering agent
   */
  protected void addConditionListener(String condition, AgentId listener) throws Exception {
    ConditionItem citem = addCondition(condition);
    citem.listeners.addListener(listener);

    for (ScheduleItem item = items; item != null; item = item.next) {
      if (! item.event.name.equals(condition))
	continue;
      if (item.status)
	sendTo(listener, new Condition(item.event.name, item.status));
    }
  }

  /**
   * Removes an agent from the list of listeners for an event.
   *
   * @param condition	condition name of events to unregister to
   * @param listener	unregistering agent
   */
  protected void removeConditionListener(String condition, AgentId listener) throws Exception {
    ConditionItem citem = findCondition(condition);
    if (citem == null)
      return;
    citem.listeners.removeListener(listener);

    // checks if condition may be deleted
    Enumeration listeners = citem.listeners.getListeners();
    if (listeners != null &&
	listeners.hasMoreElements())
      return;
    for (ScheduleItem item = items; item != null; item = item.next) {
      if (item.event.name.equals(condition))
	return;
    }
    removeCondition(condition);
  }

  /**
   * Signals a condition to registered agents.
   *
   * @param item	event to signal
   */
  protected void signalEvent(ScheduleItem item) {
    ConditionItem condition = findCondition(item.event.name);
    if (condition == null)
      return;
    sendTo(condition.listeners, new Condition(item.event.name, item.status));
  }
  
  /**
   * Overrides the ProxyAgent behavior in order not 
   * to react to DriverDone notifications.
   * Useless for the Scheduler.
   * Moreover it may stop the scheduler when restarting
   * the agent server after a stop.
   */
  protected void driverDone(DriverDone not) throws IOException { 

  }
}
