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

package fr.dyade.aaa.agent;

import java.util.Properties;

/**
 * <code>Notification</code> reporting the current set of properties
 * from an A3 agent server.
 *
 * @author	Freyssinet Andr*
 * @version	v1.0
 *
 * @see		AgentAdmin
 * @see		AdminRequest
 */
public final class A3ServerProperties extends Notification {

  /** RCS version number of this file: $Revision: 1.15 $ */
  public static final String RCS_VERSION="@(#)$Id: A3ServerProperties.java,v 1.15 2004-02-13 08:12:56 fmaistre Exp $";

  /**
   * The current system properties. 
   */
  public Properties system;

  /**
   * Creates a notification to be sent.
   */
  public A3ServerProperties() {
    system = System.getProperties();
  }
}
