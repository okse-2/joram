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
import java.lang.*;
import java.util.*;
import javax.jms.*;

/**
 * Admin can create or delete a fr.dyade.aaa.joram.ConnectionFactory
 * a ConnectionFacory is a AgentClient in the mom
 * <p>
 *  sample:
 * <dd>
 *    <dt>// Connect a default Joram server (joram://localhost:16010)<dd>
 *    <dt>fr.dyade.aaa.joram.Admin admin = new fr.dyade.aaa.joram.Admin(); <dd>
 *    <dt>// Create a Joram client
 *    <dt>fr.dyade.aaa.joram.ConnectionFactory sender = admin.createAgentClient();<dd>
 *    <dt>admin.close();
 * <p>
 * 
 * 
 * @author      Nicolas Tachker
 * 
 * @see         fr.dyade.aaa.joram.ConnectionFactory
 * @see         fr.dyade.aaa.mom.AgentClient
 * @see         fr.dyade.aaa.mom.CommonClientAAA
 * @see         javax.jms.QueueConnectionFactory
 * @see         javax.jms.TopicConnectionFactory
 */

public class Admin {
  /** host adress */
  private InetAddress addr;
  /** remote port */ 
  private int port;

  /** Joram URL */
  private URL url;
	
  /** the security reference */	 
  private String login = "anonymous"; 
  private String passwd = "anonymous";
  
  /** Socket */
  protected Socket sock = null;
  /** ObjectOutputStream */
  protected ObjectOutputStream oos = null;
  /** ObjectInputStream */
  protected ObjectInputStream ois = null;
  
