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
  * @author      Richard Mathis / Nicolas Tachker
  * 
  * @see         javax.jms.QueueConnectionFactory 
  * @see         javax.jms.TopicConnectionFactory
  */ 
 
 
public class ConnectionFactory implements javax.jms.QueueConnectionFactory, javax.jms.TopicConnectionFactory, java.io.Serializable { 
  
    /** - addrProxy : host address where is TCP Proxy */ 
    private InetAddress addrProxy;  
    
    /** - portProxy	: lisent port TCP Proxy */ 
    private int portProxy; 
    
    /** the name of the agentClient in the mom */
    private String agentClient; 
    
    /* the security reference */	 
    private String login = "anonymous"; 
    private String passwd = "anonymous";
    
    /** Socket use to create New Topic or New Queue (or delete Queue/Topic)*/
    protected Socket sock = null;
    /** ObjectOutputStream */
    protected ObjectOutputStream oos = null; 
    /** ObjectInputStream */
    protected ObjectInputStream ois = null;
    
    /** URL for Joram naming */
    URL url = null;
	
    public ConnectionFactory(String agentClient, InetAddress addrProxy, int portProxy) { 
	this.agentClient = agentClient;
	this.addrProxy = addrProxy;
	this.portProxy = portProxy;
	try {
	    try {
		url = new URL(fr.dyade.aaa.joram.ConfigURLStreamHandlerFactory.Joram,
			      addrProxy.getHostName(),
			      portProxy,
			      "/" + agentClient);
	    } catch (Exception e) {
		if (e instanceof MalformedURLException) {
		    URL.setURLStreamHandlerFactory(new fr.dyade.aaa.joram.ConfigURLStreamHandlerFactory());
		    url = new URL(fr.dyade.aaa.joram.ConfigURLStreamHandlerFactory.Joram,
				  addrProxy.getHostName(),
				  portProxy,
				  "/" + agentClient);
		}
	    }
	} catch (Exception e) {
	    System.out.println("Jms ConnectionFactory");
	    e.printStackTrace();
	}
    }
    
    /** call ConnectionFactory like
     * ConnectionFactory("joram://host:port/#x.y.z")
     */
    public ConnectionFactory(String stringURL) {
	try {
	    try {
		url = new URL(stringURL);
	    } catch (Exception e) {
		if (e instanceof MalformedURLException) {
		    URL.setURLStreamHandlerFactory(new fr.dyade.aaa.joram.ConfigURLStreamHandlerFactory());
		    url = new URL(stringURL);
		}
	    }
	    if (Debug.debug)
		if (Debug.admin)
		    System.out.println("->ConnectionFactory (Protocol=" + url.getProtocol() +
				       ", Host=" + url.getHost() +
				       ", Port=" + url.getPort() +
				       ", AgentId=#" + url.getRef() + ")");
	    
	    if ( url.getProtocol().equals(fr.dyade.aaa.joram.ConfigURLStreamHandlerFactory.Joram) ) {
 		this.agentClient = "#" + url.getRef();
		this.addrProxy = InetAddress.getByName(url.getHost());
		this.portProxy = url.getPort();
	    }
	} catch (Exception e) {
	    System.out.println("ConnectionFactory Exception");
	    e.printStackTrace();
	}
    }

    /** constructor with Joram URL */
    public ConnectionFactory(URL url) {
	try {
	    if (Debug.debug)
		if (Debug.admin)
		    System.out.println("->ConnectionFactory (Protocol=" + url.getProtocol() +
				       ", Host=" + url.getHost() +
				       ", Port=" + url.getPort() +
				       ", AgentId=#" + url.getRef() + ")");

	    if ( url.getProtocol().equals(fr.dyade.aaa.joram.ConfigURLStreamHandlerFactory.Joram) ) {
		this.url = url;
 		this.agentClient = "#" + url.getRef();
		this.addrProxy = InetAddress.getByName(url.getHost());
		this.portProxy = url.getPort();
	    }
	} catch (Exception e) {
	    System.out.println("ConnectionFactory Exception");
	    e.printStackTrace();
	}
    }
    
    /** Create a queue connection with default user identity.
     *  @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications
     */
    public javax.jms.QueueConnection createQueueConnection() throws javax.jms.JMSException { 
	return(new fr.dyade.aaa.joram.Connection(agentClient, addrProxy, portProxy, login, passwd)); 
    } 
    
