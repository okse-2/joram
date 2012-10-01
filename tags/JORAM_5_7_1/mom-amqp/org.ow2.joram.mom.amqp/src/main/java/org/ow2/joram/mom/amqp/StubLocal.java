/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 ScalAgent Distributed Technologies
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

import java.rmi.AlreadyBoundException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ow2.joram.mom.amqp.exceptions.AccessRefusedException;
import org.ow2.joram.mom.amqp.exceptions.CommandInvalidException;
import org.ow2.joram.mom.amqp.exceptions.NoConsumersException;
import org.ow2.joram.mom.amqp.exceptions.NotAllowedException;
import org.ow2.joram.mom.amqp.exceptions.NotFoundException;
import org.ow2.joram.mom.amqp.exceptions.PreconditionFailedException;
import org.ow2.joram.mom.amqp.exceptions.ResourceLockedException;
import org.ow2.joram.mom.amqp.exceptions.SyntaxErrorException;
import org.ow2.joram.mom.amqp.exceptions.TransactionException;
import org.ow2.joram.mom.amqp.marshalling.AMQP;
import org.ow2.joram.mom.amqp.structures.AddBoundExchange;
import org.ow2.joram.mom.amqp.structures.RemoveBoundExchange;
import org.ow2.joram.mom.amqp.structures.RemoveQueueBindings;

/**
 * The {@link StubLocal} class handles interactions with local AMQP objects.
 */
public class StubLocal {

  /**
   * The exchange name consists of a non-empty sequence of these characters:
   * letters, digits, hyphen, underscore, period, or colon.
   */
  private static final Pattern exchangeNamePattern = Pattern.compile("[-_.:a-zA-Z0-9]*");

  public static AMQP.Queue.DeclareOk queueDeclare(String queueName, boolean passive, boolean durable,
      boolean autoDelete, boolean exclusive, short serverId, long proxyId) throws NotFoundException,
      ResourceLockedException, PreconditionFailedException, AccessRefusedException, TransactionException {
    // Generate queue name if unspecified
    if (queueName.equals("")) {
      queueName = Naming.nextQueueName();
    }

    // Check if the queue already exists
    Queue queue = Naming.lookupQueue(queueName);

    if (queue == null) {
      if (passive) {
        throw new NotFoundException("Passive declaration of an unknown queue: '" + queueName + "'.");
      }
      checkName(queueName);
      queue = new Queue(queueName, durable, autoDelete, exclusive, serverId, proxyId);
      try {
        Naming.bindQueue(queueName, queue);
      } catch (AlreadyBoundException exc) {
        // TODO 
      }

      // All message queues MUST BE automatically bound to the nameless exchange using the
      // message queue's name as routing key.
      queueBind(queueName, IExchange.DEFAULT_EXCHANGE_NAME, queueName, null, serverId, proxyId);
      return new AMQP.Queue.DeclareOk(queueName, 0, 0);

    } else {
      if (!passive) {
        // If passive is not set and the queue exists, the server MUST check that the
        // existing queue has the same values for durable, exclusive, auto-delete,
        // and arguments fields.
        if (durable != queue.isDurable()) {
          throw new PreconditionFailedException("Queue durable property do not match existing queue '"
              + queueName + "'.");
        }
        if (exclusive != queue.isExclusive()) {
          throw new ResourceLockedException("Queue exclusive property do not match existing queue '"
              + queueName + "'.");
        }
        if (autoDelete != queue.isAutodelete()) {
          throw new PreconditionFailedException("Queue autodelete property do not match existing queue one '"
              + queueName + "'.");
        }
      }
      return queue.getInfo(serverId, proxyId);
    }
  }

  public static int queueDelete(String queueName, boolean ifUnused, boolean ifEmpty, short serverId,
      long proxyId) throws PreconditionFailedException, NotFoundException, ResourceLockedException,
      TransactionException {
    Queue queue = Naming.lookupQueue(queueName);
    if (queue == null) {
      throw new NotFoundException("Unknown queue for deletion: '" + queueName + "'.");
    }

    if (ifEmpty && queue.getToDeliverMessageCount() > 0) {
      throw new PreconditionFailedException("Deletion error: queue '" + queueName + "' is not empty.");
    }
    if (ifUnused && queue.getConsumerCount() > 0) {
      throw new PreconditionFailedException("Deletion error: queue '" + queueName + "' is not unused.");
    }
    queue.deleteQueue(queueName, serverId, proxyId);
    Naming.unbindQueue(queueName);

    // Unbind exchanges bound to the queue
    List<String> boundExchanges = queue.getBoundExchanges();
    Iterator<String> exchangesIterator = boundExchanges.iterator();
    while (exchangesIterator.hasNext()) {
      String exchangeName = exchangesIterator.next();
      if (Naming.isLocal(exchangeName)) {
        IExchange exchange = Naming.lookupExchange(exchangeName);
        if (exchange != null) {
          exchange.removeQueueBindings(queueName);
        }
      } else {
        StubAgentOut.asyncSend(new RemoveQueueBindings(exchangeName, queueName), Naming.resolveServerId(exchangeName));
      }
    }
    
    return queue.getToDeliverMessageCount();
  }

