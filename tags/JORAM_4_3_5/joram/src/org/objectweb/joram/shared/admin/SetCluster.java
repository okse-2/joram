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
 * Initial developer(s): Frederic Maistre
 * Contributor(s):
 */
package org.objectweb.joram.shared.admin;

/** 
 * A <code>SetCluster</code> instance is used for adding a given topic
 * to a cluster an other topic is part of, or for creating a new cluster.
 */
public class SetCluster extends AdminRequest
{
  /**
   * Identifier of the topic already part of a cluster, or chosen as the
   * initiator.
   */
  private String initId;
  /** Identifier of the topic joining the cluster, or the initiator. */
  private String topId;

  /**
   * Constructs a <code>SetCluster</code> instance.
   *
   * @param initName  Identifier of the topic already part of a cluster, or
   *          chosen as the initiator.
   * @param topName  Identifier of the topic joining the cluster, or the
   *          initiator.
   */
  public SetCluster(String initId, String topId)
  {
    this.initId = initId;
    this.topId = topId;
  }

  /**
   * Returns the identifier of the topic already part of a cluster, or chosen
   * as the initiator.
   */
  public String getInitId()
  {
    return initId;
  }

  /**
   * Returns the identifier of the topic joining the cluster, or the
   * initiator.
   */
  public String getTopId()
  {
    return topId;
  }
}