/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - ScalAgent Distributed Technologies
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
package org.objectweb.joram.shared.admin;

import java.util.Vector;


/**
 * A <code>Monitor_GetServersIdsRep</code> instance holds the list of the
 * platform's servers' identifiers.
 */
public class Monitor_GetServersIdsRep extends Monitor_Reply
{
  /** Servers identifiers. */
  private Vector ids;


  /**
   * Constructs a <code>Monitor_GetServersRep</code> instance.
   *
   * @param ids  Vector of servers' identifiers.
   */
  public Monitor_GetServersIdsRep(Vector ids)
  {
    this.ids = ids;
  }


  /** Returns the servers' identifiers. */
  public Vector getIds()
  {
    return ids;
  }
}
