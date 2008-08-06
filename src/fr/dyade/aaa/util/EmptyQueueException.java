/*
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies
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


package fr.dyade.aaa.util;

/**
 * Thrown by methods in the <code>Queue</code> class to indicate 
 * that the queue is empty. 
 *
 * @see     fr.dyade.aaa.util.Queue
 */
public class EmptyQueueException extends RuntimeException {


  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new <code>EmptyQueueException</code> with no detail message.
   */
  public EmptyQueueException() {}
}
