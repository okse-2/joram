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


package fr.dyade.aaa.joram; 

import java.lang.*; 
import java.util.*;  

/** 
 *	a QueueSession is as JMS specifications 
 *	InvalidException is never thrown at time in this Object
 * 
 *	@see javax.jms.QueueSession 
 *	@see fr.dyade.aaa.joram.Session
 *	@see javax.jms.Session 
 */ 
 
public class QueueSession extends fr.dyade.aaa.joram.Session implements javax.jms.QueueSession { 
  
    public QueueSession(boolean transacted, int acknowledgeMode, long sessionIDNew, Connection refConnectionNew) {
	super(transacted, acknowledgeMode, sessionIDNew, refConnectionNew);
    }
    
    /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
    public  javax.jms.Queue createQueue(java.lang.String queueName) throws javax.jms.JMSException {
	throw (new fr.dyade.aaa.joram.JMSAAAException("Not yet available",JMSAAAException.NOT_YET_AVAILABLE));
    }
  
    /**	The InvalidException is at time never thrown so it could arrive that
     *	the name of the Queue is invalid
     *	@see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications
     */
    public  javax.jms.QueueReceiver createReceiver(javax.jms.Queue queue) throws javax.jms.JMSException {
	return this.createReceiver(queue, "");
    }
 
 
  public javax.jms.QueueReceiver createReceiver(javax.jms.Queue queue,
    java.lang.String messageSelector) throws javax.jms.JMSException
  {
    try {

      if (messageSelector != null && ! messageSelector.equals("")) {
        fr.dyade.aaa.mom.selectors.checkParser parser =
          new fr.dyade.aaa.mom.selectors.checkParser(
          new fr.dyade.aaa.mom.selectors.Lexer(messageSelector));

        // If syntax is wrong, throws a javax.jms.InvalidSelectorException.
        Object result = parser.parse().value;
      }

        if (super.messageListener != null)
          throw new javax.jms.JMSException("Canno't create a receiver in a session that has a messageListener set");

        
        
	    Object obj = new Object();
	    long messageJMSMOMID = refConnection.getMessageMOMID();
	    Long longMsgID = new Long(messageJMSMOMID);
	    fr.dyade.aaa.mom.CreationWorkerQueueMOMExtern msgCreation = new fr.dyade.aaa.mom.CreationWorkerQueueMOMExtern(messageJMSMOMID, (fr.dyade.aaa.mom.QueueNaming) queue);
	    /*	synchronization because it could arrive that the notify was
	     *	called before the wait 
	     */
	    synchronized(obj) {
		/* the processus of the client waits the response */
		refConnection.waitThreadTable.put(longMsgID,obj);
		/* get the messageJMSMOM identifier */
		this.sendToConnection(msgCreation);
	
		obj.wait();
	    }
      
	    /* the clients wakes up */
	    fr.dyade.aaa.mom.MessageMOMExtern msgMOM;
      
	    /* tests if the key exists 
	     * dissociates the message null (on receiveNoWait) and internal error
	     */
	    if(!refConnection.messageJMSMOMTable.containsKey(longMsgID))
		throw (new fr.dyade.aaa.joram.JMSAAAException("No Request Agree received",JMSAAAException.ERROR_NO_MESSAGE_AVAILABLE));
      
	    /* get the the message back or the exception*/
	    msgMOM = (fr.dyade.aaa.mom.MessageMOMExtern) refConnection.messageJMSMOMTable.remove(longMsgID);
	    if(msgMOM instanceof fr.dyade.aaa.mom.RequestAgreeMOMExtern) {
		fr.dyade.aaa.joram.QueueReceiver queueReceiver = new fr.dyade.aaa.joram.QueueReceiver( new Long(counterConsumerID).toString(), refConnection, this, queue, messageSelector);
        counterConsumerID ++;
		if(queueReceiver==null)
		    throw (new fr.dyade.aaa.joram.JMSAAAException("Internal Error during creation QueueReceiver",JMSAAAException.ERROR_CREATION_MESSAGECONSUMER));
	
		/* inscription in the Table of the Session */
		synchronized(messageConsumerTable) {	
		    java.util.Vector v;
		    if((v = (java.util.Vector) messageConsumerTable.get((fr.dyade.aaa.mom.QueueNaming) queue))!=null) {
			/* already a MessageConsumer of this session is receiving from the queue */
			v.addElement(queueReceiver);
		    } else {
			/* first MessageConsumer receiving from the Queue */
			v = new java.util.Vector();
			v.addElement(queueReceiver);
			messageConsumerTable.put((fr.dyade.aaa.mom.QueueNaming) queue, v);
		    }
		}
		return queueReceiver;
	    } else if(msgMOM instanceof fr.dyade.aaa.mom.ExceptionMessageMOMExtern) {
		/* exception sent back to the client */
		fr.dyade.aaa.mom.ExceptionMessageMOMExtern msgExc = (fr.dyade.aaa.mom.ExceptionMessageMOMExtern) msgMOM;
		fr.dyade.aaa.joram.JMSAAAException except = new fr.dyade.aaa.joram.JMSAAAException("MOM Internal Error : ",JMSAAAException.MOM_INTERNAL_ERROR);
		except.setLinkedException(msgExc.exception);
		throw(except);
	    } else {
		/* unknown message */
		/* should never arrived */
		fr.dyade.aaa.joram.JMSAAAException except = new fr.dyade.aaa.joram.JMSAAAException("MOM Internal Error : ",JMSAAAException.MOM_INTERNAL_ERROR);
		throw(except);
	    }
	} catch (javax.jms.JMSException exc) {
	    throw(exc);
	} catch (Exception exc) {
	    javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
	    except.setLinkedException(exc);
	    throw(except);
	}
    
    }
  
