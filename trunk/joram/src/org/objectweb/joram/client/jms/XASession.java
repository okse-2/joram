/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Contributor(s): Nicolas Tachker (Bull SA)
 */
package org.objectweb.joram.client.jms;

import org.objectweb.joram.shared.client.*;

import javax.jms.JMSException;
import javax.jms.IllegalStateException;
import javax.jms.TransactionInProgressException;

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
  private javax.transaction.xa.XAResource xaResource;
  
  /**
   * An XA Session actually wraps what looks like a "normal" session object.
   */
  protected Session sess;


  /**
   * Constructs an <code>XASession</code>.
   *
   * @param cnx  The connection the session belongs to.
   * @param rm   The resource manager.
   *
   * @exception JMSException  Actually never thrown.
   */
  XASession(Connection cnx, XAResourceMngr rm) throws JMSException
  {
    super(cnx, true, 0);
    sess = new Session(cnx, true, 0);
    // The wrapped session is removed from the connection's list, as it
    // is to be only seen by the wrapping XA session.
    cnx.sessions.remove(sess);

    xaResource = new XAResource(rm, sess);

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
   * @param cnx   The connection the session belongs to.
   * @param sess  The wrapped "regular" session.
   * @param rm    The resource manager.
   *
   * @exception JMSException  Actually never thrown.
   */
  public XASession(Connection cnx, Session sess, XAResourceMngr rm)
    throws JMSException
  {
    super(cnx, true, 0);
    this.sess = sess;
    // The wrapped session is removed from the connection's list, as it
    // is to be only seen by the wrapping XA session.
    cnx.sessions.remove(sess);

    xaResource = new XAResource(rm, sess);

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
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.QueueBrowser
         createBrowser(javax.jms.Queue queue, String selector)
         throws JMSException
  {
    return sess.createBrowser(queue, selector);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.QueueBrowser createBrowser(javax.jms.Queue queue)
         throws JMSException
  {
    return sess.createBrowser(queue);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.MessageProducer createProducer(javax.jms.Destination dest)
         throws JMSException
  {
    return sess.createProducer(dest);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.MessageConsumer
         createConsumer(javax.jms.Destination dest,
                        String selector,
                        boolean noLocal)
         throws JMSException
  {
    return sess.createConsumer(dest, selector, noLocal);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.MessageConsumer
         createConsumer(javax.jms.Destination dest, String selector)
         throws JMSException
  {
    return sess.createConsumer(dest, selector);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.MessageConsumer createConsumer(javax.jms.Destination dest)
         throws JMSException
  {
    return sess.createConsumer(dest);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.TopicSubscriber
         createDurableSubscriber(javax.jms.Topic topic,
                                 String name,
                                 String selector,
                                 boolean noLocal)
         throws JMSException
  {
    return sess.createDurableSubscriber(topic, name, selector, noLocal);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public javax.jms.TopicSubscriber
         createDurableSubscriber(javax.jms.Topic topic, String name)
         throws JMSException
  {
    return sess.createDurableSubscriber(topic, name);
  }

  /**
   * API method inherited from session, but intercepted here for
   * forbidding its use in the XA context (as defined by the API).
   *
   * @exception IllegalStateException  Systematically thrown.
   */
  public void commit() throws JMSException
  {
    throw new IllegalStateException("Forbidden call on an XA session.");
  }

  /**
   * API method inherited from session, but intercepted here for
   * forbidding its use in the XA context (as defined by the API).
   *
   * @exception IllegalStateException  Systematically thrown.
   */
  public void rollback() throws JMSException
  {
    throw new IllegalStateException("Forbidden call on an XA session.");
  }

  /**
   * API method inherited from session, but intercepted here for
   * forbidding its use in the XA context (as defined by the API).
   *
   * @exception IllegalStateException  Systematically thrown.
   */
  public void recover() throws JMSException
  {
    throw new IllegalStateException("Forbidden call on an XA session.");
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

    // Emptying the current pending deliveries:
    try {
      sess.repliesIn.stop();
    }
    catch (InterruptedException iE) {}

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
}
