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

import java.net.*; 
import java.util.*;
import java.lang.*;

/** 
 *	a Topic is an object associated with a real Topic
 *	in the AAAMOM
 * 
 *      sample : Topic("joram://host:port/#x.y.z");
 *
 *      @author Nicolas Tachker
 *
 *	@see fr.dyade.aaa.mom.Destination
 *	@see javax.jms.Topic
 *	@see fr.dyade.aaa.mom.Topic
 */  
 
public class Topic extends fr.dyade.aaa.mom.TopicNaming {
    /** agent Topic in AAAMOM */
    String agentTopic;
    /** host Name */
    String host; 
    /** port */
    int port;
    /** Joram URL */
    URL url;

    /* constructor of the Topic : Topic("joram://host:port/#x.y.z");*/    
    public Topic(String stringURL) {
	super(null,".");
	try {
	    url = new URL(stringURL);
	    if (Debug.debug)
		if (Debug.topic)
		    System.out.println("Topic (Protocol=" + url.getProtocol() +
				       ", Host=" + url.getHost() +
				       ", Port=" + url.getPort() +
				       ", AgentId=#" + url.getRef() + ")");
	    
	    if ( url.getProtocol().equals(fr.dyade.aaa.joram.ConfigURLStreamHandlerFactory.Joram) ) {
		agentTopic ="#" + url.getRef();
		host = url.getHost();
		port = url.getPort();
		dest = agentTopic;
	    }
	} catch (Exception e) {
	    System.out.println("Topic Exception");
	    e.printStackTrace();
	}
    }

    /* constructor of the Topic with Joram URL */
    public Topic(java.net.URL url) {
	super(null,".");
	try {
	    if (Debug.debug)
		if (Debug.topic)
		    System.out.println("Topic (Protocol=" + url.getProtocol() +
				       ", Host=" + url.getHost() +
				       ", Port=" + url.getPort() +
				       ", AgentId=#" + url.getRef() + ")");
	    
	    if ( url.getProtocol().equals(fr.dyade.aaa.joram.ConfigURLStreamHandlerFactory.Joram) ) {
		this.url = url;
		host = url.getHost();
		port = url.getPort();
		agentTopic ="#" + url.getRef();
		dest = agentTopic;
	    }
	} catch (Exception e) {
	    System.out.println("Topic Exception");
	    e.printStackTrace();
	}
    }

    /** return the agent Topic in AAAMOM */
    public String getAgentTopic() {
	return agentTopic;
    }
    public String toString() {
	try {
	    return url.toString();
	} catch (Exception e) {
	    System.out.println("Topic Exception ");
	    e.printStackTrace();
	    return null;
	}
    }
}
