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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.objectweb.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.UnknownAgent;

/**
 * The fanout exchange type works as follows:
 * <ul>
 * <li>1. A message queue binds to the exchange with no arguments.
 * <li>2. A publisher sends the exchange a message.
 * <li>3. The message is passed to the message queue unconditionally.
 * </ul>
 */
public class FanoutExchange extends ExchangeAgent {
  
  private List boundQueues;
  
  public FanoutExchange(String name, boolean durable) {
    super(name, durable);
    boundQueues = new ArrayList();
  }

  public void bind(String queue, String routingKey, Map arguments) {
    AgentId queueAgent = (AgentId) NamingAgent.getSingleton().lookup(queue);
    if (queueAgent != null && !boundQueues.contains(queueAgent)) {
      boundQueues.add(queueAgent);
    }
  }

  public void unbind(String queue, String routingKey, Map arguments) {
    AgentId queueAgent = (AgentId) NamingAgent.getSingleton().lookup(queue);
    boundQueues.remove(queueAgent);
  }

  public void publish(String exchange, String routingKey, BasicProperties properties, byte[] body) {
    Iterator it = boundQueues.iterator();
    while (it.hasNext()) {
      AgentId queueAgent = (AgentId) it.next();
      sendTo(queueAgent, new PublishNot(exchange, routingKey, properties, body));
    }
  }

  public void setArguments(Map arguments) {
    // TODO Auto-generated method stub

  }

  public void doReact(UnknownAgent not) {
    // Queue must have been deleted: remove it from bindings
    Iterator it = boundQueues.iterator();
    while (it.hasNext()) {
      AgentId queue = (AgentId) it.next();
      if (queue.equals(not.agent)) {
        it.remove();
        return;
      }
    }
  }

  public boolean isUnused() {
    return boundQueues.size() == 0;
  }

}
