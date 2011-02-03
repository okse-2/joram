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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package org.objectweb.joram.client.jms;

import fr.dyade.aaa.util.Debug;

import org.objectweb.util.monolog.api.Logger;

/**
 * The <code>JoramTracing</code> class centralizes the log tracing for joram.
 */
public class JoramTracing
{
  /**
   * Logger used to trace joram admin activity, to be set using topic
   * org.objectweb.joram.client.jms.Admin
   */
  public static Logger dbgAdmin = null;
  /**
   * Logger used to trace joram activity, to be set using topic
   * org.objectweb.joram.client.jms.Client
   */
  public static Logger dbgClient = null;

  /**
   * <code>true</code> when <code>init</code> has been called.
   */
  private static boolean initialized = false;


  /**
   * Initializes the package by setting the various loggers.
   */
  static
  {
    dbgAdmin = Debug.getLogger("org.objectweb.joram.client.jms.Admin");
    dbgClient = Debug.getLogger("org.objectweb.joram.client.jms.Client");
  }
}