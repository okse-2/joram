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
 * it is part of a distributed transaction. The XASession wraps what looks like
 * a "normal"Session object. This object takes care of producing and
 * consuming messages, the actual sendings and acknowledgement being managed
 * by this XA wrapper.
 */
public class XASession extends Session implements javax.jms.XASession
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
   * An XA Session actually wraps what looks like a "normal" session object.
   */
  protected Session sess;


  /**
   * Constructs an <code>XASession</code>.
   *
   * @param cnx  The connection the session belongs to.
   *
   * @exception JMSException  Actually never thrown.
   */
  XASession(Connection cnx) throws JMSException
  {
    super(cnx, true, 0);
    sess = new Session(cnx, true, 0);
    // The wrapped session is removed from the connection's list, as it
    // is to be only seen by the wrapping XA session.
    cnx.sessions.remove(sess);

    xaResource = new XAResource(this);
    transactionsTable = new Hashtable();

    // This session's resources are not used by XA sessions:
    consumers = null;
    producers = null;
    sendings = null;
    deliveries = null;
  }

  /**
   * Constructs an <code>XASession</code>.
   * <p>
   * This constructor is called by subclasses.
   *
   * @param cnx  The connection the session belongs to.
   * @param sess  The wrapped "regular" session.
   *
   * @exception JMSException  Actually never thrown.
   */
  XASession(Connection cnx, Session sess) throws JMSException
  {
    super(cnx, true, 0);
    this.sess = sess;
    // The wrapped session is removed from the connection's list, as it
    // is to be only seen by the wrapping XA session.
    cnx.sessions.remove(sess);

    xaResource = new XAResource(this);
    transactionsTable = new Hashtable();

    // This session's resources are not used by XA sessions:
    consumers = null;
    producers = null;
    sendings = null;
    deliveries = null;
  }

   /** Returns a String image of this session. */
  public String toString()
  {
    return "XASess:" + ident;
  }

  
  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed.
   */
  public javax.jms.Session getSession() throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed session.");

    return sess;
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
  public void close() throws JMSException
  {
    // Ignoring the call if the session is already closed:
    if (closed)
      return;

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "---" + this
                                 + ": closing..."); 

    // Stopping the wrapped session:
    sess.stop();

    // Closing the wrapped session's resources:
    while (! sess.consumers.isEmpty())
      ((MessageConsumer) sess.consumers.get(0)).close();
    while (! sess.producers.isEmpty())
      ((MessageProducer) sess.producers.get(0)).close();

    sess.closed = true;

    cnx.sessions.remove(this);

    closed = true;

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": closed."); 
  }

  
  /** 
   * API method inherited from session, but intercepted here for
   * adapting its behaviour to the XA context.
   * <p>
   * This method processes asynchronous deliveries coming from a connection
   * consumer by passing them to the wrapped session.
   */
  public void run()
  {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + this
                                 + ": running...");
    sess.messageListener = super.messageListener;
    sess.connectionConsumer = super.connectionConsumer;
    sess.repliesIn = super.repliesIn;
    sess.run();
    super.repliesIn.removeAllElements();
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": runned.");
  }

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
   * This method is called by the wrapped <code>XAResource</code> for saving
   * the "state" of the wrapped session for later modifying it or commiting it.
   * <p>
   * The word "state" actually means the messages produced by the session's
   * producers, and the acknowledgements due to its consumers.
   *
   * @exception XAException  If the session is not involved with this
   *              transaction. 
   */
  void saveTransaction(Xid xid) throws XAException
  {
    XAContext xaC = (XAContext) transactionsTable.get(xid);

    if (xaC == null)
      throw new XAException("Resource is not involved in specified"
                            + " transaction.");

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + this
                                 + ": saves transaction "
                                 + xid.toString()); 
    
    xaC.addSendings(sess.sendings);
    xaC.addDeliveries(sess.deliveries);
  }

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
   * This method is called by the wrapped <code>XAResource</code> for
   * preparing the session by sending the corresponding messages and
   * acknowledgements previously saved.
   *
   * @exception XAException  If the session is not involved with this
   *              transaction.
   * @exception JMSException If the prepare failed because of the
   *              Joram server.
   */
  void prepareTransaction(Xid xid) throws Exception
  {
    XAContext xaC = (XAContext) transactionsTable.get(xid);

    if (xaC == null)
      throw new XAException("Resource is not involved in specified"
                            + " transaction.");

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + this
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
    sess.cnx.syncRequest(new XASessPrepare(ident + " " + xid.toString(),
                                           pMs, sessAcks));
  }

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
  void rollbackTransaction(Xid xid) throws Exception
  {
    XAContext xaC = (XAContext) transactionsTable.get(xid);

    if (xaC == null)
      throw new XAException("Resource is not involved in specified"
                            + " transaction.");

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + this
                                 + ": rolls back transaction "
                                 + xid.toString()); 

    Enumeration targets; 
    String target;
    MessageAcks acks;

    XASessRollback rollbackRequest;

    targets = xaC.deliveries.keys();

    rollbackRequest = new XASessRollback(ident + " " + xid.toString());

    while (targets.hasMoreElements()) {
      target = (String) targets.nextElement();
      acks = (MessageAcks) xaC.deliveries.remove(target);
      rollbackRequest.add(target, acks.getIds(), acks.getQueueMode());
    }

    // Sending to the proxy:
    sess.cnx.syncRequest(rollbackRequest);
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
