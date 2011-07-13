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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.ow2.joram.mom.amqp.exceptions.NoConsumersException;
import org.ow2.joram.mom.amqp.exceptions.NotFoundException;
import org.ow2.joram.mom.amqp.exceptions.TransactionException;
import org.ow2.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;

/**
 * The fanout exchange type works as follows:
 * <ul>
 * <li>1. A message queue binds to the exchange with no arguments.
 * <li>2. A publisher sends the exchange a message.
 * <li>3. The message is passed to the message queue unconditionally.
 * </ul>
 */
public class FanoutExchange extends IExchange {
  
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  public static final String DEFAULT_NAME = "amq.fanout";

  public static final String TYPE = "fanout";

  private Set<String> boundQueues;

  public FanoutExchange() {
    super();
  }

  public FanoutExchange(String name, boolean durable) {
    super(name, durable);
    boundQueues = new HashSet<String>();
    if (durable)
      createExchange();
  }

  public synchronized void bind(String queueName, String routingKey, Map<String, Object> arguments) {
    boundQueues.add(queueName);
    if (durable) {
      saveExchange();
    }
  }

  public synchronized void unbind(String queueName, String routingKey, Map<String, Object> arguments)
      throws NotFoundException {
    boolean removed = boundQueues.remove(queueName);
    if (!removed) {
      throw new NotFoundException("Fanout exchange '" + name + "' not bound with queue '" + queueName + "'.");
    }
    if (durable) {
      saveExchange();
    }
  }

  public void doPublish(String routingKey, boolean mandatory, boolean immediate, BasicProperties properties,
      byte[] body, int channelNumber, short serverId, long proxyId) throws NoConsumersException,
      NotFoundException, TransactionException {
    Iterator<String> it = boundQueues.iterator();
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
    return boundQueues.size() == 0;
  }

  public String getType() {
    return TYPE;
  }

  public synchronized void removeQueueBindings(String queueName) {
    boundQueues.remove(queueName);
    if (durable) {
      saveExchange();
    }
  }

  public Set<String> getBoundQueues() {
    return boundQueues;
  }

  /**
   * @param out
   * @throws IOException
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeObject(boundQueues);
  }

  /**
   * @param in
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    super.readExternal(in);
    boundQueues = (Set<String>) in.readObject();
  }
}