    /**	The InvalidException is at time never thrown so it could arrive that
     *	the name of the Queue is invalid
     *	But the exception is thrown during the send method
     *	@see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications
     */
    public  javax.jms.QueueSender createSender(javax.jms.Queue queue) throws javax.jms.JMSException {
	fr.dyade.aaa.joram.QueueSender queueSender = new fr.dyade.aaa.joram.QueueSender(refConnection, this, queue);
	if(queueSender==null)
	    throw (new fr.dyade.aaa.joram.JMSAAAException("Internal Error during creation QueueSender",JMSAAAException.ERROR_CREATION_MESSAGECONSUMER));
	else 
	    return queueSender;
    }
  
    /**	The InvalidException is at time never thrown so it could arrive that
     *	the name of the Queue is invalid
     *	@see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications
     */
    public  javax.jms.QueueBrowser createBrowser(javax.jms.Queue queue) throws javax.jms.JMSException {
	return this.createBrowser(queue, "");
    }
  
    /**	The InvalidException is at time never thrown so it could arrive that
     *	the name of the Queue is invalid
     *	@see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications
     */
    public  javax.jms.QueueBrowser createBrowser(javax.jms.Queue queue, java.lang.String messageSelector) throws javax.jms.JMSException {
	try {
	    Object obj = new Object();
	    long messageJMSMOMID = refConnection.getMessageMOMID();
	    Long longMsgID = new Long(messageJMSMOMID);
      
	    fr.dyade.aaa.mom.CreationWorkerQueueMOMExtern msgCreation = new fr.dyade.aaa.mom.CreationWorkerQueueMOMExtern(messageJMSMOMID, (fr.dyade.aaa.mom.QueueNaming) queue);
	    /*	synchronization because it could arrive that the notify was
	     *	called before the wait 
	     */
	    synchronized(obj) {
		/* the processus of the client waits the response */
		refConnection.waitThreadTable.put(longMsgID,obj);
		/* get the messageJMSMOM identifier */
		this.sendToConnection(msgCreation);
				
		obj.wait();
	    }
      
	    /* the clients wakes up */
	    fr.dyade.aaa.mom.MessageMOMExtern msgMOM;
      
	    /* tests if the key exists 
	     * dissociates the message null (on receiveNoWait) and internal error
	     */
	    if(!refConnection.messageJMSMOMTable.containsKey(longMsgID))
		throw (new fr.dyade.aaa.joram.JMSAAAException("No Request Agree received",JMSAAAException.ERROR_NO_MESSAGE_AVAILABLE));
      
	    /* get the the message back or the exception*/
	    msgMOM = (fr.dyade.aaa.mom.MessageMOMExtern) refConnection.messageJMSMOMTable.remove(longMsgID);
	    if(msgMOM instanceof fr.dyade.aaa.mom.RequestAgreeMOMExtern) {
		fr.dyade.aaa.joram.QueueBrowser queueBrowser = new fr.dyade.aaa.joram.QueueBrowser(refConnection, this, queue, messageSelector);
		if(queueBrowser==null)
		    throw (new fr.dyade.aaa.joram.JMSAAAException("Internal Error during creation QueueSender",JMSAAAException.ERROR_CREATION_MESSAGECONSUMER));
		else 
		    return queueBrowser;
	    } else if(msgMOM instanceof fr.dyade.aaa.mom.ExceptionMessageMOMExtern) {
		/* exception sent back to the client */
		fr.dyade.aaa.mom.ExceptionMessageMOMExtern msgExc = (fr.dyade.aaa.mom.ExceptionMessageMOMExtern) msgMOM;
		fr.dyade.aaa.joram.JMSAAAException except = new fr.dyade.aaa.joram.JMSAAAException("MOM Internal Error : ",JMSAAAException.MOM_INTERNAL_ERROR);
		except.setLinkedException(msgExc.exception);
		throw(except);
	    } else {
		/* unknown message */
		/* should never arrived */
		fr.dyade.aaa.joram.JMSAAAException except = new fr.dyade.aaa.joram.JMSAAAException("MOM Internal Error : ",JMSAAAException.MOM_INTERNAL_ERROR);
		throw(except);
	    }
	} catch (javax.jms.JMSException exc) {
	    throw(exc);
	} catch (Exception exc) {
	    javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
	    except.setLinkedException(exc);
	    throw(except);
	}
    }
  
