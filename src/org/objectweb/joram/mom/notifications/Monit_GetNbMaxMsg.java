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
 * A <code>Monit_GetNbMaxMsgRequest</code> instance is used by a client agent
 * to get NbMaxMsg value of queue or subscribtion.
 */
public class Monit_GetNbMaxMsg extends AdminRequest {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** subscription name */
  private String subName = null;

  /**
   * Constructs a <code>Monit_GetNbMaxMsgRequest</code> instance.
   *
   * @param id  Identifier of the request, may be null.
   * @param subName Subscription name (not used for queue).
   */
  public Monit_GetNbMaxMsg(String id, String subName) {
    super(id);
    this.subName = subName;
  }

  /** Returns SubName */
  public String getSubName() {
    return subName;
  }
} 
