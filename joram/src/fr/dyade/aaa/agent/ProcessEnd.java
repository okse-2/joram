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
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
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

public static final String RCS_VERSION="@(#)$Id: ProcessEnd.java,v 1.10 2002-10-21 08:41:13 maistrfr Exp $"; 


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