    /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
    public  javax.jms.TemporaryQueue createTemporaryQueue() throws javax.jms.JMSException {
	try {
	    Object obj = new Object();
	    long messageJMSMOMID = refConnection.getMessageMOMID();
	    Long longMsgID = new Long(messageJMSMOMID);
      
	    fr.dyade.aaa.mom.CreationTemporaryQueueMOMExtern msgCreation = new fr.dyade.aaa.mom.CreationTemporaryQueueMOMExtern(messageJMSMOMID);
	    /*	synchronization because it could arrive that the notify was
	     *	called before the wait 
	     */
	    synchronized(obj) {
		/* the processus of the client waits the response */
		refConnection.waitThreadTable.put(longMsgID,obj);
		/* get the messageJMSMOM identifier */
		this.sendToConnection(msgCreation);
	
		obj.wait();
	    }
      
	    /* the clients wakes up */
	    fr.dyade.aaa.mom.MessageMOMExtern msgMOM;
      
	    /* tests if the key exists 
	     * dissociates the message null (on receiveNoWait) and internal error
	     */
	    if(!refConnection.messageJMSMOMTable.containsKey(longMsgID))
		throw (new fr.dyade.aaa.joram.JMSAAAException("No Request Agree received",JMSAAAException.ERROR_NO_MESSAGE_AVAILABLE));
      
	    /* get the the message back or the exception*/
	    msgMOM = (fr.dyade.aaa.mom.MessageMOMExtern) refConnection.messageJMSMOMTable.remove(longMsgID);
	    if(msgMOM instanceof fr.dyade.aaa.mom.CreationBackDestinationMOMExtern) {
		/* return the temporaryTopic Object with the name given by the agentClient */
		fr.dyade.aaa.mom.QueueNaming queue = (fr.dyade.aaa.mom.QueueNaming) ((fr.dyade.aaa.mom.CreationBackDestinationMOMExtern) msgMOM).destination;
		fr.dyade.aaa.joram.TemporaryQueue tempQueue = new fr.dyade.aaa.joram.TemporaryQueue(refConnection, this, queue.getQueueName());
		return tempQueue;
	    } else if(msgMOM instanceof fr.dyade.aaa.mom.ExceptionMessageMOMExtern) {
		/* exception sent back to the client */
		fr.dyade.aaa.mom.ExceptionMessageMOMExtern msgExc = (fr.dyade.aaa.mom.ExceptionMessageMOMExtern) msgMOM;
		fr.dyade.aaa.joram.JMSAAAException except = new fr.dyade.aaa.joram.JMSAAAException("MOM Internal Error : ",JMSAAAException.MOM_INTERNAL_ERROR);
		except.setLinkedException(msgExc.exception);
		throw(except);
	    } else {
		/* unknown message */
		/* should never arrived */
		fr.dyade.aaa.joram.JMSAAAException except = new fr.dyade.aaa.joram.JMSAAAException("MOM Internal Error : ",JMSAAAException.MOM_INTERNAL_ERROR);
		throw(except);
	    }
	} catch (Exception exc) {
	    javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
	    except.setLinkedException(exc);
	    throw(except);
	}
    }
  
    /**overwrite the methode from MessageConsumer  */
    public void close()  throws javax.jms.JMSException {
      if (listener != null)
        listener.stop();
	  messageConsumerTable.clear();
      super.close() ;
    }

