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
import fr.dyade.aaa.joram.*;

/**	generic behaviour for all the clients in the MOM-AAA
 *
 *	@see	fr.dyade.aaa.mom.AgentClient
 */


public class CommonClientAAA implements java.io.Serializable {
	
    /** delivery mode which allows duplication messages. */ 
    public static final int TRANSACTED = 0; 
	
    /** delivery mode which allows duplication messages. */ 
    public static final int DUPS_OK_ACKNOWLEDGE  = javax.jms.Session.DUPS_OK_ACKNOWLEDGE; 
 
    /** automatic acknowledege delivery mode. */ 
    public static final int AUTO_ACKNOWLEDGE  = javax.jms.Session.AUTO_ACKNOWLEDGE; 

    /** delivery mode which allows client to acknowledge messages himself. */ 
    public static final int CLIENT_ACKNOWLEDGE  =  javax.jms.Session.CLIENT_ACKNOWLEDGE; 
	
    /** reference to an AgentClient */
    protected AgentClientItf agentClient; 
	
    /** counter for message identifier */
    private String stringID;
	
    /** hashtable with all of the subscriptions of the client 
     *	key		:	KeyClientSubscription
     *	object	:	SubscriptionClient
     */
    protected Hashtable subscriptionTable;
	
    /** vector containing all of the Temporary Queue or Topic
     *	created by the agentClient;
     */
    private Vector temporaryQueueTopicVector;
	
    /** if a client has an ExceptionListener */
    protected boolean exceptionMode;
	
    /** if the clients is ready to receive message or not */
    protected boolean startMode;
	
    /** if the client is connected or not */
    protected boolean connectMode;
	
    /** the connectionMetaData whivh contians all the informations
     *	about the MOM
     */
    private fr.dyade.aaa.mom.MetaData metaData;
	
    /** this hashtable contains the temporary subscriptions of the client
     *	by sesion so as to discard temporary subscription in case of
     *	closing of the Session
     *	key		:	sessionID	
     *	object	:	SessionSubscription (vector of KeyClientSubscription, ...)
     */
    protected Hashtable sessionTemporarySubscriptionTable;
	
    /** this hashtable contains the durable subscriptions of the client
     *	by session so as to stop delivery of the messages
     *	key		:	sessionID	
     *	object	:	SessionSubscription (vector of KeyClientSubscription, ...)
     */
    protected Hashtable sessionDurableSubscriptionTable;
	
    /**	this hashtable contains the requests of subscriptions of the client
     *	not yet acccepted. These table allows to refind if the subscription 
     *	is durable or temporary. 
     *	If this table didn't exist, the only solution would have been to
     *	add a new entry to 1 of the "subscription" Table without knowing 
     *	if the sub was accepted by the Topic. It must be avoided.
     *	Moreover we have to know the mode of acknowledgment of the session
     *	An other solution would have to add the sessionID to the notification of
     *	subscription, but we have to take into account rate.
     *	key		:	longrequestID	
     *	object	:	NotYetSubRecordObject = (sessionID, subDurable)
     */
    private Hashtable notYetSubscriptionRecordTable;
	
    /** vector of messages from the Queue which were'nt able to
     *	be delivered because the client is in stopMode
     *	ie vector which contains MessageQueueDeliverMOMExtern
     */
    private Vector messageSynchroRecVector;
	
    /** Vector of the agentId of all the Queue asked at least one time by the client
     *	will allow to check if the name of the Queue exist
     */
    private Vector queueAgentIdAskedVector;
	
    /**
     * Object storing the messages and the acks received from the client
     * during an XA transaction and waiting for a commit.
     */
    private XidTable xidTable;


    public CommonClientAAA(AgentClientItf agentClient) {
	stringID = "a";
	this.agentClient = agentClient;
	subscriptionTable = new Hashtable();
	exceptionMode = false;
	startMode = false;
	connectMode = false;
	metaData = new fr.dyade.aaa.mom.MetaData();
	sessionTemporarySubscriptionTable = new Hashtable();
	sessionDurableSubscriptionTable = new Hashtable();
	notYetSubscriptionRecordTable =  new Hashtable();
	temporaryQueueTopicVector = new Vector();
	messageSynchroRecVector = new Vector();
	queueAgentIdAskedVector = new Vector();
	xidTable = new XidTable();
    }

    /** notification of exception sent by a Queue or a Topic */
    protected void notificationMOMException(AgentId from, NotificationMOMException not) {
	/* print of notification type and the exception*/
	if(Debug.debug)
	    if(Debug.clientTest)
		System.out.println("\n MOM "+((Agent)agentClient).getId().toString()+" "+not.except+" Erreur Code : "+not.except.getErrorCode());
		
	/* treatment of exception */
	if(not.typeNotification instanceof NotificationQueueSend) {
	    fr.dyade.aaa.mom.NotificationQueueSend notQueue = (NotificationQueueSend) not.typeNotification;
	    fr.dyade.aaa.mom.ExceptionSendMessageMOMExtern msgExc = new fr.dyade.aaa.mom.ExceptionSendMessageMOMExtern(notQueue.notMOMID, not.except, notQueue.msg);
	    agentClient.sendMessageMOMExtern(msgExc);
	} else if(not.typeNotification instanceof NotificationTopicSend) {
	    fr.dyade.aaa.mom.NotificationTopicSend notTopic = (NotificationTopicSend) not.typeNotification;
	    fr.dyade.aaa.mom.ExceptionSendMessageMOMExtern msgExc = new fr.dyade.aaa.mom.ExceptionSendMessageMOMExtern(notTopic.notMOMID, not.except, notTopic.msg);
	    agentClient.sendMessageMOMExtern(msgExc);
	} else if(not.typeNotification instanceof NotificationSubscription) {
	    fr.dyade.aaa.mom.NotificationSubscription notSub = (NotificationSubscription) not.typeNotification;
			
	    /* discard the entry in the temporary notYetSubscriptionRecordTable table */
	    notYetSubscriptionRecordTable.remove(new Long(notSub.notMOMID));
				
	    /* constructs an ExceptionMessage and send to the client */
	    fr.dyade.aaa.mom.ExceptionMessageMOMExtern msgExc = new fr.dyade.aaa.mom.ExceptionMessageMOMExtern(notSub.notMOMID, not.except);
	    agentClient.sendMessageMOMExtern(msgExc);
	} else {
	    /* constructs an ExceptionMessage and send to the client */
	    fr.dyade.aaa.mom.ExceptionMessageMOMExtern msgExc = new fr.dyade.aaa.mom.ExceptionMessageMOMExtern(not.typeNotification.notMOMID, not.except);
	    agentClient.sendMessageMOMExtern(msgExc);
	}
    }
	
    /** notification which contains a message sent by a Queue */
    protected void notificationMessageDeliver(AgentId from, NotificationMessageDeliver not) throws Exception {
	/* tests for agentClient */
	if(Debug.debug)
	    if(Debug.clientTest && (not.msg instanceof fr.dyade.aaa.mom.TextMessage)) {
		fr.dyade.aaa.mom.TextMessage msgTxt = (fr.dyade.aaa.mom.TextMessage) not.msg;
				/* print the message body */
		if(msgTxt!=null) {
		    try {
			System.out.println("Queue "+((Agent)agentClient).getId().toString()+": "+msgTxt.getText());
		    } catch (Exception exc) {
			System.err.println("Error de print on Queue "+exc);
		    }
		} else
		    System.out.println("\n"+((Agent)agentClient).getId().toString()+" le message recu est null : ");
	    }
		
	if(connectMode) {
	    fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern msgQueueDeliver = new fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern(not.notMOMID, not.msg);		
	    if(startMode) {
				/* constructs a QueueDeliverMessage and send to the client */
		agentClient.sendMessageMOMExtern(msgQueueDeliver);
	    } else {
				/* add the message to the vector for later delivery */
		messageSynchroRecVector.addElement(msgQueueDeliver);
	    }
	} 
    }
	
    /** notification which contains an enumeration of messages presents in a Queue */ 
    protected void notificationReadDeliver(AgentId from, NotificationReadDeliver not) {
	if(Debug.debug)
	    if(Debug.clientTest) {
		try {
		    Enumeration queueEnumeration = not.messageEnumerate.elements();
		    fr.dyade.aaa.mom.TextMessage msgTxt ;
			
		    /* Print */
		    while(queueEnumeration.hasMoreElements()) {
			fr.dyade.aaa.mom.Message msg = (fr.dyade.aaa.mom.Message) queueEnumeration.nextElement();
			if(msg instanceof fr.dyade.aaa.mom.TextMessage) {
			    msgTxt = (fr.dyade.aaa.mom.TextMessage) msg;
			    System.out.println("Queue : "+msgTxt.getText());
			}
		    }
		} catch (Exception exc) {
		    System.err.println("\n"+((Agent)agentClient).getId().toString()+" "+exc);
		}
	    }
			
	/* constructs a ReadDeliverMessage and send to the client */
	fr.dyade.aaa.mom.ReadDeliverMessageMOMExtern msgReadDeliver = new fr.dyade.aaa.mom.ReadDeliverMessageMOMExtern(not.notMOMID, not.messageEnumerate);
	agentClient.sendMessageMOMExtern(msgReadDeliver);
    } 
	
    /** notification which contains a message sent by a Theme of a Topic */ 
    protected void notificationTopicMessageDeliver(AgentId from, NotificationTopicMessageDeliver not) throws Exception {
	fr.dyade.aaa.mom.SubscriptionClient sub ;
	fr.dyade.aaa.mom.TopicNaming topic = (fr.dyade.aaa.mom.TopicNaming) not.msg.getJMSDestination();
	fr.dyade.aaa.mom.KeyClientSubscription key = new fr.dyade.aaa.mom.KeyClientSubscription(not.nameSubscription, from, not.theme);
		
	if(Debug.debug)
	    if(Debug.clientSub && (not.msg instanceof fr.dyade.aaa.mom.TextMessage)) {
		try {
		    System.out.println("absolute name : "+from.toString()+"_"+not.theme+" sanmeSub : "+not.nameSubscription);
		    Debug.printKeysSubscription(((Agent)agentClient).getId().toString()+" ",subscriptionTable);
		    System.out.println("Topic "+((Agent)agentClient).getId().toString()+": "+((fr.dyade.aaa.mom.TextMessage)not.msg).getText());
		} catch (Exception exc) {
		    System.err.println("notificationTopicMessageDeliver"+((Agent)agentClient).getId().toString()+" "+exc);
		}
	    }
			
	if((sub = (fr.dyade.aaa.mom.SubscriptionClient) subscriptionTable.get(key))==null) 
	    throw(new MOMException("Delivery Message NotAccepted : Subscription doesn't exist ",MOMException.MSG_RECEIVED_WITHOUT_SUBSCRIPTION));
		
	/* add message in the specific queue */
	sub.putMessageInAgentClient(not.msg);
			
	if(connectMode && startMode) {
	    /* add the message in the queue of autoAck Session if any */
	    String sessionID = sub.getSessionID();
	    if(sessionID!=null) { 
		SessionSubscription sessionSub = null;		
		if((sessionSub = (SessionSubscription) sessionTemporarySubscriptionTable.get(sessionID))==null) {
		    sessionSub = (SessionSubscription) sessionDurableSubscriptionTable.get(sessionID);
		}

		if(sessionSub==null) 
		    throw(new MOMException("No Session corresponds to the Message delivered by the Topic",MOMException.NO_SUCH_SESSION_EXIST));
				
				
		if(sessionSub.ackMode!=AUTO_ACKNOWLEDGE) {
		    /* delivers the message if the client arose a messageListener */
		    if(sub.getMessageListener()) {
			fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern msgDeliver = new fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern((long) -1, not.nameSubscription, not.msg, not.theme);
			agentClient.sendMessageMOMExtern(msgDeliver);
		    }
		} else {
		    /* add the message in the queue of messages to deliver */
		    sessionSub.addDeliveredMessage(sub, 1);
		    /* delivers the message if possible */
		    fr.dyade.aaa.mom.Message msgAuto;
		    if((msgAuto = sessionSub.deliveryMessage())!=null) {
			fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern msgDeliver = new fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern((long) -1, not.nameSubscription, msgAuto, not.theme);
			agentClient.sendMessageMOMExtern(msgDeliver);
		    }
		} 
	    }
	}
    }
	
