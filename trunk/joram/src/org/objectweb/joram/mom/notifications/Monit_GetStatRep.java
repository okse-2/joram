/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - ScalAgent Distributed Technologies
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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package org.objectweb.joram.mom.notifications;

import java.util.Hashtable;

/**
 * A <code>Monit_GetStatRep</code> reply is used by a destination for
 * sending to an administrator client the statistic.
 */
public class Monit_GetStatRep extends AdminReply {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private Hashtable stats;

  /**
   * Constructs a <code>Monit_GetStatRep</code> instance.
   *
   * @param request  The request this reply replies to.
   * @param stats    The hashtable statistic.
   */
  public Monit_GetStatRep(AdminRequest request, 
                          Hashtable stats) {
    super(request, true, null);
    this.stats = stats;
  }
  
  /** Returns the Hastable of stats. */
  public Hashtable getStats() {
    if (stats == null)
      return new Hashtable();
    return stats;
  }
}
