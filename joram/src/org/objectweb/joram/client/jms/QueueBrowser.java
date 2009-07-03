/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2006 ScalAgent Distributed Technologies
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

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.jms.IllegalStateException;
import javax.jms.InvalidDestinationException;
import javax.jms.InvalidSelectorException;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;

import org.objectweb.joram.shared.client.QBrowseReply;
import org.objectweb.joram.shared.client.QBrowseRequest;
import org.objectweb.joram.shared.selectors.ClientSelector;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * Implements the <code>javax.jms.QueueBrowser</code> interface.
 */
public class QueueBrowser implements javax.jms.QueueBrowser {
  /** The session the browser belongs to. */
  private Session sess;

  /** The queue the browser browses. */
  private Queue queue;

  /** The selector for filtering messages. */
  private String selector;

  /** <code>true</code> if the browser is closed. */
  private boolean closed = false;

  private static Logger logger = Debug.getLogger(QueueBrowser.class.getName());

  /**
   * Constructs a browser.
   *
   * @param sess  The session the browser belongs to.
   * @param queue  The queue the browser browses.
   * @param selector  The selector for filtering messages.
   *
   * @exception InvalidDestinationException if an invalid destination is specified.
   * @exception InvalidSelectorException  If the selector syntax is invalid.
   * @exception IllegalStateException  If the connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  QueueBrowser(Session sess, Queue queue, String selector) throws JMSException {
    if (queue == null)
      throw new InvalidDestinationException("Invalid queue: " + queue);
    queue.check();

    try {
      ClientSelector.checks(selector);
    } catch (org.objectweb.joram.shared.excepts.SelectorException sE) {
      throw new InvalidSelectorException("Invalid selector syntax: " + sE);
    }

    this.sess = sess;
    this.queue = queue;
    this.selector = selector;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + ": created.");
  }

  /** Returns a string view of this browser. */
  public String toString() {
    return "QueueBrowser:" + sess.getId();
  }

  /** 
   * API method.
   *
   * @exception IllegalStateException  If the browser is closed.
   */
  public synchronized javax.jms.Queue getQueue() throws JMSException {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed browser.");

    return queue;
  }

  /** 
   * API method.
   *
   * @exception IllegalStateException  If the browser is closed.
   */
  public synchronized String getMessageSelector() throws JMSException {
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
  public synchronized Enumeration getEnumeration() throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 this + ": requests an enumeration.");

    if (closed)
      throw new IllegalStateException("Forbidden call on a closed browser.");

    // Sending a "browse" request:
    QBrowseRequest browReq = new QBrowseRequest(queue.getName(), selector);
    // Expecting an answer:
    QBrowseReply reply = (QBrowseReply) sess.syncRequest(browReq);

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 this + ": received an enumeration.");

    // Return an enumeration:
    return new QueueEnumeration(reply.getMessages());
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public synchronized void close() throws JMSException {
    // Ignoring the call if the browser is already closed:
    if (closed)
      return;

    sess.closeBrowser(this);
    closed = true;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " closed.");
  }

  /**
   * The <code>QueueEnumeration</code> class is used to enumerate the browses
   * sent by queues.
   */
  private class QueueEnumeration implements java.util.Enumeration {
    /** The vector of messages. */
    private Vector messages;

    /**
     * Constructs a <code>QueueEnumeration</code> instance.
     *
     * @param messages  The vector of messages to enumerate.
     */
    private QueueEnumeration(Vector messages) {
      this.messages = messages;
    }

    /** API method. */
    public boolean hasMoreElements() {
      if (messages == null)
        return false;
      return (! messages.isEmpty());
    }

    /** API method. */
    public Object nextElement() {
      if (messages == null || messages.isEmpty())
        throw new NoSuchElementException();

      Message jmsMsg = null;
      org.objectweb.joram.shared.messages.Message msg = null;
      try {
        msg = (org.objectweb.joram.shared.messages.Message) messages.remove(0);
        jmsMsg = Message.wrapMomMessage(null, msg);
      } catch (JMSException exc) {
        logger.log(BasicLevel.ERROR,
                   this + ", bad message: " + msg, exc);
      }
      return jmsMsg;
    }
  }
}
