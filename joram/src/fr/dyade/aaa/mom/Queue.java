/*
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
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */
package fr.dyade.aaa.mom; 
 
import java.lang.*; 
import java.util.*; 
import fr.dyade.aaa.agent.*;
 
/** 
 * A <code>Queue</code> is a PTP mode destination object.
 * <p>
 * Modified: Frederic Maistre.
 * 
 *	@see  fr.dyade.aaa.mom.Topic 
 *	@see  fr.dyade.aaa.mom.AgentClient
 *	@see  fr.dyade.aaa.mom.Destination
 */ 
public class Queue extends fr.dyade.aaa.mom.Destination { 
  /**  
   * Vector in which messages are stored before delivery
   * and acknowledgement.
   */ 
  Vector messagesVector ; 
  /** Vector in which requests are stored. */
  Vector requestVector;

  /** Constructor. */  
  public Queue()
  {
    super();
    messagesVector = new Vector();
    requestVector = new Vector();	
  }  
  

  public void react(AgentId from, Notification not) throws Exception
  {
    if (not instanceof NotificationQueueSend) {
      notificationSend(from, (NotificationQueueSend) not);
    } else if (not instanceof NotificationAck) { 
      notificationAck(from, (NotificationAck) not); 
    } else if (not instanceof NotificationReceiveSync) { 
      notificationReceiveSync(from, (NotificationReceiveSync) not); 
    } else if (not instanceof NotificationReadOnly) { 
      notificationReadOnly(from, (NotificationReadOnly) not); 
    } else if (not instanceof NotificationCloseReception) { 
      notificationCloseReception(from, (NotificationCloseReception) not); 
    } else if (not instanceof NotificationRecover) { 
      notificationRecover(from, (NotificationRecover) not);
    } else if (not instanceof NotificationRollback) {
      notificationRollbackDeliveryQueueMessage(from, (NotificationRollback) not);
    } else { 
      super.react(from, not); 
    } 
  }


  /** 
   * Treatment of a NotificationQueueSend, holding the SendingMessageQueueMOMExtern
   * sent by the client (javax.jms.QueueSender.send() method).
   */ 
  protected void notificationSend(AgentId from, NotificationQueueSend not) throws Exception { 
    try { 
      int drvKey = not.driverKey;

      // Construction of a MessageAndAck. 
      fr.dyade.aaa.mom.MessageAndAck msgAndAck = 
        new fr.dyade.aaa.mom.MessageAndAck(not.msg); 
 	
      /* Checking the message's fields --> not implemented in mom.Destination
      if(!checkFieldsMessage(not.msg))
        throw (new MOMException("Fields of the Message Incomplete",
          MOMException.MESSAGE_INCOMPLETE));*/

      // Checking the message
      if(checkMessage(not.msg)) {
        // Pushing the MessageAndAck in the queue. 
        messagesVector.addElement(msgAndAck);
 
        if(Debug.debug)
          if(Debug.queueSend)
            Debug.printRequest("Send req : ",requestVector);

        // Checking the presence of requests in the requests vector.
        if(!requestVector.isEmpty()) {
          Selector selector = new Selector();

          if(Debug.debug)
            if(Debug.queueSend)
              System.out.println(requestVector.size());

          int i = 0;
          while(i < requestVector.size()) {
            fr.dyade.aaa.mom.RequestQueueObject agentRequest = 
              (fr.dyade.aaa.mom.RequestQueueObject) requestVector.elementAt(i);
            if((agentRequest.getTimeOut() < 0) || 
              ((System.currentTimeMillis() - agentRequest.getTimeOut()) < 0)) {

              // Checking with the Selector object.
              if(selector.matches(not.msg, agentRequest.getSelector())) {

                // Delivering the message. 
                deliveryQueueMessage(agentRequest.getAgentIdentity(), 
                  messagesVector.size()-1, agentRequest.getNotMOMID(), 
                  agentRequest.getSessionID(), agentRequest.getSelector(),
                  agentRequest.toListener, agentRequest.getDriversKey());

                requestVector.removeElementAt(i);
                break;
              } else
                i++;
            } else {
              // Removing the request because timeOut expired.
              requestVector.removeElementAt(i);
            }
          }
        }
      }

      // Delivering an agreement to the client if the message is persistent.
      // --------> to be done in AgentClient !!
      if(not.msg.getJMSDeliveryMode() ==fr.dyade.aaa.mom.Message.PERSISTENT)
        deliveryAgreement(from, not);

      // DEBUG
      if(Debug.debug) 
        if(Debug.queueSend)
  
        Debug.printQueue("queue send",messagesVector);

    } catch (MOMException exc) { 
      deliveryException (from, not, exc);
    } 
  } 

 
  /** 
   * Treatment of a NotificationReceiveSync, sent by the AgentClient after
   * having received a ReceptionMessageMOMExtern from the client
   * (javax.jms.QueueReceiver.receive() method).
   */ 
  protected void notificationReceiveSync(AgentId from, 
    NotificationReceiveSync not) throws Exception { 
    try {
      int index;
      int drvKey = not.driverKey;

      // Debug 
      if(Debug.debug)
        if(Debug.queueReceive)
          System.out.println(" REce Sync : "+from.toString());

      if((index = indexMsgAvailable(not)) < 0) {
        // Debug
        if(Debug.debug) 
          if(Debug.queueReceive)
            System.out.println(" Queue msg : ");

        if(not.timeOut == 0) {
          // In case of a receiveNoWait, delivering of a null message. 
          fr.dyade.aaa.mom.Message msg = null;
          fr.dyade.aaa.mom.NotifMessageFromQueue notMsgDeliv = 
            new fr.dyade.aaa.mom.NotifMessageFromQueue(not.notMOMID, 
            msg, not.selector, drvKey);
          notMsgDeliv.toListener = not.toListener;

          sendTo(from, notMsgDeliv);
        } else if(not.timeOut < 0) {
          fr.dyade.aaa.mom.RequestQueueObject requestQueueObject = 
            new fr.dyade.aaa.mom.RequestQueueObject(from, not.timeOut, 
            not.selector, not.notMOMID, not.sessionID, drvKey);
          requestQueueObject.toListener = not.toListener;

          requestVector.addElement(requestQueueObject);
        } else {
          if(not.timeOut-System.currentTimeMillis()>0) {
            fr.dyade.aaa.mom.RequestQueueObject requestQueueObject = 
              new fr.dyade.aaa.mom.RequestQueueObject(from, not.timeOut, 
              not.selector, not.notMOMID, not.sessionID, drvKey);
            requestQueueObject.toListener = not.toListener;

            requestVector.addElement(requestQueueObject);
          } else {
            // Delivery of a null message
            fr.dyade.aaa.mom.Message msg = null;
            fr.dyade.aaa.mom.NotifMessageFromQueue notMsgDeliv = 
              new fr.dyade.aaa.mom.NotifMessageFromQueue(not.notMOMID, 
              msg, not.selector, drvKey);
            notMsgDeliv.toListener = not.toListener;

            sendTo(from, notMsgDeliv);
          }
        }

        // Debug
        if(Debug.debug) 
          if(Debug.queueReceive)
            Debug.printRequest("requetes : ",requestVector);

      } else {
        // Delivering the first available message
        deliveryQueueMessage(from, index, not.notMOMID, not.sessionID,
          not.selector, not.toListener, drvKey);
      }
    } catch (MOMException exc) {
      deliveryException (from, not, exc);
    }	 
  } 

