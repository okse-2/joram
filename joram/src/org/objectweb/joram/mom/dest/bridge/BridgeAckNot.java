/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - Bull SA
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
 * Initial developer(s): Frederic Maistre (Bull SA)
 * Contributor(s):
 */
package org.objectweb.joram.mom.dest.bridge;


/**
 * A <code>BridgeAckNot</code> notification carries the identifier of a
 * message successfuly delivered by a JMS module to a foreign JMS server.
 */
public class BridgeAckNot extends fr.dyade.aaa.agent.Notification
{
  /**
   * Identifier of the message successfuly delivered to the foreign
   * JMS server.
   */
  private String id;


  /**
   * Constructs a <code>BridgeAckNot</code> wrapping the identifier of a
   * message successfuly delivered to the foreign JMS server.
   */
  public BridgeAckNot(String id)
  {
    this.id = id;
  }


  /**
   * Returns the identifier of the message successfuly delivered to
   * the foreign JMS server.
   */
  public String getIdentifier()
  {
    return id;
  }
}
