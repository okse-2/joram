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
import fr.dyade.aaa.agent.*;
import java.util.*; 
 
/**
 *	a Subsccritpion object contains all of information necessary
 *	to recognize a subscription from a client
 *	
 * @see         fr.dyade.aaa.mom.Topic
 */


public class SubscriptionClient implements java.io.Serializable { 
 
 	/** the agentClient choses if he wants to receive its own meseage  
	 *	0 means not 
	 */ 
	private boolean noLocal;  
	 
	/** the selector of the request */ 
	private String selector;  
	
	/**  the name of the Topic of the subscription */
 	private AgentId topicID;
	
	/** the name of the Theme of the subscription */
	private String theme;

	/** the Vector of messages for the present subscription */
 	Vector queueThemeMessage;
	
	/** indicates if the (external) cliant arose a messageListener */
	private boolean messageListener;
	
	/** index of the last delivered message to the (external) client */
	private int msgLastDelivered;
	
	/** the session bound to the subscription
	 *	if the session is null, this means that the client doesn't work on
	 *	subscription at present time
	 */
	private String sessionID = null;
 
	public SubscriptionClient(boolean noLocal, String selector, AgentId topicID, String theme, String sessionID) {
		this.noLocal = noLocal; 
		this.selector = selector ;
		this.topicID = topicID;
		this.theme = theme;
		this.queueThemeMessage = new Vector();
		this.msgLastDelivered = -1;
		this.messageListener = false;
		this.sessionID = sessionID;
	}

	/** update of the parameters of the subscription 
	 *	and consequently destruction possible of messages in the Queue
	 *	because they don't respond to the selector 
	 */
	public void updateSubscription(AgentId agentClient, boolean noLocalNew, String selectorNew) throws Exception {
		/* destruction of the own messages of the client */
		int i = 0;
		fr.dyade.aaa.mom.Selector selecObj = new fr.dyade.aaa.mom.Selector();
		String messageID;
		String sender;
		
		/* record of new parameter of filtering */
		noLocal = noLocalNew; 
		selector = selectorNew ;
		
		while(i<queueThemeMessage.size()) {
			fr.dyade.aaa.mom.Message msg = (fr.dyade.aaa.mom.Message) queueThemeMessage.elementAt(i);
			if(Destination.checkMessage(msg)) {
						
				/* the selector makes testing if a mesage was delivered or not */
				/* because messages are not destroyed if selector is not OK */
				messageID = msg.getJMSMessageID();
				sender =  messageID.substring(0,messageID.indexOf('_'));
				if(( noLocal && agentClient.toString().equals(sender)) || (selecObj.isAvailable(msg,selector)))
					queueThemeMessage.removeElementAt(i);
				else
					i++;
			} else {
				/* destruction of the message of the queue becauese timeOut expired*/
				queueThemeMessage.removeElementAt(i);
			}
		}
	}

	/** add message in the queue of the subscription with priority 
	 *	increments the index of the last deliver if the client arose
	 *	a messageListener 
	 */
	public void putMessageInAgentClient(fr.dyade.aaa.mom.Message msg) {
		queueThemeMessage.addElement(msg);
	}

	/** remove al least one message in the queue of the subscrption */
	public void removeMessage(String messageID) throws Exception {
		boolean messageNotFound = true;
		/* destruction of all previous messages */ 
		while(!queueThemeMessage.isEmpty()) {
			fr.dyade.aaa.mom.Message msg = (fr.dyade.aaa.mom.Message) queueThemeMessage.firstElement();

			/* tests for all messages from the "from" agentClient */
			if(msg.getJMSMessageID().equals(messageID)) {
				queueThemeMessage.removeElementAt(0);
				msgLastDelivered--;
				messageNotFound = false;
				break ;
			} else {
				/* destuction of previous messages */
				queueThemeMessage.removeElementAt(0);
				msgLastDelivered--;
			}
			
		}
		
		/*	this case can happen when the client close a durable Session and then
		 *	acknowledge a message 
		 */
		if(msgLastDelivered<-1)
			msgLastDelivered= -1;
			
		/* if no messageID exists, no message is destroyed thanks to the engine AAA */
		if(messageNotFound) {
			System.out.println("except agentClient : ID "+messageID);
			throw (new MOMException("No Existing MessageID",MOMException.MESSAGEID_NO_EXIST));
		}
	}

