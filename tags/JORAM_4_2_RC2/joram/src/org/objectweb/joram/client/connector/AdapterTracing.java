/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
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
 * Initial developer(s): Frederic Maistre (Bull SA)
 * Contributor(s): Nicolas Tachker (Bull SA)
 */
package org.objectweb.joram.client.connector;

import fr.dyade.aaa.util.Debug;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;


/**
 * Utility class for logging.
 */
public class AdapterTracing {
  public static Logger dbgAdapter = null;
  private static boolean initialized = false;

  static
  {
    dbgAdapter =
      Debug.getLogger("org.objectweb.joram.client.connector.Adapter");
  }

  /** Debugging method (INFO level). */
  static void debugINFO(String message)
  {
    if (dbgAdapter.isLoggable(BasicLevel.INFO))
      dbgAdapter.log(BasicLevel.INFO, message);
  }

  /** Debugging method (DEBUG level). */
  static void debugDEBUG(String message)
  {
    if (dbgAdapter.isLoggable(BasicLevel.DEBUG))
      dbgAdapter.log(BasicLevel.DEBUG, message);
  }

  /** Debugging method (WARN level). */
  static void debugWARN(String message)
  {
    if (dbgAdapter.isLoggable(BasicLevel.WARN))
      dbgAdapter.log(BasicLevel.WARN, message);
  }

  /** Debugging method (ERROR level). */
  static void debugERROR(String message)
  {
    if (dbgAdapter.isLoggable(BasicLevel.ERROR))
      dbgAdapter.log(BasicLevel.ERROR, message);
  }
}
