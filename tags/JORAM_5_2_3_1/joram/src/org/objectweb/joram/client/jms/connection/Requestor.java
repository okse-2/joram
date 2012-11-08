/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2009 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.client.jms.connection;

import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;

import org.objectweb.joram.shared.client.AbstractJmsReply;
import org.objectweb.joram.shared.client.AbstractJmsRequest;
import org.objectweb.joram.shared.client.ConsumerMessages;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Debug;

public class Requestor implements ReplyListener, ErrorListener {

  private static class Status {
    
    /**
     * The requestor is free: it can be called by a client thread.
     */
    public static final int INIT = 0;

    /**
     * The requestor is busy: the client thread is waiting.
     * Two threads can make a call:
     *   1- the demultiplexer thread can call replyReceived and replyAborted.
     *   2- another client thread can abort the request.
     */
    public static final int RUN = 1;

    /**
     * The requestor is either completed (by the demultiplxer thread) or
     * aborted (by another client thread  or a timeout).
     * This state is transitional. It enables the requesting client thread to
     * finalize its request.
     */
    public static final int DONE = 2;

    public static final int CLOSE = 3;

    private static final String[] names = {
      "INIT", "RUN", "DONE", "CLOSE"};

    public static String toString(int status) {
      return names[status];
    }
  }
  
  private static Logger logger = Debug.getLogger(Requestor.class.getName());

  public static final String DEFAULT_REQUEST_TIMEOUT_PROPERTY = "org.objectweb.joram.client.jms.connection.Requestor.defaultRequestTimeout";

  public static final long DEFAULT_REQUEST_TIMEOUT_VALUE = 0;

  private long defaultRequestTimeout;

  private RequestMultiplexer mtpx;

  private Object reply;

  private int requestId;

  private int status;

  public Requestor(RequestMultiplexer mtpx) {
    this.mtpx = mtpx;
    init();
  }

  private void setStatus(int status) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Requestor.setStatus(" + Status.toString(status) + ')');
    this.status = status;
  }

  public final synchronized int getRequestId() {
    return requestId;
  }

  private void init() {
   // set the default request timeout
   defaultRequestTimeout = Long.getLong(DEFAULT_REQUEST_TIMEOUT_PROPERTY,
                                        DEFAULT_REQUEST_TIMEOUT_VALUE).longValue();
    if (status == Status.DONE) {
      setStatus(Status.INIT);
      reply = null;
      requestId = -1;
    }
    // Else the requestor can be closed.
    // Nothing to do.
  }

  public synchronized AbstractJmsReply request(
    AbstractJmsRequest request) 
    throws JMSException {
    return request(request, defaultRequestTimeout);
  }

  /**
   * Method sending a synchronous request to the server and waiting for an
   * answer.
   *
   * @exception IllegalStateException  If the connection is closed or broken,
   *                                   if the server state does not allow to
   *                                   process the request.
   * @exception JMSSecurityException  When sending a request to a destination
   *              not accessible because of security.
   * @exception InvalidDestinationException  When sending a request to a
   *              destination that no longer exists.
   * @exception JMSException  If the request failed for any other reason.
   */
  public synchronized AbstractJmsReply request(AbstractJmsRequest request, long timeout) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Requestor.request(" + request + ',' + timeout + ')');

    if (status != Status.INIT) {
      if (status == Status.CLOSE) {
        // throw new javax.jms.IllegalStateException("Closed requestor");
        return null;
      } else {
        throw new javax.jms.IllegalStateException("Requestor already used");
      }
    }
    mtpx.sendRequest(request, this);
    setStatus(Status.RUN);
    requestId = request.getRequestId();
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, " -> request #" + requestId + " wait");
    
    try {
      wait(timeout);
    } catch (InterruptedException exc) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, "", exc);
      setStatus(Status.DONE);
    }

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, " -> request #" + requestId + " awake");
    
    try {
      if (status == Status.RUN) {
        // Means that the wait ended with a timeout.
        // Abort the request.
        mtpx.abortRequest(requestId);
        return null;
      } else if (status == Status.CLOSE) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, " -> deny " + reply);
        if (reply instanceof ConsumerMessages) {
          mtpx.deny((ConsumerMessages)reply);
        }
        return null;
      } else if (status == Status.DONE) {
        // Status
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, " -> request #" + requestId + " done");      
        if (reply instanceof AbstractJmsReply) {
          return (AbstractJmsReply)reply;
        } else if (reply instanceof JMSException) {
          throw (JMSException)reply;
        } else {
          // Reply aborted or thread interrupted.
          return null;
        }
      } else throw new Error();
    } finally {
      init();
    }
  }

  public synchronized boolean replyReceived(AbstractJmsReply reply) throws AbortedRequestException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Requestor.replyReceived(" + reply + ')');

    if (status == Status.RUN &&
        reply.getCorrelationId() == requestId) {      
      this.reply = reply;
      setStatus(Status.DONE);
      notify();
      return true;
    } else {
      // The request has been aborted.
      throw new AbortedRequestException();
    }
  }

  public synchronized void errorReceived(int replyId, JMSException exc) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Requestor.errorReceived(" + replyId + ',' + exc + ')');
    
    if (status == Status.RUN &&
        replyId == requestId) {
      reply = exc;
      setStatus(Status.DONE);
      notify();
    } 
    // Else The request has been aborted.
    // Do nothing
  }
  
  public synchronized void replyAborted(int replyId) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Requestor.replyAborted(" + replyId + ')');
    if (status == Status.RUN &&
        replyId == requestId) {
      reply = null;
      setStatus(Status.DONE);
      notify();
    }
    // Else the request has been aborted.
    // Do nothing
  }

  public synchronized void abortRequest() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Requestor[" + Status.toString(status) + ',' + requestId
          + "].abortRequest()");
    if (status == Status.RUN && requestId > 0) {
      mtpx.abortRequest(requestId);
      setStatus(Status.DONE);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> notify requestor");
      notify();
    }
    // Else the request has been completed.
    // Do nothing
  }

  public synchronized void close() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Requestor.close()");
    if (status != Status.CLOSE) {
      abortRequest();
      setStatus(Status.CLOSE);
    }
    // Else idempotent.
  }
}
