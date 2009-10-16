/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.amqp;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.util.Map;
import java.util.Set;

import org.objectweb.joram.mom.amqp.exceptions.NoConsumersException;
import org.objectweb.joram.mom.amqp.exceptions.NotFoundException;
import org.objectweb.joram.mom.amqp.exceptions.TransactionException;
import org.objectweb.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;
import org.objectweb.joram.mom.amqp.structures.PublishToQueue;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.util.Transaction;

public abstract class IExchange implements Serializable {
  
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  public final static Logger logger = 
    fr.dyade.aaa.common.Debug.getLogger(IExchange.class.getName());
  
  public static final String DEFAULT_EXCHANGE_NAME = "";
  public static final String PREFIX_EXCHANGE = "Exchange_";
  
  private String saveName = null;

  protected String name;

  protected boolean durable;
  
  private boolean published;
  
  protected static Transaction transaction = AgentServer.getTransaction();

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

  public abstract void publish(String routingKey, boolean mandatory, boolean immediate,
      BasicProperties properties, byte[] body, int channelNumber, short serverId, long proxyId)
      throws NoConsumersException, NotFoundException, TransactionException;

  public abstract void bind(String queueName, String routingKey, Map<String, Object> arguments);

  public abstract void unbind(String queueName, String routingKey, Map<String, Object> arguments);
  
  public abstract boolean isUnused();

  public abstract void setArguments(Map<String, Object> arguments);

  public abstract void removeQueueBindings(String queueName) throws TransactionException;

  public abstract Set<String> getBoundQueues();
  
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
    IExchange exchange = (IExchange) transaction.load(name);
    try {
      Naming.bindExchange(exchange.name, exchange);
    } catch (AlreadyBoundException exc) {
      // TODO 
      exc.printStackTrace();
    }
    return exchange;
  }

  protected final void saveExchange() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "IExchange.saveExchange(" + name + ')');
    try {
      transaction.create(this, saveName);
    } catch (IOException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "IExchange.saveExchange ERROR::", e);
      //throw new TransactionException(e.getMessage());  
    }
  }

  protected final void deleteExchange() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "IExchange.deleteExchange(" + name + ')');
    if (durable) {
      transaction.delete(saveName);
    }
  }

  /**
   * @param out
   * @throws IOException
   */
  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    // Writes exchange name
    out.writeObject(name);
    // Writes durable
    out.writeBoolean(durable);
  }

  /**
   * @param in
   * @throws IOException
   * @throws ClassNotFoundException
   */
  private void readObject(java.io.ObjectInputStream in)
  throws IOException, ClassNotFoundException {
    // Reads exchange name
    name = (String) in.readObject();
    // Reads durable
    durable = in.readBoolean();
    saveName = PREFIX_EXCHANGE + Naming.getLocalName(name);
  }
  
}
