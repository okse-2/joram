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
package org.objectweb.joram.mom.notifications;

import java.util.Vector;


/**
 * A <code>Monit_GetUsersRep</code> reply is used by a destination for
 * sending to an administrator client the identifiers of its readers or
 * writers.
 */
public class Monit_GetUsersRep extends AdminReply
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** Vector of readers' or writers' identifiers. */
  private Vector users;


  /**
   * Constructs a <code>Monit_GetUsersRep</code> instance.
   *
   * @param request  The request this reply replies to.
   * @param readers  The vector or readers' or writers' identifiers.
   */
  public Monit_GetUsersRep(AdminRequest request, Vector users)
  {
    super(request, true, null);
    this.users = users;
  }

  
  /** Returns the vector of readers' or writers' identifiers. */
  public Vector getUsers()
  {
    if (users == null)
      return new Vector();
    return users;
  }
}
