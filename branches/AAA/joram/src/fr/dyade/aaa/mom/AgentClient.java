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
import java.io.*; 
import fr.dyade.aaa.agent.*;
import fr.dyade.aaa.config.*;

/**
 *	@see	fr.dyade.aaa.mom.CommonClientAAA	
 *	@see	fr.dyade.aaa.mom.Queue
 *	@see	fr.dyade.aaa.mom.Topic
 *	@see	fr.dyade.aaa.mom.SubscriptionClient
 */


public class AgentClient extends fr.dyade.aaa.mom.ConnectionFactory implements AgentClientItf {
    
    /** CommonClient : Common behaviour of all the agentClient */
    CommonClientAAA commonClient;

    public AgentClient() {
	super();
	commonClient = new CommonClientAAA(this);
    }
    
    public void react(AgentId from, Notification not) throws Exception { 
	try {
	    if(Debug.debug)
		if(Debug.clientTest)
		    System.out.println("AgentClient: Message "+not.getClass().getName());	 
	    
	    if (not instanceof NotificationMOMException) { 
		commonClient.notificationMOMException(from, (NotificationMOMException) not); 
	    } else if (not instanceof NotificationMessageDeliver) { 
		commonClient.notificationMessageDeliver(from, (NotificationMessageDeliver) not); 
	    } else if (not instanceof NotificationReadDeliver) { 
		commonClient.notificationReadDeliver(from, (NotificationReadDeliver) not);
	    } else if (not instanceof NotificationTopicMessageDeliver) { 
		commonClient.notificationTopicMessageDeliver(from, (NotificationTopicMessageDeliver) not);
	    } else if (not instanceof ExceptionNotification) { 
		commonClient.notificationEngineException(from, (ExceptionNotification) not);
	    } else if (not instanceof NotificationInputMessage) {
		commonClient.treatmentExternRequest((NotificationInputMessage) not);
	    } else if (not instanceof NotificationAgreeAsk) { 
		commonClient.notificationRequestAgree(from, (NotificationAgreeAsk) not);
	    } else  if (not instanceof fr.dyade.aaa.agent.UnknownAgent) {
		commonClient.notificationUnkownAgent((fr.dyade.aaa.agent.UnknownAgent) not);
	    } else if (not instanceof DriverDone) { 
		commonClient.notificationCloseConnection();
		super.react(from, not);
	    } else  if (not instanceof fr.dyade.aaa.ip.ConnectNot) {
		commonClient.notificationBeginingConnection();
		super.react(from, not);
	    } else  if (not instanceof NotificationAdminDeleteDestination) {
		this.delete();
	    } else {
		super.react(from, not); 
	    } 
	} catch (Exception exc) {
	    if(Debug.debug)
		if(Debug.clientSub)
		    System.err.println(exc);
	    
	    /* canceling previous actions du to an ack of tha Topic which didn't exist */
	    if(exc instanceof MOMException) {
		MOMException excMOM = (MOMException) exc;
		/* spreading of the exception */
		if(excMOM.getErrorCode()==MOMException.TOPIC_MESSAGEID_NO_EXIST)
		    throw(exc);
	    }
			
	    /* exception not identify */
	    commonClient.deliverAlienException(exc); 
	}
    }
    
    /** send a Message to a client */
    public void sendMessageMOMExtern(fr.dyade.aaa.mom.MessageMOMExtern msgMOMExtern) {
	fr.dyade.aaa.mom.NotificationOutputMessage not = new fr.dyade.aaa.mom.NotificationOutputMessage(msgMOMExtern);
	qout.push(not);
    }
    
    /** send a notification to a Queue or a Topic (destination "object") */
    public void sendNotification(fr.dyade.aaa.agent.AgentId to, fr.dyade.aaa.agent.Notification not) {
	sendTo(to, not);
    }
}
