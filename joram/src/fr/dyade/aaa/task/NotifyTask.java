/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 */

package fr.dyade.aaa.task;

import fr.dyade.aaa.agent.*;


/**
 * <code>Task</code> whose goal is to send a notification.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 */
public class NotifyTask extends Task {

public static final String RCS_VERSION="@(#)$Id: NotifyTask.java,v 1.1 2002-03-06 16:52:20 joram Exp $"; 


  /** agent to send notification to */
  protected AgentId target;
  /** notification to send to target */
  protected Notification notification;

  /**
   * Creates an agent to be deployed remotely.
   *
   * @param to		 agent server id where agent is to be deployed
   * @param parent	 agent to report status to
   * @param target	 agent to send notification to
   * @param notification notification to send to target
   */
  public NotifyTask(short to, AgentId parent, AgentId target, Notification notification) throws Exception {
    super(to, parent);
    this.target = target;
    this.notification = notification;
  }

  
  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",target=" + target +
      ",notification=" + notification + ")";
  }

  /**
   * Starts program execution, overloads <code>start</code> from base class.
   */
  protected void start() throws Exception {
    setStatus(Status.RUN);
    sendTo(target, notification);
    setStatus(Status.DONE);
  }

  /**
   * Stops task execution.
   * This function must ensure that <code>setStatus(Status.DONE/FAIL/STOP)</code>
   * is eventually called.
   */
  protected void taskStop() throws Exception {
    // should never be called
  }
}
