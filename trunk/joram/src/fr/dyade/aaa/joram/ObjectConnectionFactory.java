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
import java.io.*;
import java.net.*; 
  
/** 
  * @author       Nicolas Tachker
  * 
  * @see         javax.naming.spi.ObjectFactory
  */ 
 
 
public class ObjectConnectionFactory implements javax.naming.spi.ObjectFactory {

    public Object getObjectInstance(Object refObj, javax.naming.Name name, javax.naming.Context nameCtx, java.util.Hashtable env) throws Exception {
	javax.naming.Reference ref = (javax.naming.Reference) refObj;
	if (ref.getClassName().equals("fr.dyade.aaa.joram.XATopicConnectionFactory")) {
	    return new XATopicConnectionFactory((String)ref.get("xacnxfactory.joramURL").getContent());
	} else 	if (ref.getClassName().equals("fr.dyade.aaa.joram.XAQueueConnectionFactory")) {
	    return new XAQueueConnectionFactory((String)ref.get("xacnxfactory.joramURL").getContent());
	} else 	if (ref.getClassName().equals("fr.dyade.aaa.joram.TopicConnectionFactory")) {
	    return new  TopicConnectionFactory((String)ref.get("cnxfactory.joramURL").getContent());
	} else 	if (ref.getClassName().equals("fr.dyade.aaa.joram.QueueConnectionFactory")) {
	    return new  QueueConnectionFactory((String)ref.get("cnxfactory.joramURL").getContent());
	} else if (ref.getClassName().equals("fr.dyade.aaa.joram.Topic")) {
	    return new Topic((String)ref.get("topic.joramURL").getContent());
	} else if (ref.getClassName().equals("fr.dyade.aaa.joram.Queue")) {
	    return new Queue((String)ref.get("queue.joramURL").getContent());
	} else {
	    return null;
	}
    }    
}
 
