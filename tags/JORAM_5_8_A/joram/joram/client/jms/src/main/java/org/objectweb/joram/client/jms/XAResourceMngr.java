/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2009 ScalAgent Distributed Technologies
 * Copyright (C) 2004 - 2000 Bull SA
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
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.client.jms;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.jms.JMSException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.objectweb.joram.shared.client.ProducerMessages;
import org.objectweb.joram.shared.client.SessAckRequest;
import org.objectweb.joram.shared.client.XACnxCommit;
import org.objectweb.joram.shared.client.XACnxPrepare;
import org.objectweb.joram.shared.client.XACnxRecoverReply;
import org.objectweb.joram.shared.client.XACnxRecoverRequest;
import org.objectweb.joram.shared.client.XACnxRollback;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * Utility class used by XA connections for managing XA resources.
 */
public class XAResourceMngr {
  /** Transaction active. */
  public static final int STARTED = 0;
  /** Transaction suspended. */
  public static final int SUSPENDED = 1;
  /** Transaction successful. */
  public static final int SUCCESS = 2;
  /** Failed transaction. */
  public static final int ROLLBACK_ONLY = 3;
  /** Prepared transaction. */
  public static final int PREPARED = 4;

  private static Logger logger = Debug.getLogger(XAResourceMngr.class.getName());

  /**
   * The table of known transactions.
   * <p>
   * <b>Key:</b> transaction identifier<br>
   * <b>Object:</b> <code>XAContext</code> instance
   */
  private Hashtable transactions;

  /** The connection this manager belongs to. */
  Connection cnx;

  /** table of Session (key Xid). */
  Hashtable sessionTable;

