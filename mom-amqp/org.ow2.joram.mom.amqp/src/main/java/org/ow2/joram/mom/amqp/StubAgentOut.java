/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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

import java.util.Iterator;
import java.util.List;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.ow2.joram.mom.amqp.exceptions.AMQPException;
import org.ow2.joram.mom.amqp.exceptions.InterruptedException;
import org.ow2.joram.mom.amqp.structures.Deliver;

import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.common.Debug;

/**
 * The {@link StubAgentOut} class handles output interactions with other Joram
 * AMQP servers.
 */
public class StubAgentOut implements DeliveryListener {

  /** logger */
  public static Logger logger = Debug.getLogger(StubAgentOut.class.getName());
  
  private static long timeOut;
  private static long lockCount;

  /**
   * @param timeOut
   */
  public StubAgentOut(long timeOut) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "StubAgentOut<" + timeOut + '>');
    lockCount = 1;
    StubAgentOut.timeOut = timeOut;
  }
    
  /**
   * @return
   */
  public static synchronized Long getNextLock() {
    return new Long(lockCount++);
  }

  /**
   * @param request
   * @param serverId
   * @return
   * @throws InterruptedException
   */
  public static Object syncSend(Object request, short serverId) throws AMQPException {
    return syncSend(request, serverId, -1);
  }

  /**
   * @param request
   * @param serverId
   * @param proxyId
   * @return
   * @throws AMQPException 
   */
  public static Object syncSend(Object request, short serverId, long proxyId) throws AMQPException {
    Long lock = getNextLock();
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "syncSend(" + request + ", " + serverId + ") lock=" + lock);

    synchronized (lock) {
      AMQPAgent.sendRequestTo(request, serverId, proxyId, lock);
      try {
        lock.wait(timeOut);
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "syncSend wakeup lock=" + lock);
      } catch (java.lang.InterruptedException e) {
        // remove the entry.
        AMQPAgent.getResponse(lock);
        throw new InterruptedException(e.getMessage());
      }
    }
    Object response = AMQPAgent.getResponse(lock);
    if (response instanceof AMQPException)
      throw (AMQPException) response;
    return response;
  }
  
  /**
   * @param request
   * @param serverId
   */
  public static void asyncSend(Object request, short serverId) {
    asyncSend(request, serverId, -1);
  }
  

  /**
   * @param request
   * @param serverId
   * @param proxyId
   */
  public static void asyncSend(Object request, short serverId, long proxyId) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "asyncSend(" + request + ", " + serverId + ')');
    AMQPAgent.sendRequestTo(request, serverId, proxyId, null);
  }

  public boolean deliver(String consumerTag, int channelId, Queue queue, short serverId, long proxyId) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "StubAgentOut.deliver(" + queue + ')');
    List<Deliver> deliveries = queue.getDeliveries(consumerTag, channelId, 1, serverId, proxyId);
    for (Iterator<Deliver> iterator = deliveries.iterator(); iterator.hasNext();) {
      Deliver deliver = iterator.next();
      AMQPResponseNot not = new AMQPResponseNot();
      not.obj = deliver;
      not.keyLock = -1;
      Channel.sendTo(AMQPAgent.getAMQPId(deliver.serverId), not);
    }
    return true;
  }

}
