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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.UnknownAgent;

/**
 * The headers exchange type works as follows:
 * <ul>
 * <li>1. A message queue is bound to the exchange with a table of arguments
 * containing the headers to be matched for that binding and optionally the
 * values they should hold. The routing key is not used.
 * <li>2. A publisher sends a message to the exchange where the 'headers'
 * property contains a table of names and values.
 * <li>3. The message is passed to the queue if the headers property matches the
 * arguments with which the queue was bound.
 * </ul>
 * The matching algorithm is controlled by a special bind argument passed as a
 * name value pair in the arguments table. The name of this argument is
 * 'xmatch'.<br>
 * It can take one of two values, dictating how the rest of the name value pairs
 * in the table are treated during matching:
 * <ul>
 * <li>(i) 'all' implies that all the other pairs must match the headers
 * property of a message for that message to be routed (i.e. and AND match)
 * <li>(ii) 'any' implies that the message should be routed if any of the fields
 * in the headers property match one of the fields in the arguments table (i.e.
 * an OR match)
 * </ul>
 * A field in the bind arguments matches a field in the message if either the
 * field in the bind arguments has no value and a field of the same name is
 * present in the message headers or if the field in the bind arguments has a
 * value and a field of the same name exists in the message headers and has that
 * same value.
 */
public class HeadersExchange extends ExchangeAgent {
  
  private Map bindings;
  
  public HeadersExchange(String name, boolean durable) {
    super(name, durable);
  }

  public void bind(String queue, String routingKey, Map arguments) {
    List boundQueues = (List) bindings.get(arguments);
    if (boundQueues == null) {
      boundQueues = new ArrayList();
      bindings.put(arguments, boundQueues);
    }
    AgentId queueAgent = (AgentId) NamingAgent.getSingleton().lookup(queue);
    if (queueAgent != null && !boundQueues.contains(queueAgent)) {
      boundQueues.add(queueAgent);
    }
  }

  public void unbind(String queue, String routingKey, Map arguments) {
    List boundQueues = (List) bindings.get(arguments);
    if (boundQueues != null) {
      AgentId queueAgent = (AgentId) NamingAgent.getSingleton().lookup(queue);
      boundQueues.remove(queueAgent);
      if (boundQueues.size() == 0) {
        bindings.remove(arguments);
      }
    }
  }

  public void publish(String exchange, String routingKey, BasicProperties properties, byte[] body) {
    Set destQueues = new HashSet();
    Iterator iteratorMaps = bindings.keySet().iterator();
    
    while (iteratorMaps.hasNext()) {
      Map bindArguments = (Map) iteratorMaps.next();
      Iterator iteratorArguments = bindArguments.keySet().iterator();

      // Match any : find the first argument matching to publish to the bound queues
      if (((String) bindArguments.get("x-match")).equalsIgnoreCase("any")) {
        while (iteratorArguments.hasNext()) {
          String argument = (String) iteratorArguments.next();
          if (bindArguments.get(argument) == null && properties.headers.containsKey(argument)
              || ((String) bindArguments.get(argument)).equals("") && properties.headers.containsKey(argument)
              || ((String) bindArguments.get(argument)).equals(properties.headers.get(argument))) {
            destQueues.addAll((List) bindings.get(bindArguments));
            break;
          }
        }
      }
      // Match all : check all arguments are matching 
      else {
        boolean matched = true;
        while (iteratorArguments.hasNext()) {
          String argument = (String) iteratorArguments.next();
          if (bindArguments.get(argument) == null && !properties.headers.containsKey(argument)
              || ((String) bindArguments.get(argument)).equals("") && !properties.headers.containsKey(argument)
              || !((String) bindArguments.get(argument)).equals(properties.headers.get(argument))) {
            matched = false;
            break;
          }
        }
        if (matched) {
          destQueues.addAll((List) bindings.get(bindArguments));
        }
      }
    }
    
    Iterator it = destQueues.iterator();
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
