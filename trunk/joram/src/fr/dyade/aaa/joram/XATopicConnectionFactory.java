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
 * XATopicConnectionFactory allows an application program to create new
 * XATopicConnections to a JMS PTP proxy.
 */

public class XATopicConnectionFactory extends XAConnectionFactory implements javax.jms.XATopicConnectionFactory {

    /** Socket use to create New Topic (or delete Topic)*/
    protected Socket sock = null;
    /** ObjectOutputStream */
    protected ObjectOutputStream oos = null; 
    /** ObjectInputStream */
    protected ObjectInputStream ois = null;
    
    /**
     * Creates a new XATopicConnectionFactory
     * @param proxyAgentURLString the URL string of the JMS proxy agent
     */
    public XATopicConnectionFactory(String proxyAgentURLString) {
	super(proxyAgentURLString);
    }

    /**
     * Creates a new XATopicConnectionFactory
     * @param proxyAgentURL the URL of the JMS proxy agent
     */
    public XATopicConnectionFactory(URL proxyAgentURL) {
	super(proxyAgentURL);
    }

    /**
     * Constructs a new XATopicConnectionFactory.
     */
    public XATopicConnectionFactory(String agentClient, InetAddress addrProxy, int portProxy) {
	super(agentClient, addrProxy, portProxy);
    }


    /**
     * Create an XA topic connection with default user identity.
     */
    public javax.jms.XATopicConnection createXATopicConnection() throws JMSException {
	return (new XATopicConnection(proxyAgentIdString,
				      proxyAddress, proxyPort,
				      "anonymous", "anonymous"));
    }

    /**
     * Create an XA topic connection with specific user identity.
     */
    public javax.jms.XATopicConnection createXATopicConnection(String userName, String password) throws JMSException {
	return (new XATopicConnection(proxyAgentIdString,
				      proxyAddress, proxyPort,
				      userName, password));
    }

    /**
     * Create a topic connection with default user identity.
     */
    public javax.jms.TopicConnection createTopicConnection() throws JMSException {
	return (new TopicConnection(proxyAgentIdString,
				    proxyAddress, proxyPort,
				    "anonymous", "anonymous"));
    }


    /**
     * Create a topic connection with specified user identity.
     */
    public javax.jms.TopicConnection createTopicConnection(String userName, String password) throws JMSException {
	return (new TopicConnection(proxyAgentIdString,
				    proxyAddress, proxyPort,
				    userName,
				    password));
    }
    /** Create New Topic 
     * @see #delete(javax.jms.Topic)
     */
    public javax.jms.Topic createNewTopic () throws javax.jms.JMSException {
	try {
	    if (Debug.debug)
		if (Debug.admin)
		    System.out.println("->XATopicConnectionFactory : createNewTopic (Protocol=" + proxyAgentURL.getProtocol() +
				       ", Host=" + proxyAgentURL.getHost() +
				       ", Port=" + proxyAgentURL.getPort() +
				       ", AgentId=#" + proxyAgentURL.getRef() + ")");
	    
	    if ( proxyAgentURL.getProtocol().equals(fr.dyade.aaa.joram.ConfigURLStreamHandlerFactory.Joram) ) {
		sock = new Socket(proxyAddress, proxyPort);
		if ( sock != null ) {
		    sock.setTcpNoDelay(true);
		    sock.setSoTimeout(0);
		    sock.setSoLinger(true,1000);
		
		    /* send the name of the agentClient */
		    DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
		    dos.writeUTF(proxyAgentIdString);
		    dos.flush();
		    /* creation of the objectinputStream and ObjectOutputStream */
		    oos = new ObjectOutputStream(sock.getOutputStream());
		    ois = new ObjectInputStream(sock.getInputStream());
	    
		    fr.dyade.aaa.mom.MessageMOMExtern msgMOM = new fr.dyade.aaa.mom.MessageAdminCreateTopic(1, proxyAgentIdString,null);
		    if (oos != null) {
			oos.writeObject(msgMOM);
			oos.flush();
			oos.reset();
		    }
		    if (ois != null) {
			fr.dyade.aaa.mom.MessageAdminCreateTopic msg = (fr.dyade.aaa.mom.MessageAdminCreateTopic) ois.readObject();
			if (Debug.debug)
			    if (Debug.admin)
				System.out.println("<-XATopicConnectionFactory : createNewTopic  msg=" + msg.toString());
			oos.close();
			ois.close();
			sock.close();
			return new fr.dyade.aaa.joram.Topic(proxyAgentURL.getProtocol() +
							    "://" + proxyAgentURL.getHost() +
							    ":" + proxyAgentURL.getPort() +
							    "/" + msg.getTopicName());
		    } 
		}
	    }
	    oos.close();
	    ois.close();
	    sock.close();
	    return null;
	} catch (Exception exc) {
	    javax.jms.JMSException except = new javax.jms.JMSException("Exception=XATopicConnectionFactory : createNewTopic");
	    except.setLinkedException(exc);
	    throw(except);
	}
    }

    /** delete Topic */
    public void delete(javax.jms.Topic topic) throws javax.jms.JMSException {
	try {
	    if (Debug.debug)
		if (Debug.admin)
		    System.out.println("->XATopicConnectionFactory : delete  Topic" + topic.getTopicName());
	    if ( proxyAgentURL.getProtocol().equals(fr.dyade.aaa.joram.ConfigURLStreamHandlerFactory.Joram) ) {
		sock = new Socket(proxyAddress, proxyPort);
		if ( sock != null ) {
		    sock.setTcpNoDelay(true);
		    sock.setSoTimeout(0);
		    sock.setSoLinger(true,1000);

		    /* send the name of the agentClient */
		    DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
		    dos.writeUTF(proxyAgentIdString);
		    dos.flush();
		    
		    /* creation of the objectinputStream and ObjectOutputStream */
		    oos = new ObjectOutputStream(sock.getOutputStream());
		    ois = new ObjectInputStream(sock.getInputStream());

		    fr.dyade.aaa.mom.MessageMOMExtern msgMOM = new fr.dyade.aaa.mom.MessageAdminDeleteTopic(1, proxyAgentIdString,topic.getTopicName());
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
		    System.out.println("<-XATopicConnectionFactory : delete");
	} catch (Exception exc) {
	    javax.jms.JMSException except = new javax.jms.JMSException("Exception XATopicConnectionFactory : delete");
	    except.setLinkedException(exc);
	    throw(except);
	}
    }        

} // XATopicConnectionFactory
