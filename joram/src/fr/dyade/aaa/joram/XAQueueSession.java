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
import javax.transaction.xa.*;

import org.objectweb.monolog.api.BasicLevel;

/**
 * Implements the <code>javax.jms.XAQueueSession</code> interface.
 * <p>
 * An XA QueueSession actually wraps what looks like a "normal" QueueSession
 * object. This object takes care of producing and consuming messages, the
 * actual sendings and acknowledgement being managed by this XA wrapper.
 */
public class XAQueueSession extends XASession 
                            implements javax.jms.XAQueueSession
{
  /**
   * An XA QueueSession actually wraps what looks like a "normal"
   * session object.
   */
  private QueueSession qs;

    
  /**
   * Constructs an <code>XAQueueSession</code> instance.
   *
   * @param ident  Identifier of the session.
   * @param cnx  The connection the session belongs to.
   *
   * @exception JMSException  Actually never thrown.
   */
  XAQueueSession(String ident, XAQueueConnection cnx) throws JMSException
  {
    super(ident, cnx);
    qs = new QueueSession(ident, cnx, true, 0);

    // The wrapped session is removed from the connection's list, as it
    // is to be only seen by the wrapping XA session.
    cnx.sessions.remove(qs);
  }

  
  /** Returns a String image of this session. */
  public String toString()
  {
    return "XAQueueSess:" + ident;
  }


  /** API method. */ 
  public javax.jms.QueueSession getQueueSession() throws JMSException
  {
    return qs;
  }

  /** 
   * This method inherited from the <code>XASession</code> class processes
   * the asynchronous deliveries coming from a connection consumer.
   * <p>
   * These deliveries are actually handed to the wrapped session.
   */
  public void run()
  {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + this
                                 + ": running...");
    qs.messageListener = super.messageListener;
    qs.connectionConsumer = super.connectionConsumer;
    qs.repliesIn = super.repliesIn;
    qs.run();
    super.repliesIn.removeAllElements();
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": runned.");
  }

  /**
   * Method basically inherited from session, but intercepted here for
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
    qs.stop();

    // Closing the wrapped session's resources:
    while (! qs.consumers.isEmpty())
      ((MessageConsumer) qs.consumers.get(0)).close();
    while (! qs.producers.isEmpty())
      ((MessageProducer) qs.producers.get(0)).close();

    qs.closed = true;

    cnx.sessions.remove(this);

    closed = true;

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": closed."); 
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
    
    xaC.addSendings(qs.sendings);
    xaC.addDeliveries(qs.deliveries);
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

    Enumeration dests;    
    String dest;
    Vector pMs = new Vector();
    Vector ids;
    Vector acks = new Vector();

    // Getting all the ProducerMessages to send:
    dests = xaC.sendings.keys();
    while (dests.hasMoreElements()) {
      dest = (String) dests.nextElement();
      pMs.add(xaC.sendings.remove(dest));
    }

    // Getting all the QSessAckRequest to send:
    dests = xaC.deliveries.keys();
    while (dests.hasMoreElements()) {
      dest = (String) dests.nextElement();
      ids = (Vector) xaC.deliveries.remove(dest);
      acks.add(new QSessAckRequest(dest, ids));
    }

    // Sending to the proxy:
    qs.cnx.syncRequest(new XAQSessPrepare(ident + " " + xid.toString(),
                                          pMs, acks));
  }
}