  /** 
   * Admin("joram://hostname:port/")
   */
  public Admin (String stringURL) throws JMSException {
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
	  System.out.println("->Admin : (Protocol=" + url.getProtocol() +
			     ", Host=" + url.getHost() +
			     ", Port=" + url.getPort() + ")");
      
      if ( url.getProtocol().equals(fr.dyade.aaa.joram.ConfigURLStreamHandlerFactory.Joram) ) {
	this.addr = java.net.InetAddress.getByName(url.getHost());
	this.port = url.getPort();
      }
    } catch (Exception exc) {
      System.out.println("Admin error");
    }
  }
  
  /** constructor with default args 
   * host=localhost, port=16010
   */
  public Admin() throws Exception {
    this("joram://"+ InetAddress.getLocalHost().getHostName()+":16010/");
  }
  
  /** constructor with Joram URL */
  public Admin (URL url) throws JMSException {
    try {
      if (Debug.debug)
	if (Debug.admin)
	  System.out.println("->Admin : (Protocol=" + url.getProtocol() +
			     ", Host=" + url.getHost() +
			     ", Port=" + url.getPort() + ")");
      
      if ( url.getProtocol().equals(fr.dyade.aaa.joram.ConfigURLStreamHandlerFactory.Joram) ) {
	this.url = url;
	this.addr = java.net.InetAddress.getByName(url.getHost());
	this.port = url.getPort();
      }
    } catch (Exception exc) {
      System.out.println("Admin error");
    }
  }
  

  /** create Topic agent client 
   * @see #delete(fr.dyade.aaa.joram.ConnectionFactory)
   */
  public fr.dyade.aaa.joram.TopicConnectionFactory createTopicAgentClient () throws Exception {
    sock = new Socket(addr, port);
    if ( sock != null ) {
      sock.setTcpNoDelay(true);
      sock.setSoTimeout(0);
      sock.setSoLinger(true,1000);
      DataOutputStream dos = new DataOutputStream(sock.getOutputStream()); 
      dos.writeUTF("NewAgentClient");
      dos.flush();
      ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
      ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
      fr.dyade.aaa.mom.MessageMOMExtern msgMOM = new fr.dyade.aaa.mom.MessageAdminGetAgentClient(1);
      oos.writeObject(msgMOM);
      oos.flush();
      oos.reset();
      msgMOM = (fr.dyade.aaa.mom.MessageAdminGetAgentClient) ois.readObject();
      String agentClient = ((fr.dyade.aaa.mom.MessageAdminGetAgentClient) msgMOM).getAgent();
      // stop Driver in MOM
      msgMOM = new fr.dyade.aaa.mom.MessageAdminCleanDriver(1);
      oos.writeObject(msgMOM);
      oos.flush();
      oos.reset();
      sock.close();
      return new fr.dyade.aaa.joram.TopicConnectionFactory(agentClient,addr,port);
    } else 
      return null;
  }
 
  /** create Queue agent client 
   * @see #delete(fr.dyade.aaa.joram.ConnectionFactory)
   */
  public fr.dyade.aaa.joram.QueueConnectionFactory createQueueAgentClient () throws Exception {
    sock = new Socket(addr, port);
    if ( sock != null ) {
      sock.setTcpNoDelay(true);
      sock.setSoTimeout(0);
      sock.setSoLinger(true,1000);
      DataOutputStream dos = new DataOutputStream(sock.getOutputStream()); 
      dos.writeUTF("NewAgentClient");
      dos.flush();
      ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
      ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
      fr.dyade.aaa.mom.MessageMOMExtern msgMOM = new fr.dyade.aaa.mom.MessageAdminGetAgentClient(1);
      oos.writeObject(msgMOM);
      oos.flush();
      oos.reset();
      msgMOM = (fr.dyade.aaa.mom.MessageAdminGetAgentClient) ois.readObject();
      String agentClient = ((fr.dyade.aaa.mom.MessageAdminGetAgentClient) msgMOM).getAgent();
      // stop Driver in MOM
      msgMOM = new fr.dyade.aaa.mom.MessageAdminCleanDriver(1);
      oos.writeObject(msgMOM);
      oos.flush();
      oos.reset();
      sock.close();
      return new fr.dyade.aaa.joram.QueueConnectionFactory(agentClient,addr,port);
    } else 
      return null;
  }
  
  /** create XATopic agent client 
   * @see #delete(fr.dyade.aaa.joram.ConnectionFactory)
   */
  public fr.dyade.aaa.joram.XATopicConnectionFactory createXATopicAgentClient () throws Exception {
    sock = new Socket(addr, port);
    if ( sock != null ) {
      sock.setTcpNoDelay(true);
      sock.setSoTimeout(0);
      sock.setSoLinger(true,1000);
      DataOutputStream dos = new DataOutputStream(sock.getOutputStream()); 
      dos.writeUTF("NewAgentClient");
      dos.flush();
      ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
      ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
      fr.dyade.aaa.mom.MessageMOMExtern msgMOM = new fr.dyade.aaa.mom.MessageAdminGetAgentClient(1);
      oos.writeObject(msgMOM);
      oos.flush();
      oos.reset();
      msgMOM = (fr.dyade.aaa.mom.MessageAdminGetAgentClient) ois.readObject();
      String agentClient = ((fr.dyade.aaa.mom.MessageAdminGetAgentClient) msgMOM).getAgent();
      // stop Driver in MOM
      msgMOM = new fr.dyade.aaa.mom.MessageAdminCleanDriver(1);
      oos.writeObject(msgMOM);
      oos.flush();
      oos.reset();
      sock.close();
      return new fr.dyade.aaa.joram.XATopicConnectionFactory(agentClient,addr,port);
    } else 
      return null;
  }
  
  /** create XAQueue agent client 
   * @see #delete(fr.dyade.aaa.joram.ConnectionFactory)
   */
  public fr.dyade.aaa.joram.XAQueueConnectionFactory createXAQueueAgentClient () throws Exception {
    sock = new Socket(addr, port);
    if ( sock != null ) {
      sock.setTcpNoDelay(true);
      sock.setSoTimeout(0);
      sock.setSoLinger(true,1000);
      DataOutputStream dos = new DataOutputStream(sock.getOutputStream()); 
      dos.writeUTF("NewAgentClient");
      dos.flush();
      ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
      ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
      fr.dyade.aaa.mom.MessageMOMExtern msgMOM = new fr.dyade.aaa.mom.MessageAdminGetAgentClient(1);
      oos.writeObject(msgMOM);
      oos.flush();
      oos.reset();
      msgMOM = (fr.dyade.aaa.mom.MessageAdminGetAgentClient) ois.readObject();
      String agentClient = ((fr.dyade.aaa.mom.MessageAdminGetAgentClient) msgMOM).getAgent();
      // stop Driver in MOM
      msgMOM = new fr.dyade.aaa.mom.MessageAdminCleanDriver(1);
      oos.writeObject(msgMOM);
      oos.flush();
      oos.reset();
      sock.close();
      return new fr.dyade.aaa.joram.XAQueueConnectionFactory(agentClient,addr,port);
    } else 
      return null;
  }
   
  /** delete agent client */
  public void delete(fr.dyade.aaa.joram.ConnectionFactory cf) throws Exception {
    String agentClient = cf.getAgentClient();
    sock = new Socket(addr, port);
    if ( sock != null ) {
      sock.setTcpNoDelay(true);
      sock.setSoTimeout(0);
      sock.setSoLinger(true,1000);
      DataOutputStream dos = new DataOutputStream(sock.getOutputStream()); 
      dos.writeUTF(agentClient);
      dos.flush();
      ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
      ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
      fr.dyade.aaa.mom.MessageMOMExtern msgMOM = new fr.dyade.aaa.mom.MessageAdminDeleteAgentClient(1,agentClient);
      oos.writeObject(msgMOM);
      oos.flush();
      oos.reset();
      sock.close();
    }
  }
  
  /**
   * close ObjectOutputStream, ObjectInputStream and Socket
   */
  public void close() throws JMSException {
    try {
      if (oos != null) {
	oos.close();
	oos = null;
      }
      if (ois != null) {
	ois.close();
	ois = null;
      }
      if ( sock != null ) {
	sock.close();
	sock = null;
      }
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    }
  }
  
  public String toString() {
    return "Socket=" +sock.toString() + ", Client=" + url.toString() + ", login=" + login + ", passwd=" + passwd;
  }
}
