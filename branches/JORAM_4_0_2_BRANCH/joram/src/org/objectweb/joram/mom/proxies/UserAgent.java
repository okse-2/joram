/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): David Feliot (ScalAgent DT)
 */
package org.objectweb.joram.mom.proxies;

import fr.dyade.aaa.agent.*;
import fr.dyade.aaa.agent.management.MXWrapper;

import java.io.*;
import java.util.*;

import org.objectweb.joram.mom.MomTracing;
import org.objectweb.joram.shared.client.*;

import org.objectweb.util.monolog.api.BasicLevel;

/** 
 * Class of a user proxy agent.
 */
public class UserAgent extends Agent 
    implements ProxyAgentItf {

  /**
   * All the user requests are delegated
   * to the proxy
   */
  private ProxyImpl proxyImpl;

  /**
   * Table that contains the user connections
   * key = <code>Integer</code> (connection key)
   * value = <code>UserConnection</code>
   */
  private transient Hashtable connections;

  /**
   * Counter of the connection keys
   */
  private int keyCounter;

  /**
   * Creates a new user proxy.
   *
   * @see AdminTopicImpl
   * @see ConnectionManager
   */
  public UserAgent() {
    super(true);
    proxyImpl = new ProxyImpl();
    keyCounter = 0;    
  }

  /** (Re)initializes the agent when (re)loading. */
  public void agentInitialize(boolean firstTime) throws Exception {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, "UserAgent.agentInitialize(" + 
        firstTime + ')');
    super.agentInitialize(firstTime);
    proxyImpl.initialize(firstTime, this);
    MXWrapper.registerMBean(proxyImpl,
                            "JORAM proxies",
                            getId().toString(),
                            "JmsProxy",
                            null);
  }

  /** Finalizes the agent before it is garbaged. */
  public void agentFinalize(boolean lastTime) {
    try {
      MXWrapper.unregisterMBean("JORAM proxies",
                                getId().toString(),
                                "JmsProxy",
                                null);
    } catch (Exception exc) {
      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(
          BasicLevel.DEBUG, "", exc);
    }
  }

  /**
   * Overrides the <code>Agent</code> class <code>react</code> method for
   * providing the JMS client proxy with its specific behaviour.
   * <p>
   * A JMS proxy specifically reacts to the following notifications:
   * <ul>
   * <li><code>OpenConnectionNot</code></li>
   * </ul>
   */ 
  public void react(AgentId from, Notification not) throws Exception {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, "UserAgent.react(" + 
        from + ',' + not + ')');
    if (not instanceof OpenConnectionNot) {
      doReact(from, (OpenConnectionNot)not);
    } else {
      try {
        proxyImpl.react(from, not);
      } catch (UnknownNotificationException exc) {
        super.react(from, not);
      }
    }
  }

  /**
   * Registers and starts the <cod>UserConnection</code>.
   */
  private void doReact(AgentId from, OpenConnectionNot not) {
    UserConnection uc = not.getUserConnection();
    if (uc != null) {
      uc.start(this, keyCounter);
      if (connections == null) {
        connections = new Hashtable();
      }    
      Integer objKey = new Integer(keyCounter);
      connections.put(objKey, uc);
      keyCounter++;
      synchronized (uc) {
        uc.notify();
      }
    }
  }

  /**
   * Returns the agent identifier
   * of this agent.
   */
  public AgentId getAgentId() {
    return getId();
  }
  
  /**
   * Sends a notification to the specified agent.
   *
   * @param to the identifier of the recipient agent
   * @param not the notification to send
   */
  public void sendNot(AgentId to, Notification not) {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG,
        "UserAgent.sendNot(" + to + ',' + not + ')');
    sendTo(to, not);
  }

  /**
   * Sends a reply to the client connected through 
   * the specified connection.
   * 
   * @param key the key of the connection the client 
   * is connected through.
   * @param reply the reply to send to the client.
   */
  public void sendToClient(int key, AbstractJmsReply reply) {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, 
        "UserAgent.doReply(" + key + ',' + reply + ')');
    Integer objKey = new Integer(key);
    UserConnection uc = 
      (UserConnection)connections.get(objKey);    
    if (uc != null) {
      uc.pushReply(reply);
    }
  }

  /**
   * Invokes the user with the specified request from the
   * connection 'key'.
   *
   * @param key the key of the connection that sent the
   * request
   * @param request the request to send to the user's proxy
   */
  void invoke(int key, AbstractJmsRequest request) {
    proxyImpl.reactToClientRequest(key, request);
  }

  /**
   * Drops the specified connection from
   * the table.
   */
  void closeConnection(int key) {
    Integer objKey = new Integer(key);
    connections.remove(objKey);
  }
}
