/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 */

package fr.dyade.aaa.ns;

import java.io.*;
import fr.dyade.aaa.agent.*;


/**
  * Notification reporting a status.
  *
  * @author	Lacourte Serge
  * @version	v1.0
  *
  * @see	SimpleCommand
  * @see	Status
  */
public class SimpleReport extends Notification {

public static final String RCS_VERSION="@(#)$Id: SimpleReport.java,v 1.11 2003-09-11 09:54:12 fmaistre Exp $"; 

  public static class Status {
    /** initializing */
    public static final int NONE = 0;
    /** ready */
    public static final int WAIT = 1;
    /** acknowledge start order */
    public static final int INIT = 2;
    /** running */
    public static final int RUN = 3;
    /** completed */
    public static final int DONE = 4;
    /** failed */
    public static final int FAIL = 5;
    /** acknowledge kill order */
    public static final int KILL = 6;
    /** stopped */
    public static final int STOP = 7;

    /**
     * String description of statuses.
     * Entry index must match status int value.
     */
    private static final String[] messages = {
      "NONE", "WAIT", "INIT", "RUN", "DONE", "FAIL", "KILL", "STOP"};
    
    /**
     * Provides a string image for parameter. Uses <code>messages</code>.
     *
     * @return		string image for parameter
     */
    public static String toString(int status) {
      return messages[status];
    }
  }

  /** executed command */
  private SimpleCommand command;
  /** completion status */
  private int status;
  /** optional message */
  private String message;

  /**
   * Default constructor.
   */
  public SimpleReport() {
    this(null, Status.NONE, null);
  }

  /**
   * Creates a notification to be sent.
   *
   * @param command		executed command
   * @param status		completion status
   * @param message		optional message
   */
  public SimpleReport(SimpleCommand command, int status, String message) {
    this.command = command;
    this.status = status;
    this.message = message;
  }

  /**
   * Accesses read only property.
   *
   * @return		executed command
   */
  public SimpleCommand getCommand() { return command; }

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
