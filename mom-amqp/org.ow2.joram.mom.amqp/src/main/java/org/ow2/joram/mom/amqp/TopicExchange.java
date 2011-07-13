/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 - 2011 ScalAgent Distributed Technologies
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
package org.ow2.joram.mom.amqp;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.ow2.joram.mom.amqp.exceptions.NoConsumersException;
import org.ow2.joram.mom.amqp.exceptions.NotFoundException;
import org.ow2.joram.mom.amqp.exceptions.TransactionException;
import org.ow2.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;

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
public class TopicExchange extends IExchange {
  
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  public static final Logger logger = fr.dyade.aaa.common.Debug.getLogger(TopicExchange.class.getName());

  public static final String DEFAULT_NAME = "amq.topic";

  public static final String TYPE = "topic";

  private static Pattern createPattern(String routingPattern) {
    String newPattern = routingPattern;
    newPattern = newPattern.replaceAll("\\*", "[a-zA-Z0-9]*");

    newPattern = newPattern.replaceAll("\\.#\\.", ".([a-zA-Z0-9]*.)*");
    newPattern = newPattern.replaceAll("\\.#", "(.[a-zA-Z0-9]*)*");
    newPattern = newPattern.replaceAll("#\\.", "([a-zA-Z0-9]*.)*");
    newPattern = newPattern.replaceAll("#", "[a-zA-Z0-9\\.]*");

    newPattern = newPattern.replaceAll("\\.", "\\\\.");
    return Pattern.compile(newPattern);
  }

  private Map<KeyAndPattern, Set<String>> bindings;

  public TopicExchange() {
    super();  
  }
  
  public TopicExchange(String name, boolean durable) {
    super(name, durable);
    bindings = new HashMap<KeyAndPattern, Set<String>>();
    if (durable)
      createExchange();
  }

  public synchronized void bind(String queueName, String routingKey, Map<String, Object> arguments) {
    Pattern routingPattern = createPattern(routingKey);
    KeyAndPattern keyAndPattern = new KeyAndPattern(routingKey, routingPattern);
    Set<String> boundQueues = bindings.get(keyAndPattern);
    if (boundQueues == null) {
      boundQueues = new HashSet<String>();
      bindings.put(keyAndPattern, boundQueues);
    }
    boundQueues.add(queueName);
    if (durable)
      saveExchange();
  }

  public synchronized void unbind(String queueName, String routingKey, Map<String, Object> arguments)
      throws NotFoundException {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "TopicExchange.Unbind(" + queueName + "," + routingKey + ")");
    }
    Pattern routingPattern = createPattern(routingKey);
    KeyAndPattern keyAndPattern = new KeyAndPattern(routingKey, routingPattern);
    Set<String> boundQueues = bindings.get(keyAndPattern);
    if (boundQueues != null) {
      boolean removed = boundQueues.remove(queueName);
      if (!removed) {
        throw new NotFoundException("Unknown binding '" + routingKey + "' between topic exchange '" + name
            + "' and queue '" + queueName + "'.");
      }
      if (boundQueues.size() == 0) {
        bindings.remove(keyAndPattern);
      }
      if (durable) {
        saveExchange();
      }
    } else {
      throw new NotFoundException("Unknown binding '" + routingKey + "' between topic exchange '" + name
          + "' and queue '" + queueName + "'.");
    }
  }

  public void doPublish(String routingKey, boolean mandatory, boolean immediate, BasicProperties properties,
      byte[] body, int channelNumber, short serverId, long proxyId) throws NoConsumersException,
      NotFoundException, TransactionException {
    Set<String> destQueues = new HashSet<String>();
    
    Iterator<KeyAndPattern> iteratorPatterns = bindings.keySet().iterator();
    while (iteratorPatterns.hasNext()) {
      KeyAndPattern keyAndPattern = iteratorPatterns.next();
      Matcher matcher = keyAndPattern.pattern.matcher(routingKey);
      if (matcher.matches()) {
        Set<String> boundQueues = bindings.get(keyAndPattern);
        destQueues.addAll(boundQueues);
      }
    }
    
    Iterator<String> it = destQueues.iterator();
    while (it.hasNext()) {
      String queueName = it.next();
      publishToQueue(queueName, routingKey, immediate, properties, body, channelNumber, serverId, proxyId);
    }
    checkPublication(mandatory);
  }

  public void setArguments(Map<String, Object> arguments) {
    // TODO Auto-generated method stub

  }

  public boolean isUnused() {
    return bindings.size() == 0;
  }

  public String getType() {
    return TYPE;
  }

  /**
   * Class used to keep trace of the key which leads to the pattern. Useful for
   * binding and unbinding.
   */
  private class KeyAndPattern implements Serializable {

    /** define serialVersionUID for interoperability */
    private static final long serialVersionUID = 1L;

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

  public synchronized void removeQueueBindings(String queueName) {
    Iterator<Set<String>> queueListIterator = bindings.values().iterator();
    while (queueListIterator.hasNext()) {
      Set<String> queueList = queueListIterator.next();
      queueList.remove(queueName);
      if (queueList.size() == 0) {
        queueListIterator.remove();
      }
    }
    if (durable) {
      saveExchange();
    }
  }

  public Set<String> getBoundQueues() {
    Set<String> boundQueues = new HashSet<String>();
    Iterator<Set<String>> queueListIterator = bindings.values().iterator();
    while (queueListIterator.hasNext()) {
      Set<String> queueList = queueListIterator.next();
      boundQueues.addAll(queueList);
    }
    return boundQueues;
  }

  /**
   * @param out
   * @throws IOException
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeObject(bindings);
  }

  /**
   * @param in
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    super.readExternal(in);
    bindings = (Map<KeyAndPattern, Set<String>>) in.readObject();
  }
}