  /**
   * Creates a <code>XAResourceMngr</code> instance.
   *
   * @param cnx   The connection this manager belongs to.
   */
  public XAResourceMngr(Connection cnx) {
    this.cnx = cnx;
    transactions = new Hashtable();
    sessionTable = new Hashtable();

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                                 " XAResourceMngr cnx = " + cnx);
  }

  /**
   * Notifies the RM that a transaction is starting.
   *
   * @exception XAException  If the specified transaction is already known by
   *                         the RM in an incompatible state with the start
   *                         request.
   */
  synchronized void start(Xid xid, int flag,
                          Session sess) throws XAException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                                 " XAResourceMngr start(" + xid + ", " + flag + ", " + sess +")");

    sess.setTransacted(true); // for XAResource.TMRESUME
    sessionTable.put(xid,sess);

    // New transaction.
    if (flag == XAResource.TMNOFLAGS) {
      if (transactions.containsKey(xid))
        throw new XAException("Can't start transaction already known by RM.");

      transactions.put(xid, new XAContext());

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
                                   "--- " + this + ": involved in transaction " + xid.toString()); 
    } else if (flag == XAResource.TMRESUME) {
      // Resumed transaction.
      if (! transactions.containsKey(xid))
        throw new XAException("Can't resume unknown transaction.");

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
                                   "--- " + this + ": resumes transaction " + xid.toString()); 
    } else if (flag == XAResource.TMJOIN) {
      // Already known transaction.
      if (! transactions.containsKey(xid))
        throw new XAException("Can't join unknown transaction.");

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
                                   "--- " + this + ": joins transaction " + xid.toString()); 
    } else
      throw new XAException("Invalid flag: " + flag);

    setStatus(xid, STARTED);
  } 

  /**
   * Notifies the RM that a transaction is ended.
   *
   * @exception XAException  If the specified transaction is in an
   *                         incompatible state with the end request.
   */
  synchronized void end(Xid xid, int flag,
                        Session sess) throws XAException {
    boolean saveResourceState = true;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                                 "--- " + this + ": end(" + xid + ", " + flag + ", " + sess + ")"); 
    
    if (flag == XAResource.TMSUSPEND) {
      if (getStatus(xid) != STARTED)
        throw new XAException("Can't suspend non started transaction.");

      setStatus(xid, SUSPENDED);
    } else {
      if (getStatus(xid) != STARTED && getStatus(xid) != SUSPENDED)
        throw new XAException("Can't end non active or non "
                              + "suspended transaction.");

      // No need to save the resource's state as it has already been done
      // when suspending it.
      if (getStatus(xid) == SUSPENDED)
        saveResourceState = false;

      if (flag == XAResource.TMSUCCESS)
        setStatus(xid, SUCCESS);
      else if (flag == XAResource.TMFAIL)
        setStatus(xid, ROLLBACK_ONLY);
      else
        throw new XAException("Invalid flag: " + flag);
    }

    if (saveResourceState) {
      XAContext xaC = (XAContext) transactions.get(xid);
      xaC.addSendings(sess.sendings);
      xaC.addDeliveries(sess.deliveries);
    }

    Session session = (Session) sessionTable.get(xid);
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                                 "--- " + this + ": end(...) session="  + session);

    if (session != null) {
      session.setTransacted(false);
      sessionTable.remove(xid);
    }
  }

  /** 
   * Notifies the RM that a transaction is prepared.
   *
   * @exception XAException  If the specified transaction is in an
   *                         incompatible state with the prepare request,
   *                         or if the request fails.
   */
  synchronized void prepare(Xid xid) throws XAException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                                 "--- " + this  + ": prepare(" + xid + ")"); 
    
    try {
      if (getStatus(xid) == ROLLBACK_ONLY)
        throw new XAException("Can't prepare resource in ROLLBACK_ONLY state.");

      XAContext xaC = (XAContext) transactions.get(xid);

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
                                   "--- "
                                   + this
                                   + ": prepares transaction "
                                   + xid.toString()); 

      Enumeration targets;    
      String target;
      Vector pMs = new Vector();
      MessageAcks acks;
      Vector sessAcks = new Vector();

      // Getting all the ProducerMessages to send:
      targets = xaC.sendings.keys();
      while (targets.hasMoreElements()) {
        target = (String) targets.nextElement();
        pMs.add(xaC.sendings.remove(target));
      }

      // Getting all the SessAckRequest to send:
      targets = xaC.deliveries.keys();
      while (targets.hasMoreElements()) {
        target = (String) targets.nextElement();
        acks = (MessageAcks) xaC.deliveries.remove(target);
        sessAcks.add(new SessAckRequest(target, acks.getIds(),
                                        acks.getQueueMode()));
      }

      // Sending to the proxy:
      cnx.syncRequest(new XACnxPrepare(xid.getBranchQualifier(),
                                        xid.getFormatId(),
                                        xid.getGlobalTransactionId(),
                                        pMs,
                                        sessAcks));

      setStatus(xid, PREPARED);
    } catch (JMSException exc) {
      setStatus(xid, ROLLBACK_ONLY);
      throw new XAException("Prepare request failed: " + exc);
    } catch (XAException exc) {
      setStatus(xid, ROLLBACK_ONLY);
      throw exc;
    }
  }

  /** 
   * Notifies the RM that a transaction is commited.
   *
   * @exception XAException  If the specified transaction is in an
   *                         incompatible state with the commit request,
   *                         or if the request fails.
   */
  synchronized void commit(Xid xid) throws XAException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                                 "--- " + this + ": commit(" + xid + ")"); 

    try {
      if (getStatus(xid) != PREPARED)
        throw new XAException("Can't commit non prepared transaction.");

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
                                   "--- " + this + ": commits transaction " + xid.toString()); 

      cnx.syncRequest(new XACnxCommit(xid.getBranchQualifier(),
                                      xid.getFormatId(),
                                      xid.getGlobalTransactionId())); 

      transactions.remove(xid);
      Session session = (Session) sessionTable.get(xid);
      if (session != null)
        session.setTransacted(false);

    } catch (JMSException exc) {
      setStatus(xid, ROLLBACK_ONLY);
      throw new XAException("Commit request failed: " + exc);
    } catch (XAException exc) {
      setStatus(xid, ROLLBACK_ONLY);
      throw exc;
    }
  }

  /** 
   * Notifies the RM that a transaction is rolled back.
   *
   * @exception XAException  If the specified transaction is in an
   *                         incompatible state with the rollback request,
   *                         or if the request fails.
   */
  synchronized void rollback(Xid xid) throws XAException {

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                                 "--- " + this + ": rollback(" + xid + ")");

    try {
      XAContext xaC = (XAContext) transactions.get(xid);

      if (xaC == null)
        throw new XAException("Unknown transaction.");

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
                                   "--- " + this + ": rolls back transaction " + xid.toString()); 

      Enumeration targets; 
      String target;
      MessageAcks acks;

      XACnxRollback rollbackRequest;
  
      targets = xaC.deliveries.keys();
  
      rollbackRequest = new XACnxRollback(xid.getBranchQualifier(),
                                           xid.getFormatId(),
                                           xid.getGlobalTransactionId());

      while (targets.hasMoreElements()) {
        target = (String) targets.nextElement();
        acks = (MessageAcks) xaC.deliveries.remove(target);
        rollbackRequest.add(target, acks.getIds(), acks.getQueueMode());
      }

      // Sending to the proxy:
      cnx.syncRequest(rollbackRequest);

      transactions.remove(xid);
      Session session = (Session) sessionTable.get(xid);
      if (session != null) {
        session.setTransacted(false);
        sessionTable.remove(xid);
      }
    } catch (JMSException exc) {
      setStatus(xid, ROLLBACK_ONLY);
      throw new XAException("Rollback request failed: " + exc);
    } catch (XAException exc) {
      setStatus(xid, ROLLBACK_ONLY);
      throw exc;
    }
  }

  /** 
   * Notifies the RM to recover the prepared transactions.
   *
   * @exception XAException  If the specified flag is invalid, or if the
   *                         request fails.
   */
  synchronized Xid[] recover(int flag) throws XAException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                                 "--- "
                                 + this
                                 + ": recovers transactions.");

    if (flag == XAResource.TMSTARTRSCAN || flag == XAResource.TMENDRSCAN)
      throw new XAException("Non supported recovery flag: " + flag);

    try {
      XACnxRecoverReply reply =
        (XACnxRecoverReply) cnx.syncRequest(new XACnxRecoverRequest());
     
      if (reply == null) {
        return new Xid[0];  
      }
      
      Xid[] xids = new Xid[reply.getSize()];

      for (int i = 0; i < reply.getSize(); i++) {
        xids[i] = new XidImpl(reply.getBranchQualifier(i),
                              reply.getFormatId(i),
                              reply.getGlobalTransactionId(i));
        transactions.put(xids[i], new XAContext());
        setStatus(xids[i], PREPARED);
      }
      return xids;
    } catch (Exception exc) {
      throw new XAException("Recovery request failed: " + exc.getMessage());
    }
  }

  /** 
   * Sets the status of a transaction.
   *
   * @exception XAException  If the transaction is unknown.
   */
  private void setStatus(Xid xid, int status) throws XAException {
    XAContext xac = (XAContext) transactions.get(xid);

    if (xac == null)
      throw new XAException("Unknown transaction.");

    xac.status = status;
  }

  /** 
   * Gets the status of a transaction.
   *
   * @exception XAException  If the transaction is unknown.
   */
  private int getStatus(Xid xid) throws XAException  {
    XAContext xac = (XAContext) transactions.get(xid);

    if (xac == null)
      throw new XAException("Unknown transaction.");

    return xac.status;
  }

  /** Resource managers are equal if they belong to the same connection. */
  public boolean equals(Object o) {
    if (! (o instanceof XAResourceMngr))
      return false;

    XAResourceMngr other = (XAResourceMngr) o;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                                 this + ": equals other = " + other.cnx + 
                                 ", this.cnx = " + cnx +
                                 ", equals = " + cnx.equals(other.cnx));
    
    return cnx.equals(other.cnx);
  }

  public int hashCode() {
    return cnx.hashCode();
  }
}

