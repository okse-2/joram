/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - ScalAgent Distributed Technologies
 * Copyright (C) 2004 - France Telecom R&D
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

import fr.dyade.aaa.agent.*;
import fr.dyade.aaa.agent.management.MXWrapper;
import fr.dyade.aaa.util.Queue;

import java.io.*;
import java.util.*;

import org.objectweb.joram.mom.MomTracing;
import org.objectweb.joram.shared.client.*;
import org.objectweb.joram.mom.proxies.ProxyImpl;
import org.objectweb.joram.mom.proxies.SendReplyNot;
import org.objectweb.joram.mom.proxies.ProxyAgentItf;
import org.objectweb.joram.mom.notifications.*;

import org.objectweb.util.monolog.api.BasicLevel;

/** 
 * Class of a user proxy agent.
 */
public class UserAgent extends Agent 
    implements BagSerializer, ProxyAgentItf {

  /**
   * All the user requests are delegated
   * to the proxy
   */
  private ProxyImpl proxyImpl;

  /**
   * Table that contains the user connections
   * key = <code>Integer</code> (connection key)
   * value = <code></code>
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
   * @see AdminTopicImpl
   * @see ConnectionManager
   */
  public UserAgent() {
    super(true);
    init();
  }

  /**
   * Creates a new user proxy.
   *
   * @see AdminTopicImpl
   * @see ConnectionManager
   */
  public UserAgent(int stamp) {
    super("AdminProxy", true, stamp);
    init();
  }

  private void init() {
    proxyImpl = new ProxyImpl(this);
    keyCounter = 0;
  }

  /** (Re)initializes the agent when (re)loading. */
  public void agentInitialize(boolean firstTime) throws Exception {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, "UserAgent.agentInitialize(" + 
        firstTime + ')');
    super.agentInitialize(firstTime);
    proxyImpl.initialize(firstTime);
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
      doReact((OpenConnectionNot)not);
    } else if (not instanceof GetConnectionNot) {
      doReact((GetConnectionNot)not);
    } else if (not instanceof ProxyMessageNot) {
      doReact((ProxyMessageNot)not);
    } else if (not instanceof CloseConnectionNot) {
      doReact((CloseConnectionNot)not);
    } else if (not instanceof ResetCollocatedConnectionsNot) {
      doReact((ResetCollocatedConnectionsNot)not);
    } else if (not instanceof SendReplyNot) {
      doReact((SendReplyNot)not);
    } else if (not instanceof RequestNot) {
      doReact((RequestNot)not);
    } else if (not instanceof ReturnConnectionNot) {
      doReact((ReturnConnectionNot)not);
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
    if (connections == null) {
      connections = new Hashtable();
      heartBeatTasks = new Hashtable();
    }
    
    Integer objKey = new Integer(keyCounter);    
    Object queue;
    if (not.getReliable()) {
      ReliableConnectionContext ctx = 
	new ReliableConnectionContext(
          keyCounter,
          not.getHeartBeat());
      connections.put(objKey, ctx);
      queue = ctx.queue;
    } else {
      ConnectionContext ctx = 
	new ConnectionContext(keyCounter);
      connections.put(objKey, ctx);
      queue = ctx.queue;
    }

    if (not.getHeartBeat() > 0) {
      HeartBeatTask heartBeatTask = new HeartBeatTask(
        2 * not.getHeartBeat(), objKey);
      heartBeatTasks.put(objKey, heartBeatTask);
      heartBeatTask.start();
    }

    // Differs the reply because the connection key counter
    // must be saved before the OpenConnectionNot returns.
    sendTo(getId(), new ReturnConnectionNot(
      not, keyCounter, queue));
    keyCounter++;
  }
  
  /**
   * Differs the reply because the connection key counter
   * must be saved before the OpenConnectionNot returns.
   */
  private void doReact(ReturnConnectionNot not) {
    setNoSave();
    not.Return();
  }
  
  private void doReact(GetConnectionNot not) {
    int key = not.getKey();    
    if (connections == null) {
      not.Throw(new Exception(
        "Connection " + key + " not found"));
    } else {
      Integer objKey = new Integer(key);
      ReliableConnectionContext ctx = 
        (ReliableConnectionContext)connections.get(objKey);      
      if (ctx == null) {
	not.Throw(new Exception(
          "Connection " + key + " not found"));
      } else {
        not.Return(
          ctx.inputCounter,
          ctx.queue,
          ctx.heartBeat);
      }
    }
  }

  /**
   * This saving policy should be coded in ProxyImpl.
   */
  private void save(AbstractJmsRequest request) {
    if (request instanceof ProducerMessages ||
        request instanceof QBrowseRequest) {
      setNoSave();
    } else if (request instanceof ConsumerReceiveRequest) {
      ConsumerReceiveRequest crr = (ConsumerReceiveRequest)request;
      if (crr.getQueueMode()) setNoSave();
    } else if (request instanceof ConsumerSetListRequest) {
      ConsumerSetListRequest cslr = (ConsumerSetListRequest)request;
      if (cslr.getQueueMode()) setNoSave();
    }
  }

  private void doReact(RequestNot not) {
    save((AbstractJmsRequest)not.getMessage());
    Integer key = new Integer(not.getConnectionKey());
    if (connections != null) {
      ConnectionContext ctx = 
        (ConnectionContext)connections.get(key);
      if (ctx != null) {
        HeartBeatTask heartBeatTask = 
          (HeartBeatTask)heartBeatTasks.get(key);
        if (heartBeatTask != null) {
          heartBeatTask.touch();
        }
        
        proxyImpl.reactToClientRequest(
          ctx.key, 
          (AbstractJmsRequest)not.getMessage());
        if (not.getMessage() instanceof CnxCloseRequest) {
          CnxCloseRequest request = (CnxCloseRequest)not.getMessage();
          connections.remove(key);
          HeartBeatTask hbt = 
            (HeartBeatTask)heartBeatTasks.remove(key);
          if (hbt != null)
            hbt.cancel();
          CnxCloseReply reply = new CnxCloseReply();
          reply.setCorrelationId(request.getRequestId());
          ctx.queue.push(reply);
        }
      }
    }
    // else should not happen because:
    // - RequestNot is transient
    // - RequestNot always follows an OpenConnection or
    // a GetConnection
  }

  private void doReact(ProxyMessageNot not) {
    ProxyMessage msg = not.getMessage();
    save((AbstractJmsRequest)msg.getObject());
    Integer key = new Integer(not.getConnectionKey());
    if (connections != null) {
      ReliableConnectionContext ctx = 
        (ReliableConnectionContext)connections.get(key);

      if (ctx != null) {
        HeartBeatTask heartBeatTask = 
          (HeartBeatTask)heartBeatTasks.get(key);
        if (heartBeatTask != null) {
          heartBeatTask.touch();
        }
        
        receiveReliableMessage(ctx, not.getMessage());
        
        if (msg.getObject() instanceof CnxCloseRequest) {
          CnxCloseRequest request = (CnxCloseRequest)msg.getObject();
          connections.remove(key);
          HeartBeatTask hbt = 
            (HeartBeatTask)heartBeatTasks.remove(key);
          if (hbt != null)
            hbt.cancel();
          CnxCloseReply reply = new CnxCloseReply();
          reply.setCorrelationId(request.getRequestId());
          push(ctx, reply);
        }
      }
    }
    // else should not happen because:
    // - ProxyMessageNot is transient
    // - ProxyMessageNot always follows an OpenConnection or
    // a GetConnection
  }

  private void receiveReliableMessage(
    ReliableConnectionContext ctx,
    ProxyMessage msg) {
    if (msg != null) {
      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(
          BasicLevel.DEBUG, " -> msg = " + msg + ')');
      ctx.inputCounter = msg.getId();	
      proxyImpl.reactToClientRequest(
        ctx.key, (AbstractJmsRequest)msg.getObject());
    }
    ctx.queue.ack(msg.getAckId());
  }

  private void doReact(CloseConnectionNot not) {
    if (connections != null) {
      Integer key = new Integer(not.getKey());
      // The connection may have already been 
      // explicitely closed by a CnxCloseRequest.
      if (connections.remove(key) != null) {
        proxyImpl.reactToClientRequest(
          not.getKey(), new CnxCloseRequest());        
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
        if (obj instanceof ConnectionContext) {
          ConnectionContext cc = (ConnectionContext)obj;
          proxyImpl.reactToClientRequest(
            cc.key, new CnxCloseRequest());
          iterator.remove();
        }
      }
    }
  }

  private void doReact(SendReplyNot not) {
    setNoSave();
    sendToClient(
      not.getKey(), new ServerReply(not.getRequest()));
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
    if (connections != null) {
      Object ctx = connections.get(objKey);
      if (ctx != null) {
      if (ctx instanceof ReliableConnectionContext) {
        push((ReliableConnectionContext)ctx, reply);
        } else if (ctx instanceof ConnectionContext) {
          ((ConnectionContext)ctx).queue.push(reply);
      } else {
          if (MomTracing.dbgProxy.isLoggable(BasicLevel.ERROR))
            MomTracing.dbgProxy.log(
              BasicLevel.ERROR, "Unexpected connection context: " + ctx);
        }
      }
    }
    // else may happen. Drop the reply.
  }

  private static void push(ReliableConnectionContext ctx, 
			   AbstractJmsReply reply) {
    ProxyMessage msg = new ProxyMessage(
      ctx.outputCounter, ctx.inputCounter, reply);
    ctx.queue.push(msg);
    ctx.outputCounter++;
  }

  static class ReliableConnectionContext 
      implements java.io.Serializable {
    public int key;
    public long inputCounter;
    public long outputCounter;
    public AckedQueue queue;
    public int heartBeat;

    ReliableConnectionContext(int key,
                              int heartBeat) {
      this.key = key;
      this.heartBeat = heartBeat;
      inputCounter = -1;
      outputCounter = 0;
      queue = new AckedQueue();
    }
  }

  static class ConnectionContext 
      implements java.io.Serializable {
    public int key;
    public Queue queue;

    ConnectionContext(int key) {
      this.key = key;
      queue = new Queue();
    }
  }

  /**
   * Timer task responsible for closing the connection if 
   * it has not sent any requests for the duration 'timeout'.
   */
  class HeartBeatTask  
      extends fr.dyade.aaa.util.TimerTask
    implements java.io.Serializable {
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
        if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgProxy.log(
            BasicLevel.DEBUG,
            "HeartBeatTask: close connection");
        Object ctx = connections.remove(key);
        heartBeatTasks.remove(key);
        proxyImpl.reactToClientRequest(
          key.intValue(), new CnxCloseRequest());
        Exception exc = new Exception(
          "Connection " + getId() + ':' + key + " closed");
        if (ctx instanceof ReliableConnectionContext) {
          ((ReliableConnectionContext)ctx).queue.push(
            new ProxyMessage(-1, -1, exc));
        } else if (ctx instanceof ConnectionContext) {
          ((ConnectionContext)ctx).queue.push(exc);
        } else {
          if (MomTracing.dbgProxy.isLoggable(BasicLevel.ERROR))
            MomTracing.dbgProxy.log(
              BasicLevel.ERROR, "Unexpected context: " + ctx);
        }
      } else {
        start();
      }
    }

    public void start() {
      try {
        ConnectionManager.getTimer().schedule(
          this, timeout);
      } catch (Exception exc) {
        throw new Error(exc.toString());
      }
    }

    public void touch() {
      lastRequestDate = System.currentTimeMillis();
    }
  }

  public void readBag(ObjectInputStream in) 
    throws IOException, ClassNotFoundException {
    connections = (Hashtable)in.readObject();
    heartBeatTasks = (Hashtable)in.readObject();

    if (heartBeatTasks != null) {
      // Start the tasks
      Enumeration tasks = heartBeatTasks.elements();
      while (tasks.hasMoreElements()) {
        HeartBeatTask task = (HeartBeatTask)tasks.nextElement();
        task.start();
      }
    }

    proxyImpl.readBag(in);
  }

  public void writeBag(ObjectOutputStream out)
    throws IOException {
    out.writeObject(connections);
    out.writeObject(heartBeatTasks);
    proxyImpl.writeBag(out);
  }
}
