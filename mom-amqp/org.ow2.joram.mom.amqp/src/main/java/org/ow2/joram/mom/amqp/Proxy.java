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
import java.rmi.AlreadyBoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.ow2.joram.mom.amqp.ChannelContext.Delivery;
import org.ow2.joram.mom.amqp.exceptions.AMQPException;
import org.ow2.joram.mom.amqp.exceptions.AccessRefusedException;
import org.ow2.joram.mom.amqp.exceptions.ChannelException;
import org.ow2.joram.mom.amqp.exceptions.CommandInvalidException;
import org.ow2.joram.mom.amqp.exceptions.InternalErrorException;
import org.ow2.joram.mom.amqp.exceptions.NotAllowedException;
import org.ow2.joram.mom.amqp.exceptions.NotFoundException;
import org.ow2.joram.mom.amqp.exceptions.NotImplementedException;
import org.ow2.joram.mom.amqp.exceptions.PreconditionFailedException;
import org.ow2.joram.mom.amqp.exceptions.ResourceLockedException;
import org.ow2.joram.mom.amqp.exceptions.SyntaxErrorException;
import org.ow2.joram.mom.amqp.exceptions.TransactionException;
import org.ow2.joram.mom.amqp.marshalling.AMQP;
import org.ow2.joram.mom.amqp.marshalling.AMQP.Basic.Qos;
import org.ow2.joram.mom.amqp.marshalling.AbstractMarshallingMethod;
import org.ow2.joram.mom.amqp.structures.Ack;
import org.ow2.joram.mom.amqp.structures.Cancel;
import org.ow2.joram.mom.amqp.structures.Deliver;
import org.ow2.joram.mom.amqp.structures.GetDeliveries;
import org.ow2.joram.mom.amqp.structures.GetResponse;
import org.ow2.joram.mom.amqp.structures.Recover;
import org.ow2.joram.mom.amqp.structures.Returned;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Daemon;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.StoppedQueueException;
import fr.dyade.aaa.util.Transaction;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 * Handles the AMQP frames received by the {@link AMQPConnectionListener}.
 */
public class Proxy implements DeliveryListener, ProxyMBean {

  public static Logger logger = Debug.getLogger(Proxy.class.getName());

  public final static String PREFIX_PX = "AMQPPx"; 

  private static volatile long proxyId = 0;

  private static synchronized long getNextProxyId() {
    return proxyId++;
  }

  private ProxyName name;
  
  private fr.dyade.aaa.common.Queue queueIn = null;
  private java.util.Queue queueOut = null;

  private NetServerIn netServerIn;
  private Transaction transaction = null;

