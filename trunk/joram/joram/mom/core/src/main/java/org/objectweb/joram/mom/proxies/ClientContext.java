/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 200 ScalAgent Distributed Technologies
 * Copyright (C) 2004 France Telecom R&D
 * Copyright (C) 2003 - 2004 Bull SA
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
 * Initial developer(s): Frederic Maistre (Bull)
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.mom.proxies; 

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.objectweb.joram.shared.client.AbstractJmsReply;
import org.objectweb.joram.shared.client.XACnxPrepare;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.common.Debug;

/**
 * The <code>ClientContext</code> class holds the data related to a client
 * context.
 */
class ClientContext implements java.io.Serializable {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  /** logger */
  public static Logger logger = Debug.getLogger(ClientContext.class.getName());

  /** The proxy's agent identifier. */
  private AgentId proxyId;

  /** Context identifier. */
  private int id;
  /** Vector of temporary destinations. */
  private Vector tempDestinations;
  /** Identifiers of queues delivering messages. */
  private Hashtable deliveringQueues;
  /** Prepared transactions objects waiting for commit. */
  private Hashtable transactionsTable;

  /** <code>true</code> if the context is activated. */
  private transient boolean started;
  /**
   * Identifier of a cancelled "receive" request, set when a PTP listener has
   * been unset.
   */
  private transient int cancelledRequestId;
  /** Vector of active subscriptions' names. */
  private transient Vector activeSubs;
  /** Pending replies waiting for the context to be activated. */
  private transient Vector repliesBuffer;
  /** Contexts waiting for the replies from some local agents*/
  private transient Hashtable commitTable;
  
  private transient ProxyAgentItf proxy;

  /**
   * Constructs a <code>ClientContext</code> instance.
   *
   * @param proxyId  The proxy's agent identifier. 
   * @param id  Identifier of the context.
   */
  ClientContext(AgentId proxyId, int id) {
    this.proxyId = proxyId;
    this.id = id;

    tempDestinations = new Vector();
    deliveringQueues = new Hashtable();

    started = false;
    cancelledRequestId = -1;
    activeSubs = new Vector();
    repliesBuffer = new Vector();
  }
  
  void setProxyAgent(ProxyAgentItf px) {
    proxy = px;
  }
 
  /** Returns the identifier of the context. */
  int getId() {
    return id;
  }

  /** Sets the activation status of the context. */
  void setActivated(boolean started) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ClientContext[" + proxyId + ',' + id + "].setActivated(" + started + ')');
    this.started = started;
  }

  /** Returns <code>true</code> if the context is activated. */
  boolean getActivated()
  {
    return started;
  }

  /** Adds a temporary destination identifier. */
  void addTemporaryDestination(AgentId destId) {
    tempDestinations.add(destId);
    proxy.setSave();
  }
   
  Iterator getTempDestinations() {
    //Creates an enumeration not backed by tempDestinations
    Vector tempDests = new Vector();
    for (Iterator dests = tempDestinations.iterator(); dests.hasNext();) {
      tempDests.addElement(dests.next());
    }
    return tempDests.iterator();
  }

  /** Removes a temporary destination identifier. */
  void removeTemporaryDestination(AgentId destId) {
    deliveringQueues.remove(destId);
    tempDestinations.remove(destId);
    proxy.setSave();
  }

  /** Adds a pending delivery. */
  void addPendingDelivery(AbstractJmsReply reply) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ClientContext[" + proxyId + ',' + id + "].addPendingDelivery(" + reply + ')');
    repliesBuffer.add(reply);
  }

  /** Returns the pending deliveries. */
  Iterator getPendingDeliveries() {
    return repliesBuffer.iterator();
  }

  /** Clears the pending deliveries buffer. */
  void clearPendingDeliveries() {
    repliesBuffer.clear();
  }

  /** Adds an active subscription name. */
  void addSubName(String subName) {
    activeSubs.add(subName);
  }

  /** Returns the active subscriptions' names. */
  Iterator getActiveSubs() {
    return activeSubs.iterator();
  }

  /** Removes an active subscription name. */
  void removeSubName(String subName) {
    activeSubs.remove(subName);
  }
  
  /** Cancels a "receive" request. */
  void cancelReceive(int cancelledRequestId) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ClientContext[" + proxyId + ':' + id + "].cancelReceive(" + cancelledRequestId + ')');
    this.cancelledRequestId = cancelledRequestId;
  }

