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


package fr.dyade.aaa.agent;

import java.io.*;


/**
 * <code>Notification</code> reporting the end of a process execution.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		ProcessManager
 */
public class ProcessEnd extends Notification {

public static final String RCS_VERSION="@(#)$Id: ProcessEnd.java,v 1.15 2004-02-13 08:12:56 fmaistre Exp $"; 


  private int exitValue;	/** as returned by <code>Process.exitValue</code> */
  private String errorMessage;	/** optional, as returned by <code>Process.getErrorStream</code> */

  /**
   * Creates a notification to be sent.
   *
   * @param exitValue		as returned by <code>Process.exitValue</code>
   * @param errorMessage	optional, as returned by <code>Process.getErrorStream</code>
   */
  public ProcessEnd(int exitValue, String errorMessage) {
    this.exitValue = exitValue;
    this.errorMessage = errorMessage;
  }

  /**
   * Accesses read only property.
   *
   * @return		as returned by <code>Process.exitValue</code>
   */
  public int getExitValue() { return exitValue; }

  /**
   * Accesses read only property.
   *
   * @return		optional, as returned by <code>Process.getErrorStream</code>
   */
  public String getErrorMessage() { return errorMessage; }


  /**
   * Provides a string image for this object.
   */
  public String toString() {
    return "(" + super.toString() +
      ",exitValue=" + exitValue +
      ",errorMessage=" + (errorMessage == null ? "null" :
			 "\"" + errorMessage + "\"") + ")";
  }
}
