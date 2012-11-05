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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.objectweb.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.UnknownAgent;

/**
 * The direct exchange type provides routing of messages to zero or more queues
 * based on an exact match between the routing key of the message, and the
 * binding key used to bind the queue to the exchange. This can be used to
 * construct the classic point-to-point queue based messaging model, however, as
 * with any of the defined exchange types, a message may end up in multiple
 * queues when multiple binding keys match the message's routing key. <br>
 * The direct exchange type works as follows:
 * <ul>
 * <li>1. A message queue is bound to the exchange using a binding key, K.
 * <li>2. A publisher sends the exchange a message with the routing key R.
 * <li>3. The message is passed to all message queues bound to the exchange with
 * key K where K = R.
 * </ul>
 */
public class DirectExchange extends ExchangeAgent {
  
  private Map bindings;
  
  public DirectExchange(String name, boolean durable) {
    super(name, durable);
    bindings = new HashMap();
  }

  public void bind(String queue, String routingKey, Map arguments) {
    List boundQueues = (List) bindings.get(routingKey);
    if (boundQueues == null) {
      boundQueues = new ArrayList();
      bindings.put(routingKey, boundQueues);
    }
    AgentId queueAgent = (AgentId) NamingAgent.getSingleton().lookup(queue);
    if (queueAgent != null && !boundQueues.contains(queueAgent)) {
      boundQueues.add(queueAgent);
    }
  }

  public void unbind(String queue, String routingKey, Map arguments) {
    List boundQueues = (List) bindings.get(routingKey);
    if (boundQueues != null) {
      AgentId queueAgent = (AgentId) NamingAgent.getSingleton().lookup(queue);
      boundQueues.remove(queueAgent);
      if (boundQueues.size() == 0) {
        bindings.remove(routingKey);
      }
    }
  }

  public void publish(String exchange, String routingKey, BasicProperties properties, byte[] body) {
    List boundQueues = (List) bindings.get(routingKey);
    if (boundQueues != null) {
      Iterator it = boundQueues.iterator();
      while (it.hasNext()) {
        AgentId queueAgent = (AgentId) it.next();
        sendTo(queueAgent, new PublishNot(exchange, routingKey, properties, body));
      }
    }
  }

  public void setArguments(Map arguments) {
    // TODO Auto-generated method stub

  }

  public void doReact(UnknownAgent not) {
    // Queue must have been deleted: remove it from bindings
    Iterator iteratorLists = bindings.values().iterator();
    while (iteratorLists.hasNext()) {
      List boundQueues = (List) iteratorLists.next();
      Iterator iteratorQueues = boundQueues.iterator();
      while (iteratorQueues.hasNext()) {
        AgentId queue = (AgentId) iteratorQueues.next();
        if (queue.equals(not.agent)) {
          iteratorQueues.remove();
          break;
        }
      }
      if (boundQueues.size() == 0) {
        iteratorLists.remove();
      }
    }
  }

  public boolean isUnused() {
    return bindings.size() == 0;
  }

}
