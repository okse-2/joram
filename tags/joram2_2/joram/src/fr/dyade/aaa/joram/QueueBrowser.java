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

/** 
 *	a QueueBrowser is as JMS specifications 
 * 
 *	@see	javax.jms.QueueBrowser
 *	@see	fr.dyade.aaa.mom.Queue 
 */ 
 
public class QueueBrowser implements javax.jms.QueueBrowser { 
	
	/** the Queue associated to the QueueReceiver */
	private fr.dyade.aaa.mom.QueueNaming queue;
	
	/** the selector for the corresponding Queue/Topic */
	private java.lang.String selector;
	
	/** reference to the COnnection Object to retrieve Message from Connection */
	private fr.dyade.aaa.joram.Connection refConnection;
	
	/** reference to the Session Object so send Message to the socket */
	private fr.dyade.aaa.joram.Session refSession;
	
	
	/** constructor with no selector */
	public QueueBrowser(fr.dyade.aaa.joram.Connection refConnectionNew, fr.dyade.aaa.joram.Session refSessionNew, javax.jms.Queue queueNew) {
		this(refConnectionNew, refSessionNew, queueNew, "");
	}
	
	/** constructor with selector chosen by the client */
	public QueueBrowser(fr.dyade.aaa.joram.Connection refConnection, fr.dyade.aaa.joram.Session refSession, javax.jms.Queue queue, String selector) {
		this.queue = (fr.dyade.aaa.mom.QueueNaming) queue;
		this.selector = selector;
		this.refConnection = refConnection;
		this.refSession = refSession;
	}
	
	/** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
	public javax.jms.Queue getQueue() throws javax.jms.JMSException {
		if(queue==null)
			throw (new fr.dyade.aaa.joram.JMSAAAException("Queue name Unknown",JMSAAAException.DEFAULT_JMSAAA_ERROR));
		else
			return queue;
	}
	
	/** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
	public java.lang.String getMessageSelector() throws javax.jms.JMSException {
		return selector;
	}
	
	/** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
	public java.util.Enumeration getEnumeration() throws javax.jms.JMSException {
		try {
			Object obj = new Object();
			long messageJMSMOMID = refConnection.getMessageMOMID();
			Long longMsgID = new Long(messageJMSMOMID);
			
			/* construction of the MessageJMSMOM */
			fr.dyade.aaa.mom.ReadOnlyMessageMOMExtern msgRead = new fr.dyade.aaa.mom.ReadOnlyMessageMOMExtern(messageJMSMOMID, queue, selector);
		
			/*	synchronization because it could arrive that the notify was
			 *	called before the wait 
			 */
			synchronized(obj) {
			  /* the processus of the client waits the response */
			  refConnection.waitThreadTable.put(longMsgID,obj);
			  /* get the messageJMSMOM identifier */
			  refSession.sendToConnection(msgRead);
				
			  obj.wait();
			}
			/* the clients wakes up */
			fr.dyade.aaa.mom.MessageMOMExtern msgMOM;
		
			/* tests if the key exists 
			 * dissociates the enumeration null and internal error
			 */
			if(!refConnection.messageJMSMOMTable.containsKey(longMsgID))
				throw (new fr.dyade.aaa.joram.JMSAAAException("No back Message received ",JMSAAAException.ERROR_NO_MESSAGE_AVAILABLE));
	
			/* get the the message back or the exception*/
			msgMOM = (fr.dyade.aaa.mom.MessageMOMExtern) refConnection.messageJMSMOMTable.remove(longMsgID);
			if(msgMOM instanceof fr.dyade.aaa.mom.ReadDeliverMessageMOMExtern) {
				/* return the enumeration of the messages of the queue */
				fr.dyade.aaa.mom.ReadDeliverMessageMOMExtern msgEnum = (fr.dyade.aaa.mom.ReadDeliverMessageMOMExtern) msgMOM;
				return msgEnum.messageEnumerate.elements();
			} else {
				/* exception sent back to the client */
				fr.dyade.aaa.mom.ExceptionMessageMOMExtern msgExc = (fr.dyade.aaa.mom.ExceptionMessageMOMExtern) msgMOM;
				fr.dyade.aaa.joram.JMSAAAException except = new fr.dyade.aaa.joram.JMSAAAException("MOM Internal Error : ",JMSAAAException.MOM_INTERNAL_ERROR);
				except.setLinkedException(msgExc.exception);
				throw(except);
			}
			
		} catch (InterruptedException exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("Internal Error : ",String.valueOf(JMSAAAException.DEFAULT_JMSAAA_ERROR));
			except.setLinkedException(exc);
			throw(except);
		} 
	}
	
	/** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
	public void close()  throws javax.jms.JMSException {
	  System.gc();
	}
}
