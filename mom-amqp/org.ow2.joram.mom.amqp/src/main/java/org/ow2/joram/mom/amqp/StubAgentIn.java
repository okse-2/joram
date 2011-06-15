/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 - 2011 ScalAgent Distributed Technologies
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

import java.util.ArrayList;
import java.util.List;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.ow2.joram.mom.amqp.exceptions.AMQPException;
import org.ow2.joram.mom.amqp.exceptions.AccessRefusedException;
import org.ow2.joram.mom.amqp.exceptions.CommandInvalidException;
import org.ow2.joram.mom.amqp.exceptions.NoConsumersException;
import org.ow2.joram.mom.amqp.exceptions.NotAllowedException;
import org.ow2.joram.mom.amqp.exceptions.NotFoundException;
import org.ow2.joram.mom.amqp.exceptions.NotImplementedException;
import org.ow2.joram.mom.amqp.exceptions.PreconditionFailedException;
import org.ow2.joram.mom.amqp.exceptions.ResourceLockedException;
import org.ow2.joram.mom.amqp.exceptions.SyntaxErrorException;
import org.ow2.joram.mom.amqp.exceptions.TransactionException;
import org.ow2.joram.mom.amqp.marshalling.AMQP;
import org.ow2.joram.mom.amqp.marshalling.AbstractMarshallingMethod;
import org.ow2.joram.mom.amqp.structures.Ack;
import org.ow2.joram.mom.amqp.structures.AddBoundExchange;
import org.ow2.joram.mom.amqp.structures.Cancel;
import org.ow2.joram.mom.amqp.structures.Deliver;
import org.ow2.joram.mom.amqp.structures.PublishToQueue;
import org.ow2.joram.mom.amqp.structures.Recover;
import org.ow2.joram.mom.amqp.structures.RemoveBoundExchange;
import org.ow2.joram.mom.amqp.structures.RemoveQueueBindings;
import org.ow2.joram.mom.amqp.structures.Returned;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.common.Debug;

/**
 * The {@link StubAgentIn} class handles input interactions with other Joram
 * AMQP servers.
 */
public class StubAgentIn {

  static class Null {
  }

  /** logger */
  public static Logger logger = Debug.getLogger(StubAgentIn.class.getName());

  private static Null nullResponse = new Null();

  /**
   * @param from
   * @param keyLock
   * @param response
   */
  public static void processResponse(AgentId from, long keyLock, Object response) {   
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "processResponse(" + from + ", " + keyLock + ", " + response + ')');

