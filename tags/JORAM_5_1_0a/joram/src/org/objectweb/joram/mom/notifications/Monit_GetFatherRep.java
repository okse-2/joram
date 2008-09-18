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

/**
 * A <code>Monit_GetFatherRep</code> reply is used by a topic for sending
 * the identifier of its hierarchical father to the administrator.
 */
public class Monit_GetFatherRep extends AdminReply
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** Identifier of the hierarchical father. */
  private String fatherId;

  
  /**
   * Constructs a <code>Monit_GetFatherRep</code> instance.
   *
   * @param request  The request this reply replies to.
   * @param fatherId  Identifier of the hierarchical father.
   */
  public Monit_GetFatherRep(AdminRequest request, String fatherId)
  {
    super(request, true, null);
    this.fatherId = fatherId;
  }


  /** Returns the identifier of the hierarchical father. */
  public String getFatherId()
  {
    return fatherId;
  }
}
