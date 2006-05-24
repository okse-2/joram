/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - ScalAgent Distributed Technologies
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

import org.objectweb.joram.client.jms.JoramTracing;
import org.objectweb.joram.client.jms.connection.RequestChannel;
import org.objectweb.joram.mom.MomTracing;
import org.objectweb.joram.mom.notifications.GetProxyIdNot;
import org.objectweb.joram.mom.proxies.ConnectionManager;
import org.objectweb.joram.mom.proxies.FlowControl;
import org.objectweb.joram.mom.proxies.MultiCnxSync;
import org.objectweb.joram.mom.proxies.OpenConnectionNot;
import org.objectweb.joram.mom.proxies.RequestNot;
import org.objectweb.joram.mom.proxies.StandardConnectionContext;
import org.objectweb.joram.shared.client.AbstractJmsReply;
import org.objectweb.joram.shared.client.AbstractJmsRequest;
import org.objectweb.joram.shared.client.ProducerMessages;
import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.Channel;

public class LocalConnection 
    implements RequestChannel {
  
  private String userName;
  
  private String password;

  private AgentId proxyId;

  private StandardConnectionContext ctx;

  public LocalConnection(
    String userName2, String password2) throws JMSException {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG,
        "LocalConnection.<init>(" + userName2 + ',' + password2 + ')');
    userName = userName2;
    password = password2;
  }
  
  public void setTimer(Timer timer) {
    // No timer is useful
  }
  
  public void connect() throws Exception {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG,
        "LocalConnection.connect()");
    GetProxyIdNot gpin = new GetProxyIdNot(userName, password);
    try {
      gpin.invoke(new AgentId(AgentServer.getServerId(), AgentServer
          .getServerId(), AgentId.JoramAdminStamp));
      proxyId = gpin.getProxyId();
    } catch (Exception exc) {
      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(BasicLevel.DEBUG, "", exc);
      throw new JMSException(exc.getMessage());
    }

    OpenConnectionNot ocn = new OpenConnectionNot(false, 0);
    try {
      ocn.invoke(proxyId);
      ctx = (StandardConnectionContext)ocn.getConnectionContext();
    } catch (Exception exc) {
      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(BasicLevel.DEBUG, "", exc);
      JMSException jmse = new JMSException(exc.getMessage());
      jmse.setLinkedException(exc);
      throw jmse;
    }
  }

  public void send(AbstractJmsRequest request) 
    throws Exception {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "LocalConnection.send(" + request + ')');
    ConnectionManager.sendToProxy(
        proxyId,
        ctx.getKey(),
        request, 
        request);
  }

  public AbstractJmsReply receive() 
    throws Exception {
    AbstractJmsReply reply = 
      (AbstractJmsReply)ctx.getQueue().get();
    ctx.getQueue().pop();
    return reply;
  }

  public void close() {
    // Nothing to do
  }
}
