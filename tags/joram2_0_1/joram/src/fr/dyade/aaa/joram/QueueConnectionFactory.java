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
 * A client uses a QueueConnectionFactory to create QueueConnections
 * with a JMS PTP provider.
 */
public class QueueConnectionFactory extends ConnectionFactory implements javax.jms.QueueConnectionFactory {
    
  /** Socket use to create New Queue (or delete Queue)*/
  protected Socket sock = null;
  /** ObjectOutputStream */
  protected ObjectOutputStream oos = null; 
  /** ObjectInputStream */
  protected ObjectInputStream ois = null;

  /**
   * Constructs a new QueueConnectionFactory.
   */
  public QueueConnectionFactory(String agentClient, InetAddress addrProxy, int portProxy) {
    super(agentClient, addrProxy, portProxy);
  }

  /**
   * Constructs a new QueueConnectionFactory.
   */
  public QueueConnectionFactory(String stringURL) {
    super(stringURL);
  }
        
  /**
   * Create a queue connection with default user identity.
   */
  public javax.jms.QueueConnection createQueueConnection() throws JMSException {
    return new QueueConnection(agentClient, addrProxy, portProxy, login, passwd);
  }

  /**
   * Create a queue connection with specified user identity.
   */
  public javax.jms.QueueConnection createQueueConnection(String userName, String password) throws JMSException {
    this.login = userName;
    this.passwd = password;
    return createQueueConnection();
  }

  /** Create New Queue 
   * @see #delete(javax.jms.Queue)
   */
  public javax.jms.Queue createNewQueue () throws javax.jms.JMSException {
    try {
      if (Debug.debug)
	if (Debug.admin)
	  System.out.println("->QueueConnectionFactory : createNewQueue (Protocol=" + url.getProtocol() +
			     ", Host=" + url.getHost() +
			     ", Port=" + url.getPort() +
			     ", AgentId=" + url.getAgentId() + ")");

      if ( url.getProtocol().equals("joram") ) {
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
		System.out.println("<-QueueConnectionFactory : createNewQueue  msg=" + msg.toString());

	    return new fr.dyade.aaa.joram.Queue(url.getProtocol() +
						"://" + url.getHost() +
						":" + url.getPort() +
						"/" + msg.getQueueName());
	  } 
	}
      }

      return null;
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("Exception=QueueConnectionFactory : createNewQueue");
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

  /** Create New specific Queue 
   * your Class className must extends fr.dyade.aaa.mom.Queue
   * @see #delete(javax.jms.Queue)
   */
  public javax.jms.Queue createNewSpecificQueue (String className) throws javax.jms.JMSException {
    try {
      if (Debug.debug)
	if (Debug.admin)
	  System.out.println("->QueueConnectionFactory : createNewSpecificQueue (Protocol=" + url.getProtocol() +
			     ", Host=" + url.getHost() +
			     ", Port=" + url.getPort() +
			     ", AgentId=" + url.getAgentId() + ")");

      if ( url.getProtocol().equals("joram") ) {
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
	    
	  fr.dyade.aaa.mom.MessageMOMExtern msgMOM = new fr.dyade.aaa.mom.MessageAdminCreateSpecific(1, agentClient,className);
	  if (oos != null) {
	    oos.writeObject(msgMOM);
	    oos.flush();
	    oos.reset();
	  }
	  if (ois != null) {
	    fr.dyade.aaa.mom.MessageAdminCreateSpecific msg = (fr.dyade.aaa.mom.MessageAdminCreateSpecific) ois.readObject();
	    if (Debug.debug)
	      if (Debug.admin)
		System.out.println("<-QueueConnectionFactory : createNewSpecificQueue  msg=" + msg.toString());

	    return new fr.dyade.aaa.joram.Queue(url.getProtocol() +
						"://" + url.getHost() +
						":" + url.getPort() +
						"/" + msg.getID());
	  } 
	}
      }

      return null;
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("Exception=QueueConnectionFactory : createNewSpecificQueue");
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
      if (Debug.debug)
	if (Debug.admin)
	  System.out.println("->QueueConnectionFactory : delete  Queue" + queue.getQueueName());
      if ( url.getProtocol().equals("joram") ) {
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

	}
      }
      if ((Debug.debug) && (Debug.admin))
	System.out.println("<-QueueConnectionFactory : delete");
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("Exception QueueConnectionFactory : delete");
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

} // QueueConnectionFactory
