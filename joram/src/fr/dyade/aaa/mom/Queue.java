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
 *	a Queue Object is a queue where client are able to get messages one after one
 *	and synchronously 
 * 
 *	@see		fr.dyade.aaa.mom.Topic 
 *	@see		fr.dyade.aaa.mom.AgentClient
 *	@see		fr.dyade.aaa.mom.Destination
 */ 
 
public class Queue extends fr.dyade.aaa.mom.Destination { 
 
  /**  
   * the Queue variable so as to keep messages before delivery and acknowledge  
   * the objects of this Queue are MessageAndAck 
   */ 
  Vector queueMessage ; 
  
  /**
   *	this vector holds the list of the request with no reference of time
   *	or with a timeValue
   */
  Vector requestVector;
  
  /** constructor */ 
  public Queue() {
    super();
    queueMessage = new Vector();
    requestVector = new Vector();	
  }  
  
	 
  public void react(AgentId from, Notification not) throws Exception {
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

  /** the Queue receives a Message from a agentClient thanks to NotificationQueueSend */ 
  protected void notificationSend(AgentId from, NotificationQueueSend not) throws Exception { 
    try { 
      /* construction of an object MessageAndAck thanks to the parameter in the notification */ 
      fr.dyade.aaa.mom.MessageAndAck msgAndAck = new fr.dyade.aaa.mom.MessageAndAck(not.msg); 
		 	
      /* check the fields of the message : destroyed if incomplete */
      if(!checkFieldsMessage(not.msg))
	throw (new MOMException("Fields of the Message Incomplete",MOMException.MESSAGE_INCOMPLETE));
			
      /* check the Message : option of the message*/ 
      if(checkMessage(not.msg)) {
		 
	/* placement of the created object in the Queue with priority */ 
	putMessageInQueue(msgAndAck);  
			 
	/* DEBUG : print the Queue */
	if(Debug.debug)
	  if(Debug.queueSend)
	    Debug.printRequest("Send req : ",requestVector);
			
	/* checking of the presence of requests in the request vector */
	if(!requestVector.isEmpty()) {
	  boolean noGiven = true;
	  int i = 0;
	  fr.dyade.aaa.mom.Selector selecObj = new fr.dyade.aaa.mom.Selector();
				
	  if(Debug.debug)
	    if(Debug.queueSend)
	      System.out.println(requestVector.size());
				
	  while(i<requestVector.size()) {
	    fr.dyade.aaa.mom.RequestQueueObject agentRequest = (fr.dyade.aaa.mom.RequestQueueObject) requestVector.elementAt(i);
	    if((agentRequest.getTimeOut()<0) || ((System.currentTimeMillis()-agentRequest.getTimeOut())<0)) {
						
	      /* checking with the Selector object */
	      if(selecObj.isAvailable(not.msg,agentRequest.getSelector())) {			
		deliveryQueueMessage(agentRequest.getAgentIdentity(), queueMessage.size()-1, agentRequest.getNotMOMID(), agentRequest.getSessionID());
		requestVector.removeElementAt(i);
		break;
	      } else
		i++;
	    } else {
	      /* destruction of a request of the queue because timeOut expired*/
	      requestVector.removeElementAt(i);
	    }
	  }
	}
			 
      }
			
      /* deliver an agreement to the client if Persistent */
      if(not.msg.getJMSDeliveryMode()==fr.dyade.aaa.mom.Message.PERSISTENT)
	deliveryAgreement(from, not);
			
      /* DEBUG : print the Queue */
      if(Debug.debug) 
	if(Debug.queueSend)
	  Debug.printQueue("queue send",queueMessage);
			 
    } catch (MOMException exc) { 
      deliveryException (from, not, exc);
			
      /* DEBUG : print the Queue */
      if(Debug.debug) 
	if(Debug.queueSend)
	  Debug.printQueue("send",queueMessage);
			
    } 
  } 
 
  /** the agentClient asks to receive synchronously a message from the Queue */ 
  protected void notificationReceiveSync(AgentId from, NotificationReceiveSync not) throws Exception { 
    try {
      int index;
       		
      /* DEBUG */
      if(Debug.debug)
	if(Debug.queueReceive)
	  System.out.println(" REce Sync : "+from.toString());

      if((index = indexMsgAvailable(not))<0) {
		
	/* DEBUG */
	if(Debug.debug) 
	  if(Debug.queueReceive)
	    System.out.println(" Queue msg : ");
				
	/* No message available */
	if(not.timeOut==0) {
	  /* case of receiveNoWait */
	  /* delivery of null message */
	  fr.dyade.aaa.mom.Message msg = null;
	  fr.dyade.aaa.mom.NotificationMessageDeliver notMsgDeliv = new fr.dyade.aaa.mom.NotificationMessageDeliver(not.notMOMID, msg);
	  sendTo(from, notMsgDeliv);
	} else if(not.timeOut<0){
	  /* case of delivery before timeout or time indefinitely */
	  fr.dyade.aaa.mom.RequestQueueObject requestQueueObject = new fr.dyade.aaa.mom.RequestQueueObject(from, not.timeOut, not.selector, not.notMOMID, not.sessionID);
	  requestVector.addElement(requestQueueObject);
	} else {
	  /* case of delivery before timeout */
	  if(not.timeOut-System.currentTimeMillis()>0) {
	    fr.dyade.aaa.mom.RequestQueueObject requestQueueObject = new fr.dyade.aaa.mom.RequestQueueObject(from, not.timeOut, not.selector, not.notMOMID, not.sessionID);
	    requestVector.addElement(requestQueueObject);
	  } else {
	    /* delivery of null message */
	    fr.dyade.aaa.mom.Message msg = null;
	    fr.dyade.aaa.mom.NotificationMessageDeliver notMsgDeliv = new fr.dyade.aaa.mom.NotificationMessageDeliver(not.notMOMID, msg);
	    sendTo(from, notMsgDeliv);
	  }
	}
				
	/* DEBUG */
	if(Debug.debug) 
	  if(Debug.queueReceive)
	    Debug.printRequest("requetes : ",requestVector);
						
      } else {
	/* at least, one message available */
	deliveryQueueMessage(from, index, not.notMOMID, not.sessionID);
      }
				
    } catch (MOMException exc) {
      deliveryException (from, not, exc);
    }	 
  } 
 
 
  /** a session of an agentClient acknowledges 1 or more messages 
   *  An exceptionNotification is triggered in case of problems
   *  so as to do only 1 pass to treat the request 
   */ 
  protected void notificationAck(AgentId from, NotificationAck not) throws Exception { 
    int i = 0;
    boolean messageNotFound = true;
		
    /* DEBUG */
    if(Debug.debug)
      if(Debug.queueAck) {
	Debug.printQueue("ack before ",queueMessage);
	System.out.println("queueSize : "+queueMessage.size());
      }
			
    /* destruction of all previous messages */ 
    searchMessage : while(i<queueMessage.size()) {
      fr.dyade.aaa.mom.MessageAndAck msgAndAck = (fr.dyade.aaa.mom.MessageAndAck) queueMessage.elementAt(i);
      if(from.equals(msgAndAck.getAgentIdentity()) && (not.sessionID).equals(msgAndAck.getSessionID())) {
					
	/* DEBUG */
	if(Debug.debug)
	  if(Debug.queueAck)
	    System.out.println("\n sender "+from);
					
	/* tests for all messages from the "from" agentClient */
	if(msgAndAck.getMessage().getJMSMessageID().equals(not.messageID)) {
	  queueMessage.removeElementAt(i);
	  messageNotFound = false;
				
	  /* DEBUG */
	  if(Debug.debug)
	    if(Debug.queueAck)
	      System.out.println("\n ack "+from);
						
	  break searchMessage;
	} else {
	  /* destuction of previous messages */
	  queueMessage.removeElementAt(i);
	}
      } else
	i++;
    }
			
    /* if no messageID exists, no message is destroyed thanks to the engine AAA */
    if(messageNotFound) {
      System.out.println("except "+from.toString()+" ID "+not.messageID);
      throw (new MOMException("No Existing MessageID",MOMException.MESSAGEID_NO_EXIST));
    }
		
    /* deliver an agreement to the client */
    if(not.ackMode!=CommonClientAAA.AUTO_ACKNOWLEDGE) 
      deliveryAgreement(from, not);
			
    /* DEBUG */
    if(Debug.debug)
      if(Debug.queueAck)
	Debug.printQueue("ack",queueMessage);
					
		
  } 
 
	 
  /** the agentClient asks to receive synchronously a message from the Queue */ 
  protected void notificationReadOnly(AgentId from, NotificationReadOnly not) throws Exception { 
    try {	
      Vector queueEnumeration = new Vector(queueMessage.size());
      fr.dyade.aaa.mom.Selector selecObj = new fr.dyade.aaa.mom.Selector();
      int i =0;
		
			
      /* DEBUG */
      if(Debug.debug)
	if(Debug.queueRead) {
	  Debug.printQueue("read only ",queueMessage);
	  System.out.println("queueSize : "+queueMessage.size());
	}
		
      while(i<queueMessage.size()) {
	fr.dyade.aaa.mom.MessageAndAck msgAndAck = (fr.dyade.aaa.mom.MessageAndAck) queueMessage.elementAt(i);
				
	if(checkMessage(msgAndAck.getMessage())) {
						
	  /* checking with the Selector object */
	  if(selecObj.isAvailable(msgAndAck.getMessage(),not.selector))
	    queueEnumeration.addElement(msgAndAck.getMessage());
	  i++;
	} else {
	  /* destruction of the message of the queue becauese timeOut expired*/
	  queueMessage.removeElementAt(i);
	}
      }
		
      /* construction of the notification with the Enumeration Object */
      fr.dyade.aaa.mom.NotificationReadDeliver notReadDeliv = new fr.dyade.aaa.mom.NotificationReadDeliver(not.notMOMID, queueEnumeration);
      sendTo(from, notReadDeliv);
    } catch (MOMException exc) {
      deliveryException (from, not, exc); 
    } 
  } 
	
  /** send a message to an agent */
  protected void deliveryQueueMessage (AgentId from, int index, long messageID, String sessionID) throws Exception {
    /* at least, one message available */
    fr.dyade.aaa.mom.MessageAndAck msgAndAck = (fr.dyade.aaa.mom.MessageAndAck) queueMessage.elementAt(index);
				
    /* delivery of the message */
    fr.dyade.aaa.mom.NotificationMessageDeliver notMsgDeliv = new fr.dyade.aaa.mom.NotificationMessageDeliver(messageID, msgAndAck.getMessage());
    sendTo(from, notMsgDeliv);
			
    /* update of the MessageAndAck */
    msgAndAck.setAgentIdentity(from, sessionID);
	
    /* DEBUG : print the Queue */
    if(Debug.debug) {
      if(Debug.queueDelivery && (msgAndAck.getMessage() instanceof fr.dyade.aaa.mom.TextMessage)) {
	Debug.printQueue("deliver",queueMessage);
	System.out.println("corps "+((fr.dyade.aaa.mom.TextMessage) msgAndAck.getMessage()).getText());	
      }
    }
  }

  /** the queue receives a notification of rollback for a particular message*/
  protected void notificationRollbackDeliveryQueueMessage(AgentId from, NotificationRollback not) throws Exception{
    Enumeration e = queueMessage.elements();
		
    /* reput the message to rollback in the queue */
    while(e.hasMoreElements()) {
      fr.dyade.aaa.mom.MessageAndAck msgAndAck = (fr.dyade.aaa.mom.MessageAndAck) e.nextElement();
					
      if( from.equals(msgAndAck.getAgentIdentity()) && msgAndAck.getMessage().getJMSMessageID().equals(not.messageID) && (msgAndAck.getSessionID().equals(not.sessionID)) ) {
	/* restore the initial parameter of the msgAndAck */ 
	msgAndAck.setAgentIdentity(null,null);
					
	/* marks the message as Redelivered */
	msgAndAck.getMessage().setJMSRedelivered(true);
	break;
      } 
    }
  }
	
  /** tests if a message is available for a paricular request (selector) 
   * return the index if exists and -1 otherwise
   */
  protected int indexMsgAvailable(NotificationReceiveSync not) throws Exception {
    int i = 0;
    fr.dyade.aaa.mom.Selector selecObj = new fr.dyade.aaa.mom.Selector();
		
    while(i<queueMessage.size()) {
      fr.dyade.aaa.mom.MessageAndAck msgAndAck = (fr.dyade.aaa.mom.MessageAndAck) queueMessage.elementAt(i);
      if(checkMessage(msgAndAck.getMessage())) {
	/* the selector makes testing if a mesage was delivered or not */
	/* because messages are not destroyed if selector is not OK */
	if((msgAndAck.getAgentIdentity()==null) && (selecObj.isAvailable(msgAndAck.getMessage(),not.selector))) {
	  return i;
	} else
	  i++;
      } else {
	/* destruction of a request of the queue becauese timeOut expired*/
	queueMessage.removeElementAt(i);
      }
    }
    return -1;
  }
  
  /** put a message MessageEndAck ina Queue with management of priority 
   *  1 queue for put the message with 6 pointers (pointer of priority) 
   *  1 queue for ack message so as to preserve ordering of message if 
   *  client wants to acknowledge handly
   */
  protected void putMessageInQueue(fr.dyade.aaa.mom.MessageAndAck msgAndAck) {
    queueMessage.addElement(msgAndAck);
  }
	
  /** the agentClient acknowledges 1 or more messages 
   *  An exceptionNotification is triggered in case of probleme
   *  so as to do only 1 pass to treat the request 
   */ 
  protected void notificationCloseReception(AgentId from, NotificationCloseReception not) throws Exception { 
    Enumeration e = queueMessage.elements();
		
    /* resets the message no delivered to the "from" agent */
    while(e.hasMoreElements()) {
      fr.dyade.aaa.mom.MessageAndAck msgAndAck = (fr.dyade.aaa.mom.MessageAndAck) e.nextElement();
				
      if(from.equals(msgAndAck.getAgentIdentity())) {
	/* restore the initial parameter of the msgAndAck */ 
	msgAndAck.setAgentIdentity(null,null);
				
	/* marks the message as Redelivered */
	msgAndAck.getMessage().setJMSRedelivered(true);
      } 
    }
		
    /* destroys its requests */
    int i=0;
    while(i<requestVector.size()) {
      fr.dyade.aaa.mom.RequestQueueObject reqQueueObj = (fr.dyade.aaa.mom.RequestQueueObject) requestVector.elementAt(i);
				
      if(from.equals(reqQueueObj.getAgentIdentity())) {
	/* restore the initial parameter of the msgAndAck */ 
	requestVector.removeElementAt(i);
      } else
	i++;
    }
  } 
	
  /** the queue receives a notification of recover for a particular message */ 
  protected void notificationRecover(AgentId from, NotificationRecover not) throws Exception{ 
    Enumeration e = queueMessage.elements();
		
    /* reput the message to rollback in the queue */
    while(e.hasMoreElements()) {
      fr.dyade.aaa.mom.MessageAndAck msgAndAck = (fr.dyade.aaa.mom.MessageAndAck) e.nextElement();
					
      if(from.equals(msgAndAck.getAgentIdentity()) && msgAndAck.getMessage().getJMSMessageID().equals(not.messageID)) {
	/* restore the initial parameter of the msgAndAck */ 
	msgAndAck.setAgentIdentity(null,null);
					
	/* marks the message as Redelivered */
	msgAndAck.getMessage().setJMSRedelivered(true);
	break;
      } 
    }
    /* delivers an agreement */
    deliveryAgreement(from, not);
  }
	

}
