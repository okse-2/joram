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
 * An <code>UnsetUserThreshold</code> instance requests to unset 
 * the threshold of a given user.
 */
public class UnsetUserThreshold extends AdminRequest
{
  /** Identifier of the user's proxy which threshold is unset. */
  private String userProxId;

  /**
   * Constructs an <code>UnsetUserThreshold</code> instance.
   *
   * @param userProxId  Identifier of the user's proxy which threshold is
   *          unset. 
   */
  public UnsetUserThreshold(String userProxId)
  {
    this.userProxId = userProxId;
  }

  
  /** Returns the identifier of the user's proxy which threshold is unset. */
  public String getUserProxId()
  {
    return userProxId;
  }
}
