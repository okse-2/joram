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
 * An <code>AdminReply</code> is a reply sent by a
 * <code>fr.dyade.aaa.mom.dest.AdminTopic</code> topic and containing data or
 * information destinated to a client administrator.
 */
public class AdminReply implements java.io.Serializable
{
  /** <code>true</code> if this reply replies to a successful request. */
  private boolean success = false;
  /** Information. */
  private String info;

  /**
   * Constructs an <code>AdminReply</code> instance.
   *
   * @param success  <code>true</code> if this reply replies to a successful
   *          request.
   * @param info  Information to carry.
   */
  public AdminReply(boolean success, String info)
  {
    this.success = success;
    this.info = info;
  }

  /**
   * Returns <code>true</code> if this reply replies to a successful request.
   */
  public boolean succeeded()
  {
    return success;
  }

  /** Returns the carried info. */
  public String getInfo()
  {
    return info;
  }
}