  /** Returns the cancelled "receive" request identifier. */
  int getCancelledReceive() {
    return cancelledRequestId;
  }

  /** Adds the identifier of a delivering queue. */ 
  void addDeliveringQueue(AgentId queueId) {
    deliveringQueues.put(queueId, queueId);
    proxy.setSave();
  }

  /** Returns the identifiers of the delivering queues. */
  Iterator getDeliveringQueues() {
    return deliveringQueues.keySet().iterator();
  }
  
  /**
   * Some requests may require to wait for several
   * SendReplyNot notifications before replying to the client.
   * 
   * @param requestId
   * @param asyncReplyCount
   */
  void addMultiReplyContext(int requestId, int asyncReplyCount) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ClientContext[" + proxyId + ':' + id + "].addMultiReplyContext(" + requestId + ',' + asyncReplyCount + ')');
    if (commitTable == null) commitTable = new Hashtable();
    commitTable.put(new Integer(requestId), new MultiReplyContext(asyncReplyCount));
    proxy.setSave();
  }
  
  /**
   * Called by UserAgent when a SendReplyNot
   * arrived.
   * 
   * @param requestId
   * @return
   * > 0 if there are still some pending replies
   * 0 if all the replies arrived (the context is removed)
   * or if the context doesn't exist
   */
  int setReply(int requestId) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ClientContext[" + proxyId + ':' + id + "].setReply(" + requestId + ')');
    if (commitTable == null) return 0;
    Integer ctxKey = new Integer(requestId);
    MultiReplyContext ctx = (MultiReplyContext)commitTable.get(ctxKey);
    if (ctx == null) return 0;

    ctx.counter--;
    if (ctx.counter == 0) {
      commitTable.remove(ctxKey);
      proxy.setSave();
    }
    return ctx.counter;
  }
  
  static class MultiReplyContext {
    public int counter;
    
    MultiReplyContext(int c) {
      counter = c;
    }
  }

  /** Registers a given transaction "prepare". */
  void registerTxPrepare(Object key, XACnxPrepare prepare) throws Exception {
    if (transactionsTable == null)
      transactionsTable = new Hashtable();

    if (! transactionsTable.containsKey(key)) {
      transactionsTable.put(key, prepare);
      proxy.setSave();
    } else
      throw new Exception("Prepare request already received by "
                          + "TM for this transaction.");
  }

  /** Returns and deletes a given transaction "prepare". */
  XACnxPrepare getTxPrepare(Object key) {
    XACnxPrepare prepare = null;
    if (transactionsTable != null) {
      prepare = (XACnxPrepare) transactionsTable.remove(key);
      proxy.setSave();
    }
    return prepare;
  }

  /**
   * 
   * @param key XID
   * @return true if prepared transation.
   */
  public boolean isPrepared(Object key) {
    return transactionsTable.containsKey(key);
  }
  
  /** Returns the identifiers of the prepared transactions. */
  Iterator getTxIds() {
    if (transactionsTable == null)
      return new Hashtable().keySet().iterator();
    return transactionsTable.keySet().iterator();
  }

  public void readBag(ObjectInputStream in) 
    throws IOException, ClassNotFoundException {
    started = in.readBoolean();
    cancelledRequestId = in.readInt();
    activeSubs = (Vector)in.readObject();
    repliesBuffer = (Vector)in.readObject();
  }

  public void writeBag(ObjectOutputStream out) throws IOException {
    out.writeBoolean(started);
    out.writeInt(cancelledRequestId);
    out.writeObject(activeSubs);
    out.writeObject(repliesBuffer);
  }

  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("ClientContext (proxyId=");
    buff.append(proxyId);
    buff.append(",id=");
    buff.append(id);
    buff.append(",tempDestinations=");
    buff.append(tempDestinations);
    buff.append(",deliveringQueues=");
    buff.append(deliveringQueues);
    buff.append(",transactionsTable=");
    buff.append(transactionsTable);
    buff.append(",started=");
    buff.append(started);
    buff.append(",cancelledRequestId=");
    buff.append(cancelledRequestId);
    buff.append(",activeSubs=");
    buff.append(activeSubs);
    buff.append(",repliesBuffer=");
    buff.append(repliesBuffer);
    buff.append(')');
    return buff.toString();
  }
}
