/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
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

import javax.jms.InvalidSelectorException;
import javax.jms.InvalidDestinationException;
import javax.jms.IllegalStateException;
import javax.jms.JMSException;

import org.objectweb.monolog.api.BasicLevel;

/**
 * Implements the <code>javax.jms.QueueBrowser</code> interface.
 */
public class QueueBrowser implements javax.jms.QueueBrowser
{
  /** The session the browser belongs to. */
  private QueueSession sess;
  /** The queue the browser browses. */
  private Queue queue;
  /** The selector for filtering messages. */
  private String selector;
  /** <code>true</code> if the browser is closed. */
  private boolean closed = false;

  /**
   * Constructs a browser.
   *
   * @param sess  The session the browser belongs to.
   * @param queue  The queue the browser browses.
   * @param selector  The selector for filtering messages.
   *
   * @exception InvalidSelectorException  If the selector syntax is invalid.
   * @exception JMSSecurityException  If the client is not a READER on the
   *              queue.
   * @exception IllegalStateException  If the connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  QueueBrowser(QueueSession sess, Queue queue,
               String selector) throws JMSException
  {
    if (queue == null)
      throw new InvalidDestinationException("Invalid queue: " + queue);

    try {
      fr.dyade.aaa.mom.selectors.Selector.checks(selector);
    }
    catch (fr.dyade.aaa.mom.excepts.SelectorException sE) {
      throw new InvalidSelectorException("Invalid selector syntax: " + sE);
    }

    // Checking the user's access permission:
    sess.cnx.isReader(queue.getName());

    this.sess = sess;
    this.queue = queue;
    this.selector = selector;

    if (sess.browsers == null)
      sess.browsers = new Vector();
    sess.browsers.add(this);

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": created.");
  }

  /** Returns a string view of this browser. */
  public String toString()
  {
    return "QueueBrowser:" + sess.ident;
  }

  /** 
   * API method.
   *
   * @exception IllegalStateException  If the browser is closed.
   */
  public javax.jms.Queue getQueue() throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed browser.");

    return queue;
  }

  /** 
   * API method.
   *
   * @exception IllegalStateException  If the browser is closed.
   */
  public String getMessageSelector() throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed browser.");

    return selector;
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the browser is closed, or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public Enumeration getEnumeration() throws JMSException
  {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this
                                 + ": requests an enumeration.");
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed browser.");

    // Sending a "browse" request:
    QBrowseRequest browReq = new QBrowseRequest(queue.getName(), selector);
    // Expecting an answer:
    QBrowseReply reply = (QBrowseReply) sess.cnx.syncRequest(browReq);

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this
                                 + ": received an enumeration.");

    // Processing the received messages:
    Vector momMessages = reply.getMessages();
    Vector messages = new Vector();
    while (! momMessages.isEmpty())
      messages.add(Message.wrapMomMessage(null,
                                          (fr.dyade.aaa.mom.messages.Message)
                                          momMessages.remove(0)));
    // Return an enumeration:
    return new QueueEnumeration(messages);
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void close() throws JMSException
  {
    // Ignoring the call if the browser is already closed:
    if (closed)
      return;

    sess.browsers.remove(this);
    closed = true;

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + " closed.");
  }

  /**
   * The <code>QueueEnumeration</code> class is used to enumerate the browses
   * sent by queues.
   */
  private class QueueEnumeration implements java.util.Enumeration
  {
    /** The vector of messages. */
    private Vector messages;

    /**
     * Constructs a <code>QueueEnumeration</code> instance.
     *
     * @param messages  The vector of messages to enumerate.
     */
    private QueueEnumeration(Vector messages)
    {
      this.messages = messages;
    }

    /** API method. */
    public boolean hasMoreElements()
    {
      return (! messages.isEmpty());
    }

    /** API method. */
    public Object nextElement()
    {
      if (messages.isEmpty())
        throw new NoSuchElementException();

      return messages.remove(0);
    }
  }
}
