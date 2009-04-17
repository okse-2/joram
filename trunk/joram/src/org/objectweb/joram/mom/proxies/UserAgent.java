/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2008 ScalAgent Distributed Technologies
 * Copyright (C) 2004 France Telecom R&D
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
 */
package org.objectweb.joram.mom.proxies;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.objectweb.joram.mom.notifications.WakeUpNot;
import org.objectweb.joram.shared.client.AbstractJmsReply;
import org.objectweb.joram.shared.client.AbstractJmsRequest;
import org.objectweb.joram.shared.client.CnxCloseRequest;
import org.objectweb.joram.shared.client.JmsRequestGroup;
import org.objectweb.joram.shared.client.MomExceptionReply;
import org.objectweb.joram.shared.client.ProducerMessages;
import org.objectweb.joram.shared.client.ServerReply;
import org.objectweb.joram.shared.excepts.MomException;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.BagSerializer;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.UnknownNotificationException;
import fr.dyade.aaa.agent.WakeUpTask;
import fr.dyade.aaa.util.Debug;
import fr.dyade.aaa.util.management.MXWrapper;

/** 
 * Class of a user proxy agent.
 */
public class UserAgent extends Agent implements BagSerializer, ProxyAgentItf {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** logger */
  public static Logger logger = Debug.getLogger(UserAgent.class.getName());

  /**
   * All the user requests are delegated to the proxy
   */
  private ProxyImpl proxyImpl;

  /**
   * Table that contains the user connections:
   *  - key = <code>Integer</code> (connection key)
   *  - value = <code></code>
   */
  private transient Hashtable connections;

  private transient Hashtable heartBeatTasks;

  /**
   * Counter of the connection keys
   */
  private int keyCounter;

  /**
   * Creates a new user proxy.
   *
   * @see ConnectionManager
   */
  public UserAgent() {
    super(true);
    init();
  }

  /**
   * Creates a new user proxy.
   *
   * @see ConnectionManager
   */
  public UserAgent(int stamp) {
    super("JoramAdminProxy", true, stamp);
    init();
  }

  private void init() {
    proxyImpl = new ProxyImpl(this);
    keyCounter = 0;
  }

  private transient WakeUpTask cleaningTask;

