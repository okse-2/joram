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

import java.rmi.AlreadyBoundException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 * Naming directory for all local exchanges, proxies and queues.
 */
public class Naming {

  public static Logger logger = Debug.getLogger(Naming.class.getName());

  private static long queueCounter = 0;
  private static ConcurrentHashMap<String, IExchange> exchanges = new ConcurrentHashMap<String, IExchange>();
  private static ConcurrentHashMap<String, Queue> queues = new ConcurrentHashMap<String, Queue>();
  private static ConcurrentHashMap<ProxyName, Proxy> proxies = new ConcurrentHashMap<ProxyName, Proxy>();

  /**
   * @param name
   * @param ref
   * @throws Exception
   */
  public static synchronized String nextQueueName() {
    return "tmp.q." + queueCounter++;
  }

  /**
   * @param name
   * @param ref
   * @throws AlreadyBoundException
   */
  public static void bindExchange(String name, IExchange ref) throws AlreadyBoundException {
    String localName = getLocalName(name);
    IExchange previousValue = exchanges.putIfAbsent(localName, ref);
    if (previousValue != null) {
      throw new AlreadyBoundException(name);
    } else {
      try {
        MXWrapper.registerMBean(ref, "AMQP", "type=Exchange,name=" + localName);
      } catch (Exception exc) {
        logger.log(BasicLevel.DEBUG, "Error registering MBean.", exc);
      }
    }
  }

  /**
   * @param name
   * @param ref
   * @throws Exception
   */
  public static void bindQueue(String name, Queue ref) throws AlreadyBoundException {
    String localName = getLocalName(name);
    Queue previousValue = queues.putIfAbsent(localName, ref);
    if (previousValue != null) {
      throw new AlreadyBoundException(name);
    } else {
      try {
        MXWrapper.registerMBean(ref, "AMQP", "type=Queue,name=" + localName);
      } catch (Exception exc) {
        logger.log(BasicLevel.DEBUG, "Error registering MBean.", exc);
      }
    }
  }

  /**
   * @param name
   * @return
   */
  public static IExchange lookupExchange(String name) {
    return exchanges.get(getLocalName(name));
  }

  /**
   * @param name
   * @return
   */
  public static Queue lookupQueue(String name) {
    return queues.get(getLocalName(name));
  }

  /**
   * @return
   */
  public static Collection<Queue> getQueues() {
    return queues.values();
  }
  
  /**
   * @param name
   */
  public static void unbindQueue(String name) {
    String localName = getLocalName(name);
    Queue queue = queues.remove(localName);
    if (queue != null) {
      try {
        MXWrapper.unregisterMBean("AMQP", "type=Queue,name=" + localName);
      } catch (Exception exc) {
        logger.log(BasicLevel.DEBUG, "Error unregistering MBean.", exc);
      }
    }
  }

  /**
   * @param name
   */
  public static void unbindExchange(String name) {
    String localName = getLocalName(name);
    IExchange exchange = exchanges.remove(localName);
    if (exchange != null) {
      try {
        MXWrapper.unregisterMBean("AMQP", "type=Exchange,name=" + localName);
      } catch (Exception exc) {
        logger.log(BasicLevel.DEBUG, "Error unregistering MBean.", exc);
      }
    }
  }

  /**
   * name: 
   *  - serverName/QueueName  (serverName see a3servers.xml)
   *  - serverName/ExchangeName
   *  - QueueName
   *  - ExchangeName ...
   *  
   * @param name
   * @return the server name
   */
  private static String getServerName(String name) {
    // parse name...
    int index = name.indexOf('/');
    if (index < 0)
      return AgentServer.getServerName();
    else
      return name.substring(0, index);
  }

  /**
   * name:
   * <ul>
   * <li>serverName/QueueName (serverName see a3servers.xml)</li>
   * <li>serverName/ExchangeName</li>
   * <li>QueueName</li>
   * <li>ExchangeName ...</li>
   * </ul>
   * 
   * @param name
   * @return the QueueName, ExchangeName.
   */
  public static String getLocalName(String name) {
    // parse name...
    int index = name.indexOf('/');
    if (index < 0)
      return name;
    else
      return name.substring(index+1, name.length());
  }
  
