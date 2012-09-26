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

import org.ow2.joram.mom.amqp.exceptions.NoConsumersException;
import org.ow2.joram.mom.amqp.exceptions.NotFoundException;
import org.ow2.joram.mom.amqp.exceptions.TransactionException;
import org.ow2.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;
import org.ow2.joram.mom.amqp.marshalling.LongString;
import org.ow2.joram.mom.amqp.marshalling.LongStringHelper;

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
public class HeadersExchange extends IExchange {

  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  public static final String DEFAULT_NAME = "amq.match";

  public static final String TYPE = "headers";

  private Map<Map<String, Object>, Set<String>> bindings;

  public HeadersExchange() {
    super();
  }

  public HeadersExchange(String name, boolean durable) {
    super(name, durable);
    bindings = new HashMap<Map<String, Object>, Set<String>>();
    if (durable)
      createExchange();
  }

  public synchronized void bind(String queueName, String routingKey, Map<String, Object> arguments) {
    Set<String> boundQueues = bindings.get(arguments);

    if (boundQueues == null) {
      boundQueues = new HashSet<String>();
      bindings.put(arguments, boundQueues);
    }
    boundQueues.add(queueName);
    if (durable)
      saveExchange();
  }

  public synchronized void unbind(String queueName, String routingKey, Map<String, Object> arguments)
      throws NotFoundException {
    Set<String> boundQueues = bindings.get(arguments);
    if (boundQueues != null) {
      boolean removed = boundQueues.remove(queueName);
      if (!removed) {
        throw new NotFoundException("Unknown headers '" + arguments + "' between headers exchange '" + name
            + "' and queue '" + queueName + "'.");
      }
      if (boundQueues.size() == 0) {
        bindings.remove(arguments);
      }
      if (durable) {
        saveExchange();
      }
    } else {
      throw new NotFoundException("Unknown headers '" + arguments + "' between headers exchange '" + name
          + "' and queue '" + queueName + "'.");
    }
  }

  public void doPublish(String routingKey, boolean mandatory, boolean immediate, BasicProperties properties,
      byte[] body, int channelNumber, short serverId, long proxyId) throws NoConsumersException,
      NotFoundException, TransactionException {
    Set<String> destQueues = new HashSet<String>();
    Iterator<Map<String, Object>> iteratorMaps = bindings.keySet().iterator();

    if (properties.headers == null) {
      return;
    }

    while (iteratorMaps.hasNext()) {
      Map<String, Object> bindArguments = iteratorMaps.next();
      Iterator<String> iteratorArguments = bindArguments.keySet().iterator();

      // Match any : find the first argument matching to publish to the bound queues
      if (((LongString) bindArguments.get("x-match")).toString().equalsIgnoreCase("any")) {
        while (iteratorArguments.hasNext()) {
          String argument = iteratorArguments.next();
          if (argument.equals("x-match")) {
            continue;
          }
          if (bindArguments.get(argument) == null) {
            if (properties.headers.containsKey(argument)) {
              destQueues.addAll(bindings.get(bindArguments));
              break;
            }
          } else {
            if (bindArguments.get(argument).equals(LongStringHelper.asLongString("")) && properties.headers.containsKey(argument)
                || bindArguments.get(argument).equals(properties.headers.get(argument))) {
              destQueues.addAll(bindings.get(bindArguments));
              break;
            }
          }
        }
      }
      // Match all : check all arguments are matching 
      else {
        boolean matched = true;
        while (iteratorArguments.hasNext()) {
          String argument = iteratorArguments.next();
          if (argument.equals("x-match")) {
            continue;
          }
          if (bindArguments.get(argument) == null) {
            if (!properties.headers.containsKey(argument)) {
              matched = false;
              break;
            }
          } else {
            if (bindArguments.get(argument).equals(LongStringHelper.asLongString("")) && !properties.headers.containsKey(argument)
                || !bindArguments.get(argument).equals(properties.headers.get(argument))) {
              matched = false;
              break;
            }
          }
        }
        if (matched) {
          destQueues.addAll(bindings.get(bindArguments));
        }
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
    bindings = (HashMap<Map<String, Object>, Set<String>>) in.readObject();
  }
}
