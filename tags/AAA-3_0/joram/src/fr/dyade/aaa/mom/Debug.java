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
 
public final class Debug implements java.io.Serializable  {

	public static final boolean debug = true;
	
	/** Queue Debug */
	public static boolean queueSend = false;
	public static boolean queueReceive = false;
	public static boolean queueAck = false;
	public static boolean queueRead = false;
	public static boolean queueDelivery = false;
	
	/** Topic Debug */
	public static boolean topicTheme = false;
	public static boolean topicSend = false;
	
	/** AgentClient Debug */
	public static boolean clientTest = false;
	public static boolean clientSub = false;
	public static boolean clientAck = false;
	public static boolean clientSend = false;
	public static boolean clientClose = false;
	
	/** selector debug */
	public static boolean JmsSelector = false;
	
	/** Message debug */
	public static boolean message = false;

    /** topic debug */
    public static boolean topic = false;

    /** queue debug */
    public static boolean queue = false;


    /** admin debug */
    public static boolean admin = false;

  /** transacted debug */	
  public static boolean transacted = false;

	static {
		queueSend = Boolean.getBoolean("Debug.queueSend");
		queueReceive = Boolean.getBoolean("Debug.queueReceive");
		queueAck = Boolean.getBoolean("Debug.queueAck");
		queueRead = Boolean.getBoolean("Debug.queueRead");
		queueDelivery = Boolean.getBoolean("Debug.queueDelivery");
		
		topicTheme = Boolean.getBoolean("Debug.topicTheme");
		topicSend = Boolean.getBoolean("Debug.topicSend");
		
		clientTest = Boolean.getBoolean("Debug.clientTest");
		clientSub = Boolean.getBoolean("Debug.clientSub");
		clientAck = Boolean.getBoolean("Debug.clientAck");
		clientAck = Boolean.getBoolean("Debug.clientSend");
		clientClose = Boolean.getBoolean("Debug.clientClose");
		
		JmsSelector = Boolean.getBoolean("Debug.selector");
		message = Boolean.getBoolean("Debug.message");
		topic = Boolean.getBoolean("Debug.topic");
		topic = Boolean.getBoolean("Debug.queue");
		admin = Boolean.getBoolean("Debug.admin");
		transacted = Boolean.getBoolean("Debug.transacted");
	}
	
	
	/** print all the messages for a particular Queue */
	public static void printQueue(String where, Vector queueMessage) {
		try {
			Enumeration e = queueMessage.elements();
			fr.dyade.aaa.mom.MessageAndAck msgAndAck;
			fr.dyade.aaa.mom.TextMessage txt;
			int i=0;
			
			while(e.hasMoreElements()){
				msgAndAck = (fr.dyade.aaa.mom.MessageAndAck) e.nextElement();
				if(msgAndAck.getMessage() instanceof fr.dyade.aaa.mom.TextMessage) {
					txt = (fr.dyade.aaa.mom.TextMessage) msgAndAck.getMessage();
					System.out.println(where+" "+i+" : "+txt.getText()+"  messageId : "+txt.getJMSMessageID());
				}
				i++;
			}
		} catch (Exception exc) { 
			System.out.println("Erreur de Debug");
		}
	}
	
	/** print all the request in a Queue  */
	public static void printRequest(String where, Vector queueMessage){
		try {
		Enumeration e = queueMessage.elements();
		
			while(e.hasMoreElements()){
				fr.dyade.aaa.mom.RequestQueueObject reqObj = (fr.dyade.aaa.mom.RequestQueueObject) e.nextElement();
				System.out.println(where+" ! client : "+reqObj.getAgentIdentity().toString()+"   time : "+String.valueOf(reqObj.getTimeOut())+"   selec : "+reqObj.getSelector());
			}
		} catch (Exception exc) { 
			System.out.println("Erreur de Debug");
		}
	}

	/** print all the tree */
	public static void printThemeTree(fr.dyade.aaa.mom.Theme theme, String level) {
		Hashtable table = theme.getThemeDaughterTable();
		if(table.isEmpty()) {
			/* Theme leaf */
			System.out.println("Theme "+level+" : "+theme.getNameTheme());
		} else {
			/* Node */
			System.out.println("Theme "+level+" : "+theme.getNameTheme());
			/* recurse of the sons */ 
			Enumeration e = theme.getThemeDaughterTable().elements();
			int i=0;
			while(e.hasMoreElements()){
				printThemeTree(((fr.dyade.aaa.mom.Theme) e.nextElement()), (level+"."+String.valueOf(i)));
				i++;
			}
		}
	}

	/** print the subscription in the agentClient */
	public static void printSubscription(String whoIam,Hashtable table) {
		Enumeration e = table.elements();
		fr.dyade.aaa.mom.SubscriptionClient sub ;
		int i=0;
		
		System.out.println(whoIam+" : table souscription ");
		while(e.hasMoreElements()){
			sub = (fr.dyade.aaa.mom.SubscriptionClient) e.nextElement();
			System.out.println("abonnement "+i+" : "+sub.getTopicID()+" "+sub.getNameTheme());
			i++;
		}
	}
	
	/** print the subscription in the agentClient */
	public static void printKeysSubscription(String whoIam,Hashtable table) {
		Enumeration e = table.keys();
		int i=0;
		
		System.out.println(whoIam+" : table souscription ");
		while(e.hasMoreElements()){
			KeyClientSubscription key = (KeyClientSubscription) e.nextElement(); 
			System.out.println(key.nameSubscription);
			i++;
		}
	}
	
	/** print the subscription in the agentClient */
	public static void printMessageSubscription(String whoIam,Vector v) throws Exception{
		Enumeration e = v.elements();
		fr.dyade.aaa.mom.TextMessage msgtxt;
		fr.dyade.aaa.mom.TextMessage msg;
		
		System.out.println(whoIam+" : Message ");
		while(e.hasMoreElements()){
			msg = (fr.dyade.aaa.mom.TextMessage) e.nextElement();
			if(msg instanceof fr.dyade.aaa.mom.TextMessage) {
				msgtxt = (fr.dyade.aaa.mom.TextMessage) msg;
				System.out.println("ID "+msgtxt.getJMSMessageID()+" "+msgtxt.getText());
			}
		}
	}
	
	/** print the message from a subscriptionClient */
	public static void printSubMessage(String where, Vector v) {
		try {
			Enumeration e = v.elements();
			javax.jms.Message msg;
			while(e.hasMoreElements()){
				msg = (javax.jms.Message) e.nextElement();
				if(msg instanceof javax.jms.TextMessage) {
					fr.dyade.aaa.mom.TextMessage msgTxt = (fr.dyade.aaa.mom.TextMessage) msg;
					System.out.println(where+" : en attente : "+msgTxt.getText());
				} else if(msg instanceof javax.jms.BytesMessage)  {
					fr.dyade.aaa.mom.BytesMessage msgBytes = (fr.dyade.aaa.mom.BytesMessage) msg;
					System.out.println(where+" : en attente : "+msgBytes.readUTF());
				} else if(msg instanceof javax.jms.StreamMessage) {
					fr.dyade.aaa.mom.StreamMessage msgStream = (fr.dyade.aaa.mom.StreamMessage) msg;
					System.out.println(where+" : en attente : "+msgStream.readString());
				}
			}
		} catch (Exception exc) { 
			System.out.println("Erreur de Debug");
		}
	}

}
