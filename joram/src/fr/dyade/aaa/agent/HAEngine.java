/*
 * Copyright (C) 2004 - 2005 ScalAgent Distributed Technologies
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
 * Contributor(s): 
 */
package fr.dyade.aaa.agent;

import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.*;

/**
 *  Implementation of Engine that used JGroups in order to improve 
 *  fiability.
 */
final class HAEngine extends Engine {
  /**  Queue of messages provide from external agent. */ 
  private Vector qinFromExt;

  /** JGroups component */
  private JGroups jgroups = null;

  private static long DEFAULT_HA_TIMEOUT = 10000;
  private static String HA_TIMEOUT_PROPERTY = "fr.dyade.aaa.agent.HAEngine.HA_TIMEOUT";

  HAEngine() throws Exception {
    super();

    qinFromExt = new Vector();
    requestor = new Vector();
    
    timeout = Long.getLong(HA_TIMEOUT_PROPERTY, DEFAULT_HA_TIMEOUT).longValue();
  }

  public void setJGroups(JGroups jgroups) {
    this.jgroups = jgroups;
  }

  /**
   * Saves logical clock information to persistent storage.
   */
  public void save() throws IOException {}

  /**
   * Restores logical clock information from persistent storage.
   */
  public void restore() throws Exception {}

  /**
   *  Adds a message in "ready to deliver" list. This method allocates a
   * new time stamp to the message ; be Careful, changing the stamp imply
   * the filename change too.
   */
  public synchronized void post(Message msg) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, getName() + " post(" + msg +")");

    if (EngineThread.class.isInstance(Thread.currentThread())) {
      // It's an internal message (from an agent reaction).
      super.post(msg);
    } else {
      // send to JGROUPS msg
      if (jgroups.coordinator) jgroups.send(msg);

      stamp(msg);
      msg.save();

      qinFromExt.addElement(msg);
    }
  }

  /**
   *  If the internal queue is empty, moves the first message from external
   * queue. Il must always be bracketed by transaction begin and release
   * methods.
   */
  private void postFromExt() {
    Message msg = null;
    if (qin.size() == 0) {
      try {
        msg = (Message) qinFromExt.elementAt(0);
        qinFromExt.removeElementAt(0);
      } catch (ArrayIndexOutOfBoundsException exc) {
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG,
                     getName() + ", postFromExt(): qinFromExt empty");
        return;
      }
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + ", postFromExt() -> " + msg);
      qin.push(msg);
      qin.validate();
      return;
    }
    if (logmon.isLoggable(BasicLevel.DEBUG)) {
      logmon.log(BasicLevel.DEBUG, getName() + ", postFromExt()");
    }
  }

  /**
   * Validates all messages pushed in queue during transaction session.
   */
  public void validate() {
    if (! needToSync) postFromExt();
    super.validate();
  }

  /**
   * Commit the agent reaction in case of rigth termination:<ul>
   * <li>suppress the processed notification from message queue,
   * then deletes it ;
   * <li>push all new notifications in qin and qout, and saves them ;
   * <li>saves the agent state ;
   * <li>then commit the transaction to validate all changes.
   * </ul>
   */
  void commit() throws Exception {
    if (! requestor.isEmpty())
      needToSync = true;

    if (msg != null)
      super.commit();

    if (needToSync && (qin.size() == 0)) {
      // Get state server
      getState();

      // Now feed qin if possible
      needToSync = false;
    }
    postFromExt();
  }

  /**
   * Abort the agent reaction in case of error during execution. In case
   * of unrecoverable error during the reaction we have to rollback:<ul>
   * <li>reload the previous state of agent ;
   * <li>remove the failed notification ;
   * <li>clean the Channel queue of all pushed notifications ;
   * <li>send an error notification to the sender ;
   * <li>then commit the transaction to validate all changes.
   * </ul>
   */
  void abort(Exception exc) throws Exception {
    super.abort(exc);
    postFromExt();
  }
  
  void receiveFromJGroups(Message msg) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 getName() + " receiveFromJGroups(" + msg + ")");

    AgentServer.getTransaction().begin();
    stamp(msg);
    msg.save();
    
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,getName() + 
                 " receiveFromJGroups qin.size() = " + qin.size() +
                 ", qinFromExt.size() = " + qinFromExt.size());

    AgentServer.getTransaction().commit();
    qinFromExt.addElement(msg);
    postFromExt();
    AgentServer.getTransaction().release();
  }

  volatile boolean needToSync = false;
  volatile Vector requestor = null;

  static private final byte[] OOS_STREAM_HEADER = {
    (byte)((ObjectStreamConstants.STREAM_MAGIC >>> 8) & 0xFF),
    (byte)((ObjectStreamConstants.STREAM_MAGIC >>> 0) & 0xFF),
    (byte)((ObjectStreamConstants.STREAM_VERSION >>> 8) & 0xFF),
    (byte)((ObjectStreamConstants.STREAM_VERSION >>> 0) & 0xFF)
  };

  /**
   * Get the current state of Engine: agents, messages, etc.
   * This operation is done in transactoion so Engine can't get more
   * messages and Network can't post new !!
   */
  synchronized void getState() throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, getName() + ", getState()");

    HAStateReply reply = new HAStateReply();
    // Get clock
    reply.now = now;
    reply.stamp = getStamp();
    // gets state of all agents
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    try {
      oos.writeObject(AgentIdStamp.stamp);
      for (Enumeration e = agents.elements(); e.hasMoreElements();) {
        Agent agent = (Agent) e.nextElement();
        // Don't put the agent factory
        if (! (agent instanceof AgentFactory)) {
          oos.writeObject(agent.getId());
          oos.writeObject(agent);
          if (agent instanceof BagSerializer) {
            ((BagSerializer) agent).writeBag(oos);
          }
        }
      }
      oos.flush();
      reply.agents = baos.toByteArray();
    } finally {
      try {
        oos.close();
      } catch (Exception exc) {}
    }

    // gets all pending messages
    baos.reset();
    oos = new ObjectOutputStream(baos);
    try {
      oos.writeObject(qinFromExt);
      reply.messages = baos.toByteArray();
    } finally {
      try {
        oos.close();
      } catch (Exception exc) {}
    }

    // Get Network clock
    reply.setNetworkStamp(jgroups.network.getStamp());