  /**
   *  a session of an agentClient acknowledges 1 or more messages 
   *  An exceptionNotification is triggered in case of problems
   *  so as to do only 1 pass to treat the request 
   */ 
  protected void notificationAck(AgentId from, NotificationAck not) throws Exception { 
    // DEBUG
    if(Debug.debug)
      if(Debug.queueAck) {
        Debug.printQueue("ack before ",messagesVector);
        System.out.println("queueSize : "+messagesVector.size());
      }
			
    // Removing previous messages.
    int i = 0;
    boolean messageNotFound = true;
    int drvKey = not.driverKey;
    searchMessage : while(i < messagesVector.size()) {
      fr.dyade.aaa.mom.MessageAndAck msgAndAck = 
        (fr.dyade.aaa.mom.MessageAndAck) messagesVector.elementAt(i);

      if(from.equals(msgAndAck.getAgentIdentity()) && 
        //(not.sessionID).equals(msgAndAck.getSessionID()) &&
        msgAndAck.getDriversKey() == drvKey) {

	    // DEBUG
	    if(Debug.debug)
	      if(Debug.queueAck)
	        System.out.println("\n sender "+from);
					
        // Testing all messages corresponding to the connection 
        // identified by the key belonging to the "from" AgentClient.
       if(msgAndAck.getMessage().getJMSMessageID().equals(not.messageID)) {
          messagesVector.removeElementAt(i);
          messageNotFound = false;

          // DEBUG
          if(Debug.debug)
            if(Debug.queueAck)
              System.out.println("\n ack "+from);

          break searchMessage;
        } else {
          // Removing previous messages.
          messagesVector.removeElementAt(i);
        }
      } else
        i++;
    }

    if(messageNotFound) {
      System.out.println("except "+from.toString()+" ID "+not.messageID);
      throw (new MOMException("No Existing MessageID",MOMException.MESSAGEID_NO_EXIST));
    }

    // Delivering an agreement to the client.
    if(not.ackMode != CommonClientAAA.AUTO_ACKNOWLEDGE) 
      deliveryAgreement(from, not);

    // DEBUG 
    if(Debug.debug)
      if(Debug.queueAck)
        Debug.printQueue("ack",messagesVector);
  } 
 