  public static void queueBind(String queueName, String exchangeName, String routingKey,
      Map<String, Object> arguments, short serverId, long proxyId) throws NotFoundException,
      ResourceLockedException, TransactionException {
    IExchange exchange = Naming.lookupExchange(exchangeName);
    if (exchange == null) {
      throw new NotFoundException("Binding to a non-existent exchange: '" + exchangeName + "'.");
    }
    if (Naming.isLocal(queueName)) {
      Queue queue = Naming.lookupQueue(queueName);
      if (queue == null) {
        throw new NotFoundException("Binding to a non-existent queue: '" + queueName + "'.");
      }
      queue.addBoundExchange(exchangeName, serverId, proxyId);
    } else {
      StubAgentOut.asyncSend(new AddBoundExchange(queueName, exchangeName), Naming.resolveServerId(queueName));
    }
    exchange.bind(queueName, routingKey, arguments);
  }

  public static void queueUnbind(String exchangeName, String queueName, String routingKey,
      Map<String, Object> arguments, short serverId, long proxyId) throws NotFoundException,
      ResourceLockedException {
    IExchange exchange = Naming.lookupExchange(exchangeName);
    if (exchange != null) {
      if (Naming.isLocal(queueName)) {
        Queue queue = Naming.lookupQueue(queueName);
        if (queue != null) {
          queue.removeBoundExchange(exchangeName, serverId, proxyId);
          exchange.unbind(queueName, routingKey, arguments);
        } else {
          throw new NotFoundException("Queue not found for unbinding: '" + queueName + "'.");
        }
      } else {
        StubAgentOut.asyncSend(new RemoveBoundExchange(queueName, exchangeName), Naming.resolveServerId(queueName));
      }
    } else {
      throw new NotFoundException("Exchange not found for unbinding: '" + exchangeName + "'.");
    }
  }

  public static int queuePurge(String queueName, short serverId, long proxyId) throws SyntaxErrorException,
      NotFoundException, ResourceLockedException, TransactionException {
    Queue queue = Naming.lookupQueue(queueName);
    if (queue == null) {
      throw new NotFoundException("Purging non-existent queue: '" + queueName + "'.");
    }
    return queue.clear(serverId, proxyId);
  }

  private static void checkName(String name) throws AccessRefusedException, PreconditionFailedException {
    if (name.startsWith("amq.")) {
      throw new AccessRefusedException(
          "Queue or Exchange names starting with 'amq.' are reserved for pre-declared and standardised queues or exchanges.");
    }
    // The exchange name consists of a non-empty sequence of these characters:
    // letters, digits, hyphen, underscore, period, or colon.
    Matcher m = exchangeNamePattern.matcher(name);
    if (!m.matches()) {
      throw new PreconditionFailedException("Exchange name contains an invalid character: '" + name + "'.");
    }
  }

