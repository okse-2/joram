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
 * XAQueueConnectionFactory allows an application program to create new
 * XAQueueConnections to a JMS PTP proxy.
 */
public class XAQueueConnectionFactory extends XAConnectionFactory implements javax.jms.XAQueueConnectionFactory {
    
  /** Socket use to create New Queue (or delete Queue)*/
  protected Socket sock = null;
  /** ObjectOutputStream */
  protected ObjectOutputStream oos = null; 
  /** ObjectInputStream */
  protected ObjectInputStream ois = null;

  /**
   * Creates a new XAQueueConnectionFactory.
   * @param proxyAgentURLString the URL string of the JMS proxy agent
   */
  public XAQueueConnectionFactory(String proxyAgentURLString) {
    super(proxyAgentURLString);
  }

  /**
   * Constructs a new QueueConnectionFactory.
   */
  public XAQueueConnectionFactory(String agentClient, InetAddress addrProxy, int portProxy) {
    super(agentClient, addrProxy, portProxy);
  }


  /**
   * Create an XA queue connection with default user identity.
   */
  public javax.jms.XAQueueConnection createXAQueueConnection() throws JMSException {
    return (new XAQueueConnection(proxyAgentIdString,
				  proxyAddress, proxyPort,
				  "anonymous", "anonymous"));
  }
    
  /**
   * Create an XA queue connection with specific user identity.
   */
  public javax.jms.XAQueueConnection createXAQueueConnection(String userName, String password) throws JMSException {
    return (new XAQueueConnection(proxyAgentIdString,
				  proxyAddress, proxyPort,
				  userName, password));
  }

  /*
   * Create a queue connection with default user identity.
   */
  public javax.jms.QueueConnection createQueueConnection() throws JMSException {
    return (new QueueConnection(proxyAgentIdString,
				proxyAddress, proxyPort,
				"anonymous", "anonymous"));
  }

  /*
   * Create a queue connection with specified user identity.
   */
  public javax.jms.QueueConnection createQueueConnection(String userName, String password) throws JMSException {
    return (new QueueConnection(proxyAgentIdString,
				proxyAddress, proxyPort,
				userName,
				password));
  }
  /** Create New Queue 
   * @see #delete(javax.jms.Queue)
   */
  public javax.jms.Queue createNewQueue () throws javax.jms.JMSException {
    try {
      if (Debug.debug)
	if (Debug.admin)
	  System.out.println("->XAQueueConnectionFactory : createNewQueue (Protocol=" + proxyAgentURL.getProtocol() +
			     ", Host=" + proxyAgentURL.getHost() +
			     ", Port=" + proxyAgentURL.getPort() +
			     ", AgentId=" + proxyAgentURL.getAgentId() + ")");

      if ( proxyAgentURL.getProtocol().equals("joram") ) {
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
	    
	  fr.dyade.aaa.mom.MessageMOMExtern msgMOM = new fr.dyade.aaa.mom.MessageAdminCreateQueue(1, proxyAgentIdString,null);
	  if (oos != null) {
	    oos.writeObject(msgMOM);
	    oos.flush();
	    oos.reset();
	  }
	  if (ois != null) {
	    fr.dyade.aaa.mom.MessageAdminCreateQueue msg = (fr.dyade.aaa.mom.MessageAdminCreateQueue) ois.readObject();
	    if (Debug.debug)
	      if (Debug.admin)
		System.out.println("<-XAQueueConnectionFactory : createNewQueue  msg=" + msg.toString());

	    return new fr.dyade.aaa.joram.Queue(proxyAgentURL.getProtocol() +
						"://" + proxyAgentURL.getHost() +
						":" + proxyAgentURL.getPort() +
						"/" + msg.getQueueName());
	  } 
	}
      }

      return null;
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("Exception=XAQueueConnectionFactory : createNewQueue");
      except.setLinkedException(exc);
      throw(except);
    } finally {
      try {
	oos.close();
      } catch (IOException exc) {}
      try {
	ois.close();
      } catch (IOException exc) {}
      try {
	sock.close();
      } catch (IOException exc) {}
    }
  }

  /** delete Queue */
  public void delete(javax.jms.Queue queue) throws javax.jms.JMSException {
    try {
      if ((Debug.debug) && (Debug.admin))
	System.out.println("->XAQueueConnectionFactory : delete  Queue" + queue.getQueueName());
      if ( proxyAgentURL.getProtocol().equals("joram") ) {
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

	  fr.dyade.aaa.mom.MessageMOMExtern msgMOM = new fr.dyade.aaa.mom.MessageAdminDeleteQueue(1, proxyAgentIdString, queue.getQueueName());
	  if (oos != null) {
	    oos.writeObject(msgMOM);
	    oos.flush();
	    oos.reset();
	  }
	}
      }
      if ((Debug.debug) && (Debug.admin))
	System.out.println("<-XAQueueConnectionFactory : delete");
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("Exception XAQueueConnectionFactory : delete");
      except.setLinkedException(exc);
      throw(except);
    } finally {
      try {
	oos.close();
      } catch (IOException exc) {}
      try {
	ois.close();
      } catch (IOException exc) {}
      try {
	sock.close();
      } catch (IOException exc) {}
    }
  } 

} // XAQueueConnectionFactory