  /** the agentClient asks to receive synchronously a message from the Queue */ 
  protected void notificationReadOnly(AgentId from, NotificationReadOnly not) throws Exception { 
    try {	
      Vector queueEnumeration = new Vector(messagesVector.size());
      Selector selector = new Selector();
      int i =0;
		
			
      /* DEBUG */
      if(Debug.debug)
	if(Debug.queueRead) {
	  Debug.printQueue("read only ",messagesVector);
	  System.out.println("queueSize : "+messagesVector.size());
	}
		
      while(i<messagesVector.size()) {
	fr.dyade.aaa.mom.MessageAndAck msgAndAck = (fr.dyade.aaa.mom.MessageAndAck) messagesVector.elementAt(i);
				
	if(checkMessage(msgAndAck.getMessage())) {
						
	  /* checking with the Selector object */
	  if(selector.matches(msgAndAck.getMessage(),not.selector))
	    queueEnumeration.addElement(msgAndAck.getMessage());
	  i++;
	} else {
	  /* destruction of the message of the queue becauese timeOut expired*/
	  messagesVector.removeElementAt(i);
	}
      }
		
      /* construction of the notification with the Enumeration Object */
      fr.dyade.aaa.mom.NotifMessageEnumFromQueue notReadDeliv = new fr.dyade.aaa.mom.NotifMessageEnumFromQueue(not.notMOMID, queueEnumeration, not.driverKey);
      sendTo(from, notReadDeliv);
    } catch (MOMException exc) {
      deliveryException (from, not, exc); 
    } 
  } 


  /**
   * Method for sending a message to an AgentClient.
   */
  protected void deliveryQueueMessage (AgentId from, int index, long messageID, 
    String sessionID, String selector, boolean toListener, int driversKey) throws Exception {

    // Message to be sent
    fr.dyade.aaa.mom.MessageAndAck msgAndAck = 
      (fr.dyade.aaa.mom.MessageAndAck) messagesVector.elementAt(index);

    // Delivering the message
    fr.dyade.aaa.mom.NotifMessageFromQueue notMsgDeliv = 
      new fr.dyade.aaa.mom.NotifMessageFromQueue(messageID, 
      msgAndAck.getMessage(), selector, driversKey);
    notMsgDeliv.toListener = toListener;

    sendTo(from, notMsgDeliv);

    // Updating the MessageAndAck 
    msgAndAck.setAgentIdentity(from, sessionID, driversKey);

    // DEBUG
    if(Debug.debug) {
      if(Debug.queueDelivery && 
        (msgAndAck.getMessage() instanceof fr.dyade.aaa.mom.TextMessage)) {
        Debug.printQueue("deliver",messagesVector);
        System.out.println("corps " +
          ((fr.dyade.aaa.mom.TextMessage) msgAndAck.getMessage()).getText());	
      }
    }
  }


  /** the queue receives a notification of rollback for a particular message*/
  protected void notificationRollbackDeliveryQueueMessage(AgentId from, NotificationRollback not) throws Exception{
    Enumeration e = messagesVector.elements();
    int drvKey = not.driverKey;
		
    /* reput the message to rollback in the queue */
    while(e.hasMoreElements()) {
      fr.dyade.aaa.mom.MessageAndAck msgAndAck = (fr.dyade.aaa.mom.MessageAndAck) e.nextElement();
					
      if( from.equals(msgAndAck.getAgentIdentity()) && msgAndAck.getMessage().getJMSMessageID().equals(not.messageID) &&
      //(msgAndAck.getSessionID().equals(not.sessionID)) &&
      msgAndAck.getDriversKey() == drvKey) {
	/* restore the initial parameter of the msgAndAck */ 
	msgAndAck.setAgentIdentity(null,null, 0);
					
	/* marks the message as Redelivered */
	msgAndAck.getMessage().setJMSRedelivered(true);
	break;
      } 
    }
  }


