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
public static final String RCS_VERSION="@(#)$Id: FlowControlDuplexStream.java,v 1.7 2002-03-06 16:58:48 joram Exp $";

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
