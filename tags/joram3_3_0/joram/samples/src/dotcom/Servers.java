/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
