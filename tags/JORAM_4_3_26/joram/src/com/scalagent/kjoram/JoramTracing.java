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
 * Contributor(s):
 */
package com.scalagent.kjoram;

/**
 * The <code>JoramTracing</code> class centralizes the log tracing for joram.
 */
public class JoramTracing {
  public static int ERROR = 1;
  public static int WARN = 2;
  public static int INFO = 3;
  public static int DEBUG = 4;

  public static int traceLevel = 1;

  public static boolean dbgClient = false;
  public static boolean dbgAdmin = false;
  public static boolean dbg = false;

  static {
    String l = System.getProperty("traceLevel");
    if (l != null && l != "") {
      traceLevel = Integer.parseInt(l);
    }
    if (System.getProperty("dbgClient") != null)
      dbgClient = true;
    if (System.getProperty("dbgAdmin") != null)
      dbgAdmin = true;
    if (System.getProperty("dbg") != null)
      dbg = true;
  }
  
  public static void log(int level, String trace) {
    if (level <= traceLevel)
      System.out.println(trace);
  }

  public static void log(int level, Exception exc) {
    log(level,exc.toString());
  }
}
