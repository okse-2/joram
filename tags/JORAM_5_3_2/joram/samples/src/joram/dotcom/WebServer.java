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
 * Launching the WebServer simulating ordering and
 * sending orders to the other servers (through topicOrders). 
 * <br><br>
 * This code must be executed the latest in order to
 * have the subscribers ready.
 *
 * @author	Maistre Frederic
 *
 * @see		WebOrdering
 */
public class WebServer
{
  public static void main (String argv[]) throws Exception
  {
    // setting LAF in order to avoid the following exception :             
    // java.lang.Error: can't load javax.swing.plaf.metal.MetalLookAndFeel 
    javax.swing.UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
      
    // creating a thread to order items and send the orders to the other
    // servers
    WebOrdering webOrdering = new WebOrdering() ;
    java.lang.Thread webThread = new java.lang.Thread(webOrdering) ;
    webThread.start() ;
  }
}


/** 
 * Thread launched by the main of WebServer.
 *
 * @author	Maistre Frederic
 *
 * @see		Admin
 * @see		WebServer
 * @see		Servers
 * @see		OrderMessage
 * @see		GUI
 */
class WebOrdering implements Runnable, Servers
{
  static Context ictx = null;
  /** Connection to topicOrders. */
  TopicConnection tc ;
  /** Session for publishing to topicOrders. */
  TopicSession tsession ;
  /** Publisher sending OrderMessages. */
  TopicPublisher tp ;
  /** Order id. */
  int orderId ;
  /** Item chosen. */
  String choice ;
  /** User graphical interface. */
  GUI webGUI ;
  
  /**
   * Method called when starting the thread.
   */
  public void run()
  {
    try {
      //Getting initial context
      ictx = new InitialContext();
      // connecting to agent agWebConnT
      TopicConnectionFactory tcf = (TopicConnectionFactory) ictx.lookup("tcf");
      // connecting to topicOrders 	    
      Topic topicOrders = (Topic) ictx.lookup("tOrders");
      ictx.close();

      // creating a TopicConnection 
      tc = tcf.createTopicConnection("web", "web");
      // creating a TopicSession 
      tsession = tc.createTopicSession(true, Session.AUTO_ACKNOWLEDGE);  
      // creating a TopicPublisher (publishing in topicOrders) 
      tp = tsession.createPublisher(topicOrders);
      
      // initializing orderId and choice
      orderId = 1 ;
      choice = "Shoes" ;
      
      // creating and activating the GUI representing the WebServer 
      webGUI = new GUI("WebServer" , this, 50, 300) ;
      webGUI.setVisible(true) ;
      
      System.out.println("WebServer is ready.") ;
      
    } catch (Exception exc) {
      System.out.println("Exception caught in WebServer thread: " + exc);
      exc.printStackTrace();
    }
  }

  /** 
   * Method called when selecting a RadioButton.
   */
  public void choiceMethod(String choice) {
    // setting the item choice 
    this.choice = choice ;
  }
  
  /**
   * Method called when pressing GUI's otherButton.
   */
  public void otherMethod() {
    try {
      // deactivate the GUI 
      webGUI.setVisible(false) ;
      
      // creating the OrderMessage 
      OrderMessage orderMsg = new OrderMessage(orderId, choice) ;
      // creating an ObjectMessage encapsulating the OrderMessage 
      ObjectMessage msgSent = tsession.createObjectMessage() ;
      msgSent.setObject(orderMsg) ;
      // sending the ObjectMessage to topicOrders 
      tp.publish(msgSent);
      
      System.out.println("Message sent by WebServer to topicOrders: ");
      System.out.println("Id: " + orderMsg.id);
      System.out.println("Item: " + orderMsg.item) ;
      
      // incrementing orderId
      orderId ++ ;
      
      // updating and reactivating GUI
      webGUI.updateId(orderId) ;
      webGUI.setVisible(true) ;
     
    } catch (Exception exc) {
      System.out.println("Exception caught in WebServer otherMethod: " + exc);
      exc.printStackTrace();
    }
  } 
  
  /**
   * Method called when pressing GUI's sendButton.
   */
  public void sendMethod() {
    try {
      // deactivate the GUI
      webGUI.setVisible(false) ;
      
      // creating the OrderMessage 
      OrderMessage orderMsg = new OrderMessage(orderId, choice) ;
      // creating an ObjectMessage encapsulating the OrderMessage
      ObjectMessage msgSent = tsession.createObjectMessage() ;
      msgSent.setObject(orderMsg) ;
      // sending the ObjectMessage to topicOrders
      tp.publish(msgSent);
      
      // commiting the sending(s) 
      tsession.commit() ;
      
      System.out.println("Message sent by WebServer to topicOrders: ");
      System.out.println("Id: " + orderMsg.id);
      System.out.println("Item: " + orderMsg.item) ;
      System.out.println("Sending(s) commited!") ;
      System.out.println() ;

      // resetting orderId 
      orderId = 1 ;
      
      // reactivating the GUI
      webGUI.updateId(orderId) ;
      webGUI.setVisible(true) ;
     
    } catch (Exception exc) {
      System.out.println("Exception caught in WebServer sendMethod: " + exc);
      exc.printStackTrace();
    }
  } 
  
  /**
   * Method called when pressing GUI's cancelButton.
   */
  public void cancelMethod() {
    try {
      // deactivate the GUI
      webGUI.setVisible(false) ;
      
      // rollinback the sending(s)
      tsession.rollback() ;
      
      // resetting orderId
      orderId = 1 ;
      
      // updating and reactivating GUI
      webGUI.updateId(orderId) ;
      webGUI.setVisible(true) ;
      
      System.out.println("Sending(s) rolledback!") ;
      System.out.println() ;
     
    } catch (Exception exc) {
      System.out.println("Exception caught in WebServer cancelMethod: " + exc);
      exc.printStackTrace();
    }
  } 
  
  /**
   * Method called when pressing GUI's quitButton.
   */
  public void quitMethod() {
    try {
      // desactivate the GUI
      webGUI.setVisible(false) ;
      
      // creating a QuitMessage 
      QuitMessage quitMsg = new QuitMessage() ;
      // creating an ObjectMessage encapsulating the QuitMessage
      ObjectMessage msgSent = tsession.createObjectMessage() ;
      msgSent.setObject(quitMsg) ;
      // sending the ObjectMessage to topicOrders
      tp.publish(msgSent);
      
      // commiting the sending
      tsession.commit() ;
      
      // closing session and connection
      tsession.close() ;
      tc.close() ;
      
      System.out.println("Session and connection closed by WebServer.") ;
      System.exit(0) ;
      
    } catch (Exception exc) {
      System.out.println("Exception caught in WebServer quitMethod: " + exc);
      exc.printStackTrace();
    }
  }
   
  /**
   * Method inherited from the Servers interface, not implemented.
   */
  public void okMethod() {}
  /**
   * Method inherited from the Servers interface, not implemented.
   */
  public void noMethod() {}
  /**
   * Method inherited from the Servers interface, not implemented.
   */
  public void closeMethod() {} 
}
