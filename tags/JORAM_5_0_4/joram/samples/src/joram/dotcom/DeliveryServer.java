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

import javax.jms.*;
import javax.naming.*;

/**
 * Launching the DeliveryServer that receives an 
 * OrderMessage from CustomerServer through queueDelivery.
 * <br><br>
 * This code must be executed before WebServer.
 *
 * @author	Maistre Frederic
 * 
 * @see		Admin
 * @see		DeliveryTreatment
 */
public class DeliveryServer {

  public static void main (String argv[]) throws Exception {
    
    try {
      // setting LAF in order to avoid the following exception :            
      // java.lang.Error: can't load javax.swing.plaf.metal.MetalLookAndFeel
      javax.swing.UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
      
      // creating a thread to receive and treat the messages from queueDelivery 
      DeliveryTreatment deliveryTreatment = new DeliveryTreatment() ;
      java.lang.Thread deliveryThread = new java.lang.Thread(deliveryTreatment) ;
      deliveryThread.start() ;
			
    } catch (Exception exc) {
      System.out.println("Exception caught in DeliveryServer: " + exc) ;
      exc.printStackTrace();
    }
  }
}


/**
 * Thread launched by the main of DeliveryServer.
 *
 * @author	Maistre Frederic
 * 
 * @see		Admin
 * @see		DeliveryServer
 * @see		Servers
 * @see		OrderMessage
 * @see		GUI
 */
class DeliveryTreatment implements Runnable, Servers {
  static Context ictx = null;
  /** GUI displaying incoming OrderMessages. */
  GUI deliveryGUI ;
  
  /**
   * Method called when starting the thread.
   */
  public void run() {
    // creating the GUI representing the DeliveryServer 
    deliveryGUI = new GUI("Delivery Server", "To be delivered", this, 300, 600);
        
    try {
      // getting initial context
      ictx = new InitialContext();
      // connecting to agent agDeliveryConnQ 
      QueueConnectionFactory qcf;
      qcf = (QueueConnectionFactory) ictx.lookup("qcf");
      // connecting to queueDelivery
      Queue queueDelivery ;
      queueDelivery = (Queue) ictx.lookup("qDelivery");
      ictx.close();

      // creating a QueueConnection 
      QueueConnection qc = qcf.createQueueConnection("delivery", "delivery");
      // creating a QueueSession  
      QueueSession qsession ;       
      qsession = qc.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
      // creating a QueueReceiver
      QueueReceiver qr = qsession.createReceiver(queueDelivery);
      // starting the connection
      qc.start() ;
      
      System.out.println("DeliveryServer is ready.") ;
      
      while (true) {
        // receiving an ObjectMessage from queueDelivery
    	ObjectMessage msg = (ObjectMessage) qr.receive() ;
	
        // if msg encapsulates a QuitMessage, close session and connection
        if (msg.getObject() instanceof QuitMessage) {
          // commiting the reception
          qsession.commit() ;
        	
          // closing session and connection
          qsession.close() ;
          qc.close() ;
        
          System.out.println("Session and connection closed by DeliveryServer.");
          System.exit(0) ;
        }
        
        // if msg encapsulates an OrderMessage, treat it
        else if (msg.getObject() instanceof OrderMessage) {
          // commiting the reception
          qsession.commit() ;
        	
          // get OrderMessage 
          OrderMessage orderMsg = (OrderMessage) msg.getObject() ;
        
          System.out.println("Message received by DeliveryServer from CustomerServer: " + orderMsg.id) ;
        
          // updating and activating the GUI 
          deliveryGUI.updateId(orderMsg.id) ;
          deliveryGUI.updateItem(orderMsg.item) ;
          deliveryGUI.setVisible(true) ;
        }
      }
    }  catch (Exception exc) {
      System.out.println("Exception caught in DeliveryServer thread: " + exc) ;
      exc.printStackTrace() ;
    }	  
  }  
  
  /**
   * Method called when pressing closeButton.
   */
  public void closeMethod() {
     deliveryGUI.setVisible(false) ;
  }
  
  /**
   * Method inherited from the Servers interface, not implemented.
   */
  public void choiceMethod(String choice) {}  
  /**
   * Method inherited from the Servers interface, not implemented.
   */
  public void otherMethod() {}  
  /**
   * Method inherited from the Servers interface, not implemented.
   */
  public void sendMethod() {}  
  /**
   * Method inherited from the Servers interface, not implemented.
   */
  public void cancelMethod() {}  
  /**
   * Method inherited from the Servers interface, not implemented.
   */
  public void quitMethod() {} 
  /**
   * Method inherited from the Servers interface, not implemented.
   */
  public void okMethod() {}
  /**
   * Method inherited from the Servers interface, not implemented.
   */
  public void noMethod() {}
}
