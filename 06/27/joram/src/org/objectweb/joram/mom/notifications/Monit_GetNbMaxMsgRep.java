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


/**
 * A <code>Monit_GetNbMaxMsgRep</code> reply is used by a destination
 * to get NbMaxMsg value of queue or subscribtion.
 */
public class Monit_GetNbMaxMsgRep extends AdminReply {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** nbMaxMsg value (-1 no limit).*/
  private int nbMaxMsg;

  /**
   * Constructs a <code>GetNbMaxMsgRep</code> instance.
   *
   * @param request  The request this reply replies to.
   * @param nbMaxMsg  nb Max of Message (-1 no limit).
   */
  public Monit_GetNbMaxMsgRep(AdminRequest request, 
                              int nbMaxMsg) {
    super(request, true, null);
    this.nbMaxMsg = nbMaxMsg;
  }
  
  /** Returns the nbMaxMsg value. */
  public int getNbMaxMsg() {
    return nbMaxMsg;
  }
}
