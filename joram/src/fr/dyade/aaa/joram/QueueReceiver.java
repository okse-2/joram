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

/** 
 *	a QueueReceiver is an object associated with a real Queue
 *	in the MOM 
 * 
 *	@see javax.jms.MessageConsumer
 *	@see javax.jms.QueueReceiver
 *	@see fr.dyade.aaa.mom.Queue 
 */ 
 
public class QueueReceiver extends fr.dyade.aaa.joram.MessageConsumer implements javax.jms.QueueReceiver { 

    /** the Queue associated to the QueueReceiver */
    protected fr.dyade.aaa.mom.QueueNaming queue;

    /** constructor with selector chosen by the client */
    public QueueReceiver(String consumerID, fr.dyade.aaa.joram.Connection refConnection, fr.dyade.aaa.joram.Session refSession, javax.jms.Queue queue, String selector) {
	super();
	this.queue = (fr.dyade.aaa.mom.QueueNaming) queue;
	this.refConnection = refConnection;
	this.refSession = refSession;
	this.selector = selector;
	this.messageListener = null;
    }
	
    /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
    public javax.jms.Queue getQueue() throws javax.jms.JMSException {
	if(queue==null)
	    throw (new fr.dyade.aaa.joram.JMSAAAException("Queue name Unknown",JMSAAAException.DEFAULT_JMSAAA_ERROR));
	else
	    return queue;
    }
	
    /** overwrite the methode from MessageConsumer */
    public javax.jms.Message receive()  throws javax.jms.JMSException {
	return this.receive((long) -1);
    }
	
    /** overwrite the methode from MessageConsumer */
    public javax.jms.Message receive(long timeOut)  throws javax.jms.JMSException {
	try {
	    Object obj = new Object();
	    long messageJMSMOMID = refConnection.getMessageMOMID();
	    Long longMsgID = new Long(messageJMSMOMID);
			
	    /* construction of the MessageJMSMOM */
	    fr.dyade.aaa.mom.ReceptionMessageMOMExtern msgRec = new fr.dyade.aaa.mom.ReceptionMessageMOMExtern(messageJMSMOMID, queue, selector, timeOut, refSession.acknowledgeMode, new Long(refSession.sessionID).toString());
			
	    /*	synchronization because it could arrive that the notify was
	     *	called before the wait 
	     */
	    synchronized(obj) {
		/* the processus of the client waits the response */
		refConnection.waitThreadTable.put(longMsgID,obj);
		if(refSession.transacted || refSession.acknowledgeMode!=fr.dyade.aaa.mom.CommonClientAAA.AUTO_ACKNOWLEDGE) {
		    /* send the request to the agentClient */
		    refSession.sendToConnection(msgRec);
		} else {
		    /* add the request to the explicitRequestVector in autoAckMode */
		    refSession.explicitRequestVector.addElement(msgRec);
		}
				
		if(Debug.debug)
		    if(Debug.connectReceive)
			System.out.println("after the sendConnection, before sleep");
		
		obj.wait();
	    }
			
	    /* the clients wakes up */
	    fr.dyade.aaa.mom.MessageMOMExtern msgMOM;
		
	    /* tests if the key exists 
	     * dissociates the message null (on receiveNoWait) and internal error
	     */
	    if(!refConnection.messageJMSMOMTable.containsKey(longMsgID))
		throw (new fr.dyade.aaa.joram.JMSAAAException("No back Message received ",JMSAAAException.ERROR_NO_MESSAGE_AVAILABLE));
	
	    /* get the the message back or the exception*/
	    msgMOM = (fr.dyade.aaa.mom.MessageMOMExtern) refConnection.messageJMSMOMTable.remove(longMsgID);
	    if(msgMOM instanceof fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern) {
				/* return the message */
		fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern msgDeliver = (fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern) msgMOM;
				
		if(!refSession.transacted) {
		    if(refSession.acknowledgeMode==fr.dyade.aaa.mom.CommonClientAAA.AUTO_ACKNOWLEDGE) {
			/* prepares the acknowledge in case of autoacknowledge */
			fr.dyade.aaa.mom.AckQueueMessageMOMExtern msgAck =  new fr.dyade.aaa.mom.AckQueueMessageMOMExtern(refConnection.getMessageMOMID(), queue, msgDeliver.message.getJMSMessageID(), fr.dyade.aaa.mom.CommonClientAAA.AUTO_ACKNOWLEDGE, new Long(refSession.sessionID).toString());
			refSession.autoMessageToAckVector.addElement(msgAck);
		    } else {
			refSession.lastNotAckVector.addElement(msgDeliver);
			/* add the reference to the session to acknowledge handly */
			msgDeliver.message.setRefSessionItf(refSession);
		    }
		} else {
		    synchronized(refSession.transactedSynchroObject) {
			refSession.transactedMessageToAckVector.addElement(msgDeliver);
		    }
		}
				
				/*	reset the message to put the mode in readOnly and to
				 *	prepare the transient attributes dor the reading
				 */
		refSession.resetMessage(msgDeliver.message);
								
		return msgDeliver.message;
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

	} catch (InterruptedException exc) {
	    javax.jms.JMSException except = new javax.jms.JMSException("Internal Error : ",String.valueOf(JMSAAAException.DEFAULT_JMSAAA_ERROR));
	    except.setLinkedException(exc);
	    throw(except);
	} 
    }
	
    /** overwrite the methode from MessageConsumer */
    public javax.jms.Message receiveNoWait()  throws javax.jms.JMSException {
	return this.receive((long) 0);
    }
	
    /**overwrite the methode from MessageConsumer  */
    public void close()  throws javax.jms.JMSException {
	/* cancel the inscription in the Table of the Session */
	synchronized(refSession.messageConsumerTable) {	
	    Object obj;
	    if((obj = refSession.messageConsumerTable.remove(queue))==null)
		throw (new fr.dyade.aaa.joram.JMSAAAException("Internal Error during close",JMSAAAException.DEFAULT_JMSAAA_ERROR));
	}
	super.close();
    }
	
}
