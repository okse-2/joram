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

public class CommonClientAAA implements java.io.Serializable
{
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
  private static long msgCounter = 0;
	
  /** vector containing all of the Temporary Queue or Topic
   *	created by the agentClient;
   */
  private Vector temporaryQueueTopicVector;

  /** if a client has an ExceptionListener */
  protected Hashtable exceptionModeTable;

  /** 
   * Hashtable holding the state of each connection of
   * this <code>AgentClient</code>.
   * <ul>
   * <li>Key: driver key.</li>
   * <li>Object: <code>Boolean</code>.</li>
   * </ul>
   */
  protected Hashtable startModeTable;

  /** 
   * Hashtable holding the state of each connection of
   * this <code>AgentClient</code>.
   * <ul>
   * <li>Key: driver key.</li>
   * <li>Object: <code>Boolean</code>.</li>
   * </ul>
   */
  protected Hashtable connectModeTable;
	
  /** the connectionMetaData whivh contians all the informations
   *	about the MOM
   */
  private fr.dyade.aaa.mom.MetaData metaData;
	
  /** 
   * Hashtable holding sessions ID per connections.
   * <ul>
   * <li>Key: driver key.</li>
   * <li>Object: <code>String</code> sessionID.</li>
   * </ul>
   */
  private Hashtable connectionSessTable;
  private Hashtable connectionConsumerKeyTable;

  /** 
   * Hashtable holding the <code>ClientSubscription</code>s
   * of the clients connected to this <code>AgentClient</code>.
   * <ul>
   * <li>Key: <code>ClientSubscriptionKey</code>.</li>
   * <li>Object: <code>ClientSubscription</code>.</li>
   * </ul>
   */
  protected Hashtable subscriptionTable;

  /** 
   * Hashtable holding <code>KeySubscription</code>s
   * per driver key - sessionID pair.
   * <ul>
   * <li>Key: driverKey_sessionID .</li>
   * <li>Object: <code>KeySubscription</code>.</li>
   * </ul>
   */
  private Hashtable sessionSubTable;
	
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
  private fr.dyade.aaa.joram.XidTable xidTable;


  public CommonClientAAA(AgentClientItf agentClient) {
    stringID = "a";
    this.agentClient = agentClient;
    subscriptionTable = new Hashtable();
    exceptionModeTable = new Hashtable();
    startModeTable = new Hashtable();
    connectModeTable = new Hashtable();
    exceptionModeTable.put(new Integer(1), new Boolean(false));
    startModeTable.put(new Integer(1), new Boolean(false));
    connectModeTable.put(new Integer(1), new Boolean(false));
    metaData = new fr.dyade.aaa.mom.MetaData();
    connectionSessTable = new Hashtable();
    connectionConsumerKeyTable = new Hashtable();
    sessionSubTable = new Hashtable();
    temporaryQueueTopicVector = new Vector();
    messageSynchroRecVector = new Vector();
    queueAgentIdAskedVector = new Vector();
    xidTable = new fr.dyade.aaa.joram.XidTable();
  }

    /** notification of exception sent by a Queue or a Topic */
    protected void reactToMOMException(AgentId from, NotificationMOMException not) {
	/* print of notification type and the exception*/
	if(Debug.debug)
	    if(Debug.clientTest)
		System.out.println("\n MOM "+((Agent)agentClient).getId().toString()+" "+not.except+" Erreur Code : "+not.except.getErrorCode());
		
	/* treatment of exception */
	if(not.typeNotification instanceof NotificationQueueSend) {
	    fr.dyade.aaa.mom.NotificationQueueSend notQueue = (NotificationQueueSend) not.typeNotification;
	    fr.dyade.aaa.mom.ExceptionSendMessageMOMExtern msgExc = new fr.dyade.aaa.mom.ExceptionSendMessageMOMExtern(notQueue.notMOMID, not.except, notQueue.msg, not.driverKey);
	    agentClient.sendMessageMOMExtern(msgExc);
	} else if(not.typeNotification instanceof NotificationTopicSend) {
	    fr.dyade.aaa.mom.NotificationTopicSend notTopic = (NotificationTopicSend) not.typeNotification;
	    fr.dyade.aaa.mom.ExceptionSendMessageMOMExtern msgExc = new fr.dyade.aaa.mom.ExceptionSendMessageMOMExtern(notTopic.notMOMID, not.except, notTopic.msg, not.driverKey);
	    agentClient.sendMessageMOMExtern(msgExc);
	} else if(not.typeNotification instanceof NotificationSubscription) {
	    fr.dyade.aaa.mom.NotificationSubscription notSub = (NotificationSubscription) not.typeNotification;
			
	    /* discard the entry in the temporary notYetSubscriptionRecordTable table */
	    //notYetSubscriptionRecordTable.remove(new Long(notSub.notMOMID));
				
	    /* constructs an ExceptionMessage and send to the client */
	    fr.dyade.aaa.mom.ExceptionMessageMOMExtern msgExc = new fr.dyade.aaa.mom.ExceptionMessageMOMExtern(notSub.notMOMID, not.except, not.driverKey);
	    agentClient.sendMessageMOMExtern(msgExc);
	} else {
	    /* constructs an ExceptionMessage and send to the client */
	    fr.dyade.aaa.mom.ExceptionMessageMOMExtern msgExc = new fr.dyade.aaa.mom.ExceptionMessageMOMExtern(not.typeNotification.notMOMID, not.except, not.driverKey);
	    agentClient.sendMessageMOMExtern(msgExc);
	}
    }
	
    /** notification which contains a message sent by a Queue */
    protected void reactToQueueMsgSending(AgentId from, NotifMessageFromQueue not) throws Exception {
     Boolean startMode = (Boolean) startModeTable.get(new Integer(not.driverKey));
     Boolean connectMode = (Boolean) connectModeTable.get(new Integer(not.driverKey));
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
	if(connectMode.booleanValue()) {
	    fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern msgQueueDeliver = new fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern(not.notMOMID, not.msg, new QueueNaming(from.toString()), not.selector, not.driverKey);		
        msgQueueDeliver.toListener = not.toListener;
	    if(startMode.booleanValue()) {
				/* constructs a QueueDeliverMessage and send to the client */
		agentClient.sendMessageMOMExtern(msgQueueDeliver);
	    } else {
				/* add the message to the vector for later delivery */
		messageSynchroRecVector.addElement(msgQueueDeliver);
	    }
	} 
    }
	
    /** notification which contains an enumeration of messages presents in a Queue */ 
    protected void reactToQueueEnumSending(AgentId from, NotifMessageEnumFromQueue not) {
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
	fr.dyade.aaa.mom.ReadDeliverMessageMOMExtern msgReadDeliver = new fr.dyade.aaa.mom.ReadDeliverMessageMOMExtern(not.notMOMID, not.messageEnumerate, not.driverKey);
	agentClient.sendMessageMOMExtern(msgReadDeliver);
    } 


  /** 
   * Method reacting to a <code>NotificationMessageFromTopic</code>
   * wrapping a message sent by a <code>Topic</code>.
   *
   * @param from  the id of the topic sender of the notification.
   * @param not  the notification sent by the topic.
   *
   * @see  AgentClient
   * @see  Topic
   *
   * @author Frederic Maistre
   */
  protected void reactToTopicMsgSending(AgentId from,
    NotifMessageFromTopic not) throws Exception
  {
    int drvKey = not.driverKey;
    fr.dyade.aaa.mom.TopicNaming topic =
      (fr.dyade.aaa.mom.TopicNaming) not.msg.getJMSDestination();

    // Retrieving the corresponding non-durable ClientSubscription.
    ClientSubscriptionKey key =
      new ClientSubscriptionKey(not.nameSubscription, drvKey, false);
    ClientSubscription sub = (ClientSubscription) subscriptionTable.get(key) ;

    // If it doesn't exist, it might be a durable ClientSubscription.
    if (sub == null) {
      key = new ClientSubscriptionKey(not.nameSubscription, drvKey, true);
      sub = (ClientSubscription) subscriptionTable.get(key) ;

      // If it still does not exist, throwing an exception.
      if (sub == null)
        throw (new MOMException("Message coming from Topic for a non existing subscription", 
          MOMException.MSG_RECEIVED_WITHOUT_SUBSCRIPTION));

      // Update the connection key in case the durable subscriber reconnected.
      drvKey = sub.getDriverKey();
    }

    // If the connection is started, as well as the message listener,
    // sending the message to the client.
    Boolean connectMode = (Boolean) connectModeTable.get(new Integer(drvKey));
    Boolean startMode = (Boolean) startModeTable.get(new Integer(drvKey));
    boolean msgListener = sub.getMessageListener();
    if (!sub.isConnectionConsumer()) {
    if (connectMode.booleanValue() && startMode.booleanValue()) {
	  sub.putMsgInClientSub(not.msg);
      if (msgListener) {
	    Message msgToSend = sub.deliveryMessage();
        if (msgToSend != null) {
          MessageTopicDeliverMOMExtern msgDeliver =
            new MessageTopicDeliverMOMExtern((long) -1, not.nameSubscription,
            msgToSend, not.theme, drvKey);

          agentClient.sendMessageMOMExtern(msgDeliver);
        }
      }
      else {
        // If no messageListener has been set, retrieving an eventual
        // synchronous request.
        Message msgToSend = sub.deliveryMessage(); 
        if (msgToSend != null) {
          SynchronousReceptionRequestMsg reqMsg = sub.getRequest();
          if (reqMsg != null &&
            (reqMsg.timeOut < 0 ||
            (reqMsg.timeOut - System.currentTimeMillis()) > 0)) {
 
            // If the request is still alive, delivering the message.
            MessageTopicDeliverMOMExtern msgDeliver =
              new MessageTopicDeliverMOMExtern(reqMsg.getMessageMOMExternID(),
              not.nameSubscription, msgToSend, not.theme, drvKey);

            msgDeliver.toListener = false;
  
            agentClient.sendMessageMOMExtern(msgDeliver);
          }
        }
      }
    }
    else if (key.durable)
	  sub.putMsgInClientSub(not.msg);

    }
    else {
    if (connectMode.booleanValue() && startMode.booleanValue()) {
      sub.putMsgInClientSub(not.msg);
	  Message msgToSend = sub.deliveryMessage();
      if (msgToSend != null) {
        MessageTopicDeliverMOMExtern msgDeliver =
          new MessageTopicDeliverMOMExtern((long) -1, not.nameSubscription,
          msgToSend, not.theme, drvKey, sub.isConnectionConsumer());

        agentClient.sendMessageMOMExtern(msgDeliver);
      }
    } 
    else if (key.durable) {
	  sub.putMsgInClientSub(not.msg);
    }
    }
  } 
	
