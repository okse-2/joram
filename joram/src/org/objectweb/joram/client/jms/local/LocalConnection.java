/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2008 ScalAgent Distributed Technologies
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
package org.objectweb.joram.client.jms.local;

import java.util.Timer;

import javax.jms.JMSException;

import org.objectweb.joram.client.jms.connection.RequestChannel;
import org.objectweb.joram.mom.dest.AdminTopic;
import org.objectweb.joram.mom.notifications.GetProxyIdNot;
import org.objectweb.joram.mom.proxies.ConnectionManager;
import org.objectweb.joram.mom.proxies.OpenConnectionNot;
import org.objectweb.joram.mom.proxies.StandardConnectionContext;
import org.objectweb.joram.shared.JoramTracing;
import org.objectweb.joram.shared.client.AbstractJmsReply;
import org.objectweb.joram.shared.client.AbstractJmsRequest;
import org.objectweb.joram.shared.security.Identity;
import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;

public class LocalConnection implements RequestChannel {
  
  private Identity identity;

  private AgentId proxyId;

  private StandardConnectionContext ctx;

  public LocalConnection(Identity identity) throws JMSException {
    if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgProxy.log(BasicLevel.DEBUG,
                                "LocalConnection.<init>(" + identity + ')');
    this.identity = identity;
  }
  
  public void setTimer(Timer timer) {
    // Use of timer is useless
  }
  
  public void connect() throws Exception {
    if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgProxy.log(BasicLevel.DEBUG, "LocalConnection.connect()");

    if (AgentServer.getStatus() != AgentServer.Status.STARTED) {
      if ((AgentServer.getStatus() != AgentServer.Status.INITIALIZED) &&
          (AgentServer.getStatus() != AgentServer.Status.STOPPED)) {
        if (JoramTracing.dbgProxy.isLoggable(BasicLevel.ERROR))
          JoramTracing.dbgProxy.log(BasicLevel.ERROR,
                                    "LocalConnection.connect(), server is not initialized: " + AgentServer.getStatusInfo() + '.');

        throw new Exception();
      }
      
      if (JoramTracing.dbgProxy.isLoggable(BasicLevel.WARN))
        JoramTracing.dbgProxy.log(BasicLevel.WARN,
                                  "LocalConnection.connect(), server is not started: " + AgentServer.getStatusInfo() + '.');
    }

    GetProxyIdNot gpin = new GetProxyIdNot(identity, null);
    try {
      gpin.invoke(AdminTopic.getDefault());
      proxyId = gpin.getProxyId();
    } catch (Exception exc) {
      if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgProxy.log(BasicLevel.DEBUG, "", exc);
      throw new JMSException(exc.getMessage());
    }

    OpenConnectionNot ocn = new OpenConnectionNot(false, 0);
    try {
      ocn.invoke(proxyId);
      ctx = (StandardConnectionContext)ocn.getConnectionContext();
    } catch (Exception exc) {
      if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgProxy.log(BasicLevel.DEBUG, "", exc);
      JMSException jmse = new JMSException(exc.getMessage());
      jmse.setLinkedException(exc);
      throw jmse;
    }
  }

  public void send(AbstractJmsRequest request) 
    throws Exception {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log( BasicLevel.DEBUG, 
                                  "LocalConnection.send(" + request + ')');

    ConnectionManager.sendToProxy(proxyId, ctx.getKey(), request, request);
  }

  public AbstractJmsReply receive() throws Exception {
    AbstractJmsReply reply = (AbstractJmsReply)ctx.getQueue().get();
    ctx.getQueue().pop();

    return reply;
  }

  public void close() {
    // Nothing to do
  }
}
