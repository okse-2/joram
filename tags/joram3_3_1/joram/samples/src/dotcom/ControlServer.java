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

import javax.jms.*;
import javax.naming.*;

/**
 * Launching the ControlServer that
 * receives an OrderMessage from BillingServer through queueCheck,
 * creates an OkMessage to confirm the order and sends the OkMessage to 
 * BillingServer through queueChecked.
 * <br><br>
 * This code must be executed before WebServer.
 *
 * @author	Maistre Frederic
 * 
 * @see		Admin
 * @see		ControlTreatment
 */
public class ControlServer {
  
  public static void main (String argv[]) throws Exception {
    
    try {
      // setting LAF in order to avoid the following exception :             
      // java.lang.Error: can't load javax.swing.plaf.metal.MetalLookAndFeel 
      javax.swing.UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
      
      // creating a thread to receive and treat the messages from queueCheck
      ControlTreatment controlTreatment = new ControlTreatment() ;
      java.lang.Thread controlThread = new java.lang.Thread(controlTreatment) ;
      controlThread.start() ;
       
    } catch (Exception exc) {
      System.out.println("Exception caught in ControlServer: " + exc) ;	 
      exc.printStackTrace();
    }
  }
}


/**
 * Thread launched by the main of ControlServer.
 *
 * @author	Maistre Frederic
 * 
 * @see		Admin
 * @see		ControlServer
 * @see		Servers
 * @see		OrderMessage
 * @see		OkMessage
 * @see		GUI
 */
class ControlTreatment implements Runnable, Servers {
  static Context ictx = null;
  /** QueueSession for receiving and sending messages. */
  QueueSession qsession;
  /** QueueSender sending OkMessages. */
  QueueSender qs;
  /** OrderMessage got from queueCheck. */
  OrderMessage orderMsg ;
  /** Lock to wait for graphical interaction. */
  Object lock ;
  /** GUI for validating the OrderMessages. */
  GUI controlGUI ;
  
  /**
   * Method called when starting the thread
   */
  public void run() {  	
    // creating the GUI representing the ControlServer 
    controlGUI = new GUI("Control Server", "Validate", "Don't validate", this, 700, 300) ;
    
    try {
      // getting initial context
      ictx = new InitialContext();
      // connecting to agent agControlConnQ 
      QueueConnectionFactory qcf;
      qcf = (QueueConnectionFactory) ictx.lookup("qcf");
      // connecting to queueCheck and queueChecked 
      Queue queueCheck ;
      queueCheck = (Queue) ictx.lookup("qCheck");
      Queue queueChecked;
      queueChecked = (Queue) ictx.lookup("qChecked");
      ictx.close();

      // creating a QueueConnection 
      QueueConnection qc = qcf.createQueueConnection("control", "control");
      // creating a QueueSession         
      qsession = qc.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
      // creating a QueueReceiver (receiving from queueCheck) and a QueueSender (sending to queueChecked) 
      QueueReceiver qr = qsession.createReceiver(queueCheck);
      qs = qsession.createSender(queueChecked);
      // starting the connection
      qc.start() ;
      
      // creating the lock object
      lock = new Object() ;
      
      System.out.println("ControlServer is ready.") ;
         
      while (true) {
        // receiving an ObjectMessage from queueCheck
	    ObjectMessage msg = (ObjectMessage) qr.receive() ;
	
        // if msg encapsulates a QuitMessage
        if (msg.getObject() instanceof QuitMessage) {
          // commiting the reception
          qsession.commit() ;
        
          // closing session and connection
          qsession.close() ;
          qc.close() ;
          
          System.out.println("Session and connection closed by ControlServer.");
          System.exit(0) ;
        }
        
        // if msg encapsulates an OrderMessage, treat it
        else if (msg.getObject() instanceof OrderMessage) {
          // get OrderMessage 
          orderMsg = (OrderMessage) msg.getObject() ;
        
          System.out.println("Message received by ControlServer from BillingServer: " + orderMsg.id);
        
          // updating and activating GUI 
          controlGUI.updateId(orderMsg.id) ;
          controlGUI.updateItem(orderMsg.item) ;
          controlGUI.setVisible(true) ;
        
          // waiting for GUI interaction 
          synchronized(lock) {
            lock.wait() ;
          }
        }
      }
    } catch (Exception exc) {
      System.out.println("Exception caught in ControlServer thread: " + exc) ;
      exc.printStackTrace() ;
    }	  
  } 
  
  /** 
   * Method called when pressing GUI's okButton.
   */
  public void okMethod() {
    try {
      // deactivating the GUI 
      controlGUI.setVisible(false) ;
      
      // creating the OkMessage to be sent 
      OkMessage okMsg = new OkMessage(orderMsg.id, orderMsg.item, true);
      // creating an ObjectMessage and encapsulating the OkMessage 
      ObjectMessage msgSent = qsession.createObjectMessage() ;
      msgSent.setObject(okMsg) ;
      // sending the ObjectMessage to queueChecked 
      qs.send(msgSent);
      
      // commiting receiving and sending
      qsession.commit() ;
      
      // unlocking 
      synchronized(lock) {
        lock.notify() ;
      }
      
    } catch (Exception exc) {
      System.out.println("Exception caught in ControlServer okMethod: " + exc);
      exc.printStackTrace() ;
    }
  }	  

  /**
   * Method called when pressing GUI's noButton.
   */
  public void noMethod() {
    try {
      // deactivating the GUI 
      controlGUI.setVisible(false) ;
      
      // creating the OkMessage to be sent 
      OkMessage okMsg = new OkMessage(orderMsg.id, orderMsg.item, false);
      // creating an ObjectMessage and encapsulating the OkMessage 
      ObjectMessage msgSent = qsession.createObjectMessage() ;
      msgSent.setObject(okMsg) ;
      // sending the OkMessage to queueChecked 
      qs.send(msgSent);
      
      // commiting receiving and sending
      qsession.commit() ;
      
      // unlocking 
      synchronized(lock) {
        lock.notify() ;
      }
      
    } catch (Exception exc) {
      System.out.println("Exception caught in ControlServer noMethod: " + exc);
      exc.printStackTrace() ;
    }
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
  public void closeMethod() {}
}
