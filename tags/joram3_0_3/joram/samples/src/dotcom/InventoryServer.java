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

import javax.jms.*;
import javax.naming.*;

/**
 * Launching the InventoryServer that
 * receives an OrderMessage from WebServer through topicOrders,
 * creates an OkMessage confirming the order,
 * and sends the OkMessage to CustomerServer through queueItems.
 * <br><br>
 * This code must be executed before WebServer.
 *
 * @author	Maistre Frederic
 * 
 * @see		Admin
 * @see		InventoryTreatment
 */
public class InventoryServer {
  static Context ictx = null;

  public static void main (String argv[]) throws Exception {

    try {
      // setting LAF in order to avoid the following exception :            
      // java.lang.Error: can't load javax.swing.plaf.metal.MetalLookAndFeel
      javax.swing.UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
         
      // getting initial context
      ictx = new InitialContext();
      // connecting to agent agInventConnT 
      TopicConnectionFactory tcf;
      tcf = (TopicConnectionFactory) ictx.lookup("tcf");
      // connecting to topicOrders 
      Topic topicOrders;
      topicOrders = (Topic) ictx.lookup("tOrders");
      ictx.close();

      // creating a TopicConnection 
      TopicConnection tc = tcf.createTopicConnection("inventory", "inventory");
      // creating a TopicSession 
      TopicSession tsession;
      tsession = tc.createTopicSession(true, Session.AUTO_ACKNOWLEDGE);
      // creating a TopicSubscriber (receiving from topicOrders) 
      TopicSubscriber ts = tsession.createSubscriber(topicOrders);
      
      // creating a FifoQueue to hold incoming messages from topicOrders 
      fr.dyade.aaa.util.Queue queue ;
      queue = new fr.dyade.aaa.util.Queue() ;
	  		
      // arise the MessageListener 
      TopicListener inventoryListener = new TopicListener(tsession, queue);
      ts.setMessageListener(inventoryListener);
	
      // creating a thread to treat the messages held in queue 
      InventoryTreatment inventoryTreatment = new InventoryTreatment(queue, tc, tsession) ;
      java.lang.Thread inventoryThread = new java.lang.Thread(inventoryTreatment) ;
      inventoryThread.start() ;
			
      // starting the TopicConnection
      tc.start();
       
    } catch (Exception exc) {
      System.out.println("Exception caught in InventoryServer: " + exc);
      exc.printStackTrace();
    }
  } 
}


/**
 * Thread launched by the main of InventoryServer.
 *
 * @author	Maistre Frederic
 * 
 * @see		Admin
 * @see		InventoryServer
 * @see		Servers
 * @see		OrderMessage
 * @see		OkMessage
 * @see		GUI
 */
class InventoryTreatment implements Runnable, Servers {
  static Context ictx = null;
  /** TopicConnection created by InventoryServer, to be closed in thread. */
  TopicConnection tc ;
  /** TopicSession created by InventoryServer, to be closed in thread. */
  TopicSession tsession ;
  /** QueueSession sending messages to queueItems. */
  QueueSession qsession ;
  /** QueueSender sending OrderMessages. */
  QueueSender qs ;
  /** FifoQueue holding OrderMessages received from topicOrders. */
  fr.dyade.aaa.util.Queue queue ;
  /** OrderMessage hold by FifoQueue. */
  OrderMessage orderMsg ;
  /** Lock to wait for graphical interaction. */
  Object lock ;
  /** GUI for validating OrderMessages. */
  GUI stockGUI ;
   	
  /**
   * Creates the thread.
   * 
   * @param queue		FifoQueue in which OrderMessages are held
   * @param tc			TopicConnection created by InventoryServer.
   * @param tsession		TopicSession created by InventoryServer.
   */
  InventoryTreatment(fr.dyade.aaa.util.Queue queue, TopicConnection tc, TopicSession tsession) {
    this.queue = queue ;
    this.tc = tc ;
    this.tsession = tsession ;
  }
  
