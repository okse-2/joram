/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2004 ScalAgent Distributed Technologies
 * Copyright (C) 2004 - France Telecom R&D
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

import fr.dyade.aaa.agent.AgentId;
import org.objectweb.joram.mom.MomTracing;
import org.objectweb.joram.shared.client.AbstractJmsReply;
import org.objectweb.joram.shared.client.XACnxPrepare;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.io.*;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * The <code>ClientContext</code> class holds the data related to a client
 * context.
 */
class ClientContext implements java.io.Serializable
{
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

  /**
   * Constructs a <code>ClientContext</code> instance.
   *
   * @param proxyId  The proxy's agent identifier. 
   * @param id  Identifier of the context.
   */
  ClientContext(AgentId proxyId, int id)
  {
    this.proxyId = proxyId;
    this.id = id;

    tempDestinations = new Vector();
    deliveringQueues = new Hashtable();

    started = false;
    cancelledRequestId = -1;
    activeSubs = new Vector();
    repliesBuffer = new Vector();
  }

 
  /** Returns the identifier of the context. */
  int getId()
  {
    return id;
  }

  /** Sets the activation status of the context. */
  void setActivated(boolean started) {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG,
        "ClientContext[" + proxyId + ',' + id + 
        "].setActivated(" + started + ')');
    this.started = started;
  }

  /** Returns <code>true</code> if the context is activated. */
  boolean getActivated()
  {
    return started;
  }

  /** Adds a temporary destination identifier. */
  void addTemporaryDestination(AgentId destId)
  {
    tempDestinations.add(destId);
  }
   
  /** Returns the temporary destinations' identifiers. */
  Enumeration getTempDestinations()
  {
    return tempDestinations.elements();
  }

  /** Removes a temporary destination identifier. */
  void removeTemporaryDestination(AgentId destId)
  {
    deliveringQueues.remove(destId);
    tempDestinations.remove(destId);
  }

  /** Adds a pending delivery. */
  void addPendingDelivery(AbstractJmsReply reply) {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG,
        "ClientContext[" + proxyId + ',' + id + 
        "].addPendingDelivery(" + reply + ')');
    repliesBuffer.add(reply);
  }

  /** Returns the pending deliveries. */
  Enumeration getPendingDeliveries()
  {
    return repliesBuffer.elements();
  }

  /** Clears the pending deliveries buffer. */
  void clearPendingDeliveries()
  {
    repliesBuffer.clear();
  }

  /** Adds an active subscription name. */
  void addSubName(String subName)
  {
    activeSubs.add(subName);
  }

  /** Returns the active subscriptions' names. */
  Enumeration getActiveSubs()
  {
    return activeSubs.elements();
  }

  /** Removes an active subscription name. */
  void removeSubName(String subName)
  {
    activeSubs.remove(subName);
  }
  
  /** Cancels a "receive" request. */
  void cancelReceive(int cancelledRequestId)
  {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, 
        "ClientContext[" + proxyId + ':' + id + 
        "].cancelReceive(" + cancelledRequestId + ')');
    this.cancelledRequestId = cancelledRequestId;
  }

  /** Returns the cancelled "receive" request identifier. */
  int getCancelledReceive()
  {
    return cancelledRequestId;
  }

  /** Adds the identifier of a delivering queue. */ 
  void addDeliveringQueue(AgentId queueId)
  {
    deliveringQueues.put(queueId, queueId);
  }

  /** Returns the identifiers of the delivering queues. */
  Enumeration getDeliveringQueues()
  {
    return deliveringQueues.keys();
  }

  /** Registers a given transaction "prepare". */
  void registerTxPrepare(Object key, XACnxPrepare prepare) throws Exception
  {
    if (transactionsTable == null)
      transactionsTable = new Hashtable();

    if (! transactionsTable.containsKey(key))
      transactionsTable.put(key, prepare);
    else
      throw new Exception("Prepare request already received by "
                          + "TM for this transaction.");
  }

  /** Returns and deletes a given transaction "prepare". */
  XACnxPrepare getTxPrepare(Object key)
  {
    XACnxPrepare prepare = null;
    if (transactionsTable != null)
      prepare = (XACnxPrepare) transactionsTable.remove(key);
    return prepare;
  }

  /** Returns the identifiers of the prepared transactions. */
  Enumeration getTxIds()
  {
    if (transactionsTable == null)
      return new Hashtable().keys();
    return transactionsTable.keys();
  }

  public void readBag(ObjectInputStream in) 
    throws IOException, ClassNotFoundException {
    started = in.readBoolean();
    cancelledRequestId = in.readInt();
    activeSubs = (Vector)in.readObject();
    repliesBuffer = (Vector)in.readObject();
  }

  public void writeBag(ObjectOutputStream out)
    throws IOException {
    out.writeBoolean(started);
    out.writeInt(cancelledRequestId);
    out.writeObject(activeSubs);
    out.writeObject(repliesBuffer);
  }
}
