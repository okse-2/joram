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
package fr.dyade.aaa.mom.admin;

/**
 * A <code>Monitor_GetFatherRep</code> instance holds the identifier of a 
 * topic's hierarchical father.
 */
public class Monitor_GetFatherRep extends Monitor_Reply
{
  /** Identifier of the hierarchical father. */
  private String fatherId;

  
  /**
   * Constructs a <code>Monitor_GetFatherRep</code> instance.
   *
   * @param fatherId  Identifier of the hierarchical father.
   */
  public Monitor_GetFatherRep(String fatherId)
  {
    this.fatherId = fatherId;
  }


  /** Returns the identifier of the hierarchical father. */
  public String getFatherId()
  {
    return fatherId;
  }
}
