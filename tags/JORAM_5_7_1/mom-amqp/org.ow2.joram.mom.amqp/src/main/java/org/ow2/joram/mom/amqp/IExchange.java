/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 - 2011 ScalAgent Distributed Technologies
 * Copyright (C) 2009 CNES
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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.AlreadyBoundException;
import java.util.Map;
import java.util.Set;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.ow2.joram.mom.amqp.exceptions.AccessRefusedException;
import org.ow2.joram.mom.amqp.exceptions.NoConsumersException;
import org.ow2.joram.mom.amqp.exceptions.NotFoundException;
import org.ow2.joram.mom.amqp.exceptions.TransactionException;
import org.ow2.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;
import org.ow2.joram.mom.amqp.structures.PublishToQueue;

import fr.dyade.aaa.agent.AgentServer;

public abstract class IExchange implements IExchangeMBean, Externalizable {
  
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  public final static Logger logger = 
    fr.dyade.aaa.common.Debug.getLogger(IExchange.class.getName());
  
  public static final String DEFAULT_EXCHANGE_NAME = "";
  public static final String PREFIX_EXCHANGE = "Exchange_";
  
  private String saveName = null;

  protected String name;

  protected boolean durable;
  
  private long handledMessageCount;

  private long publishedMessageCount;

  private boolean published;
  
  public IExchange() { }
  
  public IExchange(String name, boolean durable) {
    this.name = name;
    this.durable = durable;
    saveName = PREFIX_EXCHANGE + Naming.getLocalName(name);
  }
  
  protected void publishToQueue(String queueName, String routingKey, boolean immediate,
      BasicProperties properties, byte[] body, int channelNumber, short serverId, long proxyId)
      throws NoConsumersException, TransactionException {
    published = true;
    publishedMessageCount++;
    if (Naming.isLocal(queueName)) {
      Queue queue = Naming.lookupQueue(queueName);
      queue.publish(new Message(name, routingKey, properties, body, Queue.FIRST_DELIVERY, false), immediate,
          serverId, proxyId);
    } else {
      StubAgentOut.asyncSend(new PublishToQueue(queueName, name, routingKey, immediate, properties, body,
          channelNumber, serverId, proxyId), Naming.resolveServerId(queueName));
    }
  }

  protected void checkPublication(boolean mandatory) throws NotFoundException {
    if (!published && mandatory) {
      throw new NotFoundException("No binding found for publication.");
    }
    published = false;
  }

  public final void publish(String routingKey, boolean mandatory, boolean immediate,
      BasicProperties properties, byte[] body, int channelNumber, short serverId, long proxyId)
      throws NoConsumersException, NotFoundException, TransactionException {
    handledMessageCount++;
    doPublish(routingKey, mandatory, immediate, properties, body, channelNumber, serverId, proxyId);
  }

  public abstract void doPublish(String routingKey, boolean mandatory, boolean immediate,
      BasicProperties properties, byte[] body, int channelNumber, short serverId, long proxyId)
      throws NoConsumersException, NotFoundException, TransactionException;

  public abstract void bind(String queueName, String routingKey, Map<String, Object> arguments);

  public abstract void unbind(String queueName, String routingKey, Map<String, Object> arguments)
      throws NotFoundException;
  
  public abstract boolean isUnused();

  public abstract void setArguments(Map<String, Object> arguments);

  public abstract void removeQueueBindings(String queueName) throws TransactionException;

  public abstract Set<String> getBoundQueues();
  
  public String getName() {
    return name;
  }

  public long getHandledMessageCount() {
    return handledMessageCount;
  }

  public long getPublishedMessageCount() {
    return publishedMessageCount;
  }

  //**********************************************************
  //* Persistence
  //**********************************************************
  public boolean isDurable() {
    return durable;
  }

  public static IExchange loadExchange(String name) throws IOException, ClassNotFoundException, TransactionException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "IExchange.loadExchange(" + name + ')');

    // load IExchange
    IExchange exchange = (IExchange) AgentServer.getTransaction().load(name);
    try {
      Naming.bindExchange(exchange.name, exchange);
    } catch (AlreadyBoundException exc) {
      // TODO 
      exc.printStackTrace();
    }
    return exchange;
  }

  protected final void createExchange() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "IExchange.createExchange(" + name + ')');
    try {
      AgentServer.getTransaction().create(this, saveName);
    } catch (IOException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "IExchange.createExchange ERROR::", e);
      //throw new TransactionException(e.getMessage());  
    }
  }

  protected final void saveExchange() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "IExchange.saveExchange(" + name + ')');
    try {
      AgentServer.getTransaction().save(this, saveName);
    } catch (IOException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "IExchange.saveExchange ERROR::", e);
      //throw new TransactionException(e.getMessage());  
    }
  }

  protected final void deleteExchange() throws AccessRefusedException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "IExchange.deleteExchange(" + name + ')');
    if (name.equals(DEFAULT_EXCHANGE_NAME)) {
      throw new AccessRefusedException("Can't delete default exchange.");
    }
    if (durable) {
      AgentServer.getTransaction().delete(saveName);
    }
  }

  /**
   * @param out
   * @throws IOException
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(name);
    out.writeBoolean(durable);
  }

  /**
   * @param in
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    name = (String) in.readObject();
    durable = in.readBoolean();
    saveName = PREFIX_EXCHANGE + Naming.getLocalName(name);
  }
  
}