    /** notification which holds a serious exception from a Queue or a Topic */ 
    protected void reactToException(AgentId from, ExceptionNotification not) {
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
	    fr.dyade.aaa.mom.ExceptionMessageMOMExtern msgExc = new fr.dyade.aaa.mom.ExceptionMessageMOMExtern(notAck.notMOMID, not.exc, notAck.driverKey);
	    agentClient.sendMessageMOMExtern(msgExc);
	} else {
	    //deliverAlienException(not.exc);
	}
    }
	
  /** 
   * Method processing an agreement sent by a Queue or a Topic.  
   */
  protected void reactToDestinationAcknowledgement(AgentId from, 
    NotifAckFromDestination not) throws Exception
  {
    if (not.typeNotification instanceof NotificationQueueSend) {
	  fr.dyade.aaa.mom.NotificationQueueSend notQueue = 
        (NotificationQueueSend) not.typeNotification;

      fr.dyade.aaa.mom.SendingBackMessageMOMExtern msgAgree = 
        new fr.dyade.aaa.mom.SendingBackMessageMOMExtern(notQueue.notMOMID, 
        notQueue.msg, not.driverKey);

      agentClient.sendMessageMOMExtern(msgAgree);
	} else if (not.typeNotification instanceof NotificationTopicSend) {
	    /* sending to a Topic agreement */
	    fr.dyade.aaa.mom.NotificationTopicSend notTopic = (NotificationTopicSend) not.typeNotification;
	    fr.dyade.aaa.mom.SendingBackMessageMOMExtern msgAgree = new fr.dyade.aaa.mom.SendingBackMessageMOMExtern(notTopic.notMOMID, notTopic.msg, not.driverKey);
	    agentClient.sendMessageMOMExtern(msgAgree);
	} else if (not.typeNotification instanceof NotificationAck) {
	    /* acknowledge agreement */
	    fr.dyade.aaa.mom.NotificationAck notAck = (NotificationAck) not.typeNotification;
	    fr.dyade.aaa.mom.RequestAgreeMOMExtern msgAgree = new fr.dyade.aaa.mom.RequestAgreeMOMExtern(notAck.notMOMID, notAck.driverKey);
	    agentClient.sendMessageMOMExtern(msgAgree);
    } else if (not.typeNotification instanceof NotificationSubscription) {
        treatmentSubscriptionAgreement(from, (NotificationSubscription) not.typeNotification);
	} else if (not.typeNotification instanceof NotificationUnsubscription) {
	    treatmentUnsubscriptionAgreement(from, (NotificationUnsubscription) not.typeNotification, not.driverKey);
	} else if (not.typeNotification instanceof NotificationRecover) {
	    /* acknowledge agreement */
	    fr.dyade.aaa.mom.NotificationRecover notRecover = (NotificationRecover) not.typeNotification;
	    fr.dyade.aaa.mom.RequestAgreeMOMExtern msgAgree = new fr.dyade.aaa.mom.RequestAgreeMOMExtern(notRecover.notMOMID, not.driverKey);
	    agentClient.sendMessageMOMExtern(msgAgree);
	} else if (not.typeNotification instanceof NotificationUpdateSubscription) {
	    treatmentUpdateSubscrptionAgreement(from, (NotificationUpdateSubscription) not.typeNotification);	
	} else {
	    /* would never past but costs nothing to treat */
	    deliverAlienException(new MOMException("Subclass of NotifDestinationAcknowledgementknown Unknown",MOMException.DEFAULT_MOM_ERROR), not.driverKey);
	}
    } 


  /**
   * Method processing a subscription agreement sent by a
   * <code>Topic</code> after it registered the subscription.
   * Allows the subscription to be registered by the <code>
   * AgentClient</code>.
   *
   * @param from  topic which sent the agreement.
   * @param notSub  notification wrapping the agreement.
   *
   * @author Frederic Maistre
   */
  protected void treatmentSubscriptionAgreement(AgentId from, 
    NotificationSubscription notSub)
  {
    // Building the key.
    fr.dyade.aaa.mom.ClientSubscriptionKey key =
      new ClientSubscriptionKey(notSub.nameSubscription,
      notSub.driverKey, notSub.durable);

    // If the key is already used for an existing subscription, throwing an
    // exception.
    if(subscriptionTable.containsKey(key)) {
      MOMException exc =
        new MOMException("AgentClient can't agree an already existing subscription.",
        MOMException.SUBSCRIPTION_ALREADY_EXIST);
				
      // Sending warning to the administrator (??? TO BE CHECKED).
      warningAdministrator(exc);
    }
		
    // Building and registering the subscription.
    ClientSubscription sub = new ClientSubscription(notSub.noLocal, notSub.selector,
      from, notSub.theme, notSub.driverKey, notSub.connectionConsumer);

    subscriptionTable.put(key, sub);


    if (notSub.connectionConsumer)
      connectionConsumerKeyTable.put(new Integer(notSub.driverKey), key);
    else {
      Vector keyVec = (Vector) connectionSessTable.get(new Integer(notSub.driverKey));
      if (keyVec == null)
        keyVec = new Vector();
      keyVec.addElement(notSub.sessionID);
      connectionSessTable.put(new Integer(notSub.driverKey), keyVec);

      keyVec = (Vector) sessionSubTable.get(new Integer(notSub.driverKey) + "_" +
        notSub.sessionID);
      if (keyVec == null)
        keyVec = new Vector();
      keyVec.addElement(key);
      sessionSubTable.put(new Integer(notSub.driverKey) + "_" + notSub.sessionID, keyVec);
    }
				
    // Sending an agreement to the client.
    RequestAgreeMOMExtern msgAgree = new RequestAgreeMOMExtern(notSub.notMOMID,
      notSub.driverKey);
    agentClient.sendMessageMOMExtern(msgAgree);	
  }

  /* treatment of the unsubscription agreement */
  protected void treatmentUnsubscriptionAgreement(AgentId from, NotificationUnsubscription notUnsub, int driversKey) throws Exception{
	fr.dyade.aaa.mom.ClientSubscription sub ;
	fr.dyade.aaa.mom.ClientSubscriptionKey key = new fr.dyade.aaa.mom.ClientSubscriptionKey(notUnsub.nameSubscription, driversKey, false);
			
	/* remove the entry in the hashtable of the agentClient */
	if((sub = (fr.dyade.aaa.mom.ClientSubscription) subscriptionTable.remove(key))==null) {
	    MOMException exc = new MOMException("Remove Impossible : Subscription doesn't exist in AgentClient",MOMException.SUBSCRIPTION_NO_EXIST);
				
	    /* warning sent to the administrator */
	    warningAdministrator(exc);
	}
		
	/* remove the durable subscription either in the durable table or the temporary */
	//NotYetSubRecordObject objRecord;
	//if((objRecord = (NotYetSubRecordObject) notYetSubscriptionRecordTable.remove(new Long(driversKey + notUnsub.notMOMID)))!=null) {
		
	    /*SessionSubscription sessionSub = (SessionSubscription) sessionDurableSubscriptionTable.get(driversKey + "_" + notUnsub.sessionID);
	    if(sessionSub!=null) {
				/* asynchronous mode => closeConnexion before this method (if) 
		if(sessionSub.subSessionVector!=null) {
		    sessionSub.subSessionVector.removeElement(key);
			
		    /* releases the ressource if any 
		    if(sessionSub.ackMode==CommonClientAAA.AUTO_ACKNOWLEDGE)
			sessionSub.removeSubFromDelivery(sub);
		}
	    }
	} 
    Boolean connectMode = (Boolean) connectModeTable.get(new Integer(driversKey));
	if(connectMode.booleanValue()) {
	    /* send the agreement to the client */
	    fr.dyade.aaa.mom.RequestAgreeMOMExtern msgAgree = new fr.dyade.aaa.mom.RequestAgreeMOMExtern(notUnsub.notMOMID, driversKey);
	    agentClient.sendMessageMOMExtern(msgAgree);
    }
	
    /* treatment of the updating of a subscription agreement */
    protected void treatmentUpdateSubscrptionAgreement(AgentId from, NotificationUpdateSubscription not) throws Exception {
    int drvKey = not.driverKey;
	fr.dyade.aaa.mom.ClientSubscription sub ;
	fr.dyade.aaa.mom.ClientSubscriptionKey key = 
      new fr.dyade.aaa.mom.ClientSubscriptionKey(not.nameSubscription, drvKey, true);			
			
	if((sub = (fr.dyade.aaa.mom.ClientSubscription) subscriptionTable.get(key))==null){
	    /* I don't know how it would be possible */
	    MOMException exc = new MOMException("Remove Impossible : Subscription doesn't exist in AgentClient",MOMException.SUBSCRIPTION_NO_EXIST);
				
	    /* warning sent to the administrator*/
	    warningAdministrator(exc);
	} 
			
	/* updating of the parameter in the agentClient */
	sub.updateSubscription(((Agent)agentClient).getId(), not.noLocal, not.selector);
		
	/* send the agreement to the client */
	fr.dyade.aaa.mom.RequestAgreeMOMExtern msgAgree = new fr.dyade.aaa.mom.RequestAgreeMOMExtern(not.notMOMID, drvKey);
	agentClient.sendMessageMOMExtern(msgAgree);	
    }
	
    /** treatment of the requests of the extern client */
    protected void reactToProxyNotification(ProxyNotification pNot) throws MOMException {
    NotificationInputMessage not = (NotificationInputMessage) pNot.getNotification();
    int drvKey = pNot.getDriverKey();
	try {
	    if(Debug.debug)
		if(Debug.clientTest)
		    System.out.println("CommonClient: Message Extern"+not.msgMOMExtern.getClass().getName());	 
        (not.msgMOMExtern).setDriverKey(drvKey);
		
        if (not.msgMOMExtern instanceof SynchronousReceptionRequestMsg) {
        this.reactToSynchronousReceptionRequest((SynchronousReceptionRequestMsg)
          not.msgMOMExtern);	
	    } else if (not.msgMOMExtern instanceof ReceptionMessageMOMExtern) { 
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
		deliverAlienException(new MOMException("Subclass of NotificationInputMessage Unknown",MOMException.DEFAULT_MOM_ERROR), drvKey);
	    }
	} catch (Exception exc) {
	    if(Debug.debug)
		if(Debug.clientTest)
		    System.err.println(exc);
				
	    /* send the error to the client extern */
	    fr.dyade.aaa.mom.ExceptionMessageMOMExtern msgExc = new fr.dyade.aaa.mom.ExceptionMessageMOMExtern(not.msgMOMExtern.getMessageMOMExternID(), exc, drvKey);
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


  /**
   * Method reacting to a synchronous reception request from a 
   * <code>fr.dyade.aaa.joram.TopicSubscriber</code>.
   *
   * @param reqMsg  The request.
   *
   * @author Frederic Maistre
   */
  protected void reactToSynchronousReceptionRequest(SynchronousReceptionRequestMsg reqMsg)
    throws Exception
  {
    int driverKey = reqMsg.getDriverKey();
    long requestId = reqMsg.getMessageMOMExternID();

    // Getting connection status.
    Boolean connectMode = (Boolean) connectModeTable.get(new Integer(driverKey));
    Boolean startMode = (Boolean) startModeTable.get(new Integer(driverKey));

      String subscriptionName = reqMsg.subscriptionName;
  
      // Trying to find the correspondant non durable subscription. 
      ClientSubscriptionKey subKey = new ClientSubscriptionKey(subscriptionName, 
        driverKey, false);

      ClientSubscription sub = (ClientSubscription) subscriptionTable.get(subKey);

      if (sub == null) {
        // The subscription might be durable.
        subKey = new ClientSubscriptionKey(subscriptionName, driverKey, true);
        sub = (ClientSubscription) subscriptionTable.get(subKey);
        
        if (sub == null)
          throw new MOMException("Can't find subscription corresponding to request");
      }

      if (connectMode.booleanValue() && startMode.booleanValue()) {
        Message subMessage = sub.deliveryMessage(); 
        if (subMessage != null && 
          (reqMsg.timeOut == 0 || reqMsg.timeOut < 0 ||
          (reqMsg.timeOut - System.currentTimeMillis()) > 0)) {

          // If message available in subscription and request still alive,
          // delivering the message.
          MessageTopicDeliverMOMExtern msgToSend =
            new MessageTopicDeliverMOMExtern(requestId, subscriptionName,
            subMessage, sub.getTheme(), driverKey);

          msgToSend.toListener = false;

          agentClient.sendMessageMOMExtern(msgToSend);
        }
        else if (subMessage == null) {
          if (reqMsg.timeOut == 0) {
            // If no message in subscription and request is immediate, delivering
            // a null message.
            MessageTopicDeliverMOMExtern msgToSend =
              new MessageTopicDeliverMOMExtern(requestId, subscriptionName,
              subMessage, sub.getTheme(), driverKey);

            msgToSend.toListener = false;

            agentClient.sendMessageMOMExtern(msgToSend);
          }
          else if (reqMsg.timeOut < 0 ||
            // If no message in subscription and request still alive, storing
            // it in subscription.
            (reqMsg.timeOut - System.currentTimeMillis()) > 0)

	        sub.addRequest(reqMsg);
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
	((ProxyAgent) agentClient).cleanDriverOut(msg.getDriverKey());
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
    int drvKey = msgVector.getDriverKey();

	Vector vect = msgVector.getVector();
	while(!vect.isEmpty()) {
	    Object msgInVect =  vect.firstElement();
	    if (msgInVect instanceof  SendingMessageQueueMOMExtern) {
        ((SendingMessageQueueMOMExtern) msgInVect).setDriverKey(drvKey);
		notificationQueueSend((SendingMessageQueueMOMExtern) msgInVect);
		vect.removeElementAt(0);
	    } else {
        ((SendingMessageTopicMOMExtern) msgInVect).setDriverKey(drvKey);
		notificationTopicSend((SendingMessageTopicMOMExtern) msgInVect);
		vect.removeElementAt(0);
	    }
	}
	/* Now, we can send an ACK to the client */
	MessageAckTransactedVector msgAgree = new MessageAckTransactedVector(msgVector.getMessageMOMExternID(), drvKey);
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

    int drvKey = msgVect.getDriverKey();

	Vector vect = msgVect.getVector();
	if (msgVect.isTopicRollback()) {
	    /* for Topic */
	    while (!vect.isEmpty()) {
		AckTopicMessageMOMExtern msgInVect = (AckTopicMessageMOMExtern) vect.firstElement();
        msgInVect.setDriverKey(drvKey);
		notificationTransactedRollbackTopicAck(msgInVect);
		vect.removeElementAt(0);
	    }
	} else {
	    /* for Queue */
	    while(!vect.isEmpty()) {
		MessageRollbackMOMExtern msgInVect = (MessageRollbackMOMExtern) vect.firstElement();

		DestinationNaming dest = (DestinationNaming) msgInVect.getJMSDestination();
		AgentId to = AgentId.fromString(dest.getDestination());
		NotificationRollback notMsg = new NotificationRollback(msgInVect.getMessageMOMExternID(), msgInVect.getJMSMessageID(),msgInVect.getJMSSessionID(), drvKey);
		agentClient.sendNotification(to, notMsg);
		vect.removeElementAt(0);
	    }
	}
	/* Now, we can send an ACK to the client */
	MessageAckTransactedRollback msgAck = new MessageAckTransactedRollback(msgVect.getMessageMOMExternID(), drvKey);
	agentClient.sendMessageMOMExtern(msgAck);
	  
	if (Debug.debug)
	    if (Debug.transacted)
		System.out.println("<-CommonClientAAA : notificationTransactedRollback  ACK send");
    }

    /** send all msg again to the client after a topic Transacted Rollback  */
    protected void notificationTransactedRollbackTopicAck(AckTopicMessageMOMExtern msgMOMAck) throws Exception {
    int drvKey = msgMOMAck.getDriverKey();
	try {
	    AgentId topic = AgentId.fromString(msgMOMAck.topic.getTopicName());
	    ClientSubscription sub;
	    ClientSubscriptionKey key = new ClientSubscriptionKey(msgMOMAck.nameSubscription, drvKey, true);	
      
	    /* checks if the subscription exists */
	    if((sub = (ClientSubscription) subscriptionTable.get(key))==null)
		throw (new MOMException("Impossible : Subscription doesn't exist in AgentClient",MOMException.TOPIC_MESSAGEID_NO_EXIST));
      
	    Message msg = null;
	    while((msg = sub.deliveryMessage())!=null) {
		MessageTopicDeliverMOMExtern msgDeliver = new MessageTopicDeliverMOMExtern((long) -1, msgMOMAck.nameSubscription, msg, sub.getTheme(), drvKey, sub.isConnectionConsumer());
		agentClient.sendMessageMOMExtern(msgDeliver);
	    }
	    
	} catch (MOMException exc) {
	    /* constructs an ExceptionMessage and send to the client */
	    fr.dyade.aaa.mom.ExceptionMessageMOMExtern msgExc = new fr.dyade.aaa.mom.ExceptionMessageMOMExtern(msgMOMAck.getMessageMOMExternID(), exc, drvKey);
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
		
	fr.dyade.aaa.mom.NotificationQueueSend notMsgSend = new fr.dyade.aaa.mom.NotificationQueueSend(msgSendMOMExtern.getMessageMOMExternID(), msg, msgSendMOMExtern.getDriverKey());
	agentClient.sendNotification(to, notMsgSend);
    }

    /** notification for sending a message to a Topic */
    protected void notificationTopicSend(SendingMessageTopicMOMExtern msgSendMOMExtern) throws Exception { 
	fr.dyade.aaa.mom.TopicNaming topic = (fr.dyade.aaa.mom.TopicNaming) msgSendMOMExtern.message.getJMSDestination();
	AgentId to = AgentId.fromString(topic.getTopicName());
	fr.dyade.aaa.mom.Message msg = msgSendMOMExtern.message;
    int drvKey = msgSendMOMExtern.getDriverKey();
			
	/* set of the identifier of the Message */
	msg.setJMSMessageID(calculateMessageID());
			
	/* checking the message */
	checking(msg);
		
	fr.dyade.aaa.mom.NotificationTopicSend notMsgSend = new fr.dyade.aaa.mom.NotificationTopicSend(msgSendMOMExtern.getMessageMOMExternID(), msg, drvKey);
	agentClient.sendNotification(to, notMsgSend);
    }

	
  /** Method acknowledging one or more messages in a Queue. */ 
  protected void notificationQueueAck(AckQueueMessageMOMExtern msgMOMAck) { 
    AgentId to = AgentId.fromString(msgMOMAck.queue.getQueueName());
    int drvKey = msgMOMAck.getDriverKey();

    // Sending the acknowledgement to the Queue. */
    fr.dyade.aaa.mom.NotificationAck notMsgAck = 
      new fr.dyade.aaa.mom.NotificationAck(msgMOMAck.getMessageMOMExternID(), 
      msgMOMAck.messageID, msgMOMAck.ackMode, msgMOMAck.sessionID, drvKey);
    agentClient.sendNotification(to, notMsgAck);
  }

	
    /** notification to receive an enumeration of messages presents in a Queue */ 
    protected void notificationReadOnly(ReadOnlyMessageMOMExtern msgMOMExtern) { 
	AgentId to = AgentId.fromString(msgMOMExtern.queue.getQueueName());
		
	/* send the request to the Queue */
	fr.dyade.aaa.mom.NotificationReadOnly notMsgReadOnly = new fr.dyade.aaa.mom.NotificationReadOnly(msgMOMExtern.getMessageMOMExternID(), msgMOMExtern.selector, msgMOMExtern.getDriverKey());
	agentClient.sendNotification(to, notMsgReadOnly);
    }
	
    /** notification to receive a message from a Queue */ 
    protected void notificationReceiveSync(ReceptionMessageMOMExtern msgMOMExtern) { 
	AgentId to = AgentId.fromString(msgMOMExtern.queue.getQueueName());
		
	/* send the request to the Queue */
	fr.dyade.aaa.mom.NotificationReceiveSync notRecSync = new fr.dyade.aaa.mom.NotificationReceiveSync(msgMOMExtern.getMessageMOMExternID(), msgMOMExtern.timeOut, msgMOMExtern.selector, msgMOMExtern.sessionID,  msgMOMExtern.getDriverKey()); 
    notRecSync.toListener = msgMOMExtern.toListener;
	agentClient.sendNotification(to, notRecSync);
    }


  /** 
   * Method processing a notification to subscribe durably to a Topic theme.
   */
  protected void notificationSubscription(SubscriptionMessageMOMExtern msgSub)
  {
    int drvKey = msgSub.getDriverKey();
    AgentId to = AgentId.fromString(msgSub.topic.getTopicName());

    ClientSubscriptionKey key =
      new ClientSubscriptionKey(msgSub.nameSubscription, drvKey, true);

    ClientSubscription sub = (ClientSubscription) subscriptionTable.get(key);

    if (sub != null) {
      // If the subscription already exists, just updating the sessionID and
      // driverKey parameters, and register the subscription again.
      //sub.setSessionID(msgSub.sessionID);
      sub.setDriverKey(drvKey);

      if (!msgSub.connectionConsumer) {
        Vector keyVec = (Vector) sessionSubTable.get(new Integer(drvKey) + "_" + msgSub.sessionID);
        if (keyVec == null)
          keyVec = new Vector();
        keyVec.addElement(key);
        sessionSubTable.put(new Integer(drvKey) + "_" + msgSub.sessionID, keyVec);

        keyVec = (Vector) connectionSessTable.get(new Integer(drvKey));
        if (keyVec == null)
          keyVec = new Vector();
        keyVec.addElement(msgSub.sessionID);
        connectionSessTable.put(new Integer(drvKey), keyVec);
        
        // Sending an agreement to the client request.
        RequestAgreeMOMExtern msgAgree =
          new RequestAgreeMOMExtern(msgSub.getMessageMOMExternID(), drvKey);
        agentClient.sendMessageMOMExtern(msgAgree);
      }
      else 
        connectionConsumerKeyTable.put(new Integer(drvKey), key);
    } 
    else {
      // If sub is null, the subscription is new.
      NotificationSubscription notSub = new fr.dyade.aaa.mom.NotificationSubscription(msgSub);
      agentClient.sendNotification(to, notSub);
    }
  }


	
    /** notification to unsusbcribe to a theme of a Topic */
    protected void notificationUnsubscription(UnsubscriptionMessageMOMExtern msgUnsub) { 
    int drvKey = msgUnsub.getDriverKey();
	AgentId to = AgentId.fromString(msgUnsub.topic.getTopicName());
		
	/* 	preparation of a new durable unsubscription, which will be effective
	 *	in reception of the agreement of the Topic (o know the sessionID
	 */
	//NotYetSubRecordObject objRecord = new NotYetSubRecordObject(msgUnsub.sessionID, true, msgUnsub.ackMode);
	//notYetSubscriptionRecordTable.put(new Long(msgUnsub.getMessageMOMExternID()), objRecord);
			
	/* remove subscription in the Topic */
	//fr.dyade.aaa.mom.NotificationUnsubscription notUnsub = new fr.dyade.aaa.mom.NotificationUnsubscription(msgUnsub.getMessageMOMExternID(), msgUnsub.nameSubscription, msgUnsub.topic.getTheme(), drvKey);
	fr.dyade.aaa.mom.NotificationUnsubscription notUnsub =
      new fr.dyade.aaa.mom.NotificationUnsubscription(msgUnsub);
	agentClient.sendNotification(to, notUnsub);
    }
	
    /** notification to update a susbcription to a theme of a Topic */
    protected void notificationUpdateSubscription(UpdatingSubscriptionMOMExtern msgUpdate) {
	AgentId to = AgentId.fromString(msgUpdate.topic.getTopicName());
    int drvKey = msgUpdate.getDriverKey();
		
	/* update subscription in the Topic */
	fr.dyade.aaa.mom.NotificationUpdateSubscription notUpdateSub = new fr.dyade.aaa.mom.NotificationUpdateSubscription(msgUpdate.getMessageMOMExternID(), msgUpdate.nameSubscription, msgUpdate.topic.getTheme(), msgUpdate.noLocal, msgUpdate.selector, drvKey);
	agentClient.sendNotification(to, notUpdateSub);
    }
	
 
  /** 
   * Method processing a non durable subscription to Topic theme.  
   */
  protected void notificationNoDurableSub(SubscriptionNoDurableMOMExtern msgSub) 
  {
    int drvKey = msgSub.getDriverKey();

    AgentId to = AgentId.fromString(msgSub.topic.getTopicName());

    NotificationSubscription notSub = new NotificationSubscription(msgSub);
    notSub.durable = false;

	agentClient.sendNotification(to, notSub);
  }


  /** 
   * Method reacting to a <code>SettingListenerMOMExtern</code>
   * message sent by a client setting a listener through the
   * <code>TopicSubscriber.setListener()</code> method.
   *
   * @param msgSetMOM  the message sent by a client.
   *
   * @see  AgentClient
   *
   * @author Frederic Maistre
   */
  protected void notificationSettingListener(SettingListenerMOMExtern msgSetMOM) 
    throws Exception
  {
    int drvKey = msgSetMOM.getDriverKey();
    try {
      AgentId topic = AgentId.fromString(msgSetMOM.topic.getTopicName());

      // Retrieving the corresponding non-durable ClientSubscription. 
      ClientSubscriptionKey key = new ClientSubscriptionKey(msgSetMOM.nameSubscription, drvKey, false);	
      ClientSubscription sub = (ClientSubscription) subscriptionTable.get(key);	

      // If it does not exist, it might be durable.
      if (sub == null) {
        key = new ClientSubscriptionKey(msgSetMOM.nameSubscription, drvKey, true);
        sub = (ClientSubscription) subscriptionTable.get(key);	

        if (sub == null)
          throw (new MOMException("Setting listener impossible : subscription does not exist",
            MOMException.SUBSCRIPTION_NO_EXIST));
      }

      // Setting the MessageListener.
      sub.setMessageListener(msgSetMOM.messageListener);

      // Sending request agreement to the client.
      RequestAgreeMOMExtern msgAgree =
        new RequestAgreeMOMExtern(msgSetMOM.getMessageMOMExternID(), drvKey);
	  agentClient.sendMessageMOMExtern(msgAgree);

      // Getting the connection state.
      Boolean startMode = (Boolean) startModeTable.get(new Integer(drvKey));
      Boolean connectMode = (Boolean) connectModeTable.get(new Integer(drvKey));
      boolean msgListener = sub.getMessageListener();
		
      if (startMode.booleanValue() && connectMode.booleanValue() && msgListener) {
        Message msg = null;
        while ((msg = sub.deliveryMessage()) != null) {

          MessageTopicDeliverMOMExtern msgDeliver =
            new MessageTopicDeliverMOMExtern((long) -1,
            msgSetMOM.nameSubscription, msg, sub.getTheme(), drvKey);

          agentClient.sendMessageMOMExtern(msgDeliver);
        }
      }
		
		/*String sessionID = sub.getSessionID();
		if(sessionID==null) 
		    throw(new MOMException("2 No Session corresponds to the Message delivered by the Topic",MOMException.NO_SUCH_SESSION_EXIST));
				
		SessionSubscription sessionSub = null;		
		if((sessionSub = (SessionSubscription) sessionTemporarySubscriptionTable.get(drvKey + "_" + sessionID))==null) 
		    sessionSub = (SessionSubscription) sessionDurableSubscriptionTable.get(drvKey + "_" + sessionID);
			
		if(sessionSub==null) 
		    throw(new MOMException("3 No Session corresponds to the Message delivered by the Topic",MOMException.NO_SUCH_SESSION_EXIST));
				
		if(sessionSub.ackMode!=AUTO_ACKNOWLEDGE) {
		    /*	delivers the message if the client arose a messageListener 
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
					
			fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern msgDeliver = new fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern((long) -1, msgSetMOM.nameSubscription, msg, sub.getNameTheme(), drvKey);
System.out.println("CommonClientAAA: calling sendMessageMOMExtern, 27");
			agentClient.sendMessageMOMExtern(msgDeliver);
		    }
		} else {
		    /* delivers the message if possible 
		    if((msg = sessionSub.deliveryMessage())!=null) {
			fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern msgDeliver = new fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern((long) -1, msgSetMOM.nameSubscription, msg, sub.getNameTheme(), drvKey);
System.out.println("CommonClientAAA: calling sendMessageMOMExtern, 28");
			agentClient.sendMessageMOMExtern(msgDeliver);
		    }
		}*/
		
    } catch (MOMException exc) {
      ExceptionMessageMOMExtern msgExc =
        new ExceptionMessageMOMExtern(msgSetMOM.getMessageMOMExternID(), exc, drvKey);
      agentClient.sendMessageMOMExtern(msgExc);
	} 
  }


  /**
   * Method reacting to an <code>AckTopicMessageMOMExtern</code>
   * acknowledging a message sent by a <code>Topic</code>.
   *
   * @param msgMOMAck  the acknowledgement.
   *
   * @author Frederic Maistre.
   */
  protected void notificationTopicAck(AckTopicMessageMOMExtern msgMOMAck) throws Exception
  {
    int drvKey = msgMOMAck.getDriverKey();
    try {
      AgentId topic = AgentId.fromString(msgMOMAck.topic.getTopicName());

      // Retrieving the corresponding non durable subscription.
      ClientSubscriptionKey key = new ClientSubscriptionKey(msgMOMAck.nameSubscription,
        drvKey, false);	
      ClientSubscription sub = (ClientSubscription) subscriptionTable.get(key);

      // If it does not exist, it might be durable. 
      if (sub == null) {
        key = new ClientSubscriptionKey(msgMOMAck.nameSubscription,
          drvKey, true);
        sub = (ClientSubscription) subscriptionTable.get(key);

        if (sub == null)
          throw (new MOMException("notificationTopicAck: subscription does not exist",
            MOMException.TOPIC_MESSAGEID_NO_EXIST));
      }
      sub.removeMessage(msgMOMAck.messageID);	

      // Acknowledging the client's request.		
      RequestAgreeMOMExtern msgAgree =
        new RequestAgreeMOMExtern(msgMOMAck.getMessageMOMExternID(), drvKey);
      agentClient.sendMessageMOMExtern(msgAgree);		

    } catch (MOMException exc) {
        ExceptionMessageMOMExtern msgExc =
          new ExceptionMessageMOMExtern(msgMOMAck.getMessageMOMExternID(), exc, drvKey);
        agentClient.sendMessageMOMExtern(msgExc);
    } 
  }


  /**
   * Method reacting to an <code>AckMSPMessageMOMExtern</code> sent
   * by a client to acknowledge a group of messages.
   *
   * @param msgAckMSP  the multiple acknowledgement.
   *
   * @author Nicolas Tachker
   */
  protected void notificationMSPAck(AckMSPMessageMOMExtern msgAckMSP) throws Exception
  {

    for(int i = 0; i < msgAckMSP.ackTab.length; i++) {

      // Acknowledging messages sent by a topic.
      if(msgAckMSP.ackTab[i] instanceof AckTopicMessageMOMExtern) {
        // Setting the driverKey parameter of each acknowledgement. 
        (msgAckMSP.ackTab[i]).setDriverKey(msgAckMSP.getDriverKey());
        // Acknowledging messages one by one.
        notificationTopicAck((AckTopicMessageMOMExtern) msgAckMSP.ackTab[i]);

      // Acknowledging messages sent by a .
      } else if(msgAckMSP.ackTab[i] instanceof AckQueueMessageMOMExtern) {
        // Setting the driverKey parameter of each acknowledgement. 
        (msgAckMSP.ackTab[i]).setDriverKey(msgAckMSP.getDriverKey());
        // Acknowledging messages one by one.
        notificationQueueAck((AckQueueMessageMOMExtern) msgAckMSP.ackTab[i]);
      }
    }
  }


  /** 
   * Method reacting to a <code>StateListenMessageMOMExtern</code>
   * message sent by a client starting a connection.
   *
   * @param msgListen  the start message sent by a client.
   *
   * @see  AgentClient
   *
   * @author Frederic Maistre
   */
  protected void notificationStateListen(StateListenMessageMOMExtern msgListen)
    throws Exception
  {
    int drvKey = msgListen.getDriverKey();

    try {
      Boolean startMode = new Boolean(msgListen.startMode);
      startModeTable.put(new Integer(drvKey), startMode);

      // Sending the request agreement to the client.
      RequestAgreeMOMExtern msgAgree =
        new RequestAgreeMOMExtern(msgListen.getMessageMOMExternID(), drvKey);
      agentClient.sendMessageMOMExtern(msgAgree);

      Boolean connectMode = (Boolean) connectModeTable.get(new Integer(drvKey));

      if(connectMode.booleanValue() && msgListen.startMode) { 
        ClientSubscriptionKey subKey;
        ClientSubscription clientSub ;
        MessageTopicDeliverMOMExtern msgDeliver;
        Message msg;

        // Getting the vector of ClientSubscriptionKeys for this driverKey.
        Vector sessionVec = (Vector) connectionSessTable.get(new Integer(drvKey));
        if (sessionVec != null) {
        for (int i = 0; i < sessionVec.size(); i++) {
          String sessionID = (String) sessionVec.elementAt(i);
          Vector subVec = (Vector) sessionSubTable.get(new Integer(drvKey) + "_" + sessionID);
          if (subVec != null) {
          for (int j = 0; j < subVec.size(); j++) {
            subKey = (ClientSubscriptionKey) subVec.elementAt(j);
            clientSub = (ClientSubscription) subscriptionTable.get(subKey);

            // Delivering the messages if any, and if there is a message
            // listener for this subscription.
            if (clientSub != null && clientSub.getMessageListener()) {
              while ((msg = clientSub.deliveryMessage()) != null) {
                msgDeliver = new MessageTopicDeliverMOMExtern((long) -1,
                  subKey.subscriptionName, msg, clientSub.getTheme(),
                  clientSub.getDriverKey());

                agentClient.sendMessageMOMExtern(msgDeliver);
              }
            }
          }}
        }}
 
        subKey = (ClientSubscriptionKey) connectionConsumerKeyTable.get(new Integer(drvKey));
        if (subKey != null) {
          clientSub = (ClientSubscription) subscriptionTable.get(subKey);
          if (clientSub != null) {
            while ((msg = clientSub.deliveryMessage()) != null) {
              msgDeliver = new MessageTopicDeliverMOMExtern((long) -1, subKey.subscriptionName,
                msg, clientSub.getTheme(), clientSub.getDriverKey(),
                clientSub.isConnectionConsumer());

              agentClient.sendMessageMOMExtern(msgDeliver);
            }
          } 
        }


		    /* add the message in the queue of autoAck Session if any 
		     *	sessionID can be Null, if durable sub exists (last connexion)
		    String sessionID = sub.getSessionID();
		    if(sessionID!=null) {
						
			SessionSubscription sessionSub = null;		
			if((sessionSub = (SessionSubscription) sessionTemporarySubscriptionTable.get(sub.getDriverKey() + "_" + sessionID))==null) {
			    sessionSub = (SessionSubscription) sessionDurableSubscriptionTable.get(sub.getDriverKey() + "_" + sessionID);
			}
				
			if(sessionSub==null) 
			    throw(new MOMException("6 No Session corresponds to the Message delivered by the Topic",MOMException.NO_SUCH_SESSION_EXIST));
				
			if(sessionSub.ackMode!=AUTO_ACKNOWLEDGE) {
			    /*  enumeration of the messages 
               while((msg = sub.deliveryMessage())!=null) {
				msgDeliver = new fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern((long) -1, key.nameSubscription, msg, sub.getNameTheme(), sub.getDriverKey());
               System.out.println("CommonClientAAA: calling sendMessageMOMExtern, 34");
				agentClient.sendMessageMOMExtern(msgDeliver);
			    }
			} else {
                System.out.println("SessionSub: " + sessionSub);
			    /* delivers the message if possible 
			    if((msg = sessionSub.deliveryMessage())!=null) {
            System.out.println("----------> Sub name: " + key.nameSubscription);
				msgDeliver = new fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern((long) -1, key.nameSubscription, msg, sub.getNameTheme(), sub.getDriverKey());
System.out.println("CommonClientAAA: calling sendMessageMOMExtern, 35");
				agentClient.sendMessageMOMExtern(msgDeliver);
			    }
			}
		    }
		}*/

        // 
        Enumeration qMessages = messageSynchroRecVector.elements();
        if (qMessages != null) {
        while (qMessages.hasMoreElements())
          agentClient.sendMessageMOMExtern((MessageQueueDeliverMOMExtern) qMessages.nextElement());
        messageSynchroRecVector.removeAllElements();
        }
      }
    } catch (MOMException exc) {
      ExceptionMessageMOMExtern msgExc =
        new ExceptionMessageMOMExtern(msgListen.getMessageMOMExternID(), exc, drvKey);
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
	    fr.dyade.aaa.mom.CreationBackDestinationMOMExtern msgAgree = new fr.dyade.aaa.mom.CreationBackDestinationMOMExtern(msgCreation.getMessageMOMExternID(), queue, msgCreation.getDriverKey());
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
	    fr.dyade.aaa.mom.CreationBackDestinationMOMExtern msgAgree = new fr.dyade.aaa.mom.CreationBackDestinationMOMExtern(msgCreation.getMessageMOMExternID(), topic, msgCreation.getDriverKey());
	    agentClient.sendMessageMOMExtern(msgAgree);
			
	} catch (java.io.IOException exc) {
	    warningAdministrator(exc);
	    throw (new MOMException("Error during depolyement Temporary Topic",MOMException.ERROR_DURING_DEPLOYEMENT));
	}
    }
	
    /** close a no durable subscription */
    protected void notificationCloseSubscriber(CloseSubscriberMOMExtern msgCloseSub) throws Exception{
	AgentId to = AgentId.fromString(msgCloseSub.topic.getTopicName());
	//SessionSubscription sessionSub = null;
    int drvKey = msgCloseSub.getDriverKey();
			
	if(msgCloseSub.subDurable) {
	    /*sessionSub = (SessionSubscription) sessionDurableSubscriptionTable.get(drvKey + "_" + msgCloseSub.sessionID);
	    if(sessionSub!=null) {
		if(sessionSub.subSessionVector!=null) {*/
    ClientSubscriptionKey key = new ClientSubscriptionKey(msgCloseSub.nameSubscription, drvKey, true) ;
		    //sessionSub.subSessionVector.removeElement(key);
			
		    /* stop the delivery of the message*/
		    fr.dyade.aaa.mom.ClientSubscription sub ;
		    if((sub = (fr.dyade.aaa.mom.ClientSubscription) subscriptionTable.get(key))!=null) {
			sub.setMessageListener(false);
						
			/* no session bound to the subscription */
			//sub.setSessionID(null);
			sub.putBackNonAckMessages();
		    }
					
		    /* releases the ressource if any 
		    if(sessionSub.ackMode==CommonClientAAA.AUTO_ACKNOWLEDGE)
			sessionSub.removeSubFromDelivery(sub);
		}*/
	    /*	send the agreement to the client */
	    fr.dyade.aaa.mom.RequestAgreeMOMExtern msgAgree = new fr.dyade.aaa.mom.RequestAgreeMOMExtern(msgCloseSub.getMessageMOMExternID(), drvKey);
	    agentClient.sendMessageMOMExtern(msgAgree);
				
	} else {
	    /* remove the entry of the no durable subscription 
	    sessionSub = (SessionSubscription) sessionTemporarySubscriptionTable.get(drvKey + msgCloseSub.sessionID);
			
	    if(sessionSub!=null) {
		if(sessionSub.subSessionVector!=null) {*/
		    fr.dyade.aaa.mom.ClientSubscriptionKey key = new fr.dyade.aaa.mom.ClientSubscriptionKey(msgCloseSub.nameSubscription, drvKey, true);
//		    sessionSub.subSessionVector.removeElement(key);
					
		    /* releases the ressource if any */
		    fr.dyade.aaa.mom.ClientSubscription sub = (fr.dyade.aaa.mom.ClientSubscription) subscriptionTable.get(key);
/*		    if(sessionSub.ackMode==CommonClientAAA.AUTO_ACKNOWLEDGE)
			sessionSub.removeSubFromDelivery(sub);*/
	    }
	    /* remove subscription in the Topic */
	    fr.dyade.aaa.mom.NotificationUnsubscription notUnsub = new fr.dyade.aaa.mom.NotificationUnsubscription(msgCloseSub.getMessageMOMExternID(), msgCloseSub.nameSubscription, msgCloseSub.topic.getTheme(), drvKey);
	    agentClient.sendNotification(to, notUnsub);
    }

    
	
  protected void notificationCloseTopicSession(CloseTopicSessionMOMExtern msgCloseSub)
    throws Exception
  {
    int drvKey = msgCloseSub.getDriverKey();
    String sessionID = msgCloseSub.sessionID;

    Vector sessionSubKeys = (Vector) sessionSubTable.remove(new Integer(drvKey) + "_" + sessionID);

    if (sessionSubKeys != null) {
    while (sessionSubKeys.size() > 0) {
      ClientSubscriptionKey subKey = (ClientSubscriptionKey) sessionSubKeys.remove(0);

      // Removing the subscription from the table and in the topic if it is not durable.
      if (!subKey.durable) {
        ClientSubscription clientSub = (ClientSubscription) subscriptionTable.get(subKey);
        NotificationUnsubscription notUnsub =
          new NotificationUnsubscription(msgCloseSub.getMessageMOMExternID(),
          subKey.subscriptionName, clientSub.getTheme(), drvKey);

        agentClient.sendNotification(clientSub.getTopicID(), notUnsub);
      }
      // Stoping delivery of messages if the subscription is durable.
      else {
        ClientSubscription clientSub = (ClientSubscription) subscriptionTable.get(subKey);
        clientSub.setMessageListener(false);
        //clientSub.setSessionID(null);
        clientSub.putBackNonAckMessages();
      }
    }}

/*
fr.dyade.aaa.mom.ClientSubscriptionKey key;
SessionSubscription sessionSub = null;
		
	/* removes the temporary subscriptions 
   
	sessionSub= (SessionSubscription) sessionTemporarySubscriptionTable.remove(drvKey + "_" + msgCloseSub.sessionID);
	if(sessionSub!=null) {
    System.out.println("---------------------------------"); 
    System.out.println("Removing sessionSub from sessionTempSubscriptionTable, key " + (drvKey + "_" + msgCloseSub.sessionID));
    System.out.println("---------------------------------"); 
	    if(sessionSub.subSessionVector!=null) {
		Vector v = sessionSub.subSessionVector;
		while(!v.isEmpty()) {
		    /* remove the entry of the no durable subscription 
		    key = (fr.dyade.aaa.mom.ClientSubscriptionKey) v.firstElement();
		    v.removeElementAt(0);
			
		    /* remove subscription in the Topic 
		    fr.dyade.aaa.mom.NotificationUnsubscription notUnsub = new fr.dyade.aaa.mom.NotificationUnsubscription(msgCloseSub.getMessageMOMExternID(), key.nameSubscription, key.theme, drvKey);
    System.out.println("CommonClientAAA: send notification, 15");
		    agentClient.sendNotification(key.topic, notUnsub);
		}
				
				/* autoAck : removes the list of the message to deliver
		if(sessionSub.ackMode==CommonClientAAA.AUTO_ACKNOWLEDGE)
		    sessionSub.removeAllSubFromDelivery();
	    }
	} 
		
	/* stops the delivery of message of the durable subscriptions 
	sessionSub = (SessionSubscription) sessionDurableSubscriptionTable.remove(drvKey + "_" + msgCloseSub.sessionID);
	if(sessionSub!=null) {
        System.out.println("CommonClientAAA.closeTopicSession: removing sessionSub from table with key " + (msgCloseSub.sessionID));
	    if(sessionSub.subSessionVector!=null) {
		Enumeration e = sessionSub.subSessionVector.elements();
		fr.dyade.aaa.mom.ClientSubscription sub;
		while(e.hasMoreElements()) {
		    /* MessageListener is put to false 
		    key = (fr.dyade.aaa.mom.ClientSubscriptionKey) e.nextElement();
		    if((sub = (fr.dyade.aaa.mom.ClientSubscription) subscriptionTable.get(key))!=null) {
			sub.setMessageListener(false);
						
			/* no session bound to the subscription 
			sub.setSessionID(null);
			sub.putBackNonAckMessages();
		    }
		}
				
				/* autoAck : removes the list of the message to deliver
		if(sessionSub.ackMode==CommonClientAAA.AUTO_ACKNOWLEDGE)
		    sessionSub.removeAllSubFromDelivery();
	    }
	}*/
		
    // Sending an agreement to the client's request.
    RequestAgreeMOMExtern msgAgree =
      new RequestAgreeMOMExtern(msgCloseSub.getMessageMOMExternID(), drvKey);
    agentClient.sendMessageMOMExtern(msgAgree);
  }	


	
    /** close a temporary destination */
    protected void notificationCloseDestination(CloseDestinationMOMExtern msgCloseDest) throws MOMException {
    int drvKey = msgCloseDest.getDriverKey();
	AgentId to = AgentId.fromString(msgCloseDest.destination.getDestination());
		
	/* checks if the subscription exists */
	if(!temporaryQueueTopicVector.removeElement(to))
	    throw (new MOMException("Destruction impossible : Destination Object no exists",MOMException.NO_SUCH_TEMPORARY_DESTINATION_EXIST));
			
	/* remove subscription in the Topic */
	fr.dyade.aaa.mom.NotificationCloseDestination notClose = new fr.dyade.aaa.mom.NotificationCloseDestination(msgCloseDest.getMessageMOMExternID(), drvKey);
	agentClient.sendNotification(to, notClose);
		
	/*	send the agreement to the client 
	 *	agreement is sent before efficient action because after a delete, no message
	 *	can't be sent
	 */
	fr.dyade.aaa.mom.RequestAgreeMOMExtern msgAgree = new fr.dyade.aaa.mom.RequestAgreeMOMExtern(msgCloseDest.getMessageMOMExternID(), drvKey);
	agentClient.sendMessageMOMExtern(msgAgree);
    }
	

    /** close all the temporary destinations and the temporary subscriptions */
    protected void reactToClosingConnection(int driversKey) {
	try {
	    /* stop the delivery of the messages */
	    startModeTable.put(new Integer(driversKey), new Boolean(false));
	    exceptionModeTable.put(new Integer(driversKey), new Boolean(false));
	    connectModeTable.put(new Integer(driversKey), new Boolean(false));
		
	    /* close of the destinations */
	    Enumeration e = temporaryQueueTopicVector.elements();
	    while(e.hasMoreElements()) {
		AgentId to = (AgentId) e.nextElement();
				/* remove subscription in the Topic */
				/* (long) -1 has means nothing*/
		fr.dyade.aaa.mom.NotificationCloseDestination notClose = new fr.dyade.aaa.mom.NotificationCloseDestination((long) 0, driversKey);
		agentClient.sendNotification(to, notClose);
	    }
	    temporaryQueueTopicVector.removeAllElements();


    Vector sessions = (Vector) connectionSessTable.remove(new Integer(driversKey));
    if (sessions != null) {
    while (sessions.size() > 0) {
      String sessionID = (String) sessions.remove(0);
      Vector subVec = (Vector) sessionSubTable.remove(new Integer(driversKey) + "_" + sessionID);
      
      if (subVec != null) {
        while (subVec.size() > 0) {
          ClientSubscriptionKey subKey = (ClientSubscriptionKey) subVec.remove(0);
      
          // Removing the subscription from the table and in the topic if it is not durable.
          if (!subKey.durable) {
            ClientSubscription clientSub = (ClientSubscription) subscriptionTable.get(subKey);
            NotificationUnsubscription notUnsub =
              new NotificationUnsubscription((long) -1,
              subKey.subscriptionName, clientSub.getTheme(), driversKey);

            agentClient.sendNotification(clientSub.getTopicID(), notUnsub);
          }
          // Stoping delivery of messages if the subscription is durable.
          else {
            ClientSubscription clientSub = (ClientSubscription) subscriptionTable.get(subKey);
            clientSub.setMessageListener(false);
            //clientSub.setSessionID(null);
            clientSub.putBackNonAckMessages();
          }
        }
      }
    }}

    ClientSubscriptionKey subKey =
      (ClientSubscriptionKey) connectionConsumerKeyTable.remove(new Integer(driversKey));

    if (subKey != null) {
      ClientSubscription clientSub = (ClientSubscription) subscriptionTable.get(subKey);
      if (clientSub != null) {
        if (!subKey.durable) {
          NotificationUnsubscription notUnsub = new NotificationUnsubscription((long) -1,
            subKey.subscriptionName, clientSub.getTheme(), driversKey);
    
          agentClient.sendNotification(clientSub.getTopicID(), notUnsub);
        }
        else
          clientSub.putBackNonAckMessages();
      }
    }



	    /* unsubscribe to the Topic the temporary subscription of the client 
        Enumeration eTemp = sessionTemporarySubscriptionTable.keys();
	    //Enumeration eTemp = sessionTemporarySubscriptionTable.elements();
	    while(eTemp.hasMoreElements()) {
        String sKey = (String) eTemp.nextElement();
		fr.dyade.aaa.mom.ClientSubscriptionKey key;
		//SessionSubscription sessionSub = (SessionSubscription) eTemp.nextElement();
		SessionSubscription sessionSub = (SessionSubscription) sessionTemporarySubscriptionTable.get(sKey);
		if(sessionSub!=null) {
		    if(sessionSub.subSessionVector!=null) {
			Vector v = sessionSub.subSessionVector;

	            //while(!v.isEmpty()) {
                for (int i = 0; i < v.size(); i++) {
			    /* remove the entry of the no durable subscription 
			    //key = (fr.dyade.aaa.mom.ClientSubscriptionKey) v.firstElement();
			    key = (fr.dyade.aaa.mom.ClientSubscriptionKey) v.get(i);
              
                if (key.driversKey == driversKey) { 
			      //v.removeElementAt(0);
                  sessionTemporarySubscriptionTable.remove(sKey);
			      v.removeElementAt(i);
                  i--;
    			    /* remove subscription in the Topic 
    			    fr.dyade.aaa.mom.NotificationUnsubscription notUnsub = new fr.dyade.aaa.mom.NotificationUnsubscription((long) -1, key.nameSubscription, key.theme, driversKey);
                    System.out.println("CommonClientAAA: send notification, 2");
     			    agentClient.sendNotification(key.topic, notUnsub);
	      		}
						
		    	/* autoAck : removes the list of the message to deliver
	    		if(sessionSub.ackMode==CommonClientAAA.AUTO_ACKNOWLEDGE)
    			    sessionSub.removeAllSubFromDelivery();
    		    }
          }
	    }
        } 
	    //sessionTemporarySubscriptionTable.clear();
		
	    /* stops the delivery of message of the durable subscriptions 
	    Enumeration eDurable = sessionDurableSubscriptionTable.elements();
	    //Enumeration eDurable = sessionDurableSubscriptionTable.keys();
	    while(eDurable.hasMoreElements()) {
		fr.dyade.aaa.mom.ClientSubscriptionKey key;
        //String sKey = (String) eDurable.nextElement();
		SessionSubscription sessionSub = (SessionSubscription) eDurable.nextElement();
		//SessionSubscription sessionSub = (SessionSubscription) sessionDurableSubscriptionTable.get(sKey) ;
		fr.dyade.aaa.mom.ClientSubscription sub ;
			
		if(sessionSub!=null) {
		    if(sessionSub.subSessionVector!=null) {
			Vector v = sessionSub.subSessionVector;
				
			//while(!v.isEmpty()) {
            for (int i = 0; i < v.size(); i++) {
			    /* MessageListener is put to false */
			    /*key = (fr.dyade.aaa.mom.ClientSubscriptionKey) v.firstElement();
			    key = (fr.dyade.aaa.mom.ClientSubscriptionKey) v.get(i);
			    if((sub = (fr.dyade.aaa.mom.ClientSubscription) subscriptionTable.get(key))!=null
                  && key.driversKey == driversKey ) {
				/* put the messageListener to false 
				sub.setMessageListener(false);
								
				/* no session bound to the subscription 
				sub.setSessionID(null);
								
				/*	put the field of the Message JMSRedelivered to TRUE for messages 
				 *	delivered but not acknowledged
				 
				sub.putBackNonAckMessages();
								
				if(Debug.debug)
				    if(Debug.clientClose)
					Debug.printSubMessage("Durable Sub ", sub.queueThemeMessage) ;
			    }
			    //v.removeElementAt(0);
			    v.removeElementAt(i);
                //sessionDurableSubscriptionTable.remove(sKey);
                sessionDurableSubscriptionTable.remove(driversKey + "_" + sub.getSessionID());

			/* autoAck : removes the list of the message to deliver 
			if(sessionSub.ackMode==CommonClientAAA.AUTO_ACKNOWLEDGE)
			    sessionSub.removeSubFromDelivery(sub);
		    }
			}
						
			/* autoAck : removes the list of the message to deliver
			if(sessionSub.ackMode==CommonClientAAA.AUTO_ACKNOWLEDGE)
			    sessionSub.removeAllSubFromDelivery();
		    }
		}
	    }
	    //sessionDurableSubscriptionTable.clear();
        */
		
	    // we put back the message no delivered to the client in their Queue from 
	    Enumeration eQueue = queueAgentIdAskedVector.elements();
	    fr.dyade.aaa.mom.NotificationCloseReception notClose = new fr.dyade.aaa.mom.NotificationCloseReception(-1, driversKey);
	    while(eQueue.hasMoreElements()) {
		agentClient.sendNotification((AgentId)eQueue.nextElement(), notClose);
	    }
	    //queueAgentIdAskedVector.removeAllElements();
			
	    // clear the message (synchronous reception) in waiting of delivery 
	    //messageSynchroRecVector.removeAllElements();
	} catch (Exception exc) {
	    warningAdministrator(exc);
	}
    }
	
    /** set or unset the exceptionMode chosen by the client */
    protected void notificationSettingExcListener(SettingExcListenerMOMExtern msgSetExcListener) {
      int drvKey = msgSetExcListener.getDriverKey();
	  exceptionModeTable.put(new Integer(drvKey),  new Boolean(msgSetExcListener.exceptionMode));
    }
	
    /** record the name of the Queues where the client is working */
    protected void notificationCreationWorkerQueue(CreationWorkerQueueMOMExtern msgWorkQueue) {
	Enumeration e = queueAgentIdAskedVector.elements();
	AgentId agentID = AgentId.fromString(msgWorkQueue.queue.getQueueName());
    int drvKey = msgWorkQueue.getDriverKey();
		
	/* research of presence of the name of this Queue in the vector */
	while(e.hasMoreElements()) {
	    if(agentID.equals((AgentId) e.nextElement()))
		break;
	}
		
	/* add the new agentId to the vector*/
	if(!e.hasMoreElements())
	    queueAgentIdAskedVector.addElement(agentID);
	
	/* send the agreement to the client */
	fr.dyade.aaa.mom.RequestAgreeMOMExtern msgAgree = new fr.dyade.aaa.mom.RequestAgreeMOMExtern(msgWorkQueue.getMessageMOMExternID(), drvKey);
	agentClient.sendMessageMOMExtern(msgAgree);
    }						
	
    /** sends a metaData object to the client */
    protected void notificationMetaData(MetaDataRequestMOMExtern msgMetaDataReq) {
	/* send the response to the client */
	fr.dyade.aaa.mom.MetaDataMOMExtern msgMetaData = new fr.dyade.aaa.mom.MetaDataMOMExtern(msgMetaDataReq.getMessageMOMExternID(), this.metaData, msgMetaDataReq.getDriverKey());
	agentClient.sendMessageMOMExtern(msgMetaData);
    }
			
	
    /** changes an unknownAgent notification received to
     *	an exception ExceptionUnkonwnObjMOMExtern resent to the client
     *	 the name of the agent given by the client is incorrect
     */
    protected void reactToUnknownAgentExcept(fr.dyade.aaa.agent.UnknownAgent not) throws Exception {
	fr.dyade.aaa.agent.UnknownAgent unknownNot = (fr.dyade.aaa.agent.UnknownAgent) not;
	fr.dyade.aaa.mom.NotificationMOMRequest notMOM = (fr.dyade.aaa.mom.NotificationMOMRequest) unknownNot.not;
    int drvKey = notMOM.driverKey;
				
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
	fr.dyade.aaa.mom.ExceptionUnknownObjMOMExtern excUnknownAgent= new fr.dyade.aaa.mom.ExceptionUnknownObjMOMExtern(notMOM.notMOMID, exc, dest, drvKey);
	agentClient.sendMessageMOMExtern(excUnknownAgent);
    }
	
    /** set the connectionMode to true : the Connection between the client
     *	and the agentClient is begining
     */	
    public void reactToOpeningConnection(int drvKey) {
	connectModeTable.put(new Integer(drvKey), new Boolean(true));
	exceptionModeTable.put(new Integer(drvKey), new Boolean(false));
	startModeTable.put(new Integer(drvKey), new Boolean(false));
    }
	
    /** notification to recover a set of messages from a session */
    protected void notificationRecover(RecoverMsgMOMExtern msgRecover) throws Exception{ 
	int i = 0;
    int drvKey = msgRecover.getDriverKey();
	AgentId to ;
	if(msgRecover.rollbackTab instanceof fr.dyade.aaa.mom.RecoverQueue[]) {
	    /* message from queue */
	    fr.dyade.aaa.mom.RecoverQueue[] rollbackTab = (fr.dyade.aaa.mom.RecoverQueue[]) msgRecover.rollbackTab;
			
	    /* treatment of all the messages to rollback */
	    for(i=0;i<rollbackTab.length;i++) {
		to = AgentId.fromString(rollbackTab[i].queue.getQueueName());
		fr.dyade.aaa.mom.NotificationRecover notRec = new fr.dyade.aaa.mom.NotificationRecover(msgRecover.getMessageMOMExternID() ,rollbackTab[i].messageID, drvKey);
		agentClient.sendNotification(to, notRec);
	    }
	} else {
	    /* message from Topic */
	    fr.dyade.aaa.mom.RecoverTopic[] rollbackTab = (fr.dyade.aaa.mom.RecoverTopic[]) msgRecover.rollbackTab;
				
	    fr.dyade.aaa.mom.ClientSubscription sub ;
	    fr.dyade.aaa.mom.ClientSubscriptionKey key ;
	    /* treatment of all the messages to rollback */
	    for(i=0;i<rollbackTab.length;i++) {
		to = AgentId.fromString(rollbackTab[i].topic.getTopicName());
		key = new fr.dyade.aaa.mom.ClientSubscriptionKey(rollbackTab[i].nameSubscription, drvKey, false);
		sub = (fr.dyade.aaa.mom.ClientSubscription) subscriptionTable.get(key);
       
        if (sub == null) {
		  key = new fr.dyade.aaa.mom.ClientSubscriptionKey(rollbackTab[i].nameSubscription, drvKey, true);
		  sub = (fr.dyade.aaa.mom.ClientSubscription) subscriptionTable.get(key);
        } 
				
				/* set the message as Redelivered */
		sub.putBackNonAckMessage(rollbackTab[i].messageID);
	    }
			
	    /*	send the agreement to the client */
	    fr.dyade.aaa.mom.RequestAgreeMOMExtern msgAgree = new fr.dyade.aaa.mom.RequestAgreeMOMExtern(msgRecover.getMessageMOMExternID(), drvKey);
	    agentClient.sendMessageMOMExtern(msgAgree);
	
	    /* delivers the message to set as redelivered if possible */
        Boolean startMode = (Boolean) startModeTable.get(new Integer(drvKey));
	    if(startMode.booleanValue()) {
		fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern msgDeliver;
		fr.dyade.aaa.mom.Message msg;
		for(i=0;i<rollbackTab.length;i++) {
		    to = AgentId.fromString(rollbackTab[i].topic.getTopicName());
		    key = new fr.dyade.aaa.mom.ClientSubscriptionKey(rollbackTab[i].nameSubscription, drvKey, false);
		    sub = (fr.dyade.aaa.mom.ClientSubscription) subscriptionTable.get(key);
            if (sub == null) {
		      key = new fr.dyade.aaa.mom.ClientSubscriptionKey(rollbackTab[i].nameSubscription, drvKey, true);
		      sub = (fr.dyade.aaa.mom.ClientSubscription) subscriptionTable.get(key);
            }
				
		    while((msg = sub.deliveryMessage())!=null) {
            if (sub.isConnectionConsumer())
			msgDeliver = new fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern((long) -1, key.subscriptionName, msg, sub.getTheme(), sub.getDriverKey(), sub.isConnectionConsumer());
            else {
			msgDeliver = new fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern((long) -1, key.subscriptionName, msg, sub.getTheme(), sub.getDriverKey());
			agentClient.sendMessageMOMExtern(msgDeliver);
            }
		    }
		}
	    }
	}
    }	


    /**
     * Stores a set of messages waiting for the transaction's commit.
     */
    protected void notificationXAPrepare(MessageXAPrepare msgPrepare) throws Exception {
      int drvKey = msgPrepare.getDriverKey();
	if (msgPrepare.ackVector != null) xidTable.setAckToSendXid(msgPrepare.xid, msgPrepare.ackVector);
	if (msgPrepare.msgVector != null) xidTable.setMessageToSendXid(msgPrepare.xid, msgPrepare.msgVector);

	xidTable.setXidStatus(msgPrepare.xid, fr.dyade.aaa.joram.XidTable.PREPARED);

	// Send an ack back to the client
    MessageMOMExtern msgMOM = new MessageAckXAPrepare(msgPrepare.getMessageMOMExternID());
    msgMOM.setDriverKey(drvKey);
	agentClient.sendMessageMOMExtern(msgMOM);
    }


    /**
     * Perform the commit following the prepare during a XA transaction.
     */
    protected void notificationXACommit(MessageXACommit msgCommit) throws Exception {
	try {
        int drvKey = msgCommit.getDriverKey();
	    Vector msgVector = xidTable.getMessageToSendXid(msgCommit.xid);
	    Vector ackVector = xidTable.getMessageToAckXid(msgCommit.xid);

	    // Send the messages
	    if (msgVector != null) {
		while (!msgVector.isEmpty()) {
		    Object msg = msgVector.remove(0);
		    if (msg instanceof SendingMessageQueueMOMExtern) {
            ((SendingMessageQueueMOMExtern) msg).setDriverKey(drvKey);
			notificationQueueSend((SendingMessageQueueMOMExtern) msg);
		    } else if (msg instanceof SendingMessageTopicMOMExtern) {
            ((SendingMessageTopicMOMExtern) msg).setDriverKey(drvKey);
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
            ((AckQueueMessageMOMExtern) ack).setDriverKey(drvKey);
			notificationQueueAck((AckQueueMessageMOMExtern) ack);
		    } else if (ack instanceof AckTopicMessageMOMExtern) {
            ((AckTopicMessageMOMExtern) ack).setDriverKey(drvKey);
			notificationTopicAck((AckTopicMessageMOMExtern) ack);
		    } else {
			throw new Exception();
		    }
		}
	    }

	    // Remove the messages and the acks from the xid
	    xidTable.setXidStatus(msgCommit.xid, fr.dyade.aaa.joram.XidTable.COMMITTED);

	    // Send an ack back to the client
        MessageMOMExtern msgMOM = new MessageAckXACommit(msgCommit.getMessageMOMExternID());
        msgMOM.setDriverKey(drvKey);
	    agentClient.sendMessageMOMExtern(msgMOM);
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new Exception();
	}
    }


    protected void notificationXARollback(MessageXARollback msgRollback) throws Exception {
	Vector msgVector = msgRollback.msgToRollbackVector;
    int drvKey = msgRollback.getDriverKey();

	if (msgVector == null) {
	    // No messages to rollback, delete the messages and put the Xid in ROLLBACKED mode
	    xidTable.removeXid(msgRollback.xid);
	    xidTable.setXidStatus(msgRollback.xid, fr.dyade.aaa.joram.XidTable.ROLLBACKED);
	} else {
	    // We have to push back the messages to the source to signal a rollback
	    while (!msgVector.isEmpty()) {
		Object msg = msgVector.remove(0);
		if (msg instanceof MessageRollbackMOMExtern) {
		    // Queue
		    MessageRollbackMOMExtern currentMsg = (MessageRollbackMOMExtern) msg;
		    AgentId to = AgentId.fromString(((DestinationNaming) currentMsg.getJMSDestination()).getDestination());
		    NotificationRollback not = new NotificationRollback(currentMsg.getMessageMOMExternID(), currentMsg.getJMSMessageID(),
									currentMsg.getJMSSessionID(), drvKey);
		    agentClient.sendNotification(to, not);
		} else if (msg instanceof AckTopicMessageMOMExtern) {
		    // Topic
		    notificationTransactedRollbackTopicAck((AckTopicMessageMOMExtern) msg);
		}
	    }
	}
    MessageMOMExtern msgMOM = new MessageAckXARollback(msgRollback.getMessageMOMExternID());
    msgMOM.setDriverKey(drvKey);
	agentClient.sendMessageMOMExtern(msgMOM);
    }


    protected void notificationXARecover(MessageXARecover msgRecover) throws Exception {
      int drvKey = msgRecover.getDriverKey();
      MessageMOMExtern msgMOM = new MessageAckXARecover(msgRecover.getMessageMOMExternID(), 
        xidTable.getXidList());
      msgMOM.setDriverKey(drvKey);
	agentClient.sendMessageMOMExtern(msgMOM);
    }


    private String calculateMessageID() {
      return (new Long(msgCounter++)).toString();
    }
    /** incrementation of the counter with the syntax
     *	a,b,c,...,z,aa,ab,ac,...,az,ba,... 

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
    }*/
	
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
    protected void deliverAlienException(Exception exc, int drvKey) {
    Boolean exceptionMode = (Boolean) exceptionModeTable.get(new Integer(drvKey));
	if(exceptionMode.booleanValue()) {
	    // client arises an exceptionlistener 
	    fr.dyade.aaa.mom.ExceptionListenerMOMExtern msgExc = new fr.dyade.aaa.mom.ExceptionListenerMOMExtern(exc, drvKey);
	    agentClient.sendMessageMOMExtern(msgExc);
     
	}
		
	// warning to the administrator
	warningAdministrator(exc);
    }
	
}
