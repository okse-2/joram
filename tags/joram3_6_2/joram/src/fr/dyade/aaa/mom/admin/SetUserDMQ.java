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
 * Contributor(s):
 */
package fr.dyade.aaa.mom.admin;

/**
 * A <code>SetUserDMQ</code> instance requests to set a given DMQ as the
 * DMQ for a given user.
 */
public class SetUserDMQ extends AdminRequest
{
  /** Identifier of the user's proxy the DMQ is set for. */
  private String userProxId;
  /** Identifier of the DMQ. */
  private String dmqId;

  /**
   * Constructs a <code>SetUserDMQ</code> instance.
   *
   * @param userId  Identifier of the user's proxy the DMQ is set for.
   * @param dmqId  Identifier of the DMQ.
   */
  public SetUserDMQ(String userProxId, String dmqId)
  {
    this.userProxId = userProxId;
    this.dmqId = dmqId;
  }

  
  /** Returns the ProxId of the user the DMQ is set for. */
  public String getUserProxId()
  {
    return userProxId;
  }

  /** Returns the identifier of the DMQ. */
  public String getDmqId()
  {
    return dmqId;
  }
}
