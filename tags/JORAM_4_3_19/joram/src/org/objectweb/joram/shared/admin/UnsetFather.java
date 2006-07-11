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
 * An <code>UnsetFather</code> instance is used for notifying a topic to unset
 * its father.
 */
public class UnsetFather extends AdminRequest
{
  /** Identifier of the topic. */
  private String id;

  /**
   * Constructs an <code>UnsetFather</code> instance.
   *
   * @param id  Identifier of the topic which has father is unset.
   */
  public UnsetFather(String id)
  {
    this.id = id;
  }

  /** Returns the identifier of the topic which father is unset. */
  public String getTopId()
  {
    return id;
  }
}
