/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * The present code contributor is ScalAgent Distributed Technologies.
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