    /** prepares the messages to acknowledge so as to decrease the overhead 
     */
    protected Vector preparesHandlyAck(String messageID, long messageJMSMOMID) throws javax.jms.JMSException{
	int i = 0;
	int indexMessage = -1;
	fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern currentMsg ;
	fr.dyade.aaa.mom.QueueNaming currentQueue;
    
	Vector resultVector = new Vector();
    
	/* first pass to find the index of the message in the vector */
	while(i<lastNotAckVector.size()) {
	    currentMsg = (fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern) lastNotAckVector.elementAt(i);
      
	    if(messageID.equals(currentMsg.message.getJMSMessageID())) {
		indexMessage = i;
		break;
	    }
	    i++;
	}
    
	/*	prepares the vector of the message 2nd pass 
	 *	begins the indexMessage-1 and continues until the index 0
	 */
	javax.jms.Message message;
	fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern previousMsg ;
	fr.dyade.aaa.mom.QueueNaming previousQueue;
	i = indexMessage;
	while(i>=0) {
	    /* add the elment in the ack Vector & removes from the other */
	    currentMsg = (fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern) lastNotAckVector.elementAt(i);
	    currentQueue = (fr.dyade.aaa.mom.QueueNaming) currentMsg.message.getJMSDestination();
	    message = currentMsg.message;
	    resultVector.addElement(new fr.dyade.aaa.mom.AckQueueMessageMOMExtern(messageJMSMOMID, currentQueue, message.getJMSMessageID(), acknowledgeMode, new Long(sessionID).toString()));
      
	    int j = i-1;
	    while(j>=0) {
		previousMsg = (fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern) lastNotAckVector.elementAt(j);
		previousQueue = (fr.dyade.aaa.mom.QueueNaming) previousMsg.message.getJMSDestination();
	
		if(currentQueue.equals(previousQueue)) {
		    lastNotAckVector.removeElementAt(j);
		    j--;
		    i--;
		} else
		    j--;
	    }
      
	    /* removes the message from the vector */
	    lastNotAckVector.removeElementAt(i);
	    i--;
	}
	return resultVector;
    }
  
    /** prepares the messages to acknowledge so as to decrease the overhead  */
    protected Vector preparesTransactedAck(long messageJMSMOMID) throws javax.jms.JMSException {
	int i = transactedMessageToAckVector.size()-1;
	Vector resultVector = new Vector();
    
	/*	prepares the vector of the message 2nd pass 
	 *	begins the indexMessage-1 and continues until the index 0
	 */
	javax.jms.Message message;
	fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern currentMsg ;
	fr.dyade.aaa.mom.QueueNaming currentQueue;
	fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern previousMsg ;
	fr.dyade.aaa.mom.QueueNaming previousQueue;
	while(i>=0) {
	    /* add the elment in the ack Vector & removes from the other */
	    currentMsg = (fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern) transactedMessageToAckVector.elementAt(i);
	    currentQueue = (fr.dyade.aaa.mom.QueueNaming) currentMsg.message.getJMSDestination();
	    message = currentMsg.message;
	    resultVector.addElement(new fr.dyade.aaa.mom.AckQueueMessageMOMExtern(messageJMSMOMID, currentQueue, message.getJMSMessageID(), acknowledgeMode, new Long(sessionID).toString()));

	    int j = i-1;
	    while(j>=0) {
		previousMsg = (fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern) transactedMessageToAckVector.elementAt(j);
		previousQueue = (fr.dyade.aaa.mom.QueueNaming) previousMsg.message.getJMSDestination();
	
		if(currentQueue.equals(previousQueue)) {
		    transactedMessageToAckVector.removeElementAt(j);
		    j--;
		    i--;
		} else
		    j--;
	    }
      
	    /* removes the message from the vector */
	    transactedMessageToAckVector.removeElementAt(i);
	    i--;
	}
	return resultVector;
    }
  
