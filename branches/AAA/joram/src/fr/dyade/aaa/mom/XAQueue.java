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

import java.util.*; 
import fr.dyade.aaa.agent.*;

/** 
 *	a XAQueue is a Queue which implements the transacted behaviour
 *
 *	@see fr.dyade.aaa.mom.Queue
 */ 
 
public class XAQueue extends fr.dyade.aaa.mom.Queue { 
 
 	/** constructor */ 
	public XAQueue() {
		super();
	}  

	/** method react with the notification of rollback */
	public void react(AgentId from, Notification not) throws Exception { 
		if (not instanceof NotificationXARollback) { 
			notificationXARollback(from, (NotificationXARollback) not); 
		} else { 
			super.react(from, not); 
		} 
	}
	
	/** the queue receives a notification of rollback for a particular message */ 
	protected void notificationXARollback(AgentId from, NotificationXARollback not) throws Exception{ 
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
	
	/** the Queue receives a Message from a agentClient thanks to NotificationQueueSend 
	 *	a requestAgree must be sent even the message is not PERSISTENT
	 */ 
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
			
			/* deliver an agreement to the client */
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
}
