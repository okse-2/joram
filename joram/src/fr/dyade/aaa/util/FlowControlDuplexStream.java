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


package fr.dyade.aaa.util;

import java.util.Date;


/**
 * This class provides common definitions used by classes
 * <code>FlowControlDxInputStream</code> and
 * <code>FlowControlDxOutputStream</code>.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		FlowControlDxInputStream
 * @see		FlowControlDxOutputStream
 */
public class FlowControlDuplexStream {
public static final String RCS_VERSION="@(#)$Id: FlowControlDuplexStream.java,v 1.10 2004-02-13 10:26:43 fmaistre Exp $";

  /** default window size value, which is 8000 */
  public static final int WINDOW_SIZE = 8000;

  /** control byte signaling windowSize characters read */
  static final int CTRL_WINDOW = 1;

  /**
   * if <code>true</code>, enables debug tracing code.
   */
  public static final boolean DEBUG = true;

  /**
   * if <code>true</code>, traces messages in streams.
   * Requires <code>DEBUG</code> to be <code>true</code>.
   * May be set using property
   * <code>Debug.var.fr.dyade.aaa.util.FlowControlDuplexStream.dbgStreams</code>.
   * Default value is <code>false</code>.
   */
  public static boolean dbgStreams = false;

  /**
   * if <code>true</code>, traces control messages in streams.
   * Requires <code>DEBUG</code> to be <code>true</code>.
   * May be set using property
   * <code>Debug.var.fr.dyade.aaa.util.FlowControlDuplexStream.dbgControl</code>.
   * Default value is <code>false</code>.
   */
  public static boolean dbgControl = false;

  /**
   * if <code>true</code>, traces synchronization operations.
   * Requires <code>DEBUG</code> to be <code>true</code>.
   * May be set using property
   * <code>Debug.var.fr.dyade.aaa.util.FlowControlDuplexStream.dbgSynchro</code>.
   * Default value is <code>false</code>.
   */
  public static boolean dbgSynchro = false;

  /** object used for synchronizing calls to <code>trace</code> */
  static Object lock = new Object();

  /**
   * Traces message.
   * <p>
   * This function is synchronized, allowing for concurrent calls from
   * multiple threads.
   *
   * @param msg		message to trace
   */
  public static void trace(String msg) {
    synchronized (lock) {
      System.out.print(Thread.currentThread());
      System.out.print(" [");
      System.out.print(System.currentTimeMillis());
      System.out.print("] ");
      System.out.println(msg);
    }
  }

  /** identifier count, used when <code>DEBUG</code> is <code>true</code> */
  static int idCount = 0;

  /**
   * Gets a new unique id.
   *
   * @return	a new unique id.
   */
  static synchronized int newId() {
    return idCount ++;
  }
}
