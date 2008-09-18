/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 */
package com.scalagent.kjoram.excepts;


public class JMSException extends Exception {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private String errorCode;
  private Exception linkedException;
  
  /**
   * Constructs a <code>JMSException</code> instance.
   */
  public JMSException(String info) {
    super(info);
  }
  
  public String getErrorCode() {
    return this.errorCode;
  }
  
  public Exception getLinkedException() {
    return (linkedException);
  }

  public  synchronized void setLinkedException(Exception exc) {
      linkedException = exc;
  }
}
