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
package com.scalagent.kjoram.util;

/**
 * Thrown by the <code>push</code> method of the <code>Queue</code> class
 * when the queue is actually stopping or stopped.
 *
 * @see fr.dyade.aaa.util.Queue
 */
public class StoppedQueueException extends RuntimeException
{
  public static final String RCS_VERSION="@(#)$Id: StoppedQueueException.java,v 1.4 2004-02-13 10:12:42 fmaistre Exp $"; 

  /**
   * Constructs a new <code>StoppedQueueException</code> with
   * no detail message.
   */
  public StoppedQueueException()
  {}
}