  /**
   * Method called when starting the thread.
   */
  public void run() {
    // creating the GUI representing the InventoryServer 
    stockGUI = new GUI("Inventory Server", "Validate", "Don't validate", this, 700, 600) ;
        
    try {
      // getting initial context
      ictx = new InitialContext();
      
      // connecting to agent agInventConnQ
      QueueConnectionFactory qcf;
      qcf = (QueueConnectionFactory) ictx.lookup("qcf");
      // connecting to queueItems 
      Queue queueItems;
      queueItems = (Queue) ictx.lookup("qItems");
      ictx.close();

      // creating a QueueConnection  
      QueueConnection qc = qcf.createQueueConnection("inventory", "inventory");
      // creating a QueueSession 
      qsession = qc.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
      // creating a QueueSender (sending to queueItems) 
      qs = qsession.createSender(queueItems);
      
      System.out.println("InventoryServer is ready.") ;
      
      // creating the lock object
      lock = new Object() ;
      
      while (true) {
        // ObjectMessage got from the FifoQueue
        ObjectMessage msg ;
        
      	// waiting for the FifoQueue to be filled 
        msg = (ObjectMessage) queue.get() ;
      
        // poping out FifoQueue's first OrderMessage 
        msg = (ObjectMessage) queue.pop();
        
        // if msg encapsulates a QuitMessage, close sessions and connections
        if (msg.getObject() instanceof QuitMessage) {
          tsession.close() ;
          tc.close() ;
          qsession.close() ;
          qc.close() ;
          
          System.out.println("Sessions and connections closed by InventoryServer.");
          System.exit(0) ;
        }
        
        // if msg encapsulates an OrderMessage, treate it
        else if (msg.getObject() instanceof OrderMessage) {
          // get OrderMessage 
          orderMsg = (OrderMessage) msg.getObject() ;
        
          System.out.println("Message received by InventoryServer from WebServer: " + orderMsg.id) ;

          // updating and activating GUI 
          stockGUI.updateId(orderMsg.id) ;
          stockGUI.updateItem(orderMsg.item) ;
          stockGUI.setVisible(true) ;

          // waiting for GUI interaction 
          synchronized(lock) {
            lock.wait() ;
          }
        }
      } 
    } catch (Exception exc) {
      System.out.println("Exception caught in InventoryServer thread: " + exc);
      exc.printStackTrace() ;  
    }	  
  }
  
  /** 
   * Method called when pressing GUI's okButton.
   */
  public void okMethod() {
    try {
      // desctivate the GUI 
      stockGUI.setVisible(false) ;
      
      // creating the OkMessage to be sent 
      OkMessage okMsg = new OkMessage(orderMsg.id, orderMsg.item, true) ;  
      // creating an ObjectMessage and encapsulating the OkMessage 
      ObjectMessage msgSent = qsession.createObjectMessage() ;
      msgSent.setObject(okMsg) ;
      // sending the ObjectMessage to queueItems 
      qs.send(msgSent);
      
      // commiting the sending
      qsession.commit() ;
      
      // unlocking 
      synchronized(lock) {
        lock.notify() ; 
      }
      
    } catch (Exception exc) {
      System.out.println("Exception caught in InventoryServer okMethod: " + exc);
      exc.printStackTrace() ;
    }
  }	  


  /**
   * Method called when pressing GUI's noButton.
   */
  public void noMethod() {
    try {
      // deactivate the GUI 
      stockGUI.setVisible(false) ;
      
      // creating the OkMessage to be sent 
      OkMessage okMsg = new OkMessage(orderMsg.id, orderMsg.item, false) ; 
      // creating an ObjectMessage and encapsulating the OkMessage 
      ObjectMessage msgSent = qsession.createObjectMessage() ;
      msgSent.setObject(okMsg) ;
      // sending the ObjectMessage to queueItems 
      qs.send(msgSent);
      
      // commiting the sending
      qsession.commit() ;
      
      // unlocking 
      synchronized(lock) {
        lock.notify() ;
      }
      
    } catch (Exception exc) {
      System.out.println("Exception caught in InventoryServer noMethod: " + exc);
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
