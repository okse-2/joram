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

import java.util.Vector;

import javax.jms.IllegalStateException;
import javax.jms.InvalidSelectorException;
import javax.jms.JMSException;

import org.objectweb.monolog.api.BasicLevel;

/**
 * Implements the <code>javax.jms.ConnectionConsumer</code> interface.
 */
public abstract class ConnectionConsumer
                    implements javax.jms.ConnectionConsumer
{
  /** The daemon taking care of the asynchronous deliveries distribution. */
  protected fr.dyade.aaa.util.Daemon ccDaemon;
  /** The connection the consumer belongs to. */
  protected Connection cnx;
  /** The selector for filtering messages. */
  protected String selector;
  /** The session pool provided by the application server. */
  protected javax.jms.ServerSessionPool sessionPool;
  /** The maximum number of messages a session may process at once. */
  protected int maxMessages;

  /**
   * The FIFO queue where the connection pushes the asynchronous server
   * deliveries.
   */
  protected fr.dyade.aaa.util.Queue repliesIn;
  /** The current consuming request. */
  protected fr.dyade.aaa.mom.jms.AbstractJmsRequest currentReq = null;
  /** <code>true</code> if the connection consumer is closed. */
  protected boolean closed = false;

  /** The name of the destination the consumer consumes on. */
  String destName;


  /**
   * Constructs a <code>ConnectionConsumer</code>.
   *
   * @param cnx  The connection the consumer belongs to.
   * @param destName  The name of the destination where consuming messages.
   * @param selector  The selector for filtering messages.
   * @param sessionPool  The session pool provided by the application server.
   * @param maxMessages  The maximum number of messages to be passed at once
   *          to a session.
   *
   * @exception InvalidSelectorException  If the selector syntax is wrong.
   * @exception InvalidDestinationException  If the target destination does not
   *              exist.
   * @exception JMSSecurityException  If the user is not a READER on the
   *              destination.
   * @exception JMSException  If one of the parameters is wrong.
   */
  protected ConnectionConsumer(Connection cnx, String destName,
                               String selector,
                               javax.jms.ServerSessionPool sessionPool,
                               int maxMessages) throws JMSException
  {
    try {
      fr.dyade.aaa.mom.selectors.Selector.checks(selector);
    }
    catch (fr.dyade.aaa.mom.excepts.SelectorException sE) {
      throw new InvalidSelectorException("Invalid selector syntax: " + sE);
    }

    if (sessionPool == null)
      throw new JMSException("Invalid ServerSessionPool parameter: "
                             + sessionPool);
    if (maxMessages <= 0)
      throw new JMSException("Invalid maxMessages parameter: "
                             + maxMessages);

    this.cnx = cnx;
    this.destName = destName;
    this.selector = selector;
    this.sessionPool = sessionPool;
    this.maxMessages = maxMessages;

    // Checking the user's access permission:
    cnx.isReader(destName);

    repliesIn = new fr.dyade.aaa.util.Queue();

    if (cnx.cconsumers == null)
      cnx.cconsumers = new Vector();
 
    cnx.cconsumers.add(this);

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": created.");
  }

  /** Returns a string image of the connection consumer. */
  public String toString()
  {
    return "ConnCons:" + cnx.toString();
  }


  /**
   * API method.
   *
   * @exception IllegalStateException  If the ConnectionConsumer is closed.
   */
  public javax.jms.ServerSessionPool getServerSessionPool() throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed"
                                      + " ConnectionConsumer.");
    return sessionPool;
  }


  /** API method, implemented in subclasses. */
  public abstract void close() throws JMSException;
}