    /** prepares the messages to recover the messages of the session */
    protected fr.dyade.aaa.mom.RecoverObject[] preparesRecover() throws javax.jms.JMSException {
	int i = lastNotAckVector.size()-1;
	Vector resultVector = new Vector();
    
	/*	prepares the vector of the message 2nd pass 
	 *	begins the indexMessage-1 and continues until the index 0
	 */
	javax.jms.Message message;
	fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern currentMsg ;
	fr.dyade.aaa.mom.QueueNaming currentQueue;
	fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern previousMsg ;
	fr.dyade.aaa.mom.QueueNaming previousQueue;
	while(i>=0) {
	    /* add the elment in the ack Vector & removes from the other */
	    currentMsg = (fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern) lastNotAckVector.elementAt(i);
	    currentQueue = (fr.dyade.aaa.mom.QueueNaming) currentMsg.message.getJMSDestination();
	    message = currentMsg.message;
	    resultVector.addElement(new fr.dyade.aaa.mom.RecoverQueue(currentQueue, message.getJMSMessageID()));
      
	    int j = i-1;
	    while(j>=0) {
		previousMsg = (fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern) lastNotAckVector.elementAt(j);
		previousQueue = (fr.dyade.aaa.mom.QueueNaming) previousMsg.message.getJMSDestination();
	
		if(currentQueue.equals(previousQueue)) {
		    lastNotAckVector.removeElementAt(j);
		    j--;
		    i--;
		} else
		    j--;
	    }
      
	    /* removes the message from the vector */
	    lastNotAckVector.removeElementAt(i);
	    i--;
	}
	int size = resultVector.size();
	fr.dyade.aaa.mom.RecoverQueue[] ackTab = new fr.dyade.aaa.mom.RecoverQueue[size];
	resultVector.copyInto(ackTab);
	return ackTab;
    
    }
   
    /**
     * Allows the session to rollback a message delivered to the JMS client
     */
    protected void rollbackDeliveryMsg() throws javax.jms.JMSException {
	try {
	    Vector rollbackDeliveryMsg = new Vector();
	    while (!transactedMessageToAckVector.isEmpty()) {
		fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern currentMsg = (fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern) transactedMessageToAckVector.remove(transactedMessageToAckVector.size()-1);
	
		/* built Vector for Rollback */
		javax.jms.Destination destination = (javax.jms.Destination) currentMsg.message.getJMSDestination();
		String messageMOMID = currentMsg.message.getJMSMessageID();
		fr.dyade.aaa.mom.MessageRollbackMOMExtern msgRollback = new fr.dyade.aaa.mom.MessageRollbackMOMExtern(currentMsg.getMessageMOMExternID(), destination ,new Long(sessionID).toString(), messageMOMID);
		rollbackDeliveryMsg.addElement(msgRollback);
	    }
	    if (!rollbackDeliveryMsg.isEmpty()) {
		/* send vector for rollback */
		long messageJMSMOMID = refConnection.getMessageMOMID();
		fr.dyade.aaa.mom.MessageMOMExtern msgSend = new fr.dyade.aaa.mom.MessageTransactedRollback(messageJMSMOMID,rollbackDeliveryMsg,false);
		sendMessage(msgSend);
	    }
	} catch (javax.jms.JMSException exc) {
	    throw(exc);
	}
    }
  
    /** prepares the messages to rollback the messages of the session */
    protected fr.dyade.aaa.mom.QueueNaming[] preparesRollback() throws javax.jms.JMSException {
	int i = transactedMessageToAckVector.size()-1;
	Vector resultVector = new Vector();
    
	/*	prepares the vector of the message 2nd pass 
	 *	begins the indexMessage-1 and continues until the index 0
	 */
	javax.jms.Message message;
	fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern currentMsg ;
	fr.dyade.aaa.mom.QueueNaming currentQueue;
	fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern previousMsg ;
	fr.dyade.aaa.mom.QueueNaming previousQueue;
	while(i>=0) {
	    /* add the elment in the ack Vector & removes from the other */
	    currentMsg = (fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern) transactedMessageToAckVector.elementAt(i);
	    currentQueue = (fr.dyade.aaa.mom.QueueNaming) currentMsg.message.getJMSDestination();
	    message = currentMsg.message;
	    resultVector.addElement(currentQueue);
      
	    int j = i-1;
	    while(j>=0) {
		previousMsg = (fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern) transactedMessageToAckVector.elementAt(j);
		previousQueue = (fr.dyade.aaa.mom.QueueNaming) previousMsg.message.getJMSDestination();
	
		if(currentQueue.equals(previousQueue)) {
		    transactedMessageToAckVector.removeElementAt(j);
		    j--;
		    i--;
		} else
		    j--;
	    }
      
	    /* removes the message from the vector */
	    transactedMessageToAckVector.removeElementAt(i);
	    i--;
	}
	int size = resultVector.size();
	fr.dyade.aaa.mom.QueueNaming[] ackTab = new fr.dyade.aaa.mom.QueueNaming[size];
	resultVector.copyInto(ackTab);
	return ackTab;
    }
  
}
