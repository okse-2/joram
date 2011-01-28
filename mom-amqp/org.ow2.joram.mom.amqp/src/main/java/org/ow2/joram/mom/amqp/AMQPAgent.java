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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.ow2.joram.mom.amqp.structures.Recover;

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.common.Debug;

/**
 * An {@link AMQPAgent} is responsible of the communications with other Joram
 * AMQP servers.
 */
public class AMQPAgent extends Agent {

  /**
   * serialVersionUID
   */
  private static final long serialVersionUID = 1L;

  /** logger */
  public static Logger logger = Debug.getLogger(AMQPAgent.class.getName());
  
  /** contains the lock Object to notify. */
  private static Map<Long, Long> lockers;
  /** contains the async response. */
  private static Map<Long, Object> responses;
  
  public static StubAgentIn stubAgentIn;
  public static StubAgentOut stubAgentOut;

  /**
   * Empty constructor for newInstance(). 
   */ 
  public AMQPAgent() {
    super("AMQPAgent", true, AgentId.AMQPAgentStamp);
  }
  
  /**
   * Gives this agent an opportunity to initialize after having been deployed,
   * and each time it is loaded into memory.
   *
   * @param firstTime   true when first called by the factory
   *
   * @exception Exception
   *  unspecialized exception
   */
  protected void agentInitialize(boolean firstTime) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AMQPAgent.agentInitialize(" + firstTime + ')');

    super.agentInitialize(firstTime);
    // create lockers map
    lockers = new HashMap<Long, Long>();
    // create response map
    responses = new HashMap<Long, Object>();
    
    // create stub agent in
    stubAgentIn = new StubAgentIn();

    // create stub agent out
    stubAgentOut = new StubAgentOut(60000);

    if (!firstTime) {
      sendRestart();
    }
    
//    try {
//      MXWrapper.registerMBean(amqp, "AMQP#"+AgentServer.getServerId(), getMBeanName());
//    } catch (Exception exc) {
//      logger.log(BasicLevel.ERROR, this + " jmx failed", exc);
//    }
  }

  /** Finalizes the agent before it is garbaged. */
  public void agentFinalize(boolean lastTime) {
//    try {
//      MXWrapper.unregisterMBean("AMQP#"+AgentServer.getServerId(), getMBeanName());
//    } catch (Exception exc) {
//      if (logger.isLoggable(BasicLevel.DEBUG))
//        logger.log(BasicLevel.DEBUG, "AMQPAgent.agentFinalize", exc);
//    }
    super.agentFinalize(lastTime);
  }
  
//  private String getMBeanName() {
//    return new StringBuffer()
//      .append("type=AMQPAgent")
//      .append(",name=").append((name==nullName)?getId().toString():name)
//      .toString();
//  }

  /**
   * Distributes the received notifications to the appropriate reactions.
   * @throws Exception 
   */
  public void react(AgentId from, Notification not) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "AMQPAgent.react(" + from + ',' + not + ')');

    // set agent no save (this is the default).
    setNoSave();

    if (not instanceof AMQPRequestNot) {
      // AMQP request
      Object obj = ((AMQPRequestNot) not).obj;
      long keyLock = ((AMQPRequestNot) not).keyLock;
      long proxyId = ((AMQPRequestNot) not).proxyId;
      StubAgentIn.processRequest(from, keyLock, proxyId, obj);
    } else if (not instanceof AMQPResponseNot){
      //AMQP response
      Object obj = ((AMQPResponseNot) not).obj;
      long keyLock = ((AMQPResponseNot) not).keyLock;
      StubAgentIn.processResponse(from, keyLock, obj);
    } else if (not instanceof RestartNot) {
      short sid = from.getFrom();
      // clean proxies
      Iterator<Proxy> proxies = Naming.getProxies().iterator();
      while (proxies.hasNext()) {
        Proxy proxy = proxies.next();
        proxy.cleanConsumers(sid);
      }
      // clean queues
      Iterator<Queue> queues = Naming.getQueues().iterator();
      while (queues.hasNext()) {
        Queue queue = queues.next();
        queue.cleanConsumers(sid);
      }
    } else {
      super.react(from, not);
    }
  }
  
  /**
   * get destination AgentId of AMQPAgent.
   * 
   * @param serverId destination server Id.
   * @return destination AgentId of AMQPAgent.
   */
  public static AgentId getAMQPId(short serverId) {
    return new AgentId(serverId, serverId, AgentId.AMQPAgentStamp);
  }
  
  /**
   * send AMQP request to serverId AMQP agent.
   * and store keyLock.
   * 
   * @param request the request to send
   * @param serverId destination server Id.
   * @param keyLock the locker Object for synchronous call or null for async.
   */
  public static void sendRequestTo(Object request, short serverId, long proxyId, Long keyLock) {
    AMQPRequestNot not = new AMQPRequestNot();
    not.obj = request;
    not.proxyId = proxyId;
    not.keyLock = -1;
    if (keyLock != null) {
      lockers.put(keyLock, keyLock);
      not.keyLock = keyLock.longValue();
    }
    Channel.sendTo(getAMQPId(serverId), not);
  }
  
  /**
   * @param lock
   * @param response
   */
  public static void putResponse(AgentId from, long lock, Object response) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AMQPAgent.putResponse(" + lock + ',' + response + ')');
    if (lockers.containsKey(Long.valueOf(lock))) {
      responses.put(Long.valueOf(lock), response);
      Long locker = lockers.remove(Long.valueOf(lock));
      synchronized (locker) {
        locker.notify();
      }
    } else {
      if (response instanceof Message) {
        Message msg = (Message) response;
        AMQPRequestNot not = new AMQPRequestNot();
        List<Long> list = new ArrayList<Long>();
        list.add(Long.valueOf(msg.queueMsgId));
        not.obj = new Recover(msg.queueName, list);
        Channel.sendTo(from, not);
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "AMQPAgent.putResponse recover queueMsgId = " + msg.queueMsgId);
      } else {
        logger.log(BasicLevel.ERROR, "!!!!!!! TODO recover? response", new Exception());
      }
    }
  }
  
  /**
   * @param lock
   * @return
   */
  public static Object getResponse(long lock) {
    lockers.remove(Long.valueOf(lock));
    return responses.remove(Long.valueOf(lock));
  }
  
  private void sendRestart() {
    Enumeration<Short> sids = AgentServer.getServersIds();
    while (sids.hasMoreElements()) {
      Short sid = sids.nextElement();
      if (sid.shortValue() != AgentServer.getServerId()) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "AMQPAgent.sendRestart notification to " + sid);
        Channel.sendTo(getAMQPId(sid.shortValue()), new RestartNot());
      }
    }
  }
}
