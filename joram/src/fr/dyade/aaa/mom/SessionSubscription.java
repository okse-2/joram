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

/* 
 *	contains a vector of all the subscriptions of the present session
 *	and if the session is in autoAck a queue with the messages to
 *	deliver to the Session (FIFO)
 *
 *	@see	fr.dyade.aaa.mom.CommonClientAAA
 */


public class SessionSubscription implements java.io.Serializable { 
 
 	/** class used to do the marshalling with a subscriptionClient
	 *	and its size. the ordering of the received messages has to  
	 *	be kept.
	 *	a susbcriptionClient is added after subscription, and if a 
	 *	continual flow of messages is sent on this sub, other messages 
	 *	of other sub of the same session will be never delivered
	 */
 	class SessionSubInit implements java.io.Serializable {
		/** the size of the init sub */
		int numberMessage;
		
		/** the subscriptionClient */
		fr.dyade.aaa.mom.SubscriptionClient sub;
		
		SessionSubInit(fr.dyade.aaa.mom.SubscriptionClient sub, int numberMessage) {
			this.sub = sub;
			this.numberMessage = numberMessage;
		}
	}	
	
	/** the vector of the KeyClientSubscription of the session */
	protected java.util.Vector subSessionVector = null;
	
	/** the queue of messages to deliver 
	 *	the elements are not neither Messages, neither susbcriptionClient
	 *	but SessionSubInit
	 */
	private java.util.Vector messageSubVector = null;
	
	/** the index of the subscription which delivered the message
	 *	-1 means ready to deliver
	 *	only 1 message can be delivered each time
	 *	not a boolean because of the MessageListener can be put to false
	 */
	private int subLastDelivery = -1;
	
	/** the mode of acknowledgment of the session */
	protected int ackMode;
	
	public SessionSubscription(int ackMode) {
		subSessionVector = new java.util.Vector();
		this.ackMode = ackMode;
		if(this.ackMode==CommonClientAAA.AUTO_ACKNOWLEDGE)
			messageSubVector = new java.util.Vector();
	}
	
	/** adds a new entry to the messageSubVector */
	protected void addDeliveredMessage(fr.dyade.aaa.mom.SubscriptionClient sub, int numberMessage) {
		if(numberMessage!=0) 
			messageSubVector.addElement(new SessionSubInit(sub, numberMessage));
	} 
	
	/** returns a message to deliver to the client, if any
	 *	else the message is null	
	 */
	protected fr.dyade.aaa.mom.Message deliveryMessage() throws Exception{
		if(subLastDelivery==-1) {
			/* searchs a Subscription with a message to deliver */ 
			SessionSubInit sessionSubInit;
			fr.dyade.aaa.mom.SubscriptionClient sub;
			fr.dyade.aaa.mom.Message msg;
			int i=0;
			while(i<messageSubVector.size()) {
				sessionSubInit = (SessionSubInit) messageSubVector.elementAt(i);
				sub = sessionSubInit.sub;
				if((msg=sub.deliveryMessage())!=null) {
					subLastDelivery = i;
					return msg;
				} else
					i++;
			}
			return null;
		}
		return null;
	}

	/** the message from the was autoacknowledged, checks for the next */
	protected void ackDeliveredMessage() {
		SessionSubInit sessionSubInit = (SessionSubInit) messageSubVector.elementAt(subLastDelivery);
		fr.dyade.aaa.mom.SubscriptionClient sub = sessionSubInit.sub;
			
		/* decrements the counter of message to deliver
		 * (from the subscription) 
		 */
		sessionSubInit.numberMessage--;
				
		/* remove the entry if all the messages have been delivered */
		if(sessionSubInit.numberMessage<=0)
			messageSubVector.removeElementAt(subLastDelivery);
		
		/* new delivery avaiable */ 
		subLastDelivery = -1;
	}

	/** Autoack mode : reput the message in case of close of the subscriber
	 *	releases ressources if any and marks as Redelivered.	
	 */
	protected void removeSubFromDelivery(fr.dyade.aaa.mom.SubscriptionClient subClose) throws Exception {
		/* searchs the Subscription to remove */ 
		SessionSubInit sessionSubInit;
		fr.dyade.aaa.mom.SubscriptionClient sub;
		int i=0;
		while(i<messageSubVector.size()) {
			sessionSubInit = (SessionSubInit) messageSubVector.elementAt(i);
			sub = sessionSubInit.sub;
			if(sub==subClose) {	
				/* releases the ressources */
				if(subLastDelivery==i) {
					subLastDelivery = -1;
					subClose.replaceRedeliveredMsg();
				}
				
				/* removes the sub from the vector */
				messageSubVector.removeElementAt(i);
				
			} else
				i++;
		}	
	}
	
	/**autoAck mode : removes all subscription from the sesion */
	protected void removeAllSubFromDelivery()  throws Exception {
		/* searchs the Subscription to remove */ 
		SessionSubInit sessionSubInit;
		fr.dyade.aaa.mom.SubscriptionClient sub;
		int i=0;
		while(!messageSubVector.isEmpty()) {
			sessionSubInit = (SessionSubInit) messageSubVector.elementAt(i);
			sub = sessionSubInit.sub;
			
			/* releases the ressources */
			if(subLastDelivery==i) {
				subLastDelivery = -1;
				sub.replaceRedeliveredMsg();
			}
				
			/* removes the sub from the vector */
			messageSubVector.removeElementAt(0);
		}
	}
}
