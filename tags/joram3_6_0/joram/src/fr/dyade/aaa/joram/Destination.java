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
 * Contributor(s): Nicolas Tachker (ScalAgent DT)
 */
package fr.dyade.aaa.joram;

import java.util.Vector;
import java.util.Hashtable;

import javax.naming.*;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * Implements the <code>javax.jms.Destination</code> interface.
 */
public abstract class Destination
                      extends fr.dyade.aaa.joram.admin.AdministeredObject
                      implements javax.jms.Destination
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

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": created.");
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

  /** Sets the naming reference of a destination. */
  public Reference getReference() throws NamingException
  {
    Reference ref = super.getReference();
    ref.add(new StringRefAddr("dest.name", agentId));
    return ref;
  }

  /**
   * Returns <code>true</code> if the parameter object is a Joram destination
   * wrapping the same agent identifier.
   */
  public boolean equals(Object obj)
  {
    if (! (obj instanceof fr.dyade.aaa.joram.Destination))
      return false;

    return (agentId.equals(((fr.dyade.aaa.joram.Destination) obj).getName()));
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
