/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * The present code contributor is ScalAgent Distributed Technologies.
 */
package fr.dyade.aaa.joram;

import fr.dyade.aaa.mom.jms.*;

import java.util.*;

import javax.jms.JMSException;
import javax.jms.IllegalStateException;
import javax.jms.TransactionInProgressException;
import javax.transaction.xa.*;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * Implements the <code>javax.jms.XASession</code> interface.
 * <p>
 * An XA session actually extends the behaviour of a normal session by
 * providing an XA resource representing it to a Transaction Manager, so that
 * it is part of a distributed transaction.
 */
public abstract class XASession extends Session implements javax.jms.XASession
{
  /** The XA resource representing the session to the transaction manager. */
  private XAResource xaResource;
  /**
   * The table of transaction contexts the session is involved in.
   * <p>
   * <b>Key:</b> transaction id<br>
   * <b>Object:</b> <code>XAContext</code> instance
   */
  protected Hashtable transactionsTable;


  /**
   * Constructs an <code>XASession</code>.
   *
   * @param ident  Identifier of the session.
   * @param cnx  The connection the session belongs to.
   *
   * @exception JMSException  Actually never thrown.
   */
  XASession(String ident, Connection cnx) throws JMSException
  {
    super(ident, cnx, true, 0);
    xaResource = new XAResource(this);
    transactionsTable = new Hashtable();

    // This session's resources are not used by XA sessions:
    consumers = null;
    producers = null;
    sendings = null;
    deliveries = null;
  }
 
 
  /** API method. */  
  public javax.transaction.xa.XAResource getXAResource()
  {
    return xaResource;
  }

