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

/**
 * A <code>Monitor_GetStat</code> instance requests the
 * statistic of the destination.
 */
public class Monitor_GetStat extends Monitor_Request {
  private static final long serialVersionUID = -4556399140524209310L;

  /** Identifier of the destination. */
  private String destId;

  /**
   * Constructs a <code>Monitor_GetStat</code> instance.
   *
   * @param destId  Identifier of the destination.
   */
  public Monitor_GetStat(String destId) {
    this.destId = destId;
  }

  /** Returns the identifier of the destination. */
  public String getDest() {
    return destId;
  }
}