	/**	returns the first non-delivered message in the queue if any
	 *	returns null otherwise
	 */
	public fr.dyade.aaa.mom.Message deliveryMessage() throws Exception {
		if(Debug.debug)
			if(Debug.clientSub) {
				System.out.println(" Delivery,  listener: "+messageListener+" lastDeliver:  "+msgLastDelivered);
				System.out.println("size: "+queueThemeMessage.size());
			}
		fr.dyade.aaa.mom.Message msg;
		while((msgLastDelivered<(queueThemeMessage.size()-1))) {
			msg = (fr.dyade.aaa.mom.Message) queueThemeMessage.elementAt(msgLastDelivered+1);
			
			/* check if the message is expired*/
			if(Destination.checkMessage(msg)) {
				/* check if the the messageListener is put*/
				if(messageListener) {
					msgLastDelivered++;
			
					if(Debug.debug)
						if(Debug.clientSub) {
							try {
								System.out.println(" message found: "+((fr.dyade.aaa.mom.TextMessage) msg).getText());
							} catch (Exception exc) {
								System.err.println("notificationSettingListener "+exc);
							}
						}
					return msg;
				}
				return null;
			} else
				queueThemeMessage.removeElementAt(msgLastDelivered+1);
		} 
		return null;
	}
	
	/** Close Connection, Session or Consumer
	 *	replace the messages delivered but not acknowledged 
	 *	keep the initial order
	 */
	public void replaceRedeliveredMsg() throws Exception {
		int i;
		for(i=0;i<=msgLastDelivered;i++) {
			fr.dyade.aaa.mom.Message msg = (fr.dyade.aaa.mom.Message) queueThemeMessage.elementAt(i);
			msg.setJMSRedelivered(true);
		}
		msgLastDelivered = -1;
	}

	/** function Recover
	 *	replace messages delivered but not acknowledged 
	 *	reset messages in the end of the queue
	 *	
	 */
	protected void recoverDeliveredMsg(String messageID) throws Exception {

		if(msgLastDelivered>-1) {
			/* destruction of all previous messages */ 
			while(!queueThemeMessage.isEmpty()) {
				fr.dyade.aaa.mom.Message msg = (fr.dyade.aaa.mom.Message) queueThemeMessage.firstElement();
					
				/* removes the message from it place and reset in the end */
				if(msg.getJMSMessageID().equals(messageID)) {
					queueThemeMessage.removeElementAt(0);
					this.putMessageInAgentClient(msg);	
					msgLastDelivered--;
					break ;
				} else {
					/* destuction of previous messages */
					queueThemeMessage.removeElementAt(0);
					this.putMessageInAgentClient(msg);	
					msgLastDelivered--;
				}
			}
		}
	}
	
	/** transacted session
	 *	replace the messages delivered but not acknowledged 
	 *	keep the initial order
	 */
	protected void rollbackDeliveredMsg() throws Exception {
		int i;
		for(i=0;i<=msgLastDelivered;i++) {
			fr.dyade.aaa.mom.Message msg = (fr.dyade.aaa.mom.Message) queueThemeMessage.elementAt(i);
		}
		msgLastDelivered = -1;
	}
	
  /** */
  protected Message transactedRollbackDeliveredMsg(String messageID) throws Exception {
    //TODO
    return null;
  }
	
	/** get the selector of the subcription */
	public String getSelector() {
		return this.selector;
	}
	
	/** get the noLocal attribute of the subcription */
	public boolean getNoLocal() {
		return this.noLocal;
	}
	
	/** get the name of the Topic of the present subscription */
	public AgentId getTopicID() {
		return this.topicID;
	}
	
	/** get the name of the theme of the present subscription */
	public String getNameTheme() {
		return this.theme;
	}
	
	/** get the queue of messages of the subscription */
	public Vector getThemeMessage() {
		return this.queueThemeMessage;
	}

	/** set or unset a MessageListener */
	public void setMessageListener(boolean messageListenerNew) {
		messageListener = messageListenerNew;
	}
	
	/** get the MessageListener */
	public boolean getMessageListener() {
		return this.messageListener;
	}
	
	/** get the sessionID bound to the subscritpion */
	public String getSessionID() {
		return this.sessionID;
	}
	
	/** set the sesionID bound to the subscription */
	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}
}
