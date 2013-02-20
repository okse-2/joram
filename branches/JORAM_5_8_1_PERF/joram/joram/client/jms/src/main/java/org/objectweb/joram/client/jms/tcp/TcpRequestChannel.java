/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2009 ScalAgent Distributed Technologies
 * Copyright (C) 2004 France Telecom R&D
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.client.jms.tcp;

import java.util.Timer;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;

import org.objectweb.joram.client.jms.FactoryParameters;
import org.objectweb.joram.client.jms.connection.RequestChannel;
import org.objectweb.joram.shared.client.AbstractJmsReply;
import org.objectweb.joram.shared.client.AbstractJmsRequest;
import org.objectweb.joram.shared.security.Identity;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * A <code>TcpConnection</code> links a Joram client and a Joram platform
 * with a TCP socket.
 * <p>
 * Requests and replies travel through the socket after serialization.
 */
public class TcpRequestChannel implements RequestChannel { 

  private static Logger logger = Debug.getLogger(TcpRequestChannel.class.getName());

  private ReliableTcpClient tcpClient = null;
  
  private Identity identity = null;

  /**
   * Creates a <code>TcpConnection</code> instance.
   *
   * @param params  Factory parameters.
   * @param identity
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not reachable.
   */
  public TcpRequestChannel(FactoryParameters params, 
                           Identity identity) throws JMSException {
    this(params, identity, "org.objectweb.joram.client.jms.tcp.ReliableTcpClient");
  }

  /**
   * Creates a <code>TcpConnection</code> instance.
   *
   * @param params  Factory parameters.
   * @param identity 
   * @param reliableClass  reliable class name.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not reachable.
   */
  public TcpRequestChannel(FactoryParameters params, 
                           Identity identity,
                           String reliableClass) throws JMSException {

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "TcpConnection.<init>(" + params + ',' + identity + ',' + reliableClass +')');

    if (reliableClass == null || reliableClass.equals("") || reliableClass.length() < 1) {
      reliableClass = "org.objectweb.joram.client.jms.tcp.ReliableTcpClient";
    }
    try {
      tcpClient = (ReliableTcpClient) Class.forName(reliableClass).newInstance(); 
    } catch (ClassNotFoundException exc) {
      JMSException jmsExc =  new JMSException("TcpConnection: ClassNotFoundException : " + reliableClass);
      jmsExc.setLinkedException(exc);
      throw jmsExc;
    } catch (InstantiationException exc) {
      JMSException jmsExc =  new JMSException("TcpConnection: InstantiationException : " + reliableClass);
      jmsExc.setLinkedException(exc);
      throw jmsExc;
    } catch (IllegalAccessException exc) {
      JMSException jmsExc = new JMSException("TcpConnection: IllegalAccessException : " + reliableClass);
      jmsExc.setLinkedException(exc);
      throw jmsExc;
    }
    tcpClient.init(params, identity, params.cnxPendingTimer > 0);
    tcpClient.addServerAddress(params.getHost(), params.getPort());
    this.identity = identity;
  }
  
  //JORAM_PERF_BRANCH
  public int size() {
    return tcpClient.size();
  }

  public void setTimer(Timer timer) {
    tcpClient.setTimer(timer);
  }

  public void connect() throws Exception {
    tcpClient.connect();
  }

  /**
   * Sending a JMS request through the TCP connection.
   *
   * @exception IllegalStateException  If the connection is broken.
   */
  public synchronized void send(AbstractJmsRequest request) throws Exception {
    tcpClient.send(request);
  }

  public AbstractJmsReply receive() throws Exception {
    return (AbstractJmsReply)tcpClient.receive();
  }

  /** Closes the TCP connection. */
  public void close() {
    tcpClient.close();
  }

  public String toString() {
    return '(' + super.toString() + ",tcpClient=" + tcpClient + ')';
  }

  public void closing() {
    tcpClient.stopReconnections();
  }
  
  public Identity getIdentity() {
    return identity;
  }

}
