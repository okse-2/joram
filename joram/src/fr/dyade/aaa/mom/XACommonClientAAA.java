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

import fr.dyade.aaa.agent.*;
import java.util.*;

/**	the XACommonClient is a subclass of the CommonClientAAA class
 *	which ilmplements the transacted behaviour 
 *
 *	@see	fr.dyade.aaa.mom.CommonClientAAA
 *	@see	fr.dyade.aaa.mom.AgentClient
 */


public class XACommonClientAAA extends fr.dyade.aaa.mom.CommonClientAAA {
	
	public XACommonClientAAA(AgentClientItf agentClient) {
		super(agentClient);
	}
	
	/** treatment of the requests of the extern client */
	protected void treatmentExternRequest(NotificationInputMessage not) throws MOMException {
		try {
			if (not.msgMOMExtern instanceof XARollbackQueueMOMExtern) { 
				this.notificationXARollbackQueue((XARollbackQueueMOMExtern) not.msgMOMExtern); 
			} else if (not.msgMOMExtern instanceof XARollbackTopicMOMExtern) { 
				this.notificationXARollbackTopic((XARollbackTopicMOMExtern) not.msgMOMExtern); 
			} else
				super.treatmentExternRequest(not);
		} catch (Exception exc) {
			if(Debug.debug)
				if(Debug.clientTest)
					System.err.println(exc);
				
			/* send the error to the client extern */
			fr.dyade.aaa.mom.ExceptionMessageMOMExtern msgExc = new fr.dyade.aaa.mom.ExceptionMessageMOMExtern(not.msgMOMExtern.getMessageMOMExternID(), exc);
			agentClient.sendMessageMOMExtern(msgExc);
			
			/* canceling previous actions due to an ack of tha Topic which didn't exist */
			if(exc instanceof MOMException) {
				MOMException excMOM = (MOMException) exc;
				/* spreading of the exception */
				if(excMOM.getErrorCode()==MOMException.TOPIC_MESSAGEID_NO_EXIST)
					throw(excMOM);
			}
		}	
	}
	
	/** treatment of the agreement sent by the Queue or the Topic (on request of the client) */
	protected void notificationRequestAgree(AgentId from, NotificationAgreeAsk not) throws Exception {
		if (not.typeNotification instanceof NotificationXARollback) {
			/* acknowledge agreement */
			fr.dyade.aaa.mom.NotificationXARollback notRollback = (NotificationXARollback) not.typeNotification;
			fr.dyade.aaa.mom.RequestAgreeMOMExtern msgAgree = new fr.dyade.aaa.mom.RequestAgreeMOMExtern(notRollback.notMOMID);
			agentClient.sendMessageMOMExtern(msgAgree);
		} else
			super.notificationRequestAgree(from, not);
	}
	
	/** notification to roolback a set of messages from a Queuesession */
	protected void notificationXARollbackQueue(XARollbackQueueMOMExtern msgXARoll) throws Exception{ 
		int i = 0;
		AgentId to ;
		fr.dyade.aaa.mom.QueueNaming[] tabQueue = msgXARoll.tabQueue;
		
		for(i=0;i<tabQueue.length;i++) {
			/* treatment of all the messages to rollback */
			to = AgentId.fromString(tabQueue[i].getQueueName());
			fr.dyade.aaa.mom.NotificationXARollback notRoll = new fr.dyade.aaa.mom.NotificationXARollback(msgXARoll.getMessageMOMExternID(), msgXARoll.sessionID);
			agentClient.sendNotification(to, notRoll);
		}
	}	
	
	/** notification to roolback a set of messages from a Topicsession */
	protected void notificationXARollbackTopic(XARollbackTopicMOMExtern msgXARoll) throws Exception{ 
		SessionSubscription sessionSub = null;		
		if((sessionSub = (SessionSubscription) sessionTemporarySubscriptionTable.get(msgXARoll.sessionID))==null) {
			sessionSub = (SessionSubscription) sessionDurableSubscriptionTable.get(msgXARoll.sessionID);
		}
				
		if(sessionSub==null) 
			throw(new MOMException("No Session corresponds to the Message delivered by the Topic",MOMException.NO_SUCH_SESSION_EXIST));
			
		Enumeration	e = sessionSub.subSessionVector.elements();
		fr.dyade.aaa.mom.KeyClientSubscription key;
		fr.dyade.aaa.mom.SubscriptionClient sub ;
		while(e.hasMoreElements()) {
			key = (fr.dyade.aaa.mom.KeyClientSubscription) e.nextElement();
			sub = (fr.dyade.aaa.mom.SubscriptionClient) subscriptionTable.get(key);
				
			/* set the message as Redelivered */
			sub.rollbackDeliveredMsg();
		}
			
		/*	send the agreement to the client */
		fr.dyade.aaa.mom.RequestAgreeMOMExtern msgAgree = new fr.dyade.aaa.mom.RequestAgreeMOMExtern(msgXARoll.getMessageMOMExternID());
		agentClient.sendMessageMOMExtern(msgAgree);
	
		/* delivers the message to set as redelivered if possible */
		if(startMode) {
			fr.dyade.aaa.mom.Message msg;
			Enumeration	eDeliver = sessionSub.subSessionVector.elements();
				
			while(eDeliver.hasMoreElements()) {
				key = (fr.dyade.aaa.mom.KeyClientSubscription) eDeliver.nextElement();
				sub = (fr.dyade.aaa.mom.SubscriptionClient) subscriptionTable.get(key);
				while((msg = sub.deliveryMessage())!=null) {
					fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern msgDeliver = new fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern((long) -1, key.nameSubscription, msg, sub.getNameTheme());
					agentClient.sendMessageMOMExtern(msgDeliver);
				}
			}
		}
	}	
}
