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
 *	a Queue is an object associated with a real Queue
 *	in the MOM, it's its name
 *      
 *      sample : Queue("joram://host:port/#x.y.z");
 * 
 *      @author     Nicolas Tachker
 *
 *	@see subclasses
 *	@see javax.jms.Queue
 *	@see fr.dyade.aaa.mom.Queue 
 */ 
 
public class Queue extends fr.dyade.aaa.mom.QueueNaming implements javax.naming.Referenceable, java.io.Serializable {
    /** agent Queue in AAAMOM */
    String agentQueue;
    /** host Name */
    String host; 
    /** port */
    int port;
    /** Joram URL */
    CURL url;
    // The static table of all Queue (jndi)
    private static Hashtable qList = new Hashtable();

    /** contructor : Queue("joram://host:port/#x.y.z"); */
    public Queue(String stringURL) {
	super(null);
	try {
	    url = new CURL(stringURL);
	    if (Debug.debug)
		if (Debug.queue)
		    System.out.println("Queue (Protocol=" + url.getProtocol() +
				       ", Host=" + url.getHost() +
				       ", Port=" + url.getPort() +
				       ", AgentId=" + url.getAgentId() + ")");
	    
	    if ( url.getProtocol().equals("joram") ) {
		agentQueue = url.getAgentId();
		host = url.getHost();
		port = url.getPort();
		dest = agentQueue;
	    }
	} catch (Exception e) {
	    System.out.println("Queue Exception");
	    e.printStackTrace();
	}
    }

    /** return agent Queue in AAAMOM */
    public String getAgentQueue() {
	return agentQueue;
    }

    /** comes from the javax.jndi.referenceable interface */
    public javax.naming.Reference getReference() throws javax.naming.NamingException{
	javax.naming.Reference ref =  new javax.naming.Reference(this.getClass().getName(),
					  "fr.dyade.aaa.joram.ObjectConnectionFactory",
					  null);
	ref.add(new javax.naming.StringRefAddr("queue.joramURL", url.toString()));
	return ref;
    }

    // use for jndi
    public void setQueueList(String s) {
	qList.put(s,this);
    }
    public static Queue getQueue(String s) {
	return (Queue) qList.get(s);
    }

    public String toString() {
	try {
	    return url.toString();
	} catch (Exception e) {
	    System.out.println("Queue Exception ");
	    e.printStackTrace();
	    return null;
	}
    }
}