    /** notification which holds a serious exception from a Queue or a Topic */ 
    protected void notificationEngineException(AgentId from, ExceptionNotification not) {
	if(Debug.debug)
	    if(Debug.clientTest) {
		if (not.exc instanceof MOMException) 
		    System.out.println("\n MOM "+((Agent)agentClient).getId().toString()+" "+not.exc+" Erreur Code : "+((MOMException) not.exc).getErrorCode());
		else
		    System.err.println("\n AgentClient "+((Agent)agentClient).getId().toString()+" "+not.exc);
	    }
		
	/* treatment of the exception */
	if(not.not instanceof NotificationAck) {
	    /* exception due to an ack */
	    /* this exception is treated hier so as to do only 1 pass in the Queue */
	    fr.dyade.aaa.mom.NotificationAck notAck = (NotificationAck) not.not;
	    fr.dyade.aaa.mom.ExceptionMessageMOMExtern msgExc = new fr.dyade.aaa.mom.ExceptionMessageMOMExtern(notAck.notMOMID, not.exc);
	    agentClient.sendMessageMOMExtern(msgExc);
	} else {
	    deliverAlienException(not.exc);
	}
    }
	
    /** treatment of the agreement sent by the Queue or the Topic (on request of the client) */
    protected void notificationRequestAgree(AgentId from, NotificationAgreeAsk not) throws Exception {
	if (not.typeNotification instanceof NotificationQueueSend) {
	    /* sending to a Queue agreement */
	    fr.dyade.aaa.mom.NotificationQueueSend notQueue = (NotificationQueueSend) not.typeNotification;
	    fr.dyade.aaa.mom.SendingBackMessageMOMExtern msgAgree = new fr.dyade.aaa.mom.SendingBackMessageMOMExtern(notQueue.notMOMID, notQueue.msg);
	    agentClient.sendMessageMOMExtern(msgAgree);
	} else if (not.typeNotification instanceof NotificationTopicSend) {
	    /* sending to a Topic agreement */
	    fr.dyade.aaa.mom.NotificationTopicSend notTopic = (NotificationTopicSend) not.typeNotification;
	    fr.dyade.aaa.mom.SendingBackMessageMOMExtern msgAgree = new fr.dyade.aaa.mom.SendingBackMessageMOMExtern(notTopic.notMOMID, notTopic.msg);
	    agentClient.sendMessageMOMExtern(msgAgree);
	} else if (not.typeNotification instanceof NotificationAck) {
	    /* acknowledge agreement */
	    fr.dyade.aaa.mom.NotificationAck notAck = (NotificationAck) not.typeNotification;
	    fr.dyade.aaa.mom.RequestAgreeMOMExtern msgAgree = new fr.dyade.aaa.mom.RequestAgreeMOMExtern(notAck.notMOMID);
	    agentClient.sendMessageMOMExtern(msgAgree);
	} else if (not.typeNotification instanceof NotificationSubscription) {
	    treatmentSubscriptionAgreement(from, (NotificationSubscription) not.typeNotification);
	} else if (not.typeNotification instanceof NotificationUnsubscription) {
	    treatmentUnsubscriptionAgreement(from, (NotificationUnsubscription) not.typeNotification);
	} else if (not.typeNotification instanceof NotificationRecover) {
	    /* acknowledge agreement */
	    fr.dyade.aaa.mom.NotificationRecover notRecover = (NotificationRecover) not.typeNotification;
	    fr.dyade.aaa.mom.RequestAgreeMOMExtern msgAgree = new fr.dyade.aaa.mom.RequestAgreeMOMExtern(notRecover.notMOMID);
	    agentClient.sendMessageMOMExtern(msgAgree);
	} else if (not.typeNotification instanceof NotificationUpdateSubscription) {
	    treatmentUpdateSubscrptionAgreement(from, (NotificationUpdateSubscription) not.typeNotification);	
	} else {
	    /* would never past but costs nothing to treat */
	    deliverAlienException(new MOMException("Subclass of NotificationAgreeAskknown Unknown",MOMException.DEFAULT_MOM_ERROR));
	}
    } 
	
    /* treatment of the subscription agreement */
    protected void treatmentSubscriptionAgreement(AgentId from, NotificationSubscription notSub) {
	fr.dyade.aaa.mom.KeyClientSubscription key = new fr.dyade.aaa.mom.KeyClientSubscription(notSub.nameSubscription, from, notSub.theme);
			
	if(subscriptionTable.containsKey(key)) {
	    /* I don't know how it would be possible */
	    MOMException exc = new MOMException("Subscription already exist in AgentClient",MOMException.SUBSCRIPTION_ALREADY_EXIST);
				
	    /* warning sent to the administrator*/
	    warningAdministrator(exc);
	}
		
	/* add the subscription either in the durable table or the temporary */
	NotYetSubRecordObject objRecord;
	if((objRecord = (NotYetSubRecordObject) notYetSubscriptionRecordTable.remove(new Long(notSub.notMOMID)))!=null) {
	    SessionSubscription sessionSub = null;
				
	    if(objRecord.subDurable) {	
				/* checks if already a durable subscription exists for the session */
		if((sessionSub = (SessionSubscription) sessionDurableSubscriptionTable.get(objRecord.sessionID))==null) {
		    /* no entry */
		    sessionSub = new fr.dyade.aaa.mom.SessionSubscription(objRecord.ackMode);
		    sessionSub.subSessionVector.addElement(key);
		    sessionDurableSubscriptionTable.put(objRecord.sessionID, sessionSub);
		} else {
		    /* already at least 1 entry */
		    sessionSub.subSessionVector.addElement(key);
		}
	    } else if(connectMode) {
				/* checks if already a no durable subscribtion exists */
		if((sessionSub = (SessionSubscription) sessionTemporarySubscriptionTable.get(objRecord.sessionID))==null) {
		    /* no entry */
		    sessionSub = new fr.dyade.aaa.mom.SessionSubscription(objRecord.ackMode);
		    sessionSub.subSessionVector.addElement(key);
		    sessionTemporarySubscriptionTable.put(objRecord.sessionID, sessionSub);
		} else {
		    /* already at least 1 entry */
		    sessionSub.subSessionVector.addElement(key);
		}
	    }
			
	    if(objRecord.subDurable || connectMode) {
				/* construction and record of the subscription in the agentClient */
		fr.dyade.aaa.mom.SubscriptionClient sub = new fr.dyade.aaa.mom.SubscriptionClient(notSub.noLocal, notSub.selector, from, notSub.theme, objRecord.sessionID);
		subscriptionTable.put(key, sub);
				
				/* add the subscription to the delivery of the message  */
		if(connectMode && (sessionSub.ackMode==CommonClientAAA.AUTO_ACKNOWLEDGE))
		    sessionSub.addDeliveredMessage(sub, sub.queueThemeMessage.size());
	    }
	}	
			
		
	if(Debug.debug)
	    if(Debug.clientSub) 
		Debug.printKeysSubscription("AgentClient "+((Agent)agentClient).getId().toString()+" ",subscriptionTable);
		
	/* send the agreement to the client */
	fr.dyade.aaa.mom.RequestAgreeMOMExtern msgAgree = new fr.dyade.aaa.mom.RequestAgreeMOMExtern(notSub.notMOMID);
	agentClient.sendMessageMOMExtern(msgAgree);	
    }
	
    /* treatment of the unsubscription agreement */
    protected void treatmentUnsubscriptionAgreement(AgentId from, NotificationUnsubscription notUnsub) throws Exception{
	fr.dyade.aaa.mom.SubscriptionClient sub ;
	fr.dyade.aaa.mom.KeyClientSubscription key = new fr.dyade.aaa.mom.KeyClientSubscription(notUnsub.nameSubscription, from, notUnsub.theme);
			
	/* remove the entry in the hashtable of the agentClient */
	if((sub = (fr.dyade.aaa.mom.SubscriptionClient) subscriptionTable.remove(key))==null) {
	    /* I don't know how it would be possible */
	    MOMException exc = new MOMException("Remove Impossible : Subscription doesn't exist in AgentClient",MOMException.SUBSCRIPTION_NO_EXIST);
				
	    /* warning sent to the administrator*/
	    warningAdministrator(exc);
	}
		
	/* remove the durable subscription either in the durable table or the temporary */
	NotYetSubRecordObject objRecord;
	if((objRecord = (NotYetSubRecordObject) notYetSubscriptionRecordTable.remove(new Long(notUnsub.notMOMID)))!=null) {
		
	    SessionSubscription sessionSub = (SessionSubscription) sessionDurableSubscriptionTable.get(objRecord.sessionID);
	    if(sessionSub!=null) {
				/* asynchronous mode => closeConnexion before this method (if) */
		if(sessionSub.subSessionVector!=null) {
		    sessionSub.subSessionVector.removeElement(key);
			
		    /* releases the ressource if any */
		    if(sessionSub.ackMode==CommonClientAAA.AUTO_ACKNOWLEDGE)
			sessionSub.removeSubFromDelivery(sub);
		}
	    }
	} 
			
	if(connectMode) {
	    /* send the agreement to the client */
	    fr.dyade.aaa.mom.RequestAgreeMOMExtern msgAgree = new fr.dyade.aaa.mom.RequestAgreeMOMExtern(notUnsub.notMOMID);
	    agentClient.sendMessageMOMExtern(msgAgree);
	}
    }
	