/**
 * Utility class holding a resource's state during transaction progress.
 */
class XAContext {
  /** The transaction status. */
  int status;
  /**
   * Table holding the <code>ProducerMessages</code> produced in the
   * transaction.
   * <p>
   * <b>Key:</b> destination name<br>
   * <b>Object:</b> <code>ProducerMessages</code>
   */
  Hashtable sendings;
  /** 
   * Table holding the identifiers of the messages delivered per
   * destination or subscription, in the transaction.
   * <p>
   * <b>Key:</b> destination or subscription name<br>
   * <b>Object:</b> corresponding <code>MessageAcks</code> instance
   */
  Hashtable deliveries;


  /**
   * Constructs an <code>XAContext</code> instance.
   */
  XAContext() {
    sendings = new Hashtable();
    deliveries = new Hashtable();
  }


  /**
   * Adds new sendings performed by the resumed transaction.
   */
  void addSendings(Hashtable newSendings) {
    String newDest;
    ProducerMessages newPM;
    ProducerMessages storedPM;
    Vector msgs;

    // Browsing the destinations for which messages have been produced:
    Enumeration newDests = newSendings.keys();
    while (newDests.hasMoreElements()) {
      newDest = (String) newDests.nextElement();
      newPM = (ProducerMessages) newSendings.remove(newDest);
      storedPM = (ProducerMessages) sendings.get(newDest);
      // If messages haven't already been produced for this destination,
      // storing the new ProducerMessages object:
      if (storedPM == null)
        sendings.put(newDest, newPM);
      // Else, adding the newly produced messages to the existing
      // ProducerMessages:
      else {
        msgs = newPM.getMessages();
        for (int i = 0; i < msgs.size(); i++)
          storedPM.addMessage((org.objectweb.joram.shared.messages.Message) msgs.get(i));
      }
    }
  }

  /**
   * Adds new deliveries occured within the resumed transaction.
   */
  void addDeliveries(Hashtable newDeliveries) {
    String newName;
    MessageAcks newAcks;
    MessageAcks storedAcks;

    // Browsing the destinations or subscriptions to which messages will have
    // to be acknowledged:
    Enumeration newNames = newDeliveries.keys();
    while (newNames.hasMoreElements()) {
      newName = (String) newNames.nextElement();
      newAcks = (MessageAcks) newDeliveries.remove(newName);
      storedAcks = (MessageAcks) deliveries.get(newName);
      // If there are no messages to acknowledge for this destination or 
      // subscription, storing the new vector:
      if (storedAcks == null)
        deliveries.put(newName, newAcks);
      // Else, adding the new ids to the stored ones:
      else
        storedAcks.addIds(newAcks.getIds());
    }
  }
}
