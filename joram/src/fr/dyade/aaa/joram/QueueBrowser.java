/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
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
 * Contributor(s):
 */
package fr.dyade.aaa.joram;

import fr.dyade.aaa.mom.jms.*;

import java.util.*;

import javax.jms.InvalidSelectorException;
import javax.jms.InvalidDestinationException;
import javax.jms.IllegalStateException;
import javax.jms.JMSException;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * Implements the <code>javax.jms.QueueBrowser</code> interface.
 */
public class QueueBrowser implements javax.jms.QueueBrowser
{
  /** The session the browser belongs to. */
  private Session sess;
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
   * @exception IllegalStateException  If the connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  QueueBrowser(Session sess, Queue queue, String selector) throws JMSException
  {
    if (queue == null)
      throw new InvalidDestinationException("Invalid queue: " + queue);

    try {
      fr.dyade.aaa.mom.selectors.Selector.checks(selector);
    }
    catch (fr.dyade.aaa.mom.excepts.SelectorException sE) {
      throw new InvalidSelectorException("Invalid selector syntax: " + sE);
    }

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
   * @exception JMSSecurityException  If the client is not a READER on the
   *              queue.
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
    Vector messages = null;
    if (momMessages != null) {
      messages = new Vector();
      fr.dyade.aaa.mom.messages.Message momMsg;
      for (int i = 0; i < momMessages.size(); i++) {
        momMsg = (fr.dyade.aaa.mom.messages.Message) momMessages.get(i);
        messages.add(Message.wrapMomMessage(null, momMsg));
      }
    }
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
      if (messages == null)
        return false;
      return (! messages.isEmpty());
    }

    /** API method. */
    public Object nextElement()
    {
      if (messages == null || messages.isEmpty())
        throw new NoSuchElementException();

      return messages.remove(0);
    }
  }
}