    /* treatment of the updating of a subscription agreement */
    protected void treatmentUpdateSubscrptionAgreement(AgentId from, NotificationUpdateSubscription not) throws Exception {
	fr.dyade.aaa.mom.SubscriptionClient sub ;
	fr.dyade.aaa.mom.KeyClientSubscription key = new fr.dyade.aaa.mom.KeyClientSubscription(not.nameSubscription, from, not.theme);			
			
	if((sub = (fr.dyade.aaa.mom.SubscriptionClient) subscriptionTable.get(key))==null){
	    /* I don't know how it would be possible */
	    MOMException exc = new MOMException("Remove Impossible : Subscription doesn't exist in AgentClient",MOMException.SUBSCRIPTION_NO_EXIST);
				
	    /* warning sent to the administrator*/
	    warningAdministrator(exc);
	} 
			
	/* updating of the parameter in the agentClient */
	sub.updateSubscription(((Agent)agentClient).getId(), not.noLocal, not.selector);
		
	/* send the agreement to the client */
	fr.dyade.aaa.mom.RequestAgreeMOMExtern msgAgree = new fr.dyade.aaa.mom.RequestAgreeMOMExtern(not.notMOMID);
	agentClient.sendMessageMOMExtern(msgAgree);	
    }
	
    /** treatment of the requests of the extern client */
    protected void treatmentExternRequest(NotificationInputMessage not) throws MOMException {
	try {
	    if(Debug.debug)
		if(Debug.clientTest)
		    System.out.println("CommonClient: Message Extern"+not.msgMOMExtern.getClass().getName());	 
			
	    if (not.msgMOMExtern instanceof ReceptionMessageMOMExtern) { 
		this.notificationReceiveSync((ReceptionMessageMOMExtern) not.msgMOMExtern); 
	    } else if (not.msgMOMExtern instanceof ReadOnlyMessageMOMExtern) { 
		this.notificationReadOnly((ReadOnlyMessageMOMExtern) not.msgMOMExtern); 
	    } else if (not.msgMOMExtern instanceof SendingMessageQueueMOMExtern) { 
		this.notificationQueueSend((SendingMessageQueueMOMExtern) not.msgMOMExtern); 
	    } else if (not.msgMOMExtern instanceof SendingMessageTopicMOMExtern) { 
		this.notificationTopicSend((SendingMessageTopicMOMExtern) not.msgMOMExtern); 
	    } else if (not.msgMOMExtern instanceof AckQueueMessageMOMExtern) { 
		notificationQueueAck((AckQueueMessageMOMExtern) not.msgMOMExtern);
	    } else if (not.msgMOMExtern instanceof AckTopicMessageMOMExtern) { 
		notificationTopicAck((AckTopicMessageMOMExtern) not.msgMOMExtern);
	    } else if (not.msgMOMExtern instanceof AckMSPMessageMOMExtern) { 
		notificationMSPAck((AckMSPMessageMOMExtern) not.msgMOMExtern);
	    } else if (not.msgMOMExtern instanceof SettingListenerMOMExtern) { 
		notificationSettingListener((SettingListenerMOMExtern) not.msgMOMExtern);
	    } else if (not.msgMOMExtern instanceof StateListenMessageMOMExtern) { 
		notificationStateListen((StateListenMessageMOMExtern) not.msgMOMExtern);
	    } else if (not.msgMOMExtern instanceof SubscriptionNoDurableMOMExtern) { 
		notificationNoDurableSub((SubscriptionNoDurableMOMExtern) not.msgMOMExtern);
	    } else if (not.msgMOMExtern instanceof SubscriptionMessageMOMExtern) { 
		this.notificationSubscription((SubscriptionMessageMOMExtern) not.msgMOMExtern); 
	    } else if (not.msgMOMExtern instanceof UnsubscriptionMessageMOMExtern) { 
		this.notificationUnsubscription((UnsubscriptionMessageMOMExtern) not.msgMOMExtern); 
	    } else if (not.msgMOMExtern instanceof CloseSubscriberMOMExtern) { 
		notificationCloseSubscriber((CloseSubscriberMOMExtern) not.msgMOMExtern);
	    } else if (not.msgMOMExtern instanceof CloseTopicSessionMOMExtern) { 
		notificationCloseTopicSession((CloseTopicSessionMOMExtern) not.msgMOMExtern);
	    } else if (not.msgMOMExtern instanceof CreationTemporaryQueueMOMExtern) { 
		notificationCreationTemporaryQueue((CreationTemporaryQueueMOMExtern) not.msgMOMExtern);
	    } else if (not.msgMOMExtern instanceof CreationTemporaryTopicMOMExtern) { 
		notificationCreationTemporaryTopic((CreationTemporaryTopicMOMExtern) not.msgMOMExtern);
	    } else if (not.msgMOMExtern instanceof CloseDestinationMOMExtern) { 
		notificationCloseDestination((CloseDestinationMOMExtern) not.msgMOMExtern);
	    } else if (not.msgMOMExtern instanceof SettingExcListenerMOMExtern) { 
		notificationSettingExcListener((SettingExcListenerMOMExtern) not.msgMOMExtern);
	    } else if (not.msgMOMExtern instanceof CreationWorkerQueueMOMExtern) { 
		notificationCreationWorkerQueue((CreationWorkerQueueMOMExtern) not.msgMOMExtern);
	    } else if (not.msgMOMExtern instanceof MetaDataRequestMOMExtern) { 
		notificationMetaData((MetaDataRequestMOMExtern) not.msgMOMExtern);
	    } else if (not.msgMOMExtern instanceof RecoverMsgMOMExtern) { 
		notificationRecover((RecoverMsgMOMExtern) not.msgMOMExtern);
	    } else if (not.msgMOMExtern instanceof UpdatingSubscriptionMOMExtern) { 
		notificationUpdateSubscription((UpdatingSubscriptionMOMExtern) not.msgMOMExtern);
	    } else if (not.msgMOMExtern instanceof MessageAdminCreateTopic) {
		adminCreateTopic((MessageAdminCreateTopic) not.msgMOMExtern);
	    } else if (not.msgMOMExtern instanceof MessageAdminDeleteTopic) {
		adminDeleteTopic((MessageAdminDeleteTopic) not.msgMOMExtern);
	    } else if (not.msgMOMExtern instanceof MessageAdminCreateQueue) {
		adminCreateQueue((MessageAdminCreateQueue) not.msgMOMExtern);
	    } else if (not.msgMOMExtern instanceof MessageAdminDeleteQueue) {
		adminDeleteQueue((MessageAdminDeleteQueue) not.msgMOMExtern);
	    } else  if (not.msgMOMExtern instanceof MessageAdminGetAgentClient) {
		adminGetAgentClient((MessageAdminGetAgentClient) not.msgMOMExtern);
	    } else  if (not.msgMOMExtern instanceof MessageAdminDeleteAgentClient) {
		adminDeleteAgentClient((MessageAdminDeleteAgentClient) not.msgMOMExtern);
	    } else  if (not.msgMOMExtern instanceof MessageAdminCleanDriver) {
		adminCleanDriver((MessageAdminCleanDriver) not.msgMOMExtern);
	    } else  if (not.msgMOMExtern instanceof MessageTransactedVector) {
		notificationTransactedVectorSend((MessageTransactedVector) not.msgMOMExtern);
	    } else  if (not.msgMOMExtern instanceof MessageTransactedRollback) {
		notificationTransactedRollback((MessageTransactedRollback) not.msgMOMExtern);
	    } else if (not.msgMOMExtern instanceof MessageXAPrepare) {
		notificationXAPrepare((MessageXAPrepare) not.msgMOMExtern);
	    } else if (not.msgMOMExtern instanceof MessageXACommit) {
		notificationXACommit((MessageXACommit) not.msgMOMExtern);
	    } else if (not.msgMOMExtern instanceof MessageXARollback) {
		notificationXARollback((MessageXARollback) not.msgMOMExtern);
	    } else if (not.msgMOMExtern instanceof MessageXARecover) {
		notificationXARecover((MessageXARecover) not.msgMOMExtern);
	    } else if (not.msgMOMExtern instanceof MessageAdminCreateSpecific) {
		adminCreatespecific((MessageAdminCreateSpecific) not.msgMOMExtern);
	    } else {
		/* would never past but costs nothing to treat */
		deliverAlienException(new MOMException("Subclass of NotificationInputMessage Unknown",MOMException.DEFAULT_MOM_ERROR));
	    }
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
	
    /** Get agentClient with the admin tools */
    protected void adminGetAgentClient(MessageAdminGetAgentClient msg) throws Exception {
	if (Debug.debug)
	    if (Debug.admin)
		System.out.println("->CommonClientAAA : adminGetAgentClient " + msg.toString() + 
				   "\n  agentClient " + agentClient);
	msg.setAgent(((Agent)agentClient).getId().toString());
	agentClient.sendMessageMOMExtern(msg);
	if (Debug.debug)
	    if (Debug.admin)
		System.out.println("<-CommonClientAAA : adminGetAgentClient : "+ msg.getAgent());
    }

    /** Delete agent with the admin tools */
    protected void adminDeleteAgentClient(MessageAdminDeleteAgentClient msg) throws Exception {
	if (Debug.debug)
	    if (Debug.admin)
		System.out.println("->CommonClientAAA : adminDeleteAgentClient " + msg.toString() + 
				   "\n  agentClient " + agentClient);
	agentClient.sendNotification(AgentId.fromString(msg.getAgent()),new NotificationAdminDeleteDestination());
	if (Debug.debug)
	    if (Debug.admin)
		System.out.println("<-CommonClientAAA : adminDeleteAgentClient : "+ msg.getAgent() + " deleted");
    }
    /** stop driver of this ProxyAgent */
    protected void adminCleanDriver(MessageAdminCleanDriver msg) throws Exception {
	if (Debug.debug)
	    if (Debug.admin)
		System.out.println("->CommonClientAAA : adminCleanDriver = " + msg.toString() + 
				   "\n  agentClient " + agentClient);
	((ProxyAgent) agentClient).cleanDriverOut();
	//agentClient.sendNotification(((Agent)agentClient).getId(),new fr.dyade.aaa.agent.DriverDone(msg.getDriver()));
	if (Debug.debug)
	    if (Debug.admin)
		System.out.println("<-CommonClientAAA : adminCleanDriver send to " + ((Agent)agentClient).getId());
    }
    /** Creation of Agent Topic with the admin tools */
    protected void adminCreateTopic(MessageAdminCreateTopic msg) throws Exception {
	if (Debug.debug)
	    if (Debug.admin)
		System.out.println("->CommonClientAAA : adminCreateTopic " + msg.toString() + 
				   "\n  agentClient " + agentClient);

	/* creation of the Topic */
	fr.dyade.aaa.mom.Topic agentTopic = new fr.dyade.aaa.mom.Topic();
	AgentId idTopic = agentTopic.getId();
		
	/* creation of the theme tree */
	agentTopic.constructTheme(". ;");
	agentTopic.deploy();
	/* send TopicName to creator (admin) */
	msg.setTopicName(idTopic.toString());
	agentClient.sendMessageMOMExtern(msg);
	if (Debug.debug)
	    if (Debug.admin)
		System.out.println("<-CommonClientAAA : adminCreateTopic : "+ idTopic.toString() + " deployed");
    }

    /** destruction of Agent Topic with the admin tools */
    protected void adminDeleteTopic(MessageAdminDeleteTopic msg) throws Exception {
	if (Debug.debug)
	    if (Debug.admin)
		System.out.println("<->CommonClientAAA : adminDeleteTopic " + msg.toString());
	agentClient.sendNotification(AgentId.fromString(msg.getTopicName()),new NotificationAdminDeleteDestination());
    }

    /** Creation of Agent Queue with the admin tools */
    protected void adminCreateQueue(MessageAdminCreateQueue msg) throws Exception {
	if (Debug.debug)
	    if (Debug.admin)
		System.out.println("->CommonClientAAA : adminCreateQueue " + msg.toString());

	/* creation of the Queue */
	fr.dyade.aaa.mom.Queue agentQueue = new fr.dyade.aaa.mom.Queue();
	AgentId idQueue = agentQueue.getId();

	agentQueue.deploy();
	/* send QueueName to creator (admin) */
	msg.setQueueName(idQueue.toString());
	if (Debug.debug)
	    if (Debug.admin)
		System.out.println("->CommonClientAAA : adminCreateQueue "+ idQueue.toString() + " deployed");
	agentClient.sendMessageMOMExtern(msg);
    }

    /** destruction of Agent Queue with the admin tools */
    protected void adminDeleteQueue(MessageAdminDeleteQueue msg) throws Exception {
	if (Debug.debug)
	    if (Debug.admin)
		System.out.println("->CommonClientAAA : adminDeleteQueue " + msg.toString());
	agentClient.sendNotification(AgentId.fromString(msg.getQueueName()),new NotificationAdminDeleteDestination());
    }

    /** Creation of specific Agent with the admin tools */
    protected void adminCreatespecific(MessageAdminCreateSpecific msg) throws Exception {
	if (Debug.debug)
	    if (Debug.admin)
		System.out.println("->CommonClientAAA : adminCreatespecific " + msg.toString());

	/* creation of the specific object */
	Object agent = null;
	try {
	    Class c = Class.forName(msg.getClassName());
	    agent = c.newInstance();
	} catch (Exception e) {
	    e.printStackTrace();
	    throw(e);
	}
	AgentId id = ((Agent) agent).getId();

	((Agent) agent).deploy();
	/* send id to creator (admin) */
	msg.setID(id.toString());
	if (Debug.debug)
	    if (Debug.admin)
		System.out.println("->CommonClientAAA : adminCreatespecific "+ id.toString() + " deployed");
	agentClient.sendMessageMOMExtern(msg);
    }

    /** notification for sending  messages to a Queue/Topic */
    protected void notificationTransactedVectorSend(MessageTransactedVector msgVector) throws Exception {
	if (Debug.debug)
	    if (Debug.transacted)
		System.out.println("->CommonClientAAA : notificationTransactedVectorSend  msgVector=" + msgVector.toString());

	Vector vect = msgVector.getVector();
	while(!vect.isEmpty()) {
	    Object msgInVect =  vect.firstElement();
	    if (msgInVect instanceof  SendingMessageQueueMOMExtern) {
		notificationQueueSend((SendingMessageQueueMOMExtern) msgInVect);
		vect.removeElementAt(0);
	    } else {
		notificationTopicSend((SendingMessageTopicMOMExtern) msgInVect);
		vect.removeElementAt(0);
	    }
	}
	/* Now, we can send an ACK to the client */
	MessageAckTransactedVector msgAgree = new MessageAckTransactedVector(msgVector.getMessageMOMExternID());
	agentClient.sendMessageMOMExtern(msgAgree);
	if (Debug.debug)
	    if (Debug.transacted)
		System.out.println("<-CommonClientAAA : notificationTransactedVectorSend  ACK send");
    }

    /** notification for sending Rollback  messages to destination (Queue or Topic) */
    protected void notificationTransactedRollback(MessageTransactedRollback msgVect) throws Exception {
	if (Debug.debug)
	    if (Debug.transacted)
		System.out.println("->CommonClientAAA : notificationTransactedRollback  msgVect=" + msgVect.toString());

	Vector vect = msgVect.getVector();
	if (msgVect.isTopicRollback()) {
	    /* for Topic */
	    while (!vect.isEmpty()) {
		AckTopicMessageMOMExtern msgInVect = (AckTopicMessageMOMExtern) vect.firstElement();
		notificationTransactedRollbackTopicAck(msgInVect);
		vect.removeElementAt(0);
	    }
	} else {
	    /* for Queue */
	    while(!vect.isEmpty()) {
		MessageRollbackMOMExtern msgInVect = (MessageRollbackMOMExtern) vect.firstElement();

		DestinationNaming dest = (DestinationNaming) msgInVect.getJMSDestination();
		AgentId to = AgentId.fromString(dest.getDestination());

		NotificationRollback notMsg = new NotificationRollback(msgInVect.getMessageMOMExternID(), msgInVect.getJMSMessageID(),msgInVect.getJMSSessionID());
		agentClient.sendNotification(to, notMsg);
		vect.removeElementAt(0);
	    }
	}
	/* Now, we can send an ACK to the client */
	MessageAckTransactedRollback msgAck = new MessageAckTransactedRollback(msgVect.getMessageMOMExternID());
	agentClient.sendMessageMOMExtern(msgAck);
	  
	if (Debug.debug)
	    if (Debug.transacted)
		System.out.println("<-CommonClientAAA : notificationTransactedRollback  ACK send");
    }

    /** send all msg again to the client after a topic Transacted Rollback  */
    protected void notificationTransactedRollbackTopicAck(AckTopicMessageMOMExtern msgMOMAck) throws Exception {
	try {
	    AgentId topic = AgentId.fromString(msgMOMAck.topic.getTopicName());
	    SubscriptionClient sub;
	    KeyClientSubscription key = new KeyClientSubscription(msgMOMAck.nameSubscription, topic, msgMOMAck.topic.getTheme());	
      
	    /* checks if the subscription exists */
	    if((sub = (SubscriptionClient) subscriptionTable.get(key))==null)
		throw (new MOMException("Impossible : Subscription doesn't exist in AgentClient",MOMException.TOPIC_MESSAGEID_NO_EXIST));
      
	    Message msg = null;
	    while((msg = sub.deliveryMessage())!=null) {
		MessageTopicDeliverMOMExtern msgDeliver = new MessageTopicDeliverMOMExtern((long) -1, msgMOMAck.nameSubscription, msg, sub.getNameTheme());
		agentClient.sendMessageMOMExtern(msgDeliver);
	    }
	    
	} catch (MOMException exc) {
	    /* constructs an ExceptionMessage and send to the client */
	    fr.dyade.aaa.mom.ExceptionMessageMOMExtern msgExc = new fr.dyade.aaa.mom.ExceptionMessageMOMExtern(msgMOMAck.getMessageMOMExternID(), exc);
	    agentClient.sendMessageMOMExtern(msgExc);
	}
    }
  
    /** notification for sending a message to a Queue */
    protected void notificationQueueSend(SendingMessageQueueMOMExtern msgSendMOMExtern) throws Exception { 
	fr.dyade.aaa.mom.QueueNaming queue = (fr.dyade.aaa.mom.QueueNaming) msgSendMOMExtern.message.getJMSDestination();
	AgentId to = AgentId.fromString(queue.getQueueName());
	fr.dyade.aaa.mom.Message msg = msgSendMOMExtern.message;
		
	/* set of the identifier of the Message */
	msg.setJMSMessageID(calculateMessageID());
		
	if(Debug.debug)
	    if(Debug.clientSend && (msg instanceof fr.dyade.aaa.mom.TextMessage))
		System.out.println("CommonClient: Message "+((TextMessage)msg).getText());	 
			
			
	/* checking the message */
	checking(msg);
		
	fr.dyade.aaa.mom.NotificationQueueSend notMsgSend = new fr.dyade.aaa.mom.NotificationQueueSend(msgSendMOMExtern.getMessageMOMExternID(), msg);
	agentClient.sendNotification(to, notMsgSend);
    }

    /** notification for sending a message to a Queue */
    protected void notificationTopicSend(SendingMessageTopicMOMExtern msgSendMOMExtern) throws Exception { 
	fr.dyade.aaa.mom.TopicNaming topic = (fr.dyade.aaa.mom.TopicNaming) msgSendMOMExtern.message.getJMSDestination();
	AgentId to = AgentId.fromString(topic.getTopicName());
	fr.dyade.aaa.mom.Message msg = msgSendMOMExtern.message;
			
	/* set of the identifier of the Message */
	msg.setJMSMessageID(calculateMessageID());
			
	/* checking the message */
	checking(msg);
		
	fr.dyade.aaa.mom.NotificationTopicSend notMsgSend = new fr.dyade.aaa.mom.NotificationTopicSend(msgSendMOMExtern.getMessageMOMExternID(), msg);
	agentClient.sendNotification(to, notMsgSend);
    }
	
    /** notification to acknowledge 1 or more messages in a Queue */ 
    protected void notificationQueueAck(AckQueueMessageMOMExtern msgMOMAck) { 
	AgentId to = AgentId.fromString(msgMOMAck.queue.getQueueName());
			
	/* send the acknowledgment to the Queue */
	fr.dyade.aaa.mom.NotificationAck notMsgAck = new fr.dyade.aaa.mom.NotificationAck(msgMOMAck.getMessageMOMExternID(), msgMOMAck.messageID, msgMOMAck.ackMode, msgMOMAck.sessionID);
	agentClient.sendNotification(to, notMsgAck);
    }
	
    /** notification to receive an enumeration of messages presents in a Queue */ 
    protected void notificationReadOnly(ReadOnlyMessageMOMExtern msgMOMExtern) { 
	AgentId to = AgentId.fromString(msgMOMExtern.queue.getQueueName());
		
	/* send the request to the Queue */
	fr.dyade.aaa.mom.NotificationReadOnly notMsgReadOnly = new fr.dyade.aaa.mom.NotificationReadOnly(msgMOMExtern.getMessageMOMExternID(), msgMOMExtern.selector);
	agentClient.sendNotification(to, notMsgReadOnly);
    }
	
    /** notification to receive a message from a Queue */ 
    protected void notificationReceiveSync(ReceptionMessageMOMExtern msgMOMExtern) { 
	AgentId to = AgentId.fromString(msgMOMExtern.queue.getQueueName());
		
	/* send the request to the Queue */
	fr.dyade.aaa.mom.NotificationReceiveSync notRecSync = new fr.dyade.aaa.mom.NotificationReceiveSync(msgMOMExtern.getMessageMOMExternID(), msgMOMExtern.timeOut, msgMOMExtern.selector, msgMOMExtern.sessionID); 
	agentClient.sendNotification(to, notRecSync);
    }
	
    /** notification to susbcribe to a theme of a Topic */
    protected void notificationSubscription(SubscriptionMessageMOMExtern msgSub) {
	AgentId to = AgentId.fromString(msgSub.topic.getTopicName());
	fr.dyade.aaa.mom.KeyClientSubscription key = new fr.dyade.aaa.mom.KeyClientSubscription(msgSub.nameSubscription, to, msgSub.topic.getTheme());
	fr.dyade.aaa.mom.SubscriptionClient sub;
    
	if((sub = (SubscriptionClient) subscriptionTable.get(key))!=null) {
      
	    if(Debug.debug)
		if(Debug.clientClose)
		    Debug.printSubMessage("sub retaken ", sub.queueThemeMessage); 
      
	    /* add the subscription in the current durableSubscription */
	    SessionSubscription sessionSub = null;
	    /* checks if already a durable subscription exists for the session */
	    if((sessionSub = (SessionSubscription) sessionDurableSubscriptionTable.get(msgSub.sessionID))==null) {
		/* no entry */
		sessionSub = new fr.dyade.aaa.mom.SessionSubscription(msgSub.ackMode);
		sessionSub.subSessionVector.addElement(key);
		sessionDurableSubscriptionTable.put(msgSub.sessionID, sessionSub);
	    } else {
		/* already at least 1 entry */
		sessionSub.subSessionVector.addElement(key);
	
	    }
      
	    /** add the identifier of the session to the subscription */
	    sub.setSessionID(msgSub.sessionID);
      
	    /* add the subscription to the delivery of the message  */
	    if(sessionSub.ackMode==CommonClientAAA.AUTO_ACKNOWLEDGE)
		sessionSub.addDeliveredMessage(sub, sub.queueThemeMessage.size());
      
	    /* reconnexion to the Topic after interruption of the session or Connection */
	    /* send the agreement to the client */
	    fr.dyade.aaa.mom.RequestAgreeMOMExtern msgAgree = new fr.dyade.aaa.mom.RequestAgreeMOMExtern(msgSub.getMessageMOMExternID());
	    agentClient.sendMessageMOMExtern(msgAgree);
	} else {
	    /* 	preparation of a new durable subscription, which will be effective
	     *	in reception of the agreement of the Topic
	     */
	    NotYetSubRecordObject objRecord = new NotYetSubRecordObject(msgSub.sessionID, true, msgSub.ackMode);
	    notYetSubscriptionRecordTable.put(new Long(msgSub.getMessageMOMExternID()), objRecord);
      
	    /* subscribe in the Topic */
	    fr.dyade.aaa.mom.NotificationSubscription notSub = new fr.dyade.aaa.mom.NotificationSubscription(msgSub.getMessageMOMExternID(), msgSub.nameSubscription, msgSub.topic.getTheme(), msgSub.noLocal, msgSub.selector);
	    agentClient.sendNotification(to, notSub);
	}
    }
	
    /** notification to unsusbcribe to a theme of a Topic */
    protected void notificationUnsubscription(UnsubscriptionMessageMOMExtern msgUnsub) { 
	AgentId to = AgentId.fromString(msgUnsub.topic.getTopicName());
		
	/* 	preparation of a new durable unsubscription, which will be effective
	 *	in reception of the agreement of the Topic (o know the sessionID
	 */
	NotYetSubRecordObject objRecord = new NotYetSubRecordObject(msgUnsub.sessionID, true, msgUnsub.ackMode);
	notYetSubscriptionRecordTable.put(new Long(msgUnsub.getMessageMOMExternID()), objRecord);
			
	/* remove subscription in the Topic */
	fr.dyade.aaa.mom.NotificationUnsubscription notUnsub = new fr.dyade.aaa.mom.NotificationUnsubscription(msgUnsub.getMessageMOMExternID(), msgUnsub.nameSubscription, msgUnsub.topic.getTheme());
	agentClient.sendNotification(to, notUnsub);
    }
	
    /** notification to update a susbcription to a theme of a Topic */
    protected void notificationUpdateSubscription(UpdatingSubscriptionMOMExtern msgUpdate) {
	AgentId to = AgentId.fromString(msgUpdate.topic.getTopicName());
		
	/* update subscription in the Topic */
	fr.dyade.aaa.mom.NotificationUpdateSubscription notUpdateSub = new fr.dyade.aaa.mom.NotificationUpdateSubscription(msgUpdate.getMessageMOMExternID(), msgUpdate.nameSubscription, msgUpdate.topic.getTheme(), msgUpdate.noLocal, msgUpdate.selector);
	agentClient.sendNotification(to, notUpdateSub);
    }
	
    /** notification of a no durable susbcription to a theme of a Topic */
    protected void notificationNoDurableSub(SubscriptionNoDurableMOMExtern msgSub) {
	AgentId to = AgentId.fromString(msgSub.topic.getTopicName());
		
	/* 	preparation of a new durable subscription, which will be effective
	 *	in reception of the agreement of the Topic
	 */
	NotYetSubRecordObject objRecord = new NotYetSubRecordObject(msgSub.sessionID, false, msgSub.ackMode);
	notYetSubscriptionRecordTable.put(new Long(msgSub.getMessageMOMExternID()), objRecord);
		
	/* then same behavour as a durable subscription */
	fr.dyade.aaa.mom.NotificationSubscription notSub = new fr.dyade.aaa.mom.NotificationSubscription(msgSub.getMessageMOMExternID(), msgSub.nameSubscription, msgSub.topic.getTheme(), msgSub.noLocal, msgSub.selector);
	agentClient.sendNotification(to, notSub);
    }		
				
    /** allows the delivery of the message to the (external) client  */
    protected void notificationSettingListener(SettingListenerMOMExtern msgSetMOM) throws Exception{
	try {
	    AgentId topic = AgentId.fromString(msgSetMOM.topic.getTopicName());

	    fr.dyade.aaa.mom.SubscriptionClient sub ;
	    fr.dyade.aaa.mom.KeyClientSubscription key = new fr.dyade.aaa.mom.KeyClientSubscription(msgSetMOM.nameSubscription, topic, msgSetMOM.topic.getTheme());	
		
	    /* checks if the subscription exists */
	    if((sub = (fr.dyade.aaa.mom.SubscriptionClient) subscriptionTable.get(key))==null)
		throw (new MOMException("Delivery Impossible : Subscription no exist",MOMException.SUBSCRIPTION_NO_EXIST));
		
	    /* set the MessageListener */
	    sub.setMessageListener(msgSetMOM.messageListener);
		
	    if(Debug.debug)
		if(Debug.clientSub)
		    System.out.println("settingListener "+msgSetMOM.messageListener+" OK, startmode : "+startMode);	 
		
		
	    if(startMode) {
				/* delivery the message presents in the queue if any present */
		fr.dyade.aaa.mom.Message msg = null;
		
				/* add the message in the queue of autoAck Session if any */
		String sessionID = sub.getSessionID();
		if(sessionID==null) 
		    throw(new MOMException("2 No Session corresponds to the Message delivered by the Topic",MOMException.NO_SUCH_SESSION_EXIST));
				
		SessionSubscription sessionSub = null;		
		if((sessionSub = (SessionSubscription) sessionTemporarySubscriptionTable.get(sessionID))==null) 
		    sessionSub = (SessionSubscription) sessionDurableSubscriptionTable.get(sessionID);
			
		if(sessionSub==null) 
		    throw(new MOMException("3 No Session corresponds to the Message delivered by the Topic",MOMException.NO_SUCH_SESSION_EXIST));
				
		if(sessionSub.ackMode!=AUTO_ACKNOWLEDGE) {
		    /*	delivers the message if the client arose a messageListener */
		    while((msg = sub.deliveryMessage())!=null) {
			if(Debug.debug)
			    if(Debug.clientSub && msg instanceof fr.dyade.aaa.mom.TextMessage) {
				try {
				    System.out.println(" SetListener, nameSub : "+msgSetMOM.nameSubscription);
				    System.out.println("Topic "+((Agent)agentClient).getId().toString()+": "+((fr.dyade.aaa.mom.TextMessage) msg).getText());
				} catch (Exception exc) {
				    System.err.println("notificationSettingListener"+((Agent)agentClient).getId().toString()+" "+exc);
				}
			    }
					
			fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern msgDeliver = new fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern((long) -1, msgSetMOM.nameSubscription, msg, sub.getNameTheme());
			agentClient.sendMessageMOMExtern(msgDeliver);
		    }
		} else {
		    /* delivers the message if possible */
		    if((msg = sessionSub.deliveryMessage())!=null) {
			fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern msgDeliver = new fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern((long) -1, msgSetMOM.nameSubscription, msg, sub.getNameTheme());
			agentClient.sendMessageMOMExtern(msgDeliver);
		    }
		}
	    }
			
	    /* send the agreement to the client */
	    fr.dyade.aaa.mom.RequestAgreeMOMExtern msgAgree = new fr.dyade.aaa.mom.RequestAgreeMOMExtern(msgSetMOM.getMessageMOMExternID());
	    agentClient.sendMessageMOMExtern(msgAgree);
		
	} catch (MOMException exc) {
	    /* constructs an ExceptionMessage and send to the client */
	    fr.dyade.aaa.mom.ExceptionMessageMOMExtern msgExc = new fr.dyade.aaa.mom.ExceptionMessageMOMExtern(msgSetMOM.getMessageMOMExternID(), exc);
	    agentClient.sendMessageMOMExtern(msgExc);
	} 
    }
	
    /** method to remove a message from a subscription */
    protected void notificationTopicAck(AckTopicMessageMOMExtern msgMOMAck) throws Exception {
	try {
	    AgentId topic = AgentId.fromString(msgMOMAck.topic.getTopicName());
		
	    fr.dyade.aaa.mom.SubscriptionClient sub ;
	    fr.dyade.aaa.mom.KeyClientSubscription key = new fr.dyade.aaa.mom.KeyClientSubscription(msgMOMAck.nameSubscription, topic, msgMOMAck.topic.getTheme());	
		
	    /* checks if the subscription exists */
	    if((sub = (fr.dyade.aaa.mom.SubscriptionClient) subscriptionTable.get(key))==null)
		throw (new MOMException("Ack Impossible : Subscription doesn't exist in AgentClient",MOMException.TOPIC_MESSAGEID_NO_EXIST));
		
	    /* remove 1 or more messages */
	    sub.removeMessage(msgMOMAck.messageID);	
		
		
	    if(Debug.debug)
		if(Debug.clientAck)
		    Debug.printMessageSubscription(((Agent)agentClient).getId().toString()+" ",sub.getThemeMessage());
		
		
	    if(msgMOMAck.ackMode!=CommonClientAAA.AUTO_ACKNOWLEDGE) {
				/* sends an acknowledge to the client */
				/* send the agreement to the client */
		fr.dyade.aaa.mom.RequestAgreeMOMExtern msgAgree = new fr.dyade.aaa.mom.RequestAgreeMOMExtern(msgMOMAck.getMessageMOMExternID());
		agentClient.sendMessageMOMExtern(msgAgree);		
	    } else {
				/* autoacknowledgment mode */
		String sessionID = sub.getSessionID();
		if(sessionID==null) 
		    throw(new MOMException("4 No Session corresponds to the Message delivered by the Topic",MOMException.NO_SUCH_SESSION_EXIST));
				
		SessionSubscription sessionSub = null;		
		if((sessionSub = (SessionSubscription) sessionTemporarySubscriptionTable.get(sessionID))==null) {
		    sessionSub = (SessionSubscription) sessionDurableSubscriptionTable.get(sessionID);
		}
				
				/* ack the message to the sessionSubscription in autoack */
		sessionSub.ackDeliveredMessage();
				
		if(startMode) {
		    /* add the message in the queue of autoAck Session if any */
		    fr.dyade.aaa.mom.Message msgAuto;
		    if((msgAuto = sessionSub.deliveryMessage())!=null) {
			fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern msgDeliver = new fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern((long) -1, msgMOMAck.nameSubscription, msgAuto, sub.getNameTheme());
			agentClient.sendMessageMOMExtern(msgDeliver);
		    }
		}
	    }
	} catch (MOMException exc) {
	    /* constructs an ExceptionMessage and send to the client */
	    fr.dyade.aaa.mom.ExceptionMessageMOMExtern msgExc = new fr.dyade.aaa.mom.ExceptionMessageMOMExtern(msgMOMAck.getMessageMOMExternID(), exc);
	    agentClient.sendMessageMOMExtern(msgExc);
	} 
    }
	
    /** method to remove a set of messages frome multiples Queues and Topics */
    protected void notificationMSPAck(AckMSPMessageMOMExtern msgAckMSP) throws Exception {
		
	if(Debug.debug)
	    if(Debug.clientAck)
		System.out.println("length : "+msgAckMSP.ackTab.length);
	for(int i = 0; i < msgAckMSP.ackTab.length; i++) {
	    if(msgAckMSP.ackTab[i] instanceof AckTopicMessageMOMExtern) {
		notificationTopicAck((AckTopicMessageMOMExtern) msgAckMSP.ackTab[i]);
	    } else if(msgAckMSP.ackTab[i] instanceof AckQueueMessageMOMExtern) {
		notificationQueueAck((AckQueueMessageMOMExtern) msgAckMSP.ackTab[i]);
	    }
	}
    }
	
    /** method to start or stop the delivery of message */
    protected void notificationStateListen(StateListenMessageMOMExtern msgListen) throws Exception{
	try {
	    startMode = msgListen.startMode;
			
	    /* redelivered the messages */
	    if(startMode) {
				/* messages from topic */
		Enumeration e = subscriptionTable.elements();
		Enumeration keys = subscriptionTable.keys();
		fr.dyade.aaa.mom.SubscriptionClient sub ;
		fr.dyade.aaa.mom.KeyClientSubscription key;
		fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern msgDeliver;
		fr.dyade.aaa.mom.Message msg;
				
				/* enumeration of the hashtable */
		while(e.hasMoreElements()) {
		    sub = (fr.dyade.aaa.mom.SubscriptionClient) e.nextElement();
		    key = (fr.dyade.aaa.mom.KeyClientSubscription) keys.nextElement();
				
		    /* add the message in the queue of autoAck Session if any 
		     *	sessionID can be Null, if durable sub exists (last connexion)
		     */
		    String sessionID = sub.getSessionID();
		    if(sessionID!=null) {
						
			SessionSubscription sessionSub = null;		
			if((sessionSub = (SessionSubscription) sessionTemporarySubscriptionTable.get(sessionID))==null) {
			    sessionSub = (SessionSubscription) sessionDurableSubscriptionTable.get(sessionID);
			}
				
			if(sessionSub==null) 
			    throw(new MOMException("6 No Session corresponds to the Message delivered by the Topic",MOMException.NO_SUCH_SESSION_EXIST));
				
			if(sessionSub.ackMode!=AUTO_ACKNOWLEDGE) {
			    /*  enumeration of the messages */
			    while((msg = sub.deliveryMessage())!=null) {
				msgDeliver = new fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern((long) -1, key.nameSubscription, msg, sub.getNameTheme());
				agentClient.sendMessageMOMExtern(msgDeliver);
			    }
			} else {
			    /* delivers the message if possible */
			    if((msg = sessionSub.deliveryMessage())!=null) {
				msgDeliver = new fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern((long) -1, key.nameSubscription, msg, sub.getNameTheme());
				agentClient.sendMessageMOMExtern(msgDeliver);
			    }
			}
		    }
		}
			
				/* messages from Queue */
		Enumeration eQueue = messageSynchroRecVector.elements();
		while(eQueue.hasMoreElements()) {
		    agentClient.sendMessageMOMExtern((fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern) eQueue.nextElement());
		}
		messageSynchroRecVector.removeAllElements();
	    }
		
	    /* send the agreement to the client */
	    fr.dyade.aaa.mom.RequestAgreeMOMExtern msgAgree = new fr.dyade.aaa.mom.RequestAgreeMOMExtern(msgListen.getMessageMOMExternID());
	    agentClient.sendMessageMOMExtern(msgAgree);
	} catch (MOMException exc) {
	    /* constructs an ExceptionMessage and send to the client */
	    fr.dyade.aaa.mom.ExceptionMessageMOMExtern msgExc = new fr.dyade.aaa.mom.ExceptionMessageMOMExtern(msgListen.getMessageMOMExternID(), exc);
	    agentClient.sendMessageMOMExtern(msgExc);
	} 	
    }
	
    /** create a Temporary Queue */
    protected void notificationCreationTemporaryQueue(CreationTemporaryQueueMOMExtern msgCreation) throws MOMException {
	try {
	    AgentId idAgent = null;
			
	    /* creation of a temporary Queue */
	    fr.dyade.aaa.mom.Queue agentQueue = new fr.dyade.aaa.mom.Queue();
	    idAgent = agentQueue.getId();
	    agentQueue.deploy();
			
	    /* add the Queue in the list managed by the agentClient */
	    temporaryQueueTopicVector.addElement(idAgent);
			
	    /*	send the agreement to the client */
	    fr.dyade.aaa.mom.QueueNaming queue = new fr.dyade.aaa.mom.QueueNaming(idAgent.toString());
	    fr.dyade.aaa.mom.CreationBackDestinationMOMExtern msgAgree = new fr.dyade.aaa.mom.CreationBackDestinationMOMExtern(msgCreation.getMessageMOMExternID(), queue);
	    agentClient.sendMessageMOMExtern(msgAgree);
			
	} catch (java.io.IOException exc) {
	    warningAdministrator(exc);
	    throw (new MOMException("Error during depolyement Temporary Queue",MOMException.ERROR_DURING_DEPLOYEMENT));
	}
    }
	
    /** create a Temporary Topic with theme="."  
     *	Later, it would be better to construct a Topic with another nodes
     */
    protected void notificationCreationTemporaryTopic(CreationTemporaryTopicMOMExtern msgCreation) throws MOMException {
	try {
	    AgentId idAgent = null;
			
	    /* creation of a temporary Topic */
	    fr.dyade.aaa.mom.Topic agentTopic = new fr.dyade.aaa.mom.Topic();
	    idAgent = agentTopic.getId();
	    agentTopic.deploy();
			
	    /* add the Queue or the Topic in the list managed by the agentClient */
	    temporaryQueueTopicVector.addElement(idAgent);
			
	    /*	send the agreement to the client */
	    fr.dyade.aaa.mom.TopicNaming topic = new fr.dyade.aaa.mom.TopicNaming(idAgent.toString(), ".");
	    fr.dyade.aaa.mom.CreationBackDestinationMOMExtern msgAgree = new fr.dyade.aaa.mom.CreationBackDestinationMOMExtern(msgCreation.getMessageMOMExternID(), topic);
	    agentClient.sendMessageMOMExtern(msgAgree);
			
	} catch (java.io.IOException exc) {
	    warningAdministrator(exc);
	    throw (new MOMException("Error during depolyement Temporary Topic",MOMException.ERROR_DURING_DEPLOYEMENT));
	}
    }
	
    /** close a no durable subscription */
    protected void notificationCloseSubscriber(CloseSubscriberMOMExtern msgCloseSub) throws Exception{
	AgentId to = AgentId.fromString(msgCloseSub.topic.getTopicName());
	SessionSubscription sessionSub = null;
			
	if(msgCloseSub.subDurable) {
	    sessionSub = (SessionSubscription) sessionDurableSubscriptionTable.get(msgCloseSub.sessionID);
	    if(sessionSub!=null) {
		if(sessionSub.subSessionVector!=null) {
		    fr.dyade.aaa.mom.KeyClientSubscription key = new fr.dyade.aaa.mom.KeyClientSubscription(msgCloseSub.nameSubscription, to, msgCloseSub.topic.getTheme()) ;
		    sessionSub.subSessionVector.removeElement(key);
			
		    /* stop the delivery of the message*/
		    fr.dyade.aaa.mom.SubscriptionClient sub ;
		    if((sub = (fr.dyade.aaa.mom.SubscriptionClient) subscriptionTable.get(key))!=null) {
			sub.setMessageListener(false);
						
			/* no session bound to the subscription */
			sub.setSessionID(null);
			sub.replaceRedeliveredMsg();
		    }
					
		    /* releases the ressource if any */
		    if(sessionSub.ackMode==CommonClientAAA.AUTO_ACKNOWLEDGE)
			sessionSub.removeSubFromDelivery(sub);
		}
	    }
	    /*	send the agreement to the client */
	    fr.dyade.aaa.mom.RequestAgreeMOMExtern msgAgree = new fr.dyade.aaa.mom.RequestAgreeMOMExtern(msgCloseSub.getMessageMOMExternID());
	    agentClient.sendMessageMOMExtern(msgAgree);
				
	} else {
	    /* remove the entry of the no durable subscription */
	    sessionSub = (SessionSubscription) sessionTemporarySubscriptionTable.get(msgCloseSub.sessionID);
			
	    if(sessionSub!=null) {
		if(sessionSub.subSessionVector!=null) {
		    fr.dyade.aaa.mom.KeyClientSubscription key = new fr.dyade.aaa.mom.KeyClientSubscription(msgCloseSub.nameSubscription, to, msgCloseSub.topic.getTheme()) ;
		    sessionSub.subSessionVector.removeElement(key);
					
		    /* releases the ressource if any */
		    fr.dyade.aaa.mom.SubscriptionClient sub = (fr.dyade.aaa.mom.SubscriptionClient) subscriptionTable.get(key);
		    if(sessionSub.ackMode==CommonClientAAA.AUTO_ACKNOWLEDGE)
			sessionSub.removeSubFromDelivery(sub);
		}
	    }
	    /* remove subscription in the Topic */
	    fr.dyade.aaa.mom.NotificationUnsubscription notUnsub = new fr.dyade.aaa.mom.NotificationUnsubscription(msgCloseSub.getMessageMOMExternID(), msgCloseSub.nameSubscription, msgCloseSub.topic.getTheme());
	    agentClient.sendNotification(to, notUnsub);
	}
    }
	
    /** close multiple no durable subscription */
    protected void	notificationCloseTopicSession(CloseTopicSessionMOMExtern msgCloseSub) throws Exception{
	fr.dyade.aaa.mom.KeyClientSubscription key;
	SessionSubscription sessionSub = null;
		
	/* removes the temporary subscriptions */ 
	sessionSub= (SessionSubscription) sessionTemporarySubscriptionTable.remove(msgCloseSub.sessionID);
	if(sessionSub!=null) {
	    if(sessionSub.subSessionVector!=null) {
		Vector v = sessionSub.subSessionVector;
		while(!v.isEmpty()) {
		    /* remove the entry of the no durable subscription */
		    key = (fr.dyade.aaa.mom.KeyClientSubscription) v.firstElement();
		    v.removeElementAt(0);
			
		    /* remove subscription in the Topic */
		    fr.dyade.aaa.mom.NotificationUnsubscription notUnsub = new fr.dyade.aaa.mom.NotificationUnsubscription(msgCloseSub.getMessageMOMExternID(), key.nameSubscription, key.theme);
		    agentClient.sendNotification(key.topic, notUnsub);
		}
				
				/* autoAck : removes the list of the message to deliver*/
		if(sessionSub.ackMode==CommonClientAAA.AUTO_ACKNOWLEDGE)
		    sessionSub.removeAllSubFromDelivery();
	    }
	} 
		
	/* stops the delivery of message of the durable subscriptions */
	sessionSub = (SessionSubscription) sessionDurableSubscriptionTable.remove(msgCloseSub.sessionID);
	if(sessionSub!=null) {
	    if(sessionSub.subSessionVector!=null) {
		Enumeration e = sessionSub.subSessionVector.elements();
		fr.dyade.aaa.mom.SubscriptionClient sub;
		while(e.hasMoreElements()) {
		    /* MessageListener is put to false */
		    key = (fr.dyade.aaa.mom.KeyClientSubscription) e.nextElement();
		    if((sub = (fr.dyade.aaa.mom.SubscriptionClient) subscriptionTable.get(key))!=null) {
			sub.setMessageListener(false);
						
			/* no session bound to the subscription */
			sub.setSessionID(null);
			sub.replaceRedeliveredMsg();
		    }
		}
				
				/* autoAck : removes the list of the message to deliver*/
		if(sessionSub.ackMode==CommonClientAAA.AUTO_ACKNOWLEDGE)
		    sessionSub.removeAllSubFromDelivery();
	    }
	}
		
	/*	send the agreement to the client */
	fr.dyade.aaa.mom.RequestAgreeMOMExtern msgAgree = new fr.dyade.aaa.mom.RequestAgreeMOMExtern(msgCloseSub.getMessageMOMExternID());
	agentClient.sendMessageMOMExtern(msgAgree);
    }	
	
    /** close a temporary destination */
    protected void notificationCloseDestination(CloseDestinationMOMExtern msgCloseDest) throws MOMException {
	AgentId to = AgentId.fromString(msgCloseDest.destination.getDestination());
		
	/* checks if the subscription exists */
	if(!temporaryQueueTopicVector.removeElement(to))
	    throw (new MOMException("Destruction impossible : Destination Object no exists",MOMException.NO_SUCH_TEMPORARY_DESTINATION_EXIST));
			
	/* remove subscription in the Topic */
	fr.dyade.aaa.mom.NotificationCloseDestination notClose = new fr.dyade.aaa.mom.NotificationCloseDestination(msgCloseDest.getMessageMOMExternID());
	agentClient.sendNotification(to, notClose);
		
	/*	send the agreement to the client 
	 *	agreement is sent before efficient action because after a delete, no message
	 *	can't be sent
	 */
	fr.dyade.aaa.mom.RequestAgreeMOMExtern msgAgree = new fr.dyade.aaa.mom.RequestAgreeMOMExtern(msgCloseDest.getMessageMOMExternID());
	agentClient.sendMessageMOMExtern(msgAgree);
    }
	
    /** close all the temporary destinations and the temporary subscriptions */
    protected void notificationCloseConnection() {
	try {
	    /* stop the delivery of the messages */
	    startMode = false;
	    exceptionMode = false;
	    connectMode = false;
		
	    /* close of the destinations */
	    Enumeration e = temporaryQueueTopicVector.elements();
	    while(e.hasMoreElements()) {
		AgentId to = (AgentId) e.nextElement();
				/* remove subscription in the Topic */
				/* (long) -1 has means nothing*/
		fr.dyade.aaa.mom.NotificationCloseDestination notClose = new fr.dyade.aaa.mom.NotificationCloseDestination((long) -1);
		agentClient.sendNotification(to, notClose);
	    }
	    temporaryQueueTopicVector.removeAllElements();
		
	    /* unsubscribe to the Topic the temporary subscription of the client */
	    Enumeration eTemp = sessionTemporarySubscriptionTable.elements();
	    while(eTemp.hasMoreElements()) {
		fr.dyade.aaa.mom.KeyClientSubscription key;
		SessionSubscription sessionSub = (SessionSubscription) eTemp.nextElement();
		if(sessionSub!=null) {
		    if(sessionSub.subSessionVector!=null) {
			Vector v = sessionSub.subSessionVector;
				
			while(!v.isEmpty()) {
			    /* remove the entry of the no durable subscription */
			    key = (fr.dyade.aaa.mom.KeyClientSubscription) v.firstElement();
			    v.removeElementAt(0);
			
			    /* remove subscription in the Topic */
			    fr.dyade.aaa.mom.NotificationUnsubscription notUnsub = new fr.dyade.aaa.mom.NotificationUnsubscription((long) -1, key.nameSubscription, key.theme);
			    agentClient.sendNotification(key.topic, notUnsub);
			}
						
			/* autoAck : removes the list of the message to deliver*/
			if(sessionSub.ackMode==CommonClientAAA.AUTO_ACKNOWLEDGE)
			    sessionSub.removeAllSubFromDelivery();
		    }
		}
	    }
	    sessionTemporarySubscriptionTable.clear();
		
	    /* stops the delivery of message of the durable subscriptions */
	    Enumeration eDurable = sessionDurableSubscriptionTable.elements();
	    while(eDurable.hasMoreElements()) {
		fr.dyade.aaa.mom.KeyClientSubscription key;
		SessionSubscription sessionSub = (SessionSubscription) eDurable.nextElement();
		fr.dyade.aaa.mom.SubscriptionClient sub ;
			
		if(sessionSub!=null) {
		    if(sessionSub.subSessionVector!=null) {
			Vector v = sessionSub.subSessionVector;
				
			while(!v.isEmpty()) {
			    /* MessageListener is put to false */
			    key = (fr.dyade.aaa.mom.KeyClientSubscription) v.firstElement();
			    if((sub = (fr.dyade.aaa.mom.SubscriptionClient) subscriptionTable.get(key))!=null) {
				/* put the messageListener to false */
				sub.setMessageListener(false);
								
				/* no session bound to the subscription */
				sub.setSessionID(null);
								
				/*	put the field of the Message JMSRedelivered to TRUE for messages 
				 *	delivered but not acknowledged
				 */
				sub.replaceRedeliveredMsg();
								
				if(Debug.debug)
				    if(Debug.clientClose)
					Debug.printSubMessage("Durable Sub ", sub.queueThemeMessage) ;
			    }
			    v.removeElementAt(0);
			}
						
			/* autoAck : removes the list of the message to deliver*/
			if(sessionSub.ackMode==CommonClientAAA.AUTO_ACKNOWLEDGE)
			    sessionSub.removeAllSubFromDelivery();
		    }
		}
	    }
	    sessionDurableSubscriptionTable.clear();
		
	    /* we put back the message no delivered to the client in their Queue from */
	    Enumeration eQueue = queueAgentIdAskedVector.elements();
	    fr.dyade.aaa.mom.NotificationCloseReception notClose = new fr.dyade.aaa.mom.NotificationCloseReception(-1);
	    while(eQueue.hasMoreElements()) {
		agentClient.sendNotification((AgentId)eQueue.nextElement(), notClose);
	    }
	    queueAgentIdAskedVector.removeAllElements();
			
	    /* clear the message (synchronous reception) in waiting of delivery */
	    messageSynchroRecVector.removeAllElements();
			
	} catch (Exception exc) {
	    warningAdministrator(exc);
	}
    }
	
    /** set or unset the exceptionMode chosen by the client */
    protected void notificationSettingExcListener(SettingExcListenerMOMExtern msgSetExcListener) {
	this.exceptionMode = msgSetExcListener.exceptionMode;
    }
	
    /** record the name of the Queues where the client is working */
    protected void notificationCreationWorkerQueue(CreationWorkerQueueMOMExtern msgWorkQueue) {
	Enumeration e = queueAgentIdAskedVector.elements();
	AgentId agentID = AgentId.fromString(msgWorkQueue.queue.getQueueName());
		
	/* research of presence of the name of this Queue in the vector */
	while(e.hasMoreElements()) {
	    if(agentID.equals((AgentId) e.nextElement()))
		break;
	}
		
	/* add the new agentId to the vector*/
	if(!e.hasMoreElements())
	    queueAgentIdAskedVector.addElement(agentID);
	
	/* send the agreement to the client */
	fr.dyade.aaa.mom.RequestAgreeMOMExtern msgAgree = new fr.dyade.aaa.mom.RequestAgreeMOMExtern(msgWorkQueue.getMessageMOMExternID());
	agentClient.sendMessageMOMExtern(msgAgree);
    }						
	
    /** sends a metaData object to the client */
    protected void notificationMetaData(MetaDataRequestMOMExtern msgMetaDataReq) {
	/* send the response to the client */
	fr.dyade.aaa.mom.MetaDataMOMExtern msgMetaData = new fr.dyade.aaa.mom.MetaDataMOMExtern(msgMetaDataReq.getMessageMOMExternID(), this.metaData);
	agentClient.sendMessageMOMExtern(msgMetaData);
    }
			
	
    /** changes an unknownAgent notification received to
     *	an exception ExceptionUnkonwnObjMOMExtern resent to the client
     *	 the name of the agent given by the client is incorrect
     */
    protected void notificationUnkownAgent(fr.dyade.aaa.agent.UnknownAgent not) throws Exception {
	fr.dyade.aaa.agent.UnknownAgent unknownNot = (fr.dyade.aaa.agent.UnknownAgent) not;
	fr.dyade.aaa.mom.NotificationMOMRequest notMOM = (fr.dyade.aaa.mom.NotificationMOMRequest) unknownNot.not;
				
	/* constructs the exception */
	fr.dyade.aaa.mom.MOMException exc = new fr.dyade.aaa.mom.MOMException("Incorrect name of agent Queue or Topic ",MOMException.INCORRECT_NAME_OF_AGENT);
	fr.dyade.aaa.mom.DestinationNaming dest = null;
	if (notMOM instanceof NotificationUnsubscription)
	    dest = new fr.dyade.aaa.mom.TopicNaming(unknownNot.agent.toString() ,((NotificationUnsubscription) notMOM).theme);
	else if (notMOM instanceof NotificationTopicSend)
	    dest = (fr.dyade.aaa.mom.DestinationNaming) ((NotificationTopicSend) unknownNot.not).msg.getJMSDestination();
	else if (notMOM instanceof NotificationSubscription)
	    dest = new fr.dyade.aaa.mom.TopicNaming(unknownNot.agent.toString() ,((NotificationSubscription) notMOM).theme);
	else if (notMOM instanceof NotificationReceiveSync)
	    dest = new fr.dyade.aaa.mom.QueueNaming(unknownNot.agent.toString());
	else if (notMOM instanceof NotificationReadOnly)
	    dest = new fr.dyade.aaa.mom.QueueNaming(unknownNot.agent.toString());
	else if (notMOM instanceof NotificationQueueSend)
	    dest = new fr.dyade.aaa.mom.QueueNaming(unknownNot.agent.toString());
			
	/*send the exception to the client */
	fr.dyade.aaa.mom.ExceptionUnknownObjMOMExtern excUnknownAgent= new fr.dyade.aaa.mom.ExceptionUnknownObjMOMExtern(notMOM.notMOMID, exc, dest);
	agentClient.sendMessageMOMExtern(excUnknownAgent);
    }
	
    /** set the connectionMode to true : the Connection between the client
     *	and the agentClient is begining
     */	
    public void notificationBeginingConnection() {
	connectMode = true;
    }
	
    /** notification to recover a set of messages from a session */
    protected void notificationRecover(RecoverMsgMOMExtern msgRecover) throws Exception{ 
	int i = 0;
	AgentId to ;
	if(msgRecover.rollbackTab instanceof fr.dyade.aaa.mom.RecoverQueue[]) {
	    /* message from queue */
	    fr.dyade.aaa.mom.RecoverQueue[] rollbackTab = (fr.dyade.aaa.mom.RecoverQueue[]) msgRecover.rollbackTab;
			
	    /* treatment of all the messages to rollback */
	    for(i=0;i<rollbackTab.length;i++) {
		to = AgentId.fromString(rollbackTab[i].queue.getQueueName());
		fr.dyade.aaa.mom.NotificationRecover notRec = new fr.dyade.aaa.mom.NotificationRecover(msgRecover.getMessageMOMExternID() ,rollbackTab[i].messageID);
		agentClient.sendNotification(to, notRec);
	    }
	} else {
	    /* message from Topic */
	    fr.dyade.aaa.mom.RecoverTopic[] rollbackTab = (fr.dyade.aaa.mom.RecoverTopic[]) msgRecover.rollbackTab;
				
	    fr.dyade.aaa.mom.SubscriptionClient sub ;
	    fr.dyade.aaa.mom.KeyClientSubscription key ;
	    /* treatment of all the messages to rollback */
	    for(i=0;i<rollbackTab.length;i++) {
		to = AgentId.fromString(rollbackTab[i].topic.getTopicName());
		key = new fr.dyade.aaa.mom.KeyClientSubscription(rollbackTab[i].nameSubscription, to, rollbackTab[i].topic.getTheme());
		sub = (fr.dyade.aaa.mom.SubscriptionClient) subscriptionTable.get(key);
				
				/* set the message as Redelivered */
		sub.recoverDeliveredMsg(rollbackTab[i].messageID);
	    }
			
	    /*	send the agreement to the client */
	    fr.dyade.aaa.mom.RequestAgreeMOMExtern msgAgree = new fr.dyade.aaa.mom.RequestAgreeMOMExtern(msgRecover.getMessageMOMExternID());
	    agentClient.sendMessageMOMExtern(msgAgree);
	
	    /* delivers the message to set as redelivered if possible */
	    if(startMode) {
		fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern msgDeliver;
		fr.dyade.aaa.mom.Message msg;
		for(i=0;i<rollbackTab.length;i++) {
		    to = AgentId.fromString(rollbackTab[i].topic.getTopicName());
		    key = new fr.dyade.aaa.mom.KeyClientSubscription(rollbackTab[i].nameSubscription, to, rollbackTab[i].topic.getTheme());
		    sub = (fr.dyade.aaa.mom.SubscriptionClient) subscriptionTable.get(key);
				
		    while((msg = sub.deliveryMessage())!=null) {
			msgDeliver = new fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern((long) -1, key.nameSubscription, msg, sub.getNameTheme());
			agentClient.sendMessageMOMExtern(msgDeliver);
		    }
		}
	    }
	}
    }	


    /**
     * Stores a set of messages waiting for the transaction's commit.
     */
    protected void notificationXAPrepare(MessageXAPrepare msgPrepare) throws Exception {
	if (msgPrepare.ackVector != null) xidTable.setAckToSendXid(msgPrepare.xid, msgPrepare.ackVector);
	if (msgPrepare.msgVector != null) xidTable.setMessageToSendXid(msgPrepare.xid, msgPrepare.msgVector);
	xidTable.setXidStatus(msgPrepare.xid, XidTable.PREPARED);

	// Send an ack back to the client
	agentClient.sendMessageMOMExtern(new MessageAckXAPrepare(msgPrepare.getMessageMOMExternID()));
    }


    /**
     * Perform the commit following the prepare during a XA transaction.
     */
    protected void notificationXACommit(MessageXACommit msgCommit) throws Exception {
	try {
	    Vector msgVector = xidTable.getMessageToSendXid(msgCommit.xid);
	    Vector ackVector = xidTable.getMessageToAckXid(msgCommit.xid);

	    // Send the messages
	    if (msgVector != null) {
		while (!msgVector.isEmpty()) {
		    Object msg = msgVector.remove(0);
		    if (msg instanceof SendingMessageQueueMOMExtern) {
			notificationQueueSend((SendingMessageQueueMOMExtern) msg);
		    } else if (msg instanceof SendingMessageTopicMOMExtern) {
			notificationTopicSend((SendingMessageTopicMOMExtern) msg);
		    } else {
			throw new Exception();
		    }
		}
	    }

	    // Send the acks
	    if (ackVector != null) {
		while (!ackVector.isEmpty()) {
		    Object ack = ackVector.remove(0);
		    if (ack instanceof AckQueueMessageMOMExtern) {
			notificationQueueAck((AckQueueMessageMOMExtern) ack);
		    } else if (ack instanceof AckTopicMessageMOMExtern) {
			notificationTopicAck((AckTopicMessageMOMExtern) ack);
		    } else {
			throw new Exception();
		    }
		}
	    }

	    // Remove the messages and the acks from the xid
	    xidTable.setXidStatus(msgCommit.xid, XidTable.COMMITTED);

	    // Send an ack back to the client
	    agentClient.sendMessageMOMExtern(new MessageAckXACommit(msgCommit.getMessageMOMExternID()));
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new Exception();
	}
    }


    protected void notificationXARollback(MessageXARollback msgRollback) throws Exception {
	Vector msgVector = msgRollback.msgToRollbackVector;

	if (msgVector == null) {
	    // No messages to rollback, delete the messages and put the Xid in ROLLBACKED mode
	    xidTable.removeXid(msgRollback.xid);
	    xidTable.setXidStatus(msgRollback.xid, XidTable.ROLLBACKED);
	} else {
	    // We have to push back the messages to the source to signal a rollback
	    while (!msgVector.isEmpty()) {
		Object msg = msgVector.remove(0);
		if (msg instanceof MessageRollbackMOMExtern) {
		    // Queue
		    MessageRollbackMOMExtern currentMsg = (MessageRollbackMOMExtern) msg;
		    AgentId to = AgentId.fromString(((DestinationNaming) currentMsg.getJMSDestination()).getDestination());
		    NotificationRollback not = new NotificationRollback(currentMsg.getMessageMOMExternID(), currentMsg.getJMSMessageID(),
									currentMsg.getJMSSessionID());
		    agentClient.sendNotification(to, not);
		} else if (msg instanceof AckTopicMessageMOMExtern) {
		    // Topic
		    notificationTransactedRollbackTopicAck((AckTopicMessageMOMExtern) msg);
		}
	    }
	}
	agentClient.sendMessageMOMExtern(new MessageAckXARollback(msgRollback.getMessageMOMExternID()));
    }


    protected void notificationXARecover(MessageXARecover msgRecover) throws Exception {
	agentClient.sendMessageMOMExtern(new MessageAckXARecover(msgRecover.getMessageMOMExternID(), xidTable.getXidList()));
    }


    /** incrementation of the counter with the syntax
     *	a,b,c,...,z,aa,ab,ac,...,az,ba,... 
     */
    private String calculateMessageID() {
	String MessageID;
	char[] chtmp;
	int i=0;
	int length = stringID.length();
		
	MessageID = ((Agent)agentClient).getId().toString()+"_"+stringID ;
			
	chtmp = stringID.toCharArray();
	while(i<length) {
	    if(chtmp[length-1-i]=='z') {
		chtmp[length-1-i] = 'a';
		i++;
	    }
	    else {
		chtmp[length-1-i] = (char) (chtmp[length-1-i] + 1);
		break;
	    }
	} 		
	if(i==length)
	    stringID = (new String(chtmp))+"a";
	else
	    stringID = (new String(chtmp));
		
	return MessageID;
    }
	
    /** checking the message and add the field missing */
    private void checking(fr.dyade.aaa.mom.Message msg) {
		
    } 

    /** sends a warning to the administrator of the MOM */
    protected void warningAdministrator(Exception exc) {
	System.err.println(exc);
    }
	
    /** delivers the exception to the administrator and 
     *	to the client if it has an ExceptionListener
     */
    protected void deliverAlienException(Exception exc) {
	if(exceptionMode) {
	    /* client arises an exceptionlistener */
	    fr.dyade.aaa.mom.ExceptionListenerMOMExtern msgExc = new fr.dyade.aaa.mom.ExceptionListenerMOMExtern(exc);
	    agentClient.sendMessageMOMExtern(msgExc);
	}
		
	/* warning to the administrator*/
	warningAdministrator(exc);
    }
	
}
