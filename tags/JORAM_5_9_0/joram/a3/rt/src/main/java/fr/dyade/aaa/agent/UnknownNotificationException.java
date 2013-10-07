/*
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
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
 *
 * Initial developer(s): Dyade
 * Contributor(s): ScalAgent Distributed Technologies
 */

package fr.dyade.aaa.agent;

/**
 * Thrown by Agent in the <code>react</code> method to indicate 
 * that the notification is unknown. 
 */
public class UnknownNotificationException extends Exception {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /**
   *  Constructs a new <code>UnknownNotificationException</code> with
   * no detail message.
   */
  public UnknownNotificationException() {
    super();
  }

  /**
   *  Constructs a new <code>UnknownNotificationException</code> with
   * the specified  detail message. 
   *
   * @param   s   the detail message.
   */
  public UnknownNotificationException(String s) {
    super(s);
  }
}