  /**
   * API method. 
   *
   * @exception IllegalStateException  If the session is closed.
   */
  public boolean getTransacted() throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");
    return true;
  }

  /**
   * API method inherited from session, but intercepted here for
   * forbidding its use in the XA context (as defined by the API).
   *
   * @exception TransactionInProgressException  Systematically thrown.
   */
  public void commit() throws JMSException
  {
    throw new TransactionInProgressException("Forbidden call on an XA"
                                             + " session.");
  }

  /**
   * API method inherited from session, but intercepted here for
   * forbidding its use in the XA context (as defined by the API).
   *
   * @exception TransactionInProgressException  Systematically thrown.
   */
  public void rollback() throws JMSException
  {
    throw new TransactionInProgressException("Forbidden call on an XA"
                                             + " session.");
  }

  /**
   * API method inherited from session, but intercepted here for
   * adapting its behaviour to the XA context.
   *
   * @exception JMSException  Actually never thrown.
   */
  public abstract void close() throws JMSException;

  
  /** 
   * API method inherited from session, but intercepted here for
   * adapting its behaviour to the XA context.
   */
  public abstract void run();

  /** 
   * This abstract method inherited from session is empty and does nothing.
   * <p>
   * Its purpose is to force the <code>QueueSession</code> and
   * <code>TopicSession</code> classes to specifically take care of
   * acknowledging the consumed messages. The messages consumed by an XA
   * session are consumed through the wrapped session, and acknowledgement
   * is not done through the same mechanism.
   */
  void acknowledge() throws IllegalStateException
  {}

  /** 
   * This abstract method inherited from session is empty and does nothing.
   * <p>
   * Its purpose is to force the <code>QueueSession</code> and
   * <code>TopicSession</code> classes to specifically take care of
   * denying the consumed messages. The messages consumed by an XA
   * session are consumed through the wrapped session, and denying
   * is not done through the same mechanism.
   */
  void deny()
  {}

  /** 
   * This abstract method inherited from session is empty and does nothing.
   * <p>
   * Its purpose is to force the <code>QueueSession</code> and
   * <code>TopicSession</code> classes to specifically take care of
   * distributing the asynchronous deliveries destinated to their consumers.
   * An XA session actually does not directly manage consumers, this is done
   * by the session it wraps.
   */
  void distribute(AbstractJmsReply reply)
  {}
   
 
  /** 
   * Method called by the XA resource to involve the session in a given
   * transaction.
   *
   * @exception XAException  If the session is already involved in this
   *              transaction. 
   */
  void getInvolvedIn(Xid xid) throws XAException
  {
    if (transactionsTable.containsKey(xid))
      throw new XAException("Resource already involved in specified"
                            + " transaction.");

    transactionsTable.put(xid, new XAContext());

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + this
                                 + ": involved in transaction "
                                 + xid.toString()); 
  }

  /** 
   * Method called by the XA resource when a transaction has either
   * committed or rolledback, and is therefor terminated.
   */
  void removeTransaction(Xid xid)
  {
    transactionsTable.remove(xid);

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + this
                                 + ": terminated transaction "
                                 + xid.toString()); 
  }

  /** 
   * Method called by the XA resource to set the status of a given transaction.
   *
   * @exception XAException  If the session is not involved in this
   *              transaction. 
   */
  void setTransactionStatus(Xid xid, int status) throws XAException
  {
    XAContext xaC = (XAContext) transactionsTable.get(xid);

    if (xaC == null)
      throw new XAException("Resource is not involved in specified"
                            + " transaction.");

    xaC.status = status;
  }

  /** 
   * Method called by the XA resource to get the status of a given transaction.
   *
   * @exception XAException  If the session is not involved in this
   *              transaction. 
   */
  int getTransactionStatus(Xid xid) throws XAException
  {
    XAContext xaC = (XAContext) transactionsTable.get(xid);

    if (xaC == null)
      throw new XAException("Resource is not involved in specified"
                            + " transaction.");

    return xaC.status;
  }

  /** 
   * The purpose of this abstract method is to force the
   * <code>XAQueueSession</code> and <code>XATopicSession</code> classes to
   * specifically react to their delisting from the transaction.
   *
   * @exception XAException  If the session is not involved with this
   *              transaction. 
   */
  abstract void saveTransaction(Xid xid) throws XAException;

  /** 
   * Method called by the XA resource when it is enlisted again to a given
   * transaction.
   *
   * @exception XAException  If the session is not involved with this
   *              transaction. 
   */
  void resumeTransaction(Xid xid) throws XAException
  {
    XAContext xaC = (XAContext) transactionsTable.get(xid);

    if (xaC == null)
      throw new XAException("Resource is not involved in specified"
                            + " transaction.");
  }

  /** 
   * The purpose of this abstract method is to force the
   * <code>XAQueueSession</code> and <code>XATopicSession</code> classes to
   * specifically react to a preparing transaction.
   *
   * @exception XAException  If the session is not involved with this
   *              transaction.
   * @exception JMSException If the prepare failed because of the
   *              Joram server.
   */
  abstract void prepareTransaction(Xid xid) throws Exception;

  /** 
   * Method called by the XA resource when the transaction commits.
   *
   * @exception XAException  If the session is not involved with this
   *              transaction.
   * @exception JMSException  If the commit fails because of Joram server.
   */
  void commitTransaction(Xid xid) throws Exception
  {
    XAContext xaC = (XAContext) transactionsTable.get(xid);

    if (xaC == null)
      throw new XAException("Resource is not involved in specified"
                            + " transaction.");
    
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + this
                                 + ": commits transaction "
                                 + xid.toString()); 

    cnx.syncRequest(new XASessCommit(ident + " " + xid.toString())); 
  }

  /** 
   * Method called by the XA resource when the transaction rolls back.
   *
   * @exception XAException  If the session is not involved with this
   *              transaction.
   * @exception JMSException  If the rollback fails because of Joram server.
   */
  void rollTransactionBack(Xid xid) throws Exception
  {
    XAContext xaC = (XAContext) transactionsTable.get(xid);

    if (xaC == null)
      throw new XAException("Resource is not involved in specified"
                            + " transaction.");

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + this
                                 + ": rolls transaction "
                                 + xid.toString() + " back."); 

    cnx.syncRequest(new XASessRollback(ident + " " + xid.toString())); 
  }

  /**
   * Returns an array of the identifiers of the prepared transactions the
   * session is involved in.
   */
  Xid[] getPreparedTransactions()
  {
    Enumeration keys = transactionsTable.keys();
    Xid key;
    XAContext xaC;
    Vector ids = new Vector();
    while (keys.hasMoreElements()) {
      key = (Xid) keys.nextElement();
      xaC = (XAContext) transactionsTable.get(key);
      if (xaC.status == XAResource.PREPARED)
        ids.add(key);
    }
    Xid[] array = new Xid[ids.size()];
    ids.copyInto(array);
    return array;
  }
}
