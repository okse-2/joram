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


public class Subscription implements java.io.Serializable { 
 
 	/** the name of a the subscription given by the agentClient */
	public String nameSubscription;
	
	/** the agentClient choses if he wants to receive its own meseage  
	 *	0 means not 
	 */ 
	boolean noLocal;  
	 
	/** the selector of the request */ 
	String selector;  
	
	/** the agentId of the client who takes the subscription */
 	AgentId agentClient;
 
	public Subscription(boolean noLocalNew, String selectorNew, AgentId clientNew, String nameSubscriptionNew) {
		nameSubscription = nameSubscriptionNew;
		noLocal = noLocalNew; 
		selector = selectorNew ;
		agentClient = clientNew;
	}

	/** get the selector of the subcription */
	public String getSelector() {
		return selector;
	}
	
	/** get the noLocal attribute of the subcription */
	public boolean getNoLocal() {
		return noLocal;
	}
	
	/** get the name of the subcriber */
	public AgentId getAgentClient() {
		return agentClient;
	}
	
	/** get the name of the subcription */
	public String getNameSubscription() {
		return nameSubscription;
	}
	
	/** update the subscription */
	public void updateSubscription(boolean noLocalNew, String selectorNew) {
		selector = selectorNew;
		noLocal = noLocalNew;
	}
	
}
