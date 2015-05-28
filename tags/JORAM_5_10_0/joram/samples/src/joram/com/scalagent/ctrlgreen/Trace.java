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

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import fr.dyade.aaa.common.Debug;

public class Trace {
  public static Logger logger = Debug.getLogger("joram.ctrlgreen");

  public static void debug(String msg) {
    logger.log(BasicLevel.DEBUG, msg);
  }

  public static void debug(String msg, Throwable exc) {
    logger.log(BasicLevel.DEBUG, msg, exc);
  }

  public static void error(String msg) {
    logger.log(BasicLevel.ERROR, msg);
  }

  public static void error(String msg, Throwable exc) {
    logger.log(BasicLevel.ERROR, msg, exc);
  }

  public static void fatal(String msg) {
    logger.log(BasicLevel.FATAL, msg);
  }

  public static void fatal(String msg, Throwable exc) {
    logger.log(BasicLevel.FATAL, msg, exc);
  }
}
