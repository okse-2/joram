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

/**
 * A <code>Monitor_GetFreeAccess</code> instance checks the free access
 * settings on a given destination.
 */
public class Monitor_GetFreeAccess extends Monitor_Request
{
  /** Identifier of the target destination. */
  private String dest;

  
  /**
   * Constructs a <code>Monitor_GetFreeAccess</code> instance.
   *
   * @param dest  Identifier of the target destination.
   */
  public Monitor_GetFreeAccess(String dest)
  {
    this.dest = dest;
  }


  /** Returns the identifier of the target destination. */
  public String getDest()
  {
    return dest;
  }
}