//    while (! requestor.isEmpty()) {
//      jgroups.sendTo((org.jgroups.Address) requestor.firstElement(), reply);
//      requestor.removeElementAt(0);
//    }
    requestor.clear();
    jgroups.send(reply);
  }

  synchronized void setState(HAStateReply reply) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, getName() + ", setState()");
    
    now = reply.now;
    setStamp(reply.stamp);
    // creates all agents
    ByteArrayInputStream bis = new ByteArrayInputStream(reply.agents);
    ObjectInputStream ois = new ObjectInputStream(bis);
    try {
      AgentId id = null;
      Agent agent = null;
      AgentIdStamp.stamp = (AgentIdStamp) ois.readObject();
      while (true) {
        id = (AgentId) ois.readObject();
        agent = (Agent) ois.readObject();
        agent.id = id;
        agent.deployed = true;
        // If there is a bag associated with this agent don't initialize it!
        if (agent instanceof BagSerializer) {
          ((BagSerializer) agent).readBag(ois);
        } else {
          agent.agentInitialize(false);
        }
        createAgent(agent);
      }
    } catch (EOFException exc) {
    } finally {
      try {
        ois.close();
      } catch (Exception exc) {}
    }
    // inserts all pending messages
    bis = new ByteArrayInputStream(reply.messages);
    ois = new ObjectInputStream(bis);
    try {
      qinFromExt = (Vector) ois.readObject();
      postFromExt();
    } finally {
      try {
        ois.close();
      } catch (Exception exc) {}
    }
  }

  Object load(byte[] buf) throws Exception {
    Object obj = null;
    ByteArrayInputStream bis = new ByteArrayInputStream(buf);
    ObjectInputStream ois = new ObjectInputStream(bis);	  
    obj = ois.readObject();
    try {
      ois.close();
    } catch (IOException exc) {}
    return obj;
  }
  
  protected void onTimeOut() throws Exception {
    if (! requestor.isEmpty())
      commit();
  }
}
