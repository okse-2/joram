/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 */

package fr.dyade.aaa.task;

import java.io.*;
import fr.dyade.aaa.agent.*;
import fr.dyade.aaa.task.Task.Status;


/**
  * Notification reporting a service task completion status.
  * Reuse status definition from <code>Task.Status</code>.
  *
  * @author	Lacourte Serge
  * @version	v1.0
  *
  * @see	Command
  * @see	Status
  */
public class Report extends Notification {

public static final String RCS_VERSION="@(#)$Id: Report.java,v 1.1 2002-03-06 16:52:20 joram Exp $"; 


  /** executed command */
  private Command command;
  /** completion status */
  private int status;
  /** optional message */
  private String message;

  /**
   * Default constructor.
   */
  public Report() {
    this(null, Status.NONE, null);
  }

  /**
   * Creates a notification to be sent.
   *
   * @param command		executed command
   * @param status		completion status
   * @param message		optional message
   */
  public Report(Command command, int status, String message) {
    this.command = command;
    this.status = status;
    this.message = message;
  }

  /**
   * Accesses read only property.
   *
   * @return		executed command
   */
  public Command getCommand() { return command; }

  /**
   * Accesses read only property.
   *
   * @return		completion status
   */
  public int getStatus() { return status; }

  /**
   * Accesses read only property.
   *
   * @return		optional message
   */
  public String getMessage() { return message; }


  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",command=" + command.toString() +
      ",status=" + Status.toString(status) +
      ",message=" + (message == null ? "null" :
			 "\"" + message + "\"") + ")";
  }
}
