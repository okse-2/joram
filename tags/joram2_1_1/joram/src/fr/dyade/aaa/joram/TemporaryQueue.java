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
 *	a TemporaryQueue is a Queue for the time of the Connexion
 *	this object is not in the fr.dyade.aaa.mom package because it's not used and
 *	has not any utility in the MOM
 * 
 *	@see fr.dyade.aaa.mom.TopicNaming
 *	@see javax.jms.Topic
 *	@see fr.dyade.aaa.mom.Topic
 *	@see fr.dyade.aaa.mom.TemporaryTopic
 */ 
 
public class TemporaryQueue extends fr.dyade.aaa.mom.QueueNaming implements javax.jms.TemporaryQueue { 
	
	/** reference to the COnnection Object to retrieve Message from Connection */
	protected fr.dyade.aaa.joram.Connection refConnection;
	
	/** reference to the Session Object so send Message to the socket */
	protected fr.dyade.aaa.joram.Session refSession;
	
	/* constructor of the TemporaryTopic */
	public TemporaryQueue(fr.dyade.aaa.joram.Connection refConnection, fr.dyade.aaa.joram.Session refSession, String nameQueue) {
		super(nameQueue);
		this.refConnection = refConnection;
		this.refSession = refSession;
	}
	
	/** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
	public void delete() throws javax.jms.JMSException {
		try {
			Object obj = new Object();
			long messageJMSMOMID = refConnection.getMessageMOMID();
			Long longMsgID = new Long(messageJMSMOMID);
			
			fr.dyade.aaa.mom.CloseDestinationMOMExtern msgClose = new fr.dyade.aaa.mom.CloseDestinationMOMExtern(messageJMSMOMID, this);
			/*	synchronization because it could arrive that the notify was
			 *	called before the wait 
			 */
			synchronized(obj) {
				/* the processus of the client waits the response */
				refConnection.waitThreadTable.put(longMsgID,obj);
				/* get the messageJMSMOM identifier */
				refSession.sendToConnection(msgClose);
				
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
			if(!(msgMOM instanceof fr.dyade.aaa.mom.RequestAgreeMOMExtern)) {
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
	
	
	
}
