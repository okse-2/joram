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
 *	a XATopic is a Topic which implements the transacted behaviour
 *
 *	@see fr.dyade.aaa.mom.Topic
 */


public class XATopic extends fr.dyade.aaa.mom.Topic { 
 
 	/** Constructor of the 1st Theme if administrator doesn't make*/
	public XATopic() {
		super();
	}
	
	/** the Topic receives a Message from a agentClient thanks to NotificationSend 
	 *	a requestAgree must be sent even the message is not PERSISTENT
	 */ 
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
			
			/* deliver an agreement to the client */
			deliveryAgreement(from, not);
			
		} catch (MOMException exc) { 
			deliveryException (from, not, exc);
		} 
	}
 
}
