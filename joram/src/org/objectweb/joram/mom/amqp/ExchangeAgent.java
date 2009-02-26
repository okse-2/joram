/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
 * Copyright (C) 2008 CNES
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

import org.objectweb.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.DeleteAck;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.UnknownAgent;

public abstract class ExchangeAgent extends Agent {
  
  private String name;
  private boolean durable;

  public ExchangeAgent(String name, boolean durable) {
    this.name = name;
    this.durable = durable;
  }
  
  protected void agentInitialize(boolean firstTime) throws Exception {
    super.agentInitialize(firstTime);
    if (!firstTime && !durable) {
      delete();
    }
  }
  
  public void react(AgentId from, Notification not) throws Exception {
    if (not instanceof PublishNot) {
      doReact((PublishNot) not);
    } else if (not instanceof BindNot) {
      doReact((BindNot) not);
    } else if (not instanceof UnbindNot) {
      doReact((UnbindNot) not);
    } else if (not instanceof DeleteNot) {
      doReact((DeleteNot) not, from);
    } else if (not instanceof UnknownAgent) {
      doReact((UnknownAgent) not);
    } else {
      super.react(from, not);
    }
    if (!durable) {
      setNoSave();
    }
  }

  private void doReact(DeleteNot not, AgentId from) throws Exception {
    if (not.isIfUnused() && !isUnused()) {
      sendTo(from, new DeleteAck(getId()));
    } else {
      NamingAgent.getSingleton().unbind(name);
      delete(from);
    }
  }

  private void doReact(PublishNot not) {
    publish(not.getExchange(), not.getRoutingKey(), not.getProperties(), not.getBody());
  }
  
  private void doReact(BindNot not) {
    bind(not.getQueue(), not.getRoutingKey(), not.getArguments());
  }
  
  private void doReact(UnbindNot not) {
    unbind(not.getQueue(), not.getRoutingKey(), not.getArguments());
  }
  
  public abstract void setArguments(Map arguments);

  public abstract void doReact(UnknownAgent not);
  
  public abstract void publish(String exchange, String routingKey, BasicProperties properties, byte[] body);

  public abstract void bind(String queue, String routingKey, Map arguments);

  public abstract void unbind(String queue, String routingKey, Map arguments);
  
  public abstract boolean isUnused();

}
