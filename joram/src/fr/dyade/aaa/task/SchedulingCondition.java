/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 */

package fr.dyade.aaa.task;

import fr.dyade.aaa.agent.*;


/**
 * This class enables a <code>Task</code> agent to register against the
 * <code>Scheduler</code> agent at configuration time, without any specific
 * code in the registering agent.
 * <p>
 * The listening agent may actually not be a <code>Task</code> agent.
 * <p>
 * The variables are set by the configurator when the agent is deployed.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		Scheduler
 * @see		Task
 * @see		Condition
 */
public class SchedulingCondition extends Agent {

  /** RCS version number of this file: $Revision: 1.1 $ */
  public static final String RCS_VERSION="@(#)$Id: SchedulingCondition.java,v 1.1 2002-03-06 16:52:20 joram Exp $"; 


  /** the <code>Scheduler</code> agent */
  public AgentId scheduler;

  /** the listening <code>Task</code> agent */
  public final Role listener = new Role("listener");

  /** condition name in the <code>Scheduler</code> agent */
  public String schedulerCondition;

  /** condition name in the listening <code>Task</code> agent */
  public String listenerCondition;

  /**
   * when <code>true</code> sends a <code>ResetNotification</code> to the
   * listener when initializing, to initialize it at the status WAIT.
   * Defaults to <code>false</code>.
   */
  public boolean iniTask = false;

  /**
   * when <code>true</code> sends a <code>ResetNotification</code> to the
   * listener once it has failed or completed its task, to reinitialize it
   * automatically.
   * Defaults to <code>false</code>.
   */
  public boolean reiniTask = false;


  /**
   * Creates an agent to be deployed remotely.
   *
   * @param to		id of agent server to deploy the agent to
   * @param name	agent name
   */
  public SchedulingCondition(short to, String name) {
    super(to, name);
    schedulerCondition = null;
    listenerCondition = null;

  }

  /**
   * Gives this agent an opportunity to initialize after having been deployed,
   * and each time it is loaded into memory.
   *
   * @param firstTime	<code>true</code> when first called by the factory
   */
  protected void initialize(boolean firstTime) throws Exception {
    if (firstTime) {
      if(scheduler == null)
	scheduler = Scheduler.getDefault();

      // registers this agent in the <code>Scheduler</code> agent
      sendTo(scheduler, new AddConditionListener(schedulerCondition));

      sendTo(listener, new AddStatusListenerNot());

      // requests the task to get ready for execution
      if (iniTask)
	sendTo(listener, new ResetNotification());
    }
  }

  /**
   * Reacts to notifications.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   *                    either a Condition from scheduler
   *                    or     a StatusNotification from listener
   */
  public void react(AgentId from, Notification not) throws Exception {
    if (not instanceof Condition) {
      doReact(from, (Condition) not);

    } else if (not instanceof StatusNotification) {
      doReact((StatusNotification) not);

    } else {
      super.react(from, not);
    }
  }


  /**
   * Reacts to <code>Condition</code> notifications.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   */
  public void doReact(AgentId from, Condition not) throws Exception {
    // the condition is a <code>schedulerCondition</code> coming from
    // <code>scheduler</code>; forwards it to the listener
    sendTo(listener, new Condition(listenerCondition, not.status));
  }


   /**
   * Reacts to <code>StatusNotification</code> notifications.
   * In case of <code>CronEvent<code>, handles the reinitilization of
   * the listener status after completed execution (DONE) or failure (FAIL)
   *
   * @param not		notification to react to
   */
  public void doReact(StatusNotification not) throws Exception {
    if (reiniTask &&
	((not.getStatus() == Task.Status.DONE) ||
	 ( not.getStatus() == Task.Status.FAIL))) {
      sendTo(listener, new ResetNotification());
    }
  }


  /**
   *  setter used in the GCT
   */
  public void setListener(AgentId id) {
    listener.setListener(id);
  }

  /**
   *  setter used in the GCT
   */
  public void setIniTask(Boolean iniTask) {
    this.iniTask = iniTask.booleanValue();
  }

  /**
   *  setter used in the GCT
   */
  public void setReiniTask(Boolean reiniTask) {
    this.reiniTask = reiniTask.booleanValue();
  }

  /**
   *  setter used in the GCT
   */
  public void setScheduler(AgentId scheduler) {
    this.scheduler = scheduler;
  }

  /**
   *  setter used in the GCT
   */
  public void setSchedulerCondition(String skedcond) {
    schedulerCondition = skedcond;
  }

  /**
   *  setter used in the GCT
   */
  public void setListenerCondition(String listencond) {
    listenerCondition = listencond;
  }

}
