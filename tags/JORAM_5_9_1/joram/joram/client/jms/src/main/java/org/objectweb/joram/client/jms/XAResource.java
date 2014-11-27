/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
 * Copyright (C) 2004 Bull SA
 * Copyright (C) 1996 - 2000 Dyade
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

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * A <code>XAResource</code> instance is used by a <code>XASession</code> 
 * instance as a delegate to a Transaction Manager.
 */
public class XAResource implements javax.transaction.xa.XAResource {
  /** <code>true</code> if the resource is enlisted in a transaction. */
  private boolean enlisted = false;
  /** The current transaction identifier. */
  private Xid currentXid = null;

  /** The XA connection acting as resource manager. */
  XAResourceMngr rm;

  /** The session producing and consuming messages. */
  Session sess;

  private static Logger logger = Debug.getLogger(XAResource.class.getName());

  /**
   * Constructs an XA resource representing a given session.
   */
  public XAResource(XAResourceMngr rm, Session sess) {
    this.rm = rm;
    this.sess = sess;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 " XAResource rm = " + rm + ", sess = " + sess);
  }

  /**
   * Enlists this resource in a given transaction.
   *
   * @exception XAException  If the resource is already enlisted in a
   *                         transaction, or if the RM fails to enlist the
   *                         resource.
   */
  public void start(Xid xid, int flag) 
  throws XAException {
    if (enlisted)
      throw new XAException("Resource already enlisted.");

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 this + ": start(" + xid + 
                 ", " + flag + ")");

    rm.start(xid, flag, sess);

    enlisted = true;
    currentXid = xid;
  }

  /**
   * Delists this resource.
   *
   * @exception XAException  If the resource is not enlisted in the specified
   *                         transaction, or if the RM fails to delist the
   *                         resource.
   */
  public void end(Xid xid, int flag)
  throws XAException {
    if (! enlisted || ! xid.equals(currentXid))
      throw new XAException("Resource is not enlisted in specified"
                            + " transaction.");

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 this + ": end(" + xid + 
                 ", " + flag + ")");

    rm.end(xid, flag, sess);

    enlisted = false;
    currentXid = null;
  }

  /**
   * Prepares the resource.
   *
   * @exception XAException  If the RM fails to prepare the resource.
   */
  public int prepare(Xid xid) 
  throws XAException {

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 this + ": prepare(" + xid + ")");
    rm.prepare(xid);
    return XA_OK;
  }

  /**
   * Commits the resource.
   *
   * @exception XAException  If the RM fails to commit the resource.
   */
  public void commit(Xid xid, boolean onePhase) 
  throws XAException {

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 this + ": commit(" + xid + 
                 ", " + onePhase + ")");

    if (onePhase)
      rm.prepare(xid);

    rm.commit(xid);
  }

  /**
   * Rolls the resource back.
   *
   * @exception XAException  If the RM fails to roll the resource back.
   */
  public void rollback(Xid xid) 
  throws XAException {

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 this + ": rollback(" + xid + ")");

    if (enlisted && currentXid.equals(xid)) {
      rm.end(xid, javax.transaction.xa.XAResource.TMFAIL, sess);
      enlisted = false;
      currentXid = null;
    }

    rm.rollback(xid);
  }

  /** 
   * Recovers the prepared transactions identifiers.
   *
   * @exception XAException  If the RM fails to recover.
   */
  public Xid[] recover(int flag) throws XAException {
    return rm.recover(flag);
  }

  /** 
   * Not implemented as transactions are not heuristically completed.
   *
   * @exception XAException  Always thrown.
   */
  public void forget(Xid xid) throws XAException
  {
    throw new XAException("Non implemented method.");
  }

  /**
   * Returns <code>false</code> as timeout feaure is not supported.
   *
   * @exception XAException  Never thrown.
   */
  public boolean setTransactionTimeout(int seconds) throws XAException
  {
    return false;
  }

  /**
   * Returns 0 as timeout feaure is not supported.
   *
   * @exception XAException  Never thrown.
   */
  public int getTransactionTimeout() throws XAException
  {
    return 0;
  }

  /** 
   * Checks wether this resource shares the same resource manager
   * (XAConnection) with an other resource.
   *
   * @exception XAException  Never thrown.
   */
  public boolean isSameRM(javax.transaction.xa.XAResource o)
  throws XAException {

    if (! (o instanceof org.objectweb.joram.client.jms.XAResource))
      return false;

    XAResource other = (org.objectweb.joram.client.jms.XAResource) o;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 this + ": isSameRM  other.rm = " + other.rm + 
                 ", this.rm = " + this.rm +
                 ", equals = " + this.rm.equals(other.rm));

    return this.rm.equals(other.rm);
  }
}
