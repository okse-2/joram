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
 * Contributor(s): Nicolas Tachker (ScalAgent)
 */
package com.scalagent.kjoram;

import java.util.Vector;
import java.util.Hashtable;

public abstract class Destination
  extends com.scalagent.kjoram.admin.AdministeredObject
{
  /** Identifier of the agent destination. */
  protected String agentId;


  /**
   * Constructs a destination.
   *
   * @param agentId  Identifier of the agent destination.
   */ 
  public Destination(String agentId)
  {
    super(agentId);
    this.agentId = agentId;

    if (JoramTracing.dbgClient)
      JoramTracing.log(JoramTracing.DEBUG, this + ": created.");
  }

  /**
   * Constructs an empty destination.
   */ 
  public Destination()
  {}


  /** Returns the name of the destination. */
  public String getName()
  {
    return agentId;
  }

  /**
   * Returns <code>true</code> if the parameter object is a Joram destination
   * wrapping the same agent identifier.
   */
  public boolean equals(Object obj)
  {
    if (! (obj instanceof Destination))
      return false;

    return (agentId.equals(((Destination) obj).getName()));
  }

  public void setAgentId(String agentId) {
    this.agentId = agentId;
  }

 /**
   * Codes a <code>Destination</code> as a Hashtable for travelling through the
   * SOAP protocol.
   */
  public Hashtable code() {
    Hashtable h = super.code();
    h.put("agentId",agentId);
    return h;
  }
}
