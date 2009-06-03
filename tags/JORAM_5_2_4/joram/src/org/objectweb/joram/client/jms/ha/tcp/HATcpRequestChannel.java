/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2009 ScalAgent Distributed Technologies
 * Copyright (C) 2004 Bull SA
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 *
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s):
 */
package org.objectweb.joram.client.jms.ha.tcp;

import java.util.StringTokenizer;
import java.util.Timer;

import javax.jms.JMSException;
import javax.jms.JMSSecurityException;

import org.objectweb.joram.client.jms.FactoryParameters;
import org.objectweb.joram.client.jms.connection.RequestChannel;
import org.objectweb.joram.client.jms.tcp.ReliableTcpClient;
import org.objectweb.joram.shared.client.AbstractJmsReply;
import org.objectweb.joram.shared.client.AbstractJmsRequest;
import org.objectweb.joram.shared.security.Identity;

public class HATcpRequestChannel implements RequestChannel {

  private ReliableTcpClient tcpClient;

  /**
   * Creates a <code>HATcpConnection</code> instance.
   *
   * @param params  Factory parameters.
   * @param identity
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not reachable.
   */
  public HATcpRequestChannel(String url,
                         FactoryParameters params, 
                         Identity identity) 
    throws JMSException {
    this(url,
         params, 
         identity,
         "org.objectweb.joram.client.jms.tcp.ReliableTcpClient");
  }
  
  public HATcpRequestChannel() {
    super();
  }
  
  /**
   * Creates a <code>HATcpConnection</code> instance.
   *
   * @param params  Factory parameters.
   * @param identity
   * @param reliableClass  reliable class name.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not reachable.
   */
  public HATcpRequestChannel(String url,
                         FactoryParameters params, 
                         Identity identity,
                         String reliableClass) throws JMSException {
    try {
      tcpClient = (ReliableTcpClient) Class.forName(reliableClass).newInstance(); 
    } catch (ClassNotFoundException exc) {
      JMSException jmsExc = new JMSException("HATcpConnection: ClassNotFoundException : " + reliableClass);
      jmsExc.setLinkedException(exc);
      throw jmsExc;
    } catch (InstantiationException exc) {
      JMSException jmsExc = new JMSException("HATcpConnection: InstantiationException : " + reliableClass);
      jmsExc.setLinkedException(exc);
      throw jmsExc;
    } catch (IllegalAccessException exc) {
      JMSException jmsExc = new JMSException("HATcpConnection: IllegalAccessException : " + reliableClass);
      jmsExc.setLinkedException(exc);
      throw jmsExc;
    }
    tcpClient.init(params, identity, true);
    
    StringTokenizer tokenizer = new StringTokenizer(url, "/:,");
    if (! tokenizer.hasMoreElements()) 
      throw new javax.jms.JMSException("URL not valid:" + url);
    String protocol = tokenizer.nextToken();        
    if (protocol.equals("hajoram")) {
      while (tokenizer.hasMoreElements()) {
        tcpClient.addServerAddress(tokenizer.nextToken(), Integer.parseInt(tokenizer.nextToken()));
      }
    } else {
      throw new javax.jms.JMSException("Unknown protocol:" + protocol);
    }
  }
  
  public void setTimer(Timer timer) {
    tcpClient.setTimer(timer);
  }
  
  public void connect() throws Exception {
    tcpClient.connect();
  }

  /**
   * Sending a JMS request through the TCP connection.
   */
  public void send(AbstractJmsRequest request) throws Exception {
    tcpClient.send(request);
  }

  public AbstractJmsReply receive() throws Exception {
    return (AbstractJmsReply)tcpClient.receive();
  }

  /** Closes the TCP connection. */
  public void close() {
    tcpClient.close();
  }

  public void closing() {
    tcpClient.stopReconnections();
  }
}