  /** 
   * Method testing if a message is available for a specific request 
   * (selector). Returns the index if exists, -1 otherwise.
   */
  protected int indexMsgAvailable(NotificationReceiveSync not) throws Exception {
    int i = 0;
    Selector selector = new Selector();
    while(i < messagesVector.size()) {
      fr.dyade.aaa.mom.MessageAndAck msgAndAck = 
        (fr.dyade.aaa.mom.MessageAndAck) messagesVector.elementAt(i);

      if(checkMessage(msgAndAck.getMessage())) {
        /* the selector makes testing if a mesage was delivered or not */
        /* because messages are not destroyed if selector is not OK */
        if((msgAndAck.getAgentIdentity() == null) && 
          (selector.matches(msgAndAck.getMessage(), not.selector))) {
          return i;
        } else
	      i++;
      } else {
        messagesVector.removeElementAt(i);
      }
    }
    return -1;
  }
 
 
  protected void notificationCloseReception(AgentId from, 
    NotificationCloseReception not) throws Exception { 
    int drvKey = not.driverKey;
    Enumeration e = messagesVector.elements();
		
    // Reseting the messages not delivered to the connection identified
    // by the drvKey of the "from" agent.
    while(e.hasMoreElements()) {
      fr.dyade.aaa.mom.MessageAndAck msgAndAck = 
        (fr.dyade.aaa.mom.MessageAndAck) e.nextElement();

      if(from.equals(msgAndAck.getAgentIdentity()) &&
         drvKey == msgAndAck.getDriversKey()) {
        // Restoring the initial parameters of the msgAndAck
        msgAndAck.setAgentIdentity(null,null,0);
				
	    // Marking the message as Redelivered
	    msgAndAck.getMessage().setJMSRedelivered(true);
      } 
    }
		
    /* destroys its requests */
    int i=0;
    while(i<requestVector.size()) {
      fr.dyade.aaa.mom.RequestQueueObject reqQueueObj = (fr.dyade.aaa.mom.RequestQueueObject) requestVector.elementAt(i);
				
      if(from.equals(reqQueueObj.getAgentIdentity()) &&
         reqQueueObj.getDriversKey() == drvKey) {
	/* restore the initial parameter of the msgAndAck */ 
	requestVector.removeElementAt(i);
      } else
	i++;
    }

  } 

	
  /** the queue receives a notification of recover for a particular message */ 
  protected void notificationRecover(AgentId from, NotificationRecover not) throws Exception{ 
    Enumeration e = messagesVector.elements();
	
    int index = -1;	
    while(e.hasMoreElements()) {
      index ++;
      fr.dyade.aaa.mom.MessageAndAck msgAndAck = (fr.dyade.aaa.mom.MessageAndAck) e.nextElement();

      if(from.equals(msgAndAck.getAgentIdentity()) && msgAndAck.getMessage().getJMSMessageID().equals(not.messageID)) {
	/* restore the initial parameter of the msgAndAck */
	msgAndAck.setAgentIdentity(null,null, 0);

	/* marks the message as Redelivered */
	msgAndAck.getMessage().setJMSRedelivered(true);
      if(!requestVector.isEmpty()) {
      Selector selector = new Selector();

          int i = 0;
          while (i < requestVector.size()) {
            fr.dyade.aaa.mom.RequestQueueObject agentRequest = 
              (fr.dyade.aaa.mom.RequestQueueObject) requestVector.elementAt(i);
            if((agentRequest.getTimeOut() < 0) || 
              ((System.currentTimeMillis() - agentRequest.getTimeOut()) < 0)) {

              // Checking with the Selector object.
              if(selector.matches(msgAndAck.msg, agentRequest.getSelector())) {

                // Delivering the message. 
                deliveryQueueMessage(agentRequest.getAgentIdentity(), 
                  index, agentRequest.getNotMOMID(), 
                  agentRequest.getSessionID(), agentRequest.getSelector(),
                  agentRequest.toListener, agentRequest.getDriversKey());
                requestVector.removeElementAt(i);
                break;
              } else
                i++;
            } else {
              // Removing the request because timeOut expired.
              requestVector.removeElementAt(i);
            }
          }
        }
					
	break;
      } 
      else {
	  /* restore the initial parameter of the msgAndAck */ 
	  msgAndAck.setAgentIdentity(null,null, 0);
	  /* marks the message as Redelivered */
	  msgAndAck.getMessage().setJMSRedelivered(true);
      if(!requestVector.isEmpty()) {
      Selector selector = new Selector();

          int i = 0;
          while (i < requestVector.size()) {
            fr.dyade.aaa.mom.RequestQueueObject agentRequest = 
              (fr.dyade.aaa.mom.RequestQueueObject) requestVector.elementAt(i);
            if((agentRequest.getTimeOut() < 0) || 
              ((System.currentTimeMillis() - agentRequest.getTimeOut()) < 0)) {

              // Checking with the Selector object.
              if(selector.matches(msgAndAck.msg, agentRequest.getSelector())) {

                // Delivering the message. 
                deliveryQueueMessage(agentRequest.getAgentIdentity(), 
                  index, agentRequest.getNotMOMID(), 
                  agentRequest.getSessionID(), agentRequest.getSelector(),
                  agentRequest.toListener, agentRequest.getDriversKey());
                requestVector.removeElementAt(i);
                break;
              } else
                i++;
            } else {
              // Removing the request because timeOut expired.
              requestVector.removeElementAt(i);
            }
          }
        }
					
      }
    }
    /* delivers an agreement */
    deliveryAgreement(from, not);
  }
}
