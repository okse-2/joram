/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 - 2009 ScalAgent Distributed Technologies
 * Copyright (C) 2008 - 2009 CNES
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
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.UnknownAgent;

/**
 * The topic exchange type works as follows:
 * <ul>
 * <li>1. A message queue binds to the exchange using a routing pattern, P.
 * <li>2. A publisher sends the exchange a message with the routing key R.
 * <li>3. The message is passed to the message queue if R matches P.
 * </ul>
 * The routing key used for a topic exchange MUST consist of zero or more words
 * delimited by dots. Each word may contain the letters A-Z and a-z and digits
 * 0-9.<br>
 * The routing pattern follows the same rules as the routing key with the
 * addition that * matches a single word, and # matches zero or more words. Thus
 * the routing pattern *.stock.# matches the routing keys usd.stock and
 * eur.stock.db but not stock.nasdaq.
 */
public class TopicExchange extends ExchangeAgent {
  
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  public static final Logger logger = fr.dyade.aaa.common.Debug.getLogger(TopicExchange.class.getName());

  public static final String DEFAULT_NAME = "amq.topic";

  private static Pattern createPattern(String routingPattern) {
    String newPattern = routingPattern;
    newPattern = newPattern.replaceAll("\\*", "[a-zA-Z0-9]+");

    newPattern = newPattern.replaceAll("\\.#\\.", ".([a-zA-Z0-9]+.)*");
    newPattern = newPattern.replaceAll("\\.#", "(.[a-zA-Z0-9]+)*");
    newPattern = newPattern.replaceAll("#\\.", "([a-zA-Z0-9]+.)*");
    newPattern = newPattern.replaceAll("#", "[a-zA-Z0-9\\.]*");

    newPattern = newPattern.replaceAll("\\.", "\\\\.");
    return Pattern.compile(newPattern);
  }

  private Map bindings;

  public TopicExchange(String name, boolean durable) {
    super(name, durable);
    bindings = new HashMap();
  }

  public void bind(AgentId queueId, String routingKey, Map arguments) {
    Pattern routingPattern = createPattern(routingKey);
    KeyAndPattern keyAndPattern = new KeyAndPattern(routingKey, routingPattern);
    List boundQueues = (List) bindings.get(keyAndPattern);
    if (boundQueues == null) {
      boundQueues = new ArrayList();
      bindings.put(keyAndPattern, boundQueues);
    }
    if (!boundQueues.contains(queueId)) {
      boundQueues.add(queueId);
    }
  }

  public void unbind(AgentId queueId, String routingKey, Map arguments) {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "TopicExchange.Unbind(" + queueId + "," + routingKey + ")");
    }
    Pattern routingPattern = createPattern(routingKey);
    KeyAndPattern keyAndPattern = new KeyAndPattern(routingKey, routingPattern);
    List boundQueues = (List) bindings.get(keyAndPattern);
    if (boundQueues != null) {
      boundQueues.remove(queueId);
      if (boundQueues.size() == 0) {
        bindings.remove(keyAndPattern);
      }
    }
  }

  public void publish(String exchange, String routingKey, BasicProperties properties, byte[] body) {
    Set destQueues = new HashSet();
    
    Iterator iteratorPatterns = bindings.keySet().iterator();
    while (iteratorPatterns.hasNext()) {
      KeyAndPattern keyAndPattern = (KeyAndPattern) iteratorPatterns.next();
      Matcher matcher = keyAndPattern.pattern.matcher(routingKey);
      if (matcher.matches()) {
        List boundQueues = (List) bindings.get(keyAndPattern);
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

  /**
   * Class used to keep trace of the key which leads to the pattern. Useful for
   * binding and unbinding.
   */
  private class KeyAndPattern {

    public String key;

    public Pattern pattern;

    public KeyAndPattern(String key, Pattern pattern) {
      this.key = key;
      this.pattern = pattern;
    }

    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (!(obj instanceof KeyAndPattern))
        return false;
      KeyAndPattern other = (KeyAndPattern) obj;
      return key.equals(other.key);
    }

    public int hashCode() {
      return key.hashCode();
    }

    public String toString() {
      return key + " + pattern";
    }

  }

}