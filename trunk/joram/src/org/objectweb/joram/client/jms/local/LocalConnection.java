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

import org.objectweb.joram.client.jms.*;
import org.objectweb.joram.client.jms.Connection;
import org.objectweb.joram.shared.client.*;
import org.objectweb.joram.mom.MomTracing;
import org.objectweb.joram.mom.proxies.*;
import org.objectweb.joram.mom.notifications.*;
import org.objectweb.joram.client.jms.connection.RequestChannel;

import fr.dyade.aaa.agent.*;

import fr.dyade.aaa.util.Queue;

import javax.jms.*;

import org.objectweb.util.monolog.api.BasicLevel;

public class LocalConnection 
    implements RequestChannel {

  private AgentId proxyId;

  private int key;

  private Queue replyQueue;

  public LocalConnection(
    String userName, String password) throws JMSException {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG,
        "LocalConnection.<init>(" + userName + ',' + password + ')');

    GetProxyIdNot gpin = new GetProxyIdNot(
      userName, password);
    try {
      gpin.invoke(new AgentId(AgentServer.getServerId(),
                              AgentServer.getServerId(),
                              AgentId.JoramAdminStamp));
      proxyId = gpin.getProxyId();
    } catch (Exception exc) {
      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(BasicLevel.DEBUG, "", exc);
      throw new JMSException(exc.getMessage());
    }

    OpenConnectionNot ocn = new OpenConnectionNot(false, 0);
    try {
      ocn.invoke(proxyId);
      replyQueue = (Queue)ocn.getReplyQueue();
      key = ocn.getKey();
    } catch (Exception exc) {
      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(BasicLevel.DEBUG, "", exc);
      JMSException jmse = new JMSException(
        exc.getMessage());
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

    Channel.sendTo(proxyId, 
      new RequestNot(key, request));

    if (request instanceof ProducerMessages) {
      FlowControl.flowControl();
    }
  }

  public AbstractJmsReply receive() 
    throws Exception {
    AbstractJmsReply reply = 
      (AbstractJmsReply)replyQueue.get();
    replyQueue.pop();
    return reply;
  }

  public void close() {
    // Nothing to do
  }
}
