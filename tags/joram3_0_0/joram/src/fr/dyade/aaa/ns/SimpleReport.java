/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, fr.dyade.aaa.ns,
 * fr.dyade.aaa.jndi and fr.dyade.aaa.joram, released September 11, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
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

public static final String RCS_VERSION="@(#)$Id: SimpleReport.java,v 1.5 2002-03-06 16:34:37 joram Exp $"; 

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