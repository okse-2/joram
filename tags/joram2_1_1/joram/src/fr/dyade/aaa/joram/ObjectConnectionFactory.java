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
	    //XATopicConnectionFactory
	    String xatcfName = (String)ref.get("xacnxfactory.joramURL").getContent();
	    XATopicConnectionFactory xatcf = (XATopicConnectionFactory) XAConnectionFactory.getXAConnectionFactory(xatcfName);
	    if(xatcf==null) {
		xatcf = new XATopicConnectionFactory(xatcfName);
		xatcf.setXAConnectionFactoryList(xatcfName);
	    }
	    return xatcf;

	} else 	if (ref.getClassName().equals("fr.dyade.aaa.joram.XAQueueConnectionFactory")) {
	    //XAQueueConnectionFactory
	    String xaqcfName = (String)ref.get("xacnxfactory.joramURL").getContent();
	    XAQueueConnectionFactory xaqcf = (XAQueueConnectionFactory) XAConnectionFactory.getXAConnectionFactory(xaqcfName);
	    if(xaqcf==null) {
		xaqcf = new XAQueueConnectionFactory(xaqcfName);
		xaqcf.setXAConnectionFactoryList(xaqcfName);
	    }
	    return xaqcf;
	} else 	if (ref.getClassName().equals("fr.dyade.aaa.joram.TopicConnectionFactory")) {
	    //TopicConnectionFactory
	    String tcfName = (String)ref.get("cnxfactory.joramURL").getContent();
	    TopicConnectionFactory tcf = (TopicConnectionFactory) ConnectionFactory.getConnectionFactory(tcfName);
	    if(tcf==null) {
		tcf = new  TopicConnectionFactory(tcfName);
		tcf.setConnectionFactoryList(tcfName);
	    }
	    return tcf;
	} else 	if (ref.getClassName().equals("fr.dyade.aaa.joram.QueueConnectionFactory")) {
	    //QueueConnectionFactory
	    String qcfName = (String)ref.get("cnxfactory.joramURL").getContent();
	    QueueConnectionFactory qcf = (QueueConnectionFactory) ConnectionFactory.getConnectionFactory(qcfName);
	    if(qcf==null) {
		qcf = new  QueueConnectionFactory(qcfName);
		qcf.setConnectionFactoryList(qcfName);
	    }
	    return qcf;
	} else if (ref.getClassName().equals("fr.dyade.aaa.joram.Topic")) {
	    //Topic
	    String topic = (String)ref.get("topic.joramURL").getContent();
	    Topic t = Topic.getTopic(topic);
	    if (t==null) {
		t = new Topic(topic);
		t.setTopicList(topic);
	    }
	    return t;
	} else if (ref.getClassName().equals("fr.dyade.aaa.joram.Queue")) {
	    //Queue
	    String queue = (String)ref.get("queue.joramURL").getContent();
	    Queue q = Queue.getQueue(queue);
	    if (q==null) {
		q = new Queue(queue);
		q.setQueueList(queue);
	    }
	    return q;
	} else {
	    return null;
	}
    }    
}
 
