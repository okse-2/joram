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
 * The BillingServer receives an OrderMessage from WebServer through 
 * topicOrders, sends the OrderMessage to ControlServer through queueCheck,
 * and waits for ControlServer to confirm the order with an OkMessage 
 * through queueChecked.<br>
 * When confirmed, sends the OkMessage to CustomerServer through queueBills.
 * <br><br>
 * This code must be executed before WebServer.
 *
 * @author	Maistre Frederic
 * 
 * @see		Admin
 * @see		BillingTreatment
 */
public class BillingServer {
  static Context ictx = null;
	
  public static void main (String argv[]) throws Exception {
    
    try {
      // getting initial context
      ictx = new InitialContext();
      // connecting to agent agBillingConnT
      TopicConnectionFactory tcf;
      tcf = (TopicConnectionFactory) ictx.lookup("tcf");
      // connecting to topicOrders 
      Topic topicOrders;
      topicOrders = (Topic) ictx.lookup("tOrders");
      ictx.close();

      // creating a TopicConnection 
      TopicConnection tc = tcf.createTopicConnection("billing", "billing");
      // creating a TopicSession 
      TopicSession tsession;
      tsession = tc.createTopicSession(true, Session.AUTO_ACKNOWLEDGE);
      // creating a TopicSubscriber (receiving from topicOrders 
      TopicSubscriber ts = tsession.createSubscriber(topicOrders);
      
      // creating a FifoQueue to hold incoming messages
      fr.dyade.aaa.util.Queue queue ;
      queue = new fr.dyade.aaa.util.Queue() ;
	  		
      // arise the MessageListener 
      TopicListener billingListener = new TopicListener(tsession, queue);
      ts.setMessageListener(billingListener);
	
      // creating a thread to treat the messages held in queue 
      BillingTreatment billingTreatment = new BillingTreatment(queue, tc, tsession) ;
      java.lang.Thread billingThread = new java.lang.Thread(billingTreatment) ;
      billingThread.start() ;
			
      // starting the TopicConnection
      tc.start();
       
    } catch (Exception exc) {
      System.out.println("Exception caught in BillingServer: " + exc);
      exc.printStackTrace();
    }
  }
}


/**
 * Thread launched by the main of BillingServer.
 *
 * @author	Maistre Frederic
 * 
 * @see		Admin
 * @see		BillingServer
 * @see		Servers
 * @see		OrderMessage
 * @see		OkMessage
 */
class BillingTreatment implements Runnable {
  static Context ictx = null;
  /** TopicConnection created by BillingServer, to be closed in thread. */
  TopicConnection tc ;
  /** TopicSession created by BillingServer, to be closed in thread. */
  TopicSession tsession ;
  /** FifoQueue holding OrderMessages received from topicOrders. */
  fr.dyade.aaa.util.Queue queue ;
  
  /**
   * Creates the thread.
   * 
   * @param queue		FifoQueue in which OrderMessages are held.
   * @param tc			TopicConnection created by BillingServer.
   * @param tsession		TopicSession created by BillingServer.
   */
  BillingTreatment(fr.dyade.aaa.util.Queue queue, TopicConnection tc, TopicSession tsession) {
    this.queue = queue ;
    this.tc = tc ;
    this.tsession = tsession ;
  }
  
  /**
   * Method called when starting the thread.
   */
  public void run() {
    try {
      // getting initial context
      ictx = new InitialContext();
      // connecting to agent agBillingConnQ
      QueueConnectionFactory qcf ;
      qcf = (QueueConnectionFactory) ictx.lookup("qcf");
      // connecting to queueCheck, queueChecked and queueBills 
      Queue queueCheck ;
      queueCheck = (Queue) ictx.lookup("qCheck");
      Queue queueChecked ;
      queueChecked = (Queue) ictx.lookup("qChecked");
      Queue queueBills ;
      queueBills = (Queue) ictx.lookup("qBills");
      ictx.close();

      // creating a QueueConnection 
      QueueConnection qc = qcf.createQueueConnection("billing", "billing");
      // creating a QueueSession         
      QueueSession qsession = qc.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
      // creating a QueueSender (sending to queueCheck)
      QueueSender qsCheck = qsession.createSender(queueCheck);
      // creating a QueueReceiver (receiving from queueChecked) 
      QueueReceiver qr = qsession.createReceiver(queueChecked);
      // creating a QueueSender (sending to queueBills)     
      QueueSender qsBills = qsession.createSender(queueBills);
      // starting the QueueConnection 
      qc.start() ;	 
      
      System.out.println("BillingServer is ready.");
      
      while (true) {
      	// ObjectMessage got from the FifoQueue
        ObjectMessage msg ;
        
      	// waiting for the FifoQueue to be filled 
        msg = (ObjectMessage) queue.get() ;
      
        // poping out FifoQueue's first ObjectMessage 
        msg = (ObjectMessage) queue.pop();
        
        // if msg encapsulates a QuitMessage, close sessions and connections
        if (msg.getObject() instanceof QuitMessage) {
          // getting the QuitMessage
          QuitMessage quitMsg = (QuitMessage) msg.getObject() ;
          // creating an ObjectMessage and encapsulating the QuitMessage 
          ObjectMessage msgSent = qsession.createObjectMessage();
          msgSent.setObject(quitMsg) ;
          // forwarding the QuitMessage to ControlServer 
          qsCheck.send(msgSent) ;
          qsession.commit() ;
          
          // closing sessions and connections
          tsession.close() ;
          tc.close() ;
          qsession.close() ;
          qc.close() ;
          
          System.out.println("Sessions and connections closed by BillingServer.");
          System.exit(0) ;
        }
        
        // if msg encapsulates an OrderMessage, treat it
        else if (msg.getObject() instanceof OrderMessage) {
          // get OrderMessage 
          OrderMessage orderMsg = (OrderMessage) msg.getObject() ;
        
          System.out.println("Message received by BillingServer from WebServer: " + orderMsg.id) ;
         
	      // creating an ObjectMessage and encapsulating the OrderMessage 
	      ObjectMessage msgSent = qsession.createObjectMessage();
	      msgSent.setObject(orderMsg) ;
	      // sending the ObjectMessage to topicCheck 
	      qsCheck.send(msgSent) ; 
	      // commiting the sending
	      qsession.commit() ;
	
	      // receiving an ObjectMessage from queueChecked 
	      ObjectMessage msgRec = (ObjectMessage) qr.receive() ;
	      // getting encapsulated OkMessage 
	      OkMessage okMsg = (OkMessage)(msgRec.getObject()) ;
	
	      System.out.println("Message received by BillingServer from ControlServer: " + okMsg.id) ;
	 
	      // creating an ObjectMessage and encapsulating the OkMessage 
	      msgSent = qsession.createObjectMessage() ;
	      msgSent.setObject(okMsg) ;
	      // sending the ObjectMessage to queueBills 
	      qsBills.send(msgSent);
	
	      // commiting reception from queueChecked and sending to queueBills
	      qsession.commit() ;
        }
      }
    } catch (Exception exc) {
      System.out.println("Exception caught in BillingServer thread: " + exc) ;
      exc.printStackTrace() ;
    } 
  }	
} 
