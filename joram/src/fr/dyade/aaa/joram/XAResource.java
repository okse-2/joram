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

import javax.jms.JMSException;
import javax.transaction.xa.*;

/**
 * Implements the <code>javax.transaction.xa.XAResource</code> interface.
 * <p>
 * An <code>XAResource</code> instance actually represents an
 * <code>XASession</code> to a transaction manager. It gets the TM orders
 * and in a way forwards them to the session.
 */
public class XAResource implements javax.transaction.xa.XAResource
{
  /** Suspended transaction state: the resource is ready for resuming. */
  public static final int SUSPENDED = 1;
  /** Successful transaction state: the resource is ready for preparing. */
  public static final int SUCCESS = 2;
  /**
   * Failed transaction state: the resource failed while preparing and is
   * waiting for rolling back.
   */
  public static final int ROLLBACK_ONLY = 3;
  /**
   * Prepared transaction state: the resource succeeded to prepare and is
   * waiting for commiting.
   */
  public static final int PREPARED = 4;


  /** The XA session represented to the TM by the resource. */
  private XASession sess;
  /**
   * <code>true</code> if the resource is enlisted in an active transaction.
   */
  private boolean enlisted = false;
  /**
   * The identifier of the transaction the resource is currently enlisted in.
   */
  private javax.transaction.xa.Xid currentXid = null;

 

  /**
   * Constructs an XA resource representing a given XA session.
   *
   * @param sess  The XA session the resource will represent to the TM.
   */
  public XAResource(XASession sess)
  {
    this.sess = sess;
  }


  /**
   * JTA API method: starts or resumes a transaction.
   *
   * @exception XAException  If the resource is not involved in the
   *              transaction, or if its state is invalid, or if the
   *              given flag is incorrect.
   */
  public void start(Xid xid, int flags) throws XAException
  {
    if (enlisted)
      throw new XAException("The resource is already enlisted in an active"
                            + " transaction.");

    // No flags means that the resource is enlisted in a new transaction.
    if (flags == TMNOFLAGS)
      sess.getInvolvedIn(xid);
    // Resume flag means that the resource is enlisted in a known transaction.
    else if (flags == TMRESUME) {
      if (sess.getTransactionStatus(xid) != SUSPENDED)
        throw new XAException("Can't resume a non suspended transaction.");

      sess.resumeTransaction(xid);
    }
    else
      throw new XAException("Invalid flag for enlisting the resource.");

    enlisted = true;
    currentXid = xid;
  }


  /**
   * JTA API method: stops or suspends a transaction.
   *
   * @exception XAException  If the resource is not involved in the
   *              transaction, or if its state is invalid, or if the
   *              given flag is incorrect.
   */
  public void end(Xid xid, int flags) throws XAException
  {
    if (! enlisted || ! xid.equals(currentXid))
      throw new XAException("The resource is not enlisted in the specified"
                            + " transaction.");

    if (flags == TMSUSPEND)
      sess.setTransactionStatus(xid, SUSPENDED);
    else if (flags == TMFAIL)
      sess.setTransactionStatus(xid, ROLLBACK_ONLY);
    else if (flags == TMSUCCESS)
      sess.setTransactionStatus(xid, SUCCESS);
    else
      throw new XAException("Invalid flag for delisting the resource.");

    sess.saveTransaction(xid);
    enlisted = false;
    currentXid = null;
  }


  /**
   * JTA API method: prepares the resource for commit.
   *
   * @exception XAException  If the resource is not involved in the
   *              transaction, or if its state is invalid, or if the prepare
   *              failed because of Joram server.
   */
  public int prepare(Xid xid) throws XAException
  {
    if (sess.getTransactionStatus(xid) != SUCCESS)
      throw new XAException("The transaction state does not allow to prepare"
                            + " the resource.");

    try {
      sess.prepareTransaction(xid);
      sess.setTransactionStatus(xid, PREPARED);
      return XA_OK;
    }
    catch (Exception e) {
      sess.setTransactionStatus(xid, ROLLBACK_ONLY);
      throw new XAException("Exception while preparing the resource: "
                            + e.getMessage());
    }
  }
      

  /**
   * JTA API method: commits the resource.
   *
   * @exception XAException  If the resource is not involved in the
   *              transaction, or if its state is invalid, or if the commit
   *              failed because of Joram server.
   */
  public void commit(Xid xid, boolean onePhase) throws XAException
  {
    if (onePhase) {
      try {
        prepare(xid);
      }
      catch (Exception e) {
        throw new XAException("Exception in one-phase commit: "
                              + e.getMessage());
      }
    }
    if (sess.getTransactionStatus(xid) != PREPARED)
      throw new XAException("The transaction state does not allow to"
                            + " commit the resource.");
    try {
      sess.commitTransaction(xid);
      sess.removeTransaction(xid);
    }
    catch (Exception e2) {
      sess.setTransactionStatus(xid, ROLLBACK_ONLY);
      throw new XAException("Exception while committing the resource: "
                            + e2.getMessage());
    }
  }


  /** 
   * JTA API method: gets the array of the ids of the prepared transactions
   * the resource is involved in.
   *
   * @exception XAException  Actually never thrown.
   */
  public Xid[] recover(int flag) throws XAException
  {
    if (flag == TMSTARTRSCAN || flag == TMENDRSCAN)
      throw new XAException("Non supported flags.");

    return sess.getPreparedTransactions();
  }


  /**
   * JTA API method: rolls the transaction back.
   *
   * @exception XAException  If the resource is not involved in the
   *              transaction.
   */
  public void rollback(Xid xid) throws XAException
  {
    if (enlisted && currentXid.equals(xid)) {
      sess.saveTransaction(xid);
      enlisted = false;
      currentXid = null;
    }

    try {
      sess.rollTransactionBack(xid);
    }
    catch (Exception e) {}

    sess.removeTransaction(xid);
  }


  /** Non implemented JTA API method. */
  public void forget(Xid xid) throws XAException
  {
    throw new XAException("Non implemented method.");
  }

  /** Non implemented JTA API method. */
  public boolean setTransactionTimeout(int seconds) throws XAException
  {
    throw new XAException("Non implemented method.");
  }

  /** Non implemented JTA API method. */
  public int getTransactionTimeout() throws XAException
  {
    throw new XAException("Non implemented method.");
  }

  /** 
   * JTA API method.
   *
   * @exception XAException  Actually never thrown.
   */
  public boolean isSameRM(javax.transaction.xa.XAResource xares)
               throws XAException
  {
    return xares.equals(this);
  }
}
