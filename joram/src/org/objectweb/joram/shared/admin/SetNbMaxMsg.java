/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2006 ScalAgent Distributed Technologies
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
 * Initial developer(s): Nicolas Tachker (Scalagent)
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.shared.admin;

/**
 * A <code>SetNbMaxMsg</code> instance requests to set a 
 * number max of message in Queue or Subscription.
 */
public class SetNbMaxMsg extends AdminRequest {
  private static final long serialVersionUID = -2173959770467329047L;

  /** Identifier of the queue or subscription. */
  private String id;
  /** nbMaxMsg value (-1 no limit).*/
  private int nbMaxMsg;
  /** subscription name */
  private String subName = null;

  /**
   * Constructs a <code>SetNbMaxMsg</code> instance.
   *
   * @param id        Identifier of the queue or subscription. 
   * @param nbMaxMsg  nb Max of Message (-1 no limit).
   */
  public SetNbMaxMsg(String id, int nbMaxMsg) {
    this.id = id;
    this.nbMaxMsg = nbMaxMsg;
  }

  /**
   * Constructs a <code>SetNbMaxMsg</code> instance.
   *
   * @param id        Identifier of the queue or subscription. 
   * @param nbMaxMsg  nb Max of Message (-1 no limit).
   * @param subName Subscription name.
   */
  public SetNbMaxMsg(String id, 
                     int nbMaxMsg,
                     String subName) {
    this.id = id;
    this.nbMaxMsg = nbMaxMsg;
    this.subName = subName;
  }
  
  /** Returns the identifier of the queue or subscription. */
  public String getId() {
    return id;
  }

  /** Returns the nbMaxMsg value. */
  public int getNbMaxMsg() {
    return nbMaxMsg;
  }

  /** Returns SubName */
  public String getSubName() {
    return subName;
  }
}
