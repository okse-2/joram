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
 * a Topic is a an object where a client can subscribe on a particular Theme
 * and receive messages asynchronously with particular selector
 *	
 * @see		fr.dyade.aaa.mom.Queue
 * @see		fr.dyade.aaa.mom.AgentClient
 * @see		fr.dyade.aaa.mom.Destination	
 */


public class Topic extends fr.dyade.aaa.mom.Destination { 
 
 	/** root of Theme tree */
	fr.dyade.aaa.mom.Theme rootTheme;
	
	/** Constructor of the 1st Theme if administrator doesn't make*/
	public Topic() {
		rootTheme = new Theme(".",".");
	}
	
 	public void react(AgentId from, Notification not) throws Exception {
		if (not instanceof NotificationSubscription) { 
			notificationSubscription(from, (NotificationSubscription) not); 
		} else if (not instanceof NotificationUnsubscription) { 
			notificationUnsubscription(from, (NotificationUnsubscription) not);
		} else if (not instanceof NotificationUpdateSubscription) { 
			notificationUpdateSubscription(from, (NotificationUpdateSubscription) not);	 
		} else if (not instanceof NotificationTopicSend) { 
			notificationSend(from, (NotificationTopicSend) not);
		} else  if (not instanceof NotificationAdminDeleteDestination) {
		    delete();
		} else { 
			super.react(from, not); 
		} 
	}
 
 	/** the Topic receives a Message from a agentClient thanks to NotificationSend */ 
	protected void notificationSend(AgentId from, NotificationTopicSend not) throws Exception { 
		try {
 			fr.dyade.aaa.mom.TopicNaming topic = (fr.dyade.aaa.mom.TopicNaming) not.msg.getJMSDestination();
			fr.dyade.aaa.mom.Theme theme = searchTheme(topic.getTheme());
			if(theme==null)
				throw (new MOMException("No existing such Theme",MOMException.THEME_NO_EXIST));
			
			/* check the fields of the message : destroyed if incomplete */
			if(!checkFieldsMessage(not.msg))
				throw (new MOMException("Fields of the Message Incomplete",MOMException.MESSAGE_INCOMPLETE));
			
			/* check the Message */ 
		 	if(checkMessage(not.msg)) {
		 
				StringTokenizer st = new StringTokenizer(topic.getTheme(),"/",false);
				
				/* delivery to the root because themeNode = rootTheme */
				fr.dyade.aaa.mom.Theme themeNode = rootTheme;
				deliveryTopicMessage(themeNode, not.msg, from);
				st.nextToken();
				
				while(st.hasMoreTokens()){
				
					if(Debug.debug)
						if(Debug.topicSend && (not.msg instanceof fr.dyade.aaa.mom.TextMessage))
							System.out.println("Ancetre: "+((fr.dyade.aaa.mom.TextMessage)not.msg).getText());
				
					themeNode = (fr.dyade.aaa.mom.Theme) themeNode.getThemeDaughter(st.nextToken());
					deliveryTopicMessage(themeNode, not.msg, from);
				}
			}
			
			/* deliver an agreement to the client if Persistent */
			if(not.msg.getJMSDeliveryMode()==fr.dyade.aaa.mom.Message.PERSISTENT)
				deliveryAgreement(from, not);
			
		} catch (MOMException exc) { 
			deliveryException (from, not, exc);
		} 
	}
 
 	/** a client subscribes to a theme */ 
	protected void notificationSubscription(AgentId from, NotificationSubscription not) throws Exception { 
		try {
			fr.dyade.aaa.mom.Theme theme = searchTheme(not.theme);
			if(theme==null)
				throw (new MOMException("Subscribe : No existing such Theme",MOMException.THEME_NO_EXIST));
		
			fr.dyade.aaa.mom.Subscription sub = new fr.dyade.aaa.mom.Subscription(not.noLocal,not.selector,from, not.nameSubscription);
			theme.addSubscription(not.nameSubscription, from, sub);
			
			/* deliver an agreement to the client */
			deliveryAgreement(from, not);
			
 		} catch (MOMException exc) { 
			deliveryException (from, not, exc);
		}
	}
 
 	
 	/** the Topic receives a Message from a agentClient thanks to NotificationSend */ 
	protected void notificationUnsubscription(AgentId from, NotificationUnsubscription not) throws Exception{ 
		try {
			fr.dyade.aaa.mom.Theme theme = searchTheme(not.theme);
			if(theme==null)
				throw (new MOMException("Unsubscribe : No existing such Theme",MOMException.THEME_NO_EXIST));
		
			if(theme.removeSubscription(not.nameSubscription, from)==null)
				throw (new MOMException("Unsubscribe : No subscription existed",MOMException.SUBSCRIPTION_NO_EXIST));
 			
			/* deliver an agreement to the client */
			deliveryAgreement(from, not);
			
		} catch (MOMException exc) { 
			deliveryException (from, not, exc);
		}
 	}
 