    /** Create a queue connection with specified user identity.
     *  @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
    public javax.jms.QueueConnection createQueueConnection(java.lang.String login, java.lang.String passwd) throws javax.jms.JMSException { 
	this.login = login; 
	this.passwd = passwd; 
	return this.createQueueConnection(); 
    } 
    
    /** Create a topic connection with default user identity.
     *  @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications
     */
    public javax.jms.TopicConnection createTopicConnection() throws javax.jms.JMSException {
	return(new fr.dyade.aaa.joram.Connection(agentClient, addrProxy, portProxy, login, passwd)); 
    } 
    
    /** Create a topic connection with specified user identity.
     *  @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
    public javax.jms.TopicConnection createTopicConnection(java.lang.String userName, java.lang.String password) throws javax.jms.JMSException { 
	this.login = login; 
	this.passwd = passwd; 
	return this.createTopicConnection(); 
    } 
    
    /** Create New Topic 
     * @see #delete(javax.jms.Topic)
     */
    public javax.jms.Topic createNewTopic () throws javax.jms.JMSException {
	try {
	    if (Debug.debug)
		if (Debug.admin)
		    System.out.println("->ConnectionFactory : createNewTopic (Protocol=" + url.getProtocol() +
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
				System.out.println("<-ConnectionFactory : createNewTopic  msg=" + msg.toString());
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
	    javax.jms.JMSException except = new javax.jms.JMSException("Exception=ConnectionFactory : createNewTopic");
	    except.setLinkedException(exc);
	    throw(except);
	}
    }

    /** delete Topic */
    public void delete(javax.jms.Topic topic) throws javax.jms.JMSException {
	try {
	    if (Debug.debug)
		if (Debug.admin)
		    System.out.println("->ConnectionFactory : delete  Topic" + topic.getTopicName());
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
		    System.out.println("<-ConnectionFactory : delete");
	} catch (Exception exc) {
	    javax.jms.JMSException except = new javax.jms.JMSException("Exception ConnectionFactory : delete");
	    except.setLinkedException(exc);
	    throw(except);
	}
    }
    

    /** Create New Queue 
     * @see #delete(javax.jms.Queue)
     */
    public javax.jms.Queue createNewQueue () throws javax.jms.JMSException {
	try {
	    if (Debug.debug)
		if (Debug.admin)
		    System.out.println("->ConnectionFactory : createNewQueue (Protocol=" + url.getProtocol() +
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
	    
		    fr.dyade.aaa.mom.MessageMOMExtern msgMOM = new fr.dyade.aaa.mom.MessageAdminCreateQueue(1, agentClient,null);
		    if (oos != null) {
			oos.writeObject(msgMOM);
			oos.flush();
			oos.reset();
		    }
		    if (ois != null) {
			fr.dyade.aaa.mom.MessageAdminCreateQueue msg = (fr.dyade.aaa.mom.MessageAdminCreateQueue) ois.readObject();
			if (Debug.debug)
			    if (Debug.admin)
				System.out.println("<-ConnectionFactory : createNewQueue  msg=" + msg.toString());
			oos.close();
			ois.close();
			sock.close();
			return new fr.dyade.aaa.joram.Queue(url.getProtocol() +
						       "://" + url.getHost() +
						       ":" + url.getPort() +
						       "/" + msg.getQueueName());
		    } 
		}
	    }
	    oos.close();
	    ois.close();
	    sock.close();
	    return null;
	} catch (Exception exc) {
	    javax.jms.JMSException except = new javax.jms.JMSException("Exception=ConnectionFactory : createNewQueue");
	    except.setLinkedException(exc);
	    throw(except);
	}
    }

    /** delete Queue */
    public void delete(javax.jms.Queue queue) throws javax.jms.JMSException {
	try {
	    if (Debug.debug)
		if (Debug.admin)
		    System.out.println("->ConnectionFactory : delete  Queue" + queue.getQueueName());
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

		    fr.dyade.aaa.mom.MessageMOMExtern msgMOM = new fr.dyade.aaa.mom.MessageAdminDeleteQueue(1, agentClient,queue.getQueueName());
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
		    System.out.println("<-ConnectionFactory : delete");
	} catch (Exception exc) {
	    javax.jms.JMSException except = new javax.jms.JMSException("Exception ConnectionFactory : delete");
	    except.setLinkedException(exc);
	    throw(except);
	}
    }

    public String getAgentClient() {
	return agentClient;
    }
    
    public String toString() {
	try {
	    return new URL(fr.dyade.aaa.joram.ConfigURLStreamHandlerFactory.Joram,
			   addrProxy.getHostName(),
			   portProxy,
			   "/" + agentClient).toString();
	} catch (Exception e) {
	    System.out.println("ConnectionFactory Exception ");
	    e.printStackTrace();
	    return null;
	}
    }
}
 
