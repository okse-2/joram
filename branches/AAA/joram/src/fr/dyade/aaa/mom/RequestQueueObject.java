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
 
/** 
 *	a Request object allows to stack the requests of a client
 * 
 * @see         fr.dyade.aaa.mom.Topic 
 * @see         fr.dyade.aaa.mom.AgentClient 
 */ 
 
public class RequestQueueObject implements java.io.Serializable { 
	
	/** the identity of the agent which has to acknowledge the message */ 
	private fr.dyade.aaa.agent.AgentId agentClient; 
	
	/** the instant of the end of the request */
	private long timeOut;
	
	/** the selector of the request */ 
	private String selector;  
	
	/** the identifier of the MOM Notification */ 
	private long notMOMID;
	
	/** the identifier of the Session */  
	private String sessionID;
	
	
	RequestQueueObject(fr.dyade.aaa.agent.AgentId agentClientNew, long timeOutNew, String selectorNew, long notMOMIDNew, String sessionIDNew) {
		agentClient = agentClientNew;
		timeOut = timeOutNew;
		selector = selectorNew;
		notMOMID = notMOMIDNew;
		sessionID = sessionIDNew;
	}
	
	/** get the identity of the agent doing the request */
	public fr.dyade.aaa.agent.AgentId getAgentIdentity() {
		return agentClient;
	}
	
	/** the timeOut of the request*/
	public long getTimeOut() {
		return timeOut;
	}
	
	/** the selector of the request  */
	public String getSelector() {
		return selector;
	}
	
	/** the MOMID of the request */
	public long getNotMOMID() {
		return notMOMID;
	}
	
	/** return the sessionID which has to acknowledge the message */
	public String getSessionID() {
		return sessionID;
	}
}
