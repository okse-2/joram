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
  * This class is a variation of <code>Report</code>, which it may eventually
  * replace.
  *
  * @author	Lacourte Serge
  * @version	v1.0
  *
  * @see	IndexedCommand
  * @see	Monitor
  * @see	Report
  */
public class IndexedReport extends Notification {

public static final String RCS_VERSION="@(#)$Id: IndexedReport.java,v 1.1 2002-03-06 16:52:20 joram Exp $"; 


  /** executed command identifier */
  private int command;
  /** completion status */
  private int status;
  /** optional message, when status is FAIL */
  private String errorMessage;
  /** optional value, when status is DONE */
  private Object returnValue;

  /**
   * Creates a notification to be sent.
   *
   * @param command		executed command identifier
   * @param status		completion status
   * @param errorMessage	optional message
   * @param returnValue		optional value
   */
  public IndexedReport(int command, int status, String errorMessage,
		       Object returnValue) {
    super();
    this.command = command;
    this.status = status;
    this.errorMessage = errorMessage;
    this.returnValue = returnValue;
  }

  /**
   * Accesses read only property.
   *
   * @return		executed command identifier
   */
  public int getCommand() { return command; }

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
  public String getErrorMessage() { return errorMessage; }

  /**
   * Accesses read only property.
   *
   * @return		optional value
   */
  public Object getReturnValue() { return returnValue; }


  /**
   * Provides a string image for this object.
   *
   * @return		a string image for this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",command=" + command +
      ",status=" + Status.toString(status) +
      ",errorMessage=" + errorMessage +
      ",returnValue=" + returnValue + ")";
  }
}