  /** (Re)initializes the agent when (re)loading. */
  public void agentInitialize(boolean firstTime) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "UserAgent.agentInitialize(" +  firstTime + ')');

    super.agentInitialize(firstTime);
    proxyImpl.initialize(firstTime);
    cleaningTask = new WakeUpTask(getId(), WakeUpNot.class);
    cleaningTask.schedule(proxyImpl.getPeriod());
    try {
      MXWrapper.registerMBean(proxyImpl, "Joram#"+AgentServer.getServerId(), getMBeanName());
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, this + " jmx failed", exc);
    }
  }

  /** Finalizes the agent before it is garbaged. */
  public void agentFinalize(boolean lastTime) {
    try {
      MXWrapper.unregisterMBean("Joram#"+AgentServer.getServerId(), getMBeanName());
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
    }
    super.agentFinalize(lastTime);
  }

  private String getMBeanName() {
    return new StringBuffer().append("type=User").append(",name=").append(
                                                                          (name == nullName) ? getId().toString() : name).toString();
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
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "UserAgent.react(" + from + ',' + not + ')');

    // set agent no save:
    // the default behavior is transient
    setNoSave();

    if (not instanceof OpenConnectionNot) {
      doReact((OpenConnectionNot) not);
    } else if (not instanceof GetConnectionNot) {
      doReact((GetConnectionNot) not);
    } else if (not instanceof CloseConnectionNot) {
      doReact((CloseConnectionNot) not);
    } else if (not instanceof ResetCollocatedConnectionsNot) {
      doReact((ResetCollocatedConnectionsNot) not);
    } else if (not instanceof SendReplyNot) {
      doReact((SendReplyNot) not);
    } else if (not instanceof RequestNot) {
      doReact((RequestNot) not);
    } else if (not instanceof ReturnConnectionNot) {
      doReact((ReturnConnectionNot) not);
    } else if (not instanceof SendRepliesNot) {
      doReact((SendRepliesNot) not);
    } else if (not instanceof ProxyRequestGroupNot) {
      doReact((ProxyRequestGroupNot) not);
    } else if (not instanceof WakeUpNot) {
      try {
        proxyImpl.cleanPendingMessages(System.currentTimeMillis());
      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR, "--- " + this + " Proxy(...)", exc);
      }

      if (cleaningTask == null)
        cleaningTask = new WakeUpTask(getId(), WakeUpNot.class);
      cleaningTask.schedule(proxyImpl.getPeriod());


    } else {
      try {
        proxyImpl.react(from, not);
      } catch (UnknownNotificationException exc) {
        super.react(from, not);
      }
    }
  }

  /**
   * Registers and starts the <code>UserConnection</code>.
   */
  private void doReact(OpenConnectionNot not) {
    // state change, so save.
    setSave();

    if (connections == null) {
      connections = new Hashtable();
      heartBeatTasks = new Hashtable();
    }

    Integer objKey = new Integer(keyCounter);
    ConnectionContext ctx;
    if (not.getReliable()) {
      ctx = new ReliableConnectionContext(
                                          proxyImpl, keyCounter,
                                          not.getHeartBeat());
      connections.put(objKey, ctx);
    } else {
      ctx = new StandardConnectionContext(
                                          proxyImpl, keyCounter);
      connections.put(objKey, ctx);
    }

    if (not.getHeartBeat() > 0) {
      HeartBeatTask heartBeatTask = new HeartBeatTask(2 * not.getHeartBeat(),
                                                      objKey);
      heartBeatTasks.put(objKey, heartBeatTask);
      heartBeatTask.start();
    }

    // Differs the reply because the connection key counter
    // must be saved before the OpenConnectionNot returns.
    sendTo(getId(), new ReturnConnectionNot(not, ctx));
    keyCounter++;
  }

  /**
   * Differs the reply because the connection key counter
   * must be saved before the OpenConnectionNot returns.
   */
  private void doReact(ReturnConnectionNot not) {
    not.Return();
  }

  private void doReact(GetConnectionNot not) {
    int key = not.getKey();
    if (connections == null) {
      not.Throw(new Exception("Connection " + key + " not found"));
    } else {
      Integer objKey = new Integer(key);
      ReliableConnectionContext ctx = (ReliableConnectionContext) connections
      .get(objKey);
      if (ctx == null) {
        not.Throw(new Exception("Connection " + key + " not found"));
      } else {
        not.Return(ctx);
      }
    }
  }

  private void doReact(RequestNot not) {
    Integer key = new Integer(not.getConnectionKey());
    if (connections != null) {
      ConnectionContext ctx = (ConnectionContext) connections.get(key);
      if (ctx != null) {
        HeartBeatTask heartBeatTask = (HeartBeatTask) heartBeatTasks.get(key);
        if (heartBeatTask != null) {
          heartBeatTask.touch();
        }

        AbstractJmsRequest request = ctx.getRequest(not.getMessage());  
        proxyImpl.reactToClientRequest(key.intValue(), request);

        if (ctx.isClosed()) {
          //CnxCloseRequest request = (CnxCloseRequest) not.getMessage();
          connections.remove(key);
          HeartBeatTask hbt = (HeartBeatTask) heartBeatTasks.remove(key);
          if (hbt != null) {
            hbt.cancel();
          }
        }
      }
    }
    // else should not happen because:
    // - RequestNot is transient
    // - RequestNot always follows an OpenConnection or
    // a GetConnection
  }

  private void doReact(ProxyRequestGroupNot not) {
    RequestNot[] requests = not.getRequests();
    RequestBuffer rm = new RequestBuffer(this);
    for (int i = 0; i < requests.length; i++) {
      RequestNot req = requests[i];
      Integer key = new Integer(req.getConnectionKey());
      HeartBeatTask heartBeatTask = (HeartBeatTask) heartBeatTasks.get(key);
      if (heartBeatTask != null) {
        heartBeatTask.touch();
      }
      ConnectionContext ctx = (ConnectionContext) connections.get(key);
      if (ctx != null) {
        AbstractJmsRequest request = ctx.getRequest(req.getMessage());
        if (request instanceof ProducerMessages) {
          ProducerMessages pm = (ProducerMessages) request;
          rm.put(req.getConnectionKey(), pm);
        } else if (request instanceof JmsRequestGroup) {
          JmsRequestGroup jrg = (JmsRequestGroup)request;
          AbstractJmsRequest[] groupedRequests = jrg.getRequests();
          for (int j = 0; j < groupedRequests.length; j++) {
            if (groupedRequests[i] instanceof ProducerMessages) {
              ProducerMessages pm = (ProducerMessages) groupedRequests[i];
              rm.put(req.getConnectionKey(), pm);
            } else {
              proxyImpl.reactToClientRequest(key.intValue(), groupedRequests[i]);
            }
          }
        } else {
          proxyImpl.reactToClientRequest(key.intValue(), request);
        }
      }
    }
    rm.flush();
  }

  private void doReact(CloseConnectionNot not) {
    if (connections != null) {
      Integer key = new Integer(not.getKey());
      // The connection may have already been 
      // explicitely closed by a CnxCloseRequest.
      if (connections.remove(key) != null) {
        proxyImpl.reactToClientRequest(not.getKey(), new CnxCloseRequest());
        heartBeatTasks.remove(key);
      }
    }
    // else should not happen:
    // 1- CloseConnectionNot is transient
    // 2- CloseConnectionNot follows an OpenConnectionNot
    // or a GetConnectionNot
  }

  private void doReact(ResetCollocatedConnectionsNot not) {
    if (connections != null) {
      Collection values = connections.values();
      Iterator iterator = values.iterator();
      while (iterator.hasNext()) {
        Object obj = iterator.next();
        // Standard connections must be dropped.
        // Only reliable connections can be recovered.
        if (obj instanceof StandardConnectionContext) {
          ConnectionContext cc = (ConnectionContext) obj;
          proxyImpl.reactToClientRequest(
                                         cc.getKey(), new CnxCloseRequest());
          iterator.remove();
        }
      }
    }
  }

  private void doReact(SendRepliesNot not) {
    Enumeration en = not.getReplies();
    while (en.hasMoreElements()) {
      SendReplyNot sr = (SendReplyNot) en.nextElement();
      doReact(sr);
    }
  }

  /**
   * Notification sent by local agents (destinations)
   * indicating that the proxy can reply to a client.
   * @param not
   */
  private void doReact(SendReplyNot not) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "UserAgent.doReact(" + not + ')');
    ClientContext cc = proxyImpl.getClientContext(not.getKey());
    if (cc != null) {
      if (cc.setReply(not.getRequestId()) == 0) {
        sendToClient(not.getKey(), new ServerReply(not.getRequestId()));
      }
    } else if (logger.isLoggable(BasicLevel.DEBUG)) {
      // Can happen if the connection is closed before the SendReplyNot
      // arrives.
      logger.log(BasicLevel.DEBUG,
                 "UserAgent: unknown client context for " + not);
    }
  }

  /**
   * Sends a notification to the specified agent.
   *
   * @param to the identifier of the recipient agent
   * @param not the notification to send
   */
  public void sendNot(AgentId to, Notification not) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
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
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "UserAgent.sendToClient(" + key + ',' + reply + ')');
    Integer objKey = new Integer(key);
    if (connections != null) {
      ConnectionContext ctx = (ConnectionContext)connections.get(objKey);
      if (ctx != null) {
        ctx.pushReply(reply);
      }
    }
    // else may happen. Drop the reply.
  }

  /**
   * Timer task responsible for closing the connection if 
   * it has not sent any requests for the duration 'timeout'.
   */
  class HeartBeatTask extends fr.dyade.aaa.util.TimerTask implements
  java.io.Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private int timeout;

    private Integer key;

    private long lastRequestDate;

    HeartBeatTask(int timeout, Integer key) {
      this.timeout = timeout;
      this.key = key;
    }

    public void run() {
      long date = System.currentTimeMillis();
      if ((date - lastRequestDate) > timeout) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "HeartBeatTask: close connection");
        ConnectionContext ctx = (ConnectionContext) connections.remove(key);
        heartBeatTasks.remove(key);
        proxyImpl.reactToClientRequest(key.intValue(), new CnxCloseRequest());
        MomException exc = new MomException(MomExceptionReply.HBCloseConnection,
                                            "Connection " + getId() + ':' + key + " closed");
        ctx.pushError(exc);
      } else {
        start();
      }
    }

    public void start() {
      try {
        AgentServer.getTimer().schedule(this, timeout);
      } catch (Exception exc) {
        throw new Error(exc.toString());
      }
    }

    public void touch() {
      lastRequestDate = System.currentTimeMillis();
    }
  }

  public void setNoSave() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "setNoSave()");

    super.setNoSave();
  }

  public void setSave() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "UserAgent.setSave()");

    super.setSave();
  }

  public void readBag(ObjectInputStream in) throws IOException,
  ClassNotFoundException {
    connections = (Hashtable) in.readObject();
    heartBeatTasks = (Hashtable) in.readObject();

    if (heartBeatTasks != null) {
      // Start the tasks
      Enumeration tasks = heartBeatTasks.elements();
      while (tasks.hasMoreElements()) {
        HeartBeatTask task = (HeartBeatTask) tasks.nextElement();
        task.start();
      }
    }

    proxyImpl.readBag(in);
  }

  public void writeBag(ObjectOutputStream out) throws IOException {
    out.writeObject(connections);
    out.writeObject(heartBeatTasks);
    proxyImpl.writeBag(out);
  }
}
