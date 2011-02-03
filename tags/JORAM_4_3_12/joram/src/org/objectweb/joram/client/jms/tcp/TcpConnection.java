/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2004 ScalAgent Distributed Technologies
 * Copyright (C) 2004 - France Telecom R&D
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

import org.objectweb.joram.client.jms.Connection;
import org.objectweb.joram.client.jms.FactoryParameters;
import org.objectweb.joram.shared.client.AbstractJmsRequest;
import org.objectweb.joram.shared.client.AbstractJmsReply;
import org.objectweb.joram.client.jms.connection.RequestChannel;

import java.io.*;
import java.net.*;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;

import org.objectweb.joram.client.jms.JoramTracing;
import org.objectweb.util.monolog.api.BasicLevel;

/**
 * A <code>TcpConnection</code> links a Joram client and a Joram platform
 * with a TCP socket.
 * <p>
 * Requests and replies travel through the socket after serialization.
 */
public class TcpConnection 
    implements RequestChannel { 
  
  private ReliableTcpClient tcpClient = null;

  /**
   * Creates a <code>TcpConnection</code> instance.
   *
   * @param params  Factory parameters.
   * @param name  Name of user.
   * @param password  Password of user.
   *
   * @exception JMSSecurityException  If the user identification is incorrrect.
   * @exception IllegalStateException  If the server is not reachable.
   */
  public TcpConnection(FactoryParameters params, 
                       String name,
                       String password) 
    throws JMSException {
    this(params,
         name,
         password,
         "org.objectweb.joram.client.jms.tcp.ReliableTcpClient");
  }

  /**
   * Creates a <code>TcpConnection</code> instance.
   *
   * @param params  Factory parameters.
   * @param name  Name of user.
   * @param password  Password of user.
   * @param reliableClass  reliable class name.
   *
   * @exception JMSSecurityException  If the user identification is incorrrect.
   * @exception IllegalStateException  If the server is not reachable.
   */
  public TcpConnection(FactoryParameters params, 
                       String name,
                       String password,
                       String reliableClass) 
    throws JMSException {

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "TcpConnection.<init>(" + params + ',' +
        name + ',' + password + ',' + reliableClass +')');

    if (reliableClass == null ||
        reliableClass.equals("") ||
        reliableClass.length() < 1) {
      reliableClass = "org.objectweb.joram.client.jms.tcp.ReliableTcpClient";
    }
    try {
      tcpClient = 
        (ReliableTcpClient) Class.forName(reliableClass).newInstance(); 
    } catch (ClassNotFoundException exc) {
      JMSException jmsExc = 
        new JMSException("TcpConnection: ClassNotFoundException : " + 
                         reliableClass);
      jmsExc.setLinkedException(exc);
      throw jmsExc;
    } catch (InstantiationException exc) {
      JMSException jmsExc = 
        new JMSException("TcpConnection: InstantiationException : " + 
                         reliableClass);
      jmsExc.setLinkedException(exc);
      throw jmsExc;
    } catch (IllegalAccessException exc) {
      JMSException jmsExc = 
        new JMSException("TcpConnection: IllegalAccessException : " + 
                         reliableClass);
      jmsExc.setLinkedException(exc);
      throw jmsExc;
    }
    tcpClient.init(params, 
                   name,
                   password,
                   params.cnxPendingTimer > 0);
    tcpClient.addServerAddress(
      params.getHost(),
      params.getPort());
    tcpClient.connect();
  }

  
  /**
   * Sending a JMS request through the TCP connection.
   *
   * @exception IllegalStateException  If the connection is broken.
   */
  public synchronized void send(AbstractJmsRequest request)
    throws Exception {
    tcpClient.send(request);
  }

  public AbstractJmsReply receive()
    throws Exception {
    return (AbstractJmsReply)tcpClient.receive();
  }

  /** Closes the TCP connection. */
  public void close() {
    tcpClient.close();
  }

  public String toString() {
    return '(' + super.toString() + 
      ",tcpClient=" + tcpClient + ')';
  }
}