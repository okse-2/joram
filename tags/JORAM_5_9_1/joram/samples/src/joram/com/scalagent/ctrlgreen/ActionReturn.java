/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 ScalAgent Distributed Technologies
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
package com.scalagent.ctrlgreen;

public class ActionReturn {
  boolean replied = true;
  
  int error;
  String msg;

  public static final int NOP = -99;

  public static final int FATAL = -3;
  public static final int WARN = -2;
  public static final int ERROR = -1;
  public static final int OK = 0;
  
  /**
   * Creates an action return with OK result and no message.
   */
  public ActionReturn() {
    error = OK;
    msg = null;
  }
  
  /**
   * Creates an action return with specified result and no message.
   * 
   * @param error The error code to transmit to caller.
   */
  public ActionReturn(int error) {
    this.error = error;
    this.msg = null;
  }

  /**
   * Creates an action return with OK result and specified message.
   * 
   * @param msg The message to transmit to caller.
   */
  public ActionReturn(String msg) {
    this.error = OK;
    this.msg = msg;
  }
  
  /**
   * Creates an action return with specified result and message.
   * 
   * @param error The error code to transmit to caller.
   * @param msg   The message to transmit to caller.
   */
  public ActionReturn(int error, String msg) {
    this.error = error;
    this.msg = msg;
  }
  
  public int getReturnCode() {
    return error;
  }
  
  public String getReturnMessage() {
    return msg;
  }
  
  public String toString() {
    if (msg != null)
      return msg + '[' + error + ']';
    return "[" + error + ']';
  }
}
