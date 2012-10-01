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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.ow2.joram.mom.amqp.exceptions.NoConsumersException;
import org.ow2.joram.mom.amqp.exceptions.NotFoundException;
import org.ow2.joram.mom.amqp.exceptions.TransactionException;
import org.ow2.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;

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
public class DirectExchange extends IExchange {
  
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  public final static Logger logger = fr.dyade.aaa.common.Debug.getLogger(DirectExchange.class.getName());

  public static final String DEFAULT_NAME = "amq.direct";

  public static final String TYPE = "direct";

  private Map<String, Set<String>> bindings;

  public DirectExchange() {
    super();
  }

  public DirectExchange(String name, boolean durable) {
    super(name, durable);
    bindings = new HashMap<String, Set<String>>();
    if (durable)
      createExchange();
  }

  public synchronized void bind(String queueName, String routingKey, Map<String, Object> arguments) {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "DirectExchange.Bind(" + queueName + "," + routingKey + ") with " + name);
    }

    Set<String> boundQueues = bindings.get(routingKey);
    if (boundQueues == null) {
      boundQueues = new HashSet<String>();
      bindings.put(routingKey, boundQueues);
    }
    boundQueues.add(queueName);
    if (durable)
      saveExchange();
  }

  public synchronized void unbind(String queueName, String routingKey, Map<String, Object> arguments)
      throws NotFoundException {
    Set<String> boundQueues = bindings.get(routingKey);
    if (boundQueues != null) {
      boolean removed = boundQueues.remove(queueName);
      if (!removed) {
        throw new NotFoundException("Unknown routing key '" + routingKey + "' between direct exchange '"
            + name + "' and queue '" + queueName + "'.");
      }
      if (boundQueues.size() == 0) {
        bindings.remove(routingKey);
      }
      if (durable)
        saveExchange();
    } else {
      throw new NotFoundException("Unknown routing key '" + routingKey + "' between direct exchange '" + name
          + "' and queue '" + queueName + "'.");
    }
  }

  public void doPublish(String routingKey, boolean mandatory, boolean immediate, BasicProperties properties,
      byte[] body, int channelNumber, short serverId, long proxyId) throws NotFoundException,
      NoConsumersException, TransactionException {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "DirectExchange.Publish(" + name + "," + routingKey + ")");
    }
    
    Set<String> boundQueues = bindings.get(routingKey);
    if (boundQueues != null) {
      Iterator<String> it = boundQueues.iterator();
      while (it.hasNext()) {
        String queueName = it.next();
        publishToQueue(queueName, routingKey, immediate, properties, body, channelNumber, serverId, proxyId);
      }
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

  public synchronized void removeQueueBindings(String queueName) {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "DirectExchange.removeQueueBindings(" + queueName + ")");
    }

    Iterator<Set<String>> queueListIterator = bindings.values().iterator();
    while (queueListIterator.hasNext()) {
      Set<String> queueList = queueListIterator.next();
      queueList.remove(queueName);
      if (queueList.size() == 0) {
        queueListIterator.remove();
      }
    }
    if (durable)
      saveExchange();
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
    bindings = (Map<String, Set<String>>) in.readObject();
  }

}
