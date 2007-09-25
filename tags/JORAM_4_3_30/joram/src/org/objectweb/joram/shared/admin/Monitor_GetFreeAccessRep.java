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
 * A <code>Monitor_GetFreeAccessRep</code> instance carries the free access
 * settings of a destination.
 */
public class Monitor_GetFreeAccessRep extends Monitor_Reply {
  private static final long serialVersionUID = -2836960153340068909L;

  /** <code>true</code> if READ access is free. */
  private boolean freeReading;
  /** <code>true</code> if WRITE access is free. */
  private boolean freeWriting;

  /**
   * Constructs a <code>Monit_GetFreeAccessRep</code> instance.
   *
   * @param freeReading  <code>true</code> if READ access is free.
   * @param freeWriting  <code>true</code> if WRITE access is free.
   */
  public Monitor_GetFreeAccessRep(boolean freeReading, boolean freeWriting) {
    this.freeReading = freeReading;
    this.freeWriting = freeWriting;
  }

  /** Returns <code>true</code> if READ access is free. */
  public boolean getFreeReading() {
    return freeReading;
  }

  /** Returns <code>true</code> if WRITE access is free. */
  public boolean getFreeWriting() {
    return freeWriting;
  }
}
