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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.UnknownAgent;

/**
 * The topic exchange type provides routing to bound queues based on a pattern
 * match between the binding key and the routing key of the message. This
 * exchange type may be used to support the classic publish/subscribe paradigm
 * using a topic namespace as the addressing model to select and deliver
 * messages across multiple consumers based on a partial or full match on a
 * topic pattern.<br>
 * The topic exchange type works as follows:
 * <ul>
 * <li>1. A message queue is bound to the exchange using a binding key, K.
 * <li>2. A publisher sends the exchange a message with the routing key R.
 * <li>3. The message is passed to the all message queues where K matches R.
 * </ul>
 * The binding key is formed using zero or more tokens, with each token
 * delimited by the '.' char. The binding key MUST be specified in this form and
 * additionally supports special wild-card characters: '*' matches a single word
 * and '#' matches zero or more words.<br>
 * Thus the binding key "*.stock.#" matches the routing keys "usd.stock" and
 * "eur.stock.db" but not "stock.nasdaq".
 */
public class TopicExchange extends ExchangeAgent {
  
  private static Pattern createPattern(String routingPattern) {
    String newPattern = routingPattern;
    newPattern = newPattern.replaceAll("\\*", "[a-zA-Z]+");

    newPattern = newPattern.replaceAll("\\.#\\.", ".([a-zA-Z]+.)*");
    newPattern = newPattern.replaceAll("\\.#", "(.[a-zA-Z]+)*");
    newPattern = newPattern.replaceAll("#\\.", "([a-zA-Z]+.)*");

    newPattern = newPattern.replaceAll("\\.", "\\\\.");
    return Pattern.compile(newPattern);
  }

  private Map bindings;

  public TopicExchange(String name, boolean durable) {
    super(name, durable);
    bindings = new HashMap();
  }

  public void bind(String queue, String routingKey, Map arguments) {
    Pattern routingPattern = createPattern(routingKey);
    List boundQueues = (List) bindings.get(routingPattern);
    if (boundQueues == null) {
      boundQueues = new ArrayList();
      bindings.put(routingPattern, boundQueues);
    }
    AgentId queueAgent = (AgentId) NamingAgent.getSingleton().lookup(queue);
    if (queueAgent != null && !boundQueues.contains(queueAgent)) {
      boundQueues.add(queueAgent);
    }
  }

  public void unbind(String queue, String routingKey, Map arguments) {
    Pattern routingPattern = createPattern(routingKey);
    List boundQueues = (List) bindings.get(routingPattern);
    if (boundQueues != null) {
      AgentId queueAgent = (AgentId) NamingAgent.getSingleton().lookup(queue);
      boundQueues.remove(queueAgent);
      if (boundQueues.size() == 0) {
        bindings.remove(routingPattern);
      }
    }
  }

  public void publish(String exchange, String routingKey, BasicProperties properties, byte[] body) {
    Set destQueues = new HashSet();
    
    Iterator iteratorPatterns = bindings.keySet().iterator();
    while (iteratorPatterns.hasNext()) {
      Pattern pattern = (Pattern) iteratorPatterns.next();
      Matcher matcher = pattern.matcher(routingKey);
      if (matcher.matches()) {
        List boundQueues = (List) bindings.get(pattern);
        destQueues.addAll(boundQueues);
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