 	/** updating of the subscription of the client */
 	protected void notificationUpdateSubscription(AgentId from, NotificationUpdateSubscription not) {
		try {
			fr.dyade.aaa.mom.Theme theme = searchTheme(not.theme);
			if(theme==null)
				throw (new MOMException("Update : No existing such Theme",MOMException.THEME_NO_EXIST));
		
			fr.dyade.aaa.mom.Subscription sub;
			if((sub=theme.getSubscription(not.nameSubscription, from))==null)
				throw (new MOMException("Update : No existing such Subscription",MOMException.SUBSCRIPTION_NO_EXIST));
			else
				sub.updateSubscription(not.noLocal, not.selector);
			
			/* deliver an agreement to the client */
			deliveryAgreement(from, not);
			
		} catch (MOMException exc) { 
			deliveryException (from, not, exc);
		}
	}
 
 	/** send a message to all of the agentClients who subscribed to the node */
	protected void deliveryTopicMessage (fr.dyade.aaa.mom.Theme theme, fr.dyade.aaa.mom.Message msg, AgentId from) throws Exception{
		/* get the list of subscriptions */
		Enumeration e = theme.getAllSubscriptions();
		fr.dyade.aaa.mom.Selector selecObj = new fr.dyade.aaa.mom.Selector();
			
		while(e.hasMoreElements()){
			fr.dyade.aaa.mom.Subscription sub = (fr.dyade.aaa.mom.Subscription) e.nextElement();
				
			/* check of the noLocal attribute [ !a ou (a && !b) ] and the selector */
			if((!sub.getNoLocal() || !(from.equals(sub.getAgentClient()))) && selecObj.isAvailable(msg,sub.getSelector())) {
				
				if(Debug.debug)
					if(Debug.topicSend && (msg instanceof fr.dyade.aaa.mom.TextMessage)) {
						System.out.println("msg send "+sub.getAgentClient().toString()+" : "+((fr.dyade.aaa.mom.TextMessage) msg).getText());
						System.out.println("absolute name : "+theme.getAbsoluteNameTheme());
					}
				
				/* delivery of the message */
				fr.dyade.aaa.mom.NotificationTopicMessageDeliver notMsgDeliv = new fr.dyade.aaa.mom.NotificationTopicMessageDeliver(sub.getNameSubscription(), msg, theme.getAbsoluteNameTheme());
				sendTo(sub.getAgentClient(), notMsgDeliv);
			}
			
		}
	}
 
 	/** searchs a particular Theme in the tree of the Topic */
	protected fr.dyade.aaa.mom.Theme searchTheme(String nameTheme) {
		
		StringTokenizer st = new StringTokenizer(nameTheme,"/",false);
		fr.dyade.aaa.mom.Theme themeNode = rootTheme;
		String nameNode;		
		String DaughterNode;
		int separator;
		
		/* begining case */
		nameNode = st.nextToken();
		if(!st.hasMoreTokens()) {
			/* Topic leaf */
			if(nameNode.equals(rootTheme.getNameTheme()))
				return rootTheme;
			else
				return null;
		}
		
		while(true) {
			if(!st.hasMoreTokens()) {
				/* Topic leaf */
				if(nameNode.equals(themeNode.getNameTheme()))
					return themeNode;
				else
					return null;
			} else {
				/* directory */
				DaughterNode = st.nextToken();
				
				if((themeNode = themeNode.getThemeDaughter(DaughterNode))!=null) {
					/* a new level */
					nameNode = DaughterNode;
				} else {
					/* No theme Found*/
					return null;
				}
			}
		}
	}
	
	
	/**	class to take the subdirectories themes one after one  
	 *	lexical analysis 
	 */
	private class Token {

		transient StringTokenizer st;

		public Token(String line) {
			st = new StringTokenizer(line," \t\n\r;:",true);
		}

		public String get() {
			String tok;
			while (st.hasMoreTokens()) {
				tok = st.nextToken();
				if ((tok.equals(" ")) || (tok.equals("\t")))
					continue;
				return tok;
			}
			return ("");
		}
	}

	
	/** constructs a subject-Theme tree 
	 * . subdirectories ; ./directory subdirectories; ./.../directory subdirectories ... ;
	 */
	public void constructTheme(String line) throws Exception {
		fr.dyade.aaa.mom.Theme themeNode ;
		Token token = new Token(line);
		String tok;
		
		while(!(tok = token.get()).equals("")) {
			
			if(Debug.debug)
				if(Debug.topicTheme)
					System.out.println("tok "+tok);
			
			/* find the directory */
			themeNode = searchTheme(tok);
			if(themeNode==null)
				throw (new Exception("Erreur Constuctor Theme Tree"));
			/* subdirectories */
			while(!(tok = token.get()).equals(";")) {
				/* add a Theme subdirectory */
				themeNode.addThemeDaughter(tok, new Theme(tok,themeNode.getAbsoluteNameTheme()+"/"+tok));
			}
		}
		
		if(Debug.debug)
			if(Debug.topicTheme)
				Debug.printThemeTree(rootTheme,"0");
	}
	
}