  public static void exchangeDeclare(String exchangeName, String type, boolean durable, boolean passive)
      throws NotFoundException, CommandInvalidException, NotAllowedException, PreconditionFailedException,
      AccessRefusedException {
    IExchange exchange = Naming.lookupExchange(exchangeName);
    if (exchange == null) {
      if (passive) {
        throw new NotFoundException("Passive declaration of an unknown exchange: '" + exchangeName + "'.");
      }
      checkName(exchangeName);
      if (type.equalsIgnoreCase(DirectExchange.TYPE)) {
        exchange = new DirectExchange(exchangeName, durable);
      } else if (type.equalsIgnoreCase(TopicExchange.TYPE)) {
        exchange = new TopicExchange(exchangeName, durable);
      } else if (type.equalsIgnoreCase(FanoutExchange.TYPE)) {
        exchange = new FanoutExchange(exchangeName, durable);
      } else if (type.equalsIgnoreCase(HeadersExchange.TYPE)) {
        exchange = new HeadersExchange(exchangeName, durable);
      } else {
        try {
          Class exchangeClass = Class.forName(type);
          exchange = (IExchange) exchangeClass.newInstance();
        } catch (ClassNotFoundException exc) {
          throw new CommandInvalidException("Unknown exchange type: " + type);
        } catch (Exception exc) {
          throw new CommandInvalidException(exc.getMessage());
        }
      }
      //      exchange.setArguments(arguments);
      try {
        Naming.bindExchange(exchangeName, exchange);
      } catch (AlreadyBoundException exc) {
        // TODO
      }
    } else {
      if (passive) {
        return;
      }
      // Check if exchange type corresponds with existing exchange
      if (type.equalsIgnoreCase(DirectExchange.TYPE)) {
        if (!(exchange instanceof DirectExchange)) {
          throw new NotAllowedException("Exchange type do not match existing exchange '" + exchangeName + "'.");
        }
      } else if (type.equalsIgnoreCase(TopicExchange.TYPE)) {
        if (!(exchange instanceof TopicExchange)) {
          throw new NotAllowedException("Exchange type do not match existing exchange '" + exchangeName + "'.");
        }
      } else if (type.equalsIgnoreCase(FanoutExchange.TYPE)) {
        if (!(exchange instanceof FanoutExchange)) {
          throw new NotAllowedException("Exchange type do not match existing exchange '" + exchangeName + "'.");
        }
      } else if (type.equalsIgnoreCase(HeadersExchange.TYPE)) {
        if (!(exchange instanceof HeadersExchange)) {
          throw new NotAllowedException("Exchange type do not match existing exchange '" + exchangeName + "'.");
        }
      } else {
        if (!exchange.getClass().getName().equals(type)) {
          throw new NotAllowedException("Exchange type do not match existing exchange '" + exchangeName + "'.");
        }
      }

      if (durable != exchange.isDurable()) {
        throw new PreconditionFailedException("Exchange durable property do not match existing exchange '"
            + exchangeName + "'.");
      }
    }
  }

  public static void exchangeDelete(String exchangeName, boolean ifUnused) throws NotFoundException,
      PreconditionFailedException, AccessRefusedException {
    IExchange exchange = Naming.lookupExchange(exchangeName);
    if (exchange == null) {
      throw new NotFoundException("Exchange not found for deletion: '" + exchangeName + "'.");
    }
    if (ifUnused && !exchange.isUnused()) {
      throw new PreconditionFailedException("Deletion error: Exchange '" + exchangeName + "' is not unused.");
    }
    
    exchange.deleteExchange();
    Naming.unbindExchange(exchangeName);

    // Unbind queues bound to the exchange
    Set<String> boundQueues = exchange.getBoundQueues();
    Iterator<String> queuesIterator = boundQueues.iterator();
    while (queuesIterator.hasNext()) {
      String queueName = queuesIterator.next();
      if (Naming.isLocal(queueName)) {
        Queue queue = Naming.lookupQueue(queueName);
        queue.removeBoundExchange(exchangeName);
      } else {
        StubAgentOut.asyncSend(new RemoveBoundExchange(queueName, exchangeName), Naming.resolveServerId(queueName));
      }
    }
  }

  public static Message basicGet(String queueName, boolean noAck, short serverId, long proxyId)
      throws NotFoundException, ResourceLockedException, TransactionException {
    Queue queue = Naming.lookupQueue(queueName);
    if (queue == null) {
      throw new NotFoundException("Can't get message on an unknown queue: '" + queueName + "'.");
    }
    Message msg = queue.receive(noAck, serverId, proxyId);
    return msg;
  }

  public static void basicConsume(DeliveryListener deliveryListener, String queueName,
      String consumerTag, boolean exclusive, boolean noAck, boolean noLocal, int channelNumber,
      short serverId, long proxyId) throws NotFoundException, ResourceLockedException, AccessRefusedException {
    Queue queue = Naming.lookupQueue(queueName);
    if (queue == null) {
      throw new NotFoundException("Consuming from non-existent queue: '" + queueName + "'.");
    }
    queue.consume(deliveryListener, channelNumber, consumerTag, exclusive, noAck, noLocal, serverId, proxyId);
  }

  public static void basicPublish(PublishRequest publishRequest, short serverId, long proxyId)
      throws NotFoundException, NoConsumersException, TransactionException {
    IExchange exchange = Naming.lookupExchange(publishRequest.getPublish().exchange);
    if (exchange == null) {
      throw new NotFoundException("Can't publish on an unknwon exchange: '"
          + publishRequest.getPublish().exchange + "'.");
    }
    exchange.publish(publishRequest.getPublish().routingKey, publishRequest.getPublish().mandatory,
        publishRequest.getPublish().immediate, publishRequest.getHeader(), publishRequest.getBody(),
        publishRequest.channel, serverId, proxyId);
  }

}
