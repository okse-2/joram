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
import java.io.*;
import javax.jms.*;


/**
 * A client uses a TopicConnectionFactory to create TopicConnections
 * with a JMS Pub/Sub provider.
 */

public class TopicConnectionFactory extends ConnectionFactory implements javax.jms.TopicConnectionFactory {
    
    /** Socket use to create New Topic (or delete Topic)*/
    protected Socket sock = null;
    /** ObjectOutputStream */
    protected ObjectOutputStream oos = null; 
    /** ObjectInputStream */
    protected ObjectInputStream ois = null;

    /**
     * Constructs a new TopicConnectionFactory.
     */
    public TopicConnectionFactory(String agentClient, InetAddress addrProxy, int portProxy) {
	super(agentClient, addrProxy, portProxy);
    }

    /**
     * Constructs a new TopicConnectionFactory.
     */
    public TopicConnectionFactory(String stringURL) {
	super(stringURL);
    }

    /**
     * Constructs a new TopicConnectionFactory.
     */
    public TopicConnectionFactory(URL url) {
	super(url);
    }


    /**
     * Create a topic connection with default user identity.
     */
    public javax.jms.TopicConnection createTopicConnection() throws JMSException {
	return new TopicConnection(agentClient, addrProxy, portProxy, login, passwd);
    }
    
    
    /**
     * Create a topic connection with specified user identity.
     */
    public javax.jms.TopicConnection createTopicConnection(String userName, String password) throws JMSException {
	this.login = userName;
	this.passwd = password;
	return createTopicConnection();
    }

    /** Create New Topic 
     * @see #delete(javax.jms.Topic)
     */
    public javax.jms.Topic createNewTopic () throws javax.jms.JMSException {
	try {
	    if (Debug.debug)
		if (Debug.admin)
		    System.out.println("->TopicConnectionFactory : createNewTopic (Protocol=" + url.getProtocol() +
				       ", Host=" + url.getHost() +
				       ", Port=" + url.getPort() +
				       ", AgentId=#" + url.getRef() + ")");
	    
	    if ( url.getProtocol().equals(fr.dyade.aaa.joram.ConfigURLStreamHandlerFactory.Joram) ) {
		sock = new Socket(addrProxy, portProxy);
		if ( sock != null ) {
		    sock.setTcpNoDelay(true);
		    sock.setSoTimeout(0);
		    sock.setSoLinger(true,1000);
		
		    /* send the name of the agentClient */
		    DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
		    dos.writeUTF(agentClient);
		    dos.flush();
		    /* creation of the objectinputStream and ObjectOutputStream */
		    oos = new ObjectOutputStream(sock.getOutputStream());
		    ois = new ObjectInputStream(sock.getInputStream());
	    
		    fr.dyade.aaa.mom.MessageMOMExtern msgMOM = new fr.dyade.aaa.mom.MessageAdminCreateTopic(1, agentClient,null);
		    if (oos != null) {
			oos.writeObject(msgMOM);
			oos.flush();
			oos.reset();
		    }
		    if (ois != null) {
			fr.dyade.aaa.mom.MessageAdminCreateTopic msg = (fr.dyade.aaa.mom.MessageAdminCreateTopic) ois.readObject();
			if (Debug.debug)
			    if (Debug.admin)
				System.out.println("<-TopicConnectionFactory : createNewTopic  msg=" + msg.toString());
			oos.close();
			ois.close();
			sock.close();
			return new fr.dyade.aaa.joram.Topic(url.getProtocol() +
							    "://" + url.getHost() +
							    ":" + url.getPort() +
							    "/" + msg.getTopicName());
		    } 
		}
	    }
	    oos.close();
	    ois.close();
	    sock.close();
	    return null;
	} catch (Exception exc) {
	    javax.jms.JMSException except = new javax.jms.JMSException("Exception=TopicConnectionFactory : createNewTopic");
	    except.setLinkedException(exc);
	    throw(except);
	}
    }

    /** delete Topic */
    public void delete(javax.jms.Topic topic) throws javax.jms.JMSException {
	try {
	    if (Debug.debug)
		if (Debug.admin)
		    System.out.println("->TopicConnectionFactory : delete  Topic" + topic.getTopicName());
	    if ( url.getProtocol().equals(fr.dyade.aaa.joram.ConfigURLStreamHandlerFactory.Joram) ) {
		sock = new Socket(addrProxy, portProxy);
		if ( sock != null ) {
		    sock.setTcpNoDelay(true);
		    sock.setSoTimeout(0);
		    sock.setSoLinger(true,1000);

		    /* send the name of the agentClient */
		    DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
		    dos.writeUTF(agentClient);
		    dos.flush();
		    
		    /* creation of the objectinputStream and ObjectOutputStream */
		    oos = new ObjectOutputStream(sock.getOutputStream());
		    ois = new ObjectInputStream(sock.getInputStream());

		    fr.dyade.aaa.mom.MessageMOMExtern msgMOM = new fr.dyade.aaa.mom.MessageAdminDeleteTopic(1, agentClient,topic.getTopicName());
		    if (oos != null) {
			oos.writeObject(msgMOM);
			oos.flush();
			oos.reset();
		    }
		    oos.close();
		    ois.close();
		    sock.close();
		}
	    }
	    if (Debug.debug)
		if (Debug.admin)
		    System.out.println("<-TopicConnectionFactory : delete");
	} catch (Exception exc) {
	    javax.jms.JMSException except = new javax.jms.JMSException("Exception TopicConnectionFactory : delete");
	    except.setLinkedException(exc);
	    throw(except);
	}
    }        

} // TopicConnectionFactory
