/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2006 ScalAgent Distributed Technologies
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
package org.objectweb.joram.shared.admin;

import java.util.Hashtable;

/**
 * A <code>Monitor_GetStatRep</code> instance replies to a get stat,
 * monitoring request.
 */
public class Monitor_GetStatRep extends Monitor_Reply {
  private static final long serialVersionUID = 5241964631247563162L;

  /** Table holding the statistic. */
  private Hashtable stats;

  /**
   * Constructs a <code>Monitor_GetStatRep</code> instance.
   */
  public Monitor_GetStatRep(Hashtable stats) {
    this.stats = stats;
  }

  /** Returns the stats table. */
  public Hashtable getStats() {
    return stats;
  }
}