  public static String getGlobalName(String name) {
    // parse name...
    int index = name.indexOf('/');
    if (index < 0)
      return AgentServer.getServerName() + '/' + name;
    return name;
  }
  
  /**
   * name: 
   *  - serverName/QueueName  (serverName see a3servers.xml)
   *  - serverName/ExchangeName
   *  - QueueName
   *  - ExchangeName ...
   *  
   * @param name
   * @return true if local
   */
  public static boolean isLocal(String name) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Naming.isLocal(" + name + ')');

    // is name already bind in queues or exchanges ?
    if (exchanges.containsKey(name) || queues.containsKey(name))
      return true;

    String serverName = getServerName(name);
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "server name: " + serverName);

    try {
      if (AgentServer.getServerId() == AgentServer.getServerIdByName(serverName))
        return true;
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Error getting server Id.", e);
    }
    return false;
  }

  /**
   * @param name
   * @return
   * @throws Exception 
   */
  public static short resolveServerId(String name) {
    try {
      return AgentServer.getServerIdByName(getServerName(name));
    } catch (Exception e) {
      // TODO Auto-generated catch block
      return -1;
    }
  }

  /**
   * @param name
   * @param ref
   * @throws AlreadyBoundException
   */
  public static void bindProxy(ProxyName proxyName, Proxy ref) throws AlreadyBoundException {
    Proxy previousValue = proxies.put(proxyName, ref);
    if (previousValue != null) {
      throw new AlreadyBoundException(proxyName.toString());
    }
  }
  /**
   * @param name
   */
  public static void unbindProxy(ProxyName proxyName) {
    proxies.remove(proxyName);
  }

  /**
   * @param proxyName
   * @return
   */
  public static Proxy lookupProxy(ProxyName proxyName) {
    return proxies.get(proxyName);
  }

  /**
   * @return
   */
  public static Collection<Proxy> getProxies() {
    return proxies.values();
  }
  
  public static void clearAll() {
    for (Iterator<String> iterator = exchanges.keySet().iterator(); iterator.hasNext();) {
      try {
        MXWrapper.unregisterMBean("AMQP", "type=Exchange,name=" + iterator.next());
      } catch (Exception exc) {
        logger.log(BasicLevel.DEBUG, "Error unregistering MBean.", exc);
      }
    }
    exchanges.clear();

    for (Iterator<String> iterator = queues.keySet().iterator(); iterator.hasNext();) {
      try {
        MXWrapper.unregisterMBean("AMQP", "type=Queue,name=" + iterator.next());
      } catch (Exception exc) {
        logger.log(BasicLevel.DEBUG, "Error unregistering MBean.", exc);
      }
    }
    queues.clear();

    proxies.clear();
  }

  public static void createDefaults() throws Exception {
    IExchange exchange;
    exchange = new DirectExchange(IExchange.DEFAULT_EXCHANGE_NAME, true);
    Naming.bindExchange(IExchange.DEFAULT_EXCHANGE_NAME, exchange);

    exchange = new DirectExchange(DirectExchange.DEFAULT_NAME, true);
    Naming.bindExchange(DirectExchange.DEFAULT_NAME, exchange);

    exchange = new FanoutExchange(FanoutExchange.DEFAULT_NAME, true);
    Naming.bindExchange(FanoutExchange.DEFAULT_NAME, exchange);

    exchange = new TopicExchange(TopicExchange.DEFAULT_NAME, true);
    Naming.bindExchange(TopicExchange.DEFAULT_NAME, exchange);

    exchange = new HeadersExchange(HeadersExchange.DEFAULT_NAME, true);
    Naming.bindExchange(HeadersExchange.DEFAULT_NAME, exchange);
  }
}
