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
 * Launching the CustomerServer that
 * receives an OrderMessage from WebServer through topicOrders,
 * waits for BillingServer to confirm the order with an OkMessage through queueChecked,
 * and waits for InventoryServer to confirm the order with an OkMessage through queueItems.
 * When confirmed, sends the order to DeliveryServer through queueDelivery.
 * <br><br>
 * This code must be executed before WebServer.
 *
 * @author	Maistre Frederic
 * 
 * @see		Admin
 * @see		CustomerTreatment
 */
public class CustomerServer {
  static Context ictx = null;

  public static void main (String argv[]) throws Exception {
    
    try {
      // setting LAF in order to avoid the following exception :            
      // java.lang.Error: can't load javax.swing.plaf.metal.MetalLookAndFeel
      javax.swing.UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
     
      // getting initial context
      ictx = new InitialContext(); 
      // connecting to agent agCustomerConnT
      TopicConnectionFactory tcf;
      tcf = (TopicConnectionFactory) ictx.lookup("tcf");
      // connecting to topicOrders 
      Topic topicOrders;
      topicOrders = (Topic) ictx.lookup("tOrders");
      ictx.close();

      // creating a TopicConnection 
      TopicConnection tc = tcf.createTopicConnection("customer", "customer");
      // creating a TopicSession 
      TopicSession tsession;
      tsession = tc.createTopicSession(true, Session.AUTO_ACKNOWLEDGE);
      // creating a TopicSubscriber (receiving from topicOrders) 
      TopicSubscriber ts = tsession.createSubscriber(topicOrders);
      
      // creating a FifoQueue to hold incoming messages from topicOrders 
      fr.dyade.aaa.util.Queue queue ;
      queue = new fr.dyade.aaa.util.Queue() ;
	  		
      // arise the MessageListener 
      TopicListener customerListener = new TopicListener(tsession, queue);
      ts.setMessageListener(customerListener);
	
      // creating and starting a thread to treate the messages held in queue 
      CustomerTreatment customerTreatment = new CustomerTreatment(queue, tc, tsession) ;
      java.lang.Thread customerThread = new java.lang.Thread(customerTreatment) ;
      customerThread.start() ;
			
      // starting the TopicConnection
      tc.start();
       
    } catch (Exception exc) {
      System.out.println("Exception caught in CustomerServer: " + exc);
      exc.printStackTrace();
    }
  }
}


/**
 * Thread launched by the main of CustomerServer.
 *
 * @author	Maistre Frederic
 * 
 * @see		Admin
 * @see		CustomerServer
 * @see		Servers
 * @see		OrderMessage
 * @see		OkMessage
 * @see		GUI
 */
class CustomerTreatment implements Runnable, Servers {
  static Context ictx = null;
  /** TopicConnection created by CustomerServer, to be closed in thread. */ 
  TopicConnection tc ;
  /** TopicSession created by CustomerServer, to be closed in thread. */
  TopicSession tsession ;
  /** QueueConnection connecting to queueDelivery. */
  QueueConnection qc ;
  /** QueueSession publishing OrderMessages. */
  QueueSession qsession ;
  /** QueueSender sending messages. */
  QueueSender qs ;
  /** FifoQueue holding OrderMessages received from topicOrders. */
  fr.dyade.aaa.util.Queue queue ;
  /** OrderMessage got from the FifoQueue. */
  OrderMessage orderMsg ;
  /** Lock to wait for graphical interaction. */
  Object lock ;
  /** GUI for validating OrderMessages. */
  GUI customerGUI1 ;
  /** GUI for displaying incoming non validated OrderMessages. */
  GUI customerGUI2 ;
                 
  /**
   * Creates the thread.
   * 
   * @param queue		fifo queue in which OrderMessages are held.
   * @param tc			TopicConnection created in CustomerServer.
   * @param tsession		TopicSession created in CustomerServer.
   */
  CustomerTreatment(fr.dyade.aaa.util.Queue queue, TopicConnection tc, TopicSession tsession) {
    this.queue = queue ;
    this.tc = tc ;
    this.tsession = tsession ;
  }
  
