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


package fr.dyade.aaa.joram; 

import java.lang.*;
/* 
 *	a KeyConnectionSubscription allows to the Connection to refind
 *	the session so as to it deliver the message received
 *
 *	@see	fr.dyade.aaa.mom.AgentClient
 */


public class KeyConnectionSubscription implements java.io.Serializable { 
 
 	/** the name of a the subscription given by the agentClient */
	public String nameSubscription;
	
	/** the name of the topic where the agentClient took the subscription */
	public String topic;
	
	/** the name of a theme */
	public String theme;

	/** Constructor */
	public KeyConnectionSubscription(String nameSubscriptionNew, String topicNew, String themeNew) {
		nameSubscription = nameSubscriptionNew;
		topic = topicNew;
		theme = themeNew;
		
	}

	public boolean equals(Object obj) {
		if(obj instanceof KeyConnectionSubscription) {
			KeyConnectionSubscription key = (KeyConnectionSubscription) obj;
			return (topic.equals(key.topic) && nameSubscription.equals(key.nameSubscription) && theme.equals(key.theme));
		} else	
			return false;
	}

	public int hashCode() {
		return nameSubscription.hashCode();
	}

}
