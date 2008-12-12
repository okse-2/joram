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
package dotcom;

/**
 * An OkMessage confirms an order. 
 *
 * @author	Maistre Frederic
 * 
 * @see		InventoryTreatment
 * @see		ControlTreatment
 * @see		BillingTreatment
 * @see		CustomerTreatment
 */
public class OkMessage implements java.io.Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** Order id. */
  int id ;
  /** Item ordered. */
  String item ;
  /** Status of the message: true if validated, false otherwise. */
  boolean ok ;
  
  /** 
   * Creates an OkMessage.
   *
   * @param id			Order id
   * @param item		Item ordered
   * @param ok			Status of the order (confirmed or not)
   */
  OkMessage(int id, String item, boolean ok) {
    this.id = id ;
    this.item = item ;
    this.ok = ok ;
  }   
}
