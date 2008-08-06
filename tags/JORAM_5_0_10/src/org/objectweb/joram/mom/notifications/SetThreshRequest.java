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
package org.objectweb.joram.mom.notifications;


/**
 * A <code>SetThreshRequest</code> instance is used by a client agent
 * for notifying a queue of its threshold value.
 */
public class SetThreshRequest extends AdminRequest
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /**
   * The threshold value, negative or zero for no threshold, <code>null</code>
   * for unsetting the previous value.
   */
  private Integer threshold;


  /**
   * Constructs a <code>SetThresholdRequest</code> instance.
   *
   * @param id  Identifier of the request, may be null.
   * @param threshold  Threshold value, negative or zero for no threshold,
   *          <code>null</code> for unsetting the previous value.
   */
  public SetThreshRequest(String id, Integer threshold)
  {
    super(id);
    this.threshold = threshold;
  }
  


  /**
   * Returns the threshold value, negative or zero for no threshold, 
   * <code>null</code> for unsetting the previous value.
   */
  public Integer getThreshold()
  {
    return threshold;
  }
} 
