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
  
/** 
  *	this object allows to conserve messages and to know who has to acknowledge
  *	the message 
  *
  * @version     29/05/1999 
  * @author      Richard Mathis 
  * 
  * @see         fr.dyade.aaa.mom.Topic 
  * @see         fr.dyade.aaa.mom.AgentClient 
  */ 
 
 
public class MessageAndAck implements java.io.Serializable  { 
 
 
	/** the message conserved by a Queue*/ 
	private fr.dyade.aaa.mom.Message msg; 
	  
	/** the identity of the agent which has to acknowledge the message */ 
	private fr.dyade.aaa.agent.AgentId agentClient;
	 
	/** the identifier of the Session */  
	private String sessionID;
	
	 
	/** Constructor only for messages sent  */ 
	public MessageAndAck(fr.dyade.aaa.mom.Message newMessage) { 
		msg = newMessage; 
		agentClient = null;
		sessionID = null;
	}  
	 
	/** return the message conserved by the object */  
	public fr.dyade.aaa.mom.Message getMessage() { 
		return msg; 
	} 
	 
	/** return the agentClient which has to acknowledge the message */ 
	public fr.dyade.aaa.agent.AgentId getAgentIdentity() { 
		return agentClient; 
	} 
	
	/** return the sessionID which has to acknowledge the message */
	public String getSessionID() {
		return sessionID;
	}
	
	/** set the agentClient which has to acknowledge the message */ 
	public void setAgentIdentity(fr.dyade.aaa.agent.AgentId agentAck, String sessionAck) { 
		 agentClient = agentAck;
		 sessionID = sessionAck;
	}
	 
	 
} 
