/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
*/

package fr.dyade.aaa.task;

import fr.dyade.aaa.agent.*;
import java.io.*;
import java.util.*;


/**
 * Notification reporting a status change in a <code>Task</code> agent.
 * <p>
 * This object identifies the <code>Task</code> agent changing status,
 * the new status, an optional message mainly used when the new status is
 * <code>FAIL</code>, and an optional result object when the new status is
 * <code>DONE</code>.
 * <p>
 * The result value is provided as an <code>Object</code> object, which
 * of course is specialized for specific <code>Task</code> agents. That
 * requires all agent servers receiving this notification to be able to
 * deserialize that specialized result class. It is then recommended to
 * send to registered status listeners of a <code>Task</code> agent a
 * degenerated notification omitting the result object.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		Task
 */
public class StatusNotification extends Notification {

public static final String RCS_VERSION="@(#)$Id: StatusNotification.java,v 1.1 2002-03-06 16:52:20 joram Exp $"; 


  /** date of status change */
  private Date date;
  /** task which changed status */
  private AgentId task;
  /** new status */
  private int status;
  /** optional message */
  private String message;
  /** optional result value when status is DONE */
  private Object result;

  /**
   * Creates a notification to be sent.
   *
   * @param task		task which changed status
   * @param status		new status
   * @param message		optional message
   * @param result		optional result value
   */
  public StatusNotification(AgentId task, int status, String message, Object result) {
    this.date = new Date();
    this.task = task;
    this.status = status;
    this.message = message;
    this.result = result;
  }

  /**
   * Creates a notification to be sent with null result object.
   *
   * @param task		task which changed status
   * @param status		new status
   * @param message		optional message
   */
  public StatusNotification(AgentId task, int status, String message) {
    this(task, status, message, null);
  }

  /**
   * Accesses read only property.
   *
   * @return		date of status change
   */
  public Date getDate() { return date; }

  /**
   * Accesses read only property.
   *
   * @return		task which changed status
   */
  public AgentId getTask() { return task; }

  /**
   * Accesses read only property.
   *
   * @return		new status
   */
  public int getStatus() { return status; }

  /**
   * Accesses read only property.
   *
   * @return		optional message
   */
  public String getMessage() { return message; }

  /**
   * Accesses property.
   *
   * @return		optional result value
   */
  public Object getResult() { return result; }

  /**
   * Sets property.
   *
   * @param result	optional result value
   */
  public void setResult(Object result) {
    this.result = result;
  }


  /**
   * Provides a string image for this object.
   *
   * @result		string image for this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",date=" + date +
      ",task=" + task +
      ",status=" + Program.Status.toString(status) +
      ",message=" + (message == null ? "null" :
			 "\"" + message + "\"") +
      ",result=" + result + ")";
  }
}
