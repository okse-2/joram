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
 *	a Destination wrapps methos used by Queue and Topic
 *	it's for easier management of Queue and Topic 
 * 
 *	@see         fr.dyade.aaa.mom.Topic 
 *	@see         fr.dyade.aaa.mom.AgentClient 
 */ 
 
public class Destination extends fr.dyade.aaa.agent.Agent { 

	
	/* Constructor */
	public Destination() {}

	public void react(AgentId from, Notification not) throws Exception { 
		if (not instanceof NotificationCloseDestination) { 
			notificationCloseDestination(from, (NotificationCloseDestination) not); 
		} else { 
			super.react(from, not); 
		} 
	}
	
	/** allows to notify to a Queue or a Topic to delete itself */
	protected void notificationCloseDestination(AgentId from, NotificationCloseDestination not) {
		super.delete();
	}	
	
	/** checks if all the fields of the messages are completed
	 *	used before distribute messages on agentClients
	 */
	protected boolean checkFieldsMessage(fr.dyade.aaa.mom.Message msg) {
		return true;
	}
	
	/** checks if message is OK with option 
	 *	public and not protected due tu the Object SubscriptionClient
	 */
	public static boolean checkMessage(fr.dyade.aaa.mom.Message msg) throws Exception {
		/* check the expiration field and other */
		
		if(msg.getJMSExpiration()!=0) {
			if(System.currentTimeMillis()-msg.getJMSExpiration()>=0) 
				return false;
			else
				return true;
		} else {
			/* message not expired */
			return true;
		}
	}
	
	/** send an exception to an agentClient */
	protected void deliveryException (AgentId to, NotificationMOMRequest not, MOMException exc) {
		/* construction of the exception notification except in auto-acknowledge */ 
		fr.dyade.aaa.mom.NotificationMOMException notException = new fr.dyade.aaa.mom.NotificationMOMException(not, exc); 
		sendTo(to, notException);
	}

	/** send an agreement to a request of an agentClient */
	protected void deliveryAgreement (AgentId to, NotificationMOMRequest not) {
		/* construction of the exception notification except in auto-acknowledge */ 
		fr.dyade.aaa.mom.NotificationAgreeAsk notAgree = new fr.dyade.aaa.mom.NotificationAgreeAsk(not); 
		sendTo(to, notAgree);
	}

}
 
 