  /**
   * Method called when starting the thread.
   */
  public void run() {
    // creating the GUIs representing the CustomerServer 
    customerGUI1 = new GUI("Customer Server", "Deliver", "Don't deliver", this, 300, 100) ;
    customerGUI2 = new GUI("Customer Server", "Not validated by StockServer and/or BillingServer", this, 320, 120) ;
    
    try {
      // getting initial context
      ictx = new InitialContext();
      // connecting to agent agCustomerConnQ
      QueueConnectionFactory qcf;
      qcf = (QueueConnectionFactory) ictx.lookup("qcf");
      // connecting to queueItems, queueBills and queueDelivery 
      Queue queueItems;
      queueItems = (Queue) ictx.lookup("qItems");
      Queue queueBills ;
      queueBills = (Queue) ictx.lookup("qBills");
      Queue queueDelivery ;
      queueDelivery = (Queue) ictx.lookup("qDelivery");
      ictx.close();

      // creating a QueueConnection 
      QueueConnection qc = qcf.createQueueConnection("customer", "customer");
      // creating a QueueSession     
      qsession = qc.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
      // creating a QueueReceiver (receiving from queueItems) 
      QueueReceiver qrItems = qsession.createReceiver(queueItems);
      // creating a QueueReceiver (receiving from queueBills) 
      QueueReceiver qrBills = qsession.createReceiver(queueBills);
      // creating a QueueSender (sending to queueDelivery)
      qs = qsession.createSender(queueDelivery) ;
      // starting the QueueConnection 
      qc.start() ;
	
      // creating lock object
      lock = new Object() ;
      
      System.out.println("CustomerServer is ready.") ;
      
      while (true) {
        // ObjectMessage got from the FifoQueue
        ObjectMessage msg ;
        
        // waiting for the FifoQueue to be filled 
        msg = (ObjectMessage) queue.get() ;
     
        // poping out FifoQueue's first ObjectMessage 
        msg = (ObjectMessage) queue.pop();
        
        // if msg encapsulates a QuitMessage
        if (msg.getObject() instanceof QuitMessage) {
          // getting the QuitMessage
          QuitMessage quitMsg = (QuitMessage) msg.getObject() ;
          // creating an ObjectMessage and encapsulating the QuitMessage 
          ObjectMessage msgSent = qsession.createObjectMessage();
          msgSent.setObject(quitMsg) ;
          // forwarding the QuitMessage to DeliveryServer 
          qs.send(msgSent) ;
          qsession.commit() ;
          
          // closing sessions and connections
          tsession.close() ;
          tc.close() ;
          qsession.close() ;
          qc.close() ;
          
          System.out.println("Sessions and connections closed by CustomerServer.");
          System.exit(0) ;
        }
        
        // if msg encapsulates an OrderMessage, treat it
        else if (msg.getObject() instanceof OrderMessage) {
          // get encapsulated OrderMessage 
          orderMsg = (OrderMessage) msg.getObject() ;
        
          System.out.println("Message received by CustomerServer from WebServer: " +  orderMsg.id);
        
	  // waiting for an ObjectMessage from queueItems 
	  ObjectMessage msgRec = (ObjectMessage) qrItems.receive() ;
	  // getting encapsulated OkMessage 
	  OkMessage okMsg = (OkMessage)(msgRec.getObject()) ;
	
	  System.out.println("Message received by CustomerServer from InventoryServer: " + okMsg.id);
	
	  // updating ok attribute in current OrderMessage 
	  orderMsg.inventoryOK = okMsg.ok ;

	  // waiting for an ObjectMessage from queueBills 
	  msgRec = (ObjectMessage) qrBills.receive() ;
	  // getting encapsulated OkMessage 
	  okMsg = (OkMessage)(msgRec.getObject()) ;
        
	  System.out.println("Message received by CustomerServer from BillingServer: " + okMsg.id);
	  
	  // updating corresponding attribute in current OrderMessage 
	  orderMsg.billingOK = okMsg.ok ;

	  if (orderMsg.billingOK && orderMsg.inventoryOK) {
	    // updating and activating GUI1 
	    customerGUI1.updateId(okMsg.id) ;
	    customerGUI1.updateItem(okMsg.item) ;
            customerGUI1.setVisible(true) ;
          }
	  else {
	    // updating and activating GUI2 
	    customerGUI2.updateId(okMsg.id) ;
	    customerGUI2.updateItem(okMsg.item) ;
            customerGUI2.setVisible(true) ;
          }
        
          // waiting for GUI interaction
          synchronized(lock) {
            lock.wait() ;
          }
        }
      }
    } catch (Exception exc) {
      System.out.println("Exception caught in CustomerServer thread: " + exc);
      exc.printStackTrace() ;
    }	  
  }
  
  /** 
   * Method called when pressing GUI1's okButton.
   */
  public void okMethod() {
    try {
      // desactivate GUI1 
      customerGUI1.setVisible(false) ;

      // creating an ObjectMessage and encapsulating the OrderMessage 
      ObjectMessage msgSent = qsession.createObjectMessage();
      msgSent.setObject(orderMsg);
      // sending the ObjectMessage to DeliveryServer 
      qs.send(msgSent) ;
      
      // commiting receivings and sending
      qsession.commit() ;
      
      System.out.println("Message sent to DeliveryServer: " + orderMsg.id) ;
      
      // unlocking 
      synchronized(lock) {
        lock.notify() ;
      }
      
    } catch (Exception exc) {
      System.out.println("Exception caught in CustomerServer okMethod: " + exc);
      exc.printStackTrace() ;
    }
  }	  

  /**
   * Method called when pressing GUI1's noButton.
   */
  public void noMethod() {
    try {
      // desactivate GUI1 
      customerGUI1.setVisible(false) ;
    
      // commiting receivings
      qsession.commit() ;
    
      // unlocking 
      synchronized(lock) {
        lock.notify() ;
      }
    } catch (Exception exc) {
      System.out.println("Exception caught in CustomerServer noMethod: " + exc);
      exc.printStackTrace() ;
    }
  }
  
  /**
   * Method called when pressing GUI2's closeButton.
   */
  public void closeMethod() {
    try {
      // desactivate GUI1 
      customerGUI2.setVisible(false) ;
    
      // commiting receivings
      qsession.commit() ;
      	
      // unlocking 
      synchronized(lock) {
        lock.notify() ;
      }
    } catch (Exception exc) {
      System.out.println("Exception caught in CustomerServer closeMethod: " + exc);
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
}
