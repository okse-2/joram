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
 * Interface defining the methods called by objects' GUI buttons.
 *
 * @author	Maistre Frederic
 * 
 * @see		WebOrdering
 * @see		CustomerTreatment
 * @see		InventoryTreatment
 * @see		ControlTreatment
 * @see		DeliveryTreatment
 */
public interface Servers {

  /**
   * Method called when selecting a RadioButton on GUI.
   */	
  void choiceMethod(String choice) ;
  
  /**
   * Method called when selecting GUI's otherButton.
   */	
  void otherMethod() ;
  
  /**
   * Method called when selecting GUI's sendButton.
   */	
  void sendMethod() ;
  
  /**
   * Method called when selecting GUI's cancelButton.
   */	
  void cancelMethod() ;
  
  /**
   * Method called when selecting GUI's quitButton.
   */	
  void quitMethod() ;
  
  /**
   * Method called when selecting GUI's okButton.
   */	
  void okMethod() ;
  
  /**
   * Method called when selecting GUI's noButton.
   */	
  void noMethod() ;
  
  /**
   * Method called when selecting GUI's closeButton.
   */	
  void closeMethod() ;
    
}
