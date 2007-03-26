/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2006 ScalAgent Distributed Technologies
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
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.shared.admin;

/**
 * A <code>Monitor_GetWriters</code> instance requests the list of
 * the writers on a given destination.
 */
public class Monitor_GetWriters extends Monitor_Request {
  private static final long serialVersionUID = -2458847160856363315L;

  /** Identifier of the target destination. */
  private String dest;

  /**
   * Constructs a <code>Monitor_GetWriters</code> instance.
   *
   * @param dest  Identifier of the target destination.
   */
  public Monitor_GetWriters(String dest) {
    this.dest = dest;
  }

  /** Returns the identifier of the target destination. */
  public String getDest() {
    return dest;
  }
}
