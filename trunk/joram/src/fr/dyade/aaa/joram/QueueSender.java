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

//import java.lang.*; 
 
/** 
 *	a QueueSender is as JMS specifications 
 *	a QueueSender is an object associated with a real Queue
 *	in the MOM 
 * 
 *	@see fr.dyade.aaa.joram.QueueSession
 *	@see javax.jms.MessageProducer 
 *	@see javax.jms.QueueSender 
 */ 
 
public class QueueSender extends fr.dyade.aaa.joram.MessageProducer implements javax.jms.QueueSender { 
	
    /** the Queue associated to the QueueReceiver */
    fr.dyade.aaa.mom.QueueNaming queue;

    /* constructor */
    public QueueSender(Connection refConnectionNew, QueueSession refSessionNew, javax.jms.Queue queueNew) {
	super(refConnectionNew, refSessionNew);
	queue = (fr.dyade.aaa.mom.QueueNaming) queueNew;
    }
	
    /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
    public javax.jms.Queue getQueue() throws javax.jms.JMSException {
	try {
	    if(queue==null)
		throw (new fr.dyade.aaa.joram.JMSAAAException("Queue name Unknown",JMSAAAException.DEFAULT_JMSAAA_ERROR));
	    else
		return queue;
	} catch (javax.jms.JMSException exc) {
	    throw(exc);
	} catch (Exception exc) {
	    javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
	    except.setLinkedException(exc);
	    throw(except);
	}
    }
	
    /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
    public void send(javax.jms.Message message) throws javax.jms.JMSException {
	this.send(this.queue, message, super.deliveryMode, super.priority, super.timeToLive); 		
    }
	
    /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
    public void send(javax.jms.Message message, int deliveryModeNew, int priorityNew, long timeToLiveNew) throws javax.jms.JMSException {
	this.send(this.queue, message, deliveryModeNew, priorityNew, timeToLiveNew);
    }
	
    /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
    public void send(javax.jms.Queue queueNew, javax.jms.Message message)  throws javax.jms.JMSException {
	this.send(queueNew, message, super.deliveryMode, super.priority, super.timeToLive);
    }
	
    /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
    public void send(javax.jms.Queue queueNew, javax.jms.Message message, int deliveryModeNew, int priorityNew, long timeToLiveNew) throws javax.jms.JMSException {
	try {
	    /* padding the fields of the message */
	    message.setJMSDestination(queueNew);
	    message.setJMSDeliveryMode(deliveryModeNew);
	    message.setJMSPriority(priorityNew);
		
	    if(timeToLiveNew>0)
		message.setJMSExpiration(System.currentTimeMillis()+((long) timeToLiveNew));
	    else
		message.setJMSExpiration((long) 0);
			
	    /* set the timestamp which will be updated by "Connection" */
	    message.setJMSTimestamp(System.currentTimeMillis());
			
	    /*	reset the message to put the mode in readOnly and to
	     *	destroy the transient attributes
	     */
	    refSession.resetMessage(message);
			
	    long messageJMSMOMID = refConnection.getMessageMOMID();
	    Long longMsgID = new Long(messageJMSMOMID);
			
	    /* construction of the MessageJMSMOM */
	    fr.dyade.aaa.mom.SendingMessageQueueMOMExtern msgSend = new fr.dyade.aaa.mom.SendingMessageQueueMOMExtern(messageJMSMOMID, (fr.dyade.aaa.mom.Message) message);
			
	    if(refSession.transacted) {
		/* Queue not ack this msg, this msg is add in the vector and 
		 * CommonClientAAA acknoledge the vector */
		msgSend.message.setJMSDeliveryMode(1);
		/* add the message in the vector waiting for the commit */
		refSession.transactedMessageToSendVector.addElement(msgSend);
	    } else if(message.getJMSDeliveryMode()!=fr.dyade.aaa.mom.Message.PERSISTENT &&
                  message.getJMSDeliveryMode()!=fr.dyade.aaa.mom.Message.NON_PERSISTENT) {
				/* deliver an agreement to the client if Persistent */
		Object obj = new Object();
			
				/*	synchronization because it could arrive that the notify was
				 *	called before the wait 
				 */
		synchronized(obj) {
		    /* the processus of the client waits the response */
		    refConnection.waitThreadTable.put(longMsgID,obj);
		    /* sends the messageJMSMOM r */
		    refSession.sendToConnection(msgSend);
				  
		    obj.wait();	
		}
		
		if(Debug.debug)
		    if(Debug.connect)
			System.out.println("QueueSender : i wake up");
		
				/* the clients wakes up */
		fr.dyade.aaa.mom.MessageMOMExtern msgMOM;
			
				/* tests if the key exists 
				 * dissociates the enumeration null and internal error
				 */
		if(!refConnection.messageJMSMOMTable.containsKey(longMsgID))
		    throw (new fr.dyade.aaa.joram.JMSAAAException("No back Message received ",JMSAAAException.ERROR_NO_MESSAGE_AVAILABLE));
	
				/* get the the message back or the exception*/
		msgMOM = (fr.dyade.aaa.mom.MessageMOMExtern) refConnection.messageJMSMOMTable.remove(longMsgID);
		if(msgMOM instanceof fr.dyade.aaa.mom.SendingBackMessageMOMExtern) {
		    /* update the fields of the message */
		    fr.dyade.aaa.mom.SendingBackMessageMOMExtern msgSendBack = (fr.dyade.aaa.mom.SendingBackMessageMOMExtern) msgMOM;
		    message = msgSendBack.message;
		} else if(msgMOM instanceof fr.dyade.aaa.mom.ExceptionSendMessageMOMExtern) {
		    /* exception sent back to the client */
		    fr.dyade.aaa.mom.ExceptionSendMessageMOMExtern msgExc = (fr.dyade.aaa.mom.ExceptionSendMessageMOMExtern) msgMOM;
		    fr.dyade.aaa.joram.JMSAAAException except = new fr.dyade.aaa.joram.JMSAAAException("MOM Internal Error : ",JMSAAAException.MOM_INTERNAL_ERROR);
		    except.setLinkedException(msgExc.exception);
		    message = msgExc.message;
		    throw(except);
		} else if(msgMOM instanceof fr.dyade.aaa.mom.ExceptionUnknownObjMOMExtern) {
		    /* exception sent back to the client */
		    fr.dyade.aaa.mom.ExceptionUnknownObjMOMExtern msgExc = (fr.dyade.aaa.mom.ExceptionUnknownObjMOMExtern) msgMOM;
		    javax.jms.InvalidDestinationException except = new javax.jms.InvalidDestinationException("Invalid Queue :  ",String.valueOf(JMSAAAException.MOM_INTERNAL_ERROR));
		    except.setLinkedException(msgExc.exception);
		    throw(except);
		} else {
		    /* unknown message */
		    /* should never arrived */
		    fr.dyade.aaa.joram.JMSAAAException except = new fr.dyade.aaa.joram.JMSAAAException("MOM Internal Error : ",JMSAAAException.MOM_INTERNAL_ERROR);
		    throw(except);
		}
	    } else {
				/* sends the messageJMSMOM r */
		refSession.sendToConnection(msgSend);
	    }
	} catch (javax.jms.JMSException exc) {
	    throw(exc);
	} catch (ClassCastException exc) {
	    /* TO CHECK : I'm not sure */
	    javax.jms.MessageFormatException except = new javax.jms.MessageFormatException("internal Error");
	    except.setLinkedException(exc);
	    throw(except);
	} catch (Exception exc) {
	    exc.printStackTrace();
	    javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
	    except.setLinkedException(exc);
	    throw(except);
	}
    }
	
}
