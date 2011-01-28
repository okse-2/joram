package org.ow2.joram.mom.amqp;

import java.rmi.AlreadyBoundException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

  public static AMQP.Queue.DeclareOk queueDeclare(String queueName, boolean passive, boolean durable,
      boolean autoDelete, boolean exclusive, short serverId, long proxyId) throws NotFoundException,
      ResourceLockedException, TransactionException {
    // Generate queue name if unspecified
    if (queueName.equals("")) {
      queueName = Naming.nextQueueName();
    }

    // Check if the queue already exists
    Queue queue = Naming.lookupQueue(queueName);

    if (queue == null) {
      if (passive) {
        throw new NotFoundException("Passive declaration of an unknown queue.");
      }
      queue = new Queue(queueName, durable, autoDelete, exclusive,
          serverId, proxyId);
      try {
        Naming.bindQueue(queueName, queue);
      } catch (AlreadyBoundException exc) {
        // TODO 
      }

      // All message queues MUST BE automatically bound to the nameless exchange using the
      // message queue's name as routing key.
      queueBind(queueName, IExchange.DEFAULT_EXCHANGE_NAME, queueName, null);
      return new AMQP.Queue.DeclareOk(queueName, 0, 0);

    } else {
      return queue.getInfo(serverId, proxyId);
    }
  }

  public static int queueDelete(String queueName, boolean ifUnused, boolean ifEmpty)
      throws PreconditionFailedException, NotFoundException, TransactionException {
    Queue queue = Naming.lookupQueue(queueName);
    if (queue == null) {
      throw new NotFoundException("Unknown queue for deletion: " + queueName);
    }

    if (ifEmpty && queue.getMessageCount() > 0) {
      throw new PreconditionFailedException("Queue not empty.");
    }
    if (ifUnused && queue.getConsumerCount() > 0) {
      throw new PreconditionFailedException("Queue not unused.");
    }
    Naming.unbindQueue(queueName);
    if (queue.isDurable())
      queue.deleteQueue(queueName);

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
    
    return queue.getMessageCount();
  }

  public static void queueBind(String queueName, String exchangeName, String routingKey,
      Map<String, Object> arguments) throws NotFoundException, TransactionException {
    IExchange exchange = Naming.lookupExchange(exchangeName);
    if (exchange == null) {
      throw new NotFoundException("Binding to a non-existent exchange.");
    }
    if (Naming.isLocal(queueName)) {
      Queue queue = Naming.lookupQueue(queueName);
      if (queue == null) {
        throw new NotFoundException("Binding to a non-existent queue.");
      }
      queue.addBoundExchange(exchangeName);
    } else {
      StubAgentOut.asyncSend(new AddBoundExchange(queueName, exchangeName), Naming.resolveServerId(queueName));
    }
    exchange.bind(queueName, routingKey, arguments);
  }

  public static void queueUnbind(String exchangeName, String queueName, String routingKey,
      Map<String, Object> arguments) throws NotFoundException {
    IExchange exchange = Naming.lookupExchange(exchangeName);
    if (exchange != null) {
      if (Naming.isLocal(queueName)) {
        Queue queue = Naming.lookupQueue(queueName);
        if (queue != null) {
          exchange.unbind(queueName, routingKey, arguments);
          queue.removeBoundExchange(exchangeName);
        } else {
          throw new NotFoundException("Queue not found.");
        }
      } else {
        StubAgentOut.asyncSend(new RemoveBoundExchange(queueName, exchangeName), Naming.resolveServerId(queueName));
      }
    } else {
      throw new NotFoundException("Exchange not found.");
    }
  }

  public static int queuePurge(String queueName, short serverId, long proxyId) throws SyntaxErrorException,
      NotFoundException, ResourceLockedException, TransactionException {
    Queue queue = Naming.lookupQueue(queueName);
    if (queue == null) {
      throw new NotFoundException("Purging non-existent queue");
    }
    return queue.clear(serverId, proxyId);
  }

  public static void exchangeDeclare(String exchangeName, String type, boolean durable, boolean passive)
      throws NotFoundException, CommandInvalidException, NotAllowedException {
    IExchange exchange = Naming.lookupExchange(exchangeName);
    if (exchange == null) {
      if (passive) {
        throw new NotFoundException("Passive declaration of an unknown exchange.");
      }
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
      // Check if exchange type corresponds with existing exchange
      if (type.equalsIgnoreCase(DirectExchange.TYPE)) {
        if (!(exchange instanceof DirectExchange)) {
          throw new NotAllowedException("Exchange type do not match existing exchange.");
        }
      } else if (type.equalsIgnoreCase(TopicExchange.TYPE)) {
        if (!(exchange instanceof TopicExchange)) {
          throw new NotAllowedException("Exchange type do not match existing exchange.");
        }
      } else if (type.equalsIgnoreCase(FanoutExchange.TYPE)) {
        if (!(exchange instanceof FanoutExchange)) {
          throw new NotAllowedException("Exchange type do not match existing exchange.");
        }
      } else if (type.equalsIgnoreCase(HeadersExchange.TYPE)) {
        if (!(exchange instanceof HeadersExchange)) {
          throw new NotAllowedException("Exchange type do not match existing exchange.");
        }
      } else {
        if (!exchange.getClass().getName().equals(type)) {
          throw new NotAllowedException("Exchange type do not match existing exchange.");
        }
      }
    }
  }

  public static void exchangeDelete(String exchangeName, boolean ifUnused) throws NotFoundException,
      PreconditionFailedException {
    IExchange exchange = Naming.lookupExchange(exchangeName);
    if (exchange == null) {
      throw new NotFoundException("Exchange not found for deletion.");
    }
    if (ifUnused && !exchange.isUnused()) {
      throw new PreconditionFailedException("Exchange not unused.");
    }
    Naming.unbindExchange(exchangeName);
    
    if (exchange.durable)
      exchange.deleteExchange();

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
      throw new NotFoundException("Can't get message on an unknown queue.");
    }
    Message msg = queue.receive(noAck, serverId, proxyId);
    return msg;
  }

  public static void basicConsume(DeliveryListener deliveryListener, String queueName,
      String consumerTag, boolean exclusive, boolean noAck, boolean noLocal, int channelNumber,
      short serverId, long proxyId) throws NotFoundException, ResourceLockedException, AccessRefusedException {
    Queue queue = Naming.lookupQueue(queueName);
    if (queue == null) {
      throw new NotFoundException("Consuming from non-existent queue.");
    }
    queue.consume(deliveryListener, channelNumber, consumerTag, exclusive, noAck, noLocal, serverId, proxyId);
  }

  public static void basicPublish(PublishRequest publishRequest, short serverId, long proxyId)
      throws NotFoundException, NoConsumersException, TransactionException {
    IExchange exchange = Naming.lookupExchange(publishRequest.getPublish().exchange);
    if (exchange == null) {
      throw new NotFoundException("Exchange " + publishRequest.getPublish().exchange + " not found.");
    }
    exchange.publish(publishRequest.getPublish().routingKey, publishRequest.getPublish().mandatory,
        publishRequest.getPublish().immediate, publishRequest.getHeader(), publishRequest.getBody(),
        publishRequest.channel, serverId, proxyId);
  }

}
