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
 *	a Queue is an object associated with a real Queue
 *	in the MOM, it's its name
 * 
 *	@see subclasses
 *	@see javax.jms.Queue
 *	@see fr.dyade.aaa.mom.Queue 
 */ 
 
public class QueueNaming extends fr.dyade.aaa.mom.DestinationNaming implements javax.jms.Queue { 

    /* constructor of the QueueNaming */
    public QueueNaming(String nameQueue) {
	super(nameQueue);
    }

    /** for this time, the name of the Queue is the name of the agent Queue.
     *	later it will be a symbolic name
     */
    public String getQueueName() {
	return dest;
    }
    
    /* @see JMS specifications */
    public String toString() {
	return  dest;
    }

    /* method equals */
    public boolean equals(Object obj) {
	if(obj instanceof QueueNaming) {
	    QueueNaming key = (QueueNaming) obj;
	    return (dest.equals(key.dest));
	} else	
	    return false;
    }
    
    public int hashCode() {
	return dest.hashCode();
    }
}