  public Proxy(fr.dyade.aaa.common.Queue queueIn, java.util.Queue queueOut) throws IOException {
    if (AgentServer.getTransaction().isPersistent())
      loadProxyId();
    this.name = new ProxyName(AgentServer.getServerId(), getNextProxyId());
    if (AgentServer.getTransaction().isPersistent())
      saveProxyId();
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "<Proxy> = " + name);
    try {
      Naming.bindProxy(name, this);
    } catch (AlreadyBoundException exc) {
      // Can't happen
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "Name already bound, should never happen.", exc);
    }
    this.queueIn = queueIn;
    this.queueOut = queueOut;
    netServerIn = new NetServerIn(name.toString());
    transaction = AgentServer.getTransaction();
  }

  public void loadProxyId() throws IOException {
    try {
      Long id = (Long) AgentServer.getTransaction().load(PREFIX_PX);
      if (id != null)
        proxyId = id.longValue();
    } catch (ClassNotFoundException e) {
      // Can't happen
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "loadProxyId", e);
    }
  }

  public void saveProxyId() throws IOException {
    AgentServer.getTransaction().create(new Long(proxyId), PREFIX_PX);
    AgentServer.getTransaction().begin();
    AgentServer.getTransaction().commit(true);
  }

  /**
   * @param method
   * @throws AMQPException
   */
  protected void doProcessMethod(AbstractMarshallingMethod method) throws AMQPException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "+ doProcess marshallingMethod = " + method);

    int channelNumber = method.channelNumber;

    switch (method.getClassId()) {

    /******************************************************
     * Class Connection
     ******************************************************/
    case AMQP.Connection.INDEX:
      if (method.getMethodId() == AMQP.Connection.Close.INDEX) {
        connectionClose();
        try {
          send(new AMQP.Connection.CloseOk());
        } catch (StoppedQueueException sqe) {
          // Connection already closed.
        }
      } else {
        throw new IllegalStateException();
      }
      break;

    /******************************************************
     * Class Channel
     ******************************************************/
    case AMQP.Channel.INDEX:
      if (method.getMethodId() == AMQP.Channel.Close.INDEX) {
        channelClose(channelNumber);
        AMQP.Channel.CloseOk closeOk = new AMQP.Channel.CloseOk();
        closeOk.channelNumber = channelNumber;
        send(closeOk);
      } else {
        throw new IllegalStateException();
      }
      break;

    /******************************************************
     * Class Queue
     ******************************************************/
    case AMQP.Queue.INDEX:
      switch (method.getMethodId()) {
      case AMQP.Queue.Declare.INDEX:
        AMQP.Queue.Declare declare = (AMQP.Queue.Declare) method;
        AMQP.Queue.DeclareOk declareOk = queueDeclare(declare);
        if (declare.noWait == false) {
          declareOk.channelNumber = channelNumber;
          send(declareOk);
        }
        break;

      case AMQP.Queue.Delete.INDEX:
        AMQP.Queue.Delete delete = (AMQP.Queue.Delete) method;
        AMQP.Queue.DeleteOk deleteOk = queueDelete(delete);
        if (delete.noWait == false) {
          deleteOk.channelNumber = channelNumber;
          send(deleteOk);
        }
        break;

      case AMQP.Queue.Bind.INDEX:
        AMQP.Queue.Bind bind = (AMQP.Queue.Bind) method;
        queueBind(bind);
        if (bind.noWait == false) {
          AMQP.Queue.BindOk bindOk = new AMQP.Queue.BindOk();
          bindOk.channelNumber = channelNumber;
          send(bindOk);
        }
        break;

      case AMQP.Queue.Unbind.INDEX:
        AMQP.Queue.Unbind unbind = (AMQP.Queue.Unbind) method;
        queueUnbind(unbind);
        AMQP.Queue.UnbindOk unbindOk = new AMQP.Queue.UnbindOk();
        unbindOk.channelNumber = channelNumber;
        send(unbindOk);
        break;

      case AMQP.Queue.Purge.INDEX:
        AMQP.Queue.Purge purge = (AMQP.Queue.Purge) method;
        AMQP.Queue.PurgeOk purgeOk = queuePurge(purge);
        if (purge.noWait == false) {
          purgeOk.channelNumber = channelNumber;
          send(purgeOk);
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
        GetResponse response = basicGet(get);
        if (response == null) {
          AMQP.Basic.GetEmpty getEmpty = new AMQP.Basic.GetEmpty();
          getEmpty.channelNumber = channelNumber;
          send(getEmpty);
        } else {
          response.getOk.channelNumber = channelNumber;
          send(response);
        }
        break;

      case AMQP.Basic.Ack.INDEX:
        AMQP.Basic.Ack ack = (AMQP.Basic.Ack) method;
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "ACK = " + ack);
        basicAck(ack);
        break;

      case AMQP.Basic.Consume.INDEX:
        AMQP.Basic.Consume consume = (AMQP.Basic.Consume) method;
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "consume = " + consume);
        basicConsume(consume);
        break;

      case AMQP.Basic.Cancel.INDEX:
        AMQP.Basic.Cancel cancel = (AMQP.Basic.Cancel) method;
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "cancel consumerTag = " + cancel.consumerTag + " nowait = "
              + cancel.noWait);
        basicCancel(cancel.consumerTag, channelNumber);
        if (cancel.noWait == false) {
          AMQP.Basic.CancelOk cancelOk = new AMQP.Basic.CancelOk(cancel.consumerTag);
          cancelOk.channelNumber = channelNumber;
          send(cancelOk);
        }
        break;

      case AMQP.Basic.Reject.INDEX:
        AMQP.Basic.Reject reject = (AMQP.Basic.Reject) method;
        basicReject(reject);
        break;

      case AMQP.Basic.RecoverAsync.INDEX:
        // This method is deprecated in favor of the synchronous Recover/RecoverOk.
        AMQP.Basic.RecoverAsync recoverAsync = (AMQP.Basic.RecoverAsync) method;
        basicRecover(recoverAsync.requeue, channelNumber);
        break;

      case AMQP.Basic.Recover.INDEX:
        AMQP.Basic.Recover recover = (AMQP.Basic.Recover) method;
        basicRecover(recover.requeue, channelNumber);
        AMQP.Basic.RecoverOk recoverOk = new AMQP.Basic.RecoverOk();
        recoverOk.channelNumber = channelNumber;
        send(recoverOk);
        break;

      case AMQP.Basic.Qos.INDEX:
        AMQP.Basic.Qos qos = (AMQP.Basic.Qos) method;
        basicQoS(qos);
        AMQP.Basic.QosOk qosOk = new AMQP.Basic.QosOk();
        qosOk.channelNumber = channelNumber;
        send(qosOk);
        break;

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
          send(declareOk);
        }
        break;

      case AMQP.Exchange.Delete.INDEX:
        AMQP.Exchange.Delete delete = (AMQP.Exchange.Delete) method;
        exchangeDelete(delete);
        if (delete.noWait == false) {
          AMQP.Exchange.DeleteOk deleteOk = new AMQP.Exchange.DeleteOk();
          deleteOk.channelNumber = channelNumber;
          send(deleteOk);
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

      switch (method.getMethodId()) {
      case AMQP.Tx.Select.INDEX:
        getContext(channelNumber).transacted = true;
        AMQP.Tx.SelectOk selectOk = new AMQP.Tx.SelectOk();
        selectOk.channelNumber = channelNumber;
        send(selectOk);
        break;

      case AMQP.Tx.Commit.INDEX:
        txCommit(channelNumber);
        AMQP.Tx.CommitOk commitOk = new AMQP.Tx.CommitOk();
        commitOk.channelNumber = channelNumber;
        send(commitOk);
        break;

      case AMQP.Tx.Rollback.INDEX:
        txRollback(channelNumber);
        AMQP.Tx.RollbackOk rollbackOk = new AMQP.Tx.RollbackOk();
        rollbackOk.channelNumber = channelNumber;
        send(rollbackOk);
        break;
      }
      break;

    default:
      break;
    }
  }

  /**
   * Releases connection or channel resources and close it by sending a
   * notification to the client.
   */
  private void throwException(AMQPException amqe, int channelNumber, int classId, int methodId)
      throws AMQPException {
    if (amqe instanceof ChannelException) {
      channelClose(channelNumber);
      AMQP.Channel.Close close = new AMQP.Channel.Close(amqe.getCode(), amqe.getMessage(), classId, methodId);
      close.channelNumber = channelNumber;
      send(close);
    } else {
      connectionClose();
      AMQP.Connection.Close close = new AMQP.Connection.Close(amqe.getCode(), amqe.getMessage(), classId, methodId);
      send(close);
    }
  }

  private void commitTx() throws TransactionException {
    try {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Proxy.commitTx: phase = " + transaction.getPhaseInfo());
      transaction.begin();
      transaction.commit(true);
    } catch (IOException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "Proxy.commitTx() ERROR::", e);
      throw new TransactionException(e.getMessage());
    }
  }
  
  /* ******************************************* */
  /* ******************************************* */
  /* ************* IProxy interface ************ */
  /* ******************************************* */
  /* ******************************************* */

  private Set<QueueShell> exclusiveQueues = new HashSet<QueueShell>();

  // Maps channel id to its context
  private Map<Integer, ChannelContext> channelContexts = new HashMap<Integer, ChannelContext>();

  private ChannelContext getContext(int channelNumber) {
    ChannelContext channelContext = channelContexts.get(Integer.valueOf(channelNumber));
    if (channelContext == null) {
      channelContext = new ChannelContext();
      channelContexts.put(new Integer(channelNumber), channelContext);
    }
    return channelContext;
  }
 
  public synchronized void cleanConsumers(short sid) throws AMQPException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Proxy.cleanConsumers(" + sid + ')');
    Set<Integer> channelsIds = channelContexts.keySet();
    for (Iterator<Integer> iterator = channelsIds.iterator(); iterator.hasNext();) {
      Integer channel = iterator.next();
      int channelNumber = channel.intValue();
      ChannelContext channelContext = channelContexts.get(channel);
      if (channelContext.consumerQueues != null) {
        Collection<QueueShell> queueShells = channelContext.consumerQueues.values();
        Iterator<QueueShell> it = queueShells.iterator();
        while (it.hasNext()) {
          QueueShell queueShell = it.next();
          if (!queueShell.islocal()) {
            String name = queueShell.getName();
            if (Naming.resolveServerId(name) == sid) {
              //NotFoundException e = new NotFoundException("server " + sid + " restart.");
              InternalErrorException e = new InternalErrorException("server " + sid + " restart.");
              throwException(e, channelNumber, -1, -1);
            }
          }
        }
      }
    }
  }
  
  public void basicAck(AMQP.Basic.Ack ack) throws PreconditionFailedException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Proxy.basicAck(" + ack.deliveryTag + ", " + ack.channelNumber + ')');

    ChannelContext channelContext = getContext(ack.channelNumber);

    Iterator<Delivery> iter = channelContext.deliveriesToAck.iterator();
    if (!ack.multiple) {
      while (iter.hasNext()) {
        Delivery delivery = iter.next();
        if (delivery.deliveryTag == ack.deliveryTag) {
          if (channelContext.transacted) {
            if (delivery.waitingCommit) {
              break;
            } else {
              delivery.waitingCommit = true;
              return;
            }
          }
          List<Long> ackList = new ArrayList<Long>(1);
          ackList.add(new Long(delivery.queueMsgId));
          iter.remove();
          if (delivery.queue.islocal()) {
            delivery.queue.getReference().ackMessages(ackList);
          } else {
            StubAgentOut.asyncSend(new Ack(delivery.queue.getName(), ackList),
                Naming.resolveServerId(delivery.queue.getName()));
          }

          if (channelContext.prefetchCount != 0) {
            queueIn.push(new GetDeliveries(null, ack.channelNumber));
          }
          return;
        }
      }
      throw new PreconditionFailedException("Acknowledgement error: invalid tag '" + ack.deliveryTag + "'.");
    } else {
      Map<QueueShell, List<Long>> deliveryMap = new HashMap<QueueShell, List<Long>>();
      while (iter.hasNext()) {
        Delivery delivery = iter.next();
        if (delivery.deliveryTag <= ack.deliveryTag || ack.deliveryTag == 0) {
          if (channelContext.transacted) {
            if (delivery.waitingCommit) {
              continue;
            } else {
              delivery.waitingCommit = true;
            }
          }
          List<Long> ackList = deliveryMap.get(delivery.queue);
          if (ackList == null) {
            ackList = new ArrayList<Long>();
            deliveryMap.put(delivery.queue, ackList);
          }
          ackList.add(new Long(delivery.queueMsgId));
          if (!channelContext.transacted) {
            iter.remove();
          }
        } else if (delivery.deliveryTag > ack.deliveryTag) {
          break;
        }
      }
      if (deliveryMap.size() == 0) {
        throw new PreconditionFailedException("Acknowledgement error: invalid tag '" + ack.deliveryTag + "'.");
      }
      if (channelContext.transacted) {
        return;
      }
      Iterator<QueueShell> iterQueues = deliveryMap.keySet().iterator();
      while (iterQueues.hasNext()) {
        QueueShell queue = iterQueues.next();
        if (queue.islocal()) {
          queue.getReference().ackMessages(deliveryMap.get(queue));
        } else {
          StubAgentOut.asyncSend(new Ack(queue.getName(), deliveryMap.get(queue)),
              Naming.resolveServerId(queue.getName()));
        }
      }

      if (channelContext.prefetchCount != 0) {
        queueIn.push(new GetDeliveries(null, ack.channelNumber));
      }
    }
  }

  public void basicCancel(String consumerTag, int channelNumber) throws AMQPException,
      ResourceLockedException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Proxy.basicCancel(" + consumerTag + ", " + channelNumber + ')');

    ChannelContext channelContext = getContext(channelNumber);
    QueueShell queueShell = channelContext.consumerQueues.remove(consumerTag);
    if (queueShell == null) {
      return;
    }
    doCancel(consumerTag, channelNumber, queueShell);
  }

  private void doCancel(String consumerTag, int channelNumber, QueueShell queueShell)
      throws ResourceLockedException, NotFoundException, PreconditionFailedException, AMQPException {
    if (queueShell.islocal()) {
      Queue queue = queueShell.getReference();
      if (queue != null) {
        queue.cancel(consumerTag, channelNumber, name.serverId, name.proxyId);
        if (queue.getConsumerCount() == 0 && queue.isAutodelete()) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "Proxy: no more consumers -> autodelete");
          AMQP.Queue.Delete delete = new AMQP.Queue.Delete(0, queue.getName(), true, false, true);
          delete.channelNumber = channelNumber;
          queueDelete(delete);
        }
      }
    } else {
      Boolean queueAutodeleted = (Boolean) StubAgentOut.syncSend(new Cancel(consumerTag,
          queueShell.getName(), channelNumber), Naming.resolveServerId(queueShell.getName()), name.proxyId);
      if (queueAutodeleted == null || queueAutodeleted.booleanValue()) {
        cleanQueueContext(channelNumber, queueShell);
      }
    }
  }

  private void getDeliveries(GetDeliveries getDeliveries) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Proxy.getDeliveries(" + getDeliveries.consumerTag + ')');

    ChannelContext channelContext = channelContexts.get(Integer.valueOf(getDeliveries.channelId));
    if (channelContext == null) {
      // channel has been closed in the meantime
      return;
    }

    int maxMessage = -1;
    if (channelContext.prefetchCount > 0) {
      maxMessage = channelContext.prefetchCount - channelContext.deliveriesToAck.size();
    }
    if (maxMessage == 0) {
      return;
    }
    
    if (getDeliveries.consumerTag != null) {
      QueueShell queueShell = channelContext.consumerQueues.get(getDeliveries.consumerTag);
      if (queueShell != null) {
        Queue queue = queueShell.getReference();
        doGetDeliveries(getDeliveries.consumerTag, getDeliveries.channelId, maxMessage, queue);
      }
    } else {
      String[] tags = channelContext.consumerQueues.keySet().toArray(
          new String[channelContext.consumerQueues.size()]);
      int i = 0;
      while (i < tags.length && maxMessage != 0) {
        String consumerTag = tags[i];
        Queue queue = channelContext.consumerQueues.get(consumerTag).getReference();
        int deliveredCount = doGetDeliveries(consumerTag, getDeliveries.channelId, maxMessage, queue);
        maxMessage -= deliveredCount;
        i++;
      }
    }

  }

  private int doGetDeliveries(String consumerTag, int channelId, int maxMessage, Queue queue) {
    List<Deliver> deliveries = queue.getDeliveries(consumerTag, channelId, maxMessage, name.serverId,
        name.proxyId);
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Proxy.doGetDeliveries(" + deliveries + ')');
    if (deliveries != null) {
      for (Iterator iterator = deliveries.iterator(); iterator.hasNext();) {
        Deliver deliver = (Deliver) iterator.next();
        send(deliver, new QueueShell(queue));
      }
      return deliveries.size();
    }
    return 0;
  }

  public void basicConsume(AMQP.Basic.Consume basicConsume) throws NotFoundException, NotAllowedException,
      AMQPException, AccessRefusedException, ResourceLockedException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Proxy.basicConsume(" + basicConsume + ')');

    String queueName = basicConsume.queue;
    if (queueName == null || queueName.equals("")) {
      throw new NotAllowedException("Consuming from unspecified queue.");
    }

    ChannelContext channelContext = getContext(basicConsume.channelNumber);
    // The consumer tag is local to a channel, so two clients can use the
    // same consumer tags. If this field is empty the server will generate a unique tag.
    String tag = basicConsume.consumerTag;
    if (tag.equals("")) {
      channelContext.consumerTagCounter++;
      tag = "genTag-" + channelContext.consumerTagCounter;
    }

    if (channelContext.consumerQueues.get(tag) != null) {
      throw new NotAllowedException("Consume request failed due to non-unique tag: '" + tag + "'.");
    }

    QueueShell queueShell;
    if (Naming.isLocal(queueName)) {
      StubLocal.basicConsume(this, basicConsume.queue, tag, basicConsume.exclusive, basicConsume.noAck,
          basicConsume.noLocal, basicConsume.channelNumber, name.serverId, name.proxyId);
      queueShell = new QueueShell(Naming.lookupQueue(queueName));
    } else {
      queueShell = new QueueShell(basicConsume.queue);
      basicConsume.consumerTag = tag;
      StubAgentOut.asyncSend(basicConsume, Naming.resolveServerId(basicConsume.queue), name.proxyId);
    }

    // Send the ok response
    if (basicConsume.noWait == false) {
      AMQP.Basic.ConsumeOk consumeOk = new AMQP.Basic.ConsumeOk(tag);
      consumeOk.channelNumber = basicConsume.channelNumber;
      send(consumeOk);
    }
    channelContext.consumerQueues.put(tag, queueShell);

    queueIn.push(new GetDeliveries(tag, basicConsume.channelNumber));

  }

  public GetResponse basicGet(AMQP.Basic.Get basicGet) throws NotFoundException, AMQPException,
      SyntaxErrorException, ResourceLockedException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Proxy.basicGet(" + basicGet + ')');

    String queueName = basicGet.queue;
    ChannelContext channelContext = getContext(basicGet.channelNumber);
    if (queueName.equals("")) {
      queueName = channelContext.lastQueueCreated;
      /* If the client did not declare a queue, and the method needs a queue
       * name, this will result in a 502 (syntax error) channel exception. */
      if (queueName == null) {
        throw new SyntaxErrorException("No queue previously declared on the channel.");
      }
    }
    
    Message msg;
    QueueShell queueShell;
    if (Naming.isLocal(queueName)) {
      msg = StubLocal.basicGet(queueName, basicGet.noAck, name.serverId, name.proxyId);
      queueShell = new QueueShell(Naming.lookupQueue(queueName));
    } else {
      basicGet.queue = queueName;
      msg = (Message) StubAgentOut.syncSend(basicGet, Naming.resolveServerId(basicGet.queue));
      queueShell = new QueueShell(basicGet.queue);
    }

    if (msg == null) {
      return null;
    }
    long deliveryTag = channelContext.nextDeliveryTag();
    AMQP.Basic.GetOk getOk = new AMQP.Basic.GetOk(deliveryTag, msg.redelivered, msg.exchange, msg.routingKey,
        msg.queueSize);
    if (!basicGet.noAck) {
      channelContext.deliveriesToAck.add(new Delivery(deliveryTag, msg.queueMsgId, queueShell));
    }
    return new GetResponse(getOk, msg.properties, msg.body);
  }

  public void basicPublish(PublishRequest publishRequest) throws NotFoundException, TransactionException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Proxy.basicPublish(" + publishRequest + ')');

    ChannelContext channelContext = getContext(publishRequest.channel);
    if (channelContext.transacted) {
      IExchange exchange = Naming.lookupExchange(publishRequest.getPublish().exchange);
      if (exchange == null) {
        throw new NotFoundException("Can't publish on an unknwon exchange: '"
            + publishRequest.getPublish().exchange + "'.");
      }
      channelContext.pubToCommit.add(publishRequest);
      return;
    }

    if (Naming.isLocal(publishRequest.getPublish().exchange)) {

      try {
        AgentServer.getTransaction().begin();
      } catch (IOException exc) {
        throw new TransactionException(exc.getMessage());
      }

      try {
        StubLocal.basicPublish(publishRequest, name.serverId, name.proxyId);
      } catch (AMQPException exc) {
        AMQP.Basic.Return returned = new AMQP.Basic.Return(exc.getCode(), exc.getMessage(), publishRequest.getPublish().exchange, publishRequest.getPublish().routingKey);
        returned.channelNumber = publishRequest.channel;
        send(new Returned(returned, publishRequest.getHeader(), publishRequest.getBody()));
      }

      try {
        AgentServer.getTransaction().commit(true);
      } catch (IOException exc) {
        throw new TransactionException(exc.getMessage());
      }
    } else {
      StubAgentOut.asyncSend(publishRequest, Naming.resolveServerId(publishRequest.getPublish().exchange),
          name.proxyId);
    }
  }

  public void basicRecover(boolean requeue, int channelNumber) throws TransactionException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Proxy.basicRecover(" + requeue + ", " + channelNumber + ')');

    // Recover non-acked messages on the channel
    ChannelContext channelContext = getContext(channelNumber);
    Map<QueueShell, List<Long>> recoverMap = new HashMap<QueueShell, List<Long>>();

    Iterator<Delivery> iter = channelContext.deliveriesToAck.iterator();
    while (iter.hasNext()) {
      Delivery delivery = iter.next();
      if (delivery.waitingCommit) {
        continue;
      }
      List<Long> ackList = recoverMap.get(delivery.queue);
      if (ackList == null) {
        ackList = new ArrayList<Long>();
        recoverMap.put(delivery.queue, ackList);
      }
      ackList.add(Long.valueOf(delivery.queueMsgId));
      iter.remove();
    }

    if (recoverMap.size() > 0 && channelContext.prefetchCount != 0) {
      queueIn.push(new GetDeliveries(null, channelNumber));
    }

    Iterator<QueueShell> iterQueues = recoverMap.keySet().iterator();
    while (iterQueues.hasNext()) {
      QueueShell queue = iterQueues.next();
      if (queue.islocal()) {
        queue.getReference().recoverMessages(recoverMap.get(queue));
      } else {
        StubAgentOut.asyncSend(new Recover(queue.getName(), recoverMap.get(queue)),
            Naming.resolveServerId(queue.getName()));
      }
    }
  }

  public void basicReject(AMQP.Basic.Reject basicReject) throws TransactionException,
      PreconditionFailedException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Proxy.basicReject(" + basicReject + ')');
    ChannelContext channelContext = getContext(basicReject.channelNumber);
    Iterator<Delivery> iter = channelContext.deliveriesToAck.iterator();
    while (iter.hasNext()) {
      Delivery delivery = iter.next();
      QueueShell queue = delivery.queue;
      if (delivery.deliveryTag == basicReject.deliveryTag) {
        List<Long> recoverList = new ArrayList<Long>(1);
        recoverList.add(Long.valueOf(delivery.queueMsgId));
        if (basicReject.requeue) {
          // Recover the message to requeue it.
          if (queue.islocal()) {
            queue.getReference().recoverMessages(recoverList);
          } else {
            StubAgentOut.asyncSend(new Recover(queue.getName(), recoverList), Naming.resolveServerId(queue.getName()));
          }
        } else {
          // Ack the message to discard it from the queue.
          if (queue.islocal()) {
            queue.getReference().ackMessages(recoverList);
          } else {
            StubAgentOut.asyncSend(new Ack(queue.getName(), recoverList), Naming.resolveServerId(queue.getName()));
          }
        }
        iter.remove();
        return;
      } else if (delivery.deliveryTag > basicReject.deliveryTag) {
        break;
      }
    }
    throw new PreconditionFailedException("Reject error: invalid tag '" + basicReject.deliveryTag + "'.");
  }

  public void basicQoS(Qos qos) throws NotImplementedException {
    if (qos.global) {
      throw new NotImplementedException("Global Qos prefetch not implemented.");
    }
    if (qos.prefetchSize != 0) {
      throw new NotImplementedException("Qos prefetch size currently not implemented.");
    }
    ChannelContext channelContext = getContext(qos.channelNumber);
    if (qos.prefetchCount > channelContext.prefetchCount || qos.prefetchCount == 0) {
      queueIn.push(new GetDeliveries(null, qos.channelNumber));
    }
    channelContext.prefetchCount = qos.prefetchCount;
  }

  public void channelClose(int channelNumber) throws AMQPException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Proxy.channelClose(" + channelNumber + ")");

    // Close consumers on the channel
    ChannelContext channelContext = channelContexts.get(Integer.valueOf(channelNumber));
    if (channelContext != null) {
      Set<Map.Entry<String, QueueShell>> entrySet = channelContext.consumerQueues.entrySet();
      for (Iterator<Map.Entry<String, QueueShell>> iterator = entrySet.iterator(); iterator.hasNext();) {
        try {
          Map.Entry<String, QueueShell> entry = iterator.next();
          iterator.remove();
          doCancel(entry.getKey(), channelNumber, entry.getValue());
        } catch (ResourceLockedException exc) {
          // Can't happen.
        }
      }

      if (channelContext.transacted) {
        txRollback(channelNumber);
      }
    }

    // Recover all non-acked messages
    basicRecover(true, channelNumber);

    channelContexts.remove(Integer.valueOf(channelNumber));
  }

  public void connectionClose() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Proxy.connectionClose()");

    // Close each remaining channel
    Integer[] channelsIds = channelContexts.keySet().toArray(new Integer[channelContexts.size()]);
    for (int i = 0; i < channelsIds.length; i++) {
      try {
        channelClose(channelsIds[i].intValue());
      } catch (AMQPException exc) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "Error while cleaning channel " + channelsIds[i], exc);
      }
    }

    // Delete exclusive queues
    QueueShell[] queueArray = exclusiveQueues.toArray(new QueueShell[exclusiveQueues.size()]);
    for (int i = 0; i < queueArray.length; i++) {
      QueueShell queue = queueArray[i];
      try {
        if (queue.islocal()) {
          queueDelete(new AMQP.Queue.Delete(0, queue.getReference().getName(), false, false, true));
        } else {
          queueDelete(new AMQP.Queue.Delete(0, queue.getName(), false, false, true));
        }
      } catch (AMQPException exc) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "Error while cleaning exclusive queue " + queue, exc);
      }
    }
    exclusiveQueues.clear();

    stop();
  }

  public void exchangeDeclare(AMQP.Exchange.Declare exchangeDeclare) throws CommandInvalidException,
      NotAllowedException, NotFoundException, AMQPException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Proxy.exchangeDeclare(" + exchangeDeclare + ')');

    if (Naming.isLocal(exchangeDeclare.exchange)) {
      StubLocal.exchangeDeclare(exchangeDeclare.exchange, exchangeDeclare.type, exchangeDeclare.durable,
          exchangeDeclare.passive);
    } else {
      StubAgentOut.syncSend(exchangeDeclare, Naming.resolveServerId(exchangeDeclare.exchange));
    }
  }

  public void exchangeDelete(AMQP.Exchange.Delete exchangeDelete) throws NotFoundException,
      PreconditionFailedException, AMQPException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Proxy.exchangeDelete(" + exchangeDelete + ')');

    if (Naming.isLocal(exchangeDelete.exchange)) {
      StubLocal.exchangeDelete(exchangeDelete.exchange, exchangeDelete.ifUnused);
    } else {
      StubAgentOut.syncSend(exchangeDelete, Naming.resolveServerId(exchangeDelete.exchange));
    }
  }

  public void queueBind(AMQP.Queue.Bind queueBind) throws NotFoundException, SyntaxErrorException,
      ResourceLockedException, AMQPException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Proxy.queueBind(" + queueBind + ')');

    /* If the queue name is empty, the server uses the last queue declared on
     * the channel. If the routing key is also empty, the server uses this queue
     * name for the routing key as well. If the queue name is provided but the
     * routing key is empty, the server does the binding with that empty routing
     * key. */
    String queueName = queueBind.queue;
    if (queueName.equals("")) {
      queueName = getContext(queueBind.channelNumber).lastQueueCreated;
      /* If the client did not declare a queue, and the method needs a queue
       * name, this will result in a 502 (syntax error) channel exception. */
      if (queueName == null) {
        throw new SyntaxErrorException("No queue previously declared on the channel.");
      }
      if (queueBind.routingKey.equals("")) {
        queueBind.routingKey = queueName;
      }
    }

    if (Naming.isLocal(queueBind.exchange)) {
      StubLocal.queueBind(queueName, queueBind.exchange, queueBind.routingKey, queueBind.arguments,
          name.serverId, name.proxyId);
    } else {
      // Assign queue name in case we use lastQueueCreated field
      queueBind.queue = Naming.getGlobalName(queueName);
      StubAgentOut.syncSend(queueBind, Naming.resolveServerId(queueBind.exchange));
    }
  }

  public AMQP.Queue.DeclareOk queueDeclare(AMQP.Queue.Declare queueDeclare) throws NotFoundException,
      ResourceLockedException, AMQPException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Proxy.queueDeclare(" + queueDeclare + ')');

    AMQP.Queue.DeclareOk declareOk;
    QueueShell queueShell;

    if (Naming.isLocal(queueDeclare.queue)) {
      declareOk = StubLocal.queueDeclare(queueDeclare.queue, queueDeclare.passive, queueDeclare.durable,
          queueDeclare.autoDelete, queueDeclare.exclusive, name.serverId, name.proxyId);
      queueShell = new QueueShell(Naming.lookupQueue(declareOk.queue));
    } else {
      declareOk = (AMQP.Queue.DeclareOk) StubAgentOut.syncSend(queueDeclare, Naming.resolveServerId(queueDeclare.queue));
      queueShell = new QueueShell(declareOk.queue);
    }
    if (!queueDeclare.passive && queueDeclare.exclusive) {
      exclusiveQueues.add(queueShell);
    }
    ChannelContext context = getContext(queueDeclare.channelNumber);
    context.lastQueueCreated = declareOk.queue;
    return declareOk;
  }

  public AMQP.Queue.DeleteOk queueDelete(AMQP.Queue.Delete queueDelete) throws NotFoundException,
      PreconditionFailedException, ResourceLockedException, AMQPException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Proxy.queueDelete(" + queueDelete + ')');
    
    QueueShell queueShell;
    AMQP.Queue.DeleteOk deleteOk;
    if (Naming.isLocal(queueDelete.queue)) {
      queueShell = new QueueShell(Naming.lookupQueue(queueDelete.queue));
      int msgCount = StubLocal.queueDelete(queueDelete.queue, queueDelete.ifUnused, queueDelete.ifEmpty,
          name.serverId, name.proxyId);
      deleteOk = new AMQP.Queue.DeleteOk(msgCount);
    } else {
      queueShell = new QueueShell(queueDelete.queue);
      deleteOk = (AMQP.Queue.DeleteOk) StubAgentOut.syncSend(queueDelete, Naming.resolveServerId(queueDelete.queue));
    }
    cleanQueueContext(queueDelete.channelNumber, queueShell);
    return deleteOk;
  }

  private void cleanQueueContext(int channelNumber, QueueShell queueShell) {
    // Clean non-acked deliveries.
    ChannelContext channelContext = getContext(channelNumber);
    Iterator<Delivery> iter = channelContext.deliveriesToAck.iterator();
    while (iter.hasNext()) {
      Delivery delivery = iter.next();
      if (delivery.queue.equals(queueShell)) {
        iter.remove();
      }
    }

    // Remove queue from exclusive list
    exclusiveQueues.remove(queueShell);

    // Remove consumers using this queue
    Iterator<QueueShell> consumerQueueIterator = channelContext.consumerQueues.values().iterator();
    while (consumerQueueIterator.hasNext()) {
      QueueShell consumerQueue = consumerQueueIterator.next();
      if (consumerQueue.equals(queueShell)) {
        consumerQueueIterator.remove();
      }
    }
  }

  public AMQP.Queue.PurgeOk queuePurge(AMQP.Queue.Purge queuePurge) throws NotFoundException,
      NotAllowedException, ResourceLockedException, SyntaxErrorException, AMQPException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Proxy.queuepurge(" + queuePurge + ')');

    if (queuePurge.queue == null || queuePurge.queue.equals("")) {
      throw new NotAllowedException("Purging unspecified queue.");
    }
    if (Naming.isLocal(queuePurge.queue)) {
      int count = StubLocal.queuePurge(queuePurge.queue, name.serverId, name.proxyId);
      return new AMQP.Queue.PurgeOk(count);
    } else {
      return (AMQP.Queue.PurgeOk) StubAgentOut.syncSend(queuePurge, Naming.resolveServerId(queuePurge.queue));
    }
  }

  public void queueUnbind(AMQP.Queue.Unbind queueUnbind) throws NotFoundException, AMQPException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Proxy.queueUnbind(" + queueUnbind + ')');

    if (Naming.isLocal(queueUnbind.exchange)) {
      StubLocal.queueUnbind(queueUnbind.exchange, queueUnbind.queue, queueUnbind.routingKey,
          queueUnbind.arguments, name.serverId, name.proxyId);
    } else {
      queueUnbind.queue = Naming.getGlobalName(queueUnbind.queue);
      StubAgentOut.syncSend(queueUnbind, Naming.resolveServerId(queueUnbind.exchange));
    }
  }

  public void txCommit(int channelNumber) throws PreconditionFailedException, TransactionException {

    ChannelContext channelContext = getContext(channelNumber);
    if (!channelContext.transacted) {
      throw new PreconditionFailedException("Can't commit a non-transacted channel.");
    }

    try {
      AgentServer.getTransaction().begin();
    } catch (IOException exc) {
      throw new TransactionException(exc.getMessage());
    }
    for (PublishRequest publish : channelContext.pubToCommit) {
      if (Naming.isLocal(publish.getPublish().exchange)) {
        try {
          StubLocal.basicPublish(publish, name.serverId, name.proxyId);
        } catch (AMQPException exc) {
          // the behaviour of transactions with respect to the immediate and mandatory
          // flags on Basic.Publish methods is not defined
          AMQP.Basic.Return returned = new AMQP.Basic.Return(exc.getCode(), exc.getMessage(),
              publish.getPublish().exchange, publish.getPublish().routingKey);
          returned.channelNumber = publish.channel;
          send(new Returned(returned, publish.getHeader(), publish.getBody()));
        }
      } else {
        StubAgentOut.asyncSend(publish, Naming.resolveServerId(publish.getPublish().exchange), name.proxyId);
      }
    }
    channelContext.pubToCommit.clear();

    Map<QueueShell, List<Long>> deliveryMap = new HashMap<QueueShell, List<Long>>();
    Iterator<Delivery> iter = channelContext.deliveriesToAck.iterator();
    while (iter.hasNext()) {
      Delivery delivery = iter.next();
      if (delivery.waitingCommit) {
        List<Long> ackList = deliveryMap.get(delivery.queue);
        if (ackList == null) {
          ackList = new ArrayList<Long>();
          deliveryMap.put(delivery.queue, ackList);
        }
        ackList.add(new Long(delivery.queueMsgId));
        iter.remove();
      }
    }
    Iterator<QueueShell> iterQueues = deliveryMap.keySet().iterator();
    while (iterQueues.hasNext()) {
      QueueShell queue = iterQueues.next();
      if (queue.islocal()) {
        queue.getReference().ackMessages(deliveryMap.get(queue));
      } else {
        StubAgentOut.asyncSend(new Ack(queue.getName(), deliveryMap.get(queue)),
            Naming.resolveServerId(queue.getName()));
      }
    }

    try {
      AgentServer.getTransaction().commit(true);
    } catch (IOException exc) {
      throw new TransactionException(exc.getMessage());
    }

    if (channelContext.prefetchCount != 0) {
      queueIn.push(new GetDeliveries(null, channelNumber));
    }
  }

  public void txRollback(int channelNumber) throws PreconditionFailedException {
    ChannelContext channelContext = getContext(channelNumber);
    if (!channelContext.transacted) {
      throw new PreconditionFailedException("Can't rollback a non-transacted channel.");
    }

    channelContext.pubToCommit.clear();
    for (Delivery delivery : channelContext.deliveriesToAck) {
      delivery.waitingCommit = false;
    }
  }
  
  public boolean deliver(String consumerTag, int channelNumber, Queue queue, short serverId, long proxyId) {
    ChannelContext channelContext = getContext(channelNumber);
    if (channelContext.prefetchCount > 0
        && channelContext.deliveriesToAck.size() == channelContext.prefetchCount) {
      return false;
    }

    // Push a getDeliveries method instead of sending deliveries by the current thread
    // which leads to synchronization issues and therefore synchronizing proxy structures.
    queueIn.push(new GetDeliveries(consumerTag, channelNumber));
    return true;
  }


  public void send(AbstractMarshallingMethod method) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Proxy.send(" + method + ")");
    queueOut.add(method);
  }

  public void send(GetResponse response) {
    queueOut.add(response);
  }

  public void send(Deliver deliver, QueueShell queue) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Proxy.send(" + deliver.msgId + ", " + queue + ')');
    // Get the delivery tag.
    ChannelContext channelContext = getContext(deliver.deliver.channelNumber);
    long deliveryTag = channelContext.nextDeliveryTag();
    long queueMsgId = deliver.deliver.deliveryTag;
    deliver.deliver.deliveryTag = deliveryTag;
    if (!deliver.noAck) {
      channelContext.deliveriesToAck.add(new Delivery(deliveryTag, queueMsgId, queue));
    }
    try {
      queueOut.add(deliver);
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "Proxy.send ERROR", e);
    }
  }

  public void send(Returned response) {
    queueOut.add(response);
  }

  // Daemon....
  public void stop() {
    //queueIn.close();
    netServerIn.stop();

    try {
      MXWrapper.unregisterMBean("AMQP", "type=Proxy,name=" + name);
    } catch (Exception exc) {
      logger.log(BasicLevel.DEBUG, "Error unregistering MBean.", exc);
    }
  }

  public void start() {
    netServerIn.start();

    try {
      MXWrapper.registerMBean(this, "AMQP", "type=Proxy,name=" + name);
    } catch (Exception exc) {
      logger.log(BasicLevel.DEBUG, "Error registering MBean.", exc);
    }
  }

  public int getQueueInSize() {
    return queueIn.size();
  }

  public int getQueueOutSize() {
    return queueOut.size();
  }

  public Set<String> getExclusiveQueues() {
    Set<String> queues = new HashSet<String>(exclusiveQueues.size());
    for (Iterator<QueueShell> iterator = exclusiveQueues.iterator(); iterator.hasNext();) {
      QueueShell queue = iterator.next();
      queues.add(queue.getName());
    }
    return queues;
  }

  public Integer[] getOpenedChannels() {
    return channelContexts.keySet().toArray(new Integer[channelContexts.size()]);
  }

  final class NetServerIn extends Daemon {

    protected NetServerIn(String name) {
      super(name);
    }

    public void run() {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Proxy.run()");

      while (running) {
        try {
          canStop = true;
          Object obj = queueIn.getAndPop();
          canStop = false;
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "Proxy: object on queue : " + obj.getClass().getName());

          if (obj instanceof AbstractMarshallingMethod) {
            AbstractMarshallingMethod method = (AbstractMarshallingMethod) obj;
            try {
              doProcessMethod(method);
            } catch (AMQPException amqe) {
              if (logger.isLoggable(BasicLevel.DEBUG)) {
                logger.log(BasicLevel.DEBUG, "Proxy: AMQP error: " + amqe.getMessage());
              }
              throwException(amqe, method.channelNumber, method.getClassId(), method.getMethodId());
            }
          } else if (obj instanceof PublishRequest) {
            PublishRequest publishRequest = (PublishRequest) obj;
            try {
              basicPublish(publishRequest);
            } catch (AMQPException amqe) {
              if (logger.isLoggable(BasicLevel.DEBUG)) {
                logger.log(BasicLevel.DEBUG, "Proxy: AMQP error: " + amqe.getMessage());
              }
              throwException(amqe, publishRequest.channel, publishRequest.getPublish().getClassId(),
                  publishRequest.getPublish().getMethodId());
            }
          } else if (obj instanceof GetDeliveries) {
            getDeliveries((GetDeliveries) obj);
          } else {
            if (logger.isLoggable(BasicLevel.ERROR))
              logger.log(BasicLevel.ERROR, "Proxy: UNEXPECTED OBJECT CLASS: " + obj.getClass().getName());
          }

          try {
            commitTx();
          } catch (TransactionException e) {
            if (obj instanceof AbstractMarshallingMethod) {
              AbstractMarshallingMethod method = (AbstractMarshallingMethod) obj;
              throwException(e, method.channelNumber, method.getClassId(), method.getMethodId());
            } else if (obj instanceof PublishRequest) {
              PublishRequest publishRequest = (PublishRequest) obj;
              throwException(e, publishRequest.channel, publishRequest.getPublish().getClassId(),
                  publishRequest.getPublish().getMethodId());
            }
          }

        } catch (InterruptedException e) {
          if (logger.isLoggable(BasicLevel.ERROR))
            logger.log(BasicLevel.ERROR, "Proxy: error ", e);
        } catch (Exception exc) {
          if (logger.isLoggable(BasicLevel.ERROR))
            logger.log(BasicLevel.ERROR, "Proxy: error ", exc);
        }

      }
    }

    // Daemon....
    @Override
    protected void close() {
    }

    @Override
    protected void shutdown() {
      queueIn.close();
    }
  }

}
