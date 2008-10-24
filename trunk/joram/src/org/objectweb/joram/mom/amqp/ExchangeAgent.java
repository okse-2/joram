/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.amqp;

import java.util.Map;

import com.rabbitmq.client.AMQP.BasicProperties;

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.UnknownAgent;

public abstract class ExchangeAgent extends Agent {
  
  public abstract void setArguments(Map arguments);
  
  public void react(AgentId from, Notification not) throws Exception {
    if (not instanceof PublishNot) {
      doReact((PublishNot) not);
    } else if (not instanceof BindNot) {
      doReact((BindNot) not);
    } else if (not instanceof DeleteNot) {
      doReact((DeleteNot) not, from);
    } else if (not instanceof UnknownAgent) {
      doReact((UnknownAgent) not, from);
    } else {
      super.react(from, not);
    }
  }

  private void doReact(DeleteNot not, AgentId from) {
    delete(from);
  }

  private void doReact(PublishNot not) {
    publish(not.getExchange(), not.getRoutingKey(), not.getProperties(), not.getBody());
  }
  
  private void doReact(BindNot not) {
    bind(not.getQueue(), not.getRoutingKey(), not.getArguments());
  }

  public abstract void doReact(UnknownAgent not, AgentId from);
  
  public abstract void publish(String exchange, String routingKey, BasicProperties properties, byte[] body);

  public abstract void bind(String queue, String routingKey, Map arguments);

}