    if(response instanceof Deliver) {
      ProxyName pxName = new ProxyName(((Deliver) response).serverId, ((Deliver) response).proxyId);
      Proxy proxy = Naming.lookupProxy(pxName);
      if (proxy == null) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "processResponse recover Deliver: queue = " + ((Deliver) response).queueName + ", msgId = " + ((Deliver) response).msgId);
        AMQPRequestNot not = new AMQPRequestNot();
        List<Long> list = new ArrayList<Long>();
        list.add(Long.valueOf(((Deliver) response).msgId));
        not.obj = new Recover(((Deliver) response).queueName, list);
        Channel.sendTo(from, not);
      } else {
        proxy.send((Deliver) response, new QueueShell(((Deliver) response).queueName));
      }
    } else if (response instanceof Returned) {
      ProxyName pxName = new ProxyName(((Returned) response).serverId, ((Returned) response).proxyId);
      Proxy proxy = Naming.lookupProxy(pxName);
      if (proxy == null) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "processResponse nothing to do (Returned).");
      } else {
        proxy.send((Returned) response);
      }
    } else {
      if (keyLock > 0) {
        AMQPAgent.putResponse(from, keyLock, response);
      }
    }
  }
  
  /**
   * @param from
   * @param keyLock
   * @param request
   */
  public static void processRequest(AgentId from, long keyLock, long proxyId, Object request) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "processRequest(" + from + ", " + keyLock + ", " + request + ')');

    Object response = null;
    try {
      if (request instanceof AbstractMarshallingMethod) {  
        response = doProcessMethod((AbstractMarshallingMethod) request, from.getFrom(), proxyId);
      } else if (request instanceof Ack) {
        basicAck(((Ack) request).getQueueName(), ((Ack) request).getIdsToAck());
      } else if(request instanceof Recover) {
        basicRecover(((Recover) request).getQueueName(), ((Recover) request).getIdsToRecover());
        response = new AMQP.Basic.RecoverOk();
      } else if(request instanceof Cancel) {
        Cancel cancel = (Cancel) request;
        response = basicCancel(cancel.getConsumerTag(), cancel.getQueueName(), cancel.getChannelNumber(),
            from.getFrom(), proxyId);
      } else if(request instanceof PublishRequest) {
        basicPublish((PublishRequest) request, from.getFrom(), proxyId);
      } else if (request instanceof PublishToQueue) {
        publishToQueue((PublishToQueue) request);
      } else if (request instanceof AddBoundExchange) {
        addBoundExchange((AddBoundExchange) request, from.getFrom(), proxyId);
      } else if (request instanceof RemoveQueueBindings) {
        removeQueueBindings((RemoveQueueBindings) request);
      } else if (request instanceof RemoveBoundExchange) {
        removeBoundExchange((RemoveBoundExchange) request, from.getFrom(), proxyId);
      }
    } catch (AMQPException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "StubAgentIn: ERROR:: " + exc.getMessage());
      }
        response = exc;
    }
    
    if (response != null) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "processRequest response = " + response);
      AMQPResponseNot not = new AMQPResponseNot();
      not.keyLock = keyLock;
      if (response != nullResponse) {
        not.obj = response;
      }
      Channel.sendTo(AMQPAgent.getAMQPId(from.getFrom()), not);
    }
  }

  /**
   * @param method
   * @throws Exception
   */
  private static Object doProcessMethod(AbstractMarshallingMethod method, short serverId, long proxyId) throws AMQPException {
    Object response = null;
    
    if (method != null) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "doProcess marshallingMethod = " + method);

      int channelNumber = method.channelNumber;
      
      switch (method.getClassId()) {

      /******************************************************
       * Class Connection
       ******************************************************/
      case AMQP.Connection.INDEX:
        throw new IllegalStateException();

      /******************************************************
       * Class Channel
       ******************************************************/
      case AMQP.Channel.INDEX:
        throw new IllegalStateException();

      /******************************************************
       * Class Queue
       ******************************************************/
      case AMQP.Queue.INDEX:
        switch (method.getMethodId()) {
        case AMQP.Queue.Declare.INDEX:
          AMQP.Queue.Declare declare = (AMQP.Queue.Declare) method;
          AMQP.Queue.DeclareOk declareOk = queueDeclare(declare, serverId, proxyId);
          if (declare.noWait == false) {
            declareOk.channelNumber = channelNumber;
            response = declareOk;
          }
          break;

        case AMQP.Queue.Delete.INDEX:
          AMQP.Queue.Delete delete = (AMQP.Queue.Delete) method;
          AMQP.Queue.DeleteOk deleteOk = queueDelete(delete, serverId, proxyId);
          if (delete.noWait == false) {
            deleteOk.channelNumber = channelNumber;
            response = deleteOk;
          }
          break;

        case AMQP.Queue.Bind.INDEX:
          AMQP.Queue.Bind bind = (AMQP.Queue.Bind) method;
          queueBind(bind, serverId, proxyId);
          if (bind.noWait == false) {
            AMQP.Queue.BindOk bindOk = new AMQP.Queue.BindOk();
            bindOk.channelNumber = channelNumber;
            response = bindOk;
          }
          break;
          
        case AMQP.Queue.Unbind.INDEX:
          AMQP.Queue.Unbind unbind = (AMQP.Queue.Unbind) method;
          queueUnbind(unbind, serverId, proxyId);
          AMQP.Queue.UnbindOk unbindOk = new AMQP.Queue.UnbindOk();
          unbindOk.channelNumber = channelNumber;
          response = unbindOk;
          break;
          
        case AMQP.Queue.Purge.INDEX:
            AMQP.Queue.Purge purge = (AMQP.Queue.Purge) method;
            AMQP.Queue.PurgeOk purgeOk = queuePurge(purge, serverId, proxyId);
            if (purge.noWait == false) {
              purgeOk.channelNumber = channelNumber;
              response = purgeOk;
            }
          break;

        default:
          break;
        }
        break;

      /******************************************************
       * Class BASIC
       ******************************************************/
      case AMQP.Basic.INDEX:

        switch (method.getMethodId()) {

        case AMQP.Basic.Get.INDEX:
          AMQP.Basic.Get get = (AMQP.Basic.Get) method;
          response = basicGet(get, serverId, proxyId);
          if (response == null) {
            return nullResponse;
          }
          break;

        case AMQP.Basic.Ack.INDEX:
          throw new IllegalStateException();

        case AMQP.Basic.Consume.INDEX:
          AMQP.Basic.Consume consume = (AMQP.Basic.Consume) method;
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "consume = " + consume);
          basicConsume(consume, serverId, proxyId);
          break;

        case AMQP.Basic.Cancel.INDEX:
          throw new IllegalStateException();
          
        case AMQP.Basic.Reject.INDEX:
          throw new IllegalStateException();

        case AMQP.Basic.RecoverAsync.INDEX:
          throw new IllegalStateException();

        case AMQP.Basic.Recover.INDEX:
          throw new IllegalStateException();

        case AMQP.Basic.Qos.INDEX:
          throw new NotImplementedException("Qos method currently not implemented.");

        default:
          break;
        }
        break;

      /******************************************************
       * Class Exchange
       ******************************************************/
      case AMQP.Exchange.INDEX:

        switch (method.getMethodId()) {
        case AMQP.Exchange.Declare.INDEX:
          AMQP.Exchange.Declare declare = (AMQP.Exchange.Declare) method;
          exchangeDeclare(declare);
          if (declare.noWait == false) {
            AMQP.Exchange.DeclareOk declareOk = new AMQP.Exchange.DeclareOk();
            declareOk.channelNumber = channelNumber;
            response = declareOk;
          }
          break;

        case AMQP.Exchange.Delete.INDEX:
          AMQP.Exchange.Delete delete = (AMQP.Exchange.Delete) method;
          exchangeDelete(delete);
          if (delete.noWait == false) {
            AMQP.Exchange.DeleteOk deleteOk = new AMQP.Exchange.DeleteOk();
            deleteOk.channelNumber = channelNumber;
            response = deleteOk;
          }
          break;

        default:
          break;
        }
        break;

      /******************************************************
       * Class Tx
       ******************************************************/
      case AMQP.Tx.INDEX:
        break;

      default:
        break;
      }
    }
    return response;
  }  

  
  private static void removeQueueBindings(RemoveQueueBindings request) throws TransactionException {
    IExchange exchange = Naming.lookupExchange(request.getExchangeName());
    if (exchange != null) {
      exchange.removeQueueBindings(request.getQueueName());
    }
  }

  private static void addBoundExchange(AddBoundExchange request, short serverId, long proxyId)
      throws ResourceLockedException, TransactionException {
    Queue queue = Naming.lookupQueue(request.getQueueName());
    if (queue != null) {
      queue.addBoundExchange(request.getExchangeName(), serverId, proxyId);
    }
  }

  private static void publishToQueue(PublishToQueue request) throws TransactionException {
    Queue queue = Naming.lookupQueue(request.getQueueName());
    try {
      if (queue != null) {
        queue.publish(request.getMessage(), request.isImmediate(), request.getServerId(), request.getProxyId());
      }
    } catch (NoConsumersException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Immediate Exception: " + exc.getMessage());
      AMQPResponseNot not = new AMQPResponseNot();
      AMQP.Basic.Return returned = new AMQP.Basic.Return(exc.getCode(), exc.getMessage(), request
          .getExchangeName(), request.getRoutingKey());
      returned.channelNumber = request.getChannelNumber();
      not.obj = new Returned(returned, request.getProperties(), request.getBody(), request.getServerId(), request.getProxyId());
      not.keyLock = -1;
      Channel.sendTo(AMQPAgent.getAMQPId(request.getServerId()), not);
    }
  }

  private static void removeBoundExchange(RemoveBoundExchange request, short serverId, long proxyId)
      throws ResourceLockedException {
    Queue queue = Naming.lookupQueue(request.getQueueName());
    if (queue != null) {
      queue.removeBoundExchange(request.getExchangeName(), serverId, proxyId);
    }
  }
  
  /* ******************************************* */
  /* ******************************************* */
  /* ************* IProxy interface ************ */
  /* ******************************************* */
  /* ******************************************* */
 
  public static void basicAck(String queueName, List<Long> idsToAck) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "basicAck(" + queueName + ", " + idsToAck + ')');
    Queue queue = Naming.lookupQueue(queueName);
    queue.ackMessages(idsToAck);
  }

  public static Boolean basicCancel(String consumerTag, String queueName, int channelNumber, short serverId,
      long proxyId) throws NotFoundException, ResourceLockedException, PreconditionFailedException, TransactionException {
    Queue queue = Naming.lookupQueue(queueName);
    if (queue != null) {
      queue.cancel(consumerTag, channelNumber, serverId, proxyId);
      if (queue.getConsumerCount() == 0 && queue.isAutodelete()) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "StubAgentIn: no more consumers -> autodelete");
        StubLocal.queueDelete(queueName, true, true, serverId, proxyId);
        return Boolean.TRUE;
      }
    }
    return Boolean.FALSE;
  }

  public static void basicConsume(AMQP.Basic.Consume basicConsume, short serverId, long proxyId)
      throws NotFoundException, ResourceLockedException, AccessRefusedException {
    StubLocal.basicConsume(AMQPAgent.stubAgentOut, basicConsume.queue, basicConsume.consumerTag,
        basicConsume.exclusive, basicConsume.noAck, basicConsume.noLocal, basicConsume.channelNumber,
        serverId, proxyId);
  }

  public static Message basicGet(AMQP.Basic.Get basicGet, short serverId, long proxyId)
      throws NotFoundException, ResourceLockedException, TransactionException {
    Message msg = StubLocal.basicGet(basicGet.queue, basicGet.noAck, serverId, proxyId);
    if (msg != null) {
      msg.queueName = basicGet.queue;
    }
    return msg;
  }

  public static void basicPublish(PublishRequest publishRequest, short serverId, long proxyId)
      throws NotFoundException {
    try {
      StubLocal.basicPublish(publishRequest, serverId, proxyId);
    } catch (AMQPException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Publish Exception: " + exc.getMessage());
      AMQPResponseNot not = new AMQPResponseNot();
      AMQP.Basic.Return returned = new AMQP.Basic.Return(exc.getCode(), exc.getMessage(), publishRequest.getPublish().exchange, publishRequest.getPublish().routingKey);
      returned.channelNumber = publishRequest.channel;
      not.obj = new Returned(returned, publishRequest.getHeader(), publishRequest.getBody(), serverId, proxyId);
      not.keyLock = -1;
      Channel.sendTo(AMQPAgent.getAMQPId(serverId), not);
    }
  }

  public static void basicRecover(String queueName, List<Long> idsToRecover) throws TransactionException {
    Queue queue = Naming.lookupQueue(queueName);
    queue.recoverMessages(idsToRecover);
  }

  public static void exchangeDeclare(AMQP.Exchange.Declare exchangeDeclare) throws CommandInvalidException,
      PreconditionFailedException, NotAllowedException, NotFoundException, AccessRefusedException {
    StubLocal.exchangeDeclare(exchangeDeclare.exchange, exchangeDeclare.type, exchangeDeclare.durable,
        exchangeDeclare.passive);
  }

  public static void exchangeDelete(AMQP.Exchange.Delete exchangeDelete) throws NotFoundException,
      PreconditionFailedException, AccessRefusedException {
    StubLocal.exchangeDelete(exchangeDelete.exchange, exchangeDelete.ifUnused);
  }

  public static void queueBind(AMQP.Queue.Bind queueBind, short serverId, long proxyId)
      throws NotFoundException, ResourceLockedException, TransactionException {
    StubLocal.queueBind(queueBind.queue, queueBind.exchange, queueBind.routingKey, queueBind.arguments,
        serverId, proxyId);
  }

  public static AMQP.Queue.DeclareOk queueDeclare(AMQP.Queue.Declare queueDeclare, short serverId,
      long proxyId) throws ResourceLockedException, NotFoundException, PreconditionFailedException,
      AccessRefusedException, TransactionException {
    return StubLocal.queueDeclare(queueDeclare.queue, queueDeclare.passive, queueDeclare.durable,
        queueDeclare.autoDelete, queueDeclare.exclusive, serverId, proxyId);
  }

  public static AMQP.Queue.DeleteOk queueDelete(AMQP.Queue.Delete queueDelete, short serverId, long proxyId)
      throws NotFoundException, PreconditionFailedException, ResourceLockedException, TransactionException {
    int msgCount = StubLocal.queueDelete(queueDelete.queue, queueDelete.ifUnused, queueDelete.ifEmpty,
        serverId, proxyId);
    return new AMQP.Queue.DeleteOk(msgCount);
  }

  public static AMQP.Queue.PurgeOk queuePurge(AMQP.Queue.Purge queuePurge, short serverId, long proxyId)
      throws NotFoundException, SyntaxErrorException, ResourceLockedException, TransactionException {
    return new AMQP.Queue.PurgeOk(StubLocal.queuePurge(queuePurge.queue, serverId, proxyId));
  }

  public static void queueUnbind(AMQP.Queue.Unbind queueUnbind, short serverId, long proxyId)
      throws NotFoundException, ResourceLockedException {
    StubLocal.queueUnbind(queueUnbind.exchange, queueUnbind.queue, queueUnbind.routingKey,
        queueUnbind.arguments, serverId, proxyId);
  }

}
